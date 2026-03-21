-- ============================================
-- 创建服务对象表
-- ============================================

CREATE TABLE IF NOT EXISTS health_subject (
  subject_id bigint NOT NULL AUTO_INCREMENT COMMENT '服务对象ID',
  dept_id bigint DEFAULT NULL COMMENT '归属分组ID',
  subject_name varchar(30) NOT NULL COMMENT '服务对象名称',
  nick_name varchar(30) NOT NULL COMMENT '姓名',
  user_type varchar(2) DEFAULT '00' COMMENT '用户类型（00系统用户）',
  email varchar(50) DEFAULT '' COMMENT '用户邮箱',
  phonenumber varchar(11) DEFAULT '' COMMENT '手机号码',
  age int DEFAULT NULL COMMENT '年龄',
  sex char(1) DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  avatar varchar(100) DEFAULT '' COMMENT '头像地址',
  password varchar(100) DEFAULT '' COMMENT '密码',
  status char(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  del_flag char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  login_ip varchar(128) DEFAULT '' COMMENT '最后登录IP',
  login_date datetime DEFAULT NULL COMMENT '最后登录时间',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (subject_id),
  KEY idx_dept_id (dept_id),
  KEY idx_status (status),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='服务对象表';

-- 插入测试数据
INSERT INTO health_subject (dept_id, subject_name, nick_name, phonenumber, age, sex, status, create_by, create_time, remark) VALUES
(100, 'zhangsan', '张三', '13800138001', 75, '0', '0', 'admin', NOW(), '社区A组服务对象'),
(100, 'lisi', '李四', '13800138002', 82, '1', '0', 'admin', NOW(), '社区A组服务对象'),
(101, 'wangwu', '王五', '13800138003', 68, '0', '0', 'admin', NOW(), '社区B组服务对象');
