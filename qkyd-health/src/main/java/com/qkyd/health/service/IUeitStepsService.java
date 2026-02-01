package com.qkyd.health.service;

import java.util.Date;
import java.util.List;

import com.qkyd.health.domain.UeitHeartRate;
import com.qkyd.health.domain.UeitSteps;

/**
 * 步数Service接口
 *
 * @author z
 * @date 2024-01-05
 */
public interface IUeitStepsService
{
    /**
     * 查询步数
     *
     * @param id 步数主键
     * @return 步数
     */
    public UeitSteps selectUeitStepsById(Long id);

    /**
     * 检查该天是否已经存在步数数据
     *
     * @param ueitSteps
     * @return
     */
    public UeitSteps selectUeitSteps(UeitSteps ueitSteps);

    /**
     * 查询步数列表
     *
     * @param ueitSteps 步数
     * @return 步数集合
     */
    public List<UeitSteps> selectUeitStepsList(UeitSteps ueitSteps);

    /**
     * 新增步数
     *
     * @param ueitSteps 步数
     * @return 结果
     */
    public int insertUeitSteps(UeitSteps ueitSteps);

    /**
     * 修改步数
     *
     * @param ueitSteps 步数
     * @return 结果
     */
    public int updateUeitSteps(UeitSteps ueitSteps);

    /**
     * 批量删除步数
     *
     * @param ids 需要删除的步数主键集合
     * @return 结果
     */
    public int deleteUeitStepsByIds(Long[] ids);

    /**
     * 删除步数信息
     *
     * @param id 步数主键
     * @return 结果
     */
    public int deleteUeitStepsById(Long id);
    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @return
     */
    public List<UeitSteps> getDataBoard(int userId, Date beginReadTime, Date endReadTime);
}

