package com.ueit.health.controller;

import com.alibaba.fastjson2.JSON;
import com.ueit.common.core.controller.BaseController;
import com.ueit.common.core.domain.AjaxResult;
import com.ueit.common.utils.StringUtils;
import com.ueit.health.domain.dto.WatchBNDto;
import com.ueit.health.domain.dto.WatchDto;
import com.ueit.health.service.WatchBNService;
import com.ueit.health.service.WatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.ueit.health.utils.ParamsSignUtils.checkSign;


/**
 * 睡眠数据Controller
 *
 * @author ueit
 * @date 2023-01-04
 */
@RestController
@RequestMapping("/watch")
public class WatchController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(WatchController.class);
    @Autowired
    private WatchService watchService;
    @Autowired
    private WatchBNService watchBNService;
    /**
     * 导出手表数据列表
     */
    @PostMapping("/push")
    public AjaxResult push(@RequestBody Map<String,Object> object) {
        if(!checkSign(object)){
            throw new RuntimeException("签名校验失败");
        }
        String options = JSON.toJSONString(object);
        log.info("-5C-W4G09--7种手表推送数据：" + options);
        if (StringUtils.isEmpty(options)) {
            return error("5C-W4G09类型的手表推送数据为空！");
        }
        WatchDto watchDto = JSON.parseObject(options, WatchDto.class);

        // 根据类型存储不同数据
        AjaxResult result = watchService.handle(watchDto);
        return result;
    }

    /**
     * 863659048846942  5C-BNB02Y 导出手表数据列表
     */
    @PostMapping("/require")
    public AjaxResult require(@RequestBody Map<String,Object> object) {
        if(!checkSign(object)){
            throw new RuntimeException("签名校验失败");
        }
        String options = JSON.toJSONString(object);
        log.info("-5C-BNB02Y--6种手表推送数据：" + options);
        if (StringUtils.isEmpty(options)) {
            return error("5C-BNB02Y类型的手表推送数据为空！");
        }
        WatchBNDto watchBNDto = JSON.parseObject(options, WatchBNDto.class);

        // 根据类型存储不同数据
        AjaxResult result = watchBNService.handle(watchBNDto);
        return result;
    }

}
