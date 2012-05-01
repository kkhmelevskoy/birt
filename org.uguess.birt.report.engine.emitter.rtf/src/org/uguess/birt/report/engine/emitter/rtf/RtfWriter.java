/********************************************************************************
 * (C) Copyright 2000-2009, by Shawn Qualia.
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

import java.awt.Color;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.birt.report.engine.api.IHTMLActionHandler;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.api.impl.Action;
import org.eclipse.birt.report.engine.content.IHyperlinkAction;
import org.eclipse.birt.report.engine.css.engine.value.css.CSSConstants;
import org.eclipse.birt.report.engine.emitter.IEmitterServices;
import org.eclipse.birt.report.engine.nLayout.area.IArea;
import org.eclipse.birt.report.engine.nLayout.area.IImageArea;
import org.eclipse.birt.report.engine.nLayout.area.ITextArea;
import org.uguess.birt.report.engine.layout.wrapper.Frame;
import org.uguess.birt.report.engine.layout.wrapper.Style;
import org.uguess.birt.report.engine.util.EngineUtil;
import org.uguess.birt.report.engine.util.ImageUtil;

/**
 * RtfWriter
 */
public class RtfWriter implements RtfConstants
{

	// Constants for default writer settings
	private static final String DEFAULT_FONT_NAME = "Times New Roman"; //$NON-NLS-1$
	private static final int DEFAULT_CACHE_THRESHOLD = 1024;
	private static final int DEFAULT_BUFFER_SIZE = 1024;

	private OutputStream os;
	private SegmentedCacheWriter writer;

	private int pageWidth = -1;
	private int pageHeight = -1;

	private int marginLeft = -1;
	private int marginRight = -1;
	private int marginTop = -1;
	private int marginBottom = -1;

	private List<FontInfo> fontTable;
	private List<Color> colorTable;

	private Set<Integer> segmentTable;

	private IEmitterServices services;

	public RtfWriter( OutputStream os, boolean allowDiskCache,
			int cacheThreshold, IEmitterServices services ) throws IOException
	{
		this.os = os;
		this.services = services;

		colorTable = new ArrayList<Color>( );
		fontTable = new ArrayList<FontInfo>( );

		// init color
		colorTable.add( Color.black );
		colorTable.add( Color.white );

		writer = new SegmentedCacheWriter( ( cacheThreshold > 0 ? cacheThreshold
				: DEFAULT_CACHE_THRESHOLD ) * 1024,
				allowDiskCache );
	}

	public void newPage( boolean first, boolean landscape ) throws IOException
	{
		if ( !first )
		{
			writer.write( NEW_PAGE );
			writer.write( CLOSE_SECT );
		}
		writer.write( landscape ? OPEN_SECT_LANDSCAPE : OPEN_SECT );
	}

	public void setPageSize( Frame frame ) throws IOException
	{
		Rectangle rct = getFrameAnchor( frame );

		pageWidth = (int) rct.getWidth( );
		pageHeight = (int) rct.getHeight( );
	}

	public void setPageMargin( int ml, int mr, int mt, int mb )
	{
		if ( ml != -1 )
		{
			marginLeft = scale2twip( ml );
		}
		if ( mr != -1 )
		{
			marginRight = scale2twip( mr );
		}
		if ( mt != -1 )
		{
			marginTop = scale2twip( mt );
		}
		if ( mb != -1 )
		{
			marginBottom = scale2twip( mb );
		}
	}

	public void writeImage( Frame frame, boolean suppressUnknownImage )
			throws IOException
	{
		IImageArea imageArea = (IImageArea) frame.getData( );

		byte[] data = imageArea.getImageData( );

		if ( data == null )
		{
			String url = imageArea.getImageUrl( );

			data = loadPictureData( url );
		}

		if ( data != null )
		{
			int type = getPictureType( data );

			if ( type == -1 )
			{
				try
				{
					// try convert to png format
					data = ImageUtil.convertImage( data, "png" ); //$NON-NLS-1$
					type = PNG;
				}
				catch ( IOException e )
				{
				}
			}

			if ( type != -1 )
			{
				Rectangle rct = getFrameAnchor( frame );

				writer.write( NEW_LINE );
				writer.write( "{\\shp{\\*\\shpinst" ); //$NON-NLS-1$
				writer.write( "\\shpleft" ).write( (int) rct.getX( ) ); //$NON-NLS-1$
				writer.write( "\\shptop" ).write( (int) rct.getY( ) ); //$NON-NLS-1$
				writer.write( "\\shpright" ) //$NON-NLS-1$
						.write( (int) ( rct.getX( ) + rct.getWidth( ) ) );
				writer.write( "\\shpbottom" ) //$NON-NLS-1$
						.write( (int) ( rct.getY( ) + rct.getHeight( ) ) );
				writer.write( "\\shpfhdr0\\shpbxpage\\shpbypage\\shpwr3\\shpwrk0\\shpfblwtxt1" ); //$NON-NLS-1$
				writer.write( "{\\sp{\\sn txflTextFlow}{\\sv 0}}{\\sp{\\sn shapeType}{\\sv 75}}" ); //$NON-NLS-1$

				writeHyperlink( imageArea );

				writer.write( "{\\sp{\\sn pib}{\\sv {\\pict" ); //$NON-NLS-1$

				switch ( type )
				{
					case DIB :
						writer.write( "\\wmetafile8" ); //$NON-NLS-1$
						data = wrapBMP2WMF( data );
						break;
					case PNG :
						writer.write( "\\pngblip" ); //$NON-NLS-1$
						break;
					case JPEG :
						writer.write( "\\jpegblip" ); //$NON-NLS-1$
						break;
					default :
						break;
				}

				writer.write( SPACE_DELIMETER );
				writeHexBinary( data );

				writer.write( PENTA_CLOSE_GROUP );
			}
			else if ( !suppressUnknownImage )
			{
				// output alter text frame.
				writeFrame( frame, "<<Unsupported Image>>", false ); //$NON-NLS-1$
			}
		}
	}

