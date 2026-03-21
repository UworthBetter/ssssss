-- ============================================
-- 修复系统工具菜单 - 恢复有用的功能
-- ============================================

-- 方案1：恢复系统工具下的代码生成器（开发需要）
UPDATE sys_menu 
SET visible = '0', status = '0' 
WHERE menu_id = 116;

-- 恢复代码生成器按钮权限
UPDATE sys_menu 
SET visible = '0' 
WHERE parent_id = 116;

-- 方案2：如果不需要系统工具，直接隐藏整个目录
-- UPDATE sys_menu SET visible = '1' WHERE menu_id = 3;

-- 确保超级管理员有权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (115, 116, 1055, 1056, 1057, 1058, 1059, 1060)
ON DUPLICATE KEY UPDATE role_id = role_id;
