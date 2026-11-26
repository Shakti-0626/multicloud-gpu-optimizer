# Carbon-Aware Multicloud Optimization for AI Workloads

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Green AI](https://img.shields.io/badge/Green_AI-Carbon--Aware-emerald.svg)](https://greensoftware.foundation/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![OR-Tools](https://img.shields.io/badge/Google_OR--Tools-CP--SAT-yellow.svg)](https://developers.google.com/optimization)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4-cyan.svg)](https://tailwindcss.com/)

A sustainable decision-layer and workload scheduler that determines the mathematically optimal and carbon-conscious cloud, region, and GPU instance for AI workloads across **AWS**, **GCP**, and **Azure**, balancing compute spend, cross-cloud egress tariffs, spot interruption risk, and regional electric grid carbon intensity ($g\text{CO}_2/\text{kWh}$).

---

## 1. System Architecture

```
[React 18 Dashboard] --HTTP/JSON--> [Spring Boot 3 API]
  (Vite + Tailwind + Recharts)        |-- Controller   (REST Endpoints & Validation)
                                      |-- Service      (Recommendation, Scheduling, Simulation)
                                      |-- Optimizer    (CostModel, Greedy, ILP via OR-Tools)
                                      '-- Repository   (Spring Data JPA)
                                             |
                                      [PostgreSQL 16] <-- Flyway & CSV Seeders (prices.csv, egress.csv)
                                             ^
[Python Simulator] --runs experiments on DB or API--> results.csv --> Evaluation Metrics
```

---

## 2. Key Modules (3 Parts)

### Part 1: Backend System
- **Java 21 & Spring Boot 3**: High-performance REST API.
- **Mathematical Cost & Carbon Model**:
  $$C_{\text{total}} = C_{\text{compute}} + C_{\text{egress}} + C_{\text{interrupt}}$$
  $$\text{Carbon Emitted (kg CO}_2\text{e)} = \frac{E_{\text{energy}} \times I_{\text{grid}}}{1000} = \frac{(N_{\text{gpu}} \times P_{\text{tdp}} \times T_{\text{eff}} \times \text{PUE}) \times I_{\text{grid}}}{1000}$$
  - Compute: $n_o \times \text{hourly\_price} \times T_j$ with instance count rounding $\lceil g_j / g_o \rceil$.
  - Egress: Cross-cloud data transfer cost (0 if intra-region).
  - Risk Penalty: $p_o \times r \times C_{\text{compute}}$ ($r = 0.10$ with checkpointing, $0.50$ without).
  - Carbon Impact: Maps regional grid carbon intensity ($I_{\text{grid}}$ in $g\text{CO}_2/\text{kWh}$) to identify clean-energy regions (e.g. hydro-powered `us-west-2` vs fossil-heavy `us-east-1`).
- **Solvers**:
  - **Greedy Optimizer**: Single-job instantaneous ranking & single-cloud baseline calculation.
  - **ILP Optimizer (Google OR-Tools CP-SAT)**: Multi-job batch scheduling with regional GPU capacity constraints.
- **Flyway Migrations**: 6 relational tables (`cloud_provider`, `instance_type`, `price`, `egress_rate`, `job`, `placement`).
- **Startup Seeder**: Auto-loads dated GPU prices and egress rates from `data/prices.csv` and `data/egress.csv`.

### Part 2: Frontend Dashboard
- **React 18 + Vite + TypeScript + Tailwind CSS + Recharts**.
- **4 Dedicated Views**:
  1. **Single Job Recommender**: Form intake, top recommendation card with plain-language rationale, cost breakdown progress bar, and single-cloud baseline comparisons (All-AWS, All-GCP, All-Azure).
  2. **Batch ILP Scheduler**: Capacity limit slider, multi-job assignment solver, regional GPU capacity utilization meter, and placement table.
  3. **Workload Simulator**: 5 scenarios (Training-heavy, Inference-burst, Mixed, Capacity-crunch, Spot-volatile) with interactive Recharts for spend, savings %, and cloud distribution.
  4. **GPU Price Explorer**: Filterable instance catalog by cloud, GPU model, region, and pricing model.

### Part 3: Testing & Simulator
- **JUnit 5 Unit Tests (`CostModelTest`)**:
  - Spot vs On-Demand zero interruption verification.
  - Checkpointing risk mitigation ($r = 0.10$ vs $r = 0.50$).
  - Intra-region zero egress vs cross-cloud egress fees.
  - Ceiling math for instance count rounding.
  - Deadline rejection under high queue/transfer times.
  - Permitted GPU tier substitutions (e.g. H100 substituting A100).
- **Python Simulator (`simulator/`)**:
  - Independent Python cost model for cross-verification.
  - Seeded workload generator for 5 empirical scenarios.
  - Automated benchmark comparisons outputting `results.csv`.

---

## 3. Quickstart & Complete Setup

### Option A: Complete Docker Compose (Recommended)
Start database, backend, and dashboard in one command:
```bash
docker compose up --build
```
- Dashboard: [http://localhost:5173](http://localhost:5173)
- Backend Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- PostgreSQL: `localhost:5432` (User: `optimizer`, Pass: `optimizer`, DB: `optimizer`)

---

### Option B: Local Developer Run

#### 1. Start PostgreSQL
```bash
docker compose up -d db
```

#### 2. Run Backend
```bash
cd backend
mvn clean spring-boot:run
```
Run tests:
```bash
mvn test
```

#### 3. Run Frontend Dashboard
```bash
cd dashboard
npm install
npm run dev
```

#### 4. Run Python Workload Simulator
```bash
cd simulator
pip install -r requirements.txt
python run_experiments.py
```

---

## 4. REST API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/recommend` | Recommends top placements and baselines without saving |
| `POST` | `/api/jobs` | Submits job and persists placement results |
| `GET` | `/api/jobs/{id}` | Retrieves submitted job with evaluated placements |
| `POST` | `/api/schedule` | Multi-job batch ILP optimization with capacity constraints |
| `GET` | `/api/prices` | Filterable catalog (`provider`, `gpuModel`, `region`, `pricingModel`) |
| `POST` | `/api/simulations` | Runs a workload simulation scenario (`mixed`, `training-heavy`, etc.) |
| `GET` | `/api/simulations/{scenario}` | Retrieves simulation metrics and strategy comparison |

---

## 5. Sample Request & Response

### Request: `POST /api/recommend`
```json
{
  "name": "llm-finetune-01",
  "gpuModel": "A100",
  "gpuCount": 4,
  "durationHours": 10,
  "dataGb": 200,
  "dataProvider": "AWS",
  "dataRegion": "us-east-1",
  "deadlineHours": 24,
  "spotAllowed": true,
  "checkpointing": true
}
```

### Response:
```json
{
  "jobName": "llm-finetune-01",
  "recommendations": [
    {
      "rank": 1,
      "provider": "AWS",
      "region": "us-east-1",
      "instance": "p4d.24xlarge",
      "pricingModel": "SPOT",
      "computeCost": 125.00,
      "egressCost": 0.00,
      "interruptionCost": 1.88,
      "totalCost": 126.88,
      "estFinishHours": 10.50,
      "explanation": "Data already resides in AWS us-east-1, eliminating cross-cloud egress costs. Spot pricing delivers ~62% total savings versus On-Demand. Periodic checkpointing restricts work loss risk to 10%, keeping interruption penalty low ($1.88)."
    }
  ],
  "baselines": {
    "aws": 126.88,
    "gcp": 137.42,
    "azure": 128.96
  }
}
```
