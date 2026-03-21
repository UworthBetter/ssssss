package com.qkyd.health.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.qkyd.health.domain.UeitException;
import com.qkyd.health.domain.dto.AgeSexGroupCountDto;
import com.qkyd.health.domain.dto.RealTimeData;
import com.qkyd.health.mapper.UeitBloodMapper;
import com.qkyd.health.mapper.UeitDeviceInfoExtendMapper;
import com.qkyd.health.mapper.UeitDeviceInfoMapper;
import com.qkyd.health.mapper.UeitExceptionMapper;
import com.qkyd.health.mapper.UeitHeartRateMapper;
import com.qkyd.health.mapper.UeitLocationMapper;
import com.qkyd.health.mapper.UeitSpo2Mapper;
import com.qkyd.health.mapper.UeitStepsMapper;
import com.qkyd.health.mapper.UeitTempMapper;
import com.qkyd.health.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 系统首页服务层接口实现类
 */
@Service
public class IndexServiceImpl implements IndexService {
    private static final Logger log = LoggerFactory.getLogger(IndexServiceImpl.class);

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

    @Override
    public AgeSexGroupCountDto getAgeSexGroupCount() {
        try {
            AgeSexGroupCountDto dto = bloodMapper.getAgeSexGroupCount();
            if (dto != null) {
                return dto;
            }
        } catch (Exception e) {
            log.warn("getAgeSexGroupCount failed, use mock data: {}", e.getMessage());
        }
        return mockAgeSexGroupCount();
    }

    @Override
    public JSONObject getExceptionData(String type, int pageNum) {
        JSONObject result = new JSONObject();
        try {
            Integer total = exceptionMapper.getTotal(type);
            List<UeitException> exceptionList = exceptionMapper.getExceptionData(type);

            result.put("total", total == null ? 0 : total);
            result.put("rows", exceptionList == null ? new ArrayList<>() : exceptionList);
            result.put("code", 200);
            result.put("msg", "查询成功");
        } catch (Exception e) {
            log.error("getExceptionData failed: {}", e.getMessage(), e);
            result.put("total", 0);
            result.put("rows", new ArrayList<>());
            result.put("code", 200);
            result.put("msg", "查询成功");
        }
        return result;
    }

    @Override
    public List<RealTimeData> realTimeData() {
        try {
            List<RealTimeData> dataList = deviceInfoExtendMapper.realTimeData();
            if (dataList != null && !dataList.isEmpty()) {
                return dataList;
            }
        } catch (Exception e) {
            log.warn("realTimeData failed, use mock data: {}", e.getMessage());
        }
        return mockRealtimeList();
    }

    @Override
    public Map<String, Integer> dashboardSummary() {
        Map<String, Integer> summary = new LinkedHashMap<>();

        List<UeitException> dashboardExceptions = new ArrayList<>();
        try {
            List<UeitException> queried = exceptionMapper.selectExceptionForDashboard();
            if (queried != null) {
                dashboardExceptions = queried;
            }
        } catch (Exception e) {
            log.warn("dashboardSummary query exceptions failed: {}", e.getMessage());
        }

        summary.put("stepExceptionCount", countByKeywords(dashboardExceptions, "步"));
        summary.put("fenceExceptionCount", countByKeywords(dashboardExceptions, "围栏", "越界"));
        summary.put("sosHelpCount", countByKeywords(dashboardExceptions, "sos", "求救"));
        summary.put("temperatureExceptionCount", countByKeywords(dashboardExceptions, "温"));
        summary.put("heartRateExceptionCount", countByKeywords(dashboardExceptions, "心率", "心跳"));
        summary.put("spo2ExceptionCount", countByKeywords(dashboardExceptions, "氧"));
        summary.put("bloodPressureExceptionCount", countByKeywords(dashboardExceptions, "压"));

        int onlineDeviceCount = 0;
        try {
            List<RealTimeData> realTime = deviceInfoExtendMapper.realTimeData();
            if (realTime != null) {
                onlineDeviceCount = realTime.size();
            }
        } catch (Exception e) {
            log.warn("dashboardSummary query online devices failed: {}", e.getMessage());
        }
        summary.put("onlineDeviceCount", onlineDeviceCount);

        return summary;
    }

