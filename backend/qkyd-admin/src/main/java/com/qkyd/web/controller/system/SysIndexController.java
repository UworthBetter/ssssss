package com.qkyd.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.qkyd.common.config.QkydConfig;
import com.qkyd.common.utils.StringUtils;

/**
 * 棣栭〉
 *
 * @author qkyd
 */
@RestController
public class SysIndexController
{
    /** 绯荤粺鍩虹閰嶇疆 */
    @Autowired
    private QkydConfig qkydConfig;

    /**
     * 璁块棶棣栭〉锛屾彁绀鸿
     */
    @RequestMapping("/")
    public String index()
    {
        return StringUtils.format("欢迎使用{}，当前版本：v{}，请通过前端地址访问。", qkydConfig.getName(), qkydConfig.getVersion());
    }
}



