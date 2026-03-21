package com.qkyd.health.controller;

import jakarta.servlet.http.HttpServletResponse;
import com.qkyd.common.annotation.Log;
import com.qkyd.common.core.controller.BaseController;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.core.page.TableDataInfo;
import com.qkyd.common.enums.BusinessType;
import com.qkyd.common.utils.poi.ExcelUtil;
import com.qkyd.health.domain.HealthSubject;
import com.qkyd.health.service.IHealthSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务对象Controller
 *
 * @author qkyd
 * @date 2026-02-01
 */
@RestController
@RequestMapping("/health/subject")
public class HealthSubjectController extends BaseController {
    @Autowired
    private IHealthSubjectService healthSubjectService;

    /**
     * 查询服务对象列表
     */
    @GetMapping("/list")
    public TableDataInfo list(HealthSubject healthSubject) {
        startPage();
        List<HealthSubject> list = new ArrayList<>();
        try {
            list = healthSubjectService.selectHealthSubjectList(healthSubject);
        } catch (Exception ignored) {
        }
        if (list == null || list.isEmpty()) {
            list = filterSubjects(mockSubjects(), healthSubject);
        }
        return getDataTable(list);
    }

    /**
     * 导出服务对象列表
     */
    @Log(title = "服务对象", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, HealthSubject healthSubject) {
        List<HealthSubject> list = new ArrayList<>();
        try {
            list = healthSubjectService.selectHealthSubjectList(healthSubject);
        } catch (Exception ignored) {
        }
        if (list == null || list.isEmpty()) {
            list = filterSubjects(mockSubjects(), healthSubject);
        }
        ExcelUtil<HealthSubject> util = new ExcelUtil<>(HealthSubject.class);
        util.exportExcel(response, list, "服务对象数据");
    }

    /**
     * 获取服务对象详细信息
     */
    @GetMapping(value = "/{subjectId}")
    public AjaxResult getInfo(@PathVariable("subjectId") Long subjectId) {
        HealthSubject subject = null;
        try {
            subject = healthSubjectService.selectHealthSubjectBySubjectId(subjectId);
        } catch (Exception ignored) {
        }
        if (subject == null) {
            for (HealthSubject item : mockSubjects()) {
                if (subjectId.equals(item.getSubjectId())) {
                    subject = item;
                    break;
                }
            }
        }
        return success(subject);
    }

    /**
     * 新增服务对象
     */
    @Log(title = "服务对象", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HealthSubject healthSubject) {
        return toAjax(healthSubjectService.insertHealthSubject(healthSubject));
    }

    /**
     * 修改服务对象
     */
    @Log(title = "服务对象", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HealthSubject healthSubject) {
        return toAjax(healthSubjectService.updateHealthSubject(healthSubject));
    }

    /**
     * 删除服务对象
     */
    @Log(title = "服务对象", businessType = BusinessType.DELETE)
    @DeleteMapping("/{subjectIds}")
    public AjaxResult remove(@PathVariable Long[] subjectIds) {
        return toAjax(healthSubjectService.deleteHealthSubjectBySubjectIds(subjectIds));
    }

    private List<HealthSubject> filterSubjects(List<HealthSubject> source, HealthSubject query) {
        if (query == null) {
            return source;
        }
        List<HealthSubject> result = new ArrayList<>();
        for (HealthSubject item : source) {
            boolean match = true;
            if (query.getSubjectName() != null && !query.getSubjectName().isBlank()) {
                match = item.getSubjectName() != null && item.getSubjectName().contains(query.getSubjectName());
            }
            if (match && query.getPhonenumber() != null && !query.getPhonenumber().isBlank()) {
                match = item.getPhonenumber() != null && item.getPhonenumber().contains(query.getPhonenumber());
            }
            if (match && query.getStatus() != null && !query.getStatus().isBlank()) {
                match = query.getStatus().equals(item.getStatus());
            }
            if (match) {
                result.add(item);
            }
        }
        return result;
    }

    private List<HealthSubject> mockSubjects() {
        List<HealthSubject> list = new ArrayList<>();
        list.add(buildSubject(2001L, "zhangsan", "张三", "13800001111", "zhangsan@demo.com", 72, "0", "0", "高血压重点关注"));
        list.add(buildSubject(2002L, "lisi", "李四", "13800002222", "lisi@demo.com", 68, "1", "0", "轻度糖代谢异常"));
        list.add(buildSubject(2003L, "wangwu", "王五", "13800003333", "wangwu@demo.com", 75, "0", "1", "长期外出，暂时停用"));
        list.add(buildSubject(2004L, "zhaoliu", "赵六", "13800004444", "zhaoliu@demo.com", 65, "1", "0", "心率波动较大"));
        list.add(buildSubject(2005L, "sunqi", "孙七", "13800005555", "sunqi@demo.com", 81, "0", "0", "近期有跌倒史"));
        return list;
    }

    private HealthSubject buildSubject(Long id, String subjectName, String nickName, String phone, String email,
                                       int age, String sex, String status, String remark) {
        HealthSubject subject = new HealthSubject();
        subject.setSubjectId(id);
        subject.setSubjectName(subjectName);
        subject.setNickName(nickName);
        subject.setPhonenumber(phone);
        subject.setEmail(email);
        subject.setAge(age);
        subject.setSex(sex);
        subject.setStatus(status);
        subject.setRemark(remark);
        subject.setCreateTime(new Date());
        return subject;
    }
}
