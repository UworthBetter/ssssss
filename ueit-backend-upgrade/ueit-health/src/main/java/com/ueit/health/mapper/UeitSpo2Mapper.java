package com.ueit.health.mapper;

import java.util.Date;
import java.util.List;

import com.ueit.health.domain.UeitHeartRate;
import com.ueit.health.domain.UeitSpo2;
import org.apache.ibatis.annotations.Param;

/**
 * 血氧数据Mapper接口
 *
 * @author z
 * @date 2024-01-05
 */
public interface UeitSpo2Mapper
{
    /**
     * 查询血氧数据
     *
     * @param id 血氧数据主键
     * @return 血氧数据
     */
    public UeitSpo2 selectUeitSpo2ById(Long id);

    /**
     * 查询血氧数据列表
     *
     * @param ueitSpo2 血氧数据
     * @return 血氧数据集合
     */
    public List<UeitSpo2> selectUeitSpo2List(UeitSpo2 ueitSpo2);

    /**
     * 新增血氧数据
     *
     * @param ueitSpo2 血氧数据
     * @return 结果
     */
    public int insertUeitSpo2(UeitSpo2 ueitSpo2);

    /**
     * 修改血氧数据
     *
     * @param ueitSpo2 血氧数据
     * @return 结果
     */
    public int updateUeitSpo2(UeitSpo2 ueitSpo2);

    /**
     * 删除血氧数据
     *
     * @param id 血氧数据主键
     * @return 结果
     */
    public int deleteUeitSpo2ById(Long id);

    /**
     * 批量删除血氧数据
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUeitSpo2ByIds(Long[] ids);
    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @param userId
     * @param beginReadTime
     * @param endReadTime
     * @return
     */
    List<UeitSpo2> getDataBoard(@Param("userId") int userId, @Param("beginReadTime") Date beginReadTime, @Param("endReadTime") Date endReadTime);
}
