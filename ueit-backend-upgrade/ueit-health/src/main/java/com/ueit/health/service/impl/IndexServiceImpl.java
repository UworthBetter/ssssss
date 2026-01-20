package com.ueit.health.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.ueit.common.core.page.TableDataInfo;
import com.ueit.health.domain.UeitException;
import com.ueit.health.domain.dto.AgeSexGroupCountDto;
import com.ueit.health.domain.dto.RealTimeData;
import com.ueit.health.mapper.*;
import com.ueit.health.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 绯荤粺棣栭〉鏈嶅姟灞傛帴鍙ｅ疄鐜扮被
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

    //骞撮緞,鎬у埆鍒嗙被鏁版嵁
    @Override
    public AgeSexGroupCountDto getAgeSexGroupCount() {
        return bloodMapper.getAgeSexGroupCount();
    }

    //鏍规嵁鍋ュ悍鏁版嵁绫诲瀷鑾峰彇寮傚父鏁版嵁
    @Override
    public JSONObject getExceptionData(String type, int pageNum) {
        JSONObject result = new JSONObject();
        try {
            Integer total = exceptionMapper.getTotal(type);
            // 杩斿洖淇℃伅
            List<UeitException> exceptionList = exceptionMapper.getExceptionData(type);
            result.put("total", total);
            result.put("rows", exceptionList);
            result.put("code", 200);
            result.put("msg", "鏌ヨ鎴愬姛");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //瀹炴椂鏁版嵁
    @Override
    public List<RealTimeData> realTimeData() {
        return deviceInfoExtendMapper.realTimeData();
    }
    //鏌ヨ瀹炴椂鏁版嵁
    @Override
    public List indexUserLocation() {
        return deviceInfoExtendMapper.indexUserLocation();
    }
}
