package com.rebaze.analytics.license;

import com.rebaze.analytics.license.provider.License;

public class UnknownLicense extends License {
	public UnknownLicense(String s) {
		super(s);
	}
	
	public String name;
}
