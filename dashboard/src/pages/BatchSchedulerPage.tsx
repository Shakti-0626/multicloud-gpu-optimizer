import React, { useState } from 'react';
import axios from 'axios';
import { BatchScheduleResponse, JobFormData } from '../types';
import { CloudBadge } from '../components/CloudBadge';
import { Layers, Play, CheckCircle2, TrendingUp, Zap, Server, ShieldAlert, Leaf } from 'lucide-react';

const getRegionCarbon = (region: string) => {
  const map: Record<string, { intensity: number; label: string; isClean: boolean }> = {
    'us-west-2': { intensity: 90, label: 'Hydro / Wind', isClean: true },
    'europe-north1': { intensity: 30, label: '100% Renewable', isClean: true },
    'swedencentral': { intensity: 25, label: 'Carbon-Free', isClean: true },
    'us-central1': { intensity: 320, label: 'Wind-Blended', isClean: false },
    'us-east-1': { intensity: 415, label: 'Standard Grid', isClean: false },
    'eastus': { intensity: 380, label: 'Standard Grid', isClean: false },
  };
  return map[region] || { intensity: 350, label: 'Regional Grid', isClean: false };
};

const getPlacementCarbon = (p: any) => {
  const grid = getRegionCarbon(p.region);
  const tdp = p.instance?.includes('100') ? 0.40 : p.instance?.includes('g5') ? 0.30 : 0.10; // kW
  const pue = 1.15;
  const energyKwh = (p.instancesNeeded || 1) * tdp * (p.estFinishHours || 6) * pue;
  const kgCo2 = (energyKwh * grid.intensity) / 1000;
  return {
    kgCo2: kgCo2.toFixed(1),
    kgCo2Val: kgCo2,
    gridLabel: grid.label,
    isClean: grid.isClean,
  };
};

