import dayjs from 'dayjs';

export type TimeDimension = 'month' | 'quarter' | 'year';

export function timeDimensionOptions() {
  return [
    { label: '年度', value: 'year' },
    { label: '季度', value: 'quarter' },
    { label: '月度', value: 'month' },
  ];
}

export function buildYearValueOptions() {
  const currentYear = dayjs().year();
  return Array.from({ length: 4 }).map((_, index) => {
    const year = currentYear - index;
    return {
      label: `${year}年`,
      value: String(year),
    };
  });
}

export function buildQuarterValueOptions() {
  const now = dayjs();
  const currentYear = now.year();
  const currentQuarter = Math.ceil((now.month() + 1) / 3);
  const options: Array<{ label: string; value: string }> = [];

  let year = currentYear;
  let quarter = currentQuarter;
  for (let i = 0; i < 12; i++) {
    options.push({
      label: `${year}年Q${quarter}`,
      value: `${year}-Q${quarter}`,
    });
    quarter--;
    if (quarter < 1) {
      quarter = 4;
      year--;
    }
  }
  return options;
}

export function buildMonthValueOptions() {
  return Array.from({ length: 24 }).map((_, index) => {
    const month = dayjs().subtract(index, 'month');
    return {
      label: month.format('YYYY-MM'),
      value: month.format('YYYY-MM'),
    };
  });
}

export function buildTimeValueOptions(dimension: TimeDimension) {
  if (dimension === 'year') {
    return buildYearValueOptions();
  }
  if (dimension === 'month') {
    return buildMonthValueOptions();
  }
  return buildQuarterValueOptions();
}

export function getDefaultTimeValue(dimension: TimeDimension): string {
  const now = dayjs();
  if (dimension === 'year') {
    return String(now.year());
  }
  if (dimension === 'month') {
    return now.format('YYYY-MM');
  }
  return `${now.year()}-Q${Math.ceil((now.month() + 1) / 3)}`;
}

export function trendColor(trend?: string) {
  if (trend === 'UP') {
    return '#16a34a';
  }
  if (trend === 'DOWN') {
    return '#dc2626';
  }
  return '#6b7280';
}

export function trendText(trend?: string) {
  if (trend === 'UP') {
    return '\u4e0a\u5347';
  }
  if (trend === 'DOWN') {
    return '\u4e0b\u964d';
  }
  if (trend === 'FLAT') {
    return '\u6301\u5e73';
  }
  return '\u65e0\u6570\u636e';
}
