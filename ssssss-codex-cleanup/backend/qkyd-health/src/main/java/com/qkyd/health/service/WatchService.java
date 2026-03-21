package com.qkyd.health.service;

import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.health.domain.dto.WatchDto;

/**
 * 手表数据Service接口
 */
public interface WatchService {
    /**
     * 处理手表推送数据
     *
     * @param watchDto
     * @return
     */
    public AjaxResult handle(WatchDto watchDto);
}

