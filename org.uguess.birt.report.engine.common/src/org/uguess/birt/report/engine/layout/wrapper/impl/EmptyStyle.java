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

import java.awt.Color;

import org.uguess.birt.report.engine.layout.wrapper.Style;

/**
 * EmptyStyle
 */
public final class EmptyStyle implements Style
{

	public Color getBackgroundColor( )
	{
		return null;
	}

	public String getBackgroundImage( )
	{
		return null;
	}

	public String getBackgroundRepeat( )
	{
		return null;
	}

	public Color getBottomBorderColor( )
	{
		return null;
	}

	public String getBottomBorderStyle( )
	{
		return null;
	}

	public int getBottomBorderWidth( )
	{
		return 0;
	}

	public Color getColor( )
	{
		return null;
	}

	public String getFontFamily( )
	{
		return null;
	}

	public int getFontSize( )
	{
		return 0;
	}

	public String getFontStyle( )
	{
		return null;
	}

	public String getFontWeight( )
	{
		return null;
	}

	public Color getLeftBorderColor( )
	{
		return null;
	}

	public String getLeftBorderStyle( )
	{
		return null;
	}

	public int getLeftBorderWidth( )
	{
		return 0;
	}

	public Color getRightBorderColor( )
	{
		return null;
	}

	public String getRightBorderStyle( )
	{
		return null;
	}

	public int getRightBorderWidth( )
	{
		return 0;
	}

	public boolean isTextLineThrough( )
	{
		return false;
	}

	public boolean isTextOverline( )
	{
		return false;
	}

	public boolean isTextUnderline( )
	{
		return false;
	}

	public Color getTopBorderColor( )
	{
		return null;
	}

	public String getTopBorderStyle( )
	{
		return null;
	}

	public int getTopBorderWidth( )
	{
		return 0;
	}

	public int hashCode( )
	{
		return 0;
	}

	public boolean equals( Object obj )
	{
		if ( obj == this )
		{
			return true;
		}

		if ( !( obj instanceof Style ) )
		{
			return false;
		}

		return ( (Style) obj ).isEmpty( );
	}

	public boolean isEmpty( )
	{
		return true;
	}

	public String getTextAlign( )
	{
		return null;
	}

	public String getVerticalAlign( )
	{
		return null;
	}

}
