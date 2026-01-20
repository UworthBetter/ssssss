package com.ueit.health.service;

import java.util.Date;
import java.util.List;
import com.ueit.health.domain.UeitHeartRate;

/**
 * 心率数据Service接口
 *
 * @author z
 * @date 2024-01-05
 */
public interface IUeitHeartRateService
{
    /**
     * 查询心率数据
     *
     * @param id 心率数据主键
     * @return 心率数据
     */
    public UeitHeartRate selectUeitHeartRateById(Long id);

    /**
     * 查询心率数据列表
     *
     * @param ueitHeartRate 心率数据
     * @return 心率数据集合
     */
    public List<UeitHeartRate> selectUeitHeartRateList(UeitHeartRate ueitHeartRate);

    /**
     * 新增心率数据
     *
     * @param ueitHeartRate 心率数据
     * @return 结果
     */
    public int insertUeitHeartRate(UeitHeartRate ueitHeartRate);

    /**
     * 修改心率数据
     *
     * @param ueitHeartRate 心率数据
     * @return 结果
     */
    public int updateUeitHeartRate(UeitHeartRate ueitHeartRate);

    /**
     * 批量删除心率数据
     *
     * @param ids 需要删除的心率数据主键集合
     * @return 结果
     */
    public int deleteUeitHeartRateByIds(Long[] ids);

    /**
     * 删除心率数据信息
     *
     * @param id 心率数据主键
     * @return 结果
     */
    public int deleteUeitHeartRateById(Long id);

    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @return
     */
    public List<UeitHeartRate> getDataBoard(int userId, Date beginReadTime, Date endReadTime);

}
