package com.qkyd.health.utils;


import cn.hutool.crypto.SecureUtil;
import com.qkyd.common.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ParamsSignUtils {

    public static void main(String[] args) {
        HashMap<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("action", "heartbeat");
        objectObjectHashMap.put("imei", "9618010875");
        objectObjectHashMap.put("reversal", "71");
        objectObjectHashMap.put("step", "0");
        objectObjectHashMap.put("volt", "71");
        objectObjectHashMap.put("time", "1703042957");
        objectObjectHashMap.put("hash", "29ea69eacf356e8a92a72615b3f20a90");
        boolean b = checkSign(objectObjectHashMap);
        System.out.println(1);
    }

    /**
     * 校验签名
     * @param map 待验证签名数据
     * @return true 校验通过，false 校验不通过
     */
    public static boolean checkSign(Map<String, Object> map){

        // map 按照key排序
        map = new TreeMap<>(map);

        String time = "";
        String hash = "";
        String paramsStr = "";
        for(Map.Entry<String, Object> entry : map.entrySet()){

            if(StringUtils.isEmpty(time) && entry.getKey().equals("time")){
                /**
                 * 客户名称：河南优易信息
                 * appid：658162C5CF2A5
                 * secret：1A0426E3AE4D324B5E28DA0F03E3C884
                 */
                /**
                 * 客户名称：河南优易信息数据对接
                 * appid：65825CEEE5C01
                 * secret：7AD91F534FD208E8EFA1C1FCBECF88D6
                 */
                time = entry.getKey() + "=" + entry.getValue() + "&secret=7AD91F534FD208E8EFA1C1FCBECF88D6";
                continue;
            }

            if(StringUtils.isEmpty(hash) && entry.getKey().equals("hash")){
                hash = entry.getValue().toString();
                continue;
            }

            paramsStr += entry.getKey() + "=" + entry.getValue() + "&";
        }

        String md5Hash = SecureUtil.md5(paramsStr + time);

        return hash.equals(md5Hash);
    }

}

