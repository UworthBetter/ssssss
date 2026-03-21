package com.qkyd.health.mapper;

import java.util.List;
import com.qkyd.health.domain.HealthSubjectImportLog;

/**
 * 服务对象导入日志Mapper接口
 *
 * @author qkyd
 * @date 2026-02-06
 */
public interface HealthSubjectImportLogMapper
{
    /**
     * 查询服务对象导入日志
     *
     * @param id 服务对象导入日志主键
     * @return 服务对象导入日志
     */
    public HealthSubjectImportLog selectHealthSubjectImportLogById(Long id);

    /**
     * 查询服务对象导入日志列表
     *
     * @param healthSubjectImportLog 服务对象导入日志
     * @return 服务对象导入日志集合
     */
    public List<HealthSubjectImportLog> selectHealthSubjectImportLogList(HealthSubjectImportLog healthSubjectImportLog);

    /**
     * 新增服务对象导入日志
     *
     * @param healthSubjectImportLog 服务对象导入日志
     * @return 结果
     */
    public int insertHealthSubjectImportLog(HealthSubjectImportLog healthSubjectImportLog);

    /**
     * 修改服务对象导入日志
     *
     * @param healthSubjectImportLog 服务对象导入日志
     * @return 结果
     */
    public int updateHealthSubjectImportLog(HealthSubjectImportLog healthSubjectImportLog);

    /**
     * 删除服务对象导入日志
     *
     * @param id 服务对象导入日志主键
     * @return 结果
     */
    public int deleteHealthSubjectImportLogById(Long id);

    /**
     * 批量删除服务对象导入日志
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHealthSubjectImportLogByIds(Long[] ids);

    /**
     * 查询用户的导入历史
     *
     * @param userId 用户ID
     * @return 导入日志列表
     */
    public List<HealthSubjectImportLog> selectByUserId(Long userId);

    /**
     * 统计导入次数
     *
     * @param userId 用户ID
     * @param importType 导入类型
     * @return 导入次数
     */
    public int countImportByUserAndType(Long userId, String importType);
}
