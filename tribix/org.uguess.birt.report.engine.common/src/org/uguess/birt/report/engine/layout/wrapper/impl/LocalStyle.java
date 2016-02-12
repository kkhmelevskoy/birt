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

import java.awt.Color;

import org.eclipse.birt.report.engine.css.engine.StyleConstants;
import org.uguess.birt.report.engine.layout.wrapper.Style;

/**
 * LocalStyle
 */
public final class LocalStyle implements Style
{

	private Object[] props = new Object[StyleConstants.NUMBER_OF_STYLE];
	private boolean empty = true;

	/**
	 * Wrap and clone the given style to a <code>LocalStyle</code> instance.
	 */
	public static LocalStyle create( Style style )
	{
		if ( style == null )
		{
			return new LocalStyle( );
		}

		if ( style instanceof LocalStyle )
		{
			return createClone( (LocalStyle) style );
		}

		return createClone( wrapToLocal( style ) );
	}

	/**
	 * Wrap the given styles to a <code>LocalStyle</code> instance. The first
	 * element in the array will be the clone source, the other elements will be
	 * the parents and processed in orders.
	 */
	public static LocalStyle create( Style[] styles )
	{
		if ( styles == null || styles.length == 0 )
		{
			return new LocalStyle( );
		}

		LocalStyle ds;

		if ( styles[0] == null )
		{
			ds = new LocalStyle( );
		}
		else if ( styles[0] instanceof LocalStyle )
		{
			ds = createClone( (LocalStyle) styles[0] );
		}
		else
		{
			ds = createClone( wrapToLocal( styles[0] ) );
		}

		if ( styles.length > 1 )
		{
			Style[] parents = new Style[styles.length - 1];
			System.arraycopy( styles, 1, parents, 0, parents.length );

			flattenParentStyles( ds, wrapToLocal( parents ) );
		}

		return ds;
	}

	private static LocalStyle createClone( LocalStyle ss )
	{
		if ( ss == null )
		{
			return null;
		}

		LocalStyle cs = new LocalStyle( );
		cs.empty = ss.empty;
		System.arraycopy( ss.props, 0, cs.props, 0, ss.props.length );

		return cs;
	}

	private static void flattenParentStyles( LocalStyle style,
			LocalStyle[] parents )
	{
		Object value;
		if ( parents != null )
		{
			for ( int j = 0; j < StyleConstants.NUMBER_OF_STYLE; j++ )
			{
				if ( !isInheritable( j ) )
				{
					continue;
				}

				value = style.props[j];

				if ( !isEffectiveValue( j, value ) )
				{
					for ( int i = 0; i < parents.length; i++ )
					{
						if ( parents[i] != null )
						{
							value = parents[i].props[j];

							if ( isEffectiveValue( j, value ) )
							{
								style.setProperty( j, value );
								break;
							}
						}
					}
				}
			}
		}
	}

	private static boolean isInheritable( int sIndex )
	{
		switch ( sIndex )
		{
			case StyleConstants.STYLE_BORDER_BOTTOM_COLOR :
			case StyleConstants.STYLE_BORDER_BOTTOM_STYLE :
			case StyleConstants.STYLE_BORDER_BOTTOM_WIDTH :
			case StyleConstants.STYLE_BORDER_TOP_COLOR :
			case StyleConstants.STYLE_BORDER_TOP_STYLE :
			case StyleConstants.STYLE_BORDER_TOP_WIDTH :
			case StyleConstants.STYLE_BORDER_LEFT_COLOR :
			case StyleConstants.STYLE_BORDER_LEFT_STYLE :
			case StyleConstants.STYLE_BORDER_LEFT_WIDTH :
			case StyleConstants.STYLE_BORDER_RIGHT_COLOR :
			case StyleConstants.STYLE_BORDER_RIGHT_STYLE :
			case StyleConstants.STYLE_BORDER_RIGHT_WIDTH :
				return true;
		}

		return true;
	}

	private static boolean isEffectiveValue( int sIndex, Object value )
	{
		if ( value == null )
		{
			return false;
		}

		switch ( sIndex )
		{
			case StyleConstants.STYLE_FONT_SIZE :
			case StyleConstants.STYLE_BORDER_BOTTOM_WIDTH :
			case StyleConstants.STYLE_BORDER_TOP_WIDTH :
			case StyleConstants.STYLE_BORDER_LEFT_WIDTH :
			case StyleConstants.STYLE_BORDER_RIGHT_WIDTH :

				if ( ( (Number) value ).intValue( ) == 0 )
				{
					return false;
				}
				break;
		}

		return true;
	}

