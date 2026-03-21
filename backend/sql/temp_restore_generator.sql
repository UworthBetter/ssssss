-- ============================================
-- 临时恢复代码生成器（开发阶段使用）
-- ============================================

-- 恢复代码生成器菜单显示
UPDATE sys_menu 
SET visible = '0', status = '0' 
WHERE menu_id = 116;

-- 恢复代码生成按钮权限
UPDATE sys_menu 
SET visible = '0' 
WHERE parent_id = 116;

-- 确保超级管理员有权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id = 116
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 同时恢复表单构建器（可选）
UPDATE sys_menu 
SET visible = '0', status = '0' 
WHERE menu_id = 115;

-- 刷新后可在【系统工具】-【代码生成】中使用
