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

package org.uguess.birt.report.engine.layout.wrapper;

import java.awt.Color;

import org.uguess.birt.report.engine.layout.wrapper.impl.EmptyStyle;

/**
 * Style
 */
public interface Style
{
	Style EMPTON = new EmptyStyle( );

	boolean isEmpty( );

	Color getBackgroundColor( );

	String getBackgroundImage( );

	String getBackgroundRepeat( );

	int getLeftBorderWidth( );

	int getRightBorderWidth( );

	int getTopBorderWidth( );

	int getBottomBorderWidth( );

	Color getLeftBorderColor( );

	Color getRightBorderColor( );

	Color getTopBorderColor( );

	Color getBottomBorderColor( );

	String getLeftBorderStyle( );

	String getRightBorderStyle( );

	String getTopBorderStyle( );

	String getBottomBorderStyle( );

	boolean isTextUnderline( );

	boolean isTextOverline( );

	boolean isTextLineThrough( );

	Color getColor( );

	String getFontFamily( );

	int getFontSize( );

	String getFontStyle( );

	String getFontWeight( );

	String getTextAlign( );

	String getVerticalAlign( );
}
