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

package org.uguess.birt.report.engine.emitter.rtf;


/**
 * RtfConstants
 */
public interface RtfConstants
{

	// Constants for supported image type
	int DIB = 1;
	int PNG = 2;
	int JPEG = 3;

	// Constants for common rtf directive
	byte[] DOC_HEADER = "{\\rtf1\\ansi\\ansicpg1252".getBytes( ); //$NON-NLS-1$
	byte[] OPEN_GROUP = "{".getBytes( ); //$NON-NLS-1$
	byte[] CLOSE_GROUP = "}".getBytes( ); //$NON-NLS-1$
	byte[] QUADRI_CLOSE_GROUP = "}}}}".getBytes( ); //$NON-NLS-1$
	byte[] PENTA_CLOSE_GROUP = "}}}}}".getBytes( ); //$NON-NLS-1$
	byte[] OPEN_SECT = "\n\\sectd\\linex0\\sbknone".getBytes( ); //$NON-NLS-1$
	byte[] OPEN_SECT_LANDSCAPE = "\n\\sectd\\linex0\\lndscpsxn\\sbknone".getBytes( ); //$NON-NLS-1$
	byte[] CLOSE_SECT = "\\sect".getBytes( ); //$NON-NLS-1$
	byte[] OPEN_PARAGRAPH = "\n{\\pard\\pvpg\\phpg".getBytes( ); //$NON-NLS-1$
	byte[] CLOSE_PARAGRAPH = "\\par}".getBytes( ); //$NON-NLS-1$

	byte[] GENERATOR = ( "{\\*\\generator Tribix v" //$NON-NLS-1$
			+ Activator.getVersion( ) + "}" ).getBytes( ); //$NON-NLS-1$

	byte[] PAGE_WIDTH = "\\paperw".getBytes( ); //$NON-NLS-1$
	byte[] PAGE_HEIGHT = "\\paperh".getBytes( ); //$NON-NLS-1$
	byte[] DEFAULT_PAGE_SETTING = "\\gutter0\\windowctrl\\ftnbj\\viewkind1\\viewscale100".getBytes( ); //$NON-NLS-1$

	byte[] MARGIN_LEFT = "\\margl".getBytes( ); //$NON-NLS-1$
	byte[] MARGIN_RIGHT = "\\margr".getBytes( ); //$NON-NLS-1$
	byte[] MARGIN_TOP = "\\margt".getBytes( ); //$NON-NLS-1$
	byte[] MARGIN_BOTTOM = "\\margb".getBytes( ); //$NON-NLS-1$

	byte[] FONT_TABLE = "\n\\deff0{\\fonttbl".getBytes( ); //$NON-NLS-1$
	byte[] FONT = "\\f".getBytes( ); //$NON-NLS-1$
	byte[] UNDERLINE = "\\ul".getBytes( ); //$NON-NLS-1$
	byte[] STRIKE = "\\strike".getBytes( ); //$NON-NLS-1$
	byte[] BOLD = "\\b".getBytes( ); //$NON-NLS-1$
	byte[] ITALIC = "\\i".getBytes( ); //$NON-NLS-1$
	byte[] FONT_SIZE = "\\fs".getBytes( ); //$NON-NLS-1$
	byte[] FONT_COLOR = "\\cf".getBytes( ); //$NON-NLS-1$
	byte[] DEFAULT_FONT_SETTING = "\\froman\\fcharset0 ".getBytes( ); //$NON-NLS-1$

	byte[] COLOR_TABLE = "\n{\\colortbl".getBytes( ); //$NON-NLS-1$
	byte[] RED = "\\red".getBytes( ); //$NON-NLS-1$
	byte[] GREEN = "\\green".getBytes( ); //$NON-NLS-1$
	byte[] BLUE = "\\blue".getBytes( ); //$NON-NLS-1$

	byte[] ZERO = "0".getBytes( ); //$NON-NLS-1$
	byte[] NEW_LINE = "\n".getBytes( ); //$NON-NLS-1$
	byte[] NEW_PAGE = "\\page".getBytes( ); //$NON-NLS-1$
	byte[] SPACE_DELIMETER = " ".getBytes( ); //$NON-NLS-1$
	byte[] SEMICOLON_DELIMETER = ";".getBytes( ); //$NON-NLS-1$

}