	private byte[] loadPictureData( String url )
	{
		if ( url != null )
		{
			BufferedInputStream bis = null;
			try
			{
				bis = new BufferedInputStream( new URL( url ).openStream( ) );
				ByteArrayOutputStream bos = new ByteArrayOutputStream( );

				byte[] buf = new byte[1024];
				int count = bis.read( buf );
				while ( count != -1 )
				{
					bos.write( buf, 0, count );

					count = bis.read( buf );
				}

				return bos.toByteArray( );
			}
			catch ( Exception e )
			{
				e.printStackTrace( );
			}
			finally
			{
				if ( bis != null )
				{
					try
					{
						bis.close( );
					}
					catch ( IOException e )
					{
						e.printStackTrace( );
					}
				}
			}
		}
		return null;
	}

	private int getPictureType( byte[] data )
	{
		int type = ImageUtil.getImageType( data );

		switch ( type )
		{
			case ImageUtil.TYPE_DIB :
				return DIB;
			case ImageUtil.TYPE_PNG :
				return PNG;
			case ImageUtil.TYPE_JPEG :
				return JPEG;
		}

		return -1;
	}

	// ------- Following code are adapted from iText library --------
	/*
	 * Copyright 2001, 2002 Paulo Soares
	 * 
	 * This library is free software; you can redistribute it and/or modify it
	 * under the terms of the MPL as stated above or under the terms of the GNU
	 * Library General Public License as published by the Free Software
	 * Foundation; either version 2 of the License, or any later version.
	 * 
	 * This library is distributed in the hope that it will be useful, but
	 * WITHOUT ANY WARRANTY; without even the implied warranty of
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library
	 * general Public License for more details.
	 * 
	 * http://www.lowagie.com/iText/
	 */
	private static final int META_SETWINDOWORG = 0x020B;
	private static final int META_SETWINDOWEXT = 0x020C;
	private static final int META_DIBSTRETCHBLT = 0x0b41;
	private static final int META_SETMAPMODE = 0x0103;

	private byte[] wrapBMP2WMF( byte[] data ) throws IOException
	{
		// extract with/height
		ByteArrayInputStream bis = new ByteArrayInputStream( data );

		// skip file header
		bis.skip( 14 );

		// Start BitmapCoreHeader
		int width, height;

		long size = readDWord( bis );

		if ( size == 12 )
		{
			width = readWord( bis );
			height = readWord( bis );
		}
		else
		{
			width = readInt( bis );
			height = readInt( bis );
		}

		bis.close( );

		int sizeBmpWords = ( data.length - 14 + 1 ) >>> 1;
		ByteArrayOutputStream os = new ByteArrayOutputStream( );
		// write metafile header
		writeWord( os, 1 );
		writeWord( os, 9 );
		writeWord( os, 0x0300 );
		writeDWord( os, 9 + 4 + 5 + 5 + ( 13 + sizeBmpWords ) + 3 ); // total
		// metafile
		// size
		writeWord( os, 1 );
		writeDWord( os, 14 + sizeBmpWords ); // max record size
		writeWord( os, 0 );
		// write records
		writeDWord( os, 4 );
		writeWord( os, META_SETMAPMODE );
		writeWord( os, 8 );

		writeDWord( os, 5 );
		writeWord( os, META_SETWINDOWORG );
		writeWord( os, 0 );
		writeWord( os, 0 );

		writeDWord( os, 5 );
		writeWord( os, META_SETWINDOWEXT );
		writeWord( os, height );
		writeWord( os, width );

		writeDWord( os, 13 + sizeBmpWords );
		writeWord( os, META_DIBSTRETCHBLT );
		writeDWord( os, 0x00cc0020 );
		writeWord( os, height );
		writeWord( os, width );
		writeWord( os, 0 );
		writeWord( os, 0 );
		writeWord( os, height );
		writeWord( os, width );
		writeWord( os, 0 );
		writeWord( os, 0 );
		os.write( data, 14, data.length - 14 );
		if ( ( data.length & 1 ) == 1 )
		{
			os.write( 0 );
		}

		writeDWord( os, 3 );
		writeWord( os, 0 );
		os.close( );
		return os.toByteArray( );
	}

