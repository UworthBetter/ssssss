package com.qkyd.quartz.service.impl;

import java.util.List;
import jakarta.annotation.PostConstruct;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.qkyd.common.constant.ScheduleConstants;
import com.qkyd.common.exception.job.TaskException;
import com.qkyd.quartz.domain.SysJob;
import com.qkyd.quartz.mapper.SysJobMapper;
import com.qkyd.quartz.service.ISysJobService;
import com.qkyd.quartz.util.CronUtils;
import com.qkyd.quartz.util.ScheduleUtils;

/**
 * 瀹氭椂浠诲姟璋冨害淇℃伅 鏈嶅姟灞?
 * 
 * @author qkyd
 */
@Service
public class SysJobServiceImpl implements ISysJobService
{
    @Autowired
    private Scheduler scheduler;

    @Autowired
    private SysJobMapper jobMapper;

    /**
     * 椤圭洰鍚姩鏃讹紝鍒濆鍖栧畾鏃跺櫒 涓昏鏄槻姝㈡墜鍔ㄤ慨鏀规暟鎹簱瀵艰嚧鏈悓姝ュ埌瀹氭椂浠诲姟澶勭悊锛堟敞锛氫笉鑳芥墜鍔ㄤ慨鏀规暟鎹簱ID鍜屼换鍔＄粍鍚嶏紝鍚﹀垯浼氬鑷磋剰鏁版嵁锛?
     */
    @PostConstruct
    public void init() throws SchedulerException, TaskException
    {
        scheduler.clear();
        List<SysJob> jobList = jobMapper.selectJobAll();
        for (SysJob job : jobList)
        {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
    }

    /**
     * 鑾峰彇quartz璋冨害鍣ㄧ殑璁″垝浠诲姟鍒楄〃
     * 
     * @param job 璋冨害淇℃伅
     * @return
     */
    @Override
    public List<SysJob> selectJobList(SysJob job)
    {
        return jobMapper.selectJobList(job);
    }

    /**
     * 閫氳繃璋冨害浠诲姟ID鏌ヨ璋冨害淇℃伅
     * 
     * @param jobId 璋冨害浠诲姟ID
     * @return 璋冨害浠诲姟瀵硅薄淇℃伅
     */
    @Override
    public SysJob selectJobById(Long jobId)
    {
        return jobMapper.selectJobById(jobId);
    }

    /**
     * 鏆傚仠浠诲姟
     * 
     * @param job 璋冨害淇℃伅
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pauseJob(SysJob job) throws SchedulerException
    {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
        int rows = jobMapper.updateJob(job);
        if (rows > 0)
        {
            scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return rows;
    }

    /**
     * 鎭㈠浠诲姟
     * 
     * @param job 璋冨害淇℃伅
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resumeJob(SysJob job) throws SchedulerException
    {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        job.setStatus(ScheduleConstants.Status.NORMAL.getValue());
        int rows = jobMapper.updateJob(job);
        if (rows > 0)
        {
            scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return rows;
    }

    /**
     * 鍒犻櫎浠诲姟鍚庯紝鎵€瀵瑰簲鐨則rigger涔熷皢琚垹闄?
     * 
     * @param job 璋冨害淇℃伅
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteJob(SysJob job) throws SchedulerException
    {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        int rows = jobMapper.deleteJobById(jobId);
        if (rows > 0)
        {
            scheduler.deleteJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return rows;
    }

    /**
     * 鎵归噺鍒犻櫎璋冨害淇℃伅
     * 
     * @param jobIds 闇€瑕佸垹闄ょ殑浠诲姟ID
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJobByIds(Long[] jobIds) throws SchedulerException
    {
        for (Long jobId : jobIds)
        {
            SysJob job = jobMapper.selectJobById(jobId);
            deleteJob(job);
        }
    }

    /**
     * 浠诲姟璋冨害鐘舵€佷慨鏀?
     * 
     * @param job 璋冨害淇℃伅
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(SysJob job) throws SchedulerException
    {
        int rows = 0;
        String status = job.getStatus();
        if (ScheduleConstants.Status.NORMAL.getValue().equals(status))
        {
            rows = resumeJob(job);
        }
        else if (ScheduleConstants.Status.PAUSE.getValue().equals(status))
        {
            rows = pauseJob(job);
        }
        return rows;
    }

    /**
     * 绔嬪嵆杩愯浠诲姟
     * 
     * @param job 璋冨害淇℃伅
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean run(SysJob job) throws SchedulerException
    {
        boolean result = false;
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        SysJob properties = selectJobById(job.getJobId());
        // 鍙傛暟
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(ScheduleConstants.TASK_PROPERTIES, properties);
        JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobGroup);
        if (scheduler.checkExists(jobKey))
        {
            result = true;
            scheduler.triggerJob(jobKey, dataMap);
        }
        return result;
    }

    /**
     * 鏂板浠诲姟
     * 
     * @param job 璋冨害淇℃伅 璋冨害淇℃伅
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertJob(SysJob job) throws SchedulerException, TaskException
    {
        job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
        int rows = jobMapper.insertJob(job);
        if (rows > 0)
        {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
        return rows;
    }

    /**
     * 鏇存柊浠诲姟鐨勬椂闂磋〃杈惧紡
     * 
     * @param job 璋冨害淇℃伅
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateJob(SysJob job) throws SchedulerException, TaskException
    {
        SysJob properties = selectJobById(job.getJobId());
        int rows = jobMapper.updateJob(job);
        if (rows > 0)
        {
            updateSchedulerJob(job, properties.getJobGroup());
        }
        return rows;
    }

    /**
     * 鏇存柊浠诲姟
     * 
     * @param job 浠诲姟瀵硅薄
     * @param jobGroup 浠诲姟缁勫悕
     */
    public void updateSchedulerJob(SysJob job, String jobGroup) throws SchedulerException, TaskException
    {
        Long jobId = job.getJobId();
        // 鍒ゆ柇鏄惁瀛樺湪
        JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobGroup);
        if (scheduler.checkExists(jobKey))
        {
            // 闃叉鍒涘缓鏃跺瓨鍦ㄦ暟鎹棶棰?鍏堢Щ闄わ紝鐒跺悗鍦ㄦ墽琛屽垱寤烘搷浣?
            scheduler.deleteJob(jobKey);
        }
        ScheduleUtils.createScheduleJob(scheduler, job);
    }

    /**
     * 鏍￠獙cron琛ㄨ揪寮忔槸鍚︽湁鏁?
     * 
     * @param cronExpression 琛ㄨ揪寮?
     * @return 缁撴灉
     */
    @Override
    public boolean checkCronExpressionIsValid(String cronExpression)
    {
        return CronUtils.isValid(cronExpression);
    }
}


