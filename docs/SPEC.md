# Multicloud Optimization System for AI Workloads: Build Guide

## 1. Overview and Scope
A decision layer that picks the cheapest cloud, region, and GPU instance for AI workloads across AWS, GCP, and Azure, proving cost savings through mathematical optimization and simulation.

### Objectives
1. Cost model incorporating compute, data egress, and spot interruption risk penalty.
2. Greedy baseline scheduler vs Capacity-constrained Multi-job Integer Linear Programming (ILP) optimizer using Google OR-Tools CP-SAT.
3. Target 20–35% cost reduction compared to single-cloud baselines.
4. Maintain deadline hit rate $\ge 95\%$.
5. Interactive dashboard with real-time recommendations, cost breakdown, and plain-language explanations.

---

## 2. Tech Stack
- **Backend API**: Java 21, Spring Boot 3.3+, Spring Data JPA, Bean Validation, Lombok, Springdoc OpenAPI.
- **Optimization**: Google OR-Tools (Java CP-SAT solver) & Greedy heuristic.
- **Database**: PostgreSQL 16 with Flyway migrations.
- **Simulator**: Python 3.11, pandas, NumPy, matplotlib.
- **Frontend**: React 18, Vite, TypeScript, Tailwind CSS, Recharts, Lucide Icons.
- **Orchestration**: Docker & Docker Compose.