	// Unsigned 8 bits
	private int readUnsignedByte( InputStream stream ) throws IOException
	{
		return ( stream.read( ) & 0xff );
	}

	// Unsigned 2 bytes
	private int readUnsignedShort( InputStream stream ) throws IOException
	{
		int b1 = readUnsignedByte( stream );
		int b2 = readUnsignedByte( stream );
		return ( ( b2 << 8 ) | b1 ) & 0xffff;
	}

	// Unsigned 16 bits
	private int readWord( InputStream stream ) throws IOException
	{
		return readUnsignedShort( stream );
	}

	// Unsigned 4 bytes
	private long readUnsignedInt( InputStream stream ) throws IOException
	{
		int b1 = readUnsignedByte( stream );
		int b2 = readUnsignedByte( stream );
		int b3 = readUnsignedByte( stream );
		int b4 = readUnsignedByte( stream );
		long l = ( ( b4 << 24 ) | ( b3 << 16 ) | ( b2 << 8 ) | b1 );
		return l & 0xffffffff;
	}

	// Signed 4 bytes
	private int readInt( InputStream stream ) throws IOException
	{
		int b1 = readUnsignedByte( stream );
		int b2 = readUnsignedByte( stream );
		int b3 = readUnsignedByte( stream );
		int b4 = readUnsignedByte( stream );
		return ( b4 << 24 ) | ( b3 << 16 ) | ( b2 << 8 ) | b1;
	}

	// Unsigned 4 bytes
	private long readDWord( InputStream stream ) throws IOException
	{
		return readUnsignedInt( stream );
	}

	private void writeWord( OutputStream os, int v ) throws IOException
	{
		os.write( v & 0xff );
		os.write( ( v >>> 8 ) & 0xff );
	}

	private void writeDWord( OutputStream os, int v ) throws IOException
	{
		writeWord( os, v & 0xffff );
		writeWord( os, ( v >>> 16 ) & 0xffff );
	}

	private void writeHexBinary( byte[] data ) throws IOException
	{
		ByteArrayInputStream bis = new ByteArrayInputStream( data );

		int val = bis.read( );
		int count = 1;

		while ( val != -1 )
		{
			String hex = Integer.toHexString( val );
			if ( hex.length( ) < 2 )
			{
				hex = "0" + hex; //$NON-NLS-1$
			}

			writer.write( hex );
			val = bis.read( );

			if ( count++ > 64 )
			{
				writer.write( NEW_LINE );
				count = 1;
			}
		}
	}

	// --------- End of iText code adaption --------------

	public void writePlain( Frame frame ) throws IOException
	{
		writeFrame( frame, null, false );
	}

	public void writeText( Frame frame ) throws IOException
	{
		writeFrame( frame, ( (ITextArea) frame.getData( ) ).getText( ), false );
	}

	public void writeTotalText( Frame frame ) throws IOException
	{
		writeFrame( frame, ( (ITextArea) frame.getData( ) ).getText( ), true );
	}

	public void updateTotalText( String text )
	{
		if ( segmentTable != null )
		{
			for ( Integer seg : segmentTable )
			{
				writer.updateToken( seg, escapeText( text ) );
			}
		}
	}

