package com.qkyd.quartz.util;

import java.text.ParseException;
import java.util.Date;
import org.quartz.CronExpression;

/**
 * cron琛ㄨ揪寮忓伐鍏风被
 * 
 * @author qkyd
 *
 */
public class CronUtils
{
    /**
     * 杩斿洖涓€涓竷灏斿€间唬琛ㄤ竴涓粰瀹氱殑Cron琛ㄨ揪寮忕殑鏈夋晥鎬?
     *
     * @param cronExpression Cron琛ㄨ揪寮?
     * @return boolean 琛ㄨ揪寮忔槸鍚︽湁鏁?
     */
    public static boolean isValid(String cronExpression)
    {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * 杩斿洖涓€涓瓧绗︿覆鍊?琛ㄧず璇ユ秷鎭棤鏁圕ron琛ㄨ揪寮忕粰鍑烘湁鏁堟€?
     *
     * @param cronExpression Cron琛ㄨ揪寮?
     * @return String 鏃犳晥鏃惰繑鍥炶〃杈惧紡閿欒鎻忚堪,濡傛灉鏈夋晥杩斿洖null
     */
    public static String getInvalidMessage(String cronExpression)
    {
        try
        {
            new CronExpression(cronExpression);
            return null;
        }
        catch (ParseException pe)
        {
            return pe.getMessage();
        }
    }

    /**
     * 杩斿洖涓嬩竴涓墽琛屾椂闂存牴鎹粰瀹氱殑Cron琛ㄨ揪寮?
     *
     * @param cronExpression Cron琛ㄨ揪寮?
     * @return Date 涓嬫Cron琛ㄨ揪寮忔墽琛屾椂闂?
     */
    public static Date getNextExecution(String cronExpression)
    {
        try
        {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getNextValidTimeAfter(new Date(System.currentTimeMillis()));
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}


