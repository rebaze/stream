package com.rebaze.autocode.api;

import java.io.File;
import java.io.IOException;

import com.rebaze.autocode.api.core.Effect;

/**
 * High level interface.
 * 
 * @author tonit
 *
 */
public interface Autocode {

	Effect build(File path) throws IOException;

	void modify();

	void share();

}