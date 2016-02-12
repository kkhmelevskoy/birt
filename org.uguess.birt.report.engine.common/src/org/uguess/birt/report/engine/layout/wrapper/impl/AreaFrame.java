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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.report.engine.nLayout.area.IArea;
import org.uguess.birt.report.engine.layout.wrapper.Frame;
import org.uguess.birt.report.engine.layout.wrapper.Style;

/**
 * A basic frame implementation wraps the IArea object.
 */
public class AreaFrame implements Frame
{

	protected IArea area;
	protected Frame parent;

	protected Style style;

	protected List<Frame> children;

	protected int xOff, yOff;

	public AreaFrame( IArea area )
	{
		this.area = area;

		this.style = AreaStyle.populate( area );
	}

	public Iterator<Frame> iterator( )
	{
		if ( children == null )
		{
			List<Frame> emptyList = Collections.emptyList( );
			return emptyList.iterator( );
		}
		return children.iterator( );
	}

	public Object getData( )
	{
		return area;
	}

	public Style getStyle( )
	{
		return style;
	}

	public void addChild( Frame child )
	{
		if ( children == null )
		{
			children = new ArrayList<Frame>( );
		}
		children.add( child );
	}

	public void setParent( Frame parent )
	{
		this.parent = parent;
	}

	public Frame getParent( )
	{
		return parent;
	}

	public int getLeft( )
	{
		if ( parent != null )
		{
			return parent.getLeft( ) + area.getX( ) + xOff;
		}
		return area.getX( ) + xOff;
	}

	public int getTop( )
	{
		if ( parent != null )
		{
			return parent.getTop( ) + area.getY( ) + yOff;
		}
		return area.getY( ) + yOff;
	}

	public int getRight( )
	{
		return getLeft( ) + area.getWidth( );
	}

	public int getBottom( )
	{
		return getTop( ) + area.getHeight( );
	}

	public void setXOffset( int value )
	{
		xOff = value;
	}

	public void setYOffset( int value )
	{
		yOff = value;
	}

	public int getXOffset( )
	{
		return xOff;
	}

	public int getYOffset( )
	{
		return yOff;
	}

	@Override
	public String toString( )
	{
		return "Data: " //$NON-NLS-1$
				+ area
				+ ", Left: " //$NON-NLS-1$
				+ getLeft( )
				+ ", Right: " //$NON-NLS-1$
				+ getRight( )
				+ ", Top: " //$NON-NLS-1$
				+ getTop( )
				+ ", Bottom: " //$NON-NLS-1$
				+ getBottom( );
	}

}