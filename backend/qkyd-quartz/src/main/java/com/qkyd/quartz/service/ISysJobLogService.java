package com.qkyd.quartz.service;

import java.util.List;
import com.qkyd.quartz.domain.SysJobLog;

/**
 * 瀹氭椂浠诲姟璋冨害鏃ュ織淇℃伅淇℃伅 鏈嶅姟灞?
 * 
 * @author qkyd
 */
public interface ISysJobLogService
{
    /**
     * 鑾峰彇quartz璋冨害鍣ㄦ棩蹇楃殑璁″垝浠诲姟
     * 
     * @param jobLog 璋冨害鏃ュ織淇℃伅
     * @return 璋冨害浠诲姟鏃ュ織闆嗗悎
     */
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog);

    /**
     * 閫氳繃璋冨害浠诲姟鏃ュ織ID鏌ヨ璋冨害淇℃伅
     * 
     * @param jobLogId 璋冨害浠诲姟鏃ュ織ID
     * @return 璋冨害浠诲姟鏃ュ織瀵硅薄淇℃伅
     */
    public SysJobLog selectJobLogById(Long jobLogId);

    /**
     * 鏂板浠诲姟鏃ュ織
     * 
     * @param jobLog 璋冨害鏃ュ織淇℃伅
     */
    public void addJobLog(SysJobLog jobLog);

    /**
     * 鎵归噺鍒犻櫎璋冨害鏃ュ織淇℃伅
     * 
     * @param logIds 闇€瑕佸垹闄ょ殑鏃ュ織ID
     * @return 缁撴灉
     */
    public int deleteJobLogByIds(Long[] logIds);

    /**
     * 鍒犻櫎浠诲姟鏃ュ織
     * 
     * @param jobId 璋冨害鏃ュ織ID
     * @return 缁撴灉
     */
    public int deleteJobLogById(Long jobId);

    /**
     * 娓呯┖浠诲姟鏃ュ織
     */
    public void cleanJobLog();
}


