package com.ueit.health.service;

import java.util.Date;
import java.util.List;
import com.ueit.health.domain.UeitException;
import com.ueit.health.domain.UeitHeartRate;

/**
 * 异常数据Service接口
 *
 * @author z
 * @date 2024-01-05
 */
public interface IUeitExceptionService
{
    /**
     * 查询异常数据
     *
     * @param id 异常数据主键
     * @return 异常数据
     */
    public UeitException selectUeitExceptionById(Long id);

    /**
     * 查询异常数据列表
     *
     * @param ueitException 异常数据
     * @return 异常数据集合
     */
    public List<UeitException> selectUeitExceptionList(UeitException ueitException);

    /**
     * 新增异常数据
     *
     * @param ueitException 异常数据
     * @return 结果
     */
    public int insertUeitException(UeitException ueitException);

    /**
     * 修改异常数据
     *
     * @param ueitException 异常数据
     * @return 结果
     */
    public int updateUeitException(UeitException ueitException);

    /**
     * 批量删除异常数据
     *
     * @param ids 需要删除的异常数据主键集合
     * @return 结果
     */
    public int deleteUeitExceptionByIds(Long[] ids);

    /**
     * 删除异常数据信息
     *
     * @param id 异常数据主键
     * @return 结果
     */
    public int deleteUeitExceptionById(Long id);
    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @return
     */
    public List<UeitException> getDataBoard(int userId, Date beginReadTime, Date endReadTime);

    /**
     * 根据用户id查询异常数据列表
     * @param userId
     * @return
     */
    List<UeitException> selectUeitExceptionListByUserId(int userId);
}

