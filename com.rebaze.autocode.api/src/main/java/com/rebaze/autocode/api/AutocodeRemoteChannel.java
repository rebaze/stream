package com.rebaze.autocode.api;

import java.rmi.Remote;


import java.rmi.RemoteException;

/**
 * Some api for inter process communication.
 */
public interface AutocodeRemoteChannel extends Remote
{
    void progress(String s) throws RemoteException;
}
