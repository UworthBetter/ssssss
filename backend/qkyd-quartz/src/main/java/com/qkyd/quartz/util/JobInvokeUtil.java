package com.qkyd.quartz.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import com.qkyd.common.utils.StringUtils;
import com.qkyd.common.utils.spring.SpringUtils;
import com.qkyd.quartz.domain.SysJob;

/**
 * 浠诲姟鎵ц宸ュ叿
 *
 * @author qkyd
 */
public class JobInvokeUtil
{
    /**
     * 鎵ц鏂规硶
     *
     * @param sysJob 绯荤粺浠诲姟
     */
    public static void invokeMethod(SysJob sysJob) throws Exception
    {
        String invokeTarget = sysJob.getInvokeTarget();
        String beanName = getBeanName(invokeTarget);
        String methodName = getMethodName(invokeTarget);
        List<Object[]> methodParams = getMethodParams(invokeTarget);

        if (!isValidClassName(beanName))
        {
            Object bean = SpringUtils.getBean(beanName);
            invokeMethod(bean, methodName, methodParams);
        }
        else
        {
            Object bean = Class.forName(beanName).getDeclaredConstructor().newInstance();
            invokeMethod(bean, methodName, methodParams);
        }
    }

    /**
     * 璋冪敤浠诲姟鏂规硶
     *
     * @param bean 鐩爣瀵硅薄
     * @param methodName 鏂规硶鍚嶇О
     * @param methodParams 鏂规硶鍙傛暟
     */
    private static void invokeMethod(Object bean, String methodName, List<Object[]> methodParams)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException
    {
        if (StringUtils.isNotNull(methodParams) && methodParams.size() > 0)
        {
            Method method = bean.getClass().getMethod(methodName, getMethodParamsType(methodParams));
            method.invoke(bean, getMethodParamsValue(methodParams));
        }
        else
        {
            Method method = bean.getClass().getMethod(methodName);
            method.invoke(bean);
        }
    }

    /**
     * 鏍￠獙鏄惁涓轰负class鍖呭悕
     * 
     * @param invokeTarget 鍚嶇О
     * @return true鏄?false鍚?
     */
    public static boolean isValidClassName(String invokeTarget)
    {
        return StringUtils.countMatches(invokeTarget, ".") > 1;
    }

    /**
     * 鑾峰彇bean鍚嶇О
     * 
     * @param invokeTarget 鐩爣瀛楃涓?
     * @return bean鍚嶇О
     */
    public static String getBeanName(String invokeTarget)
    {
        String beanName = StringUtils.substringBefore(invokeTarget, "(");
        return StringUtils.substringBeforeLast(beanName, ".");
    }

    /**
     * 鑾峰彇bean鏂规硶
     * 
     * @param invokeTarget 鐩爣瀛楃涓?
     * @return method鏂规硶
     */
    public static String getMethodName(String invokeTarget)
    {
        String methodName = StringUtils.substringBefore(invokeTarget, "(");
        return StringUtils.substringAfterLast(methodName, ".");
    }

    /**
     * 鑾峰彇method鏂规硶鍙傛暟鐩稿叧鍒楄〃
     * 
     * @param invokeTarget 鐩爣瀛楃涓?
     * @return method鏂规硶鐩稿叧鍙傛暟鍒楄〃
     */
    public static List<Object[]> getMethodParams(String invokeTarget)
    {
        String methodStr = StringUtils.substringBetween(invokeTarget, "(", ")");
        if (StringUtils.isEmpty(methodStr))
        {
            return null;
        }
        String[] methodParams = methodStr.split(",(?=([^\"']*[\"'][^\"']*[\"'])*[^\"']*$)");
        List<Object[]> classs = new LinkedList<>();
        for (int i = 0; i < methodParams.length; i++)
        {
            String str = StringUtils.trimToEmpty(methodParams[i]);
            // String瀛楃涓茬被鍨嬶紝浠?鎴?寮€澶?
            if (StringUtils.startsWithAny(str, "'", "\""))
            {
                classs.add(new Object[] { StringUtils.substring(str, 1, str.length() - 1), String.class });
            }
            // boolean甯冨皵绫诲瀷锛岀瓑浜巘rue鎴栬€協alse
            else if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str))
            {
                classs.add(new Object[] { Boolean.valueOf(str), Boolean.class });
            }
            // long闀挎暣褰紝浠缁撳熬
            else if (StringUtils.endsWith(str, "L"))
            {
                classs.add(new Object[] { Long.valueOf(StringUtils.substring(str, 0, str.length() - 1)), Long.class });
            }
            // double娴偣绫诲瀷锛屼互D缁撳熬
            else if (StringUtils.endsWith(str, "D"))
            {
                classs.add(new Object[] { Double.valueOf(StringUtils.substring(str, 0, str.length() - 1)), Double.class });
            }
            // 鍏朵粬绫诲瀷褰掔被涓烘暣褰?
            else
            {
                classs.add(new Object[] { Integer.valueOf(str), Integer.class });
            }
        }
        return classs;
    }

    /**
     * 鑾峰彇鍙傛暟绫诲瀷
     * 
     * @param methodParams 鍙傛暟鐩稿叧鍒楄〃
     * @return 鍙傛暟绫诲瀷鍒楄〃
     */
    public static Class<?>[] getMethodParamsType(List<Object[]> methodParams)
    {
        Class<?>[] classs = new Class<?>[methodParams.size()];
        int index = 0;
        for (Object[] os : methodParams)
        {
            classs[index] = (Class<?>) os[1];
            index++;
        }
        return classs;
    }

    /**
     * 鑾峰彇鍙傛暟鍊?
     * 
     * @param methodParams 鍙傛暟鐩稿叧鍒楄〃
     * @return 鍙傛暟鍊煎垪琛?
     */
    public static Object[] getMethodParamsValue(List<Object[]> methodParams)
    {
        Object[] classs = new Object[methodParams.size()];
        int index = 0;
        for (Object[] os : methodParams)
        {
            classs[index] = (Object) os[0];
            index++;
        }
        return classs;
    }
}


