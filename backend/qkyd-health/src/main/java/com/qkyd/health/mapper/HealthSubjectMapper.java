package com.qkyd.health.mapper;

import java.util.List;
import com.qkyd.health.domain.HealthSubject;

/**
 * 服务对象Mapper接口
 * 
 * @author qkyd
 * @date 2026-02-01
 */
public interface HealthSubjectMapper 
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
     * 删除服务对象
     * 
     * @param subjectId 服务对象主键
     * @return 结果
     */
    public int deleteHealthSubjectBySubjectId(Long subjectId);

    /**
     * 批量删除服务对象
     * 
     * @param subjectIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHealthSubjectBySubjectIds(Long[] subjectIds);
}
