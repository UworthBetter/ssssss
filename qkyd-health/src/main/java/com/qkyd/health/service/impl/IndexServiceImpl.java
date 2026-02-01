package com.qkyd.health.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.qkyd.common.core.page.TableDataInfo;
import com.qkyd.health.domain.UeitException;
import com.qkyd.health.domain.dto.AgeSexGroupCountDto;
import com.qkyd.health.domain.dto.RealTimeData;
import com.qkyd.health.mapper.*;
import com.qkyd.health.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统首页服务层接口实现类
 */
@Service
public class IndexServiceImpl implements IndexService {
    private static final Logger log = LoggerFactory.getLogger(DataServiceImpl.class);
    @Autowired
    private UeitBloodMapper bloodMapper;
    @Autowired
    private UeitHeartRateMapper heartRateMapper;
    @Autowired
    private UeitSpo2Mapper spo2Mapper;
    @Autowired
    private UeitTempMapper tempMapper;
    @Autowired
    private UeitLocationMapper locationMapper;
    @Autowired
    private UeitStepsMapper stepsMapper;
    @Autowired
    private UeitExceptionMapper exceptionMapper;
    @Autowired
    private UeitDeviceInfoMapper ueitDeviceInfoMapper;
    @Autowired
    private UeitDeviceInfoExtendMapper deviceInfoExtendMapper;

    //年龄,性别分类数据
    @Override
    public AgeSexGroupCountDto getAgeSexGroupCount() {
        return bloodMapper.getAgeSexGroupCount();
    }

    //根据健康数据类型获取异常数据
    @Override
    public JSONObject getExceptionData(String type, int pageNum) {
        JSONObject result = new JSONObject();
        try {
            Integer total = exceptionMapper.getTotal(type);
            // 返回信息
            List<UeitException> exceptionList = exceptionMapper.getExceptionData(type);
            result.put("total", total);
            result.put("rows", exceptionList);
            result.put("code", 200);
            result.put("msg", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //实时数据
    @Override
    public List<RealTimeData> realTimeData() {
        return deviceInfoExtendMapper.realTimeData();
    }
    //查询实时数据
    @Override
    public List indexUserLocation() {
        return deviceInfoExtendMapper.indexUserLocation();
    }
}

