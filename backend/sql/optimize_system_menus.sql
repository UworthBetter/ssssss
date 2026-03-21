-- ============================================
-- 系统菜单优化脚本 - 精简为健康监测场景
-- ============================================
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 1. 删除与业务无关的菜单项（软删除，改为隐藏状态）
UPDATE sys_menu SET visible = '1', status = '1' WHERE menu_id IN (
    104,  -- 岗位管理
    115,  -- 表单构建（低代码，比赛不需要）
    116,  -- 代码生成（开发工具，运行时不需要）
    117,  -- 系统接口（Swagger，生产环境关闭）
    4     -- 若依官网外链
);

-- 2. 删除相关按钮权限
UPDATE sys_menu SET visible = '1' WHERE parent_id IN (104, 115, 116, 117);

-- 3. 改造部门管理为"监测分组管理"
UPDATE sys_menu SET 
    menu_name = '监测分组',
    component = 'system/dept/index',
    remark = '服务对象分组管理（如：社区A、社区B、家庭组）'
WHERE menu_id = 103;

-- 4. 改造用户管理为"服务对象管理"
UPDATE sys_menu SET 
    menu_name = '服务对象管理',
    remark = '管理服务对象档案信息'
WHERE menu_id = 100;

-- 5. 保留系统监控但精简
-- 只保留：在线用户、服务监控、缓存监控
UPDATE sys_menu SET visible = '1' WHERE menu_id IN (
    110,  -- 定时任务（暂时隐藏，后续可用）
    111,  -- 数据监控（Druid，生产环境建议关闭）
    114   -- 缓存列表
);

-- 6. 添加健康监测专用菜单
-- 先检查是否已存在
DELETE FROM sys_menu WHERE menu_id BETWEEN 3000 AND 3100;

-- 健康监测中心菜单组
INSERT INTO sys_menu VALUES 
(3000, '健康监测', 0, 5, 'health', NULL, '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', NOW(), '', NULL, '健康监测核心业务'),
(3001, '服务对象管理', 3000, 1, 'subject', 'health/subject/index', '', 1, 0, 'C', '0', '0', 'health:subject:list', 'user', 'admin', NOW(), '', NULL, '服务对象档案管理'),
(3002, '设备管理', 3000, 2, 'device', 'health/device/index', '', 1, 0, 'C', '0', '0', 'health:device:list', 'phone', 'admin', NOW(), '', NULL, '智能穿戴设备管理'),
(3003, '实时监测大屏', 3000, 3, 'dashboard', 'health/dashboard/index', '', 1, 0, 'C', '0', '0', 'health:dashboard:view', 'dashboard', 'admin', NOW(), '', NULL, '实时数据可视化'),
(3004, '告警管理', 3000, 4, 'alert', 'health/alert/index', '', 1, 0, 'C', '0', '0', 'health:alert:list', 'bell', 'admin', NOW(), '', NULL, '异常告警处理'),
(3005, '健康报告', 3000, 5, 'report', 'health/report/index', '', 1, 0, 'C', '0', '0', 'health:report:list', 'pdf', 'admin', NOW(), '', NULL, '健康分析报告'),
(3006, '算法监控', 3000, 6, 'algorithm', 'ai/dashboard/index', '', 1, 0, 'C', '0', '0', 'ai:dashboard:view', 'brain', 'admin', NOW(), '', NULL, 'AI算法运行监控');

-- 服务对象管理按钮权限
INSERT INTO sys_menu VALUES 
(3100, '服务对象查询', 3001, 1, '', '', '', 1, 0, 'F', '0', '0', 'health:subject:query', '#', 'admin', NOW(), '', NULL, ''),
(3101, '服务对象新增', 3001, 2, '', '', '', 1, 0, 'F', '0', '0', 'health:subject:add', '#', 'admin', NOW(), '', NULL, ''),
(3102, '服务对象修改', 3001, 3, '', '', '', 1, 0, 'F', '0', '0', 'health:subject:edit', '#', 'admin', NOW(), '', NULL, ''),
(3103, '服务对象删除', 3001, 4, '', '', '', 1, 0, 'F', '0', '0', 'health:subject:remove', '#', 'admin', NOW(), '', NULL, ''),
(3104, '设备绑定', 3001, 5, '', '', '', 1, 0, 'F', '0', '0', 'health:subject:bind', '#', 'admin', NOW(), '', NULL, '');

