package com.ueit.health.mapper;

import java.util.Date;
import java.util.List;
import com.ueit.health.domain.UeitBlood;
import com.ueit.health.domain.UeitHeartRate;
import com.ueit.health.domain.dto.AgeSexGroupCountDto;
import org.apache.ibatis.annotations.Param;

/**
 * 血压数据Mapper接口
 *
 * @author z
 * @date 2024-01-05
 */
public interface UeitBloodMapper
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
     * 删除血压数据
     *
     * @param id 血压数据主键
     * @return 结果
     */
    public int deleteUeitBloodById(Long id);

    /**
     * 批量删除血压数据
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUeitBloodByIds(Long[] ids);
    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @param userId
     * @param beginReadTime
     * @param endReadTime
     * @return
     */
    List<UeitBlood> getDataBoard(@Param("userId") int userId, @Param("beginReadTime") Date beginReadTime, @Param("endReadTime") Date endReadTime);
    // 年龄,性别分类数据
    public AgeSexGroupCountDto getAgeSexGroupCount();
}

