package com.qkyd.common.exception.file;

/**
 * 鏂囦欢鍚嶅ぇ灏忛檺鍒跺紓甯哥被
 * 
 * @author qkyd
 */
public class FileSizeLimitExceededException extends FileException
{
    private static final long serialVersionUID = 1L;

    public FileSizeLimitExceededException(long defaultMaxSize)
    {
        super("upload.exceed.maxSize", new Object[] { defaultMaxSize });
    }
}


