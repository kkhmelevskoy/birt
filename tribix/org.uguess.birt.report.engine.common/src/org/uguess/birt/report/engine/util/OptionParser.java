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

package org.uguess.birt.report.engine.util;


/**
 * OptionParser
 */
public class OptionParser
{

	/**
	 * Parses given value to String.Any unrecoganizable value will be parsed as
	 * NULL.
	 */
	public static String parseString( Object value )
	{
		if ( value instanceof String )
		{
			return (String) value;
		}
		else if ( value instanceof String[] && ( (String[]) value ).length > 0 )
		{
			return ( (String[]) value )[0];
		}

		return null;
	}

	/**
	 * Parses given value to boolean.Any unrecoganizable value will be parsed as
	 * FALSE.
	 */
	public static boolean parseBoolean( Object value )
	{
		if ( value instanceof Boolean )
		{
			return ( (Boolean) value ).booleanValue( );
		}
		else if ( value instanceof String )
		{
			return Boolean.valueOf( (String) value ).booleanValue( );
		}
		else if ( value instanceof String[] && ( (String[]) value ).length > 0 )
		{
			return Boolean.valueOf( ( (String[]) value )[0] ).booleanValue( );
		}

		return false;
	}

	/**
	 * Parses given value to int. Any unrecoganizable value will be parsed as
	 * -1.
	 */
	public static int parseInt( Object value )
	{
		if ( value instanceof Integer )
		{
			return ( (Integer) value ).intValue( );
		}
		else if ( value instanceof String )
		{
			try
			{
				return Integer.parseInt( (String) value );
			}
			catch ( NumberFormatException e )
			{
			}
		}
		else if ( value instanceof String[] && ( (String[]) value ).length > 0 )
		{
			try
			{
				return Integer.parseInt( ( (String[]) value )[0] );
			}
			catch ( NumberFormatException e )
			{
			}
		}

		return -1;
	}

}
