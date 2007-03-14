
/*******************************************************************************
 * Copyright (c) 2004, 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/
package org.eclipse.birt.data.engine.olap.script;

import java.util.HashMap;
import java.util.List;

import javax.olap.cursor.DimensionCursor;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


/**
 * 
 */
public class JSDimensionObject extends ScriptableObject
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap levels;
	
	JSDimensionObject( List levelNames, List dimensionCursor )
	{
		assert levelNames.size( ) == dimensionCursor.size( );
		
		this.levels = new HashMap();
		for( int i = 0; i < levelNames.size( ); i++ )
		{
			this.levels.put( levelNames.get( i ), new JSLevelObject((DimensionCursor)dimensionCursor.get( i )) );
		}
	}
	
	public String getClassName( )
	{
		return "JSDimensionObject";
	}

	/*
	 * @see org.mozilla.javascript.ScriptableObject#get(java.lang.String,
	 *      org.mozilla.javascript.Scriptable)
	 */
	public Object get( String name, Scriptable start )
	{
		return this.levels.get( name );
	}
}
