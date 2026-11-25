"""
Experiment Runner for AI Workload Simulator.
Compares 5 Strategies:
1. All-AWS
2. All-GCP
3. All-Azure
4. Greedy Multicloud
5. ILP Multicloud (CP-SAT)

Produces results.csv and summary metrics across 10 random seeds.
"""
import os
import time
import pandas as pd
import numpy as np
from ortools.sat.python import cp_model
from generate_jobs import generate_scenario_jobs
from cost_model import evaluate_option

def load_catalog(prices_path="../data/prices.csv", egress_path="../data/egress.csv"):
    if not os.path.exists(prices_path):
        prices_path = "data/prices.csv"
    if not os.path.exists(egress_path):
        egress_path = "data/egress.csv"

    prices_df = pd.read_csv(prices_path)
    egress_df = pd.read_csv(egress_path)

    egress_map = {}
    for _, row in egress_df.iterrows():
        key = f"{row['from_provider']}:{row['from_region']}->{row['to_provider']}:{row['to_region']}".upper()
        egress_map[key] = float(row["usd_per_gb"])

    return prices_df, egress_map

def run_single_cloud(jobs_df, prices_df, egress_map, provider_name):
    cloud_prices = prices_df[prices_df["provider"].str.upper() == provider_name.upper()]
    total_cost = 0.0
    completed = 0

    for _, job in jobs_df.iterrows():
        best_cost = float("inf")
        for _, p in cloud_prices.iterrows():
            k = f"{job['data_provider']}:{job['data_region']}->{p['provider']}:{p['region']}".upper()
            rate = egress_map.get(k, 0.0)
            res = evaluate_option(p.to_dict(), job.to_dict(), rate)
            if res["feasible"] and res["total_cost"] < best_cost:
                best_cost = res["total_cost"]
        if best_cost < float("inf"):
            total_cost += best_cost
            completed += 1

    hit_rate = (completed / len(jobs_df)) * 100.0 if len(jobs_df) > 0 else 0.0
    return round(total_cost, 2), round(hit_rate, 1)

def run_greedy(jobs_df, prices_df, egress_map):
    total_cost = 0.0
    completed = 0
    compute_tot = 0.0
    egress_tot = 0.0
    interrupt_tot = 0.0

    for _, job in jobs_df.iterrows():
        best = None
        for _, p in prices_df.iterrows():
            k = f"{job['data_provider']}:{job['data_region']}->{p['provider']}:{p['region']}".upper()
            rate = egress_map.get(k, 0.0)
            res = evaluate_option(p.to_dict(), job.to_dict(), rate)
            if res["feasible"] and (best is None or res["total_cost"] < best["total_cost"]):
                best = res
        if best:
            total_cost += best["total_cost"]
            compute_tot += best["compute_cost"]
            egress_tot += best["egress_cost"]
            interrupt_tot += best["interruption_cost"]
            completed += 1

    hit_rate = (completed / len(jobs_df)) * 100.0 if len(jobs_df) > 0 else 0.0
    return round(total_cost, 2), round(hit_rate, 1), round(compute_tot, 2), round(egress_tot, 2), round(interrupt_tot, 2)

