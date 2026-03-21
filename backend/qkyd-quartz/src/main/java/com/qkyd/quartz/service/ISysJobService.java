package com.qkyd.quartz.service;

import java.util.List;
import org.quartz.SchedulerException;
import com.qkyd.common.exception.job.TaskException;
import com.qkyd.quartz.domain.SysJob;

/**
 * 瀹氭椂浠诲姟璋冨害淇℃伅淇℃伅 鏈嶅姟灞?
 * 
 * @author qkyd
 */
public interface ISysJobService
{
    /**
     * 鑾峰彇quartz璋冨害鍣ㄧ殑璁″垝浠诲姟
     * 
     * @param job 璋冨害淇℃伅
     * @return 璋冨害浠诲姟闆嗗悎
     */
    public List<SysJob> selectJobList(SysJob job);

    /**
     * 閫氳繃璋冨害浠诲姟ID鏌ヨ璋冨害淇℃伅
     * 
     * @param jobId 璋冨害浠诲姟ID
     * @return 璋冨害浠诲姟瀵硅薄淇℃伅
     */
    public SysJob selectJobById(Long jobId);

    /**
     * 鏆傚仠浠诲姟
     * 
     * @param job 璋冨害淇℃伅
     * @return 缁撴灉
     */
    public int pauseJob(SysJob job) throws SchedulerException;

    /**
     * 鎭㈠浠诲姟
     * 
     * @param job 璋冨害淇℃伅
     * @return 缁撴灉
     */
    public int resumeJob(SysJob job) throws SchedulerException;

    /**
     * 鍒犻櫎浠诲姟鍚庯紝鎵€瀵瑰簲鐨則rigger涔熷皢琚垹闄?
     * 
     * @param job 璋冨害淇℃伅
     * @return 缁撴灉
     */
    public int deleteJob(SysJob job) throws SchedulerException;

    /**
     * 鎵归噺鍒犻櫎璋冨害淇℃伅
     * 
     * @param jobIds 闇€瑕佸垹闄ょ殑浠诲姟ID
     * @return 缁撴灉
     */
    public void deleteJobByIds(Long[] jobIds) throws SchedulerException;

    /**
     * 浠诲姟璋冨害鐘舵€佷慨鏀?
     * 
     * @param job 璋冨害淇℃伅
     * @return 缁撴灉
     */
    public int changeStatus(SysJob job) throws SchedulerException;

    /**
     * 绔嬪嵆杩愯浠诲姟
     * 
     * @param job 璋冨害淇℃伅
     * @return 缁撴灉
     */
    public boolean run(SysJob job) throws SchedulerException;

    /**
     * 鏂板浠诲姟
     * 
     * @param job 璋冨害淇℃伅
     * @return 缁撴灉
     */
    public int insertJob(SysJob job) throws SchedulerException, TaskException;

    /**
     * 鏇存柊浠诲姟
     * 
     * @param job 璋冨害淇℃伅
     * @return 缁撴灉
     */
    public int updateJob(SysJob job) throws SchedulerException, TaskException;

    /**
     * 鏍￠獙cron琛ㄨ揪寮忔槸鍚︽湁鏁?
     * 
     * @param cronExpression 琛ㄨ揪寮?
     * @return 缁撴灉
     */
    public boolean checkCronExpressionIsValid(String cronExpression);
}


