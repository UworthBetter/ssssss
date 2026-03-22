package com.qkyd.ai.mapper;

import com.qkyd.ai.model.dto.EventInsightSnapshotSummaryDTO;
import com.qkyd.ai.model.entity.EventInsightSnapshotRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EventInsightSnapshotMapper {

    @Select("SELECT id, event_id, summary, risk_level, risk_score, orchestrator_version, "
            + "fallback_used, payload_json, generated_at, create_time "
            + "FROM ai_event_insight_snapshot "
            + "WHERE event_id = #{eventId} "
            + "ORDER BY generated_at DESC, id DESC LIMIT 1")
    EventInsightSnapshotRecord selectLatestByEventId(Long eventId);

    @Select("SELECT id, event_id, summary, risk_level, risk_score, orchestrator_version, "
            + "fallback_used, generated_at, create_time "
            + "FROM ai_event_insight_snapshot "
            + "WHERE event_id = #{eventId} "
            + "ORDER BY generated_at DESC, id DESC LIMIT #{limit}")
    List<EventInsightSnapshotSummaryDTO> selectRecentByEventId(
            @Param("eventId") Long eventId,
            @Param("limit") int limit);

    @Select("SELECT id, event_id, summary, risk_level, risk_score, orchestrator_version, "
            + "fallback_used, payload_json, generated_at, create_time "
            + "FROM ai_event_insight_snapshot "
            + "WHERE event_id = #{eventId} AND id = #{snapshotId} LIMIT 1")
    EventInsightSnapshotRecord selectByIdAndEventId(
            @Param("eventId") Long eventId,
            @Param("snapshotId") Long snapshotId);

    @Insert("INSERT INTO ai_event_insight_snapshot (event_id, summary, risk_level, risk_score, "
            + "orchestrator_version, fallback_used, payload_json, generated_at, create_time) "
            + "VALUES (#{eventId}, #{summary}, #{riskLevel}, #{riskScore}, "
            + "#{orchestratorVersion}, #{fallbackUsed}, #{payloadJson}, "
            + "#{generatedAt}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EventInsightSnapshotRecord record);
}