-- 设备管理按钮权限
INSERT INTO sys_menu VALUES 
(3110, '设备查询', 3002, 1, '', '', '', 1, 0, 'F', '0', '0', 'health:device:query', '#', 'admin', NOW(), '', NULL, ''),
(3111, '设备新增', 3002, 2, '', '', '', 1, 0, 'F', '0', '0', 'health:device:add', '#', 'admin', NOW(), '', NULL, ''),
(3112, '设备修改', 3002, 3, '', '', '', 1, 0, 'F', '0', '0', 'health:device:edit', '#', 'admin', NOW(), '', NULL, ''),
(3113, '设备解绑', 3002, 4, '', '', '', 1, 0, 'F', '0', '0', 'health:device:unbind', '#', 'admin', NOW(), '', NULL, '');

-- 告警管理按钮权限
INSERT INTO sys_menu VALUES 
(3120, '告警查询', 3004, 1, '', '', '', 1, 0, 'F', '0', '0', 'health:alert:query', '#', 'admin', NOW(), '', NULL, ''),
(3121, '告警处理', 3004, 2, '', '', '', 1, 0, 'F', '0', '0', 'health:alert:handle', '#', 'admin', NOW(), '', NULL, ''),
(3122, '告警导出', 3004, 3, '', '', '', 1, 0, 'F', '0', '0', 'health:alert:export', '#', 'admin', NOW(), '', NULL, '');

-- 7. 修改字典数据为健康监测场景
-- 修改 sys_user_sex 为服务对象性别
UPDATE sys_dict_type SET dict_name = '性别', remark = '服务对象性别' WHERE dict_type = 'sys_user_sex';

-- 添加健康监测专用字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('风险等级', 'health_risk_level', '0', 'admin', NOW(), '健康风险分级'),
('告警类型', 'health_alert_type', '0', 'admin', NOW(), '系统告警分类'),
('设备类型', 'health_device_type', '0', 'admin', NOW(), '监测设备类型'),
('处理状态', 'health_handle_status', '0', 'admin', NOW(), '告警处理状态');

-- 风险等级字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time) VALUES
(1, '正常', 'normal', 'health_risk_level', 'success', 'Y', '0', 'admin', NOW()),
(2, '低风险', 'low', 'health_risk_level', 'info', 'N', '0', 'admin', NOW()),
(3, '中风险', 'medium', 'health_risk_level', 'warning', 'N', '0', 'admin', NOW()),
(4, '高风险', 'high', 'health_risk_level', 'danger', 'N', '0', 'admin', NOW()),
(5, '严重', 'critical', 'health_risk_level', 'danger', 'N', '0', 'admin', NOW());

-- 告警类型字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time) VALUES
(1, '心率异常', 'heart_rate', 'health_alert_type', 'danger', 'N', '0', 'admin', NOW()),
(2, '血压异常', 'blood_pressure', 'health_alert_type', 'warning', 'N', '0', 'admin', NOW()),
(3, '跌倒检测', 'fall', 'health_alert_type', 'danger', 'N', '0', 'admin', NOW()),
(4, '体温异常', 'temperature', 'health_alert_type', 'warning', 'N', '0', 'admin', NOW()),
(5, '血氧异常', 'spo2', 'health_alert_type', 'warning', 'N', '0', 'admin', NOW()),
(6, '电子围栏', 'fence', 'health_alert_type', 'info', 'N', '0', 'admin', NOW()),
(7, '设备离线', 'device_offline', 'health_alert_type', 'info', 'N', '0', 'admin', NOW());

-- 处理状态字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time) VALUES
(1, '未处理', '0', 'health_handle_status', 'danger', 'N', '0', 'admin', NOW()),
(2, '处理中', '1', 'health_handle_status', 'warning', 'N', '0', 'admin', NOW()),
(3, '已处理', '2', 'health_handle_status', 'success', 'N', '0', 'admin', NOW()),
(4, '已忽略', '3', 'health_handle_status', 'info', 'N', '0', 'admin', NOW());

-- 8. 将新的健康监测菜单分配给超级管理员角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 3000 AND 3122
ON DUPLICATE KEY UPDATE role_id = role_id;
