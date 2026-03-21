package com.qkyd.common.core.domain.model;

/**
 * 鐢ㄦ埛鐧诲綍瀵硅薄
 * 
 * @author qkyd
 */
public class LoginBody
{
    /**
     * 鐢ㄦ埛鍚?
     */
    private String username;

    /**
     * 鐢ㄦ埛瀵嗙爜
     */
    private String password;

    /**
     * 楠岃瘉鐮?
     */
    private String code;

    /**
     * 鍞竴鏍囪瘑
     */
    private String uuid;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }
}


