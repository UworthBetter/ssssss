package com.ueit.common.exception.user;

/**
 * 鐢ㄦ埛涓嶅瓨鍦ㄥ紓甯哥被
 * 
 * @author ruoyi
 */
public class UserNotExistsException extends UserException
{
    private static final long serialVersionUID = 1L;

    public UserNotExistsException()
    {
        super("user.not.exists", null);
    }
}
