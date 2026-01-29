package com.ueit.health.mapper;

import java.util.Date;
import java.util.List;

import com.ueit.health.domain.UeitHeartRate;
import com.ueit.health.domain.UeitTemp;
import org.apache.ibatis.annotations.Param;

/**
 * 体温数据Mapper接口
 *
 * @author z
 * @date 2024-01-05
 */
public interface UeitTempMapper
{
    /**
     * 查询体温数据
     *
     * @param id 体温数据主键
     * @return 体温数据
     */
    public UeitTemp selectUeitTempById(Long id);

    /**
     * 查询体温数据列表
     *
     * @param ueitTemp 体温数据
     * @return 体温数据集合
     */
    public List<UeitTemp> selectUeitTempList(UeitTemp ueitTemp);

    /**
     * 新增体温数据
     *
     * @param ueitTemp 体温数据
     * @return 结果
     */
    public int insertUeitTemp(UeitTemp ueitTemp);

    /**
     * 修改体温数据
     *
     * @param ueitTemp 体温数据
     * @return 结果
     */
    public int updateUeitTemp(UeitTemp ueitTemp);

    /**
     * 删除体温数据
     *
     * @param id 体温数据主键
     * @return 结果
     */
    public int deleteUeitTempById(Long id);

    /**
     * 批量删除体温数据
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUeitTempByIds(Long[] ids);
    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @param userId
     * @param beginReadTime
     * @param endReadTime
     * @return
     */
    List<UeitTemp> getDataBoard(@Param("userId") int userId, @Param("beginReadTime") Date beginReadTime, @Param("endReadTime") Date endReadTime);
}

