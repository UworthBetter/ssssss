package com.qkyd.ai.mapper;

import com.qkyd.ai.model.entity.AbnormalRecord;
import org.apache.ibatis.annotations.*;
import java.util.Date;
import java.util.List;

@Mapper
public interface AbnormalRecordMapper {

        /**
         * Insert Abnormal Record
         */
        @Insert("INSERT INTO ai_abnormal_record (user_id, device_id, metric_type, abnormal_value, normal_range, abnormal_type, risk_level, detection_method, detected_time, create_time) "
                        +
                        "VALUES (#{userId}, #{deviceId}, #{metricType}, #{abnormalValue}, #{normalRange}, #{abnormalType}, #{riskLevel}, #{detectionMethod}, #{detectedTime}, #{createTime})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(AbnormalRecord record);

        /**
         * Select by ID
         */
        @Select("SELECT * FROM ai_abnormal_record WHERE id = #{id}")
        AbnormalRecord selectById(Long id);

        /**
         * Select by User ID
         */
        @Select("SELECT * FROM ai_abnormal_record WHERE user_id = #{userId}")
        List<AbnormalRecord> selectByUserId(Long userId);

        /**
         * Select by Time Range
         */
        @Select("SELECT * FROM ai_abnormal_record WHERE user_id = #{userId} AND detected_time BETWEEN #{start} AND #{end}")
        List<AbnormalRecord> selectByTimeRange(@Param("userId") Long userId, @Param("start") Date start,
                        @Param("end") Date end);

        /**
         * Select by Metric Type
         */
        @Select("SELECT * FROM ai_abnormal_record WHERE user_id = #{userId} AND metric_type = #{metricType}")
        List<AbnormalRecord> selectByMetricType(@Param("userId") Long userId, @Param("metricType") String metricType);

        /**
         * Select Recent Records
         */
        @Select("SELECT * FROM ai_abnormal_record ORDER BY detected_time DESC LIMIT #{limit}")
        List<AbnormalRecord> selectRecent(int limit);

        /**
         * Delete by ID
         */
        @Delete("DELETE FROM ai_abnormal_record WHERE id = #{id}")
        int deleteById(Long id);
}
