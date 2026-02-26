-- 肾内科台账菜单增量脚本（适用于已有数据库）
SET @nephrology_parent_id = (
  SELECT menu_id
  FROM sys_menu
  WHERE menu_name = '肾内科' AND menu_type = 'M'
  ORDER BY menu_id DESC
  LIMIT 1
);
SET @nephrology_parent_id = IFNULL(@nephrology_parent_id, 2001);

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES
(2135, '台账', @nephrology_parent_id, 8, 'ledger', 'data-center/nephrology/ledger/index', '', 1, 0, 'C', '0', '0', 'data-center:nephrology:ledger:list', 'table', 103, 1, NOW(), NULL, NULL, '肾内科台账菜单'),
(2136, '台账维护', @nephrology_parent_id, 9, 'ledger-maintain', 'data-center/nephrology/ledger-maintain/index', '', 1, 0, 'C', '0', '0', 'data-center:nephrology:ledger-maintain:list', 'tree-table', 103, 1, NOW(), NULL, NULL, '肾内科台账维护菜单'),
(2137, '台账详情', 2135, 1, 'detail', 'data-center/nephrology/ledger/detail/index', '', 1, 1, 'C', '1', '0', 'data-center:nephrology:ledger:detail:list', '#', 103, 1, NOW(), NULL, NULL, '/data-center/nephrology/ledger'),
(2138, '台账详情查询', 2135, 1, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:nephrology:ledger:detail:list', '#', 103, 1, NOW(), NULL, NULL, ''),
(2139, '台账维护查询', 2136, 1, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:nephrology:ledger-maintain:query', '#', 103, 1, NOW(), NULL, NULL, ''),
(2140, '台账维护新增', 2136, 2, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:nephrology:ledger-maintain:add', '#', 103, 1, NOW(), NULL, NULL, ''),
(2141, '台账维护修改', 2136, 3, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:nephrology:ledger-maintain:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(2142, '台账维护删除', 2136, 4, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:nephrology:ledger-maintain:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(2143, '台账查询配置', @nephrology_parent_id, 10, 'ledger-query', 'data-center/nephrology/ledger-query/index', '', 1, 0, 'C', '0', '0', 'data-center:nephrology:ledger-query:list', 'code', 103, 1, NOW(), NULL, NULL, '肾内科台账查询配置菜单'),
(2144, '台账查询配置查询', 2143, 1, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:nephrology:ledger-query:query', '#', 103, 1, NOW(), NULL, NULL, ''),
(2145, '台账查询配置新增', 2143, 2, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:nephrology:ledger-query:add', '#', 103, 1, NOW(), NULL, NULL, ''),
(2146, '台账查询配置修改', 2143, 3, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:nephrology:ledger-query:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(2147, '台账查询配置删除', 2143, 4, '#', '', '', 1, 0, 'F', '0', '0', 'data-center:nephrology:ledger-query:remove', '#', 103, 1, NOW(), NULL, NULL, '')
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
(3, 2135),
(3, 2136),
(3, 2137),
(3, 2138),
(3, 2139),
(3, 2140),
(3, 2141),
(3, 2142),
(3, 2143),
(3, 2144),
(3, 2145),
(3, 2146),
(3, 2147);

