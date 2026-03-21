package com.qkyd.quartz.util;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import com.qkyd.quartz.domain.SysJob;

/**
 * 瀹氭椂浠诲姟澶勭悊锛堢姝㈠苟鍙戞墽琛岋級
 * 
 * @author qkyd
 *
 */
@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends AbstractQuartzJob
{
    @Override
    protected void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception
    {
        JobInvokeUtil.invokeMethod(sysJob);
    }
}


