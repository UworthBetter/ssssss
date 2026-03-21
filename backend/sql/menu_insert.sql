-- 1. Insert Top Level Menu "AI Brain"
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('AI智慧大脑', 0, 10, 'ai', NULL, 1, 0, 'M', '0', '0', '', 'brain', 'admin', SYSDATE(), '', NULL, 'AI算法中心');

-- Get the ID of the new menu (Assuming it's X, but for script we use a variable or simply hardcode a known free ID range if we knew. For now, user might drag-drop in UI. This script serves as a template)

-- 2. Insert Sub Menu "Smart Cockpit"
-- Assuming Parent ID is @parentId. You can execute this in Navicat by replacing @parentId with the actual ID.
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('跌倒检测驾驶舱', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='AI智慧大脑' LIMIT 1) AS tmp), 1, 'cockpit', 'ai/cockpit', 1, 0, 'C', '0', '0', 'ai:cockpit:view', 'monitor', 'admin', SYSDATE(), '', NULL, '可视化展示中心');

-- 3. Insert Sub Menu "Fall Detection Management"
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('跌倒检测管理', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='AI智慧大脑' LIMIT 1) AS tmp), 2, 'fall', 'ai/fall/index', 1, 0, 'C', '0', '0', 'ai:fall:list', 'list', 'admin', SYSDATE(), '', NULL, '跌倒数据管理');

-- 4. Insert Sub Menu "Algorithm Dashboard"
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('算法总览', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='AI智慧大脑' LIMIT 1) AS tmp), 0, 'dashboard', 'ai/dashboard/index', 1, 0, 'C', '0', '0', 'ai:dashboard:view', 'dashboard', 'admin', SYSDATE(), '', NULL, '算法运行概览');

-- 5. Insert Sub Menu "Abnormal Detection"
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('异常检测验证', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='AI智慧大脑' LIMIT 1) AS tmp), 3, 'abnormal', 'ai/abnormal/index', 1, 0, 'C', '0', '0', 'ai:abnormal:view', 'monitor', 'admin', SYSDATE(), '', NULL, '异常验证实验室');

-- 6. Insert Sub Menu "Trend Analysis"
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('趋势分析预测', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='AI智慧大脑' LIMIT 1) AS tmp), 4, 'trend', 'ai/trend/index', 1, 0, 'C', '0', '0', 'ai:trend:view', 'chart', 'admin', SYSDATE(), '', NULL, '数据趋势预测');

-- 7. Insert Sub Menu "Risk Assessment"
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('综合风险评估', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='AI智慧大脑' LIMIT 1) AS tmp), 5, 'risk', 'ai/risk/index', 1, 0, 'C', '0', '0', 'ai:risk:view', 'dashboard', 'admin', SYSDATE(), '', NULL, '多因子风险评估');

-- 8. Insert Sub Menu "Data Quality Assessment"
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('数据质量概览', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='AI智慧大脑' LIMIT 1) AS tmp), 6, 'quality', 'ai/dataQuality/index', 1, 0, 'C', '0', '0', 'ai:quality:view', 'list', 'admin', SYSDATE(), '', NULL, '数据完整性检测');
