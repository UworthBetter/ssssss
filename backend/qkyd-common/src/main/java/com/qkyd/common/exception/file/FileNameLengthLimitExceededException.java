package com.qkyd.common.exception.file;

/**
 * 鏂囦欢鍚嶇О瓒呴暱闄愬埗寮傚父绫?
 * 
 * @author qkyd
 */
public class FileNameLengthLimitExceededException extends FileException
{
    private static final long serialVersionUID = 1L;

    public FileNameLengthLimitExceededException(int defaultFileNameLength)
    {
        super("upload.filename.exceed.length", new Object[] { defaultFileNameLength });
    }
}


