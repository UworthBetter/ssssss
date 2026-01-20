package com.ueit.health.service;

import java.util.Date;
import java.util.List;
import com.ueit.health.domain.UeitBlood;
import com.ueit.health.domain.UeitHeartRate;

/**
 * 血压数据Service接口
 *
 * @author z
 * @date 2024-01-05
 */
public interface IUeitBloodService
{
    /**
     * 查询血压数据
     *
     * @param id 血压数据主键
     * @return 血压数据
     */
    public UeitBlood selectUeitBloodById(Long id);

    /**
     * 查询血压数据列表
     *
     * @param ueitBlood 血压数据
     * @return 血压数据集合
     */
    public List<UeitBlood> selectUeitBloodList(UeitBlood ueitBlood);

    /**
     * 新增血压数据
     *
     * @param ueitBlood 血压数据
     * @return 结果
     */
    public int insertUeitBlood(UeitBlood ueitBlood);

    /**
     * 修改血压数据
     *
     * @param ueitBlood 血压数据
     * @return 结果
     */
    public int updateUeitBlood(UeitBlood ueitBlood);

    /**
     * 批量删除血压数据
     *
     * @param ids 需要删除的血压数据主键集合
     * @return 结果
     */
    public int deleteUeitBloodByIds(Long[] ids);

    /**
     * 删除血压数据信息
     *
     * @param id 血压数据主键
     * @return 结果
     */
    public int deleteUeitBloodById(Long id);
    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @return
     */
    public List<UeitBlood> getDataBoard(int userId, Date beginReadTime, Date endReadTime);
}
