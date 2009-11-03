/**
 * Copyright (c) 2005-2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Aptana Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 *
 * Redistribution, except as permitted by the above license, is prohibited.
 * Any modifications to this file must keep this entire header intact.
 */

package com.phonegap.preferences;

/**
 * Item represents and Name Value Pair.
 * @author Joelle Lam
 * @author Kevin Sawicki (ksawicki@aptana.com)
 */
public class Item
{

	private String name;
	private String location;

	/**
	 * Creates a new item
	 * 
	 * @param name
	 * @param location
	 */
	public Item(String name, String location)
	{
		this.name = name;
		this.location = location;
	}

	/**
	 * Gets the location
	 * 
	 * @return - location
	 */
	public String getLocation()
	{
		return location;
	}

	/**
	 * Sets the location
	 * 
	 * @param location
	 */
	public void setLocation(String location)
	{
		this.location = location;
	}

	/**
	 * Gets the name
	 * 
	 * @return - name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name
	 * 
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
}
