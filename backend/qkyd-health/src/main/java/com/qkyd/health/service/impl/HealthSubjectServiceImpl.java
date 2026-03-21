package com.qkyd.health.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qkyd.health.mapper.HealthSubjectMapper;
import com.qkyd.health.domain.HealthSubject;
import com.qkyd.health.service.IHealthSubjectService;

/**
 * 服务对象Service业务层处理
 * 
 * @author qkyd
 * @date 2026-02-01
 */
@Service
public class HealthSubjectServiceImpl implements IHealthSubjectService
{
    @Autowired
    private HealthSubjectMapper healthSubjectMapper;

    /**
     * 查询服务对象
     * 
     * @param subjectId 服务对象主键
     * @return 服务对象
     */
    @Override
    public HealthSubject selectHealthSubjectBySubjectId(Long subjectId)
    {
        return healthSubjectMapper.selectHealthSubjectBySubjectId(subjectId);
    }

    /**
     * 查询服务对象列表
     * 
     * @param healthSubject 服务对象
     * @return 服务对象
     */
    @Override
    public List<HealthSubject> selectHealthSubjectList(HealthSubject healthSubject)
    {
        return healthSubjectMapper.selectHealthSubjectList(healthSubject);
    }

    /**
     * 新增服务对象
     * 
     * @param healthSubject 服务对象
     * @return 结果
     */
    @Override
    public int insertHealthSubject(HealthSubject healthSubject)
    {
        return healthSubjectMapper.insertHealthSubject(healthSubject);
    }

    /**
     * 修改服务对象
     * 
     * @param healthSubject 服务对象
     * @return 结果
     */
    @Override
    public int updateHealthSubject(HealthSubject healthSubject)
    {
        return healthSubjectMapper.updateHealthSubject(healthSubject);
    }

    /**
     * 批量删除服务对象
     * 
     * @param subjectIds 需要删除的服务对象主键
     * @return 结果
     */
    @Override
    public int deleteHealthSubjectBySubjectIds(Long[] subjectIds)
    {
        return healthSubjectMapper.deleteHealthSubjectBySubjectIds(subjectIds);
    }

    /**
     * 删除服务对象信息
     * 
     * @param subjectId 服务对象主键
     * @return 结果
     */
    @Override
    public int deleteHealthSubjectBySubjectId(Long subjectId)
    {
        return healthSubjectMapper.deleteHealthSubjectBySubjectId(subjectId);
    }
}
