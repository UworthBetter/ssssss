package com.ueit.health.service;

import com.alibaba.fastjson2.JSONObject;
import com.ueit.common.core.page.TableDataInfo;
import com.ueit.health.domain.dto.AgeSexGroupCountDto;
import com.ueit.health.domain.dto.RealTimeData;

import java.util.List;

/**
 * 系统首页服务层接口
 */
public interface IndexService {
    // 年龄,性别分类数据
    AgeSexGroupCountDto getAgeSexGroupCount();

    //获取异常数据
    JSONObject getExceptionData(String type, int pageNum);
    //获取实时数据
    List<RealTimeData> realTimeData();
    //查询实时数据
    List indexUserLocation();
}

