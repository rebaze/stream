package com.rebaze.analytics.license.provider;

public abstract class License {
	public License() {
	}
	
	public License(String source) {
		this.source = source;
	}
	
	public String source = null;

}
