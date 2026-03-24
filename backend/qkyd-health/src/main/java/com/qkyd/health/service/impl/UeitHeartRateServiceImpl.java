package com.qkyd.health.service.impl;

import java.util.Date;
import java.util.List;
import com.qkyd.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qkyd.health.mapper.UeitHeartRateMapper;
import com.qkyd.health.domain.UeitHeartRate;
import com.qkyd.health.service.IUeitHeartRateService;

/**
 * 心率数据Service业务层处理
 *
 * @author z
 * @date 2024-01-05
 */
@Service
public class UeitHeartRateServiceImpl implements IUeitHeartRateService
{
    @Autowired
    private UeitHeartRateMapper ueitHeartRateMapper;

    /**
     * 查询心率数据
     *
     * @param id 心率数据主键
     * @return 心率数据
     */
    @Override
    public UeitHeartRate selectUeitHeartRateById(Long id)
    {
        return ueitHeartRateMapper.selectUeitHeartRateById(id);
    }

    /**
     * 查询心率数据列表
     *
     * @param ueitHeartRate 心率数据
     * @return 心率数据
     */
    @Override
    public List<UeitHeartRate> selectUeitHeartRateList(UeitHeartRate ueitHeartRate)
    {
        return ueitHeartRateMapper.selectUeitHeartRateList(ueitHeartRate);
    }

    /**
     * 新增心率数据
     *
     * @param ueitHeartRate 心率数据
     * @return 结果
     */
    @Override
    public int insertUeitHeartRate(UeitHeartRate ueitHeartRate)
    {
        if (ueitHeartRate.getCreateTime() == null) {
            ueitHeartRate.setCreateTime(DateUtils.getNowDate());
        }
        return ueitHeartRateMapper.insertUeitHeartRate(ueitHeartRate);
    }

    /**
     * 修改心率数据
     *
     * @param ueitHeartRate 心率数据
     * @return 结果
     */
    @Override
    public int updateUeitHeartRate(UeitHeartRate ueitHeartRate)
    {
        return ueitHeartRateMapper.updateUeitHeartRate(ueitHeartRate);
    }

    /**
     * 批量删除心率数据
     *
     * @param ids 需要删除的心率数据主键
     * @return 结果
     */
    @Override
    public int deleteUeitHeartRateByIds(Long[] ids)
    {
        return ueitHeartRateMapper.deleteUeitHeartRateByIds(ids);
    }

    /**
     * 删除心率数据信息
     *
     * @param id 心率数据主键
     * @return 结果
     */
    @Override
    public int deleteUeitHeartRateById(Long id)
    {
        return ueitHeartRateMapper.deleteUeitHeartRateById(id);
    }

    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @param userId
     * @param beginReadTime
     * @param endReadTime
     * @return
     */
    @Override
    public List<UeitHeartRate> getDataBoard(int userId, Date beginReadTime, Date endReadTime) {
        return ueitHeartRateMapper.getDataBoard(userId, beginReadTime, endReadTime);
    }
}
