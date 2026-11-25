import React from 'react';
import { NavLink } from 'react-router-dom';
import { Cpu, Layers, BarChart3, Database } from 'lucide-react';

export const Navbar: React.FC = () => {
  const linkClasses = ({ isActive }: { isActive: boolean }) =>
    `flex items-center gap-2 px-3.5 py-2 rounded-lg text-sm font-medium transition-colors ${
      isActive
        ? 'bg-indigo-600 text-white shadow-sm'
        : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
    }`;

  return (
    <header className="sticky top-0 z-50 bg-white/90 backdrop-blur border-b border-slate-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-600 to-violet-700 flex items-center justify-center text-white shadow-md">
            <Cpu className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-base font-bold text-slate-900 leading-tight">
              Multicloud GPU Optimizer
            </h1>
            <p className="text-xs text-slate-500">AWS • GCP • Azure AI Workloads</p>
          </div>
        </div>

        <nav className="flex items-center gap-1.5">
          <NavLink to="/" className={linkClasses}>
            <Cpu className="w-4 h-4" />
            <span>Single Job</span>
          </NavLink>
          <NavLink to="/batch" className={linkClasses}>
            <Layers className="w-4 h-4" />
            <span>Batch ILP</span>
          </NavLink>
          <NavLink to="/simulation" className={linkClasses}>
            <BarChart3 className="w-4 h-4" />
            <span>Simulations</span>
          </NavLink>
          <NavLink to="/prices" className={linkClasses}>
            <Database className="w-4 h-4" />
            <span>GPU Catalog</span>
          </NavLink>
        </nav>
      </div>
    </header>
  );
};