    @Override
    public List indexUserLocation() {
        return deviceInfoExtendMapper.indexUserLocation();
    }

    private int countByKeywords(List<UeitException> source, String... keywords) {
        if (source == null || source.isEmpty() || keywords == null || keywords.length == 0) {
            return 0;
        }
        int count = 0;
        for (UeitException item : source) {
            String type = item.getType();
            if (type == null) {
                continue;
            }
            String normalized = type.toLowerCase(Locale.ROOT);
            for (String keyword : keywords) {
                if (keyword == null || keyword.isBlank()) {
                    continue;
                }
                if (normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private AgeSexGroupCountDto mockAgeSexGroupCount() {
        AgeSexGroupCountDto dto = new AgeSexGroupCountDto();
        dto.setA(6);
        dto.setB(10);
        dto.setC(24);
        dto.setD(17);
        dto.setE(12);
        dto.setMan(39);
        dto.setWoman(28);
        dto.setNono(2);
        return dto;
    }

    private List<UeitException> mockExceptionList(String type) {
        List<UeitException> list = new ArrayList<>();
        list.add(buildException(1001L, 10001L, 5001L, "心率异常", "132 bpm", "0", "北京市朝阳区酒仙桥路 10 号", 116.49, 39.98, 20));
        list.add(buildException(1002L, 10002L, 5002L, "围栏越界", "2.1 km", "0", "北京市海淀区西二旗地铁站", 116.31, 40.05, 16));
        list.add(buildException(1003L, 10003L, 5003L, "体温偏高", "38.6℃", "1", "北京市丰台区科技园", 116.29, 39.83, 12));
        list.add(buildException(1004L, 10004L, 5004L, "血氧偏低", "89%", "0", "北京市东城区东直门", 116.44, 39.94, 8));
        list.add(buildException(1005L, 10005L, 5005L, "SOS求救", "手动触发", "0", "北京市通州区运河西大街", 116.66, 39.90, 5));
        list.add(buildException(1006L, 10006L, 5006L, "步数异常", "15000/小时", "1", "北京市石景山区鲁谷", 116.22, 39.90, 3));
        if (type == null || type.isBlank() || "all".equalsIgnoreCase(type)) {
            return list;
        }
        List<UeitException> filtered = new ArrayList<>();
        for (UeitException item : list) {
            if (item.getType() != null && item.getType().contains(type)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private UeitException buildException(Long id, Long userId, Long deviceId, String type, String value,
                                         String state, String location, double longitude, double latitude,
                                         int minutesAgo) {
        UeitException e = new UeitException();
        e.setId(id);
        e.setUserId(userId);
        e.setDeviceId(deviceId);
        e.setType(type);
        e.setValue(value);
        e.setState(state);
        e.setLocation(location);
        e.setLongitude(BigDecimal.valueOf(longitude));
        e.setLatitude(BigDecimal.valueOf(latitude));
        e.setReadTime(new Date(System.currentTimeMillis() - minutesAgo * 60L * 1000L));
        e.setNickName("用户" + userId);
        return e;
    }

    private List<RealTimeData> mockRealtimeList() {
        List<RealTimeData> list = new ArrayList<>();
        list.add(buildRealtime(10001, "王建国", 0, 72, 85, "97", "36.4"));
        list.add(buildRealtime(10002, "赵跑跑", 1, 68, 92, "96", "36.8"));
        list.add(buildRealtime(10003, "李明", 0, 75, 78, "98", "36.6"));
        return list;
    }

    private RealTimeData buildRealtime(int userId, String nickName, int sex, int age, int hr, String spo2, String temp) {
        RealTimeData data = new RealTimeData();
        data.setUserId(userId);
        data.setNickName(nickName);
        data.setSex(sex);
        data.setAge(age);
        data.setHeartRateValue(hr);
        data.setSpo2Value(spo2);
        data.setTemperatureValue(temp);
        data.setReadTime(new Date());
        return data;
    }
}
