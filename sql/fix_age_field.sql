-- 添加 age 字段到 sys_user 表
USE `ueit_jkpt`;

ALTER TABLE `sys_user` 
ADD COLUMN `age` INT(3) DEFAULT NULL COMMENT '年龄' AFTER `phonenumber`;

-- 验证字段是否添加成功
DESCRIBE sys_user;
