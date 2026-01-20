package com.ueit.health.mapper;

import java.util.Date;
import java.util.List;

import com.ueit.health.domain.UeitHeartRate;
import com.ueit.health.domain.UeitLocation;
import com.ueit.health.domain.dto.RealTimeTracking;
import org.apache.ibatis.annotations.Param;

/**
 * 定位数据Mapper接口
 *
 * @author z
 * @date 2024-01-05
 */
public interface UeitLocationMapper
{
    /**
     * 查询定位数据
     *
     * @param id 定位数据主键
     * @return 定位数据
     */
    public UeitLocation selectUeitLocationById(Long id);
    //根据userId查询最新一条位置信息
    public UeitLocation selectUeitLocationByUserId(Long userId);
    /**
     * 查询定位数据列表
     *
     * @param ueitLocation 定位数据
     * @return 定位数据集合
     */
    public List<UeitLocation> selectUeitLocationList(UeitLocation ueitLocation);

    /**
     * 新增定位数据
     *
     * @param ueitLocation 定位数据
     * @return 结果
     */
    public int insertUeitLocation(UeitLocation ueitLocation);

    /**
     * 修改定位数据
     *
     * @param ueitLocation 定位数据
     * @return 结果
     */
    public int updateUeitLocation(UeitLocation ueitLocation);

    /**
     * 删除定位数据
     *
     * @param id 定位数据主键
     * @return 结果
     */
    public int deleteUeitLocationById(Long id);

    /**
     * 批量删除定位数据
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUeitLocationByIds(Long[] ids);
    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @param userId
     * @param beginReadTime
     * @param endReadTime
     * @return
     */
    List<UeitLocation> getDataBoard(@Param("userId") int userId, @Param("beginReadTime") Date beginReadTime, @Param("endReadTime") Date endReadTime);

    /**
     * 实时跟踪
     * @param coordinateType
     * @param userId
     * @return
     */
    RealTimeTracking realTimeTracking(@Param("coordinateType") String coordinateType, @Param("userId") int userId);

    /**
     * 历史轨迹
     * @param coordinateType
     * @param beginReadTime
     * @param endReadTime
     * @param userId
     * @return
     */
    List<RealTimeTracking> pathList(@Param("coordinateType")String coordinateType, @Param("beginReadTime") Date beginReadTime, @Param("endReadTime") Date endReadTime, @Param("userId") int userId);
}