	private void writeFrame( Frame frame, String text, boolean checkSegment )
			throws IOException
	{
		String border = getRtfBorder( frame.getStyle( ) );

		String shading = getRtfShading( frame.getStyle( ) );

		if ( border != null || shading != null || text != null )
		{
			writer.write( OPEN_PARAGRAPH );

			Rectangle rct = getFrameAnchor( frame );

			writer.write( "\\posx" ).write( (int) rct.getX( ) ); //$NON-NLS-1$
			writer.write( "\\posy" ).write( (int) rct.getY( ) ); //$NON-NLS-1$
			writer.write( "\\absw" ).write( (int) rct.getWidth( ) ); //$NON-NLS-1$
			writer.write( "\\absh" ).write( "-" ).write( (int) rct.getHeight( ) ); //$NON-NLS-1$ //$NON-NLS-2$

			if ( border != null )
			{
				writer.write( border );
			}

			if ( shading != null )
			{
				writer.write( shading );
			}

			writer.write( "\\fi0" ); //$NON-NLS-1$

			if ( text != null && ( checkSegment || text.length( ) > 0 ) )
			{
				writer.write( "{\\shp{\\*\\shpinst" ); //$NON-NLS-1$
				writer.write( "\\shpleft" ).write( (int) rct.getX( ) ); //$NON-NLS-1$
				writer.write( "\\shptop" ).write( (int) rct.getY( ) ); //$NON-NLS-1$
				writer.write( "\\shpright" ) //$NON-NLS-1$
						.write( (int) ( rct.getX( ) + rct.getWidth( ) ) );
				writer.write( "\\shpbottom" ) //$NON-NLS-1$
						.write( (int) ( rct.getY( ) + rct.getHeight( ) ) );
				writer.write( "\\shpfhdr0\\shpbxpage\\shpbypage\\shpwr3\\shpwrk0\\shpfblwtxt1" ); //$NON-NLS-1$
				writer.write( "{\\sp{\\sn txflTextFlow}{\\sv 0}}{\\sp{\\sn shapeType}{\\sv 202}}" ); //$NON-NLS-1$

				writer.write( "{\\sp{\\sn fLine}{\\sv 0}}{\\sp{\\sn WrapText}{\\sv 2}}" ); //$NON-NLS-1$
				writer.write( "{\\sp{\\sn dxTextLeft}{\\sv 0}}{\\sp{\\sn dxTextRight}{\\sv 0}}" ); //$NON-NLS-1$
				writer.write( "{\\sp{\\sn dyTextTop}{\\sv 0}}{\\sp{\\sn dyTextBottom}{\\sv 0}}" ); //$NON-NLS-1$
				writer.write( "{\\sp{\\sn fillOpacity}{\\sv 0}}" ); //$NON-NLS-1$

				boolean hasLink = writeHyperlink( (IArea) frame.getData( ) );

				writer.write( "{\\shptxt " ); //$NON-NLS-1$

				writer.write( "\\ltrpar" ); //$NON-NLS-1$
				writer.write( getRtfTextAlign( frame.getStyle( ) ) );
				writer.write( "{\\ltrch" ); //$NON-NLS-1$

				writeFont( frame.getStyle( ), hasLink );

				writer.write( SPACE_DELIMETER );

				if ( checkSegment )
				{
					int seg = writer.newSegment( escapeText( text ) );

					if ( segmentTable == null )
					{
						segmentTable = new HashSet<Integer>( );
					}

					segmentTable.add( seg );
				}
				else
				{
					writer.write( escapeText( text ) );
				}

				writer.write( QUADRI_CLOSE_GROUP );
			}

			writer.write( CLOSE_PARAGRAPH );
		}
	}

