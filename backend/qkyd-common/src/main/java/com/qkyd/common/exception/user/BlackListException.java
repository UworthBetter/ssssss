package com.qkyd.common.exception.user;

/**
 * 榛戝悕鍗旾P寮傚父绫?
 * 
 * @author qkyd
 */
public class BlackListException extends UserException
{
    private static final long serialVersionUID = 1L;

    public BlackListException()
    {
        super("login.blocked", null);
    }
}


