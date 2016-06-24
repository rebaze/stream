/*
 * Copyright (c) 2015 rebaze GmbH
 * All rights reserved.
 *
 * This library and the accompanying materials are made available under the terms of the Apache License Version 2.0,
 * which accompanies this distribution and is available at http://www.apache.org/licenses/LICENSE-2.0.
 *
 */
package com.rebaze.tree.api;

/**
 *
 * @author Toni Menzel <toni.menzel@rebaze.com>
 *
 */
public interface TreeSessionFactory
{    
	/**
	 * 
	 * @param digestAlo
	 * @return
	 */
    TreeSession getTreeSession(HashAlgorithm digestAlo);
}
