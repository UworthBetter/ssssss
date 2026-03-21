package com.qkyd.health.service;

import java.util.List;
import com.qkyd.health.domain.HealthSubject;

/**
 * 服务对象Service接口
 * 
 * @author qkyd
 * @date 2026-02-01
 */
public interface IHealthSubjectService 
{
    /**
     * 查询服务对象
     * 
     * @param subjectId 服务对象主键
     * @return 服务对象
     */
    public HealthSubject selectHealthSubjectBySubjectId(Long subjectId);

    /**
     * 查询服务对象列表
     * 
     * @param healthSubject 服务对象
     * @return 服务对象集合
     */
    public List<HealthSubject> selectHealthSubjectList(HealthSubject healthSubject);

    /**
     * 新增服务对象
     * 
     * @param healthSubject 服务对象
     * @return 结果
     */
    public int insertHealthSubject(HealthSubject healthSubject);

    /**
     * 修改服务对象
     * 
     * @param healthSubject 服务对象
     * @return 结果
     */
    public int updateHealthSubject(HealthSubject healthSubject);

    /**
     * 批量删除服务对象
     * 
     * @param subjectIds 需要删除的服务对象主键集合
     * @return 结果
     */
    public int deleteHealthSubjectBySubjectIds(Long[] subjectIds);

    /**
     * 删除服务对象信息
     * 
     * @param subjectId 服务对象主键
     * @return 结果
     */
    public int deleteHealthSubjectBySubjectId(Long subjectId);
}
