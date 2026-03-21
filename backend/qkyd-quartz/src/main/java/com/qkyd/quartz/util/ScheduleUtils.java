package com.qkyd.quartz.util;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import com.qkyd.common.constant.Constants;
import com.qkyd.common.constant.ScheduleConstants;
import com.qkyd.common.exception.job.TaskException;
import com.qkyd.common.exception.job.TaskException.Code;
import com.qkyd.common.utils.StringUtils;
import com.qkyd.common.utils.spring.SpringUtils;
import com.qkyd.quartz.domain.SysJob;

/**
 * 瀹氭椂浠诲姟宸ュ叿绫?
 * 
 * @author qkyd
 *
 */
public class ScheduleUtils
{
    /**
     * 寰楀埌quartz浠诲姟绫?
     *
     * @param sysJob 鎵ц璁″垝
     * @return 鍏蜂綋鎵ц浠诲姟绫?
     */
    private static Class<? extends Job> getQuartzJobClass(SysJob sysJob)
    {
        boolean isConcurrent = "0".equals(sysJob.getConcurrent());
        return isConcurrent ? QuartzJobExecution.class : QuartzDisallowConcurrentExecution.class;
    }

    /**
     * 鏋勫缓浠诲姟瑙﹀彂瀵硅薄
     */
    public static TriggerKey getTriggerKey(Long jobId, String jobGroup)
    {
        return TriggerKey.triggerKey(ScheduleConstants.TASK_CLASS_NAME + jobId, jobGroup);
    }

    /**
     * 鏋勫缓浠诲姟閿璞?
     */
    public static JobKey getJobKey(Long jobId, String jobGroup)
    {
        return JobKey.jobKey(ScheduleConstants.TASK_CLASS_NAME + jobId, jobGroup);
    }

    /**
     * 鍒涘缓瀹氭椂浠诲姟
     */
    public static void createScheduleJob(Scheduler scheduler, SysJob job) throws SchedulerException, TaskException
    {
        Class<? extends Job> jobClass = getQuartzJobClass(job);
        // 鏋勫缓job淇℃伅
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(getJobKey(jobId, jobGroup)).build();

        // 琛ㄨ揪寮忚皟搴︽瀯寤哄櫒
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
        cronScheduleBuilder = handleCronScheduleMisfirePolicy(job, cronScheduleBuilder);

        // 鎸夋柊鐨刢ronExpression琛ㄨ揪寮忔瀯寤轰竴涓柊鐨則rigger
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(getTriggerKey(jobId, jobGroup))
                .withSchedule(cronScheduleBuilder).build();

        // 鏀惧叆鍙傛暟锛岃繍琛屾椂鐨勬柟娉曞彲浠ヨ幏鍙?
        jobDetail.getJobDataMap().put(ScheduleConstants.TASK_PROPERTIES, job);

        // 鍒ゆ柇鏄惁瀛樺湪
        if (scheduler.checkExists(getJobKey(jobId, jobGroup)))
        {
            // 闃叉鍒涘缓鏃跺瓨鍦ㄦ暟鎹棶棰?鍏堢Щ闄わ紝鐒跺悗鍦ㄦ墽琛屽垱寤烘搷浣?
            scheduler.deleteJob(getJobKey(jobId, jobGroup));
        }

        // 鍒ゆ柇浠诲姟鏄惁杩囨湡
        if (StringUtils.isNotNull(CronUtils.getNextExecution(job.getCronExpression())))
        {
            // 鎵ц璋冨害浠诲姟
            scheduler.scheduleJob(jobDetail, trigger);
        }

        // 鏆傚仠浠诲姟
        if (job.getStatus().equals(ScheduleConstants.Status.PAUSE.getValue()))
        {
            scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
    }

    /**
     * 璁剧疆瀹氭椂浠诲姟绛栫暐
     */
    public static CronScheduleBuilder handleCronScheduleMisfirePolicy(SysJob job, CronScheduleBuilder cb)
            throws TaskException
    {
        switch (job.getMisfirePolicy())
        {
            case ScheduleConstants.MISFIRE_DEFAULT:
                return cb;
            case ScheduleConstants.MISFIRE_IGNORE_MISFIRES:
                return cb.withMisfireHandlingInstructionIgnoreMisfires();
            case ScheduleConstants.MISFIRE_FIRE_AND_PROCEED:
                return cb.withMisfireHandlingInstructionFireAndProceed();
            case ScheduleConstants.MISFIRE_DO_NOTHING:
                return cb.withMisfireHandlingInstructionDoNothing();
            default:
                throw new TaskException("The task misfire policy '" + job.getMisfirePolicy()
                        + "' cannot be used in cron schedule tasks", Code.CONFIG_ERROR);
        }
    }

    /**
     * 妫€鏌ュ寘鍚嶆槸鍚︿负鐧藉悕鍗曢厤缃?
     * 
     * @param invokeTarget 鐩爣瀛楃涓?
     * @return 缁撴灉
     */
    public static boolean whiteList(String invokeTarget)
    {
        String packageName = StringUtils.substringBefore(invokeTarget, "(");
        int count = StringUtils.countMatches(packageName, ".");
        if (count > 1)
        {
            return StringUtils.containsAnyIgnoreCase(invokeTarget, Constants.JOB_WHITELIST_STR);
        }
        Object obj = SpringUtils.getBean(StringUtils.split(invokeTarget, ".")[0]);
        String beanPackageName = obj.getClass().getPackage().getName();
        return StringUtils.containsAnyIgnoreCase(beanPackageName, Constants.JOB_WHITELIST_STR)
                && !StringUtils.containsAnyIgnoreCase(beanPackageName, Constants.JOB_ERROR_STR);
    }
}


