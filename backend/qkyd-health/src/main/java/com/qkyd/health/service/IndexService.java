package com.qkyd.health.service;

import com.alibaba.fastjson2.JSONObject;
import com.qkyd.health.domain.dto.AgeSexGroupCountDto;
import com.qkyd.health.domain.dto.RealTimeData;

import java.util.List;
import java.util.Map;

/**
 * 系统首页服务层接口
 */
public interface IndexService {
    // 年龄、性别分组数据
    AgeSexGroupCountDto getAgeSexGroupCount();

    // 获取异常数据
    JSONObject getExceptionData(String type, int pageNum);

    // 获取实时数据
    List<RealTimeData> realTimeData();

    // 仪表盘汇总指标（与异常列表/地图同口径）
    Map<String, Integer> dashboardSummary();

    // 查询实时位置数据
    List indexUserLocation();
}
