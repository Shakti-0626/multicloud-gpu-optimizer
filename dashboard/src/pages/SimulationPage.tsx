import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { SimulationResult } from '../types';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer,
  PieChart, Pie, Cell, CartesianGrid
} from 'recharts';
import { BarChart3, RefreshCw, Zap, TrendingDown, Clock, ShieldCheck } from 'lucide-react';

const CLOUD_COLORS: Record<string, string> = {
  AWS: '#FF9900',
  GCP: '#4285F4',
  AZURE: '#0089D6',
};

const STRATEGY_COLORS: Record<string, string> = {
  ALL_AWS: '#FF9900',
  ALL_GCP: '#4285F4',
  ALL_AZURE: '#0089D6',
  GREEDY: '#8B5CF6',
  ILP: '#10B981',
};

export const SimulationPage: React.FC = () => {
  const [scenario, setScenario] = useState<string>('mixed');
  const [loading, setLoading] = useState<boolean>(false);
  const [result, setResult] = useState<SimulationResult | null>(null);

  const fetchSimulation = async (selectedScenario: string) => {
    setLoading(true);
    try {
      const resp = await axios.get<SimulationResult>(`/api/simulations/${selectedScenario}`);
      setResult(resp.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSimulation(scenario);
  }, [scenario]);

  const handleRunFresh = async () => {
    setLoading(true);
    try {
      const resp = await axios.post<SimulationResult>(`/api/simulations?scenario=${scenario}`);
      setResult(resp.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Pie chart data
  const pieData = result?.cloudPlacementDistribution
    ? Object.entries(result.cloudPlacementDistribution).map(([cloud, pct]) => ({
        name: cloud,
        value: pct,
      }))
    : [];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">
            Workload Simulator & Benchmark Analytics
          </h2>
          <p className="text-sm text-slate-600 mt-1">
            Empirical evaluation across synthetic workload scenarios proving multi-cloud savings versus lock-in baselines.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <select
            value={scenario}
            onChange={(e) => setScenario(e.target.value)}
            className="px-3 py-2 text-sm font-medium bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none shadow-sm"
          >
            <option value="mixed">Mixed Workload (100 jobs)</option>
            <option value="training-heavy">Training-Heavy (50 jobs, 8-48h)</option>
            <option value="inference-burst">Inference Burst (200 jobs, 0.5-2h)</option>
            <option value="capacity-crunch">Capacity Crunch (High Contention)</option>
            <option value="spot-volatile">Spot-Volatile (Dynamic Risk)</option>
          </select>

          <button
            onClick={handleRunFresh}
            disabled={loading}
            className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-medium shadow-sm transition-all disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            Run Simulation
          </button>
        </div>
      </div>

      {loading && !result && (
        <div className="bg-white rounded-2xl border border-slate-200 p-16 text-center text-slate-500">
          <RefreshCw className="w-8 h-8 text-indigo-600 animate-spin mx-auto mb-2" />
          <p className="text-sm font-medium text-slate-700">Simulating workload against 5 placement strategies...</p>
        </div>
      )}

      {result && (
        <div className="space-y-6">
          {/* Key Metric Highlights */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
              <div className="text-xs text-slate-500 font-medium">Scenario Jobs</div>
              <div className="text-2xl font-bold text-slate-900 font-mono mt-1">
                {result.jobCount}
              </div>
              <div className="text-[11px] text-slate-400 mt-0.5">{result.scenarioName} profile</div>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
              <div className="text-xs text-slate-500 font-medium">Savings vs All-AWS</div>
              <div className="text-2xl font-bold text-emerald-600 font-mono mt-1">
                +{result.savingsVsBaselines?.vsAllAws}%
              </div>
              <div className="text-[11px] text-slate-400 mt-0.5">Proven simulation saving</div>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
              <div className="text-xs text-slate-500 font-medium">Savings vs All-GCP</div>
              <div className="text-2xl font-bold text-emerald-600 font-mono mt-1">
                +{result.savingsVsBaselines?.vsAllGcp}%
              </div>
              <div className="text-[11px] text-slate-400 mt-0.5">Cross-cloud egress compensated</div>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
              <div className="text-xs text-slate-500 font-medium">ILP vs Greedy Gain</div>
              <div className="text-2xl font-bold text-indigo-600 font-mono mt-1">
                +{result.savingsVsBaselines?.vsGreedy}%
              </div>
              <div className="text-[11px] text-slate-400 mt-0.5">Capacity constraint mitigation</div>
            </div>
          </div>

          {/* Charts Row 1: Total Cost per Strategy & Stacked Breakdown */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
            {/* Total Cost Comparison Bar Chart */}
            <div className="lg:col-span-7 bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-4">
              <h3 className="text-sm font-bold text-slate-900">
                Total Spend per Strategy (USD)
              </h3>
              <div className="h-72 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={result.strategyMetrics}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                    <XAxis dataKey="strategy" tick={{ fontSize: 12, fill: '#64748B' }} />
                    <YAxis tick={{ fontSize: 12, fill: '#64748B' }} />
                    <Tooltip
                      formatter={(val: number) => [`$${val.toFixed(2)}`, 'Total Cost']}
                      contentStyle={{ borderRadius: '8px', border: '1px solid #CBD5E1' }}
                    />
                    <Bar dataKey="totalCostUsd" radius={[6, 6, 0, 0]}>
                      {result.strategyMetrics.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={STRATEGY_COLORS[entry.strategy] || '#6366F1'} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Cloud Placement Distribution Pie */}
            <div className="lg:col-span-5 bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-4">
              <h3 className="text-sm font-bold text-slate-900">
                Optimal Cloud GPU Share (ILP Strategy)
              </h3>
              <div className="h-72 w-full flex items-center justify-center">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={pieData}
                      dataKey="value"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      outerRadius={80}
                      innerRadius={45}
                      paddingAngle={4}
                      label={({ name, value }) => `${name} ${value}%`}
                    >
                      {pieData.map((entry) => (
                        <Cell key={entry.name} fill={CLOUD_COLORS[entry.name] || '#94A3B8'} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(val: number) => [`${val}%`, 'Share']} />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>

          {/* Strategy Details Table */}
          <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-200">
              <h3 className="text-sm font-bold text-slate-900">
                Empirical Evaluation Benchmark Metrics
              </h3>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-200">
                  <tr>
                    <th className="px-4 py-3">Strategy</th>
                    <th className="px-4 py-3">Total Spend</th>
                    <th className="px-4 py-3">Compute Cost</th>
                    <th className="px-4 py-3">Egress Cost</th>
                    <th className="px-4 py-3">Risk Penalty</th>
                    <th className="px-4 py-3">Deadline Hit Rate</th>
                    <th className="px-4 py-3">Spot Usage</th>
                    <th className="px-4 py-3 text-right">Runtime</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {result.strategyMetrics.map((m) => (
                    <tr key={m.strategy} className="hover:bg-slate-50/80 transition-colors">
                      <td className="px-4 py-3 font-bold text-slate-800 flex items-center gap-2">
                        <span
                          className="w-2.5 h-2.5 rounded-full"
                          style={{ backgroundColor: STRATEGY_COLORS[m.strategy] || '#6366F1' }}
                        />
                        {m.strategy}
                      </td>
                      <td className="px-4 py-3 font-mono font-bold text-slate-900">${m.totalCostUsd.toFixed(2)}</td>
                      <td className="px-4 py-3 font-mono text-slate-600">${m.computeCostUsd.toFixed(2)}</td>
                      <td className="px-4 py-3 font-mono text-slate-600">${m.egressCostUsd.toFixed(2)}</td>
                      <td className="px-4 py-3 font-mono text-slate-600">${m.interruptionCostUsd.toFixed(2)}</td>
                      <td className="px-4 py-3">
                        <span className="font-semibold text-emerald-600 font-mono">
                          {m.deadlineHitRatePct}%
                        </span>
                      </td>
                      <td className="px-4 py-3 font-mono text-slate-700">{m.spotUsagePct}%</td>
                      <td className="px-4 py-3 font-mono text-slate-500 text-right">{m.runtimeMs} ms</td>
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
