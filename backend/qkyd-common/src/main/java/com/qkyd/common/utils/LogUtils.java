package com.qkyd.common.utils;

/**
 * 澶勭悊骞惰褰曟棩蹇楁枃浠?
 * 
 * @author qkyd
 */
public class LogUtils
{
    public static String getBlock(Object msg)
    {
        if (msg == null)
        {
            msg = "";
        }
        return "[" + msg.toString() + "]";
    }
}


