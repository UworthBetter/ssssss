-- 添加"算法可视化大屏"菜单
-- 父菜单：AI智慧大脑

-- 插入算法可视化大屏菜单
INSERT INTO `sys_menu`
(`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
('算法可视化大屏',
 (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='AI智慧大脑' LIMIT 1) AS tmp),
 7,
 'algorithm-dashboard',
 'ai/algorithm-dashboard/index',
 1,
 0,
 'C',
 '0',
 '0',
 'ai:algorithm:dashboard',
 'monitor',
 'admin',
 SYSDATE(),
 '',
 NULL,
 'AI算法可视化分析大屏');

-- 查询验证
-- SELECT * FROM sys_menu WHERE menu_name = '算法可视化大屏';
