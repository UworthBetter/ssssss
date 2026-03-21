package com.qkyd.ai.mapper;

import com.qkyd.ai.model.entity.RiskScoreRecord;
import org.apache.ibatis.annotations.*;
import java.util.Date;
import java.util.List;

@Mapper
public interface RiskScoreRecordMapper {

    /**
     * Insert Risk Score Record
     */
    @Insert("INSERT INTO ai_risk_score_record (user_id, risk_score, risk_level, factors, llm_report, sub_scores, analysis_time, create_time) "
            +
            "VALUES (#{userId}, #{riskScore}, #{riskLevel}, #{factors}, #{llmReport}, #{subScores}, #{analysisTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RiskScoreRecord record);

    /**
     * Select by ID
     */
    @Select("SELECT * FROM ai_risk_score_record WHERE id = #{id}")
    RiskScoreRecord selectById(Long id);

    /**
     * Select by User ID
     */
    @Select("SELECT * FROM ai_risk_score_record WHERE user_id = #{userId}")
    List<RiskScoreRecord> selectByUserId(Long userId);

    /**
     * Select by Time Range
     */
    @Select("SELECT * FROM ai_risk_score_record WHERE user_id = #{userId} AND analysis_time BETWEEN #{start} AND #{end}")
    List<RiskScoreRecord> selectByTimeRange(@Param("userId") Long userId, @Param("start") Date start,
            @Param("end") Date end);

    /**
     * Delete by ID
     */
    @Delete("DELETE FROM ai_risk_score_record WHERE id = #{id}")
    int deleteById(Long id);
}
