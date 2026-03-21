package com.qkyd.ai.mapper;

import com.qkyd.ai.model.entity.TrendRecord;
import org.apache.ibatis.annotations.*;
import java.util.Date;
import java.util.List;

@Mapper
public interface TrendRecordMapper {

    /**
     * Insert Trend Record
     */
    @Insert("INSERT INTO ai_trend_record (user_id, metric_type, trend_direction, trend_strength, predicted_value, prediction_confidence, analysis_time, create_time) "
            +
            "VALUES (#{userId}, #{metricType}, #{trendDirection}, #{trendStrength}, #{predictedValue}, #{predictionConfidence}, #{analysisTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TrendRecord record);

    /**
     * Select by ID
     */
    @Select("SELECT * FROM ai_trend_record WHERE id = #{id}")
    TrendRecord selectById(Long id);

    /**
     * Select by User ID
     */
    @Select("SELECT * FROM ai_trend_record WHERE user_id = #{userId}")
    List<TrendRecord> selectByUserId(Long userId);

    /**
     * Select by Time Range
     */
    @Select("SELECT * FROM ai_trend_record WHERE user_id = #{userId} AND analysis_time BETWEEN #{start} AND #{end}")
    List<TrendRecord> selectByTimeRange(@Param("userId") Long userId, @Param("start") Date start,
            @Param("end") Date end);

    /**
     * Delete by ID
     */
    @Delete("DELETE FROM ai_trend_record WHERE id = #{id}")
    int deleteById(Long id);
}
