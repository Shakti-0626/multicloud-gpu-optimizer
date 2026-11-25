import React, { useState } from 'react';
import axios from 'axios';
import { RecommendationResponse, JobFormData } from '../types';
import { CloudBadge } from '../components/CloudBadge';
import { CostBreakdownBar } from '../components/CostBreakdownBar';
import { Sparkles, ArrowRight, ShieldCheck, Clock, HardDrive, DollarSign, AlertCircle, CheckCircle2 } from 'lucide-react';

export const RecommendPage: React.FC = () => {
  const [formData, setFormData] = useState<JobFormData>({
    name: 'llm-finetune-llama3',
    gpuModel: 'A100',
    gpuCount: 4,
    durationHours: 10,
    dataGb: 200,
    dataProvider: 'AWS',
    dataRegion: 'us-east-1',
    deadlineHours: 24,
    spotAllowed: true,
    checkpointing: true,
  });

  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<RecommendationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const resp = await axios.post<RecommendationResponse>('/api/recommend', formData);
      setResult(resp.data);
    } catch (err: any) {
      console.error(err);
      const errorMsg = err.response?.data?.error 
        || (err.response?.status ? `Server responded with status ${err.response.status}` : null)
        || err.message 
        || 'Failed to connect to backend server. Ensure backend is running on port 8080.';
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const topPick = result?.recommendations?.[0];
  const otherPicks = result?.recommendations?.slice(1) || [];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Intro Header */}
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">AI Job Placement Recommender</h2>
        <p className="text-sm text-slate-600 mt-1">
          Evaluate cross-cloud GPU instances with mathematical cost models including compute, network egress, and interruption penalty.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Form Column */}
        <div className="lg:col-span-5 bg-white rounded-2xl border border-slate-200 shadow-sm p-6">
          <form onSubmit={handleSubmit} className="space-y-4">
            <h3 className="text-base font-semibold text-slate-900 border-b border-slate-100 pb-3 flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-indigo-600" />
              Job Specifications
            </h3>

            <div>
              <label className="block text-xs font-semibold uppercase text-slate-600 mb-1">Job Name</label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold uppercase text-slate-600 mb-1">GPU Model</label>
                <select
                  value={formData.gpuModel}
                  onChange={(e) => setFormData({ ...formData, gpuModel: e.target.value })}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                >
                  <option value="A100">A100 (80GB/40GB)</option>
                  <option value="H100">H100 (SXM/NVLink)</option>
                  <option value="A10G">A10G (24GB)</option>
                  <option value="L4">L4 (24GB)</option>
                  <option value="T4">T4 (16GB)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-slate-600 mb-1">GPU Count</label>
                <input
                  type="number"
                  min="1"
                  max="64"
                  value={formData.gpuCount}
                  onChange={(e) => setFormData({ ...formData, gpuCount: parseInt(e.target.value) || 1 })}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold uppercase text-slate-600 mb-1">Duration (Hours)</label>
                <input
                  type="number"
                  step="0.5"
                  min="0.1"
                  value={formData.durationHours}
                  onChange={(e) => setFormData({ ...formData, durationHours: parseFloat(e.target.value) || 1 })}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-slate-600 mb-1">Deadline (Hours)</label>
                <input
                  type="number"
                  step="0.5"
                  min="0.1"
                  value={formData.deadlineHours}
                  onChange={(e) => setFormData({ ...formData, deadlineHours: parseFloat(e.target.value) || 1 })}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-3 gap-2">
              <div>
                <label className="block text-xs font-semibold uppercase text-slate-600 mb-1">Data (GB)</label>
                <input
                  type="number"
                  min="0"
                  value={formData.dataGb}
                  onChange={(e) => setFormData({ ...formData, dataGb: parseFloat(e.target.value) || 0 })}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-slate-600 mb-1">Data Source</label>
                <select
                  value={formData.dataProvider}
                  onChange={(e) => setFormData({ ...formData, dataProvider: e.target.value })}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                >
                  <option value="AWS">AWS</option>
                  <option value="GCP">GCP</option>
                  <option value="AZURE">Azure</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-slate-600 mb-1">Data Region</label>
                <select
                  value={formData.dataRegion}
                  onChange={(e) => setFormData({ ...formData, dataRegion: e.target.value })}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                >
                  <option value="us-east-1">us-east-1 (AWS)</option>
                  <option value="us-west-2">us-west-2 (AWS)</option>
                  <option value="us-central1">us-central1 (GCP)</option>
                  <option value="europe-west4">europe-west4 (GCP)</option>
                  <option value="eastus">eastus (Azure)</option>
                  <option value="westeurope">westeurope (Azure)</option>
                </select>
              </div>
            </div>

            <div className="pt-2 border-t border-slate-100 space-y-2">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={formData.spotAllowed}
                  onChange={(e) => setFormData({ ...formData, spotAllowed: e.target.checked })}
                  className="w-4 h-4 text-indigo-600 rounded focus:ring-indigo-500"
                />
                <span className="text-xs font-medium text-slate-700">Allow Spot Instances (High savings with modeled risk)</span>
              </label>

              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={formData.checkpointing}
                  onChange={(e) => setFormData({ ...formData, checkpointing: e.target.checked })}
                  className="w-4 h-4 text-indigo-600 rounded focus:ring-indigo-500"
                />
                <span className="text-xs font-medium text-slate-700">Checkpointing Enabled (Limits work loss to 10%)</span>
              </label>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-4 flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-medium text-sm transition-all shadow-sm hover:shadow disabled:opacity-50"
            >
              {loading ? 'Solving Cost Model...' : 'Calculate Optimal Placement'}
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>
        </div>

        {/* Results Column */}
        <div className="lg:col-span-7 space-y-6">
          {error && (
            <div className="p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-sm flex items-center gap-2">
              <AlertCircle className="w-5 h-5 text-rose-600 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {!result && !loading && !error && (
            <div className="bg-white rounded-2xl border border-slate-200 p-12 text-center text-slate-500">
              <Sparkles className="w-10 h-10 text-indigo-400 mx-auto mb-3" />
              <h4 className="text-base font-semibold text-slate-800">Ready for Recommendation</h4>
              <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
                Submit the job details to rank placements across AWS, GCP, and Azure with cost and egress breakdown.
              </p>
            </div>
          )}

          {result && (
            <>
              {/* Single-cloud baseline comparison pills */}
              <div className="bg-slate-900 text-white rounded-2xl p-5 shadow-sm space-y-3">
                <div className="flex items-center justify-between text-xs text-slate-300">
                  <span className="font-semibold uppercase tracking-wider">Single-Cloud Baselines</span>
                  <span>Job: {result.jobName}</span>
                </div>
                <div className="grid grid-cols-3 gap-3">
                  <div className="bg-slate-800/80 p-3 rounded-xl border border-slate-700">
                    <div className="text-xs text-amber-400 font-medium">All-AWS Min</div>
                    <div className="text-lg font-bold font-mono text-white">
                      ${result.baselines?.aws?.toFixed(2) ?? 'N/A'}
                    </div>
                  </div>
                  <div className="bg-slate-800/80 p-3 rounded-xl border border-slate-700">
                    <div className="text-xs text-blue-400 font-medium">All-GCP Min</div>
                    <div className="text-lg font-bold font-mono text-white">
                      ${result.baselines?.gcp?.toFixed(2) ?? 'N/A'}
                    </div>
                  </div>
                  <div className="bg-slate-800/80 p-3 rounded-xl border border-slate-700">
                    <div className="text-xs text-teal-400 font-medium">All-Azure Min</div>
                    <div className="text-lg font-bold font-mono text-white">
                      ${result.baselines?.azure?.toFixed(2) ?? 'N/A'}
                    </div>
                  </div>
                </div>
              </div>

              {/* Rank 1 Featured Card */}
              {topPick && (
                <div className="bg-white rounded-2xl border-2 border-indigo-500/80 shadow-md p-6 space-y-5 relative overflow-hidden">
                  <div className="absolute top-0 right-0 bg-indigo-600 text-white text-[11px] font-bold px-3 py-1 rounded-bl-xl uppercase tracking-wider flex items-center gap-1">
                    <CheckCircle2 className="w-3.5 h-3.5" /> Optimal Placement #1
                  </div>

                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-2 mb-1.5">
                        <CloudBadge provider={topPick.provider} />
                        <span className="text-xs font-semibold px-2 py-0.5 rounded bg-slate-100 text-slate-700">
                          {topPick.pricingModel}
                        </span>
                        <span className="text-xs font-mono text-slate-500">{topPick.region}</span>
                      </div>
                      <h3 className="text-xl font-bold text-slate-900 font-mono">
                        {topPick.instance}
                      </h3>
                      <p className="text-xs text-slate-500 mt-0.5">
                        {topPick.instancesNeeded} instance(s) required to fulfill {formData.gpuCount}x {formData.gpuModel}
                      </p>
                    </div>

                    <div className="text-right">
                      <div className="text-xs text-slate-500 font-medium">Total Est. Cost</div>
                      <div className="text-3xl font-extrabold text-indigo-600 font-mono">
                        ${topPick.totalCost.toFixed(2)}
                      </div>
                      <div className="text-[11px] text-slate-500 mt-1 flex items-center justify-end gap-1">
                        <Clock className="w-3 h-3" /> ~{topPick.estFinishHours}h completion
                      </div>
                    </div>
                  </div>

                  {/* Cost breakdown progress bar */}
                  <div className="bg-slate-50 rounded-xl p-4 border border-slate-100">
                    <CostBreakdownBar
                      compute={topPick.computeCost}
                      egress={topPick.egressCost}
                      interruption={topPick.interruptionCost}
                      total={topPick.totalCost}
                    />
                  </div>

                  {/* Plain language explanation */}
                  <div className="p-4 rounded-xl bg-indigo-50/70 border border-indigo-100 text-xs text-indigo-950 leading-relaxed">
                    <span className="font-semibold text-indigo-900 block mb-1 flex items-center gap-1.5">
                      <Sparkles className="w-3.5 h-3.5 text-indigo-600" /> System Recommendation Rationale:
                    </span>
                    {topPick.explanation}
                  </div>
                </div>
              )}

              {/* Smaller Ranked Alternatives */}
              {otherPicks.length > 0 && (
                <div className="space-y-3">
                  <h4 className="text-xs font-bold text-slate-500 uppercase tracking-wider">
                    Ranked Alternative Placements
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {otherPicks.map((pick) => (
                      <div
                        key={`${pick.instance}-${pick.region}-${pick.pricingModel}`}
                        className="bg-white rounded-xl border border-slate-200 p-4 space-y-3 hover:border-slate-300 transition-colors shadow-sm"
                      >
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-1.5">
                            <span className="text-xs font-bold text-slate-400">#{pick.rank}</span>
                            <CloudBadge provider={pick.provider} />
                            <span className="text-[11px] px-1.5 py-0.5 rounded bg-slate-100 font-medium">
                              {pick.pricingModel}
                            </span>
                          </div>
                          <div className="text-sm font-bold font-mono text-slate-800">
                            ${pick.totalCost.toFixed(2)}
                          </div>
                        </div>

                        <div className="font-mono text-xs font-semibold text-slate-700">
                          {pick.instance} <span className="text-slate-400 font-normal">({pick.region})</span>
                        </div>

                        <CostBreakdownBar
                          compute={pick.computeCost}
                          egress={pick.egressCost}
                          interruption={pick.interruptionCost}
                          total={pick.totalCost}
                        />
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};
