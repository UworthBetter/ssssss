-- MySQL dump 10.13  Distrib 8.0.26, for Win64 (x86_64)
--
-- Host: localhost    Database: qkyd_jkpt
-- ------------------------------------------------------
-- Server version	8.0.26

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ai_abnormal_record`
--

DROP TABLE IF EXISTS `ai_abnormal_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_abnormal_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `device_id` varchar(64) DEFAULT NULL COMMENT '璁惧ID',
  `metric_type` varchar(32) NOT NULL COMMENT '鎸囨爣绫诲瀷(heart_rate/blood_pressure/temp/spo2)',
  `abnormal_value` varchar(64) NOT NULL COMMENT '寮傚父鍊?,
  `normal_range` varchar(64) DEFAULT NULL COMMENT '姝ｅ父鑼冨洿',
  `abnormal_type` varchar(32) DEFAULT NULL COMMENT '寮傚父绫诲瀷(too_high/too_low)',
  `risk_level` varchar(16) DEFAULT NULL COMMENT '椋庨櫓绛夌骇(danger/warning/normal)',
  `detection_method` varchar(32) DEFAULT NULL COMMENT '妫€娴嬫柟娉?threshold/statistical)',
  `detected_time` datetime NOT NULL COMMENT '妫€娴嬫椂闂?,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='寮傚父妫€娴嬭褰曡〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_abnormal_record`
--

LOCK TABLES `ai_abnormal_record` WRITE;
/*!40000 ALTER TABLE `ai_abnormal_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_abnormal_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_data_quality_record`
--

DROP TABLE IF EXISTS `ai_data_quality_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_data_quality_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `metric_type` varchar(32) NOT NULL COMMENT '鎸囨爣绫诲瀷',
  `missing_count` int DEFAULT NULL COMMENT '缂哄け鏁伴噺',
  `missing_rate` decimal(5,2) DEFAULT NULL COMMENT '缂哄け鐜?,
  `outlier_count` int DEFAULT NULL COMMENT '寮傚父鍊兼暟閲?,
  `outlier_rate` decimal(5,2) DEFAULT NULL COMMENT '寮傚父鍊肩巼',
  `fill_method` varchar(32) DEFAULT NULL COMMENT '濉厖鏂规硶',
  `quality_score` int DEFAULT NULL COMMENT '璐ㄩ噺璇勫垎(0-100)',
  `check_time` datetime NOT NULL COMMENT '妫€鏌ユ椂闂?,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鏁版嵁璐ㄩ噺璁板綍琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_data_quality_record`
--

