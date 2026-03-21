package com.qkyd.health.mapper;

import com.qkyd.health.domain.AiHealthRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI健康分析记录Mapper接口
 *
 * @author ueit
 * @date 2026-01-29
 */
@Mapper
public interface AiHealthRecordMapper {

    /**
     * 查询AI健康分析记录
     *
     * @param id 主键ID
     * @return AI健康分析记录
     */
    AiHealthRecord selectAiHealthRecordById(@Param("id") Long id);

    /**
     * 查询AI健康分析记录列表
     *
     * @param aiHealthRecord 查询条件
     * @return AI健康分析记录集合
     */
    List<AiHealthRecord> selectAiHealthRecordList(AiHealthRecord aiHealthRecord);

    /**
     * 根据设备ID查询最新记录
     *
     * @param deviceId 设备ID
     * @return AI健康分析记录
     */
    AiHealthRecord selectLatestByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 新增AI健康分析记录
     *
     * @param aiHealthRecord AI健康分析记录
     * @return 影响行数
     */
    int insertAiHealthRecord(AiHealthRecord aiHealthRecord);

    /**
     * 修改AI健康分析记录
     *
     * @param aiHealthRecord AI健康分析记录
     * @return 影响行数
     */
    int updateAiHealthRecord(AiHealthRecord aiHealthRecord);

    /**
     * 删除AI健康分析记录
     *
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteAiHealthRecordById(@Param("id") Long id);

    /**
     * 批量删除AI健康分析记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 影响行数
     */
    int deleteAiHealthRecordByIds(@Param("ids") Long[] ids);
}