	private String getRtfBorder( Style style )
	{
		if ( style == null )
		{
			return null;
		}

		String leftBorderStyle = style.getLeftBorderStyle( );
		String rightBorderStyle = style.getRightBorderStyle( );
		String topBorderStyle = style.getTopBorderStyle( );
		String bottomBorderStyle = style.getBottomBorderStyle( );

		boolean hasLeftBorder = leftBorderStyle != null
				&& !"none".equalsIgnoreCase( leftBorderStyle ); //$NON-NLS-1$
		boolean hasRightBorder = rightBorderStyle != null
				&& !"none".equalsIgnoreCase( rightBorderStyle ); //$NON-NLS-1$
		boolean hasTopBorder = topBorderStyle != null
				&& !"none".equalsIgnoreCase( topBorderStyle ); //$NON-NLS-1$
		boolean hasBottomBorder = bottomBorderStyle != null
				&& !"none".equalsIgnoreCase( bottomBorderStyle ); //$NON-NLS-1$

		// max line width 75 twips for rtf restriction
		int lineLeftWidth = hasLeftBorder ? Math.min( scale2twip( style.getLeftBorderWidth( ) ),
				75 )
				: 0;
		int lineRightWidth = hasRightBorder ? Math.min( scale2twip( style.getRightBorderWidth( ) ),
				75 )
				: 0;
		int lineTopWidth = hasTopBorder ? Math.min( scale2twip( style.getTopBorderWidth( ) ),
				75 )
				: 0;
		int lineBottomWidth = hasBottomBorder ? Math.min( scale2twip( style.getBottomBorderWidth( ) ),
				75 )
				: 0;

		if ( lineLeftWidth <= 0
				&& lineRightWidth <= 0
				&& lineTopWidth <= 0
				&& lineBottomWidth <= 0 )
		{
			return null;
		}

		String lineLeftStyle = hasLeftBorder ? getRtfLineStyle( leftBorderStyle )
				: null;
		String lineRightStyle = hasRightBorder ? getRtfLineStyle( rightBorderStyle )
				: null;
		String lineTopStyle = hasTopBorder ? getRtfLineStyle( topBorderStyle )
				: null;
		String lineBottomStyle = hasBottomBorder ? getRtfLineStyle( bottomBorderStyle )
				: null;

		if ( lineLeftStyle == null
				&& lineRightStyle == null
				&& lineTopStyle == null
				&& lineBottomStyle == null )
		{
			return null;
		}

		Color lineLeftColor = hasLeftBorder ? style.getLeftBorderColor( )
				: null;
		Color lineRightColor = hasRightBorder ? style.getRightBorderColor( )
				: null;
		Color lineTopColor = hasTopBorder ? style.getTopBorderColor( ) : null;
		Color lineBottomColor = hasBottomBorder ? style.getBottomBorderColor( )
				: null;

		if ( lineLeftColor == null
				&& lineRightColor == null
				&& lineTopColor == null
				&& lineBottomColor == null )
		{
			return null;
		}

		StringBuffer sb = new StringBuffer( );

		// left border
		if ( hasLeftBorder
				&& lineLeftWidth > 0
				&& lineLeftStyle != null
				&& lineLeftColor != null )
		{
			sb.append( "\\brdrl" ) //$NON-NLS-1$
					.append( lineLeftStyle )
					.append( "\\brdrw" ) //$NON-NLS-1$
					.append( lineLeftWidth )
					// .append( "\\brsp0" )
					.append( "\\brdrcf" ) //$NON-NLS-1$
					.append( addColor( lineLeftColor ) );
		}

		// right border
		if ( hasRightBorder
				&& lineRightWidth > 0
				&& lineRightStyle != null
				&& lineRightColor != null )
		{
			sb.append( "\\brdrr" ) //$NON-NLS-1$
					.append( lineRightStyle )
					.append( "\\brdrw" ) //$NON-NLS-1$
					.append( lineRightWidth )
					// .append( "\\brsp0" )
					.append( "\\brdrcf" ) //$NON-NLS-1$
					.append( addColor( lineRightColor ) );
		}

		// top border
		if ( hasTopBorder
				&& lineTopWidth > 0
				&& lineTopStyle != null
				&& lineTopColor != null )
		{
			sb.append( "\\brdrt" ) //$NON-NLS-1$
					.append( lineTopStyle )
					.append( "\\brdrw" ) //$NON-NLS-1$
					.append( lineTopWidth )
					// .append( "\\brsp0" )
					.append( "\\brdrcf" ) //$NON-NLS-1$
					.append( addColor( lineTopColor ) );
		}

		// bottom border
		if ( hasBottomBorder
				&& lineBottomWidth > 0
				&& lineBottomStyle != null
				&& lineBottomColor != null )
		{
			sb.append( "\\brdrb" ) //$NON-NLS-1$
					.append( lineBottomStyle )
					.append( "\\brdrw" ) //$NON-NLS-1$
					.append( lineBottomWidth )
					// .append( "\\brsp0" )
					.append( "\\brdrcf" ) //$NON-NLS-1$
					.append( addColor( lineBottomColor ) );
		}

		if ( sb.length( ) > 0 )
		{
			return sb.toString( );
		}
		else
		{
			return null;
		}
	}

	private String getRtfLineStyle( String style )
	{
		if ( "double".equals( style ) ) //$NON-NLS-1$
		{
			return "\\brdrdb"; //$NON-NLS-1$
		}
		if ( "solid".equals( style ) ) //$NON-NLS-1$
		{
			return "\\brdrs"; //$NON-NLS-1$
		}
		if ( "dashed".equals( style ) ) //$NON-NLS-1$
		{
			return "\\brdrdash"; //$NON-NLS-1$
		}
		if ( "dotted".equals( style ) ) //$NON-NLS-1$
		{
			return "\\brdrdot"; //$NON-NLS-1$
		}
		return null;
	}

	private String getRtfShading( Style style )
	{
		if ( style == null )
		{
			return null;
		}

		Color color = style.getBackgroundColor( );

		if ( color != null )
		{
			return "\\shading100\\cbpat" + addColor( color ); //$NON-NLS-1$
		}

		return null;
	}

	private String getRtfTextAlign( Style style )
	{
		String align = "\\ql"; //$NON-NLS-1$

		if ( style != null )
		{
			String textAlign = style.getTextAlign( );

			if ( "center".equalsIgnoreCase( textAlign ) || "middle".equalsIgnoreCase( textAlign ) ) //$NON-NLS-1$ //$NON-NLS-2$
			{
				align = "\\qc"; //$NON-NLS-1$
			}
			else if ( "right".equalsIgnoreCase( textAlign ) ) //$NON-NLS-1$
			{
				align = "\\qr"; //$NON-NLS-1$
			}
		}

		return align;
	}

