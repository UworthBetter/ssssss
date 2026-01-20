package com.ueit.health.service.impl;

import java.util.Date;
import java.util.List;
import com.ueit.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ueit.health.mapper.UeitBloodMapper;
import com.ueit.health.domain.UeitBlood;
import com.ueit.health.service.IUeitBloodService;

/**
 * 血压数据Service业务层处理
 *
 * @author z
 * @date 2024-01-05
 */
@Service
public class UeitBloodServiceImpl implements IUeitBloodService
{
    @Autowired
    private UeitBloodMapper ueitBloodMapper;

    /**
     * 查询血压数据
     *
     * @param id 血压数据主键
     * @return 血压数据
     */
    @Override
    public UeitBlood selectUeitBloodById(Long id)
    {
        return ueitBloodMapper.selectUeitBloodById(id);
    }

    /**
     * 查询血压数据列表
     *
     * @param ueitBlood 血压数据
     * @return 血压数据
     */
    @Override
    public List<UeitBlood> selectUeitBloodList(UeitBlood ueitBlood)
    {
        return ueitBloodMapper.selectUeitBloodList(ueitBlood);
    }

    /**
     * 新增血压数据
     *
     * @param ueitBlood 血压数据
     * @return 结果
     */
    @Override
    public int insertUeitBlood(UeitBlood ueitBlood)
    {
        ueitBlood.setCreateTime(DateUtils.getNowDate());
        return ueitBloodMapper.insertUeitBlood(ueitBlood);
    }

    /**
     * 修改血压数据
     *
     * @param ueitBlood 血压数据
     * @return 结果
     */
    @Override
    public int updateUeitBlood(UeitBlood ueitBlood)
    {
        return ueitBloodMapper.updateUeitBlood(ueitBlood);
    }

    /**
     * 批量删除血压数据
     *
     * @param ids 需要删除的血压数据主键
     * @return 结果
     */
    @Override
    public int deleteUeitBloodByIds(Long[] ids)
    {
        return ueitBloodMapper.deleteUeitBloodByIds(ids);
    }

    /**
     * 删除血压数据信息
     *
     * @param id 血压数据主键
     * @return 结果
     */
    @Override
    public int deleteUeitBloodById(Long id)
    {
        return ueitBloodMapper.deleteUeitBloodById(id);
    }

    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @param userId
     * @param beginReadTime
     * @param endReadTime
     * @return
     */
    @Override
    public List<UeitBlood> getDataBoard(int userId, Date beginReadTime, Date endReadTime) {
        return ueitBloodMapper.getDataBoard(userId, beginReadTime, endReadTime);
    }
}
