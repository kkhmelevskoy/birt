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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * ImageUtil
 */
public class ImageUtil
{

	/**
	 * Constant for Unknown image type
	 */
	public static final int TYPE_UNKNOWN = -1;
	/**
	 * Constant for DIB image type
	 */
	public static final int TYPE_DIB = 1;
	/**
	 * Constant for BMP image type
	 */
	public static final int TYPE_BMP = TYPE_DIB;
	/**
	 * Constant for PNG image type
	 */
	public static final int TYPE_PNG = 2;
	/**
	 * Constant for JPEG image type
	 */
	public static final int TYPE_JPEG = 3;

	/**
	 * Returns the image type by given binary data.
	 * 
	 * @param data
	 *            The binary image data
	 * @return The image type
	 */
	public static int getImageType( byte[] data )
	{
		if ( data == null )
		{
			return TYPE_UNKNOWN;
		}

		if ( data.length > 2
				&& ( data[0] & 0xff ) == 'B'
				&& ( data[1] & 0xff ) == 'M' )
		{
			return TYPE_DIB;
		}

		if ( data.length > 4
				&& ( data[0] & 0xff ) == 0x89
				&& ( data[1] & 0xff ) == 'P'
				&& ( data[2] & 0xff ) == 'N'
				&& ( data[3] & 0xff ) == 'G' )
		{
			return TYPE_PNG;
		}

		if ( data.length > 4
				&& ( data[0] & 0xff ) == 0xff
				&& ( data[1] & 0xff ) == 0xd8
				&& ( data[data.length - 2] & 0xff ) == 0xff
				&& ( data[data.length - 1] & 0xff ) == 0xd9 )
		{
			return TYPE_JPEG;
		}

		return TYPE_UNKNOWN;
	}

	public static byte[] convertImage( byte[] src, String targetFormat )
			throws IOException
	{
		BufferedImage bi = ImageIO.read( new ByteArrayInputStream( src ) );

		ByteArrayOutputStream bos = new ByteArrayOutputStream( );

		ImageIO.write( bi, targetFormat, bos );

		return bos.toByteArray( );
	}
}