	private String escapeText( String content )
	{
		int length = content.length( );
		int z = 'z';
		StringBuffer ret = new StringBuffer( length );
		for ( int i = 0; i < length; i++ )
		{
			char ch = content.charAt( i );

			if ( ch == '\\' )
			{
				ret.append( "\\\\" ); //$NON-NLS-1$
			}
			else if ( ch == '\n' )
			{
				ret.append( "\\line " ); //$NON-NLS-1$
			}
			else if ( ch == '\t' )
			{
				ret.append( "\\tab " ); //$NON-NLS-1$
			}
			else if ( ch > z )
			{
				// ret.append( "\\\'" ).append( Long.toHexString( ch ) );
				ret.append( "\\u" ).append( (long) ch ).append( '?' ); //$NON-NLS-1$
			}
			else
			{
				ret.append( ch );
			}
		}

		return ret.toString( );
	}

	private boolean writeHyperlink( IArea area ) throws IOException
	{
		IHyperlinkAction hlAction = area.getAction( );

		if ( hlAction != null )
		{
			try
			{
				IReportRunnable runnable = services.getReportRunnable( );
				String systemId = runnable == null ? null
						: runnable.getReportName( );

				Action act = new Action( systemId, hlAction );

				String link = null;
				String tooltip = EngineUtil.getActionTooltip( hlAction );

				Object ac = services.getOption( RenderOption.ACTION_HANDLER );

				if ( ac instanceof IHTMLActionHandler )
				{
					link = ( (IHTMLActionHandler) ac ).getURL( act,
							services.getReportContext( ) );
				}
				else
				{
					link = hlAction.getHyperlink( );
				}

				if ( link != null )
				{
					writer.write( "{\\sp{\\sn pihlShape}{\\sv {\\*\\hl{\\hlfr " ); //$NON-NLS-1$
					writer.write( escapeText( tooltip == null ? link : tooltip ) );
					writer.write( " }{\\hlsrc " ); //$NON-NLS-1$
					writer.write( escapeText( link ) );
					writer.write( " }}}}" ); //$NON-NLS-1$
					writer.write( "{\\sp{\\sn fIsButton}{\\sv 1}}" ); //$NON-NLS-1$

					return true;
				}
			}
			catch ( Exception e )
			{
				System.out.println( "create hyperlink failed." ); //$NON-NLS-1$

				e.printStackTrace( );
			}
		}

		return false;
	}

	private void writeFont( Style style, boolean isLink ) throws IOException
	{
		if ( style == null )
		{
			return;
		}

		String fontName = style.getFontFamily( );
		short fontSize = (short) ( style.getFontSize( ) / 1000d );
		Color color = style.getColor( );
		boolean underline = isLink ? true : style.isTextUnderline( );
		boolean strikeout = style.isTextLineThrough( );
		boolean bold = CSSConstants.CSS_BOLD_VALUE.equals( style.getFontWeight( ) );
		boolean italic = ( CSSConstants.CSS_OBLIQUE_VALUE.equals( style.getFontStyle( ) ) || CSSConstants.CSS_ITALIC_VALUE.equals( style.getFontStyle( ) ) );

		int fi = addFont( new FontInfo( fontName ) );
		int ci = addColor( color );

		writer.write( UNDERLINE ).write( underline );
		writer.write( STRIKE ).write( strikeout );
		writer.write( BOLD ).write( bold );
		writer.write( ITALIC ).write( italic );
		writer.write( FONT_SIZE ).write( fontSize * 2 );
		writer.write( FONT ).write( fi );
		writer.write( FONT_COLOR ).write( ci );
	}

	/**
	 * translate 1/1000point to twpis. (1 twip = 1/20 point)
	 */
	private int scale2twip( double val )
	{
		return (int) Math.ceil( val * 20d / 1000d );
	}

	private Rectangle getFrameAnchor( Frame frame )
	{
		int left = scale2twip( frame.getLeft( ) );
		int top = scale2twip( frame.getTop( ) );
		int width = scale2twip( frame.getRight( ) - frame.getLeft( ) );
		int height = scale2twip( frame.getBottom( ) - frame.getTop( ) );

		return new Rectangle( left, top, width, height );
	}

	private int addFont( FontInfo fi )
	{
		if ( fi == null )
		{
			return 0;
		}

		for ( int i = 0; i < fontTable.size( ); i++ )
		{
			if ( fi.equals( fontTable.get( i ) ) )
			{
				return i;
			}
		}

		fontTable.add( fi );
		return fontTable.size( ) - 1;
	}

	private int addColor( Color cl )
	{
		if ( cl == null )
		{
			return 0;
		}

		for ( int i = 0; i < colorTable.size( ); i++ )
		{
			if ( cl.equals( colorTable.get( i ) ) )
			{
				return i;
			}
		}

		colorTable.add( cl );
		return colorTable.size( ) - 1;
	}

	private void writeFontTable( DirectWriter dw ) throws IOException
	{
		dw.write( FONT_TABLE );

		// check default font, at least 1 item
		if ( fontTable.size( ) < 1 )
		{
			writeFontInfo( dw, new FontInfo( DEFAULT_FONT_NAME ), 0 );
		}
		else
		{
			for ( int i = 0; i < fontTable.size( ); i++ )
			{
				writeFontInfo( dw, fontTable.get( i ), i );
			}
		}

		dw.write( CLOSE_GROUP );
	}

