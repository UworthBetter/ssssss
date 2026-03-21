package com.qkyd.framework.manager;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.qkyd.common.utils.Threads;
import com.qkyd.common.utils.spring.SpringUtils;

/**
 * 寮傛浠诲姟绠＄悊鍣?
 * 
 * @author qkyd
 */
public class AsyncManager
{
    /**
     * 鎿嶄綔寤惰繜10姣
     */
    private final int OPERATE_DELAY_TIME = 10;

    /**
     * 寮傛鎿嶄綔浠诲姟璋冨害绾跨▼姹?
     */
    private ScheduledExecutorService executor = SpringUtils.getBean("scheduledExecutorService");

    /**
     * 鍗曚緥妯″紡
     */
    private AsyncManager(){}

    private static AsyncManager me = new AsyncManager();

    public static AsyncManager me()
    {
        return me;
    }

    /**
     * 鎵ц浠诲姟
     * 
     * @param task 浠诲姟
     */
    public void execute(TimerTask task)
    {
        executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 鍋滄浠诲姟绾跨▼姹?
     */
    public void shutdown()
    {
        Threads.shutdownAndAwaitTermination(executor);
    }
}


