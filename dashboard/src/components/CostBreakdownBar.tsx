import React from 'react';

interface CostBreakdownBarProps {
  compute: number;
  egress: number;
  interruption: number;
  total: number;
}

export const CostBreakdownBar: React.FC<CostBreakdownBarProps> = ({ compute, egress, interruption, total }) => {
  if (total <= 0) return null;

  const computePct = Math.round((compute / total) * 100);
  const egressPct = Math.round((egress / total) * 100);
  const interruptionPct = Math.max(0, 100 - computePct - egressPct);

  return (
    <div className="w-full space-y-1.5">
      <div className="flex h-3.5 w-full overflow-hidden rounded-full bg-slate-200">
        {computePct > 0 && (
          <div
            style={{ width: `${computePct}%` }}
            className="bg-indigo-600 transition-all duration-300"
            title={`Compute: $${compute.toFixed(2)} (${computePct}%)`}
          />
        )}
        {egressPct > 0 && (
          <div
            style={{ width: `${egressPct}%` }}
            className="bg-purple-500 transition-all duration-300"
            title={`Egress: $${egress.toFixed(2)} (${egressPct}%)`}
          />
        )}
        {interruptionPct > 0 && (
          <div
            style={{ width: `${interruptionPct}%` }}
            className="bg-rose-500 transition-all duration-300"
            title={`Interruption Risk: $${interruption.toFixed(2)} (${interruptionPct}%)`}
          />
        )}
      </div>

      <div className="flex justify-between text-xs text-slate-500 font-mono">
        <span className="flex items-center gap-1">
          <span className="w-2 h-2 rounded-full bg-indigo-600"></span>
          Compute: ${compute.toFixed(2)} ({computePct}%)
        </span>
        <span className="flex items-center gap-1">
          <span className="w-2 h-2 rounded-full bg-purple-500"></span>
          Egress: ${egress.toFixed(2)} ({egressPct}%)
        </span>
        <span className="flex items-center gap-1">
          <span className="w-2 h-2 rounded-full bg-rose-500"></span>
          Risk: ${interruption.toFixed(2)} ({interruptionPct}%)
        </span>
      </div>
    </div>
  );
};