	private void writeFontInfo( DirectWriter dw, FontInfo fi, int idx )
			throws IOException
	{
		dw.write( OPEN_GROUP );
		dw.write( FONT );
		dw.write( idx );
		dw.write( DEFAULT_FONT_SETTING );
		dw.write( fi.name );
		dw.write( SEMICOLON_DELIMETER );
		dw.write( CLOSE_GROUP );
	}

	private void writeColorTable( DirectWriter dw ) throws IOException
	{
		dw.write( COLOR_TABLE );

		for ( int i = 0; i < colorTable.size( ); i++ )
		{
			writeColor( dw, colorTable.get( i ) );
		}

		dw.write( CLOSE_GROUP );
	}

	private void writeColor( DirectWriter dw, Color cl ) throws IOException
	{
		dw.write( RED ).write( cl.getRed( ) );
		dw.write( GREEN ).write( cl.getGreen( ) );
		dw.write( BLUE ).write( cl.getBlue( ) );
		dw.write( SEMICOLON_DELIMETER );
	}

	public void close( ) throws IOException
	{
		// finish doc first
		writer.write( CLOSE_SECT );
		writer.write( CLOSE_GROUP );

		// prepare main doc writer
		DirectWriter mainWriter = new DirectWriter( new BufferedOutputStream( os ) );

		mainWriter.write( DOC_HEADER );

		writeFontTable( mainWriter );
		writeColorTable( mainWriter );

		mainWriter.write( GENERATOR );
		mainWriter.write( NEW_LINE );

		if ( pageWidth != -1 )
		{
			mainWriter.write( PAGE_WIDTH ).write( pageWidth );
		}
		if ( pageHeight != -1 )
		{
			mainWriter.write( PAGE_HEIGHT ).write( pageHeight );
		}
		if ( marginLeft != -1 )
		{
			mainWriter.write( MARGIN_LEFT ).write( marginLeft );
		}
		if ( marginRight != -1 )
		{
			mainWriter.write( MARGIN_RIGHT ).write( marginRight );
		}
		if ( marginTop != -1 )
		{
			mainWriter.write( MARGIN_TOP ).write( marginTop );
		}
		if ( marginBottom != -1 )
		{
			mainWriter.write( MARGIN_BOTTOM ).write( marginBottom );
		}

		mainWriter.write( DEFAULT_PAGE_SETTING );

		// flush to main writer
		writer.flushCache( mainWriter.os );
		writer = null;

		mainWriter.flush( );
		mainWriter = null;

		fontTable.clear( );
		colorTable.clear( );
		fontTable = null;
		colorTable = null;
	}

	/**
	 * FontInfo
	 */
	static class FontInfo
	{

		String name;

		FontInfo( String name )
		{
			this.name = name;
		}

		public int hashCode( )
		{
			if ( name == null )
			{
				return 0;
			}

			return name.hashCode( );
		}

		public boolean equals( Object obj )
		{
			if ( !( obj instanceof FontInfo ) )
			{
				return false;
			}

			FontInfo that = (FontInfo) obj;

			if ( this.name == null )
			{
				return that.name == null;
			}
			else
			{
				return this.name.equals( that.name );
			}
		}
	}

	/**
	 * DirectWriter
	 */
	static class DirectWriter
	{

		protected OutputStream os;

		DirectWriter( )
		{
		}

		DirectWriter( OutputStream os )
		{
			this.os = os;
		}

		DirectWriter write( String content ) throws IOException
		{
			byte[] data = content.getBytes( );
			os.write( data );
			notifyWrite( data.length );
			return this;
		}

		DirectWriter write( byte[] content ) throws IOException
		{
			os.write( content );
			notifyWrite( content.length );
			return this;
		}

		DirectWriter write( byte[] content, int off, int len )
				throws IOException
		{
			os.write( content, off, len );
			notifyWrite( len );
			return this;
		}

		DirectWriter write( int content ) throws IOException
		{
			byte[] data = String.valueOf( content ).getBytes( );
			os.write( data );
			notifyWrite( data.length );
			return this;
		}

		DirectWriter write( boolean content ) throws IOException
		{
			if ( !content )
			{
				os.write( ZERO );
				notifyWrite( ZERO.length );
			}
			return this;
		}

		void notifyWrite( int writtenSize ) throws IOException
		{
		}

		void flush( ) throws IOException
		{
			os.flush( );
		}

		void close( ) throws IOException
		{
			os.close( );
		}
	}

	/**
	 * CacheWriter
	 */
	static class CacheWriter extends DirectWriter
	{

		private ByteArrayOutputStream bos;
		private BufferedOutputStream fos;
		private File tempFile;
		private final long cacheSize;
		private final boolean allowDiskMode;

