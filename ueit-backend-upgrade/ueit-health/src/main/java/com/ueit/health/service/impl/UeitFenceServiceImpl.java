package com.ueit.health.service.impl;

import java.util.List;
import com.ueit.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ueit.health.mapper.UeitFenceMapper;
import com.ueit.health.domain.UeitFence;
import com.ueit.health.service.IUeitFenceService;

/**
 * 围栏Service业务层处理
 *
 * @author z
 * @date 2024-01-19
 */
@Service
public class UeitFenceServiceImpl implements IUeitFenceService
{
    @Autowired
    private UeitFenceMapper ueitFenceMapper;

    /**
     * 查询围栏
     *
     * @param id 围栏主键
     * @return 围栏
     */
    @Override
    public UeitFence selectUeitFenceById(Long id)
    {
        return ueitFenceMapper.selectUeitFenceById(id);
    }

    /**
     * 查询围栏列表
     *
     * @param ueitFence 围栏
     * @return 围栏
     */
    @Override
    public List<UeitFence> selectUeitFenceList(UeitFence ueitFence)
    {
        return ueitFenceMapper.selectUeitFenceList(ueitFence);
    }

    /**
     * 根据ueserId查询围栏列表
     * @param userId ueserId
     * @return
     */
    @Override
    public List<UeitFence> selectUeitFenceListByUserId(Long userId) {
        return ueitFenceMapper.selectUeitFenceListByUserId(userId);
    }

    /**
     * 新增围栏
     *
     * @param ueitFence 围栏
     * @return 结果
     */
    @Override
    public int insertUeitFence(UeitFence ueitFence)
    {
        ueitFence.setCreateTime(DateUtils.getNowDate());
        return ueitFenceMapper.insertUeitFence(ueitFence);
    }

    /**
     * 修改围栏
     *
     * @param ueitFence 围栏
     * @return 结果
     */
    @Override
    public int updateUeitFence(UeitFence ueitFence)
    {
        return ueitFenceMapper.updateUeitFence(ueitFence);
    }

    /**
     * 修改围栏状态--->是否启用
     * @param ueitFence
     * @return
     */
    @Override
    public int updateFenceStatus(UeitFence ueitFence) {
        return ueitFenceMapper.updateFenceStatus(ueitFence);
    }

    /**
     * 批量删除围栏
     *
     * @param ids 需要删除的围栏主键
     * @return 结果
     */
    @Override
    public int deleteUeitFenceByIds(Long[] ids)
    {
        return ueitFenceMapper.deleteUeitFenceByIds(ids);
    }

    /**
     * 删除围栏信息
     *
     * @param id 围栏主键
     * @return 结果
     */
    @Override
    public int deleteUeitFenceById(Long id)
    {
        return ueitFenceMapper.deleteUeitFenceById(id);
    }

    /**
     * 新增围栏/修改围栏
     * @param ueitFence
     * @return
     */
    @Override
    public int operateUeitFence(UeitFence ueitFence) {
        if (ueitFence.getId() == null) {
            // 执行新增操作
            return ueitFenceMapper.insertUeitFence(ueitFence);
        } else {
            // 执行修改操作
            return ueitFenceMapper.updateUeitFence(ueitFence);
        }
    }

}
