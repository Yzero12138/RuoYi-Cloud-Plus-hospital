export interface HospitalQcReportQuery {
  timeType?: 'quarter' | 'month' | 'custom' | string;
  timeValue?: string;
  startTime?: string;
  endTime?: string;
  deptIds?: number[];
  ledgerCodes?: string[];
}

export interface HospitalQcReportRow {
  periodLabel?: string;
  deptId?: number;
  deptName?: string;
  ledgerCode?: string;
  ledgerName?: string;
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
