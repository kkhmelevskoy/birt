/********************************************************************************
 * (C) Copyright 2000-2008, by Shawn Qualia.
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
import java.util.List;

import org.eclipse.birt.report.engine.nLayout.area.IArea;
import org.uguess.birt.report.engine.layout.wrapper.Style;

/**
 * MultiAreaFrame
 */
public class MultiAreaFrame extends AreaFrame
{

	private List<IArea> subAreas;
	private IArea clip;

	/**
	 * All coords here should be converted to absolute coords.
	 */
	private int x1, x2, y1, y2;

	public MultiAreaFrame( IArea area )
	{
		super( area );

		refreshBounds( null );
	}

	public void setClip( IArea clipArea )
	{
		this.clip = clipArea;

		refreshClip( );
	}

	public void addSubArea( IArea area )
	{
		if ( area == null )
		{
			return;
		}

		if ( subAreas == null )
		{
			subAreas = new ArrayList<IArea>( );
		}
		subAreas.add( area );

		refreshStyle( area );

		refreshBounds( area );
	}

	private void refreshStyle( IArea subArea )
	{
		Style subStyle = AreaStyle.populate( subArea );

		if ( subStyle != null )
		{
			if ( this.style == null )
			{
				this.style = subStyle;
			}
			else
			{
				this.style = LocalStyle.create( new Style[]{
						this.style, subStyle
				} );
			}
		}
	}

	private void refreshClip( )
	{
		if ( clip != null )
		{
			x1 = Math.max( x1, clip.getX( ) );
			y1 = Math.max( y1, clip.getY( ) );

			x2 = Math.min( x2, clip.getX( ) + clip.getWidth( ) );
			y2 = Math.min( y2, clip.getY( ) + clip.getHeight( ) );
		}
	}

	private void refreshBounds( IArea subArea )
	{
		if ( subArea == null )
		{
			x1 = area.getX( );
			x2 = x1 + area.getWidth( );

			y1 = area.getY( );
			y2 = y1 + area.getHeight( );
		}
		else
		{
			if ( subArea.getX( ) < x1 )
			{
				x1 = subArea.getX( );
			}
			if ( subArea.getX( ) + subArea.getWidth( ) > x2 )
			{
				x2 = subArea.getX( ) + subArea.getWidth( );
			}
			if ( subArea.getY( ) < y1 )
			{
				y1 = subArea.getY( );
			}
			if ( subArea.getY( ) + subArea.getHeight( ) > y2 )
			{
				y2 = subArea.getY( ) + subArea.getHeight( );
			}
		}

		refreshClip( );
	}

	public int getChildCount( )
	{
		if ( children == null )
		{
			return 0;
		}
		return children.size( );
	}

	public void removeAllChildren( )
	{
		if ( children != null )
		{
			children.clear( );
			children = null;
		}
	}

	@Override
	public Object getData( )
	{
		if ( subAreas == null || subAreas.size( ) == 0 )
		{
			return area;
		}

		List<IArea> data = new ArrayList<IArea>( );
		data.add( area );
		data.addAll( subAreas );

		return data;
	}

	public int getBottom( )
	{
		return y2 + getYOffset( );
	}

	public int getLeft( )
	{
		return x1 + getXOffset( );
	}

	public int getRight( )
	{
		return x2 + getXOffset( );
	}

	public int getTop( )
	{
		return y1 + getYOffset( );
	}

	@Override
	public int getXOffset( )
	{
		return ( parent instanceof AreaFrame ) ? ( (AreaFrame) parent ).getXOffset( )
				: xOff;
	}

	@Override
	public int getYOffset( )
	{
		return ( parent instanceof AreaFrame ) ? ( (AreaFrame) parent ).getYOffset( )
				: yOff;
	}
}