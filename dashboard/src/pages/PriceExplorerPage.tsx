import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { PriceItem } from '../types';
import { CloudBadge } from '../components/CloudBadge';
import { Database, Filter, Search } from 'lucide-react';

export const PriceExplorerPage: React.FC = () => {
  const [prices, setPrices] = useState<PriceItem[]>([]);
  const [provider, setProvider] = useState<string>('');
  const [gpuModel, setGpuModel] = useState<string>('');
  const [pricingModel, setPricingModel] = useState<string>('');
  const [search, setSearch] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);

  const fetchPrices = async () => {
    setLoading(true);
    try {
      const params: Record<string, string> = {};
      if (provider) params.provider = provider;
      if (gpuModel) params.gpuModel = gpuModel;
      if (pricingModel) params.pricingModel = pricingModel;

      const resp = await axios.get<PriceItem[]>('/api/prices', { params });
      setPrices(resp.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPrices();
  }, [provider, gpuModel, pricingModel]);

  const filteredPrices = prices.filter((p) => {
    if (!search) return true;
    const term = search.toLowerCase();
    return (
      p.instanceName.toLowerCase().includes(term) ||
      p.region.toLowerCase().includes(term) ||
      p.gpuModel.toLowerCase().includes(term)
    );
  });

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">
          Cloud GPU Instance & Price Catalog
        </h2>
        <p className="text-sm text-slate-600 mt-1">
          Historical & snapshot pricing across AWS, GCP, and Azure with modeled spot interruption bands and queue expectations.
        </p>
      </div>

      {/* Filter Bar */}
      <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm flex flex-wrap items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-3">
          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Search instance or region..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-9 pr-3 py-1.5 text-xs border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none w-56"
            />
          </div>

          <select
            value={provider}
            onChange={(e) => setProvider(e.target.value)}
            className="px-3 py-1.5 text-xs border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
          >
            <option value="">All Clouds</option>
            <option value="AWS">AWS</option>
            <option value="GCP">GCP</option>
            <option value="AZURE">Azure</option>
          </select>

          <select
            value={gpuModel}
            onChange={(e) => setGpuModel(e.target.value)}
            className="px-3 py-1.5 text-xs border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
          >
            <option value="">All GPU Models</option>
            <option value="A100">A100</option>
            <option value="H100">H100</option>
            <option value="A10G">A10G</option>
            <option value="L4">L4</option>
            <option value="T4">T4</option>
          </select>

          <select
            value={pricingModel}
            onChange={(e) => setPricingModel(e.target.value)}
            className="px-3 py-1.5 text-xs border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
          >
            <option value="">All Pricing Models</option>
            <option value="ON_DEMAND">On-Demand</option>
            <option value="SPOT">Spot</option>
          </select>
        </div>

        <div className="text-xs text-slate-500 font-mono">
          Showing {filteredPrices.length} instances
        </div>
      </div>

      {/* Catalog Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-200">
              <tr>
                <th className="px-4 py-3">Cloud</th>
                <th className="px-4 py-3">Instance Type</th>
                <th className="px-4 py-3">GPU Specs</th>
                <th className="px-4 py-3">vCPU / Mem</th>
                <th className="px-4 py-3">Region</th>
                <th className="px-4 py-3">Model</th>
                <th className="px-4 py-3">Hourly Rate</th>
                <th className="px-4 py-3">Interruption Risk</th>
                <th className="px-4 py-3">Avg Queue</th>
                <th className="px-4 py-3 text-right">As of Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filteredPrices.map((p) => (
                <tr key={p.id} className="hover:bg-slate-50/80 transition-colors">
                  <td className="px-4 py-3">
                    <CloudBadge provider={p.provider} />
                  </td>
                  <td className="px-4 py-3 font-mono font-bold text-slate-800">{p.instanceName}</td>
                  <td className="px-4 py-3 font-mono text-slate-700">
                    {p.gpuCount}x {p.gpuModel}
                  </td>
                  <td className="px-4 py-3 text-slate-500">
                    {p.vcpu} vCPU / {p.memoryGb} GB
                  </td>
                  <td className="px-4 py-3 text-slate-600 font-mono">{p.region}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`px-2 py-0.5 rounded text-[11px] font-semibold ${
                        p.pricingModel === 'SPOT'
                          ? 'bg-amber-100 text-amber-800'
                          : 'bg-slate-100 text-slate-700'
                      }`}
                    >
                      {p.pricingModel}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-mono font-bold text-slate-900">
                    ${p.hourlyPriceUsd.toFixed(3)}/hr
                  </td>
                  <td className="px-4 py-3 font-mono">
                    {p.pricingModel === 'SPOT' ? (
                      <span className="text-amber-700 font-medium">{p.interruptionRatePct}% band</span>
                    ) : (
                      <span className="text-slate-400">0.0% (Guaranteed)</span>
                    )}
                  </td>
                  <td className="px-4 py-3 text-slate-500 font-mono">~{p.avgQueueMinutes} min</td>
                  <td className="px-4 py-3 text-slate-400 text-right font-mono">{p.asOfDate}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
