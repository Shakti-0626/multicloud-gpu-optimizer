"""
Cost Model implementation in Python.
Mirrors com.multicloud.optimizer.optimizer.CostModel for verification and standalone experimentation.
"""
import math
from typing import Dict, Any, Optional

SUBSTITUTIONS = {
    "T4": {"T4", "L4", "A10G", "A100", "H100"},
    "L4": {"L4", "A10G", "A100", "H100"},
    "A10G": {"A10G", "A100", "H100"},
    "A100": {"A100", "H100"},
    "H100": {"H100"},
}

def is_gpu_compatible(requested: str, candidate: str) -> bool:
    req = requested.upper()
    cand = candidate.upper()
    if req == cand:
        return True
    return cand in SUBSTITUTIONS.get(req, set())

def evaluate_option(
    price_row: Dict[str, Any],
    job: Dict[str, Any],
    egress_rate_usd_per_gb: float = 0.0
) -> Dict[str, Any]:
    """
    Evaluates one candidate placement option for a given job.
    """
    inst_gpu_model = price_row["gpu_model"]
    inst_gpu_count = int(price_row["gpu_count"])
    pricing_model = price_row["pricing_model"].upper()
    hourly_usd = float(price_row["hourly_usd"])
    interruption_pct = float(price_row["interruption_pct"])
    queue_min = int(price_row["queue_minutes"])

    req_gpu_model = job["gpu_model"]
    req_gpu_count = int(job["gpu_count"])
    duration_h = float(job["duration_hours"])
    data_gb = float(job["data_gb"])
    data_provider = job["data_provider"]
    data_region = job["data_region"]
    deadline_h = float(job["deadline_hours"])
    spot_allowed = bool(job["spot_allowed"])
    checkpointing = bool(job["checkpointing"])

    # 1. Compatibility check
    if not is_gpu_compatible(req_gpu_model, inst_gpu_model):
        return {"feasible": False, "reason": f"Incompatible GPU {inst_gpu_model} for {req_gpu_model}"}

    # 2. Spot check
    if pricing_model == "SPOT" and not spot_allowed:
        return {"feasible": False, "reason": "Spot not allowed"}

    # 3. Instance rounding
    instances_needed = math.ceil(req_gpu_count / inst_gpu_count)

    # 4. Compute cost
    compute_cost = round(instances_needed * hourly_usd * duration_h, 4)

    # 5. Egress cost
    is_local = (data_provider.upper() == price_row["provider"].upper()) and (data_region.lower() == price_row["region"].lower())
    effective_egress_rate = 0.0 if is_local else egress_rate_usd_per_gb
    egress_cost = round(data_gb * effective_egress_rate, 4)

    # 6. Interruption cost
    p = (interruption_pct / 100.0) if pricing_model == "SPOT" else 0.0
    r = 0.10 if checkpointing else 0.50
    interruption_cost = round(compute_cost * p * r, 4)

    # 7. Total cost
    total_cost = round(compute_cost + egress_cost + interruption_cost, 4)

    # 8. Finish time
    queue_h = queue_min / 60.0
    transfer_h = (data_gb / 450.0) if (!is_local and data_gb > 0) else 0.0
    compute_h = duration_h * (1.0 + p * r)
    est_finish_hours = round(queue_h + transfer_h + compute_h, 2)

    # 9. Feasibility
    feasible = est_finish_hours <= deadline_h
    reason = None if feasible else f"Finish time {est_finish_hours}h exceeds deadline {deadline_h}h"

    return {
        "feasible": feasible,
        "reason": reason,
        "provider": price_row["provider"],
        "region": price_row["region"],
        "instance": price_row["instance"],
        "pricing_model": pricing_model,
        "instances_needed": instances_needed,
        "compute_cost": compute_cost,
        "egress_cost": egress_cost,
        "interruption_cost": interruption_cost,
        "total_cost": total_cost,
        "est_finish_hours": est_finish_hours,
    }
