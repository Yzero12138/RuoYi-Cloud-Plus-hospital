export interface HospitalQcOptionItem {
  value: string;
  label: string;
}

export interface HospitalQcDashboardFilterOptions {
  deptOptions?: HospitalQcOptionItem[];
  ledgerOptions?: HospitalQcOptionItem[];
}

export interface HospitalQcDashboardQuarterMetric {
  quarter?: string;
  numerator?: number;
  denominator?: number;
  indicatorPercent?: number | null;
  qoqGrowth?: number | null;
  percentagePointChange?: number | null;
  noDenominator?: boolean;
  percentDisplay?: string;
  growthDisplay?: string;
  pointChangeDisplay?: string;
}

export interface HospitalQcTrendSeries {
  ledgerCode?: string;
  ledgerName?: string;
  quarterMetrics?: HospitalQcDashboardQuarterMetric[];
}

export interface HospitalQcDashboardKpi {
  numerator?: number;
  denominator?: number;
  indicatorPercent?: number | null;
  qoqGrowth?: number | null;
  percentagePointChange?: number | null;
  percentDisplay?: string;
  growthDisplay?: string;
  pointChangeDisplay?: string;
  noDenominator?: boolean;
}

export interface HospitalQcDeptRanking {
  deptId?: number;
  deptName?: string;
  numerator?: number;
  denominator?: number;
  indicatorPercent?: number | null;
  qoqGrowth?: number | null;
  percentagePointChange?: number | null;
  trend?: 'UP' | 'DOWN' | 'FLAT' | 'NONE' | string;
  percentDisplay?: string;
  growthDisplay?: string;
  pointChangeDisplay?: string;
}

export interface HospitalQcDashboardOverview {
  year?: number;
  quarters?: string[];
  kpi?: HospitalQcDashboardKpi;
  trends?: HospitalQcTrendSeries[];
  rankings?: HospitalQcDeptRanking[];
  noDenominatorRule?: string;
}

export interface HospitalQcDashboardQuery {
  year?: number;
  deptIds?: number[];
  ledgerCodes?: string[];
}
