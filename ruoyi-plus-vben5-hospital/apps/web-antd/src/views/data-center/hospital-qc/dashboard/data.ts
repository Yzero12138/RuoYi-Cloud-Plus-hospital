import dayjs from 'dayjs';

export function buildYearOptions() {
  const currentYear = dayjs().year();
  return Array.from({ length: 8 }).map((_, index) => {
    const year = currentYear - index;
    return {
      label: String(year) + '\u5e74',
      value: year,
    };
  });
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
