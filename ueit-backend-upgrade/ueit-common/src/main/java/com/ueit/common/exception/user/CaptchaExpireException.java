package com.ueit.common.exception.user;

/**
 * 楠岃瘉鐮佸け鏁堝紓甯哥被
 * 
 * @author ruoyi
 */
public class CaptchaExpireException extends UserException
{
    private static final long serialVersionUID = 1L;

    public CaptchaExpireException()
    {
        super("user.jcaptcha.expire", null);
    }
}
