package com.ueit.health.service.impl;

import java.util.Date;
import java.util.List;

import com.ueit.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ueit.health.mapper.UeitStepsMapper;
import com.ueit.health.domain.UeitSteps;
import com.ueit.health.service.IUeitStepsService;

/**
 * 步数Service业务层处理
 *
 * @author z
 * @date 2024-01-05
 */
@Service
public class UeitStepsServiceImpl implements IUeitStepsService
{
    @Autowired
    private UeitStepsMapper ueitStepsMapper;

    /**
     * 查询步数
     *
     * @param id 步数主键
     * @return 步数
     */
    @Override
    public UeitSteps selectUeitStepsById(Long id)
    {
        return ueitStepsMapper.selectUeitStepsById(id);
    }

    /**
     * 检查该天是否已经存在步数数据
     *
     * @param ueitSteps
     * @return
     */
    @Override
    public UeitSteps selectUeitSteps(UeitSteps ueitSteps) {
        return ueitStepsMapper.selectUeitSteps(ueitSteps);
    }

    /**
     * 查询步数列表
     *
     * @param ueitSteps 步数
     * @return 步数
     */
    @Override
    public List<UeitSteps> selectUeitStepsList(UeitSteps ueitSteps)
    {
        return ueitStepsMapper.selectUeitStepsList(ueitSteps);
    }

    /**
     * 新增步数
     *
     * @param ueitSteps 步数
     * @return 结果
     */
    @Override
    public int insertUeitSteps(UeitSteps ueitSteps)
    {
        ueitSteps.setDateTime(DateUtils.getNowDate());
        return ueitStepsMapper.insertUeitSteps(ueitSteps);
    }

    /**
     * 修改步数
     *
     * @param ueitSteps 步数
     * @return 结果
     */
    @Override
    public int updateUeitSteps(UeitSteps ueitSteps)
    {
        return ueitStepsMapper.updateUeitSteps(ueitSteps);
    }

    /**
     * 批量删除步数
     *
     * @param ids 需要删除的步数主键
     * @return 结果
     */
    @Override
    public int deleteUeitStepsByIds(Long[] ids)
    {
        return ueitStepsMapper.deleteUeitStepsByIds(ids);
    }

    /**
     * 删除步数信息
     *
     * @param id 步数主键
     * @return 结果
     */
    @Override
    public int deleteUeitStepsById(Long id)
    {
        return ueitStepsMapper.deleteUeitStepsById(id);
    }

    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @param userId
     * @return
     */
    @Override
    public List<UeitSteps> getDataBoard(int userId, Date beginReadTime, Date endReadTime) {
        return ueitStepsMapper.getDataBoard(userId, beginReadTime, endReadTime);
    }
}

