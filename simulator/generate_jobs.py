"""
Synthetic Job Workload Generator for AI Workload Scenarios.
Generates 5 distinct evaluation workloads:
1. Training-heavy
2. Inference-burst
3. Mixed
4. Capacity-crunch
5. Spot-volatile
"""
import numpy as np
import pandas as pd

def generate_scenario_jobs(scenario: str, n_jobs: int = 100, seed: int = 42) -> pd.DataFrame:
    rng = np.random.default_rng(seed)
    records = []

    providers = ["AWS", "GCP", "AZURE"]
    region_map = {
        "AWS": ["us-east-1", "us-west-2"],
        "GCP": ["us-central1", "europe-west4"],
        "AZURE": ["eastus", "westeurope"],
    }

    for i in range(1, n_jobs + 1):
        job_id = f"{scenario}-job-{i:03d}"
        prov = rng.choice(providers)
        reg = rng.choice(region_map[prov])
        spot_allowed = True
        checkpointing = True

        if scenario == "training-heavy":
            gpu_model = rng.choice(["A100", "H100"])
            gpu_count = int(rng.choice([4, 8]))
            duration = round(rng.uniform(8.0, 48.0), 2)
            data_gb = round(rng.uniform(100.0, 500.0), 2)
            deadline = round(duration * rng.uniform(1.8, 3.2), 2)

        elif scenario == "inference-burst":
            gpu_model = rng.choice(["T4", "L4", "A10G"])
            gpu_count = int(rng.choice([1, 2]))
            duration = round(rng.uniform(0.5, 2.0), 2)
            data_gb = round(rng.uniform(5.0, 30.0), 2)
            deadline = round(duration * rng.uniform(1.2, 1.8), 2)

        elif scenario == "capacity-crunch":
            gpu_model = rng.choice(["A100", "H100"])
            gpu_count = 8
            duration = round(rng.uniform(6.0, 16.0), 2)
            data_gb = round(rng.uniform(50.0, 150.0), 2)
            deadline = round(duration * 2.5, 2)

        elif scenario == "spot-volatile":
            gpu_model = "A100"
            gpu_count = 4
            duration = round(rng.uniform(4.0, 14.0), 2)
            data_gb = round(rng.uniform(80.0, 180.0), 2)
            deadline = round(duration * 3.0, 2)
            checkpointing = bool(rng.choice([True, False]))

        else: # mixed
            is_training = rng.random() < 0.4
            if is_training:
                gpu_model = rng.choice(["A100", "H100"])
                gpu_count = int(rng.choice([4, 8]))
                duration = round(rng.uniform(10.0, 30.0), 2)
                data_gb = round(rng.uniform(100.0, 350.0), 2)
                deadline = round(duration * 2.2, 2)
            else:
                gpu_model = rng.choice(["T4", "A10G"])
                gpu_count = 1
                duration = round(rng.uniform(1.0, 4.0), 2)
                data_gb = round(rng.uniform(10.0, 50.0), 2)
                deadline = round(duration * 1.8, 2)

        records.append({
            "job_id": job_id,
            "gpu_model": gpu_model,
            "gpu_count": gpu_count,
            "duration_hours": duration,
            "data_gb": data_gb,
            "data_provider": prov,
            "data_region": reg,
            "deadline_hours": deadline,
            "spot_allowed": spot_allowed,
            "checkpointing": checkpointing,
        })

    return pd.DataFrame(records)

if __name__ == "__main__":
    df = generate_scenario_jobs("mixed", 10)
    print("Generated 10 sample jobs for mixed scenario:")
    print(df.head())
