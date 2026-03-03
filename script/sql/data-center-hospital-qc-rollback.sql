-- 医院数据质控中心回滚脚本
-- 执行前请确认没有保留该模块业务数据的需求

-- 1) 先回收角色菜单关系
DELETE FROM sys_role_menu
WHERE menu_id IN (
  2250, 2251, 2252, 2253, 2255, 2256, 2257, 2258, 2259,
  2260, 2261, 2262, 2263, 2264, 2265, 2266, 2267, 2268, 2269,
  2270, 2271, 2272
);

-- 2) 删除菜单与权限
DELETE FROM sys_menu
WHERE menu_id IN (
  2250, 2251, 2252, 2253, 2255, 2256, 2257, 2258, 2259,
  2260, 2261, 2262, 2263, 2264, 2265, 2266, 2267, 2268, 2269,
  2270, 2271, 2272
);

-- 3) 删除业务表（会清空模块数据）
DROP TABLE IF EXISTS hospital_qc_ledger_item;
DROP TABLE IF EXISTS hospital_qc_ledger_query;
DROP TABLE IF EXISTS hospital_qc_datasource;
