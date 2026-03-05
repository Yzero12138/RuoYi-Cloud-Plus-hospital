export interface HospitalQcOptionItem {
  value: string;
  label: string;
}

export interface HospitalQcDashboardFilterOptions {
  /** 是否管理员 */
  isAdmin?: boolean;
  /** 当前科室ID */
  currentDeptId?: number;
  /** 当前科室名称 */
  currentDeptName?: string;
  /** 允许访问的科室ID列表（空表示全部） */
  allowedDeptIds?: number[];
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
  /** 季度窗口大小（可选，默认使用配置值） */
  quarterWindow?: number;
  deptIds?: number[];
  ledgerCodes?: string[];
}
