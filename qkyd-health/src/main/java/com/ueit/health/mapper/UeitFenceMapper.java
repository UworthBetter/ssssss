package com.ueit.health.mapper;

import java.util.List;
import com.ueit.health.domain.UeitFence;

/**
 * 围栏Mapper接口
 *
 * @author z
 * @date 2024-01-19
 */
public interface UeitFenceMapper
{
    /**
     * 查询围栏
     *
     * @param id 围栏主键
     * @return 围栏
     */
    public UeitFence selectUeitFenceById(Long id);

    /**
     * 查询围栏列表
     *
     * @param ueitFence 围栏
     * @return 围栏集合
     */
    public List<UeitFence> selectUeitFenceList(UeitFence ueitFence);

    /**
     * 根据ueserId查询围栏列表
     *
     * @param userId ueserId
     * @return 围栏集合
     */
    public List<UeitFence> selectUeitFenceListByUserId(Long userId);
    /**
     * 新增围栏
     *
     * @param ueitFence 围栏
     * @return 结果
     */
    public int insertUeitFence(UeitFence ueitFence);

    /**
     * 修改围栏
     *
     * @param ueitFence 围栏
     * @return 结果
     */
    public int updateUeitFence(UeitFence ueitFence);

    /**
     * 修改围栏状态--->是否启用
     * @param ueitFence
     * @return
     */
    public int updateFenceStatus(UeitFence ueitFence);

    /**
     * 删除围栏
     *
     * @param id 围栏主键
     * @return 结果
     */
    public int deleteUeitFenceById(Long id);

    /**
     * 批量删除围栏
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUeitFenceByIds(Long[] ids);
}

