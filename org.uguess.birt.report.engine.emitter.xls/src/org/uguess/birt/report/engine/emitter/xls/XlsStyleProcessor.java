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

package org.uguess.birt.report.engine.emitter.xls;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.smartxls.RangeStyle;
import com.smartxls.WorkBook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.eclipse.birt.report.engine.css.engine.value.css.CSSConstants;
import org.uguess.birt.report.engine.layout.wrapper.Style;
import org.uguess.birt.report.engine.util.ImageUtil;

/**
 * XlsStyleProcessor
 */
public class XlsStyleProcessor
{

	private static final int INDEX_FONT = 0;
	private static final int INDEX_BACKGROUND = 1;
	private static final int INDEX_BORDER_LEFT = 2;
	private static final int INDEX_BORDER_RIGHT = 3;
	private static final int INDEX_BORDER_TOP = 4;
	private static final int INDEX_BORDER_BOTTOM = 5;

	private WorkBook workbook;

	private RangeStyle emptyCellStyle;

	private Map<?, HSSFColor> hssfColorMap;

	private Map<Style, RangeStyle[]> styleCache;

	@SuppressWarnings("unchecked")
	XlsStyleProcessor( WorkBook workbook )
	{
		this.workbook = workbook;

		this.styleCache = new HashMap<Style, RangeStyle[]>( );
		this.hssfColorMap = HSSFColor.getIndexHash( );

		initEmptyCellStyle( );
	}

	public void dispose( )
	{
		styleCache.clear( );
		hssfColorMap.clear( );

		styleCache = null;
		hssfColorMap = null;
		emptyCellStyle = null;

		workbook = null;
	}

	/**
	 * Convert from HSSF color index to ARGB for SmartXLS
	 * 
	 * @param hssfColorIndex
	 * @return
	 */
	private int rgb( short hssfColorIndex ) {
		HSSFColor color = hssfColorMap.get( hssfColorIndex );
		
		short[] t = color.getTriplet();
		
		return (t[0] << 16) | (t[1] << 8) | t[2];  
	}
	
