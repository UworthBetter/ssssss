-- =============================================
-- 演示环境一键造数脚本（可重复执行）
-- 目标：让仪表盘、设备、异常、AI日志、服务对象页面都具备可展示数据
-- 使用方式：mysql -u用户名 -p 数据库名 < backend/sql/demo_seed_complete.sql
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- 0) 兜底：确保 AI 与服务对象主表存在（若已存在则不会覆盖）
CREATE TABLE IF NOT EXISTS ai_abnormal_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  device_id VARCHAR(64) DEFAULT NULL,
  metric_type VARCHAR(32) NOT NULL,
  abnormal_value VARCHAR(64) NOT NULL,
  normal_range VARCHAR(64) DEFAULT NULL,
  abnormal_type VARCHAR(32) DEFAULT NULL,
  risk_level VARCHAR(16) DEFAULT NULL,
  detection_method VARCHAR(32) DEFAULT NULL,
  detected_time DATETIME NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS health_subject (
  subject_id BIGINT NOT NULL AUTO_INCREMENT,
  dept_id BIGINT DEFAULT NULL,
  subject_name VARCHAR(30) NOT NULL,
  nick_name VARCHAR(30) NOT NULL,
  user_type VARCHAR(2) DEFAULT '00',
  email VARCHAR(50) DEFAULT '',
  phonenumber VARCHAR(11) DEFAULT '',
  age INT DEFAULT NULL,
  sex CHAR(1) DEFAULT '0',
  avatar VARCHAR(100) DEFAULT '',
  password VARCHAR(100) DEFAULT '',
  status CHAR(1) DEFAULT '0',
  del_flag CHAR(1) DEFAULT '0',
  login_ip VARCHAR(128) DEFAULT '',
  login_date DATETIME DEFAULT NULL,
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT NULL,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 1) 清理上一轮演示数据
DELETE FROM sys_user_role WHERE user_id BETWEEN 9101 AND 9110;
DELETE FROM sys_user_post WHERE user_id BETWEEN 9101 AND 9110;
DELETE FROM sys_user WHERE user_id BETWEEN 9101 AND 9110;

DELETE FROM qkyd_device_info_extend WHERE device_id BETWEEN 9201 AND 9210;
DELETE FROM qkyd_device_info WHERE id BETWEEN 9201 AND 9210;

DELETE FROM qkyd_location WHERE id BETWEEN 9501 AND 9599;
DELETE FROM qkyd_steps WHERE id BETWEEN 9601 AND 9699;
DELETE FROM qkyd_temp WHERE id BETWEEN 9701 AND 9799;
DELETE FROM qkyd_heart_rate WHERE id BETWEEN 9801 AND 9899;
DELETE FROM qkyd_spo2 WHERE id BETWEEN 9901 AND 9999;
DELETE FROM qkyd_blood WHERE id BETWEEN 10001 AND 10099;

DELETE FROM qkyd_exception WHERE id BETWEEN 9301 AND 9399;
DELETE FROM ai_abnormal_record WHERE id BETWEEN 94001 AND 94999 OR user_id BETWEEN 9101 AND 9110;
DELETE FROM health_subject WHERE subject_id BETWEEN 9401 AND 9499;

-- 2) 用户主数据（老年关怀场景）
INSERT INTO sys_user (
  user_id, dept_id, user_name, nick_name, user_type, email, phonenumber,
  age, sex, avatar, password, status, del_flag, login_ip, login_date,
  create_by, create_time, update_by, update_time, remark
) VALUES
  (9101, 103, 'demo_zhangjian', '张建国', '00', 'demo01@qkyd.local', '13900000001', 72, '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', NOW(), 'demo_seed', NOW(), 'demo_seed', NOW(), '高血压重点关注对象'),
  (9102, 103, 'demo_limin', '李敏', '00', 'demo02@qkyd.local', '13900000002', 68, '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', NOW(), 'demo_seed', NOW(), 'demo_seed', NOW(), '血氧波动对象'),
  (9103, 103, 'demo_wanghe', '王和平', '00', 'demo03@qkyd.local', '13900000003', 81, '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', NOW(), 'demo_seed', NOW(), 'demo_seed', NOW(), '围栏风险对象'),
  (9104, 103, 'demo_chenlan', '陈兰', '00', 'demo04@qkyd.local', '13900000004', 76, '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', NOW(), 'demo_seed', NOW(), 'demo_seed', NOW(), 'SOS重点对象'),
  (9105, 103, 'demo_zhaoyun', '赵云海', '00', 'demo05@qkyd.local', '13900000005', 64, '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', NOW(), 'demo_seed', NOW(), 'demo_seed', NOW(), '日常监测对象'),
  (9106, 103, 'demo_sunyu', '孙玉珍', '00', 'demo06@qkyd.local', '13900000006', 74, '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', NOW(), 'demo_seed', NOW(), 'demo_seed', NOW(), '慢病管理对象')
