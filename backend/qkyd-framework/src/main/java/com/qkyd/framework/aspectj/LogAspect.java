package com.qkyd.framework.aspectj;

import java.util.Collection;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.qkyd.common.annotation.Log;
import com.qkyd.common.core.domain.entity.SysUser;
import com.qkyd.common.core.domain.model.LoginUser;
import com.qkyd.common.enums.BusinessStatus;
import com.qkyd.common.enums.HttpMethod;
import com.qkyd.common.filter.PropertyPreExcludeFilter;
import com.qkyd.common.utils.SecurityUtils;
import com.qkyd.common.utils.ServletUtils;
import com.qkyd.common.utils.StringUtils;
import com.qkyd.common.utils.ip.IpUtils;
import com.qkyd.framework.manager.AsyncManager;
import com.qkyd.framework.manager.factory.AsyncFactory;
import com.qkyd.system.domain.SysOperLog;

/**
 * 鎿嶄綔鏃ュ織璁板綍澶勭悊
 * 
 * @author qkyd
 */
@Aspect
@Component
public class LogAspect
{
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    /** 鎺掗櫎鏁忔劅灞炴€у瓧娈?*/
    public static final String[] EXCLUDE_PROPERTIES = { "password", "oldPassword", "newPassword", "confirmPassword" };

    /** 璁＄畻鎿嶄綔娑堣€楁椂闂?*/
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<Long>("Cost Time");

    /**
     * 澶勭悊璇锋眰鍓嶆墽琛?
     */
    @Before(value = "@annotation(controllerLog)")
    public void boBefore(JoinPoint joinPoint, Log controllerLog)
    {
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    /**
     * 澶勭悊瀹岃姹傚悗鎵ц
     *
     * @param joinPoint 鍒囩偣
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult)
    {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 鎷︽埅寮傚父鎿嶄綔
     * 
     * @param joinPoint 鍒囩偣
     * @param e 寮傚父
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e)
    {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult)
    {
        try
        {
            // 鑾峰彇褰撳墠鐨勭敤鎴?
            LoginUser loginUser = SecurityUtils.getLoginUser();

            // *========鏁版嵁搴撴棩蹇?========*//
            SysOperLog operLog = new SysOperLog();
            operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
            // 璇锋眰鐨勫湴鍧€
            String ip = IpUtils.getIpAddr();
            operLog.setOperIp(ip);
            operLog.setOperUrl(StringUtils.substring(ServletUtils.getRequest().getRequestURI(), 0, 255));
            if (loginUser != null)
            {
                operLog.setOperName(loginUser.getUsername());
                SysUser currentUser = loginUser.getUser();
                if (StringUtils.isNotNull(currentUser) && StringUtils.isNotNull(currentUser.getDept()))
                {
                    operLog.setDeptName(currentUser.getDept().getDeptName());
                }
            }

            if (e != null)
            {
                operLog.setStatus(BusinessStatus.FAIL.ordinal());
                operLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            }
            // 璁剧疆鏂规硶鍚嶇О
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");
            // 璁剧疆璇锋眰鏂瑰紡
            operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
            // 澶勭悊璁剧疆娉ㄨВ涓婄殑鍙傛暟
            getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult);
            // 璁剧疆娑堣€楁椂闂?
            operLog.setCostTime(System.currentTimeMillis() - TIME_THREADLOCAL.get());
            // 淇濆瓨鏁版嵁搴?
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
        }
        catch (Exception exp)
        {
            // 璁板綍鏈湴寮傚父鏃ュ織
            log.error("寮傚父淇℃伅:{}", exp.getMessage());
            exp.printStackTrace();
        }
        finally
        {
            TIME_THREADLOCAL.remove();
        }
    }

    /**
     * 鑾峰彇娉ㄨВ涓鏂规硶鐨勬弿杩颁俊鎭?鐢ㄤ簬Controller灞傛敞瑙?
     * 
     * @param log 鏃ュ織
     * @param operLog 鎿嶄綔鏃ュ織
     * @throws Exception
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, Log log, SysOperLog operLog, Object jsonResult) throws Exception
    {
        // 璁剧疆action鍔ㄤ綔
        operLog.setBusinessType(log.businessType().ordinal());
        // 璁剧疆鏍囬
        operLog.setTitle(log.title());
        // 璁剧疆鎿嶄綔浜虹被鍒?
        operLog.setOperatorType(log.operatorType().ordinal());
        // 鏄惁闇€瑕佷繚瀛榬equest锛屽弬鏁板拰鍊?
        if (log.isSaveRequestData())
        {
            // 鑾峰彇鍙傛暟鐨勪俊鎭紝浼犲叆鍒版暟鎹簱涓€?
            setRequestValue(joinPoint, operLog, log.excludeParamNames());
        }
        // 鏄惁闇€瑕佷繚瀛榬esponse锛屽弬鏁板拰鍊?
        if (log.isSaveResponseData() && StringUtils.isNotNull(jsonResult))
        {
            operLog.setJsonResult(StringUtils.substring(JSON.toJSONString(jsonResult), 0, 2000));
        }
    }

    /**
     * 鑾峰彇璇锋眰鐨勫弬鏁帮紝鏀惧埌log涓?
     * 
     * @param operLog 鎿嶄綔鏃ュ織
     * @throws Exception 寮傚父
     */
    private void setRequestValue(JoinPoint joinPoint, SysOperLog operLog, String[] excludeParamNames) throws Exception
    {
        Map<?, ?> paramsMap = ServletUtils.getParamMap(ServletUtils.getRequest());
        String requestMethod = operLog.getRequestMethod();
        if (StringUtils.isEmpty(paramsMap)
                && (HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod)))
        {
            String params = argsArrayToString(joinPoint.getArgs(), excludeParamNames);
            operLog.setOperParam(StringUtils.substring(params, 0, 2000));
        }
        else
        {
            operLog.setOperParam(StringUtils.substring(JSON.toJSONString(paramsMap, excludePropertyPreFilter(excludeParamNames)), 0, 2000));
        }
    }

    /**
     * 鍙傛暟鎷艰
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames)
    {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0)
        {
            for (Object o : paramsArray)
            {
                if (StringUtils.isNotNull(o) && !isFilterObject(o))
                {
                    try
                    {
                        String jsonObj = JSON.toJSONString(o, excludePropertyPreFilter(excludeParamNames));
                        params += jsonObj.toString() + " ";
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }
        return params.trim();
    }

    /**
     * 蹇界暐鏁忔劅灞炴€?
     */
    public PropertyPreExcludeFilter excludePropertyPreFilter(String[] excludeParamNames)
    {
        return new PropertyPreExcludeFilter().addExcludes(ArrayUtils.addAll(EXCLUDE_PROPERTIES, excludeParamNames));
    }

    /**
     * 鍒ゆ柇鏄惁闇€瑕佽繃婊ょ殑瀵硅薄銆?
     * 
     * @param o 瀵硅薄淇℃伅銆?
     * @return 濡傛灉鏄渶瑕佽繃婊ょ殑瀵硅薄锛屽垯杩斿洖true锛涘惁鍒欒繑鍥瀎alse銆?
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o)
    {
        Class<?> clazz = o.getClass();
        if (clazz.isArray())
        {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        }
        else if (Collection.class.isAssignableFrom(clazz))
        {
            Collection collection = (Collection) o;
            for (Object value : collection)
            {
                return value instanceof MultipartFile;
            }
        }
        else if (Map.class.isAssignableFrom(clazz))
        {
            Map map = (Map) o;
            for (Object value : map.entrySet())
            {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}


