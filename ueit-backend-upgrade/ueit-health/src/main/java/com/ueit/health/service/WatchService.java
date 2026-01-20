package com.ueit.health.service;

import com.ueit.common.core.domain.AjaxResult;
import com.ueit.health.domain.dto.WatchDto;

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
