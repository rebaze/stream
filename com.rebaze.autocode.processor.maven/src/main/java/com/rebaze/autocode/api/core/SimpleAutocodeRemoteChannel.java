package com.rebaze.autocode.api.core;

import com.rebaze.autocode.api.AutocodeRemoteChannel;

import java.io.Serializable;

/**
 * Created by tonit on 23/11/15.
 */
public class SimpleAutocodeRemoteChannel implements AutocodeRemoteChannel, Serializable
{

    @Override public void progress( String s )
    {
        System.out.println(s);
    }
}
