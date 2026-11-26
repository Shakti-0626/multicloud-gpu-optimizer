import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { RecommendPage } from './pages/RecommendPage';
import { BatchSchedulerPage } from './pages/BatchSchedulerPage';
import { SimulationPage } from './pages/SimulationPage';
import { PriceExplorerPage } from './pages/PriceExplorerPage';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <div className="min-h-screen flex flex-col bg-slate-50">
        <Navbar />
        <main className="flex-1">
          <Routes>
            <Route path="/" element={<RecommendPage />} />
            <Route path="/batch" element={<BatchSchedulerPage />} />
            <Route path="/simulation" element={<SimulationPage />} />
            <Route path="/prices" element={<PriceExplorerPage />} />
          </Routes>
        </main>
        <footer className="border-t border-slate-200 py-6 text-center text-xs text-slate-500 bg-white">
          Carbon-Aware Multicloud Optimization for AI Workloads • Sustainable High-Performance Computing
        </footer>
      </div>
    </BrowserRouter>
  );
};

export default App;
