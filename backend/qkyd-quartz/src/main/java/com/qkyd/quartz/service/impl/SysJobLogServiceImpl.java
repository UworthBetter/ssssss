package com.qkyd.quartz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qkyd.quartz.domain.SysJobLog;
import com.qkyd.quartz.mapper.SysJobLogMapper;
import com.qkyd.quartz.service.ISysJobLogService;

/**
 * 瀹氭椂浠诲姟璋冨害鏃ュ織淇℃伅 鏈嶅姟灞?
 * 
 * @author qkyd
 */
@Service
public class SysJobLogServiceImpl implements ISysJobLogService
{
    @Autowired
    private SysJobLogMapper jobLogMapper;

    /**
     * 鑾峰彇quartz璋冨害鍣ㄦ棩蹇楃殑璁″垝浠诲姟
     * 
     * @param jobLog 璋冨害鏃ュ織淇℃伅
     * @return 璋冨害浠诲姟鏃ュ織闆嗗悎
     */
    @Override
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog)
    {
        return jobLogMapper.selectJobLogList(jobLog);
    }

    /**
     * 閫氳繃璋冨害浠诲姟鏃ュ織ID鏌ヨ璋冨害淇℃伅
     * 
     * @param jobLogId 璋冨害浠诲姟鏃ュ織ID
     * @return 璋冨害浠诲姟鏃ュ織瀵硅薄淇℃伅
     */
    @Override
    public SysJobLog selectJobLogById(Long jobLogId)
    {
        return jobLogMapper.selectJobLogById(jobLogId);
    }

    /**
     * 鏂板浠诲姟鏃ュ織
     * 
     * @param jobLog 璋冨害鏃ュ織淇℃伅
     */
    @Override
    public void addJobLog(SysJobLog jobLog)
    {
        jobLogMapper.insertJobLog(jobLog);
    }

    /**
     * 鎵归噺鍒犻櫎璋冨害鏃ュ織淇℃伅
     * 
     * @param logIds 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    @Override
    public int deleteJobLogByIds(Long[] logIds)
    {
        return jobLogMapper.deleteJobLogByIds(logIds);
    }

    /**
     * 鍒犻櫎浠诲姟鏃ュ織
     * 
     * @param jobId 璋冨害鏃ュ織ID
     */
    @Override
    public int deleteJobLogById(Long jobId)
    {
        return jobLogMapper.deleteJobLogById(jobId);
    }

    /**
     * 娓呯┖浠诲姟鏃ュ織
     */
    @Override
    public void cleanJobLog()
    {
        jobLogMapper.cleanJobLog();
    }
}


