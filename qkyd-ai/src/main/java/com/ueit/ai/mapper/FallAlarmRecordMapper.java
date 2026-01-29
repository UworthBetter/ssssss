package com.ueit.ai.mapper;

import com.ueit.ai.model.entity.FallAlarmRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface FallAlarmRecordMapper {
    /**
     * Insert Fall Alarm Record
     */
    @Insert("INSERT INTO ai_fall_alarm_record (user_id, original_alarm_id, is_valid, validation_reason, acceleration_peak, has_removal_alert, recent_steps, validation_time, create_time) "
            +
            "VALUES (#{userId}, #{originalAlarmId}, #{isValid}, #{validationReason}, #{accelerationPeak}, #{hasRemovalAlert}, #{recentSteps}, #{validationTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertFallAlarmRecord(FallAlarmRecord record);
}
