export interface HospitalQcDetailQuery {
  /** 台账编码 */
  ledgerCode: string;
  /** 科室ID列表 */
  deptIds?: number[];
  /** 开始时间 */
  startTime: string;
  /** 结束时间 */
  endTime: string;
  /** 节点类型: N=分子, D=分母 */
  nodeType?: string;
  /** 页码 */
  pageNum?: number;
  /** 每页条数 */
  pageSize?: number;
}

export interface HospitalQcDetailPageResult {
  /** 总条数 */
  total: number;
  /** 数据列表 */
  records: Record<string, any>[];
}