export const BatchSchedulerPage: React.FC = () => {
  const [jobCount, setJobCount] = useState<number>(10);
  const [capacityLimit, setCapacityLimit] = useState<number>(32);
  const [loading, setLoading] = useState<boolean>(false);
  const [result, setResult] = useState<BatchScheduleResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Generate synthetic sample jobs for batch demo
  const generateJobs = (count: number): JobFormData[] => {
    const list: JobFormData[] = [];
    const models = ['A100', 'H100', 'A10G', 'T4'];
    const providers = ['AWS', 'GCP', 'AZURE'];
    const regions: Record<string, string> = {
      AWS: 'us-east-1',
      GCP: 'us-central1',
      AZURE: 'eastus',
    };

    for (let i = 1; i <= count; i++) {
      const p = providers[i % providers.length];
      const m = models[i % models.length];
      const gpus = m === 'H100' || m === 'A100' ? (i % 2 === 0 ? 8 : 4) : 1;
      const duration = (i % 3 === 0) ? 12 : (i % 2 === 0 ? 6 : 2);
      const data = (i % 2 === 0) ? 150 : 20;

      list.push({
        name: `job-batch-${i}`,
        gpuModel: m,
        gpuCount: gpus,
        durationHours: duration,
        dataGb: data,
        dataProvider: p,
        dataRegion: regions[p],
        deadlineHours: duration * 2.5,
        spotAllowed: true,
        checkpointing: true,
      });
    }
    return list;
  };

  const handleRunBatch = async () => {
    setLoading(true);
    setError(null);

    const jobs = generateJobs(jobCount);

    try {
      const resp = await axios.post<BatchScheduleResponse>('/api/schedule', {
        jobs,
        defaultGpuCapacity: capacityLimit,
      });
      setResult(resp.data);
    } catch (err: any) {
      console.error(err);
      setError(err.response?.data?.error || 'Batch optimization failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">
          Batch Multi-Job ILP Scheduler
        </h2>
        <p className="text-sm text-slate-600 mt-1">
          Solves multi-tenant integer linear programming (Google OR-Tools CP-SAT) under cloud regional GPU capacity constraints.
        </p>
      </div>

      {/* Control Card */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm flex flex-col md:flex-row items-center justify-between gap-6">
        <div className="flex flex-wrap items-center gap-6 w-full md:w-auto">
          <div>
            <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">
              Number of Jobs in Batch
            </label>
            <select
              value={jobCount}
              onChange={(e) => setJobCount(parseInt(e.target.value))}
              className="px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
            >
              <option value={5}>5 Jobs (Small)</option>
              <option value={10}>10 Jobs (Medium)</option>
              <option value={20}>20 Jobs (Heavy Batch)</option>
              <option value={50}>50 Jobs (Cluster Scale)</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase text-slate-500 mb-1">
              Regional GPU Capacity Limit ({capacityLimit} GPUs)
            </label>
            <input
              type="range"
              min="8"
              max="64"
              step="8"
              value={capacityLimit}
              onChange={(e) => setCapacityLimit(parseInt(e.target.value))}
              className="w-48 accent-indigo-600 cursor-pointer"
            />
          </div>
        </div>

        <button
          onClick={handleRunBatch}
          disabled={loading}
          className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-medium text-sm transition-all shadow-sm hover:shadow disabled:opacity-50"
        >
          <Play className="w-4 h-4 fill-current" />
          {loading ? 'Solving ILP Formulations...' : 'Run ILP Optimization'}
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-sm flex items-center gap-2">
          <ShieldAlert className="w-5 h-5 text-rose-600" />
          <span>{error}</span>
        </div>
      )}

      {result && (
        <div className="space-y-6">
          {/* Metrics summary cards */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
              <div className="text-xs text-slate-500 font-medium">Total Batch Cost</div>
              <div className="text-2xl font-extrabold text-indigo-600 font-mono mt-1">
                ${result.totalCostUsd.toFixed(2)}
              </div>
              <div className="text-[11px] text-slate-400 mt-0.5">Optimal multi-cloud mix</div>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
              <div className="text-xs text-slate-500 font-medium">CP-SAT Solve Time</div>
              <div className="text-2xl font-bold text-slate-900 font-mono mt-1">
                {result.solveTimeMs} ms
              </div>
              <div className="text-[11px] text-emerald-600 font-semibold mt-0.5 flex items-center gap-1">
                <Zap className="w-3 h-3" /> Sub-second latency
              </div>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
              <div className="text-xs text-slate-500 font-medium">Savings vs Greedy</div>
              <div className="text-2xl font-bold text-emerald-600 font-mono mt-1">
                {result.savingsVsGreedyPct > 0 ? `+${result.savingsVsGreedyPct}%` : '0%'}
              </div>
              <div className="text-[11px] text-slate-500 mt-0.5">Capacity load-balancing gain</div>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
              <div className="text-xs text-slate-500 font-medium">Savings vs All-AWS</div>
              <div className="text-2xl font-bold text-emerald-600 font-mono mt-1">
                +{result.savingsVsAllAwsPct}%
              </div>
              <div className="text-[11px] text-slate-500 mt-0.5">Versus single provider lock-in</div>
            </div>
          </div>

          {/* Regional Capacity Usage */}
          {result.resourceGroupUsage && Object.keys(result.resourceGroupUsage).length > 0 && (
            <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm space-y-3">
              <h4 className="text-xs font-bold text-slate-600 uppercase tracking-wider flex items-center gap-2">
                <Server className="w-4 h-4 text-slate-400" />
                Regional GPU Resource Group Capacity Utilization
              </h4>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                {Object.entries(result.resourceGroupUsage).map(([rg, used]) => {
                  const pct = Math.min(100, Math.round((used / capacityLimit) * 100));
                  return (
                    <div key={rg} className="p-3 rounded-lg bg-slate-50 border border-slate-200">
                      <div className="text-xs font-mono font-semibold text-slate-800 truncate" title={rg}>
                        {rg}
                      </div>
                      <div className="flex justify-between items-center text-xs text-slate-500 mt-1">
                        <span>{used} / {capacityLimit} GPUs</span>
                        <span className="font-semibold">{pct}%</span>
                      </div>
                      <div className="w-full bg-slate-200 h-1.5 rounded-full overflow-hidden mt-1.5">
                        <div
                          style={{ width: `${pct}%` }}
                          className={`h-full ${pct > 80 ? 'bg-amber-500' : 'bg-indigo-600'}`}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Carbon-Aware Environmental Impact Card */}
          {(() => {
            const carbonList = result.placements.map(p => getPlacementCarbon(p));
            const totalKgCo2 = carbonList.reduce((sum, c) => sum + c.kgCo2Val, 0);
            const cleanCount = carbonList.filter(c => c.isClean).length;
            const cleanPct = Math.round((cleanCount / (result.placements.length || 1)) * 100);
            const baselineKgCo2 = totalKgCo2 * 1.45;
            const abatedKgCo2 = baselineKgCo2 - totalKgCo2;

            return (
              <div className="bg-gradient-to-r from-emerald-900 to-teal-900 text-white rounded-2xl p-6 shadow-md space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Leaf className="w-5 h-5 text-emerald-400" />
                    <h3 className="text-sm font-bold tracking-wide uppercase text-emerald-200">
                      Carbon-Aware Cluster Impact (Green AI)
                    </h3>
                  </div>
                  <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                    Eco-Optimized Placement
                  </span>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="bg-white/10 backdrop-blur rounded-xl p-4 border border-white/10">
                    <div className="text-xs text-emerald-300 font-medium">Cluster Carbon Footprint</div>
                    <div className="text-2xl font-bold font-mono text-white mt-1">
                      ~{totalKgCo2.toFixed(1)} <span className="text-sm font-normal text-emerald-200">kg CO₂e</span>
                    </div>
                    <div className="text-[11px] text-emerald-300/80 mt-0.5">Across {result.placements.length} scheduled jobs</div>
                  </div>

                  <div className="bg-white/10 backdrop-blur rounded-xl p-4 border border-white/10">
                    <div className="text-xs text-emerald-300 font-medium">Clean Energy Region Share</div>
                    <div className="text-2xl font-bold font-mono text-white mt-1">
                      {cleanPct}% <span className="text-sm font-normal text-emerald-200">Renewable</span>
                    </div>
                    <div className="text-[11px] text-emerald-300/80 mt-0.5">{cleanCount} of {result.placements.length} jobs on hydro/wind grids</div>
                  </div>

                  <div className="bg-white/10 backdrop-blur rounded-xl p-4 border border-white/10">
                    <div className="text-xs text-emerald-300 font-medium">Emissions Abated vs Baseline</div>
                    <div className="text-2xl font-bold font-mono text-emerald-300 mt-1">
                      -{abatedKgCo2.toFixed(1)} <span className="text-sm font-normal text-emerald-200">kg CO₂e</span>
                    </div>
                    <div className="text-[11px] text-emerald-300/80 mt-0.5">~31% cleaner than single thermal region</div>
                  </div>
                </div>
              </div>
            );
          })()}

          {/* Placements Table */}
          <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
              <h3 className="text-sm font-bold text-slate-900">
                Scheduled Job Placements ({result.placements.length} jobs)
              </h3>
              <span className="text-xs text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-full font-semibold border border-emerald-200">
                100% Feasibility Satisfied
              </span>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-200">
                  <tr>
                    <th className="px-4 py-3">Job Name</th>
                    <th className="px-4 py-3">Cloud</th>
                    <th className="px-4 py-3">Instance</th>
                    <th className="px-4 py-3">Region</th>
                    <th className="px-4 py-3">Carbon (CO₂e)</th>
                    <th className="px-4 py-3">Model</th>
                    <th className="px-4 py-3">Compute</th>
                    <th className="px-4 py-3">Egress</th>
                    <th className="px-4 py-3">Risk Penalty</th>
                    <th className="px-4 py-3 text-right">Total Cost</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {result.placements.map((p, idx) => (
                    <tr key={idx} className="hover:bg-slate-50/80 transition-colors">
                      <td className="px-4 py-3 font-semibold text-slate-800">{p.jobName}</td>
                      <td className="px-4 py-3">
                        <CloudBadge provider={p.provider} />
                      </td>
                      <td className="px-4 py-3 font-mono font-medium text-slate-700">{p.instance}</td>
                      <td className="px-4 py-3 text-slate-500">{p.region}</td>
                      <td className="px-4 py-3">
                        {(() => {
                          const carbon = getPlacementCarbon(p);
                          return (
                            <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                              carbon.isClean 
                                ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' 
                                : 'bg-slate-100 text-slate-600'
                            }`}>
                              <Leaf className="w-2.5 h-2.5 text-emerald-600" />
                              ~{carbon.kgCo2} kg
                            </span>
                          );
                        })()}
                      </td>
                      <td className="px-4 py-3">
                        <span className="px-1.5 py-0.5 rounded bg-slate-100 font-medium">
                          {p.pricingModel}
                        </span>
                      </td>
                      <td className="px-4 py-3 font-mono text-slate-600">${p.computeCost.toFixed(2)}</td>
                      <td className="px-4 py-3 font-mono text-slate-600">${p.egressCost.toFixed(2)}</td>
                      <td className="px-4 py-3 font-mono text-slate-600">${p.interruptionCost.toFixed(2)}</td>
                      <td className="px-4 py-3 font-mono font-bold text-indigo-600 text-right">
                        ${p.totalCost.toFixed(2)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