LOCK TABLES `ai_data_quality_record` WRITE;
/*!40000 ALTER TABLE `ai_data_quality_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_data_quality_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_fall_alarm_record`
--

DROP TABLE IF EXISTS `ai_fall_alarm_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_fall_alarm_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `original_alarm_id` bigint DEFAULT NULL COMMENT '鍘熷鍛婅ID',
  `is_valid` tinyint(1) DEFAULT NULL COMMENT '鏄惁鏈夋晥鍛婅',
  `validation_reason` text COMMENT '鏍￠獙鍘熷洜',
  `acceleration_peak` decimal(10,2) DEFAULT NULL COMMENT '鍔犻€熷害宄板€?,
  `has_removal_alert` tinyint(1) DEFAULT NULL COMMENT '1灏忔椂鍐呮槸鍚︽湁鎽樿劚鍛婅',
  `recent_steps` int DEFAULT NULL COMMENT '鏈€杩?灏忔椂姝ユ暟',
  `validation_time` datetime NOT NULL COMMENT '鏍￠獙鏃堕棿',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='璺屽€掑憡璀﹁褰曡〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_fall_alarm_record`
--

LOCK TABLES `ai_fall_alarm_record` WRITE;
/*!40000 ALTER TABLE `ai_fall_alarm_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_fall_alarm_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_health_record`
--

DROP TABLE IF EXISTS `ai_health_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_health_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '璁惧ID',
  `user_id` bigint DEFAULT NULL COMMENT '鐢ㄦ埛ID',
  `risk_level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '椋庨櫓绛夌骇(low/medium/high/critical)',
  `risk_score` decimal(5,4) NOT NULL COMMENT '椋庨櫓璇勫垎(0.0000-1.0000)',
  `anomaly_count` int DEFAULT '0' COMMENT '寮傚父鏁伴噺',
  `risk_factors` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '椋庨櫓鍥犵礌JSON',
  `raw_data` json DEFAULT NULL COMMENT '鍘熷浣撳緛鏁版嵁',
  `data_points` int DEFAULT '0' COMMENT '鏁版嵁鐐规暟閲?,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI鍋ュ悍鍒嗘瀽璁板綍琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_health_record`
--

LOCK TABLES `ai_health_record` WRITE;
/*!40000 ALTER TABLE `ai_health_record` DISABLE KEYS */;
INSERT INTO `ai_health_record` VALUES (1,'TEST_DEVICE_001',NULL,'low',0.0000,1,'[\"鍚勯」鎸囨爣姝ｅ父\"]','[{\"steps\": 500, \"timestamp\": 1769665269851, \"heart_rate\": 72, \"blood_pressure\": \"118/76\"}, {\"steps\": 800, \"timestamp\": 1769665329851, \"heart_rate\": 75, \"blood_pressure\": \"120/80\"}, {\"steps\": 1200, \"timestamp\": 1769665389851, \"heart_rate\": 73, \"blood_pressure\": \"119/78\"}, {\"steps\": 1500, \"timestamp\": 1769665449851, \"heart_rate\": 76, \"blood_pressure\": \"121/79\"}, {\"steps\": 2000, \"timestamp\": 1769665509851, \"heart_rate\": 74, \"blood_pressure\": \"118/77\"}, {\"steps\": 2300, \"timestamp\": 1769665569851, \"heart_rate\": 77, \"blood_pressure\": \"122/81\"}, {\"steps\": 2800, \"timestamp\": 1769665629851, \"heart_rate\": 75, \"blood_pressure\": \"120/80\"}, {\"steps\": 3200, \"timestamp\": 1769665689851, \"heart_rate\": 78, \"blood_pressure\": \"123/82\"}, {\"steps\": 3500, \"timestamp\": 1769665749851, \"heart_rate\": 76, \"blood_pressure\": \"121/80\"}, {\"steps\": 3800, \"timestamp\": 1769665809851, \"heart_rate\": 74, \"blood_pressure\": \"119/78\"}, {\"steps\": 100, \"timestamp\": 1769665869851, \"heart_rate\": 130, \"blood_pressure\": \"140/95\"}]',11,'2026-01-29 13:51:10','2026-01-29 13:51:10');
/*!40000 ALTER TABLE `ai_health_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_risk_score_record`
--

DROP TABLE IF EXISTS `ai_risk_score_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_risk_score_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `total_score` int DEFAULT NULL COMMENT '鎬昏瘎鍒?0-100)',
  `risk_level` varchar(16) DEFAULT NULL COMMENT '椋庨櫓绛夌骇(high/medium/low/normal)',
  `heart_rate_score` int DEFAULT NULL COMMENT '蹇冪巼璇勫垎',
  `blood_pressure_score` int DEFAULT NULL COMMENT '琛€鍘嬭瘎鍒?,
  `blood_oxygen_score` int DEFAULT NULL COMMENT '琛€姘ц瘎鍒?,
  `temperature_score` int DEFAULT NULL COMMENT '浣撴俯璇勫垎',
  `warnings` json DEFAULT NULL COMMENT '寮傚父鍘熷洜鍒楄〃',
  `score_time` datetime NOT NULL COMMENT '璇勫垎鏃堕棿',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='椋庨櫓璇勫垎璁板綍琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_risk_score_record`
--

LOCK TABLES `ai_risk_score_record` WRITE;
/*!40000 ALTER TABLE `ai_risk_score_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_risk_score_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_trend_record`
--

DROP TABLE IF EXISTS `ai_trend_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_trend_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `metric_type` varchar(32) NOT NULL COMMENT '鎸囨爣绫诲瀷',
  `trend_direction` varchar(16) DEFAULT NULL COMMENT '瓒嬪娍鏂瑰悜(up/down/stable)',
  `trend_strength` decimal(5,2) DEFAULT NULL COMMENT '瓒嬪娍寮哄害',
  `predicted_value` decimal(10,2) DEFAULT NULL COMMENT '棰勬祴鍊?,
  `prediction_confidence` decimal(5,2) DEFAULT NULL COMMENT '棰勬祴缃俊搴?,
  `analysis_time` datetime NOT NULL COMMENT '鍒嗘瀽鏃堕棿',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='瓒嬪娍鍒嗘瀽璁板綍琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_trend_record`
--

LOCK TABLES `ai_trend_record` WRITE;
/*!40000 ALTER TABLE `ai_trend_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_trend_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gen_table`
--

DROP TABLE IF EXISTS `gen_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gen_table` (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '缂栧彿',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '琛ㄥ悕绉?,
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '琛ㄦ弿杩?,
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鍏宠仈瀛愯〃鐨勮〃鍚?,
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '瀛愯〃鍏宠仈鐨勫閿悕',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '瀹炰綋绫诲悕绉?,
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'crud' COMMENT '浣跨敤鐨勬ā鏉匡紙crud鍗曡〃鎿嶄綔 tree鏍戣〃鎿嶄綔锛?,
  `tpl_web_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍓嶇妯℃澘绫诲瀷锛坋lement-ui妯＄増 element-plus妯＄増锛?,
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鐢熸垚鍖呰矾寰?,
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鐢熸垚妯″潡鍚?,
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鐢熸垚涓氬姟鍚?,
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鐢熸垚鍔熻兘鍚?,
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鐢熸垚鍔熻兘浣滆€?,
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鐢熸垚浠ｇ爜鏂瑰紡锛?zip鍘嬬缉鍖?1鑷畾涔夎矾寰勶級',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '/' COMMENT '鐢熸垚璺緞锛堜笉濉粯璁ら」鐩矾寰勶級',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鍏跺畠鐢熸垚閫夐」',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '澶囨敞',
  PRIMARY KEY (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='浠ｇ爜鐢熸垚涓氬姟琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gen_table`
--

LOCK TABLES `gen_table` WRITE;
/*!40000 ALTER TABLE `gen_table` DISABLE KEYS */;
/*!40000 ALTER TABLE `gen_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gen_table_column`
--

DROP TABLE IF EXISTS `gen_table_column`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gen_table_column` (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '缂栧彿',
  `table_id` bigint DEFAULT NULL COMMENT '褰掑睘琛ㄧ紪鍙?,
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鍒楀悕绉?,
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鍒楁弿杩?,
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鍒楃被鍨?,
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'JAVA绫诲瀷',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'JAVA瀛楁鍚?,
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏄惁涓婚敭锛?鏄級',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏄惁鑷锛?鏄級',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏄惁蹇呭～锛?鏄級',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏄惁涓烘彃鍏ュ瓧娈碉紙1鏄級',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏄惁缂栬緫瀛楁锛?鏄級',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏄惁鍒楄〃瀛楁锛?鏄級',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏄惁鏌ヨ瀛楁锛?鏄級',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'EQ' COMMENT '鏌ヨ鏂瑰紡锛堢瓑浜庛€佷笉绛変簬銆佸ぇ浜庛€佸皬浜庛€佽寖鍥达級',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏄剧ず绫诲瀷锛堟枃鏈銆佹枃鏈煙銆佷笅鎷夋銆佸閫夋銆佸崟閫夋銆佹棩鏈熸帶浠讹級',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '瀛楀吀绫诲瀷',
  `sort` int DEFAULT NULL COMMENT '鎺掑簭',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='浠ｇ爜鐢熸垚涓氬姟琛ㄥ瓧娈?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gen_table_column`
--

LOCK TABLES `gen_table_column` WRITE;
/*!40000 ALTER TABLE `gen_table_column` DISABLE KEYS */;
/*!40000 ALTER TABLE `gen_table_column` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qkyd_blood`
--

DROP TABLE IF EXISTS `qkyd_blood`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qkyd_blood` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint DEFAULT NULL COMMENT '鐢ㄦ埛ID',
  `device_id` bigint DEFAULT NULL COMMENT '璁惧ID',
  `diastolic` int DEFAULT NULL COMMENT '鑸掑紶鍘?,
  `systolic` int DEFAULT NULL COMMENT '鏀剁缉鍘?,
  `read_time` datetime DEFAULT NULL COMMENT '璇诲彇鏃堕棿',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='琛€鍘嬫暟鎹〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qkyd_blood`
--

LOCK TABLES `qkyd_blood` WRITE;
/*!40000 ALTER TABLE `qkyd_blood` DISABLE KEYS */;
/*!40000 ALTER TABLE `qkyd_blood` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qkyd_detection_enhanced`
--

DROP TABLE IF EXISTS `qkyd_detection_enhanced`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qkyd_detection_enhanced` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint DEFAULT NULL COMMENT '鐢ㄦ埛ID',
  `device_id` bigint DEFAULT NULL COMMENT '璁惧ID',
  `is_fall` tinyint(1) DEFAULT NULL COMMENT '鏄惁璺屽€?1鏄?0鍚?',
  `confidence` decimal(5,2) DEFAULT NULL COMMENT '缃俊搴?,
  `severity` varchar(20) DEFAULT NULL COMMENT '涓ラ噸绋嬪害',
  `reasoning` text COMMENT 'AI鎺ㄧ悊鍒嗘瀽',
  `recommendation` text COMMENT 'AI寤鸿',
  `original_data` json DEFAULT NULL COMMENT '鍘熷浼犳劅鍣ㄦ暟鎹?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI澧炲己妫€娴嬭褰曡〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qkyd_detection_enhanced`
--

LOCK TABLES `qkyd_detection_enhanced` WRITE;
/*!40000 ALTER TABLE `qkyd_detection_enhanced` DISABLE KEYS */;
/*!40000 ALTER TABLE `qkyd_detection_enhanced` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qkyd_device_info`
--

DROP TABLE IF EXISTS `qkyd_device_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qkyd_device_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint DEFAULT NULL COMMENT '鐢ㄦ埛ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '璁惧鍚嶇О',
  `imei` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'IMEI淇℃伅',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '璁惧鍨嬪彿',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='璁惧淇℃伅琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qkyd_device_info`
--

LOCK TABLES `qkyd_device_info` WRITE;
/*!40000 ALTER TABLE `qkyd_device_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `qkyd_device_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qkyd_device_info_extend`
--

DROP TABLE IF EXISTS `qkyd_device_info_extend`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qkyd_device_info_extend` (
  `device_id` bigint NOT NULL COMMENT '璁惧ID',
  `last_communication_time` datetime DEFAULT NULL COMMENT '鏈€鍚庨€氳鏃堕棿',
  `battery_level` int DEFAULT NULL COMMENT '鐢甸噺',
  `step` int DEFAULT NULL COMMENT '姝ユ暟',
  `alarm_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏈€杩戝憡璀﹀唴瀹?,
  `alarm_time` datetime DEFAULT NULL COMMENT '鏈€杩戝憡璀︽椂闂?,
  `temp` float DEFAULT NULL COMMENT '浣撴俯',
  `temp_time` datetime DEFAULT NULL COMMENT '浣撴俯娴嬮噺鏃堕棿',
  `heart_rate` float DEFAULT NULL COMMENT '蹇冪巼',
  `heart_rate_time` datetime DEFAULT NULL COMMENT '蹇冪巼娴嬮噺鏃堕棿',
  `blood_diastolic` int DEFAULT NULL COMMENT '鑸掑紶鍘?,
  `blood_systolic` int DEFAULT NULL COMMENT '鏀剁缉鍘?,
  `blood_time` datetime DEFAULT NULL COMMENT '琛€鍘嬫祴閲忔椂闂?,
  `spo2` float DEFAULT NULL COMMENT '琛€姘?,
  `spo2_time` datetime DEFAULT NULL COMMENT '琛€姘ф祴閲忔椂闂?,
  `longitude` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '缁忓害',
  `latitude` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '绾害',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '璇︾粏鍦板潃',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '瀹氫綅鏂瑰紡',
  `positioning_time` datetime DEFAULT NULL COMMENT '瀹氫綅鏃堕棿',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='璁惧淇℃伅鎵╁睍琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qkyd_device_info_extend`
--

LOCK TABLES `qkyd_device_info_extend` WRITE;
/*!40000 ALTER TABLE `qkyd_device_info_extend` DISABLE KEYS */;
/*!40000 ALTER TABLE `qkyd_device_info_extend` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qkyd_exception`
--

DROP TABLE IF EXISTS `qkyd_exception`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qkyd_exception` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint DEFAULT NULL COMMENT '寮傚父浜哄憳ID',
  `user_id_who` bigint DEFAULT NULL COMMENT '寮傚父浜哄憳ID(璋?',
  `device_id` bigint DEFAULT NULL COMMENT '寮傚父鏉ユ簮璁惧ID',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '寮傚父绫诲瀷',
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '寮傚父鏁板€?,
  `longitude` decimal(20,6) DEFAULT NULL COMMENT '缁忓害',
  `latitude` decimal(20,6) DEFAULT NULL COMMENT '绾害',
  `state` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鐘舵€侊紙0鏈鐞嗐€?宸插鐞嗭級',
  `update_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '澶勭悊璇存槑',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鎵€鍦ㄥ湴鍧€',
  `read_time` datetime DEFAULT NULL COMMENT '璇诲彇鏃堕棿',
  `nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏄电О',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鐢ㄦ埛鎬у埆锛?鐢?1濂?2鏈煡锛?,
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鎵嬫満鍙风爜',
  `age` int DEFAULT NULL COMMENT '骞撮緞',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `update_by_who` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?璋?',
  `update_time_who` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿(璋?',
  `start_create_time` datetime DEFAULT NULL COMMENT '寮€濮嬪垱寤烘椂闂?,
  `end_create_time` datetime DEFAULT NULL COMMENT '缁撴潫鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='寮傚父鏁版嵁琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qkyd_exception`
--

LOCK TABLES `qkyd_exception` WRITE;
/*!40000 ALTER TABLE `qkyd_exception` DISABLE KEYS */;
/*!40000 ALTER TABLE `qkyd_exception` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qkyd_fence`
--

DROP TABLE IF EXISTS `qkyd_fence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qkyd_fence` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint DEFAULT NULL COMMENT '鐢ㄦ埛ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍥存爮鍚嶇О',
  `fence_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍥存爮绫诲瀷',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '鍥存爮璇︽儏',
  `radius` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鍗婂緞',
  `warn_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鎶ヨ绫诲瀷锛?杩涘叆锛?绂诲紑锛?杩涘叆&绂诲紑',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '鍥存爮鐘舵€侊細1鐢熸晥锛?澶辨晥',
  `longitude` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '缁忓害',
  `latitude` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '绾害',
  `shape` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鍥存爮褰㈢姸',
  `level` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鎶ヨ绛夌骇锛?绾紱2姗欙紱3榛?,
  `begin_read_time` datetime DEFAULT NULL COMMENT '寮€濮嬫姤璀︽椂闂?,
  `end_read_time` datetime DEFAULT NULL COMMENT '缁撴潫鎶ヨ鏃堕棿',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='鍥存爮琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qkyd_fence`
--

LOCK TABLES `qkyd_fence` WRITE;
/*!40000 ALTER TABLE `qkyd_fence` DISABLE KEYS */;
/*!40000 ALTER TABLE `qkyd_fence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qkyd_heart_rate`
--

DROP TABLE IF EXISTS `qkyd_heart_rate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qkyd_heart_rate` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint DEFAULT NULL COMMENT '鐢ㄦ埛ID',
  `device_id` bigint DEFAULT NULL COMMENT '璁惧ID',
  `value` float DEFAULT NULL COMMENT '蹇冪巼鍊?,
  `read_time` datetime DEFAULT NULL COMMENT '璇诲彇鏃堕棿',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='蹇冪巼鏁版嵁琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qkyd_heart_rate`
--

LOCK TABLES `qkyd_heart_rate` WRITE;
/*!40000 ALTER TABLE `qkyd_heart_rate` DISABLE KEYS */;
/*!40000 ALTER TABLE `qkyd_heart_rate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qkyd_location`
--

DROP TABLE IF EXISTS `qkyd_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qkyd_location` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint DEFAULT NULL COMMENT '鐢ㄦ埛ID',
  `device_id` bigint DEFAULT NULL COMMENT '璁惧ID',
  `accuracy` bigint DEFAULT NULL COMMENT '绮惧害',
  `altitude` bigint DEFAULT NULL COMMENT '楂樺害',
  `latitude` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '绾害',
  `longitude` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '缁忓害',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '璇︾粏鍦板潃',
  `read_time` datetime DEFAULT NULL COMMENT '璇诲彇鏃堕棿',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '瀹氫綅鏂瑰紡',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='瀹氫綅鏁版嵁琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qkyd_location`
--

LOCK TABLES `qkyd_location` WRITE;
/*!40000 ALTER TABLE `qkyd_location` DISABLE KEYS */;
/*!40000 ALTER TABLE `qkyd_location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qkyd_spo2`
--

DROP TABLE IF EXISTS `qkyd_spo2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qkyd_spo2` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint DEFAULT NULL COMMENT '鐢ㄦ埛ID',
  `device_id` bigint DEFAULT NULL COMMENT '璁惧ID',
  `value` float DEFAULT NULL COMMENT '琛€姘у€?,
  `read_time` datetime DEFAULT NULL COMMENT '璇诲彇鏃堕棿',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='琛€姘ф暟鎹〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qkyd_spo2`
--

LOCK TABLES `qkyd_spo2` WRITE;
/*!40000 ALTER TABLE `qkyd_spo2` DISABLE KEYS */;
/*!40000 ALTER TABLE `qkyd_spo2` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qkyd_steps`
--

DROP TABLE IF EXISTS `qkyd_steps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qkyd_steps` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint DEFAULT NULL COMMENT '鐢ㄦ埛ID',
  `device_id` bigint DEFAULT NULL COMMENT '璁惧ID',
  `date` date DEFAULT NULL COMMENT '鏃ユ湡',
  `value` int DEFAULT NULL COMMENT '姝ユ暟鍊?,
  `calories` bigint DEFAULT NULL COMMENT '鍗¤矾閲?,
  `date_time` datetime DEFAULT NULL COMMENT '鏃ユ湡鏃堕棿',
  `read_time` datetime DEFAULT NULL COMMENT '璇诲彇鏃堕棿',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='姝ユ暟鏁版嵁琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qkyd_steps`
--

LOCK TABLES `qkyd_steps` WRITE;
/*!40000 ALTER TABLE `qkyd_steps` DISABLE KEYS */;
/*!40000 ALTER TABLE `qkyd_steps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qkyd_temp`
--

DROP TABLE IF EXISTS `qkyd_temp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qkyd_temp` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint DEFAULT NULL COMMENT '鐢ㄦ埛ID',
  `device_id` bigint DEFAULT NULL COMMENT '璁惧ID',
  `value` float DEFAULT NULL COMMENT '浣撴俯鍊?,
  `read_time` datetime DEFAULT NULL COMMENT '璇诲彇鏃堕棿',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='浣撴俯鏁版嵁琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qkyd_temp`
--

LOCK TABLES `qkyd_temp` WRITE;
/*!40000 ALTER TABLE `qkyd_temp` DISABLE KEYS */;
/*!40000 ALTER TABLE `qkyd_temp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_blob_triggers`
--

DROP TABLE IF EXISTS `qrtz_blob_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_blob_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冨害鍚嶇О',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers琛╰rigger_name鐨勫閿?,
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers琛╰rigger_group鐨勫閿?,
  `blob_data` blob COMMENT '瀛樻斁鎸佷箙鍖朤rigger瀵硅薄',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Blob绫诲瀷鐨勮Е鍙戝櫒琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_blob_triggers`
--

LOCK TABLES `qrtz_blob_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_blob_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_blob_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_calendars`
--

DROP TABLE IF EXISTS `qrtz_calendars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_calendars` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冨害鍚嶇О',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鏃ュ巻鍚嶇О',
  `calendar` blob NOT NULL COMMENT '瀛樻斁鎸佷箙鍖朿alendar瀵硅薄',
  PRIMARY KEY (`sched_name`,`calendar_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='鏃ュ巻淇℃伅琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_calendars`
--

LOCK TABLES `qrtz_calendars` WRITE;
/*!40000 ALTER TABLE `qrtz_calendars` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_calendars` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_cron_triggers`
--

DROP TABLE IF EXISTS `qrtz_cron_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_cron_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冨害鍚嶇О',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers琛╰rigger_name鐨勫閿?,
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers琛╰rigger_group鐨勫閿?,
  `cron_expression` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cron琛ㄨ揪寮?,
  `time_zone_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏃跺尯',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Cron绫诲瀷鐨勮Е鍙戝櫒琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_cron_triggers`
--

LOCK TABLES `qrtz_cron_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_cron_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_cron_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_fired_triggers`
--

DROP TABLE IF EXISTS `qrtz_fired_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_fired_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冨害鍚嶇О',
  `entry_id` varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冨害鍣ㄥ疄渚媔d',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers琛╰rigger_name鐨勫閿?,
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers琛╰rigger_group鐨勫閿?,
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冨害鍣ㄥ疄渚嬪悕',
  `fired_time` bigint NOT NULL COMMENT '瑙﹀彂鐨勬椂闂?,
  `sched_time` bigint NOT NULL COMMENT '瀹氭椂鍣ㄥ埗瀹氱殑鏃堕棿',
  `priority` int NOT NULL COMMENT '浼樺厛绾?,
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鐘舵€?,
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '浠诲姟鍚嶇О',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '浠诲姟缁勫悕',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏄惁骞跺彂',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏄惁鎺ュ彈鎭㈠鎵ц',
  PRIMARY KEY (`sched_name`,`entry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='宸茶Е鍙戠殑瑙﹀彂鍣ㄨ〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_fired_triggers`
--

LOCK TABLES `qrtz_fired_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_fired_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_fired_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_job_details`
--

DROP TABLE IF EXISTS `qrtz_job_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_job_details` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冨害鍚嶇О',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '浠诲姟鍚嶇О',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '浠诲姟缁勫悕',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鐩稿叧浠嬬粛',
  `job_class_name` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鎵ц浠诲姟绫诲悕绉?,
  `is_durable` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鏄惁鎸佷箙鍖?,
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鏄惁骞跺彂',
  `is_update_data` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鏄惁鏇存柊鏁版嵁',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鏄惁鎺ュ彈鎭㈠鎵ц',
  `job_data` blob COMMENT '瀛樻斁鎸佷箙鍖杍ob瀵硅薄',
  PRIMARY KEY (`sched_name`,`job_name`,`job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='浠诲姟璇︾粏淇℃伅琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_job_details`
--

LOCK TABLES `qrtz_job_details` WRITE;
/*!40000 ALTER TABLE `qrtz_job_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_job_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_locks`
--

DROP TABLE IF EXISTS `qrtz_locks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_locks` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冨害鍚嶇О',
  `lock_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鎮茶閿佸悕绉?,
  PRIMARY KEY (`sched_name`,`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='瀛樺偍鐨勬偛瑙傞攣淇℃伅琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_locks`
--

LOCK TABLES `qrtz_locks` WRITE;
/*!40000 ALTER TABLE `qrtz_locks` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_locks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_paused_trigger_grps`
--

DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_paused_trigger_grps` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冨害鍚嶇О',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers琛╰rigger_group鐨勫閿?,
  PRIMARY KEY (`sched_name`,`trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='鏆傚仠鐨勮Е鍙戝櫒琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_paused_trigger_grps`
--

LOCK TABLES `qrtz_paused_trigger_grps` WRITE;
/*!40000 ALTER TABLE `qrtz_paused_trigger_grps` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_paused_trigger_grps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_scheduler_state`
--

DROP TABLE IF EXISTS `qrtz_scheduler_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_scheduler_state` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冨害鍚嶇О',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '瀹炰緥鍚嶇О',
  `last_checkin_time` bigint NOT NULL COMMENT '涓婃妫€鏌ユ椂闂?,
  `checkin_interval` bigint NOT NULL COMMENT '妫€鏌ラ棿闅旀椂闂?,
  PRIMARY KEY (`sched_name`,`instance_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='璋冨害鍣ㄧ姸鎬佽〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_scheduler_state`
--

LOCK TABLES `qrtz_scheduler_state` WRITE;
/*!40000 ALTER TABLE `qrtz_scheduler_state` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_scheduler_state` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_simple_triggers`
--

DROP TABLE IF EXISTS `qrtz_simple_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_simple_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冨害鍚嶇О',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers琛╰rigger_name鐨勫閿?,
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers琛╰rigger_group鐨勫閿?,
  `repeat_count` bigint NOT NULL COMMENT '閲嶅鐨勬鏁扮粺璁?,
  `repeat_interval` bigint NOT NULL COMMENT '閲嶅鐨勯棿闅旀椂闂?,
  `times_triggered` bigint NOT NULL COMMENT '宸茬粡瑙﹀彂鐨勬鏁?,
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='绠€鍗曡Е鍙戝櫒鐨勪俊鎭〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_simple_triggers`
--

LOCK TABLES `qrtz_simple_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_simple_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_simple_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_simprop_triggers`
--

DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_simprop_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冨害鍚嶇О',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers琛╰rigger_name鐨勫閿?,
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers琛╰rigger_group鐨勫閿?,
  `str_prop_1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String绫诲瀷鐨則rigger鐨勭涓€涓弬鏁?,
  `str_prop_2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String绫诲瀷鐨則rigger鐨勭浜屼釜鍙傛暟',
  `str_prop_3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String绫诲瀷鐨則rigger鐨勭涓変釜鍙傛暟',
  `int_prop_1` int DEFAULT NULL COMMENT 'int绫诲瀷鐨則rigger鐨勭涓€涓弬鏁?,
  `int_prop_2` int DEFAULT NULL COMMENT 'int绫诲瀷鐨則rigger鐨勭浜屼釜鍙傛暟',
  `long_prop_1` bigint DEFAULT NULL COMMENT 'long绫诲瀷鐨則rigger鐨勭涓€涓弬鏁?,
  `long_prop_2` bigint DEFAULT NULL COMMENT 'long绫诲瀷鐨則rigger鐨勭浜屼釜鍙傛暟',
  `dec_prop_1` decimal(13,4) DEFAULT NULL COMMENT 'decimal绫诲瀷鐨則rigger鐨勭涓€涓弬鏁?,
  `dec_prop_2` decimal(13,4) DEFAULT NULL COMMENT 'decimal绫诲瀷鐨則rigger鐨勭浜屼釜鍙傛暟',
  `bool_prop_1` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Boolean绫诲瀷鐨則rigger鐨勭涓€涓弬鏁?,
  `bool_prop_2` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Boolean绫诲瀷鐨則rigger鐨勭浜屼釜鍙傛暟',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='鍚屾鏈哄埗鐨勮閿佽〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_simprop_triggers`
--

LOCK TABLES `qrtz_simprop_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_simprop_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_simprop_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_triggers`
--

DROP TABLE IF EXISTS `qrtz_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冨害鍚嶇О',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '瑙﹀彂鍣ㄧ殑鍚嶅瓧',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '瑙﹀彂鍣ㄦ墍灞炵粍鐨勫悕瀛?,
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details琛╦ob_name鐨勫閿?,
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details琛╦ob_group鐨勫閿?,
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鐩稿叧浠嬬粛',
  `next_fire_time` bigint DEFAULT NULL COMMENT '涓婁竴娆¤Е鍙戞椂闂达紙姣锛?,
  `prev_fire_time` bigint DEFAULT NULL COMMENT '涓嬩竴娆¤Е鍙戞椂闂达紙榛樿涓?1琛ㄧず涓嶈Е鍙戯級',
  `priority` int DEFAULT NULL COMMENT '浼樺厛绾?,
  `trigger_state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '瑙﹀彂鍣ㄧ姸鎬?,
  `trigger_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '瑙﹀彂鍣ㄧ殑绫诲瀷',
  `start_time` bigint NOT NULL COMMENT '寮€濮嬫椂闂?,
  `end_time` bigint DEFAULT NULL COMMENT '缁撴潫鏃堕棿',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏃ョ▼琛ㄥ悕绉?,
  `misfire_instr` smallint DEFAULT NULL COMMENT '琛ュ伩鎵ц鐨勭瓥鐣?,
  `job_data` blob COMMENT '瀛樻斁鎸佷箙鍖杍ob瀵硅薄',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  KEY `sched_name` (`sched_name`,`job_name`,`job_group`),
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='瑙﹀彂鍣ㄨ缁嗕俊鎭〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_triggers`
--

LOCK TABLES `qrtz_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '鍙傛暟涓婚敭',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍙傛暟鍚嶇О',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍙傛暟閿悕',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍙傛暟閿€?,
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'N' COMMENT '绯荤粺鍐呯疆锛圷鏄?N鍚︼級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '澶囨敞',
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='鍙傛暟閰嶇疆琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` VALUES (1,'涓绘鏋堕〉-榛樿鐨偆鏍峰紡鍚嶇О','sys.index.skinName','skin-blue','Y','admin','2026-01-16 13:11:15','',NULL,'钃濊壊 skin-blue銆佺豢鑹?skin-green銆佺传鑹?skin-purple銆佺孩鑹?skin-red銆侀粍鑹?skin-yellow'),(2,'鐢ㄦ埛绠＄悊-璐﹀彿鍒濆瀵嗙爜','sys.user.initPassword','123456','Y','admin','2026-01-16 13:11:15','',NULL,'鍒濆鍖栧瘑鐮?123456'),(3,'涓绘鏋堕〉-渚ц竟鏍忎富棰?,'sys.index.sideTheme','theme-dark','Y','admin','2026-01-16 13:11:15','',NULL,'娣辫壊涓婚theme-dark锛屾祬鑹蹭富棰榯heme-light'),(4,'璐﹀彿鑷姪-楠岃瘉鐮佸紑鍏?,'sys.account.captchaEnabled','true','Y','admin','2026-01-16 13:11:15','',NULL,'鏄惁寮€鍚獙璇佺爜鍔熻兘锛坱rue寮€鍚紝false鍏抽棴锛?),(5,'璐﹀彿鑷姪-鏄惁寮€鍚敤鎴锋敞鍐屽姛鑳?,'sys.account.registerUser','false','Y','admin','2026-01-16 13:11:15','',NULL,'鏄惁寮€鍚敞鍐岀敤鎴峰姛鑳斤紙true寮€鍚紝false鍏抽棴锛?),(6,'鐢ㄦ埛鐧诲綍-榛戝悕鍗曞垪琛?,'sys.login.blackIPList','','Y','admin','2026-01-16 13:11:15','',NULL,'璁剧疆鐧诲綍IP榛戝悕鍗曢檺鍒讹紝澶氫釜鍖归厤椤逛互;鍒嗛殧锛屾敮鎸佸尮閰嶏紙*閫氶厤銆佺綉娈碉級');
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dept`
--

DROP TABLE IF EXISTS `sys_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dept` (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '閮ㄩ棬id',
  `parent_id` bigint DEFAULT '0' COMMENT '鐖堕儴闂╥d',
  `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '绁栫骇鍒楄〃',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '閮ㄩ棬鍚嶇О',
  `order_num` int DEFAULT '0' COMMENT '鏄剧ず椤哄簭',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '璐熻矗浜?,
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鑱旂郴鐢佃瘽',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '閭',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '閮ㄩ棬鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?浠ｈ〃瀛樺湪 2浠ｈ〃鍒犻櫎锛?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='閮ㄩ棬琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dept`
--

LOCK TABLES `sys_dept` WRITE;
/*!40000 ALTER TABLE `sys_dept` DISABLE KEYS */;
INSERT INTO `sys_dept` VALUES (100,0,'0','鑻ヤ緷绉戞妧',0,'鑻ヤ緷','15888888888','ops@qkyd.cn','0','0','admin','2026-01-16 13:11:13','',NULL),(101,100,'0,100','娣卞湷鎬诲叕鍙?,1,'鑻ヤ緷','15888888888','ops@qkyd.cn','0','0','admin','2026-01-16 13:11:13','',NULL),(102,100,'0,100','闀挎矙鍒嗗叕鍙?,2,'鑻ヤ緷','15888888888','ops@qkyd.cn','0','0','admin','2026-01-16 13:11:13','',NULL),(103,101,'0,100,101','鐮斿彂閮ㄩ棬',1,'鑻ヤ緷','15888888888','ops@qkyd.cn','0','0','admin','2026-01-16 13:11:13','',NULL),(104,101,'0,100,101','甯傚満閮ㄩ棬',2,'鑻ヤ緷','15888888888','ops@qkyd.cn','0','0','admin','2026-01-16 13:11:13','',NULL),(105,101,'0,100,101','娴嬭瘯閮ㄩ棬',3,'鑻ヤ緷','15888888888','ops@qkyd.cn','0','0','admin','2026-01-16 13:11:13','',NULL),(106,101,'0,100,101','璐㈠姟閮ㄩ棬',4,'鑻ヤ緷','15888888888','ops@qkyd.cn','0','0','admin','2026-01-16 13:11:13','',NULL),(107,101,'0,100,101','杩愮淮閮ㄩ棬',5,'鑻ヤ緷','15888888888','ops@qkyd.cn','0','0','admin','2026-01-16 13:11:13','',NULL),(108,102,'0,100,102','甯傚満閮ㄩ棬',1,'鑻ヤ緷','15888888888','ops@qkyd.cn','0','0','admin','2026-01-16 13:11:13','',NULL),(109,102,'0,100,102','璐㈠姟閮ㄩ棬',2,'鑻ヤ緷','15888888888','ops@qkyd.cn','0','0','admin','2026-01-16 13:11:13','',NULL);
/*!40000 ALTER TABLE `sys_dept` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict_data`
--

DROP TABLE IF EXISTS `sys_dict_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_data` (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '瀛楀吀缂栫爜',
  `dict_sort` int DEFAULT '0' COMMENT '瀛楀吀鎺掑簭',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '瀛楀吀鏍囩',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '瀛楀吀閿€?,
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '瀛楀吀绫诲瀷',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏍峰紡灞炴€э紙鍏朵粬鏍峰紡鎵╁睍锛?,
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '琛ㄦ牸鍥炴樉鏍峰紡',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'N' COMMENT '鏄惁榛樿锛圷鏄?N鍚︼級',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '澶囨敞',
  PRIMARY KEY (`dict_code`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='瀛楀吀鏁版嵁琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict_data`
--

LOCK TABLES `sys_dict_data` WRITE;
/*!40000 ALTER TABLE `sys_dict_data` DISABLE KEYS */;
INSERT INTO `sys_dict_data` VALUES (1,1,'鐢?,'0','sys_user_sex','','','Y','0','admin','2026-01-16 13:11:15','',NULL,'鎬у埆鐢?),(2,2,'濂?,'1','sys_user_sex','','','N','0','admin','2026-01-16 13:11:15','',NULL,'鎬у埆濂?),(3,3,'鏈煡','2','sys_user_sex','','','N','0','admin','2026-01-16 13:11:15','',NULL,'鎬у埆鏈煡'),(4,1,'鏄剧ず','0','sys_show_hide','','primary','Y','0','admin','2026-01-16 13:11:15','',NULL,'鏄剧ず鑿滃崟'),(5,2,'闅愯棌','1','sys_show_hide','','danger','N','0','admin','2026-01-16 13:11:15','',NULL,'闅愯棌鑿滃崟'),(6,1,'姝ｅ父','0','sys_normal_disable','','primary','Y','0','admin','2026-01-16 13:11:15','',NULL,'姝ｅ父鐘舵€?),(7,2,'鍋滅敤','1','sys_normal_disable','','danger','N','0','admin','2026-01-16 13:11:15','',NULL,'鍋滅敤鐘舵€?),(8,1,'姝ｅ父','0','sys_job_status','','primary','Y','0','admin','2026-01-16 13:11:15','',NULL,'姝ｅ父鐘舵€?),(9,2,'鏆傚仠','1','sys_job_status','','danger','N','0','admin','2026-01-16 13:11:15','',NULL,'鍋滅敤鐘舵€?),(10,1,'榛樿','DEFAULT','sys_job_group','','','Y','0','admin','2026-01-16 13:11:15','',NULL,'榛樿鍒嗙粍'),(11,2,'绯荤粺','SYSTEM','sys_job_group','','','N','0','admin','2026-01-16 13:11:15','',NULL,'绯荤粺鍒嗙粍'),(12,1,'鏄?,'Y','sys_yes_no','','primary','Y','0','admin','2026-01-16 13:11:15','',NULL,'绯荤粺榛樿鏄?),(13,2,'鍚?,'N','sys_yes_no','','danger','N','0','admin','2026-01-16 13:11:15','',NULL,'绯荤粺榛樿鍚?),(14,1,'閫氱煡','1','sys_notice_type','','warning','Y','0','admin','2026-01-16 13:11:15','',NULL,'閫氱煡'),(15,2,'鍏憡','2','sys_notice_type','','success','N','0','admin','2026-01-16 13:11:15','',NULL,'鍏憡'),(16,1,'姝ｅ父','0','sys_notice_status','','primary','Y','0','admin','2026-01-16 13:11:15','',NULL,'姝ｅ父鐘舵€?),(17,2,'鍏抽棴','1','sys_notice_status','','danger','N','0','admin','2026-01-16 13:11:15','',NULL,'鍏抽棴鐘舵€?),(18,99,'鍏朵粬','0','sys_oper_type','','info','N','0','admin','2026-01-16 13:11:15','',NULL,'鍏朵粬鎿嶄綔'),(19,1,'鏂板','1','sys_oper_type','','info','N','0','admin','2026-01-16 13:11:15','',NULL,'鏂板鎿嶄綔'),(20,2,'淇敼','2','sys_oper_type','','info','N','0','admin','2026-01-16 13:11:15','',NULL,'淇敼鎿嶄綔'),(21,3,'鍒犻櫎','3','sys_oper_type','','danger','N','0','admin','2026-01-16 13:11:15','',NULL,'鍒犻櫎鎿嶄綔'),(22,4,'鎺堟潈','4','sys_oper_type','','primary','N','0','admin','2026-01-16 13:11:15','',NULL,'鎺堟潈鎿嶄綔'),(23,5,'瀵煎嚭','5','sys_oper_type','','warning','N','0','admin','2026-01-16 13:11:15','',NULL,'瀵煎嚭鎿嶄綔'),(24,6,'瀵煎叆','6','sys_oper_type','','warning','N','0','admin','2026-01-16 13:11:15','',NULL,'瀵煎叆鎿嶄綔'),(25,7,'寮洪€€','7','sys_oper_type','','danger','N','0','admin','2026-01-16 13:11:15','',NULL,'寮洪€€鎿嶄綔'),(26,8,'鐢熸垚浠ｇ爜','8','sys_oper_type','','warning','N','0','admin','2026-01-16 13:11:15','',NULL,'鐢熸垚鎿嶄綔'),(27,9,'娓呯┖鏁版嵁','9','sys_oper_type','','danger','N','0','admin','2026-01-16 13:11:15','',NULL,'娓呯┖鎿嶄綔'),(28,1,'鎴愬姛','0','sys_common_status','','primary','N','0','admin','2026-01-16 13:11:15','',NULL,'姝ｅ父鐘舵€?),(29,2,'澶辫触','1','sys_common_status','','danger','N','0','admin','2026-01-16 13:11:15','',NULL,'鍋滅敤鐘舵€?);
/*!40000 ALTER TABLE `sys_dict_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict_type`
--

DROP TABLE IF EXISTS `sys_dict_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_type` (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '瀛楀吀涓婚敭',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '瀛楀吀鍚嶇О',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '瀛楀吀绫诲瀷',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '澶囨敞',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='瀛楀吀绫诲瀷琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict_type`
--

LOCK TABLES `sys_dict_type` WRITE;
/*!40000 ALTER TABLE `sys_dict_type` DISABLE KEYS */;
INSERT INTO `sys_dict_type` VALUES (1,'鐢ㄦ埛鎬у埆','sys_user_sex','0','admin','2026-01-16 13:11:15','',NULL,'鐢ㄦ埛鎬у埆鍒楄〃'),(2,'鑿滃崟鐘舵€?,'sys_show_hide','0','admin','2026-01-16 13:11:15','',NULL,'鑿滃崟鐘舵€佸垪琛?),(3,'绯荤粺寮€鍏?,'sys_normal_disable','0','admin','2026-01-16 13:11:15','',NULL,'绯荤粺寮€鍏冲垪琛?),(4,'浠诲姟鐘舵€?,'sys_job_status','0','admin','2026-01-16 13:11:15','',NULL,'浠诲姟鐘舵€佸垪琛?),(5,'浠诲姟鍒嗙粍','sys_job_group','0','admin','2026-01-16 13:11:15','',NULL,'浠诲姟鍒嗙粍鍒楄〃'),(6,'绯荤粺鏄惁','sys_yes_no','0','admin','2026-01-16 13:11:15','',NULL,'绯荤粺鏄惁鍒楄〃'),(7,'閫氱煡绫诲瀷','sys_notice_type','0','admin','2026-01-16 13:11:15','',NULL,'閫氱煡绫诲瀷鍒楄〃'),(8,'閫氱煡鐘舵€?,'sys_notice_status','0','admin','2026-01-16 13:11:15','',NULL,'閫氱煡鐘舵€佸垪琛?),(9,'鎿嶄綔绫诲瀷','sys_oper_type','0','admin','2026-01-16 13:11:15','',NULL,'鎿嶄綔绫诲瀷鍒楄〃'),(10,'绯荤粺鐘舵€?,'sys_common_status','0','admin','2026-01-16 13:11:15','',NULL,'鐧诲綍鐘舵€佸垪琛?);
/*!40000 ALTER TABLE `sys_dict_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job`
--

DROP TABLE IF EXISTS `sys_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job` (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '浠诲姟ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '浠诲姟鍚嶇О',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '浠诲姟缁勫悕',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冪敤鐩爣瀛楃涓?,
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'cron鎵ц琛ㄨ揪寮?,
  `misfire_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '3' COMMENT '璁″垝鎵ц閿欒绛栫暐锛?绔嬪嵆鎵ц 2鎵ц涓€娆?3鏀惧純鎵ц锛?,
  `concurrent` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '鏄惁骞跺彂鎵ц锛?鍏佽 1绂佹锛?,
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鐘舵€侊紙0姝ｅ父 1鏆傚仠锛?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '澶囨敞淇℃伅',
  PRIMARY KEY (`job_id`,`job_name`,`job_group`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='瀹氭椂浠诲姟璋冨害琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job`
--

LOCK TABLES `sys_job` WRITE;
/*!40000 ALTER TABLE `sys_job` DISABLE KEYS */;
INSERT INTO `sys_job` VALUES (1,'绯荤粺榛樿锛堟棤鍙傦級','DEFAULT','ryTask.ryNoParams','0/10 * * * * ?','3','1','1','admin','2026-01-16 13:11:15','',NULL,''),(2,'绯荤粺榛樿锛堟湁鍙傦級','DEFAULT','ryTask.ryParams(\'ry\')','0/15 * * * * ?','3','1','1','admin','2026-01-16 13:11:15','',NULL,''),(3,'绯荤粺榛樿锛堝鍙傦級','DEFAULT','ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)','0/20 * * * * ?','3','1','1','admin','2026-01-16 13:11:15','',NULL,'');
/*!40000 ALTER TABLE `sys_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_log`
--

DROP TABLE IF EXISTS `sys_job_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_log` (
  `job_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '浠诲姟鏃ュ織ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '浠诲姟鍚嶇О',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '浠诲姟缁勫悕',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '璋冪敤鐩爣瀛楃涓?,
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏃ュ織淇℃伅',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鎵ц鐘舵€侊紙0姝ｅ父 1澶辫触锛?,
  `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '寮傚父淇℃伅',
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`job_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='瀹氭椂浠诲姟璋冨害鏃ュ織琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_log`
--

LOCK TABLES `sys_job_log` WRITE;
/*!40000 ALTER TABLE `sys_job_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_job_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_logininfor`
--

DROP TABLE IF EXISTS `sys_logininfor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_logininfor` (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '璁块棶ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鐢ㄦ埛璐﹀彿',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鐧诲綍IP鍦板潃',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鐧诲綍鍦扮偣',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '娴忚鍣ㄧ被鍨?,
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鎿嶄綔绯荤粺',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鐧诲綍鐘舵€侊紙0鎴愬姛 1澶辫触锛?,
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鎻愮ず娑堟伅',
  `login_time` datetime DEFAULT NULL COMMENT '璁块棶鏃堕棿',
  PRIMARY KEY (`info_id`),
  KEY `idx_sys_logininfor_s` (`status`),
  KEY `idx_sys_logininfor_lt` (`login_time`)
) ENGINE=InnoDB AUTO_INCREMENT=177 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='绯荤粺璁块棶璁板綍';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_logininfor`
--

LOCK TABLES `sys_logininfor` WRITE;
/*!40000 ALTER TABLE `sys_logininfor` DISABLE KEYS */;
INSERT INTO `sys_logininfor` VALUES (100,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','1','楠岃瘉鐮侀敊璇?,'2026-01-19 22:57:31'),(101,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','1','楠岃瘉鐮侀敊璇?,'2026-01-19 22:57:53'),(102,'admin','127.0.0.1','鍐呯綉IP','Robot/Spider','Unknown','1','楠岃瘉鐮佸凡澶辨晥','2026-01-19 23:02:34'),(103,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-19 23:03:47'),(104,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-20 10:45:43'),(105,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-20 11:56:00'),(106,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-20 21:14:19'),(107,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-20 21:57:12'),(108,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-20 22:07:01'),(109,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','1','鐢ㄦ埛涓嶅瓨鍦?瀵嗙爜閿欒','2026-01-20 22:20:41'),(110,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','1','楠岃瘉鐮侀敊璇?,'2026-01-20 22:20:48'),(111,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-20 22:20:51'),(112,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-20 22:29:10'),(113,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-20 23:03:02'),(114,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-21 01:08:18'),(115,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-21 11:46:02'),(116,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','1','楠岃瘉鐮侀敊璇?,'2026-01-22 18:08:07'),(117,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 18:08:11'),(118,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 18:08:40'),(119,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 18:11:15'),(120,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 18:11:16'),(121,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 18:11:29'),(122,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 18:11:29'),(123,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 18:13:11'),(124,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 18:13:11'),(125,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','1','楠岃瘉鐮佸凡澶辨晥','2026-01-22 18:13:13'),(126,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 18:13:17'),(127,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 18:13:18'),(128,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 18:17:17'),(129,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 18:17:17'),(130,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 18:18:45'),(131,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 18:18:45'),(132,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 18:33:35'),(133,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 18:33:35'),(134,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 18:36:00'),(135,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 18:36:00'),(136,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 19:00:51'),(137,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 19:00:52'),(138,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 19:18:32'),(139,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 19:18:32'),(140,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','1','楠岃瘉鐮佸凡澶辨晥','2026-01-22 19:18:34'),(141,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 19:18:34'),(142,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 19:18:34'),(143,'admin','127.0.0.1','鍐呯綉IP','Mozilla','Windows 10','1','楠岃瘉鐮佸凡澶辨晥','2026-01-22 19:20:43'),(144,'admin','127.0.0.1','鍐呯綉IP','Mozilla','Windows 10','1','楠岃瘉鐮侀敊璇?,'2026-01-22 19:22:01'),(145,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 19:27:51'),(146,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 19:27:51'),(147,'admin','127.0.0.1','鍐呯綉IP','Mozilla','Windows 10','1','楠岃瘉鐮侀敊璇?,'2026-01-22 19:27:53'),(148,'admin','127.0.0.1','鍐呯綉IP','Mozilla','Windows 10','1','楠岃瘉鐮侀敊璇?,'2026-01-22 19:28:32'),(149,'admin','127.0.0.1','鍐呯綉IP','Mozilla','Windows 10','1','楠岃瘉鐮侀敊璇?,'2026-01-22 19:28:52'),(150,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 22:49:54'),(151,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','1','楠岃瘉鐮侀敊璇?,'2026-01-22 22:49:59'),(152,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 22:50:03'),(153,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','1','楠岃瘉鐮佸凡澶辨晥','2026-01-22 22:52:12'),(154,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 22:52:17'),(155,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 22:52:17'),(156,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 22:52:27'),(157,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 22:52:27'),(158,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 22:53:11'),(159,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 22:53:11'),(160,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','1','楠岃瘉鐮佸凡澶辨晥','2026-01-22 22:54:00'),(161,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 22:54:12'),(162,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 22:54:12'),(163,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','1','楠岃瘉鐮佸凡澶辨晥','2026-01-22 22:56:50'),(164,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 22:56:54'),(165,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 22:56:55'),(166,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 22:58:48'),(167,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 22:58:48'),(168,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 22:59:17'),(169,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-22 22:59:17'),(170,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-22 23:01:34'),(171,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-23 20:11:11'),(172,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-23 21:39:43'),(173,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-23 21:39:47'),(174,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','閫€鍑烘垚鍔?,'2026-01-23 21:54:49'),(175,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-23 21:54:53'),(176,'admin','127.0.0.1','鍐呯綉IP','Chrome 14','Windows 10','0','鐧诲綍鎴愬姛','2026-01-24 13:44:43');
/*!40000 ALTER TABLE `sys_logininfor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_menu`
--

DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑿滃崟ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鑿滃崟鍚嶇О',
  `parent_id` bigint DEFAULT '0' COMMENT '鐖惰彍鍗旾D',
  `order_num` int DEFAULT '0' COMMENT '鏄剧ず椤哄簭',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '璺敱鍦板潃',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '缁勪欢璺緞',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '璺敱鍙傛暟',
  `is_frame` int DEFAULT '1' COMMENT '鏄惁涓哄閾撅紙0鏄?1鍚︼級',
  `is_cache` int DEFAULT '0' COMMENT '鏄惁缂撳瓨锛?缂撳瓨 1涓嶇紦瀛橈級',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鑿滃崟绫诲瀷锛圡鐩綍 C鑿滃崟 F鎸夐挳锛?,
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鑿滃崟鐘舵€侊紙0鏄剧ず 1闅愯棌锛?,
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鑿滃崟鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏉冮檺鏍囪瘑',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '#' COMMENT '鑿滃崟鍥炬爣',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '澶囨敞',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2026 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='鑿滃崟鏉冮檺琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_menu`
--

LOCK TABLES `sys_menu` WRITE;
/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` VALUES (1,'绯荤粺绠＄悊',0,1,'system',NULL,'',1,0,'M','0','0','','system','admin','2026-01-16 13:11:14','',NULL,'绯荤粺绠＄悊鐩綍'),(2,'绯荤粺鐩戞帶',0,2,'monitor',NULL,'',1,0,'M','0','0','','monitor','admin','2026-01-16 13:11:14','',NULL,'绯荤粺鐩戞帶鐩綍'),(3,'绯荤粺宸ュ叿',0,3,'tool',NULL,'',1,0,'M','0','0','','tool','admin','2026-01-16 13:11:14','',NULL,'绯荤粺宸ュ叿鐩綍'),(4,'鑻ヤ緷瀹樼綉',0,4,'https://www.qkyd.cn',NULL,'',0,0,'M','0','0','','guide','admin','2026-01-16 13:11:14','',NULL,'鑻ヤ緷瀹樼綉鍦板潃'),(100,'鐢ㄦ埛绠＄悊',1,1,'user','system/user/index','',1,0,'C','0','0','system:user:list','user','admin','2026-01-16 13:11:14','',NULL,'鐢ㄦ埛绠＄悊鑿滃崟'),(101,'瑙掕壊绠＄悊',1,2,'role','system/role/index','',1,0,'C','0','0','system:role:list','peoples','admin','2026-01-16 13:11:14','',NULL,'瑙掕壊绠＄悊鑿滃崟'),(102,'鑿滃崟绠＄悊',1,3,'menu','system/menu/index','',1,0,'C','0','0','system:menu:list','tree-table','admin','2026-01-16 13:11:14','',NULL,'鑿滃崟绠＄悊鑿滃崟'),(103,'閮ㄩ棬绠＄悊',1,4,'dept','system/dept/index','',1,0,'C','0','0','system:dept:list','tree','admin','2026-01-16 13:11:14','',NULL,'閮ㄩ棬绠＄悊鑿滃崟'),(104,'宀椾綅绠＄悊',1,5,'post','system/post/index','',1,0,'C','0','0','system:post:list','post','admin','2026-01-16 13:11:14','',NULL,'宀椾綅绠＄悊鑿滃崟'),(105,'瀛楀吀绠＄悊',1,6,'dict','system/dict/index','',1,0,'C','0','0','system:dict:list','dict','admin','2026-01-16 13:11:14','',NULL,'瀛楀吀绠＄悊鑿滃崟'),(106,'鍙傛暟璁剧疆',1,7,'config','system/config/index','',1,0,'C','0','0','system:config:list','edit','admin','2026-01-16 13:11:14','',NULL,'鍙傛暟璁剧疆鑿滃崟'),(107,'閫氱煡鍏憡',1,8,'notice','system/notice/index','',1,0,'C','0','0','system:notice:list','message','admin','2026-01-16 13:11:14','',NULL,'閫氱煡鍏憡鑿滃崟'),(108,'鏃ュ織绠＄悊',1,9,'log','','',1,0,'M','0','0','','log','admin','2026-01-16 13:11:14','',NULL,'鏃ュ織绠＄悊鑿滃崟'),(109,'鍦ㄧ嚎鐢ㄦ埛',2,1,'online','monitor/online/index','',1,0,'C','0','0','monitor:online:list','online','admin','2026-01-16 13:11:14','',NULL,'鍦ㄧ嚎鐢ㄦ埛鑿滃崟'),(110,'瀹氭椂浠诲姟',2,2,'job','monitor/job/index','',1,0,'C','0','0','monitor:job:list','job','admin','2026-01-16 13:11:14','',NULL,'瀹氭椂浠诲姟鑿滃崟'),(111,'鏁版嵁鐩戞帶',2,3,'druid','monitor/druid/index','',1,0,'C','0','0','monitor:druid:list','druid','admin','2026-01-16 13:11:14','',NULL,'鏁版嵁鐩戞帶鑿滃崟'),(112,'鏈嶅姟鐩戞帶',2,4,'server','monitor/server/index','',1,0,'C','0','0','monitor:server:list','server','admin','2026-01-16 13:11:14','',NULL,'鏈嶅姟鐩戞帶鑿滃崟'),(113,'缂撳瓨鐩戞帶',2,5,'cache','monitor/cache/index','',1,0,'C','0','0','monitor:cache:list','redis','admin','2026-01-16 13:11:14','',NULL,'缂撳瓨鐩戞帶鑿滃崟'),(114,'缂撳瓨鍒楄〃',2,6,'cacheList','monitor/cache/list','',1,0,'C','0','0','monitor:cache:list','redis-list','admin','2026-01-16 13:11:14','',NULL,'缂撳瓨鍒楄〃鑿滃崟'),(115,'琛ㄥ崟鏋勫缓',3,1,'build','tool/build/index','',1,0,'C','0','0','tool:build:list','build','admin','2026-01-16 13:11:14','',NULL,'琛ㄥ崟鏋勫缓鑿滃崟'),(116,'浠ｇ爜鐢熸垚',3,2,'gen','tool/gen/index','',1,0,'C','0','0','tool:gen:list','code','admin','2026-01-16 13:11:14','',NULL,'浠ｇ爜鐢熸垚鑿滃崟'),(117,'绯荤粺鎺ュ彛',3,3,'swagger','tool/swagger/index','',1,0,'C','0','0','tool:swagger:list','swagger','admin','2026-01-16 13:11:14','',NULL,'绯荤粺鎺ュ彛鑿滃崟'),(500,'鎿嶄綔鏃ュ織',108,1,'operlog','monitor/operlog/index','',1,0,'C','0','0','monitor:operlog:list','form','admin','2026-01-16 13:11:14','',NULL,'鎿嶄綔鏃ュ織鑿滃崟'),(501,'鐧诲綍鏃ュ織',108,2,'logininfor','monitor/logininfor/index','',1,0,'C','0','0','monitor:logininfor:list','logininfor','admin','2026-01-16 13:11:14','',NULL,'鐧诲綍鏃ュ織鑿滃崟'),(1000,'鐢ㄦ埛鏌ヨ',100,1,'','','',1,0,'F','0','0','system:user:query','#','admin','2026-01-16 13:11:14','',NULL,''),(1001,'鐢ㄦ埛鏂板',100,2,'','','',1,0,'F','0','0','system:user:add','#','admin','2026-01-16 13:11:14','',NULL,''),(1002,'鐢ㄦ埛淇敼',100,3,'','','',1,0,'F','0','0','system:user:edit','#','admin','2026-01-16 13:11:14','',NULL,''),(1003,'鐢ㄦ埛鍒犻櫎',100,4,'','','',1,0,'F','0','0','system:user:remove','#','admin','2026-01-16 13:11:14','',NULL,''),(1004,'鐢ㄦ埛瀵煎嚭',100,5,'','','',1,0,'F','0','0','system:user:export','#','admin','2026-01-16 13:11:14','',NULL,''),(1005,'鐢ㄦ埛瀵煎叆',100,6,'','','',1,0,'F','0','0','system:user:import','#','admin','2026-01-16 13:11:14','',NULL,''),(1006,'閲嶇疆瀵嗙爜',100,7,'','','',1,0,'F','0','0','system:user:resetPwd','#','admin','2026-01-16 13:11:14','',NULL,''),(1007,'瑙掕壊鏌ヨ',101,1,'','','',1,0,'F','0','0','system:role:query','#','admin','2026-01-16 13:11:14','',NULL,''),(1008,'瑙掕壊鏂板',101,2,'','','',1,0,'F','0','0','system:role:add','#','admin','2026-01-16 13:11:14','',NULL,''),(1009,'瑙掕壊淇敼',101,3,'','','',1,0,'F','0','0','system:role:edit','#','admin','2026-01-16 13:11:14','',NULL,''),(1010,'瑙掕壊鍒犻櫎',101,4,'','','',1,0,'F','0','0','system:role:remove','#','admin','2026-01-16 13:11:14','',NULL,''),(1011,'瑙掕壊瀵煎嚭',101,5,'','','',1,0,'F','0','0','system:role:export','#','admin','2026-01-16 13:11:14','',NULL,''),(1012,'鑿滃崟鏌ヨ',102,1,'','','',1,0,'F','0','0','system:menu:query','#','admin','2026-01-16 13:11:14','',NULL,''),(1013,'鑿滃崟鏂板',102,2,'','','',1,0,'F','0','0','system:menu:add','#','admin','2026-01-16 13:11:14','',NULL,''),(1014,'鑿滃崟淇敼',102,3,'','','',1,0,'F','0','0','system:menu:edit','#','admin','2026-01-16 13:11:14','',NULL,''),(1015,'鑿滃崟鍒犻櫎',102,4,'','','',1,0,'F','0','0','system:menu:remove','#','admin','2026-01-16 13:11:14','',NULL,''),(1016,'閮ㄩ棬鏌ヨ',103,1,'','','',1,0,'F','0','0','system:dept:query','#','admin','2026-01-16 13:11:14','',NULL,''),(1017,'閮ㄩ棬鏂板',103,2,'','','',1,0,'F','0','0','system:dept:add','#','admin','2026-01-16 13:11:14','',NULL,''),(1018,'閮ㄩ棬淇敼',103,3,'','','',1,0,'F','0','0','system:dept:edit','#','admin','2026-01-16 13:11:14','',NULL,''),(1019,'閮ㄩ棬鍒犻櫎',103,4,'','','',1,0,'F','0','0','system:dept:remove','#','admin','2026-01-16 13:11:14','',NULL,''),(1020,'宀椾綅鏌ヨ',104,1,'','','',1,0,'F','0','0','system:post:query','#','admin','2026-01-16 13:11:14','',NULL,''),(1021,'宀椾綅鏂板',104,2,'','','',1,0,'F','0','0','system:post:add','#','admin','2026-01-16 13:11:14','',NULL,''),(1022,'宀椾綅淇敼',104,3,'','','',1,0,'F','0','0','system:post:edit','#','admin','2026-01-16 13:11:14','',NULL,''),(1023,'宀椾綅鍒犻櫎',104,4,'','','',1,0,'F','0','0','system:post:remove','#','admin','2026-01-16 13:11:14','',NULL,''),(1024,'宀椾綅瀵煎嚭',104,5,'','','',1,0,'F','0','0','system:post:export','#','admin','2026-01-16 13:11:14','',NULL,''),(1025,'瀛楀吀鏌ヨ',105,1,'#','','',1,0,'F','0','0','system:dict:query','#','admin','2026-01-16 13:11:14','',NULL,''),(1026,'瀛楀吀鏂板',105,2,'#','','',1,0,'F','0','0','system:dict:add','#','admin','2026-01-16 13:11:14','',NULL,''),(1027,'瀛楀吀淇敼',105,3,'#','','',1,0,'F','0','0','system:dict:edit','#','admin','2026-01-16 13:11:14','',NULL,''),(1028,'瀛楀吀鍒犻櫎',105,4,'#','','',1,0,'F','0','0','system:dict:remove','#','admin','2026-01-16 13:11:14','',NULL,''),(1029,'瀛楀吀瀵煎嚭',105,5,'#','','',1,0,'F','0','0','system:dict:export','#','admin','2026-01-16 13:11:14','',NULL,''),(1030,'鍙傛暟鏌ヨ',106,1,'#','','',1,0,'F','0','0','system:config:query','#','admin','2026-01-16 13:11:14','',NULL,''),(1031,'鍙傛暟鏂板',106,2,'#','','',1,0,'F','0','0','system:config:add','#','admin','2026-01-16 13:11:14','',NULL,''),(1032,'鍙傛暟淇敼',106,3,'#','','',1,0,'F','0','0','system:config:edit','#','admin','2026-01-16 13:11:14','',NULL,''),(1033,'鍙傛暟鍒犻櫎',106,4,'#','','',1,0,'F','0','0','system:config:remove','#','admin','2026-01-16 13:11:14','',NULL,''),(1034,'鍙傛暟瀵煎嚭',106,5,'#','','',1,0,'F','0','0','system:config:export','#','admin','2026-01-16 13:11:14','',NULL,''),(1035,'鍏憡鏌ヨ',107,1,'#','','',1,0,'F','0','0','system:notice:query','#','admin','2026-01-16 13:11:14','',NULL,''),(1036,'鍏憡鏂板',107,2,'#','','',1,0,'F','0','0','system:notice:add','#','admin','2026-01-16 13:11:14','',NULL,''),(1037,'鍏憡淇敼',107,3,'#','','',1,0,'F','0','0','system:notice:edit','#','admin','2026-01-16 13:11:14','',NULL,''),(1038,'鍏憡鍒犻櫎',107,4,'#','','',1,0,'F','0','0','system:notice:remove','#','admin','2026-01-16 13:11:14','',NULL,''),(1039,'鎿嶄綔鏌ヨ',500,1,'#','','',1,0,'F','0','0','monitor:operlog:query','#','admin','2026-01-16 13:11:14','',NULL,''),(1040,'鎿嶄綔鍒犻櫎',500,2,'#','','',1,0,'F','0','0','monitor:operlog:remove','#','admin','2026-01-16 13:11:14','',NULL,''),(1041,'鏃ュ織瀵煎嚭',500,3,'#','','',1,0,'F','0','0','monitor:operlog:export','#','admin','2026-01-16 13:11:14','',NULL,''),(1042,'鐧诲綍鏌ヨ',501,1,'#','','',1,0,'F','0','0','monitor:logininfor:query','#','admin','2026-01-16 13:11:14','',NULL,''),(1043,'鐧诲綍鍒犻櫎',501,2,'#','','',1,0,'F','0','0','monitor:logininfor:remove','#','admin','2026-01-16 13:11:14','',NULL,''),(1044,'鏃ュ織瀵煎嚭',501,3,'#','','',1,0,'F','0','0','monitor:logininfor:export','#','admin','2026-01-16 13:11:14','',NULL,''),(1045,'璐︽埛瑙ｉ攣',501,4,'#','','',1,0,'F','0','0','monitor:logininfor:unlock','#','admin','2026-01-16 13:11:14','',NULL,''),(1046,'鍦ㄧ嚎鏌ヨ',109,1,'#','','',1,0,'F','0','0','monitor:online:query','#','admin','2026-01-16 13:11:14','',NULL,''),(1047,'鎵归噺寮洪€€',109,2,'#','','',1,0,'F','0','0','monitor:online:batchLogout','#','admin','2026-01-16 13:11:14','',NULL,''),(1048,'鍗曟潯寮洪€€',109,3,'#','','',1,0,'F','0','0','monitor:online:forceLogout','#','admin','2026-01-16 13:11:14','',NULL,''),(1049,'浠诲姟鏌ヨ',110,1,'#','','',1,0,'F','0','0','monitor:job:query','#','admin','2026-01-16 13:11:14','',NULL,''),(1050,'浠诲姟鏂板',110,2,'#','','',1,0,'F','0','0','monitor:job:add','#','admin','2026-01-16 13:11:14','',NULL,''),(1051,'浠诲姟淇敼',110,3,'#','','',1,0,'F','0','0','monitor:job:edit','#','admin','2026-01-16 13:11:14','',NULL,''),(1052,'浠诲姟鍒犻櫎',110,4,'#','','',1,0,'F','0','0','monitor:job:remove','#','admin','2026-01-16 13:11:14','',NULL,''),(1053,'鐘舵€佷慨鏀?,110,5,'#','','',1,0,'F','0','0','monitor:job:changeStatus','#','admin','2026-01-16 13:11:14','',NULL,''),(1054,'浠诲姟瀵煎嚭',110,6,'#','','',1,0,'F','0','0','monitor:job:export','#','admin','2026-01-16 13:11:14','',NULL,''),(1055,'鐢熸垚鏌ヨ',116,1,'#','','',1,0,'F','0','0','tool:gen:query','#','admin','2026-01-16 13:11:14','',NULL,''),(1056,'鐢熸垚淇敼',116,2,'#','','',1,0,'F','0','0','tool:gen:edit','#','admin','2026-01-16 13:11:14','',NULL,''),(1057,'鐢熸垚鍒犻櫎',116,3,'#','','',1,0,'F','0','0','tool:gen:remove','#','admin','2026-01-16 13:11:14','',NULL,''),(1058,'瀵煎叆浠ｇ爜',116,4,'#','','',1,0,'F','0','0','tool:gen:import','#','admin','2026-01-16 13:11:14','',NULL,''),(1059,'棰勮浠ｇ爜',116,5,'#','','',1,0,'F','0','0','tool:gen:preview','#','admin','2026-01-16 13:11:14','',NULL,''),(1060,'鐢熸垚浠ｇ爜',116,6,'#','','',1,0,'F','0','0','tool:gen:code','#','admin','2026-01-16 13:11:14','',NULL,''),(2018,'AI鏅烘収澶ц剳',0,10,'ai',NULL,NULL,1,0,'M','0','0','','brain','admin','2026-01-23 21:24:42','',NULL,'AI绠楁硶涓績'),(2019,'璺屽€掓娴嬮┚椹惰埍',2018,1,'cockpit','ai/cockpit',NULL,1,0,'C','0','0','ai:cockpit:view','monitor','admin','2026-01-23 21:24:42','',NULL,'鍙鍖栧睍绀轰腑蹇?),(2020,'璺屽€掓娴嬬鐞?,2018,2,'fall','ai/fall/index',NULL,1,0,'C','0','0','ai:fall:list','list','admin','2026-01-23 21:24:42','',NULL,'璺屽€掓暟鎹鐞?),(2021,'绠楁硶鎬昏',2018,0,'dashboard','ai/dashboard/index',NULL,1,0,'C','0','0','ai:dashboard:view','dashboard','admin','2026-01-23 21:24:42','',NULL,'绠楁硶杩愯姒傝'),(2022,'寮傚父妫€娴嬮獙璇?,2018,3,'abnormal','ai/abnormal/index',NULL,1,0,'C','0','0','ai:abnormal:view','monitor','admin','2026-01-23 21:24:42','',NULL,'寮傚父楠岃瘉瀹為獙瀹?),(2023,'瓒嬪娍鍒嗘瀽棰勬祴',2018,4,'trend','ai/trend/index',NULL,1,0,'C','0','0','ai:trend:view','chart','admin','2026-01-23 22:22:08','',NULL,'鏁版嵁瓒嬪娍棰勬祴'),(2024,'缁煎悎椋庨櫓璇勪及',2018,5,'risk','ai/risk/index',NULL,1,0,'C','0','0','ai:risk:view','dashboard','admin','2026-01-23 22:31:02','',NULL,'澶氬洜瀛愰闄╄瘎浼?),(2025,'鏁版嵁璐ㄩ噺姒傝',2018,6,'quality','ai/dataQuality/index',NULL,1,0,'C','0','0','ai:quality:view','list','admin','2026-01-23 22:38:36','',NULL,'鏁版嵁瀹屾暣鎬ф娴?);
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notice`
--

DROP TABLE IF EXISTS `sys_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice` (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '鍏憡ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鍏憡鏍囬',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鍏憡绫诲瀷锛?閫氱煡 2鍏憡锛?,
  `notice_content` longblob COMMENT '鍏憡鍐呭',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鍏憡鐘舵€侊紙0姝ｅ父 1鍏抽棴锛?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '澶囨敞',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='閫氱煡鍏憡琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notice`
--

LOCK TABLES `sys_notice` WRITE;
/*!40000 ALTER TABLE `sys_notice` DISABLE KEYS */;
INSERT INTO `sys_notice` VALUES (1,'娓╅Θ鎻愰啋锛?018-07-01 鑻ヤ緷鏂扮増鏈彂甯冨暒','2',_binary '鏂扮増鏈唴瀹?,'0','admin','2026-01-16 13:11:15','',NULL,'绠＄悊鍛?),(2,'缁存姢閫氱煡锛?018-07-01 鑻ヤ緷绯荤粺鍑屾櫒缁存姢','1',_binary '缁存姢鍐呭','0','admin','2026-01-16 13:11:15','',NULL,'绠＄悊鍛?);
/*!40000 ALTER TABLE `sys_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_oper_log`
--

DROP TABLE IF EXISTS `sys_oper_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '鏃ュ織涓婚敭',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '妯″潡鏍囬',
  `business_type` int DEFAULT '0' COMMENT '涓氬姟绫诲瀷锛?鍏跺畠 1鏂板 2淇敼 3鍒犻櫎锛?,
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏂规硶鍚嶇О',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '璇锋眰鏂瑰紡',
  `operator_type` int DEFAULT '0' COMMENT '鎿嶄綔绫诲埆锛?鍏跺畠 1鍚庡彴鐢ㄦ埛 2鎵嬫満绔敤鎴凤級',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鎿嶄綔浜哄憳',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '閮ㄩ棬鍚嶇О',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '璇锋眰URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '涓绘満鍦板潃',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鎿嶄綔鍦扮偣',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '璇锋眰鍙傛暟',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '杩斿洖鍙傛暟',
  `status` int DEFAULT '0' COMMENT '鎿嶄綔鐘舵€侊紙0姝ｅ父 1寮傚父锛?,
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '閿欒娑堟伅',
  `oper_time` datetime DEFAULT NULL COMMENT '鎿嶄綔鏃堕棿',
  `cost_time` bigint DEFAULT '0' COMMENT '娑堣€楁椂闂?,
  PRIMARY KEY (`oper_id`),
  KEY `idx_sys_oper_log_bt` (`business_type`),
  KEY `idx_sys_oper_log_s` (`status`),
  KEY `idx_sys_oper_log_ot` (`oper_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='鎿嶄綔鏃ュ織璁板綍';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_oper_log`
--

LOCK TABLES `sys_oper_log` WRITE;
/*!40000 ALTER TABLE `sys_oper_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_oper_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_post`
--

DROP TABLE IF EXISTS `sys_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '宀椾綅ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '宀椾綅缂栫爜',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '宀椾綅鍚嶇О',
  `post_sort` int NOT NULL COMMENT '鏄剧ず椤哄簭',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '澶囨敞',
  PRIMARY KEY (`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='宀椾綅淇℃伅琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_post`
--

LOCK TABLES `sys_post` WRITE;
/*!40000 ALTER TABLE `sys_post` DISABLE KEYS */;
INSERT INTO `sys_post` VALUES (1,'ceo','钁ｄ簨闀?,1,'0','admin','2026-01-16 13:11:13','',NULL,''),(2,'se','椤圭洰缁忕悊',2,'0','admin','2026-01-16 13:11:13','',NULL,''),(3,'hr','浜哄姏璧勬簮',3,'0','admin','2026-01-16 13:11:13','',NULL,''),(4,'user','鏅€氬憳宸?,4,'0','admin','2026-01-16 13:11:13','',NULL,'');
/*!40000 ALTER TABLE `sys_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '瑙掕壊ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '瑙掕壊鍚嶇О',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '瑙掕壊鏉冮檺瀛楃涓?,
  `role_sort` int NOT NULL COMMENT '鏄剧ず椤哄簭',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '鏁版嵁鑼冨洿锛?锛氬叏閮ㄦ暟鎹潈闄?2锛氳嚜瀹氭暟鎹潈闄?3锛氭湰閮ㄩ棬鏁版嵁鏉冮檺 4锛氭湰閮ㄩ棬鍙婁互涓嬫暟鎹潈闄愶級',
  `menu_check_strictly` tinyint(1) DEFAULT '1' COMMENT '鑿滃崟鏍戦€夋嫨椤规槸鍚﹀叧鑱旀樉绀?,
  `dept_check_strictly` tinyint(1) DEFAULT '1' COMMENT '閮ㄩ棬鏍戦€夋嫨椤规槸鍚﹀叧鑱旀樉绀?,
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '瑙掕壊鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?浠ｈ〃瀛樺湪 2浠ｈ〃鍒犻櫎锛?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '澶囨敞',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='瑙掕壊淇℃伅琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1,'瓒呯骇绠＄悊鍛?,'admin',1,'1',1,1,'0','0','admin','2026-01-16 13:11:14','',NULL,'瓒呯骇绠＄悊鍛?),(2,'鏅€氳鑹?,'common',2,'2',1,1,'0','0','admin','2026-01-16 13:11:14','',NULL,'鏅€氳鑹?);
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_dept`
--

DROP TABLE IF EXISTS `sys_role_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_dept` (
  `role_id` bigint NOT NULL COMMENT '瑙掕壊ID',
  `dept_id` bigint NOT NULL COMMENT '閮ㄩ棬ID',
  PRIMARY KEY (`role_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='瑙掕壊鍜岄儴闂ㄥ叧鑱旇〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_dept`
--

LOCK TABLES `sys_role_dept` WRITE;
/*!40000 ALTER TABLE `sys_role_dept` DISABLE KEYS */;
INSERT INTO `sys_role_dept` VALUES (2,100),(2,101),(2,105);
/*!40000 ALTER TABLE `sys_role_dept` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_menu`
--

DROP TABLE IF EXISTS `sys_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '瑙掕壊ID',
  `menu_id` bigint NOT NULL COMMENT '鑿滃崟ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='瑙掕壊鍜岃彍鍗曞叧鑱旇〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_menu`
--

LOCK TABLES `sys_role_menu` WRITE;
/*!40000 ALTER TABLE `sys_role_menu` DISABLE KEYS */;
INSERT INTO `sys_role_menu` VALUES (2,1),(2,2),(2,3),(2,4),(2,100),(2,101),(2,102),(2,103),(2,104),(2,105),(2,106),(2,107),(2,108),(2,109),(2,110),(2,111),(2,112),(2,113),(2,114),(2,115),(2,116),(2,117),(2,500),(2,501),(2,1000),(2,1001),(2,1002),(2,1003),(2,1004),(2,1005),(2,1006),(2,1007),(2,1008),(2,1009),(2,1010),(2,1011),(2,1012),(2,1013),(2,1014),(2,1015),(2,1016),(2,1017),(2,1018),(2,1019),(2,1020),(2,1021),(2,1022),(2,1023),(2,1024),(2,1025),(2,1026),(2,1027),(2,1028),(2,1029),(2,1030),(2,1031),(2,1032),(2,1033),(2,1034),(2,1035),(2,1036),(2,1037),(2,1038),(2,1039),(2,1040),(2,1041),(2,1042),(2,1043),(2,1044),(2,1045),(2,1046),(2,1047),(2,1048),(2,1049),(2,1050),(2,1051),(2,1052),(2,1053),(2,1054),(2,1055),(2,1056),(2,1057),(2,1058),(2,1059),(2,1060);
/*!40000 ALTER TABLE `sys_role_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '鐢ㄦ埛ID',
  `dept_id` bigint DEFAULT NULL COMMENT '閮ㄩ棬ID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鐢ㄦ埛璐﹀彿',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鐢ㄦ埛鏄电О',
  `user_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '00' COMMENT '鐢ㄦ埛绫诲瀷锛?0绯荤粺鐢ㄦ埛锛?,
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鐢ㄦ埛閭',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鎵嬫満鍙风爜',
  `age` int DEFAULT NULL COMMENT '骞撮緞',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鐢ㄦ埛鎬у埆锛?鐢?1濂?2鏈煡锛?,
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '澶村儚鍦板潃',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '瀵嗙爜',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '甯愬彿鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?,
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?浠ｈ〃瀛樺湪 2浠ｈ〃鍒犻櫎锛?,
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏈€鍚庣櫥褰旾P',
  `login_date` datetime DEFAULT NULL COMMENT '鏈€鍚庣櫥褰曟椂闂?,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鍒涘缓鑰?,
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '鏇存柊鑰?,
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '澶囨敞',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='鐢ㄦ埛淇℃伅琛?;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,103,'admin','鑻ヤ緷','00','admin@qkyd.cn','15888888888',NULL,'1','','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0','127.0.0.1','2026-01-24 13:44:43','admin','2026-01-16 13:11:13','','2026-01-24 13:44:43','绠＄悊鍛?),(2,105,'ry','鑻ヤ緷','00','ops@qkyd.cn','15666666666',NULL,'1','','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0','127.0.0.1','2026-01-16 13:11:13','admin','2026-01-16 13:11:13','',NULL,'娴嬭瘯鍛?);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_post`
--

DROP TABLE IF EXISTS `sys_user_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_post` (
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `post_id` bigint NOT NULL COMMENT '宀椾綅ID',
  PRIMARY KEY (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='鐢ㄦ埛涓庡矖浣嶅叧鑱旇〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_post`
--

LOCK TABLES `sys_user_post` WRITE;
/*!40000 ALTER TABLE `sys_user_post` DISABLE KEYS */;
INSERT INTO `sys_user_post` VALUES (1,1),(2,2);
/*!40000 ALTER TABLE `sys_user_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `role_id` bigint NOT NULL COMMENT '瑙掕壊ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='鐢ㄦ埛鍜岃鑹插叧鑱旇〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES (1,1),(2,2);
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-29 19:38:08


-- ==============================
-- Remove code generator menu/perms (qkyd cleanup)
-- ==============================
DELETE FROM sys_role_menu WHERE menu_id IN (116,1055,1056,1057,1058,1059,1060);
DELETE FROM sys_menu WHERE menu_id IN (116,1055,1056,1057,1058,1059,1060);