	private void initEmptyCellStyle( )
	{
		try {
			emptyCellStyle = workbook.getRangeStyle();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		emptyCellStyle.setPattern( RangeStyle.PatternSolid );
		
		emptyCellStyle.setHorizontalAlignment( RangeStyle.HorizontalAlignmentCenter );
		emptyCellStyle.setVerticalAlignment( RangeStyle.VerticalAlignmentCenter );
		
//		emptyCellStyle.setHidden( false );
		emptyCellStyle.setLocked( false );
		emptyCellStyle.setLeftBorder( RangeStyle.BorderNone ); 
		emptyCellStyle.setRightBorder( RangeStyle.BorderNone  );
		emptyCellStyle.setTopBorder( RangeStyle.BorderNone  ); 
		emptyCellStyle.setBottomBorder( RangeStyle.BorderNone  );

		short backColorIndex = HSSFColor.BLACK.index;
		emptyCellStyle.setBottomBorderColor( rgb(backColorIndex) );
		emptyCellStyle.setTopBorderColor( rgb(backColorIndex) );
		emptyCellStyle.setLeftBorderColor( rgb(backColorIndex) );
		emptyCellStyle.setRightBorderColor( rgb(backColorIndex) );
		
		emptyCellStyle.setPatternFG( rgb(HSSFColor.WHITE.index) );
		emptyCellStyle.setPatternBG( rgb(HSSFColor.WHITE.index) );
		emptyCellStyle.setVerticalAlignment( RangeStyle.VerticalAlignmentCenter );

		emptyCellStyle.setFontName( "Serif" ); //$NON-NLS-1$
		short size = 10;
		emptyCellStyle.setFontSize( size );
	}

	public RangeStyle getEmptyCellStyle( )
	{
		return emptyCellStyle;
	}

	public int getHssfPictureType( byte[] data )
	{
		int type = ImageUtil.getImageType( data );

		switch ( type )
		{
			// there are no such constants in smartxls but these are correct
			case ImageUtil.TYPE_DIB :
				return HSSFWorkbook.PICTURE_TYPE_DIB;
			case ImageUtil.TYPE_PNG :
				return HSSFWorkbook.PICTURE_TYPE_PNG;
			case ImageUtil.TYPE_JPEG :
				return HSSFWorkbook.PICTURE_TYPE_JPEG;
		}

		return -1;
	}

	public RangeStyle getCellStyle( Style style, boolean useLinkStyle )
	{
		RangeStyle hssfStyle = null;

		if ( style == null || style.isEmpty( ) )
		{
			return emptyCellStyle;
		}

		RangeStyle[] styleEntry = null;

		// check existing cell style cache first.
		for ( Iterator<Entry<Style, RangeStyle[]>> itr = styleCache.entrySet( )
				.iterator( ); itr.hasNext( ); )
		{
			Entry<Style, RangeStyle[]> entry = itr.next( );

			if ( style.equals( entry.getKey( ) ) )
			{
				styleEntry = entry.getValue( );
				hssfStyle = styleEntry[useLinkStyle ? 1 : 0];
				break;
			}
		}

		if ( hssfStyle == null )
		{
			try {
				hssfStyle = workbook.getRangeStyle( );
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			Color[] colorFlag = new Color[6];

			setFont( style, useLinkStyle, colorFlag, hssfStyle );

			Color color = style.getBackgroundColor( );
			if ( color != null )
			{
				short cdx = getHssfColorIndex( color,
						colorFlag,
						INDEX_BACKGROUND );
				hssfStyle.setPattern( RangeStyle.PatternSolid );
				hssfStyle.setPatternBG( rgb( cdx ) );
				hssfStyle.setPatternFG( rgb( cdx ) );
			}

			if ( style.getLeftBorderStyle( ) != null
					&& !"none".equals( style.getLeftBorderStyle( ) ) ) //$NON-NLS-1$
			{
				hssfStyle.setLeftBorder( getBorder( style.getLeftBorderWidth( ),
						style.getLeftBorderStyle( ) ) );

				if ( hssfStyle.getLeftBorder( ) != RangeStyle.BorderNone )
				{
					color = style.getLeftBorderColor( );
					if ( color != null )
					{
						hssfStyle.setLeftBorderColor( rgb( getHssfColorIndex( color,
								colorFlag,
								INDEX_BORDER_LEFT ) ) );
					}
				}
			}
			if ( style.getRightBorderStyle( ) != null
					&& !"none".equals( style.getRightBorderStyle( ) ) ) //$NON-NLS-1$
			{
				hssfStyle.setRightBorder( getBorder( style.getRightBorderWidth( ),
						style.getRightBorderStyle( ) ) );

				if ( hssfStyle.getRightBorder( ) != RangeStyle.BorderNone )
				{
					color = style.getRightBorderColor( );
					if ( color != null )
					{
						hssfStyle.setRightBorderColor( rgb( getHssfColorIndex( color,
								colorFlag,
								INDEX_BORDER_RIGHT ) ) );
					}
				}
			}
			if ( style.getTopBorderStyle( ) != null
					&& !"none".equals( style.getTopBorderStyle( ) ) ) //$NON-NLS-1$
			{
				hssfStyle.setTopBorder( getBorder( style.getTopBorderWidth( ),
						style.getTopBorderStyle( ) ) );

				if ( hssfStyle.getTopBorder( ) != RangeStyle.BorderNone )
				{
					color = style.getTopBorderColor( );
					if ( color != null )
					{
						hssfStyle.setTopBorderColor( rgb( getHssfColorIndex( color,
								colorFlag,
								INDEX_BORDER_TOP ) ) );
					}
				}
			}
			if ( style.getBottomBorderStyle( ) != null
					&& !"none".equals( style.getBottomBorderStyle( ) ) ) //$NON-NLS-1$
			{
				hssfStyle.setBottomBorder( getBorder( style.getBottomBorderWidth( ),
						style.getBottomBorderStyle( ) ) );

				if ( hssfStyle.getBottomBorder( ) != RangeStyle.BorderNone )
				{
					color = style.getBottomBorderColor( );
					if ( color != null )
					{
						hssfStyle.setBottomBorderColor( rgb( getHssfColorIndex( color,
								colorFlag,
								INDEX_BORDER_BOTTOM ) ) );
					}
				}
			}

			hssfStyle.setHorizontalAlignment( getAlign( style.getTextAlign( ), true ) );
			hssfStyle.setVerticalAlignment( getAlign( style.getVerticalAlign( ),
					false ) );
			hssfStyle.setWordWrap( true );

			// if ( style.getNumberFormat( ) != null
			// && style.getNumberFormat( ).length( ) > 0 )
			// {
			// short builtInFormat = HSSFDataFormat.getBuiltinFormat(
			// style.getNumberFormat( ) );
			// if ( builtInFormat != -1 )
			// {
			// hssfStyle.setDataFormat( builtInFormat );
			// }
			// }

			if ( styleEntry == null )
			{
				styleEntry = new RangeStyle[2];
				styleEntry[useLinkStyle ? 1 : 0] = hssfStyle;

				styleCache.put( style, styleEntry );
			}
			else
			{
				styleEntry[useLinkStyle ? 1 : 0] = hssfStyle;
			}
		}
		return hssfStyle;
	}

	private void setFont( Style style, boolean useLinkStyle,
			Color[] colorFlag, RangeStyle cellStyle )
	{
		short forecolor;
		if ( useLinkStyle )
		{
			forecolor = HSSFColor.BLUE.index;
		}
		else
		{
			Color color = style.getColor( );
			forecolor = color == null ? HSSFColor.BLACK.index
					: getHssfColorIndex( color, colorFlag, INDEX_FONT );
		}

		String fontName = style.getFontFamily( ) == null ? "Serif" //$NON-NLS-1$
				: style.getFontFamily( );
		short fontSize = style.getFontSize( ) == 0 ? 10
				: (short) ( style.getFontSize( ) / 1000d );
		short underline = useLinkStyle ? ( RangeStyle.UnderlineSingle )
				: ( style.isTextUnderline( ) ? RangeStyle.UnderlineSingle 
						: RangeStyle.UnderlineNone );
		boolean strikeout = style.isTextLineThrough( );
		boolean boldweight = CSSConstants.CSS_BOLD_VALUE.equals( style.getFontWeight( ) );
		boolean italic = ( CSSConstants.CSS_OBLIQUE_VALUE.equals( style.getFontStyle( ) ) || CSSConstants.CSS_ITALIC_VALUE.equals( style.getFontStyle( ) ) );


		cellStyle.setFontName( fontName );
		cellStyle.setFontColor( rgb ( forecolor ) );
		cellStyle.setFontSize( fontSize );
		cellStyle.setFontUnderline( underline );
		cellStyle.setFontStrikeout( strikeout );
		cellStyle.setFontBold( boldweight );
		cellStyle.setFontItalic( italic );
	}

	private short getHssfColorIndex( Color awtColor, Color[] colorFlag,
			int colorIndex )
	{
		HSSFColor color = null;

		if ( hssfColorMap != null && hssfColorMap.size( ) > 0 )
		{
			HSSFColor crtColor = null;
			short[] rgb = null;
			int diff = 0;
			int minDiff = 999;

			for ( Iterator<HSSFColor> it = hssfColorMap.values( ).iterator( ); it.hasNext( ); )
			{
				crtColor = it.next( );
				rgb = crtColor.getTriplet( );

				if ( rgb[0] == awtColor.getRed( )
						&& rgb[1] == awtColor.getGreen( )
						&& rgb[2] == awtColor.getBlue( ) )
				{
					// precise match
					diff = -Integer.MAX_VALUE;
					color = crtColor;
					break;
				}
				else
				{

					diff = Math.abs( rgb[0] - awtColor.getRed( ) )
							+ Math.abs( rgb[1] - awtColor.getGreen( ) )
							+ Math.abs( rgb[2] - awtColor.getBlue( ) );

					if ( diff < minDiff )
					{
						minDiff = diff;
						color = crtColor;
					}
				}
			}
		}

		if ( color != null )
		{
			return color.getIndex( );
		}
		return HSSFColor.WHITE.index;
	}

	private short getBorder( int borderWidth, String borderStyle )
	{
		int width = (int) ( ( borderWidth + 500 ) / 1000d );

		switch ( width )
		{
			case 0 :
				return RangeStyle.BorderNone;
			case 1 :
			{
				if ( "none".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return RangeStyle.BorderNone;
				}
				if ( "double".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return RangeStyle.BorderDouble;
				}
				if ( "solid".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return RangeStyle.BorderThin;
				}
				if ( "dashed".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return RangeStyle.BorderDashed;
				}
				if ( "dotted".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return RangeStyle.BorderDotted;
				}
				return RangeStyle.BorderThin;
			}
			case 2 :
			default :
			{
				// equal or greater than 2.
				if ( "none".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return RangeStyle.BorderNone;
				}
				if ( "double".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return RangeStyle.BorderDouble;
				}
				if ( "solid".equals( borderStyle ) ) //$NON-NLS-1$
				{
					if ( width == 2 )
					{
						return RangeStyle.BorderMedium;
					}
					else
					{
						return RangeStyle.BorderThick;
					}
				}
				if ( "dashed".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return RangeStyle.BorderDashed;
				}
				if ( "dotted".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return RangeStyle.BorderDotted;
				}
				if ( width == 2 )
				{
					return RangeStyle.BorderMedium;
				}
				else
				{
					return RangeStyle.BorderThick;
				}
			}
		}
	}

	private short getAlign( String align, boolean horizontal )
	{
		if ( horizontal )
		{
			short horizontalAlignment = RangeStyle.HorizontalAlignmentLeft;

			if ( "center".equals( align ) ) //$NON-NLS-1$
			{
				horizontalAlignment = RangeStyle.HorizontalAlignmentCenter;
			}
			else if ( "right".equals( align ) ) //$NON-NLS-1$
			{
				horizontalAlignment = RangeStyle.HorizontalAlignmentRight;
			}
			else if ( "justify".equals( align ) ) //$NON-NLS-1$
			{
				horizontalAlignment = RangeStyle.HorizontalAlignmentJustify;
			}
			return horizontalAlignment;
		}
		else
		{
			short verticalAlignment = RangeStyle.VerticalAlignmentCenter;

			if ( "top".equals( align ) ) //$NON-NLS-1$
			{
				verticalAlignment = RangeStyle.VerticalAlignmentTop;
			}
			else if ( "bottom".equals( align ) ) //$NON-NLS-1$
			{
				verticalAlignment = RangeStyle.VerticalAlignmentBottom;
			}
			else if ( "justify".equals( align ) ) //$NON-NLS-1$
			{
				verticalAlignment = RangeStyle.VerticalAlignmentJustify;
			}
			return verticalAlignment;
		}
	}
}
