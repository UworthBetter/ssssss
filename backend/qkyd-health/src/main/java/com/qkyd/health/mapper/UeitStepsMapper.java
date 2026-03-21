package com.qkyd.health.mapper;

import java.util.Date;
import java.util.List;

import com.qkyd.health.domain.UeitHeartRate;
import com.qkyd.health.domain.UeitSteps;
import org.apache.ibatis.annotations.Param;

/**
 * 步数Mapper接口
 *
 * @author z
 * @date 2024-01-05
 */
public interface UeitStepsMapper
{
    /**
     * 查询步数
     *
     * @param id 步数主键
     * @return 步数
     */
    public UeitSteps selectUeitStepsById(Long id);

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
     * 删除步数
     *
     * @param id 步数主键
     * @return 结果
     */
    public int deleteUeitStepsById(Long id);

    /**
     * 批量删除步数
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUeitStepsByIds(Long[] ids);

    /**
     * 检查该天是否已经存在步数数据
     *
     * @param ueitSteps
     * @return
     */
    UeitSteps selectUeitSteps(UeitSteps ueitSteps);
    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @param userId
     * @return
     */
    List<UeitSteps> getDataBoard(@Param("userId") int userId, @Param("beginReadTime") Date beginReadTime, @Param("endReadTime") Date endReadTime);
}