		CacheWriter( )
		{
			this( 1024, false );
		}

		CacheWriter( long initialCacheSize, boolean allowDiskMode )
		{
			this.cacheSize = initialCacheSize;
			this.allowDiskMode = allowDiskMode;

			bos = new ByteArrayOutputStream( DEFAULT_BUFFER_SIZE );

			// initial as memory mode
			os = bos;
		}

		@Override
		void notifyWrite( int writtenSize ) throws IOException
		{
			if ( allowDiskMode && bos != null && bos.size( ) > cacheSize )
			{
				// switch to disk mode
				tempFile = File.createTempFile( "rtf", null ); //$NON-NLS-1$
				fos = new BufferedOutputStream( new FileOutputStream( tempFile ) );
				os = fos;

				// move memory cache to disk cache
				bos.flush( );
				BufferedInputStream bis = new BufferedInputStream( new ByteArrayInputStream( bos.toByteArray( ) ) );
				transfer( bis, os );
				bis.close( );
				bis = null;
				bos.close( );
				bos = null;
			}
		}

		void flushCache( OutputStream os ) throws IOException
		{
			if ( bos != null )
			{
				bos.flush( );
				BufferedInputStream bis = new BufferedInputStream( new ByteArrayInputStream( bos.toByteArray( ) ) );
				transfer( bis, os );
				bis.close( );
				bis = null;
				bos.close( );
				bos = null;
			}
			else if ( fos != null )
			{
				fos.close( );
				fos = null;

				BufferedInputStream bis = new BufferedInputStream( new FileInputStream( tempFile ) );
				transfer( bis, os );
				bis.close( );
				bis = null;

				tempFile.delete( );
				tempFile = null;
			}
		}

		private void transfer( InputStream is, OutputStream os )
				throws IOException
		{
			byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
			int len = is.read( buf );
			while ( len != -1 )
			{
				os.write( buf, 0, len );
				len = is.read( buf );
			}
			os.flush( );
		}
	}

	/**
	 * SegmentedCacheWriter
	 */
	static class SegmentedCacheWriter extends DirectWriter
	{

		private List<CacheWriter> segments;
		private List<String> tokens;

		private CacheWriter currentWriter;

		private long totalWrittenSize;

		private final long cacheSize;
		private final boolean allowDiskMode;

		SegmentedCacheWriter( )
		{
			this( 1024, false );
		}

		SegmentedCacheWriter( int initialCacheSize, boolean allowDiskMode )
		{
			this.cacheSize = initialCacheSize;
			this.allowDiskMode = allowDiskMode;

			segments = new ArrayList<CacheWriter>( );
			tokens = new ArrayList<String>( );

			currentWriter = new CacheWriter( initialCacheSize, allowDiskMode );
			segments.add( currentWriter );
		}

		/**
		 * Returns the 1-based last segment number.
		 * 
		 * @param token
		 * @return
		 */
		int newSegment( String token )
		{
			tokens.add( token );

			long remainCacheSize = cacheSize - totalWrittenSize;
			if ( remainCacheSize < 0 )
			{
				remainCacheSize = 0;
			}

			currentWriter = new CacheWriter( remainCacheSize, allowDiskMode );
			segments.add( currentWriter );

			return segments.size( ) - 1;
		}

		/**
		 * @param segment
		 *            1-based segment number.
		 * @param token
		 */
		void updateToken( int segment, String token )
		{
			tokens.set( segment - 1, token );
		}

		void flushCache( OutputStream os ) throws IOException
		{
			for ( int i = 0; i < segments.size( ) - 1; i++ )
			{
				String token = tokens.get( i );

				segments.get( i ).write( token );
			}

			for ( CacheWriter wrt : segments )
			{
				wrt.flushCache( os );
			}
		}

		@Override
		void notifyWrite( int writtenSize ) throws IOException
		{
			totalWrittenSize += writtenSize;
		}

		@Override
		void flush( ) throws IOException
		{
			for ( CacheWriter wrt : segments )
			{
				wrt.flush( );
			}
		}

		@Override
		void close( ) throws IOException
		{
			for ( CacheWriter wrt : segments )
			{
				wrt.close( );
			}
		}

		@Override
		DirectWriter write( boolean content ) throws IOException
		{
			currentWriter.write( content );
			return this;
		}

		@Override
		DirectWriter write( byte[] content ) throws IOException
		{
			currentWriter.write( content );
			return this;
		}

		@Override
		DirectWriter write( byte[] content, int off, int len )
				throws IOException
		{
			currentWriter.write( content, off, len );
			return this;
		}

		@Override
		DirectWriter write( int content ) throws IOException
		{
			currentWriter.write( content );
			return this;
		}

		@Override
		DirectWriter write( String content ) throws IOException
		{
			currentWriter.write( content );
			return this;
		}

	}
}
