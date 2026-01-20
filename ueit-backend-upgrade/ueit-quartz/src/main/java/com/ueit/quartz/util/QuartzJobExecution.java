package com.ueit.quartz.util;

import org.quartz.JobExecutionContext;
import com.ueit.quartz.domain.SysJob;

/**
 * 瀹氭椂浠诲姟澶勭悊锛堝厑璁稿苟鍙戞墽琛岋級
 * 
 * @author ruoyi
 *
 */
public class QuartzJobExecution extends AbstractQuartzJob
{
    @Override
    protected void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception
    {
        JobInvokeUtil.invokeMethod(sysJob);
    }
}
