package com.rebaze.autocode.internal;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.rebaze.tree.api.TreeSession;

public class Activator implements BundleActivator
{
	@Override
    public void start(BundleContext bc) throws Exception
    {
        Hashtable props = new Hashtable();
        bc.registerService(
            TreeSession.class.getName(), new TreeSession(), props);
    }

	@Override
	public void stop(BundleContext context) throws Exception {		
	}
}
