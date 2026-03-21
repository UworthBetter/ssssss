package com.qkyd.framework.manager.factory;

import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qkyd.common.constant.Constants;
import com.qkyd.common.utils.LogUtils;
import com.qkyd.common.utils.ServletUtils;
import com.qkyd.common.utils.StringUtils;
import com.qkyd.common.utils.ip.AddressUtils;
import com.qkyd.common.utils.ip.IpUtils;
import com.qkyd.common.utils.spring.SpringUtils;
import com.qkyd.system.domain.SysLogininfor;
import com.qkyd.system.domain.SysOperLog;
import com.qkyd.system.service.ISysLogininforService;
import com.qkyd.system.service.ISysOperLogService;
import eu.bitwalker.useragentutils.UserAgent;

/**
 * 寮傛宸ュ巶锛堜骇鐢熶换鍔＄敤锛?
 * 
 * @author qkyd
 */
public class AsyncFactory
{
    private static final Logger sys_user_logger = LoggerFactory.getLogger("sys-user");

    /**
     * 璁板綍鐧诲綍淇℃伅
     * 
     * @param username 鐢ㄦ埛鍚?
     * @param status 鐘舵€?
     * @param message 娑堟伅
     * @param args 鍒楄〃
     * @return 浠诲姟task
     */
    public static TimerTask recordLogininfor(final String username, final String status, final String message,
            final Object... args)
    {
        final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        final String ip = IpUtils.getIpAddr();
        return new TimerTask()
        {
            @Override
            public void run()
            {
                String address = AddressUtils.getRealAddressByIP(ip);
                StringBuilder s = new StringBuilder();
                s.append(LogUtils.getBlock(ip));
                s.append(address);
                s.append(LogUtils.getBlock(username));
                s.append(LogUtils.getBlock(status));
                s.append(LogUtils.getBlock(message));
                // 鎵撳嵃淇℃伅鍒版棩蹇?
                sys_user_logger.info(s.toString(), args);
                // 鑾峰彇瀹㈡埛绔搷浣滅郴缁?
                String os = userAgent.getOperatingSystem().getName();
                // 鑾峰彇瀹㈡埛绔祻瑙堝櫒
                String browser = userAgent.getBrowser().getName();
                // 灏佽瀵硅薄
                SysLogininfor logininfor = new SysLogininfor();
                logininfor.setUserName(username);
                logininfor.setIpaddr(ip);
                logininfor.setLoginLocation(address);
                logininfor.setBrowser(browser);
                logininfor.setOs(os);
                logininfor.setMsg(message);
                // 鏃ュ織鐘舵€?
                if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER))
                {
                    logininfor.setStatus(Constants.SUCCESS);
                }
                else if (Constants.LOGIN_FAIL.equals(status))
                {
                    logininfor.setStatus(Constants.FAIL);
                }
                // 鎻掑叆鏁版嵁
                SpringUtils.getBean(ISysLogininforService.class).insertLogininfor(logininfor);
            }
        };
    }

    /**
     * 鎿嶄綔鏃ュ織璁板綍
     * 
     * @param operLog 鎿嶄綔鏃ュ織淇℃伅
     * @return 浠诲姟task
     */
    public static TimerTask recordOper(final SysOperLog operLog)
    {
        return new TimerTask()
        {
            @Override
            public void run()
            {
                // 杩滅▼鏌ヨ鎿嶄綔鍦扮偣
                operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
                SpringUtils.getBean(ISysOperLogService.class).insertOperlog(operLog);
            }
        };
    }
}


