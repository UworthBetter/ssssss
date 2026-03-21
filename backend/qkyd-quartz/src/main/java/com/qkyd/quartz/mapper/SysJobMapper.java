package com.qkyd.quartz.mapper;

import java.util.List;
import com.qkyd.quartz.domain.SysJob;

/**
 * 璋冨害浠诲姟淇℃伅 鏁版嵁灞?
 * 
 * @author qkyd
 */
public interface SysJobMapper
{
    /**
     * 鏌ヨ璋冨害浠诲姟鏃ュ織闆嗗悎
     * 
     * @param job 璋冨害淇℃伅
     * @return 鎿嶄綔鏃ュ織闆嗗悎
     */
    public List<SysJob> selectJobList(SysJob job);

    /**
     * 鏌ヨ鎵€鏈夎皟搴︿换鍔?
     * 
     * @return 璋冨害浠诲姟鍒楄〃
     */
    public List<SysJob> selectJobAll();

    /**
     * 閫氳繃璋冨害ID鏌ヨ璋冨害浠诲姟淇℃伅
     * 
     * @param jobId 璋冨害ID
     * @return 瑙掕壊瀵硅薄淇℃伅
     */
    public SysJob selectJobById(Long jobId);

    /**
     * 閫氳繃璋冨害ID鍒犻櫎璋冨害浠诲姟淇℃伅
     * 
     * @param jobId 璋冨害ID
     * @return 缁撴灉
     */
    public int deleteJobById(Long jobId);

    /**
     * 鎵归噺鍒犻櫎璋冨害浠诲姟淇℃伅
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteJobByIds(Long[] ids);

    /**
     * 淇敼璋冨害浠诲姟淇℃伅
     * 
     * @param job 璋冨害浠诲姟淇℃伅
     * @return 缁撴灉
     */
    public int updateJob(SysJob job);

    /**
     * 鏂板璋冨害浠诲姟淇℃伅
     * 
     * @param job 璋冨害浠诲姟淇℃伅
     * @return 缁撴灉
     */
    public int insertJob(SysJob job);
}


