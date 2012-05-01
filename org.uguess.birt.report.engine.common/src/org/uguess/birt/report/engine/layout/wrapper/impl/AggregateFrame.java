/********************************************************************************
 * (C) Copyright 2000-2005, by Shawn Qualia.
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 ********************************************************************************/

package org.uguess.birt.report.engine.layout.wrapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.uguess.birt.report.engine.layout.wrapper.Frame;
import org.uguess.birt.report.engine.layout.wrapper.Style;

/**
 * A special frame implementation used to aggregate other frames only.
 */
public final class AggregateFrame implements Frame
{

	private List<Frame> children;

	private int left, right, top, bottom;

	private HashMap<String, Object> attrs;

	public AggregateFrame( )
	{
		children = new ArrayList<Frame>( );
	}

	public Iterator<Frame> iterator( )
	{
		return children.iterator( );
	}

	public Object getData( )
	{
		return null;
	}

	public Style getStyle( )
	{
		return Style.EMPTON;
	}

	public void addChild( Frame child )
	{
		children.add( child );
	}

	public Frame getParent( )
	{
		return null;
	}

	public int getLeft( )
	{
		return left;
	}

	public int getTop( )
	{
		return top;
	}

	public int getRight( )
	{
		return right;
	}

	public int getBottom( )
	{
		return bottom;
	}

	public void setLeft( int value )
	{
		left = value;
	}

	public void setTop( int value )
	{
		top = value;
	}

	public void setRight( int value )
	{
		right = value;
	}

	public void setBottom( int value )
	{
		bottom = value;
	}

	public Object getAttribute( String key )
	{
		if ( attrs != null )
		{
			return attrs.get( key );
		}
		return null;
	}

	public void putAttribute( String key, Object value )
	{
		if ( attrs == null )
		{
			attrs = new HashMap<String, Object>( );
		}
		attrs.put( key, value );
	}

}