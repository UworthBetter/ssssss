package com.qkyd.common.constant;

import io.jsonwebtoken.Claims;

/**
 * 閫氱敤甯搁噺淇℃伅
 * 
 * @author qkyd
 */
public class Constants
{
    /**
     * UTF-8 瀛楃闆?
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 瀛楃闆?
     */
    public static final String GBK = "GBK";

    /**
     * www涓诲煙
     */
    public static final String WWW = "www.";

    /**
     * http璇锋眰
     */
    public static final String HTTP = "http://";

    /**
     * https璇锋眰
     */
    public static final String HTTPS = "https://";

    /**
     * 閫氱敤鎴愬姛鏍囪瘑
     */
    public static final String SUCCESS = "0";

    /**
     * 閫氱敤澶辫触鏍囪瘑
     */
    public static final String FAIL = "1";

    /**
     * 鐧诲綍鎴愬姛
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 娉ㄩ攢
     */
    public static final String LOGOUT = "Logout";

    /**
     * 娉ㄥ唽
     */
    public static final String REGISTER = "Register";

    /**
     * 鐧诲綍澶辫触
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 鎵€鏈夋潈闄愭爣璇?
     */
    public static final String ALL_PERMISSION = "*:*:*";

    /**
     * 绠＄悊鍛樿鑹叉潈闄愭爣璇?
     */
    public static final String SUPER_ADMIN = "admin";

    /**
     * 瑙掕壊鏉冮檺鍒嗛殧绗?
     */
    public static final String ROLE_DELIMETER = ",";

    /**
     * 鏉冮檺鏍囪瘑鍒嗛殧绗?
     */
    public static final String PERMISSION_DELIMETER = ",";

    /**
     * 楠岃瘉鐮佹湁鏁堟湡锛堝垎閽燂級
     */
    public static final Integer CAPTCHA_EXPIRATION = 2;

    /**
     * 浠ょ墝
     */
    public static final String TOKEN = "token";

    /**
     * 浠ょ墝鍓嶇紑
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 浠ょ墝鍓嶇紑
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /**
     * 鐢ㄦ埛ID
     */
    public static final String JWT_USERID = "userid";

    /**
     * 鐢ㄦ埛鍚嶇О
     */
    public static final String JWT_USERNAME = Claims.SUBJECT;

    /**
     * 鐢ㄦ埛澶村儚
     */
    public static final String JWT_AVATAR = "avatar";

    /**
     * 鍒涘缓鏃堕棿
     */
    public static final String JWT_CREATED = "created";

    /**
     * 鐢ㄦ埛鏉冮檺
     */
    public static final String JWT_AUTHORITIES = "authorities";

    /**
     * 璧勬簮鏄犲皠璺緞 鍓嶇紑
     */
    public static final String RESOURCE_PREFIX = "/profile";

    /**
     * RMI 杩滅▼鏂规硶璋冪敤
     */
    public static final String LOOKUP_RMI = "rmi:";

    /**
     * LDAP 杩滅▼鏂规硶璋冪敤
     */
    public static final String LOOKUP_LDAP = "ldap:";

    /**
     * LDAPS 杩滅▼鏂规硶璋冪敤
     */
    public static final String LOOKUP_LDAPS = "ldaps:";

    /**
     * 鑷姩璇嗗埆json瀵硅薄鐧藉悕鍗曢厤缃紙浠呭厑璁歌В鏋愮殑鍖呭悕锛岃寖鍥磋秺灏忚秺瀹夊叏锛?
     */
    public static final String[] JSON_WHITELIST_STR = { "org.springframework", "com.qkyd" };

    /**
     * 瀹氭椂浠诲姟鐧藉悕鍗曢厤缃紙浠呭厑璁歌闂殑鍖呭悕锛屽鍏朵粬闇€瑕佸彲浠ヨ嚜琛屾坊鍔狅級
     */
    public static final String[] JOB_WHITELIST_STR = { "com.qkyd" };

    /**
     * 瀹氭椂浠诲姟杩濊鐨勫瓧绗?
     */
    public static final String[] JOB_ERROR_STR = { "java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
            "org.springframework", "org.apache", "com.qkyd.common.utils.file", "com.qkyd.common.config" };
}


