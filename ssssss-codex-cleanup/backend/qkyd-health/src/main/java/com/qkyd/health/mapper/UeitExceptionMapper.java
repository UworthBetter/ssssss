package com.qkyd.health.mapper;

import java.util.Date;
import java.util.List;
import com.qkyd.health.domain.UeitException;
import com.qkyd.health.domain.UeitHeartRate;
import org.apache.ibatis.annotations.Param;

/**
 * 异常数据Mapper接口
 *
 * @author z
 * @date 2024-01-05
 */
public interface UeitExceptionMapper
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
     * 删除异常数据
     *
     * @param id 异常数据主键
     * @return 结果
     */
    public int deleteUeitExceptionById(Long id);

    /**
     * 批量删除异常数据
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUeitExceptionByIds(Long[] ids);
    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @param userId
     * @param beginReadTime
     * @param endReadTime
     * @return
     */
    List<UeitException> getDataBoard(@Param("userId") int userId, @Param("beginReadTime") Date beginReadTime, @Param("endReadTime") Date endReadTime);

    List<UeitException> getExceptionData(String type);
    Integer getTotal(String type);

    /**
     * 根据用户id查询异常数据列表
     * @param userId
     * @return
     */
    List<UeitException> selectUeitExceptionListByUserId(UeitException ueitException);
}
