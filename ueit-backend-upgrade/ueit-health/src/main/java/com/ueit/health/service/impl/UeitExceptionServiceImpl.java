package com.ueit.health.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.ueit.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ueit.health.mapper.UeitExceptionMapper;
import com.ueit.health.domain.UeitException;
import com.ueit.health.service.IUeitExceptionService;

/**
 * 异常数据Service业务层处理
 *
 * @author z
 * @date 2024-01-05
 */
@Service
public class UeitExceptionServiceImpl implements IUeitExceptionService
{
    @Autowired
    private UeitExceptionMapper ueitExceptionMapper;

    /**
     * 查询异常数据
     *
     * @param id 异常数据主键
     * @return 异常数据
     */
    @Override
    public UeitException selectUeitExceptionById(Long id)
    {
        return ueitExceptionMapper.selectUeitExceptionById(id);
    }

    /**
     * 查询异常数据列表
     *
     * @param ueitException 异常数据
     * @return 异常数据
     */
    @Override
    public List<UeitException> selectUeitExceptionList(UeitException ueitException)
    {
        try {
            //转换日期格式 从 yyyy-MM-dd转成yyyy-MM-dd HH:mm:ss
            // 目的：前端传过来的结束时间也是00:00:00变成23:59:59
            //查询一天内所有的数据
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            Date startCreateTime = ueitException.getStartCreateTime();
            Date endCreateTime = ueitException.getEndCreateTime();
            if (startCreateTime != null && endCreateTime != null) {
                String s1 = sdf1.format(startCreateTime) + " 00:00:00";
                String e1 = sdf1.format(endCreateTime) + " 23:59:59";
                Date s2 = sdf.parse(s1);
                Date e2 = sdf.parse(e1);
                ueitException.setStartCreateTime(s2);
                ueitException.setEndCreateTime(e2);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ueitExceptionMapper.selectUeitExceptionList(ueitException);
    }

    /**
     * 新增异常数据
     *
     * @param ueitException 异常数据
     * @return 结果
     */
    @Override
    public int insertUeitException(UeitException ueitException)
    {
        return ueitExceptionMapper.insertUeitException(ueitException);
    }

    /**
     * 修改异常数据
     *
     * @param ueitException 异常数据
     * @return 结果
     */
    @Override
    public int updateUeitException(UeitException ueitException)
    {
        ueitException.setUpdateTime(DateUtils.getNowDate());
        ueitException.setState("1");
        return ueitExceptionMapper.updateUeitException(ueitException);
    }

    /**
     * 批量删除异常数据
     *
     * @param ids 需要删除的异常数据主键
     * @return 结果
     */
    @Override
    public int deleteUeitExceptionByIds(Long[] ids)
    {
        return ueitExceptionMapper.deleteUeitExceptionByIds(ids);
    }

    /**
     * 删除异常数据信息
     *
     * @param id 异常数据主键
     * @return 结果
     */
    @Override
    public int deleteUeitExceptionById(Long id)
    {
        return ueitExceptionMapper.deleteUeitExceptionById(id);
    }

    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @param userId
     * @param beginReadTime
     * @param endReadTime
     * @return
     */
    @Override
    public List<UeitException> getDataBoard(int userId, Date beginReadTime, Date endReadTime) {
        return ueitExceptionMapper.getDataBoard(userId, beginReadTime, endReadTime);
    }

    /**
     * 根据用户id查询异常数据列表
     * @param userId
     * @return
     */
    @Override
    public List<UeitException> selectUeitExceptionListByUserId(int userId) {
        return ueitExceptionMapper.selectUeitExceptionListByUserId(userId);
    }
}
