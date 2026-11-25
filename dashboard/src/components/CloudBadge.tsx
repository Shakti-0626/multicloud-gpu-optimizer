import React from 'react';

interface CloudBadgeProps {
  provider: string;
  className?: string;
}

export const CloudBadge: React.FC<CloudBadgeProps> = ({ provider, className = '' }) => {
  const p = provider.toUpperCase();
  if (p === 'AWS') {
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-900 border border-amber-300 ${className}`}>
        AWS
      </span>
    );
  }
  if (p === 'GCP') {
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-900 border border-blue-300 ${className}`}>
        GCP
      </span>
    );
  }
  if (p === 'AZURE') {
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-teal-100 text-teal-900 border border-teal-300 ${className}`}>
        Azure
      </span>
    );
  }
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-slate-100 text-slate-800 border border-slate-300 ${className}`}>
      {provider}
    </span>
  );
};