def run_ilp(jobs_df, prices_df, egress_map, gpu_cap=64):
    n_jobs = len(jobs_df)
    n_opts = len(prices_df)

    eval_matrix = []
    for _, job in jobs_df.iterrows():
        job_evals = []
        for _, p in prices_df.iterrows():
            k = f"{job['data_provider']}:{job['data_region']}->{p['provider']}:{p['region']}".upper()
            rate = egress_map.get(k, 0.0)
            job_evals.append(evaluate_option(p.to_dict(), job.to_dict(), rate))
        eval_matrix.append(job_evals)

    model = cp_model.CpModel()
    x = {}

    for j in range(n_jobs):
        for o in range(n_opts):
            x[(j, o)] = model.NewBoolVar(f"x_{j}_{o}")
            if not eval_matrix[j][o]["feasible"]:
                model.Add(x[(j, o)] == 0)

    # Exactly one option per job
    for j in range(n_jobs):
        model.AddExactlyOne([x[(j, o)] for o in range(n_opts)])

    # Capacity constraints per (provider, region, gpu_model)
    rg_indices = {}
    for o, (_, p) in enumerate(prices_df.iterrows()):
        rg = f"{p['provider']}_{p['region']}_{p['gpu_model']}".upper()
        rg_indices.setdefault(rg, []).append(o)

    for rg, opt_indices in rg_indices.items():
        terms = []
        for j, (_, job) in enumerate(jobs_df.iterrows()):
            g_count = int(job["gpu_count"])
            for o in opt_indices:
                terms.append(x[(j, o)] * g_count)
        model.Add(sum(terms) <= gpu_cap)

    # Objective: Minimize total cost in cents
    cost_terms = []
    for j in range(n_jobs):
        for o in range(n_opts):
            cents = int(round(eval_matrix[j][o].get("total_cost", 99999.0) * 100))
            cost_terms.append(x[(j, o)] * cents)
    model.Minimize(sum(cost_terms))

    solver = cp_model.CpSolver()
    solver.parameters.max_time_in_seconds = 20.0
    status = solver.Solve(model)

    if status in (cp_model.OPTIMAL, cp_model.FEASIBLE):
        tot_usd = round(solver.ObjectiveValue() / 100.0, 2)
        return tot_usd, 100.0
    return 0.0, 0.0

def main():
    prices_df, egress_map = load_catalog()
    scenarios = ["mixed", "training-heavy", "inference-burst", "capacity-crunch", "spot-volatile"]
    results = []

    print("Running experiments across scenarios...")
    for sc in scenarios:
        cap = 16 if sc == "capacity-crunch" else 64
        for seed in range(5): # run across multiple seeds
            jobs = generate_scenario_jobs(sc, n_jobs=50, seed=seed)
            aws_cost, aws_hit = run_single_cloud(jobs, prices_df, egress_map, "AWS")
            gcp_cost, gcp_hit = run_single_cloud(jobs, prices_df, egress_map, "GCP")
            az_cost, az_hit = run_single_cloud(jobs, prices_df, egress_map, "AZURE")
            gr_cost, gr_hit, comp, egr, intc = run_greedy(jobs, prices_df, egress_map)
            ilp_cost, ilp_hit = run_ilp(jobs, prices_df, egress_map, gpu_cap=cap)

            results.append({
                "scenario": sc,
                "seed": seed,
                "aws_cost": aws_cost,
                "gcp_cost": gcp_cost,
                "azure_cost": az_cost,
                "greedy_cost": gr_cost,
                "ilp_cost": ilp_cost,
                "greedy_savings_vs_aws_pct": round(((aws_cost - gr_cost) / aws_cost) * 100, 1) if aws_cost > 0 else 0,
                "ilp_savings_vs_aws_pct": round(((aws_cost - ilp_cost) / aws_cost) * 100, 1) if aws_cost > 0 else 0,
                "ilp_savings_vs_greedy_pct": round(((gr_cost - ilp_cost) / gr_cost) * 100, 1) if gr_cost > 0 else 0,
                "ilp_deadline_hit_pct": ilp_hit,
            })

    res_df = pd.DataFrame(results)
    out_path = os.path.join(os.path.dirname(__file__), "results.csv")
    res_df.to_csv(out_path, index=False)
    print(f"Experiments successfully completed! Saved to {out_path}")
    print("\nSummary Results (Mean across seeds):")
    print(res_df.groupby("scenario")[["aws_cost", "gcp_cost", "azure_cost", "greedy_cost", "ilp_cost", "ilp_savings_vs_aws_pct"]].mean())

if __name__ == "__main__":
    main()
