-- 医院数据质控中心菜单与权限增量脚本（适用于已有数据库）
SET @data_center_parent_id = (
  SELECT menu_id
  FROM sys_menu
  WHERE menu_name = '数据中心' AND menu_type = 'M'
  ORDER BY menu_id DESC
  LIMIT 1
);
SET @data_center_parent_id = IFNULL(@data_center_parent_id, 2000);

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES
(2250, '医院数据质控中心', @data_center_parent_id, 20, 'hospital-qc', '', '', 1, 0, 'M', '0', '0', '', 'dashboard', 103, 1, NOW(), NULL, NULL, '医院数据质控中心目录'),
(2251, '数据源配置', 2250, 1, 'datasource', 'data-center/hospital-qc/datasource/index', '', 1, 0, 'C', '0', '0', 'data-center:hospital-qc:datasource:list', 'database', 103, 1, NOW(), NULL, NULL, ''),
(2252, '台账配置', 2250, 2, 'ledger-config', 'data-center/hospital-qc/ledger-config/index', '', 1, 0, 'C', '0', '0', 'data-center:hospital-qc:ledger-maintain:list', 'tree-table', 103, 1, NOW(), NULL, NULL, ''),
(2253, '台账数据大屏', 2250, 3, 'dashboard', 'data-center/hospital-qc/dashboard/index', '', 1, 0, 'C', '0', '0', 'data-center:hospital-qc:dashboard:list', 'area-chart', 103, 1, NOW(), NULL, NULL, ''),
(2264, '台账详细报表', 2253, 1, 'report', 'data-center/hospital-qc/report/index', '', 1, 1, 'C', '1', '0', 'data-center:hospital-qc:report:list', '#', 103, 1, NOW(), NULL, NULL, '/data-center/hospital-qc/dashboard'),
(2273, '台账明细数据', 2253, 2, 'detail', 'data-center/hospital-qc/detail/index', '', 1, 1, 'C', '1', '0', 'data-center:hospital-qc:report:detail', '#', 103, 1, NOW(), NULL, NULL, '/data-center/hospital-qc/dashboard'),

(2255, '数据源查询', 2251, 1, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:datasource:query', '#', 103, 1, NOW(), NULL, NULL, ''),
(2256, '数据源新增', 2251, 2, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:datasource:add', '#', 103, 1, NOW(), NULL, NULL, ''),
(2257, '数据源修改', 2251, 3, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:datasource:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(2258, '数据源删除', 2251, 4, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:datasource:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(2259, '数据源测试', 2251, 5, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:datasource:test', '#', 103, 1, NOW(), NULL, NULL, ''),

(2260, '台账树查询', 2252, 1, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:ledger-maintain:query', '#', 103, 1, NOW(), NULL, NULL, ''),
(2261, '台账树新增', 2252, 2, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:ledger-maintain:add', '#', 103, 1, NOW(), NULL, NULL, ''),
(2262, '台账树修改', 2252, 3, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:ledger-maintain:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(2263, '台账树删除', 2252, 4, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:ledger-maintain:remove', '#', 103, 1, NOW(), NULL, NULL, ''),

(2272, '查询配置列表', 2252, 5, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:ledger-query:list', '#', 103, 1, NOW(), NULL, NULL, ''),
(2265, '查询配置查询', 2252, 6, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:ledger-query:query', '#', 103, 1, NOW(), NULL, NULL, ''),
(2266, '查询配置新增', 2252, 7, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:ledger-query:add', '#', 103, 1, NOW(), NULL, NULL, ''),
(2267, '查询配置修改', 2252, 8, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:ledger-query:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(2268, '查询配置删除', 2252, 9, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:ledger-query:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(2269, '查询配置测试', 2252, 10, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:ledger-query:test', '#', 103, 1, NOW(), NULL, NULL, ''),

(2270, '大屏查询', 2253, 1, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:dashboard:list', '#', 103, 1, NOW(), NULL, NULL, ''),
(2271, '报表查询', 2264, 1, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:report:list', '#', 103, 1, NOW(), NULL, NULL, ''),
(2274, '明细查询', 2273, 1, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:hospital-qc:report:detail', '#', 103, 1, NOW(), NULL, NULL, '')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  path = VALUES(path),
  component = VALUES(component),
  query_param = VALUES(query_param),
  is_frame = VALUES(is_frame),
  is_cache = VALUES(is_cache),
  menu_type = VALUES(menu_type),
  visible = VALUES(visible),
  status = VALUES(status),
  perms = VALUES(perms),
  icon = VALUES(icon),
  update_by = 1,
  update_time = NOW(),
  remark = VALUES(remark);

INSERT IGNORE INTO sys_role_menu(role_id, menu_id) VALUES
(3, 2250),
(3, 2251),
(3, 2252),
(3, 2253),
(3, 2255),
(3, 2256),
(3, 2257),
(3, 2258),
(3, 2259),
(3, 2260),
(3, 2261),
(3, 2262),
(3, 2263),
(3, 2272),
(3, 2264),
(3, 2265),
(3, 2266),
(3, 2267),
(3, 2268),
(3, 2269),
(3, 2270),
(3, 2271),
(3, 2273),
(3, 2274);
