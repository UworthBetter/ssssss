package com.qkyd.health.service.impl;

import java.util.Date;
import java.util.List;
import com.qkyd.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qkyd.health.mapper.UeitSpo2Mapper;
import com.qkyd.health.domain.UeitSpo2;
import com.qkyd.health.service.IUeitSpo2Service;

/**
 * 血氧数据Service业务层处理
 *
 * @author z
 * @date 2024-01-05
 */
@Service
public class UeitSpo2ServiceImpl implements IUeitSpo2Service
{
    @Autowired
    private UeitSpo2Mapper ueitSpo2Mapper;

    /**
     * 查询血氧数据
     *
     * @param id 血氧数据主键
     * @return 血氧数据
     */
    @Override
    public UeitSpo2 selectUeitSpo2ById(Long id)
    {
        return ueitSpo2Mapper.selectUeitSpo2ById(id);
    }

    /**
     * 查询血氧数据列表
     *
     * @param ueitSpo2 血氧数据
     * @return 血氧数据
     */
    @Override
    public List<UeitSpo2> selectUeitSpo2List(UeitSpo2 ueitSpo2)
    {
        return ueitSpo2Mapper.selectUeitSpo2List(ueitSpo2);
    }

    /**
     * 新增血氧数据
     *
     * @param ueitSpo2 血氧数据
     * @return 结果
     */
    @Override
    public int insertUeitSpo2(UeitSpo2 ueitSpo2)
    {
        ueitSpo2.setCreateTime(DateUtils.getNowDate());
        return ueitSpo2Mapper.insertUeitSpo2(ueitSpo2);
    }

    /**
     * 修改血氧数据
     *
     * @param ueitSpo2 血氧数据
     * @return 结果
     */
    @Override
    public int updateUeitSpo2(UeitSpo2 ueitSpo2)
    {
        return ueitSpo2Mapper.updateUeitSpo2(ueitSpo2);
    }

    /**
     * 批量删除血氧数据
     *
     * @param ids 需要删除的血氧数据主键
     * @return 结果
     */
    @Override
    public int deleteUeitSpo2ByIds(Long[] ids)
    {
        return ueitSpo2Mapper.deleteUeitSpo2ByIds(ids);
    }

    /**
     * 删除血氧数据信息
     *
     * @param id 血氧数据主键
     * @return 结果
     */
    @Override
    public int deleteUeitSpo2ById(Long id)
    {
        return ueitSpo2Mapper.deleteUeitSpo2ById(id);
    }

    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @param userId
     * @param beginReadTime
     * @param endReadTime
     * @return
     */
    @Override
    public List<UeitSpo2> getDataBoard(int userId, Date beginReadTime, Date endReadTime) {
        return ueitSpo2Mapper.getDataBoard(userId, beginReadTime, endReadTime);
    }
}

