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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
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

	private HSSFWorkbook workbook;

	private HSSFCellStyle emptyCellStyle;

	private Map<?, HSSFColor> hssfColorMap;

	private Map<Color, Integer> userColors;

	private Map<HSSFCellStyle, Color[]> variantStyles;

	private List<HSSFFont> fontCache;

	private Map<Style, HSSFCellStyle[]> styleCache;

	@SuppressWarnings("unchecked")
	XlsStyleProcessor( HSSFWorkbook workbook )
	{
		this.workbook = workbook;

		this.fontCache = new ArrayList<HSSFFont>( );
		this.styleCache = new HashMap<Style, HSSFCellStyle[]>( );
		this.hssfColorMap = HSSFColor.getIndexHash( );
		this.userColors = new HashMap<Color, Integer>( );
		// reserve white, black, blue colors
		this.userColors.put( Color.white, -HSSFColor.WHITE.index );
		this.userColors.put( Color.black, -HSSFColor.BLACK.index );
		this.userColors.put( Color.blue, -HSSFColor.BLUE.index );
		this.variantStyles = new HashMap<HSSFCellStyle, Color[]>( );

		initEmptyCellStyle( );
	}

	public void dispose( )
	{
		fontCache.clear( );
		styleCache.clear( );
		hssfColorMap.clear( );
		userColors.clear( );
		variantStyles.clear( );

		fontCache = null;
		styleCache = null;
		hssfColorMap = null;
		userColors = null;
		variantStyles = null;
		emptyCellStyle = null;

		workbook = null;
	}

	private void initEmptyCellStyle( )
	{
		emptyCellStyle = workbook.createCellStyle( );

		emptyCellStyle.setFillPattern( HSSFCellStyle.SOLID_FOREGROUND );
		emptyCellStyle.setAlignment( HSSFCellStyle.ALIGN_CENTER );
		emptyCellStyle.setHidden( false );
		emptyCellStyle.setLocked( false );
		emptyCellStyle.setBorderLeft( HSSFCellStyle.BORDER_NONE );
		emptyCellStyle.setBorderRight( HSSFCellStyle.BORDER_NONE );
		emptyCellStyle.setBorderTop( HSSFCellStyle.BORDER_NONE );
		emptyCellStyle.setBorderBottom( HSSFCellStyle.BORDER_NONE );

		short backColorIndex = HSSFColor.BLACK.index;
		emptyCellStyle.setBottomBorderColor( backColorIndex );
		emptyCellStyle.setTopBorderColor( backColorIndex );
		emptyCellStyle.setLeftBorderColor( backColorIndex );
		emptyCellStyle.setRightBorderColor( backColorIndex );
		emptyCellStyle.setFillForegroundColor( HSSFColor.WHITE.index );
		emptyCellStyle.setFillBackgroundColor( HSSFColor.WHITE.index );
		emptyCellStyle.setVerticalAlignment( HSSFCellStyle.VERTICAL_CENTER );

		HSSFFont defaultFont = workbook.createFont( );
		defaultFont.setFontName( "Serif" ); //$NON-NLS-1$
		short size = 10;
		defaultFont.setFontHeightInPoints( size );
		emptyCellStyle.setFont( defaultFont );
	}

	public HSSFCellStyle getEmptyCellStyle( )
	{
		return emptyCellStyle;
	}

	public void optimize( )
	{
		// check all used colors and re-organize the color palette
		if ( userColors.size( ) <= 56 )
		{
			// to simplify the logic, we only handle the cases that the user
			// color count <= 56. The max size of the Excel custom palette is
			// 56, indexed from 0x8-0x40 inclusive.

			Set<Integer> checkSet = new HashSet<Integer>( );
			Map<Color, Integer> colorMap = new HashMap<Color, Integer>( );

			// first collect all precise matched indices, so to skip them in
			// later custom mapping
			for ( Entry<Color, Integer> uc : userColors.entrySet( ) )
			{
				int idx = uc.getValue( );

				if ( idx < 0 )
				{
					checkSet.add( -idx );
				}
			}

			HSSFPalette palette = workbook.getCustomPalette( );
			int searchStart = 0x8;

			// update the palette index, assign a slot for each non-prcise
			// matched user color
			for ( Entry<Color, Integer> uc : userColors.entrySet( ) )
			{
				int idx = uc.getValue( );

				if ( idx > 0 )
				{
					Color c = uc.getKey( );

					for ( int i = searchStart; i <= 0x40; i++ )
					{
						if ( checkSet.contains( i ) )
						{
							continue;
						}

						palette.setColorAtIndex( (short) i,
								(byte) c.getRed( ),
								(byte) c.getGreen( ),
								(byte) c.getBlue( ) );

						colorMap.put( c, i );

						searchStart = i + 1;

						break;
					}
				}
			}

			// revisit the previous recorded cell styles to use the new palette
			// indices
			for ( Entry<HSSFCellStyle, Color[]> ent : variantStyles.entrySet( ) )
			{
				HSSFCellStyle style = ent.getKey( );
				Color[] cc = ent.getValue( );

				for ( int i = INDEX_FONT; i <= INDEX_BORDER_BOTTOM; i++ )
				{
					Color c = cc[i];

					if ( c != null )
					{
						Integer nIdxObj = colorMap.get( c );

						if ( nIdxObj != null )
						{
							short nIdx = nIdxObj.shortValue( );

							switch ( i )
							{
								case INDEX_FONT :
									style.getFont( workbook ).setColor( nIdx );
									break;
								case INDEX_BACKGROUND :
									style.setFillBackgroundColor( nIdx );
									style.setFillForegroundColor( nIdx );
									break;
								case INDEX_BORDER_LEFT :
									style.setLeftBorderColor( nIdx );
									break;
								case INDEX_BORDER_RIGHT :
									style.setRightBorderColor( nIdx );
									break;
								case INDEX_BORDER_TOP :
									style.setTopBorderColor( nIdx );
									break;
								case INDEX_BORDER_BOTTOM :
									style.setBottomBorderColor( nIdx );
									break;
							}
						}
					}
				}
			}
		}
	}

	public int getHssfPictureType( byte[] data )
	{
		int type = ImageUtil.getImageType( data );

		switch ( type )
		{
			case ImageUtil.TYPE_DIB :
				return HSSFWorkbook.PICTURE_TYPE_DIB;
			case ImageUtil.TYPE_PNG :
				return HSSFWorkbook.PICTURE_TYPE_PNG;
			case ImageUtil.TYPE_JPEG :
				return HSSFWorkbook.PICTURE_TYPE_JPEG;
		}

		return -1;
	}

	public HSSFCellStyle getHssfCellStyle( Style style, boolean useLinkStyle )
	{
		HSSFCellStyle hssfStyle = null;

		if ( style == null || style.isEmpty( ) )
		{
			return emptyCellStyle;
		}

		HSSFCellStyle[] styleEntry = null;

		// check existing cell style cache first.
		for ( Iterator<Entry<Style, HSSFCellStyle[]>> itr = styleCache.entrySet( )
				.iterator( ); itr.hasNext( ); )
		{
			Entry<Style, HSSFCellStyle[]> entry = itr.next( );

			if ( style.equals( entry.getKey( ) ) )
			{
				styleEntry = entry.getValue( );
				hssfStyle = styleEntry[useLinkStyle ? 1 : 0];
				break;
			}
		}

		if ( hssfStyle == null )
		{
			hssfStyle = workbook.createCellStyle( );

			Color[] colorFlag = new Color[6];

			hssfStyle.setFont( getHssfFont( style, useLinkStyle, colorFlag ) );

			Color color = style.getBackgroundColor( );
			if ( color != null )
			{
				short cdx = getHssfColorIndex( color,
						colorFlag,
						INDEX_BACKGROUND );
				hssfStyle.setFillPattern( HSSFCellStyle.SOLID_FOREGROUND );
				hssfStyle.setFillBackgroundColor( cdx );
				hssfStyle.setFillForegroundColor( cdx );
			}

			if ( style.getLeftBorderStyle( ) != null
					&& !"none".equals( style.getLeftBorderStyle( ) ) ) //$NON-NLS-1$
			{
				hssfStyle.setBorderLeft( getHssfBorder( style.getLeftBorderWidth( ),
						style.getLeftBorderStyle( ) ) );

				if ( hssfStyle.getBorderLeft( ) != HSSFCellStyle.BORDER_NONE )
				{
					color = style.getLeftBorderColor( );
					if ( color != null )
					{
						hssfStyle.setLeftBorderColor( getHssfColorIndex( color,
								colorFlag,
								INDEX_BORDER_LEFT ) );
					}
				}
			}
			if ( style.getRightBorderStyle( ) != null
					&& !"none".equals( style.getRightBorderStyle( ) ) ) //$NON-NLS-1$
			{
				hssfStyle.setBorderRight( getHssfBorder( style.getRightBorderWidth( ),
						style.getRightBorderStyle( ) ) );

				if ( hssfStyle.getBorderRight( ) != HSSFCellStyle.BORDER_NONE )
				{
					color = style.getRightBorderColor( );
					if ( color != null )
					{
						hssfStyle.setRightBorderColor( getHssfColorIndex( color,
								colorFlag,
								INDEX_BORDER_RIGHT ) );
					}
				}
			}
			if ( style.getTopBorderStyle( ) != null
					&& !"none".equals( style.getTopBorderStyle( ) ) ) //$NON-NLS-1$
			{
				hssfStyle.setBorderTop( getHssfBorder( style.getTopBorderWidth( ),
						style.getTopBorderStyle( ) ) );

				if ( hssfStyle.getBorderTop( ) != HSSFCellStyle.BORDER_NONE )
				{
					color = style.getTopBorderColor( );
					if ( color != null )
					{
						hssfStyle.setTopBorderColor( getHssfColorIndex( color,
								colorFlag,
								INDEX_BORDER_TOP ) );
					}
				}
			}
			if ( style.getBottomBorderStyle( ) != null
					&& !"none".equals( style.getBottomBorderStyle( ) ) ) //$NON-NLS-1$
			{
				hssfStyle.setBorderBottom( getHssfBorder( style.getBottomBorderWidth( ),
						style.getBottomBorderStyle( ) ) );

				if ( hssfStyle.getBorderBottom( ) != HSSFCellStyle.BORDER_NONE )
				{
					color = style.getBottomBorderColor( );
					if ( color != null )
					{
						hssfStyle.setBottomBorderColor( getHssfColorIndex( color,
								colorFlag,
								INDEX_BORDER_BOTTOM ) );
					}
				}
			}

			hssfStyle.setAlignment( getHssfAlign( style.getTextAlign( ), true ) );
			hssfStyle.setVerticalAlignment( getHssfAlign( style.getVerticalAlign( ),
					false ) );
			hssfStyle.setWrapText( true );

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

			for ( Color c : colorFlag )
			{
				if ( c != null )
				{
					// record the style if it has a non-precise matched color
					variantStyles.put( hssfStyle, colorFlag );
					break;
				}
			}

			if ( styleEntry == null )
			{
				styleEntry = new HSSFCellStyle[2];
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

	private HSSFFont getHssfFont( Style style, boolean useLinkStyle,
			Color[] colorFlag )
	{
		HSSFFont cellFont = null;

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
		byte underline = useLinkStyle ? ( HSSFFont.U_SINGLE )
				: ( style.isTextUnderline( ) ? HSSFFont.U_SINGLE
						: HSSFFont.U_NONE );
		boolean strikeout = style.isTextLineThrough( );
		short boldweight = CSSConstants.CSS_BOLD_VALUE.equals( style.getFontWeight( ) ) ? HSSFFont.BOLDWEIGHT_BOLD
				: HSSFFont.BOLDWEIGHT_NORMAL;
		boolean italic = ( CSSConstants.CSS_OBLIQUE_VALUE.equals( style.getFontStyle( ) ) || CSSConstants.CSS_ITALIC_VALUE.equals( style.getFontStyle( ) ) );

		// search cache to reuse existing same font.
		for ( Iterator<HSSFFont> itr = fontCache.iterator( ); itr.hasNext( ); )
		{
			HSSFFont font = itr.next( );

			if ( font.getColor( ) == forecolor
					&& font.getFontName( ).equals( fontName )
					&& font.getFontHeightInPoints( ) == fontSize
					&& font.getUnderline( ) == underline
					&& font.getStrikeout( ) == strikeout
					&& font.getBoldweight( ) == boldweight
					&& font.getItalic( ) == italic )
			{
				cellFont = font;
				break;
			}
		}

		if ( cellFont == null )
		{
			cellFont = workbook.createFont( );
			cellFont.setFontName( fontName );
			cellFont.setColor( forecolor );
			cellFont.setFontHeightInPoints( fontSize );
			cellFont.setUnderline( underline );
			cellFont.setStrikeout( strikeout );
			cellFont.setBoldweight( boldweight );
			cellFont.setItalic( italic );

			fontCache.add( cellFont );
		}
		return cellFont;
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

			// record used colors
			if ( diff != -Integer.MAX_VALUE )
			{
				colorFlag[colorIndex] = awtColor;
				userColors.put( awtColor, (int) color.getIndex( ) );
			}
			else
			{
				// negative index indicates a precise match
				userColors.put( awtColor, -color.getIndex( ) );
			}
		}

		if ( color != null )
		{
			return color.getIndex( );
		}
		return HSSFColor.WHITE.index;
	}

	private short getHssfBorder( int borderWidth, String borderStyle )
	{
		int width = (int) ( ( borderWidth + 500 ) / 1000d );

		switch ( width )
		{
			case 0 :
				return HSSFCellStyle.BORDER_NONE;
			case 1 :
			{
				if ( "none".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return HSSFCellStyle.BORDER_NONE;
				}
				if ( "double".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return HSSFCellStyle.BORDER_DOUBLE;
				}
				if ( "solid".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return HSSFCellStyle.BORDER_THIN;
				}
				if ( "dashed".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return HSSFCellStyle.BORDER_DASHED;
				}
				if ( "dotted".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return HSSFCellStyle.BORDER_DOTTED;
				}
				return HSSFCellStyle.BORDER_THIN;
			}
			case 2 :
			default :
			{
				// equal or greater than 2.
				if ( "none".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return HSSFCellStyle.BORDER_NONE;
				}
				if ( "double".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return HSSFCellStyle.BORDER_DOUBLE;
				}
				if ( "solid".equals( borderStyle ) ) //$NON-NLS-1$
				{
					if ( width == 2 )
					{
						return HSSFCellStyle.BORDER_MEDIUM;
					}
					else
					{
						return HSSFCellStyle.BORDER_THICK;
					}
				}
				if ( "dashed".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return HSSFCellStyle.BORDER_DASHED;
				}
				if ( "dotted".equals( borderStyle ) ) //$NON-NLS-1$
				{
					return HSSFCellStyle.BORDER_DOTTED;
				}
				if ( width == 2 )
				{
					return HSSFCellStyle.BORDER_MEDIUM;
				}
				else
				{
					return HSSFCellStyle.BORDER_THICK;
				}
			}
		}
	}

	private short getHssfAlign( String align, boolean horizontal )
	{
		if ( horizontal )
		{
			short horizontalAlignment = HSSFCellStyle.ALIGN_LEFT;

			if ( "center".equals( align ) ) //$NON-NLS-1$
			{
				horizontalAlignment = HSSFCellStyle.ALIGN_CENTER;
			}
			else if ( "right".equals( align ) ) //$NON-NLS-1$
			{
				horizontalAlignment = HSSFCellStyle.ALIGN_RIGHT;
			}
			else if ( "justify".equals( align ) ) //$NON-NLS-1$
			{
				horizontalAlignment = HSSFCellStyle.ALIGN_JUSTIFY;
			}
			return horizontalAlignment;
		}
		else
		{
			short verticalAlignment = HSSFCellStyle.VERTICAL_CENTER;

			if ( "top".equals( align ) ) //$NON-NLS-1$
			{
				verticalAlignment = HSSFCellStyle.VERTICAL_TOP;
			}
			else if ( "bottom".equals( align ) ) //$NON-NLS-1$
			{
				verticalAlignment = HSSFCellStyle.VERTICAL_BOTTOM;
			}
			else if ( "justify".equals( align ) ) //$NON-NLS-1$
			{
				verticalAlignment = HSSFCellStyle.VERTICAL_JUSTIFY;
			}
			return verticalAlignment;
		}
	}
}