ON DUPLICATE KEY UPDATE
  nick_name = VALUES(nick_name),
  age = VALUES(age),
  sex = VALUES(sex),
  phonenumber = VALUES(phonenumber),
  update_by = 'demo_seed',
  update_time = NOW(),
  del_flag = '0';

INSERT INTO sys_user_role (user_id, role_id) VALUES
  (9101, 2), (9102, 2), (9103, 2), (9104, 2), (9105, 2), (9106, 2)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 3) 设备与实时扩展数据
INSERT INTO qkyd_device_info (
  id, user_id, name, imei, type, create_by, create_time, update_by, update_time
) VALUES
  (9201, 9101, '腕表-A01', 'IMEI9101001', 'WATCH_PRO', 'demo_seed', NOW(), 'demo_seed', NOW()),
  (9202, 9102, '腕表-A02', 'IMEI9102002', 'WATCH_PRO', 'demo_seed', NOW(), 'demo_seed', NOW()),
  (9203, 9103, '腕表-A03', 'IMEI9103003', 'WATCH_LITE', 'demo_seed', NOW(), 'demo_seed', NOW()),
  (9204, 9104, '腕表-A04', 'IMEI9104004', 'WATCH_PRO', 'demo_seed', NOW(), 'demo_seed', NOW()),
  (9205, 9105, '腕表-A05', 'IMEI9105005', 'WATCH_LITE', 'demo_seed', NOW(), 'demo_seed', NOW()),
  (9206, 9106, '腕表-A06', 'IMEI9106006', 'WATCH_PRO', 'demo_seed', NOW(), 'demo_seed', NOW())
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  name = VALUES(name),
  imei = VALUES(imei),
  type = VALUES(type),
  update_by = 'demo_seed',
  update_time = NOW();

INSERT INTO qkyd_device_info_extend (
  device_id, last_communication_time, battery_level, step,
  alarm_content, alarm_time, temp, temp_time,
  heart_rate, heart_rate_time, blood_diastolic, blood_systolic, blood_time,
  spo2, spo2_time, longitude, latitude, location, type, positioning_time,
  create_by, create_time, update_by, update_time
) VALUES
  (9201, NOW(), 78, 5342, '心率偏高', DATE_SUB(NOW(), INTERVAL 8 MINUTE), 36.8, NOW(), 108, NOW(), 89, 146, NOW(), 96, NOW(), '121.473700', '31.230400', '上海市黄浦区人民广场', 'GPS', NOW(), 'demo_seed', NOW(), 'demo_seed', NOW()),
  (9202, NOW(), 63, 3890, '血氧偏低', DATE_SUB(NOW(), INTERVAL 15 MINUTE), 36.5, NOW(), 92, NOW(), 84, 132, NOW(), 91, NOW(), '121.481200', '31.227900', '上海市黄浦区南京东路', 'GPS', NOW(), 'demo_seed', NOW(), 'demo_seed', NOW()),
  (9203, NOW(), 55, 7601, '围栏越界', DATE_SUB(NOW(), INTERVAL 20 MINUTE), 36.7, NOW(), 87, NOW(), 82, 128, NOW(), 95, NOW(), '121.464800', '31.224500', '上海市静安区南京西路', 'LBS', NOW(), 'demo_seed', NOW(), 'demo_seed', NOW()),
  (9204, NOW(), 47, 2400, 'SOS手动触发', DATE_SUB(NOW(), INTERVAL 5 MINUTE), 37.2, NOW(), 118, NOW(), 92, 150, NOW(), 93, NOW(), '121.490100', '31.237200', '上海市虹口区四川北路', 'GPS', NOW(), 'demo_seed', NOW(), 'demo_seed', NOW()),
  (9205, NOW(), 86, 9821, '步数异常', DATE_SUB(NOW(), INTERVAL 35 MINUTE), 36.4, NOW(), 84, NOW(), 80, 126, NOW(), 97, NOW(), '121.455500', '31.240100', '上海市普陀区长寿路', 'GPS', NOW(), 'demo_seed', NOW(), 'demo_seed', NOW()),
  (9206, NOW(), 71, 4203, '血压异常', DATE_SUB(NOW(), INTERVAL 12 MINUTE), 37.0, NOW(), 99, NOW(), 98, 158, NOW(), 95, NOW(), '121.501000', '31.245600', '上海市杨浦区五角场', 'LBS', NOW(), 'demo_seed', NOW(), 'demo_seed', NOW())
