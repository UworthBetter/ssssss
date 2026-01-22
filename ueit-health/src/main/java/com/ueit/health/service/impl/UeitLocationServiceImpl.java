package com.ueit.health.service.impl;

import java.util.Date;
import java.util.List;

import com.ueit.common.utils.DateUtils;
import com.ueit.health.domain.dto.RealTimeTracking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ueit.health.mapper.UeitLocationMapper;
import com.ueit.health.domain.UeitLocation;
import com.ueit.health.service.IUeitLocationService;

/**
 * 定位数据Service业务层处理
 *
 * @author z
 * @date 2024-01-05
 */
@Service
public class UeitLocationServiceImpl implements IUeitLocationService {
    @Autowired
    private UeitLocationMapper ueitLocationMapper;

    /**
     * 查询定位数据
     *
     * @param id 定位数据主键
     * @return 定位数据
     */
    @Override
    public UeitLocation selectUeitLocationById(Long id) {
        return ueitLocationMapper.selectUeitLocationById(id);
    }

    /**
     * 根据userId查询最新一条位置信息
     * @param userId
     * @return
     */
    @Override
    public UeitLocation selectUeitLocationByUserId(Long userId) {
        return ueitLocationMapper.selectUeitLocationByUserId(userId);
    }

    /**
     * 查询定位数据列表
     *
     * @param ueitLocation 定位数据
     * @return 定位数据
     */
    @Override
    public List<UeitLocation> selectUeitLocationList(UeitLocation ueitLocation) {
        return ueitLocationMapper.selectUeitLocationList(ueitLocation);
    }

    /**
     * 新增定位数据
     *
     * @param ueitLocation 定位数据
     * @return 结果
     */
    @Override
    public int insertUeitLocation(UeitLocation ueitLocation) {
        ueitLocation.setCreateTime(DateUtils.getNowDate());
        return ueitLocationMapper.insertUeitLocation(ueitLocation);
    }

    /**
     * 修改定位数据
     *
     * @param ueitLocation 定位数据
     * @return 结果
     */
    @Override
    public int updateUeitLocation(UeitLocation ueitLocation) {
        return ueitLocationMapper.updateUeitLocation(ueitLocation);
    }

    /**
     * 批量删除定位数据
     *
     * @param ids 需要删除的定位数据主键
     * @return 结果
     */
    @Override
    public int deleteUeitLocationByIds(Long[] ids) {
        return ueitLocationMapper.deleteUeitLocationByIds(ids);
    }

    /**
     * 删除定位数据信息
     *
     * @param id 定位数据主键
     * @return 结果
     */
    @Override
    public int deleteUeitLocationById(Long id) {
        return ueitLocationMapper.deleteUeitLocationById(id);
    }

    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     *
     * @param userId
     * @param beginReadTime
     * @param endReadTime
     * @return
     */
    @Override
    public List<UeitLocation> getDataBoard(int userId, Date beginReadTime, Date endReadTime) {
        return ueitLocationMapper.getDataBoard(userId, beginReadTime, endReadTime);
    }

    /**
     * 实时跟踪
     *
     * @param coordinateType
     * @param userId
     * @return
     */
    @Override
    public RealTimeTracking realTimeTracking(String coordinateType, int userId) {
        return ueitLocationMapper.realTimeTracking(coordinateType, userId);
    }

    /*
    历史轨迹
     */
    @Override
    public List<RealTimeTracking> pathList(String coordinateType, Date beginReadTime, Date endReadTime, int userId) {
        return ueitLocationMapper.pathList(coordinateType,beginReadTime,endReadTime,userId);
    }
}

