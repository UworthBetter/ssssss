package com.qkyd.ai.mapper;

import com.qkyd.ai.model.entity.DataQualityRecord;
import org.apache.ibatis.annotations.*;
import java.util.Date;
import java.util.List;

@Mapper
public interface DataQualityRecordMapper {

    /**
     * Insert Data Quality Record
     */
    @Insert("INSERT INTO ai_data_quality_record (user_id, quality_score, missing_rate, outlier_rate, fill_strategy, suggestion, analysis_time, create_time) "
            +
            "VALUES (#{userId}, #{qualityScore}, #{missingRate}, #{outlierRate}, #{fillStrategy}, #{suggestion}, #{analysisTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DataQualityRecord record);

    /**
     * Select by ID
     */
    @Select("SELECT * FROM ai_data_quality_record WHERE id = #{id}")
    DataQualityRecord selectById(Long id);

    /**
     * Select by User ID
     */
    @Select("SELECT * FROM ai_data_quality_record WHERE user_id = #{userId}")
    List<DataQualityRecord> selectByUserId(Long userId);

    /**
     * Select by Time Range
     */
    @Select("SELECT * FROM ai_data_quality_record WHERE user_id = #{userId} AND analysis_time BETWEEN #{start} AND #{end}")
    List<DataQualityRecord> selectByTimeRange(@Param("userId") Long userId, @Param("start") Date start,
            @Param("end") Date end);

    /**
     * Delete by ID
     */
    @Delete("DELETE FROM ai_data_quality_record WHERE id = #{id}")
    int deleteById(Long id);
}