	private static LocalStyle[] wrapToLocal( Style[] ss )
	{
		if ( ss == null )
		{
			return null;
		}

		LocalStyle[] ws = new LocalStyle[ss.length];
		for ( int i = 0; i < ss.length; i++ )
		{
			ws[i] = wrapToLocal( ss[i] );
		}

		return ws;
	}

	private static LocalStyle wrapToLocal( Style ss )
	{
		if ( ss == null || ss instanceof LocalStyle )
		{
			return (LocalStyle) ss;
		}

		LocalStyle ws = new LocalStyle( );

		if ( ss.isEmpty( ) )
		{
			return ws;
		}

		safeSetProperty( ws,
				StyleConstants.STYLE_BACKGROUND_COLOR,
				ss.getBackgroundColor( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_BACKGROUND_IMAGE,
				ss.getBackgroundImage( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_BACKGROUND_REPEAT,
				ss.getBackgroundRepeat( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_BORDER_BOTTOM_COLOR,
				ss.getBottomBorderColor( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_BORDER_BOTTOM_STYLE,
				ss.getBottomBorderStyle( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_BORDER_BOTTOM_WIDTH,
				ss.getBottomBorderWidth( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_BORDER_LEFT_COLOR,
				ss.getLeftBorderColor( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_BORDER_LEFT_STYLE,
				ss.getLeftBorderStyle( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_BORDER_LEFT_WIDTH,
				ss.getLeftBorderWidth( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_BORDER_RIGHT_COLOR,
				ss.getRightBorderColor( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_BORDER_RIGHT_STYLE,
				ss.getRightBorderStyle( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_BORDER_RIGHT_WIDTH,
				ss.getRightBorderWidth( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_BORDER_TOP_COLOR,
				ss.getTopBorderColor( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_BORDER_TOP_STYLE,
				ss.getTopBorderStyle( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_BORDER_TOP_WIDTH,
				ss.getTopBorderWidth( ) );

		safeSetProperty( ws, StyleConstants.STYLE_COLOR, ss.getColor( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_FONT_FAMILY,
				ss.getFontFamily( ) );

		safeSetProperty( ws, StyleConstants.STYLE_FONT_SIZE, ss.getFontSize( ) );

		safeSetProperty( ws, StyleConstants.STYLE_FONT_STYLE, ss.getFontStyle( ) );

		safeSetProperty( ws,
				StyleConstants.STYLE_FONT_WEIGHT,
				ss.getFontWeight( ) );

		if ( ss.isTextLineThrough( ) )
		{
			ws.setProperty( StyleConstants.STYLE_TEXT_LINETHROUGH, Boolean.TRUE );
		}

		if ( ss.isTextOverline( ) )
		{
			ws.setProperty( StyleConstants.STYLE_TEXT_OVERLINE, Boolean.TRUE );
		}

		if ( ss.isTextUnderline( ) )
		{
			ws.setProperty( StyleConstants.STYLE_TEXT_UNDERLINE, Boolean.TRUE );
		}

		safeSetProperty( ws,
				StyleConstants.STYLE_VERTICAL_ALIGN,
				ss.getVerticalAlign( ) );

		safeSetProperty( ws, StyleConstants.STYLE_TEXT_ALIGN, ss.getTextAlign( ) );

		return ws;
	}

	private static void safeSetProperty( LocalStyle style, int index,
			Object value )
	{
		if ( value != null )
		{
			style.setProperty( index, value );
		}
	}

	private LocalStyle( )
	{
	}

	public boolean isEmpty( )
	{
		return empty;
	}

	public int hashCode( )
	{
		int hash = 0;

		if ( !isEmpty( ) )
		{
			for ( int i = 0; i < StyleConstants.NUMBER_OF_STYLE; i++ )
			{
				Object v1 = props[i];

				if ( v1 != null )
				{
					hash += ( i ^ v1.hashCode( ) );
				}
			}
		}

		return hash;
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

		boolean aempty = ( (Style) obj ).isEmpty( );

		if ( empty && aempty )
		{
			return true;
		}

		if ( empty != aempty )
		{
			return false;
		}

		LocalStyle implObj = null;

		if ( obj instanceof LocalStyle )
		{
			implObj = (LocalStyle) obj;
		}
		else
		{
			// wrap to styleImpl instance
			implObj = wrapToLocal( (Style) obj );
		}

		for ( int i = 0; i < StyleConstants.NUMBER_OF_STYLE; i++ )
		{
			Object v1 = props[i];
			Object v2 = implObj.props[i];

			if ( ( v1 == null && v2 != null ) || ( v2 == null && v1 != null ) )
			{
				return false;
			}

			if ( v1 != null && v2 != null && !v1.equals( v2 ) )
			{
				return false;
			}
		}

		return true;
	}

	private String getStringProperty( int index )
	{
		Object value = props[index];

		if ( value instanceof String )
		{
			return (String) value;
		}

		return null;
	}

	private boolean getBooleanProperty( int index )
	{
		Object value = props[index];

		if ( value instanceof Boolean )
		{
			return ( (Boolean) value ).booleanValue( );
		}

		return false;
	}

	private int getIntProperty( int index )
	{
		Object value = props[index];

		if ( value instanceof Number )
		{
			return ( (Number) value ).intValue( );
		}

		return 0;
	}

	// private Object getProperty( int index )
	// {
	// return props[index];
	// }

	public void setProperty( int index, Object value )
	{
		if ( index < 0 || index >= StyleConstants.NUMBER_OF_STYLE )
		{
			return;
		}

		if ( empty )
		{
			empty = false;
		}

		props[index] = value;
	}

	public void revalidate( )
	{
		boolean isEmpty = true;

		for ( int i = 0; i < StyleConstants.NUMBER_OF_STYLE; i++ )
		{
			if ( isEffectiveValue( i, props[i] ) )
			{
				isEmpty = false;
			}
			else
			{
				props[i] = null;
			}
		}

		this.empty = isEmpty;
	}

	public Color getBackgroundColor( )
	{
		return (Color) props[StyleConstants.STYLE_BACKGROUND_COLOR];
	}

	public String getBackgroundImage( )
	{
		return getStringProperty( StyleConstants.STYLE_BACKGROUND_IMAGE );
	}

	public String getBackgroundRepeat( )
	{
		return getStringProperty( StyleConstants.STYLE_BACKGROUND_REPEAT );
	}

	public Color getBottomBorderColor( )
	{
		return (Color) props[StyleConstants.STYLE_BORDER_BOTTOM_COLOR];
	}

	public String getBottomBorderStyle( )
	{
		return getStringProperty( StyleConstants.STYLE_BORDER_BOTTOM_STYLE );
	}

	public int getBottomBorderWidth( )
	{
		return getIntProperty( StyleConstants.STYLE_BORDER_BOTTOM_WIDTH );
	}

	public Color getColor( )
	{
		return (Color) props[StyleConstants.STYLE_COLOR];
	}

	public String getFontFamily( )
	{
		return getStringProperty( StyleConstants.STYLE_FONT_FAMILY );
	}

	public int getFontSize( )
	{
		return getIntProperty( StyleConstants.STYLE_FONT_SIZE );
	}

	public String getFontStyle( )
	{
		return getStringProperty( StyleConstants.STYLE_FONT_STYLE );
	}

	public String getFontWeight( )
	{
		return getStringProperty( StyleConstants.STYLE_FONT_WEIGHT );
	}

	public Color getLeftBorderColor( )
	{
		return (Color) props[StyleConstants.STYLE_BORDER_LEFT_COLOR];
	}

	public String getLeftBorderStyle( )
	{
		return getStringProperty( StyleConstants.STYLE_BORDER_LEFT_STYLE );
	}

	public int getLeftBorderWidth( )
	{
		return getIntProperty( StyleConstants.STYLE_BORDER_LEFT_WIDTH );
	}

	public Color getRightBorderColor( )
	{
		return (Color) props[StyleConstants.STYLE_BORDER_RIGHT_COLOR];
	}

	public String getRightBorderStyle( )
	{
		return getStringProperty( StyleConstants.STYLE_BORDER_RIGHT_STYLE );
	}

	public int getRightBorderWidth( )
	{
		return getIntProperty( StyleConstants.STYLE_BORDER_RIGHT_WIDTH );
	}

	public String getTextAlign( )
	{
		return getStringProperty( StyleConstants.STYLE_TEXT_ALIGN );
	}

	public Color getTopBorderColor( )
	{
		return (Color) props[StyleConstants.STYLE_BORDER_TOP_COLOR];
	}

	public String getTopBorderStyle( )
	{
		return getStringProperty( StyleConstants.STYLE_BORDER_TOP_STYLE );
	}

	public int getTopBorderWidth( )
	{
		return getIntProperty( StyleConstants.STYLE_BORDER_TOP_WIDTH );
	}

	public String getVerticalAlign( )
	{
		return getStringProperty( StyleConstants.STYLE_VERTICAL_ALIGN );
	}

	public boolean isTextLineThrough( )
	{
		return getBooleanProperty( StyleConstants.STYLE_TEXT_LINETHROUGH );
	}

	public boolean isTextOverline( )
	{
		return getBooleanProperty( StyleConstants.STYLE_TEXT_OVERLINE );
	}

	public boolean isTextUnderline( )
	{
		return getBooleanProperty( StyleConstants.STYLE_TEXT_UNDERLINE );
	}
}
