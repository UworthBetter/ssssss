package com.qkyd.health.service;

import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.health.domain.dto.WatchBNDto;

/**
 * 5C-BNB02Y类型的手表数据Service接口
 */
public interface WatchBNService {
    /**
     * 处理手表推送数据
     *
     * @param watchBNDto
     * @return
     */
    public AjaxResult handle(WatchBNDto watchBNDto);
}

