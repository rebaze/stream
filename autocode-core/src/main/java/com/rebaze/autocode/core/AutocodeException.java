package com.rebaze.autocode.core;

/**
 * Created by tonit on 04/11/15.
 */
public class AutocodeException extends RuntimeException
{
    public AutocodeException( String msg)
    {
        super(msg);
    }

    public AutocodeException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
