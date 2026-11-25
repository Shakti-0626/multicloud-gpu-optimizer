export interface RecommendationItem {
  rank: number;
  provider: 'AWS' | 'GCP' | 'AZURE' | string;
  region: string;
  instance: string;
  pricingModel: 'ON_DEMAND' | 'SPOT';
  instancesNeeded: number;
  computeCost: number;
  egressCost: number;
  interruptionCost: number;
  totalCost: number;
  estFinishHours: number;
  explanation: string;
}

export interface RecommendationResponse {
  jobId: number | null;
  jobName: string;
  recommendations: RecommendationItem[];
  baselines: {
    aws?: number;
    gcp?: number;
    azure?: number;
  };
}

export interface JobFormData {
  name: string;
  gpuModel: string;
  gpuCount: number;
  durationHours: number;
  dataGb: number;
  dataProvider: string;
  dataRegion: string;
  deadlineHours: number;
  spotAllowed: boolean;
  checkpointing: boolean;
}

export interface ScheduledPlacementItem {
  jobName: string;
  provider: string;
  region: string;
  instance: string;
  pricingModel: string;
  instancesNeeded: number;
  computeCost: number;
  egressCost: number;
  interruptionCost: number;
  totalCost: number;
  estFinishHours: number;
  resourceGroup: string;
}

export interface BatchScheduleResponse {
  solved: boolean;
  totalCostUsd: number;
  solveTimeMs: number;
  totalJobs: number;
  savingsVsGreedyPct: number;
  savingsVsAllAwsPct: number;
  savingsVsAllGcpPct: number;
  savingsVsAllAzurePct: number;
  placements: ScheduledPlacementItem[];
  resourceGroupUsage: Record<string, number>;
}

export interface StrategyMetric {
  strategy: string;
  totalCostUsd: number;
  computeCostUsd: number;
  egressCostUsd: number;
  interruptionCostUsd: number;
  deadlineHitRatePct: number;
  spotUsagePct: number;
  egressSharePct: number;
  runtimeMs: number;
}

export interface SimulationResult {
  scenarioName: string;
  jobCount: number;
  executionTimeMs: number;
  strategyMetrics: StrategyMetric[];
  savingsVsBaselines: Record<string, number>;
  cloudPlacementDistribution: Record<string, number>;
}

export interface PriceItem {
  id: number;
  provider: string;
  instanceName: string;
  gpuModel: string;
  gpuCount: number;
  vcpu: number;
  memoryGb: number;
  region: string;
  pricingModel: string;
  hourlyPriceUsd: number;
  interruptionRatePct: number;
  avgQueueMinutes: number;
  asOfDate: string;
}
