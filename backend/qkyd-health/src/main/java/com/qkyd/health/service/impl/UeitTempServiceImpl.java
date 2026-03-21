package com.qkyd.health.service.impl;

import java.util.Date;
import java.util.List;
import com.qkyd.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qkyd.health.mapper.UeitTempMapper;
import com.qkyd.health.domain.UeitTemp;
import com.qkyd.health.service.IUeitTempService;

/**
 * 体温数据Service业务层处理
 *
 * @author z
 * @date 2024-01-05
 */
@Service
public class UeitTempServiceImpl implements IUeitTempService
{
    @Autowired
    private UeitTempMapper ueitTempMapper;

    /**
     * 查询体温数据
     *
     * @param id 体温数据主键
     * @return 体温数据
     */
    @Override
    public UeitTemp selectUeitTempById(Long id)
    {
        return ueitTempMapper.selectUeitTempById(id);
    }

    /**
     * 查询体温数据列表
     *
     * @param ueitTemp 体温数据
     * @return 体温数据
     */
    @Override
    public List<UeitTemp> selectUeitTempList(UeitTemp ueitTemp)
    {
        return ueitTempMapper.selectUeitTempList(ueitTemp);
    }

    /**
     * 新增体温数据
     *
     * @param ueitTemp 体温数据
     * @return 结果
     */
    @Override
    public int insertUeitTemp(UeitTemp ueitTemp)
    {
        ueitTemp.setCreateTime(DateUtils.getNowDate());
        return ueitTempMapper.insertUeitTemp(ueitTemp);
    }

    /**
     * 修改体温数据
     *
     * @param ueitTemp 体温数据
     * @return 结果
     */
    @Override
    public int updateUeitTemp(UeitTemp ueitTemp)
    {
        return ueitTempMapper.updateUeitTemp(ueitTemp);
    }

    /**
     * 批量删除体温数据
     *
     * @param ids 需要删除的体温数据主键
     * @return 结果
     */
    @Override
    public int deleteUeitTempByIds(Long[] ids)
    {
        return ueitTempMapper.deleteUeitTempByIds(ids);
    }

    /**
     * 删除体温数据信息
     *
     * @param id 体温数据主键
     * @return 结果
     */
    @Override
    public int deleteUeitTempById(Long id)
    {
        return ueitTempMapper.deleteUeitTempById(id);
    }

    /**
     * 查询某用户在创建时间beginReadTim, endReadTime之间的数据
     * @param userId
     * @param beginReadTime
     * @param endReadTime
     * @return
     */
    @Override
    public List<UeitTemp> getDataBoard(int userId, Date beginReadTime, Date endReadTime) {
        return ueitTempMapper.getDataBoard(userId, beginReadTime, endReadTime);
    }
}