ON DUPLICATE KEY UPDATE
  last_communication_time = VALUES(last_communication_time),
  battery_level = VALUES(battery_level),
  step = VALUES(step),
  alarm_content = VALUES(alarm_content),
  alarm_time = VALUES(alarm_time),
  temp = VALUES(temp),
  temp_time = VALUES(temp_time),
  heart_rate = VALUES(heart_rate),
  heart_rate_time = VALUES(heart_rate_time),
  blood_diastolic = VALUES(blood_diastolic),
  blood_systolic = VALUES(blood_systolic),
  blood_time = VALUES(blood_time),
  spo2 = VALUES(spo2),
  spo2_time = VALUES(spo2_time),
  longitude = VALUES(longitude),
  latitude = VALUES(latitude),
  location = VALUES(location),
  type = VALUES(type),
  positioning_time = VALUES(positioning_time),
  update_by = 'demo_seed',
  update_time = NOW();

-- 4) 时序监测数据（用于历史/分析页面）
INSERT INTO qkyd_heart_rate (id, user_id, device_id, value, create_time) VALUES
  (9801, 9101, 9201, 108, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
  (9802, 9102, 9202, 92, DATE_SUB(NOW(), INTERVAL 26 MINUTE)),
  (9803, 9103, 9203, 87, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
  (9804, 9104, 9204, 118, DATE_SUB(NOW(), INTERVAL 12 MINUTE)),
  (9805, 9105, 9205, 84, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
  (9806, 9106, 9206, 99, DATE_SUB(NOW(), INTERVAL 5 MINUTE));

INSERT INTO qkyd_spo2 (id, user_id, device_id, value, create_time) VALUES
  (9901, 9101, 9201, 96, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
  (9902, 9102, 9202, 91, DATE_SUB(NOW(), INTERVAL 26 MINUTE)),
  (9903, 9103, 9203, 95, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
  (9904, 9104, 9204, 93, DATE_SUB(NOW(), INTERVAL 12 MINUTE)),
  (9905, 9105, 9205, 97, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
  (9906, 9106, 9206, 95, DATE_SUB(NOW(), INTERVAL 5 MINUTE));

INSERT INTO qkyd_temp (id, user_id, device_id, value, create_time) VALUES
  (9701, 9101, 9201, 36.8, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
  (9702, 9102, 9202, 36.5, DATE_SUB(NOW(), INTERVAL 26 MINUTE)),
  (9703, 9103, 9203, 36.7, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
  (9704, 9104, 9204, 37.2, DATE_SUB(NOW(), INTERVAL 12 MINUTE)),
  (9705, 9105, 9205, 36.4, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
  (9706, 9106, 9206, 37.0, DATE_SUB(NOW(), INTERVAL 5 MINUTE));

INSERT INTO qkyd_blood (id, user_id, device_id, diastolic, systolic, create_time) VALUES
  (10001, 9101, 9201, 89, 146, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
  (10002, 9102, 9202, 84, 132, DATE_SUB(NOW(), INTERVAL 26 MINUTE)),
  (10003, 9103, 9203, 82, 128, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
  (10004, 9104, 9204, 92, 150, DATE_SUB(NOW(), INTERVAL 12 MINUTE)),
  (10005, 9105, 9205, 80, 126, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
  (10006, 9106, 9206, 98, 158, DATE_SUB(NOW(), INTERVAL 5 MINUTE));

INSERT INTO qkyd_steps (id, user_id, device_id, date, value, calories, date_time) VALUES
  (9601, 9101, 9201, CURDATE(), 5342, 180, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
  (9602, 9102, 9202, CURDATE(), 3890, 132, DATE_SUB(NOW(), INTERVAL 26 MINUTE)),
  (9603, 9103, 9203, CURDATE(), 7601, 260, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
  (9604, 9104, 9204, CURDATE(), 2400, 84, DATE_SUB(NOW(), INTERVAL 12 MINUTE)),
  (9605, 9105, 9205, CURDATE(), 9821, 312, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
  (9606, 9106, 9206, CURDATE(), 4203, 150, DATE_SUB(NOW(), INTERVAL 5 MINUTE));

INSERT INTO qkyd_location (id, user_id, device_id, accuracy, altitude, latitude, longitude, location, create_time, read_time, type) VALUES
  (9501, 9101, 9201, 12, 8, '31.230400', '121.473700', '上海市黄浦区人民广场', DATE_SUB(NOW(), INTERVAL 8 MINUTE), DATE_SUB(NOW(), INTERVAL 8 MINUTE), 'GPS'),
  (9502, 9102, 9202, 15, 6, '31.227900', '121.481200', '上海市黄浦区南京东路', DATE_SUB(NOW(), INTERVAL 15 MINUTE), DATE_SUB(NOW(), INTERVAL 15 MINUTE), 'GPS'),
  (9503, 9103, 9203, 20, 7, '31.224500', '121.464800', '上海市静安区南京西路', DATE_SUB(NOW(), INTERVAL 20 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE), 'LBS'),
  (9504, 9104, 9204, 10, 5, '31.237200', '121.490100', '上海市虹口区四川北路', DATE_SUB(NOW(), INTERVAL 5 MINUTE), DATE_SUB(NOW(), INTERVAL 5 MINUTE), 'GPS'),
  (9505, 9105, 9205, 14, 9, '31.240100', '121.455500', '上海市普陀区长寿路', DATE_SUB(NOW(), INTERVAL 10 MINUTE), DATE_SUB(NOW(), INTERVAL 10 MINUTE), 'GPS'),
  (9506, 9106, 9206, 18, 11, '31.245600', '121.501000', '上海市杨浦区五角场', DATE_SUB(NOW(), INTERVAL 12 MINUTE), DATE_SUB(NOW(), INTERVAL 12 MINUTE), 'LBS');

-- 5) 异常流（仪表盘地图和异常列表）
INSERT INTO qkyd_exception (
  id, user_id, user_id_who, device_id, type, value,
  longitude, latitude, state, update_content, location,
  read_time, nick_name, sex, phone, age,
  create_by, create_time, update_by, update_time, update_by_who, update_time_who,
  start_create_time, end_create_time
) VALUES
  (9301, 9101, 9101, 9201, '心率异常', '108 bpm', 121.473700, 31.230400, '0', NULL, '上海市黄浦区人民广场', DATE_SUB(NOW(), INTERVAL 8 MINUTE), '张建国', '0', '13900000001', 72, 'demo_seed', DATE_SUB(NOW(), INTERVAL 8 MINUTE), '', NULL, '', NULL, NULL, NULL),
  (9302, 9102, 9102, 9202, '血氧异常', '91%', 121.481200, 31.227900, '0', NULL, '上海市黄浦区南京东路', DATE_SUB(NOW(), INTERVAL 15 MINUTE), '李敏', '1', '13900000002', 68, 'demo_seed', DATE_SUB(NOW(), INTERVAL 15 MINUTE), '', NULL, '', NULL, NULL, NULL),
  (9303, 9103, 9103, 9203, '围栏越界', '超出2.1km', 121.464800, 31.224500, '0', NULL, '上海市静安区南京西路', DATE_SUB(NOW(), INTERVAL 20 MINUTE), '王和平', '0', '13900000003', 81, 'demo_seed', DATE_SUB(NOW(), INTERVAL 20 MINUTE), '', NULL, '', NULL, NULL, NULL),
  (9304, 9104, 9104, 9204, 'SOS求救', '手动触发', 121.490100, 31.237200, '0', NULL, '上海市虹口区四川北路', DATE_SUB(NOW(), INTERVAL 5 MINUTE), '陈兰', '1', '13900000004', 76, 'demo_seed', DATE_SUB(NOW(), INTERVAL 5 MINUTE), '', NULL, '', NULL, NULL, NULL),
  (9305, 9105, 9105, 9205, '步数异常', '9821 steps/day', 121.455500, 31.240100, '1', '已电话确认无风险', '上海市普陀区长寿路', DATE_SUB(NOW(), INTERVAL 35 MINUTE), '赵云海', '0', '13900000005', 64, 'demo_seed', DATE_SUB(NOW(), INTERVAL 35 MINUTE), 'operator', DATE_SUB(NOW(), INTERVAL 25 MINUTE), 'operator', DATE_SUB(NOW(), INTERVAL 25 MINUTE), NULL, NULL),
  (9306, 9106, 9106, 9206, '血压异常', '158/98 mmHg', 121.501000, 31.245600, '0', NULL, '上海市杨浦区五角场', DATE_SUB(NOW(), INTERVAL 12 MINUTE), '孙玉珍', '1', '13900000006', 74, 'demo_seed', DATE_SUB(NOW(), INTERVAL 12 MINUTE), '', NULL, '', NULL, NULL, NULL);

-- 6) AI 检测记录（AI LOGS）
INSERT INTO ai_abnormal_record (
  id, user_id, device_id, metric_type, abnormal_value, normal_range,
  abnormal_type, risk_level, detection_method, detected_time, create_time
) VALUES
  (94001, 9101, 'IMEI9101001', 'heart_rate', '108', '60-100', 'too_high', 'warning', 'statistical', DATE_SUB(NOW(), INTERVAL 8 MINUTE), DATE_SUB(NOW(), INTERVAL 8 MINUTE)),
  (94002, 9102, 'IMEI9102002', 'spo2', '91', '95-100', 'too_low', 'high', 'threshold', DATE_SUB(NOW(), INTERVAL 15 MINUTE), DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
  (94003, 9104, 'IMEI9104004', 'sos', 'manual_trigger', 'n/a', 'too_high', 'critical', 'rule', DATE_SUB(NOW(), INTERVAL 5 MINUTE), DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
  (94004, 9106, 'IMEI9106006', 'blood_pressure', '158/98', '90-140/60-90', 'too_high', 'high', 'threshold', DATE_SUB(NOW(), INTERVAL 12 MINUTE), DATE_SUB(NOW(), INTERVAL 12 MINUTE));

-- 7) 服务对象主数据（health_subject 页面）
INSERT INTO health_subject (
  subject_id, dept_id, subject_name, nick_name, user_type, email, phonenumber,
  age, sex, avatar, password, status, del_flag, login_ip, login_date,
  create_by, create_time, update_by, update_time, remark
) VALUES
  (9401, 103, 'subject_zhangjian', '张建国', '00', 'subject01@qkyd.local', '13900000001', 72, '0', '', '', '0', '0', '', NULL, 'demo_seed', NOW(), 'demo_seed', NOW(), '对接腕表-A01'),
  (9402, 103, 'subject_limin', '李敏', '00', 'subject02@qkyd.local', '13900000002', 68, '1', '', '', '0', '0', '', NULL, 'demo_seed', NOW(), 'demo_seed', NOW(), '对接腕表-A02'),
  (9403, 103, 'subject_wanghe', '王和平', '00', 'subject03@qkyd.local', '13900000003', 81, '0', '', '', '0', '0', '', NULL, 'demo_seed', NOW(), 'demo_seed', NOW(), '对接腕表-A03'),
  (9404, 103, 'subject_chenlan', '陈兰', '00', 'subject04@qkyd.local', '13900000004', 76, '1', '', '', '0', '0', '', NULL, 'demo_seed', NOW(), 'demo_seed', NOW(), '对接腕表-A04')
ON DUPLICATE KEY UPDATE
  nick_name = VALUES(nick_name),
  age = VALUES(age),
  sex = VALUES(sex),
  status = '0',
  del_flag = '0',
  update_by = 'demo_seed',
  update_time = NOW();

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

-- 可选检查：
-- SELECT COUNT(*) AS demo_users FROM sys_user WHERE user_id BETWEEN 9101 AND 9110;
-- SELECT COUNT(*) AS demo_devices FROM qkyd_device_info WHERE id BETWEEN 9201 AND 9210;
-- SELECT COUNT(*) AS demo_exceptions FROM qkyd_exception WHERE id BETWEEN 9301 AND 9399;
-- SELECT COUNT(*) AS demo_ai_logs FROM ai_abnormal_record WHERE user_id BETWEEN 9101 AND 9110;
