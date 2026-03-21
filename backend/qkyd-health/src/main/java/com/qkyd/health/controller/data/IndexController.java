package com.qkyd.health.controller.data;

import com.alibaba.fastjson2.JSONObject;
import com.qkyd.common.core.controller.BaseController;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.core.page.TableDataInfo;
import com.qkyd.health.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统首页相关接口
 */
@RestController
@RequestMapping("/index")
public class IndexController extends BaseController {
    @Autowired
    private IndexService indexService;

    /**
     * 年龄,性别分类数据
     *
     * @return
     */
    @GetMapping(value = "/ageSexGroupCount")
    public AjaxResult ageSexGroupCount() {
        return success(indexService.getAgeSexGroupCount());
    }

    /**
     * 异常数据
     *
     * @param type
     * @param pageNum
     * @return
     */
    @GetMapping(value = "/exception")
    public JSONObject exception(@RequestParam("type") String type, @RequestParam("pageNum") int pageNum) {
        return indexService.getExceptionData(type, pageNum);
    }

    @GetMapping(value = "/charge")
    public TableDataInfo charge() {
        return null;
    }

    //首页的实时数据
    @GetMapping(value = "/realTimeData")
    public AjaxResult realTimeData() {
        return success(indexService.realTimeData());
    }

    //  查询实时数据(所有用户的最后位置)
    @GetMapping(value = "/indexUserLocation")
    public AjaxResult indexUserLocation(@RequestParam("coordinateType") String coordinateType) {
        return success(indexService.indexUserLocation());
    }
}

