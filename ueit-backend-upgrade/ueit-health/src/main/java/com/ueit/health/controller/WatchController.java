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
 * 鐫＄湢鏁版嵁Controller
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
     * 瀵煎嚭鎵嬭〃鏁版嵁鍒楄〃
     */
    @PostMapping("/push")
    public AjaxResult push(@RequestBody Map<String,Object> object) {
        if(!checkSign(object)){
            throw new RuntimeException("绛惧悕鏍￠獙澶辫触");
        }
        String options = JSON.toJSONString(object);
        log.info("-5C-W4G09--7绉嶆墜琛ㄦ帹閫佹暟鎹細" + options);
        if (StringUtils.isEmpty(options)) {
            return error("5C-W4G09绫诲瀷鐨勬墜琛ㄦ帹閫佹暟鎹负绌猴紒");
        }
        WatchDto watchDto = JSON.parseObject(options, WatchDto.class);

        // 鏍规嵁绫诲瀷瀛樺偍涓嶅悓鏁版嵁
        AjaxResult result = watchService.handle(watchDto);
        return result;
    }

    /**
     * 863659048846942  5C-BNB02Y 瀵煎嚭鎵嬭〃鏁版嵁鍒楄〃
     */
    @PostMapping("/require")
    public AjaxResult require(@RequestBody Map<String,Object> object) {
        if(!checkSign(object)){
            throw new RuntimeException("绛惧悕鏍￠獙澶辫触");
        }
        String options = JSON.toJSONString(object);
        log.info("-5C-BNB02Y--6绉嶆墜琛ㄦ帹閫佹暟鎹細" + options);
        if (StringUtils.isEmpty(options)) {
            return error("5C-BNB02Y绫诲瀷鐨勬墜琛ㄦ帹閫佹暟鎹负绌猴紒");
        }
        WatchBNDto watchBNDto = JSON.parseObject(options, WatchBNDto.class);

        // 鏍规嵁绫诲瀷瀛樺偍涓嶅悓鏁版嵁
        AjaxResult result = watchBNService.handle(watchBNDto);
        return result;
    }

}
