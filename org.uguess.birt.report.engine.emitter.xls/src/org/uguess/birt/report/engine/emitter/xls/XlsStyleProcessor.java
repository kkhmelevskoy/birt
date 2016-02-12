/********************************************************************************
 * (C) Copyright 2000-2008, by Shawn Qualia. This library is free software; you
 * can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version. This
 * library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. in the
 * United States and other countries.]
 ********************************************************************************/

package org.uguess.birt.report.engine.emitter.xls;


import java.awt.Color;

import org.eclipse.birt.report.engine.css.engine.value.css.CSSConstants;
import org.uguess.birt.report.engine.layout.wrapper.Style;
import org.uguess.birt.report.engine.spreadsheet.model.Cell;
import org.uguess.birt.report.engine.spreadsheet.model.MergeBlock;
import org.uguess.birt.report.engine.spreadsheet.model.Sheet;
import org.uguess.birt.report.engine.util.ImageUtil;

import com.smartxls.RangeStyle;
import com.smartxls.WorkBook;


/**
 * XlsStyleProcessor
 */
public class XlsStyleProcessor
{

    private static final int MIN_FONT_SIZE = 20;
    private static final int INDEX_FONT = 0;
    private static final int INDEX_BACKGROUND = 1;
    private static final int INDEX_BORDER_LEFT = 2;
    private static final int INDEX_BORDER_RIGHT = 3;
    private static final int INDEX_BORDER_TOP = 4;
    private static final int INDEX_BORDER_BOTTOM = 5;

    private WorkBook workbook;

    private RangeStyle emptyCellStyle;

    private RangeStyle emptyCellStyleMerged;

    @SuppressWarnings("unchecked")
    XlsStyleProcessor(WorkBook workbook)
    {
        this.workbook = workbook;

        emptyCellStyle = initEmptyCellStyle(false);
        emptyCellStyleMerged = initEmptyCellStyle(true);
    }

    public void dispose()
    {
        emptyCellStyle = null;
        emptyCellStyleMerged = null;

        workbook = null;
    }

    private RangeStyle initEmptyCellStyle(boolean merged)
    {
        RangeStyle emptyCellStyle;

        try
        {
            emptyCellStyle = workbook.getRangeStyle();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        emptyCellStyle.setPattern(RangeStyle.PatternSolid);

        emptyCellStyle
            .setHorizontalAlignment(RangeStyle.HorizontalAlignmentCenter);
        emptyCellStyle.setVerticalAlignment(RangeStyle.VerticalAlignmentCenter);

        // emptyCellStyle.setHidden( false );
        emptyCellStyle.setLocked(false);
        emptyCellStyle.setLeftBorder(RangeStyle.BorderNone);
        emptyCellStyle.setRightBorder(RangeStyle.BorderNone);
        emptyCellStyle.setTopBorder(RangeStyle.BorderNone);
        emptyCellStyle.setBottomBorder(RangeStyle.BorderNone);

        Color backColor = Color.BLACK;
        emptyCellStyle.setBottomBorderColor(backColor.getRGB());
        emptyCellStyle.setTopBorderColor(backColor.getRGB());
        emptyCellStyle.setLeftBorderColor(backColor.getRGB());
        emptyCellStyle.setRightBorderColor(backColor.getRGB());

        emptyCellStyle.setPatternFG(Color.WHITE.getRGB());
        emptyCellStyle.setPatternBG(Color.WHITE.getRGB());
        emptyCellStyle.setVerticalAlignment(RangeStyle.VerticalAlignmentCenter);

        emptyCellStyle.setFontName("Serif"); //$NON-NLS-1$
        short size = MIN_FONT_SIZE;
        emptyCellStyle.setFontSize(size);

        if (merged)
        {
            emptyCellStyle.setMergeCells(true);
        }

        return emptyCellStyle;
    }

    public RangeStyle getEmptyCellStyle(boolean merged)
    {
        return merged ? emptyCellStyleMerged : emptyCellStyle;
    }

    public int getPictureType(byte[] data)
    {
        int type = ImageUtil.getImageType(data);

        switch (type)
        {
        // there are no such constants in smartxls but these are correct
            case ImageUtil.TYPE_DIB:
                return 7; // HSSFWorkbook.PICTURE_TYPE_DIB;
            case ImageUtil.TYPE_PNG:
                return 6; // HSSFWorkbook.PICTURE_TYPE_PNG;
            case ImageUtil.TYPE_JPEG:
                return 5; // HSSFWorkbook.PICTURE_TYPE_JPEG;
        }

        return -1;
    }

    public RangeStyle getCellStyle(Cell element, short x, short y,
        Sheet modelSheet, boolean useLinkStyle, MergeBlock merge)
    {
        Style style = element.getStyle();

        RangeStyle hssfStyle = null;

        if (style == null || style.isEmpty())
        {
            return getEmptyCellStyle(merge != null);
        }

        if (hssfStyle == null)
        {
            try
            {
                hssfStyle = workbook.getRangeStyle();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }

            if (merge != null)
            {
                hssfStyle.setMergeCells(true);
            }

            Color[] colorFlag = new Color[6];

            setFont(style, useLinkStyle, colorFlag, hssfStyle);

            Color color = style.getBackgroundColor();
            if (color != null)
            {
                hssfStyle.setPattern(RangeStyle.PatternSolid);
                hssfStyle.setPatternBG(color.getRGB());
                hssfStyle.setPatternFG(color.getRGB());
            }

            if (style.getLeftBorderStyle() != null
                && !"none".equals(style.getLeftBorderStyle())) //$NON-NLS-1$
            {
                hssfStyle.setLeftBorder(getBorder(style.getLeftBorderWidth(),
                    style.getLeftBorderStyle()));

                if (hssfStyle.getLeftBorder() != RangeStyle.BorderNone)
                {
                    color = style.getLeftBorderColor();
                    if (color != null)
                    {
                        hssfStyle.setLeftBorderColor(color.getRGB());
                    }
                }
            }
            if (style.getRightBorderStyle() != null)
            {
                if (!"none".equals(style.getRightBorderStyle())) //$NON-NLS-1$
                {
                    hssfStyle.setRightBorder(getBorder(
                        style.getRightBorderWidth(),
                        style.getRightBorderStyle()));

                    if (hssfStyle.getRightBorder() != RangeStyle.BorderNone)
                    {
                        color = style.getRightBorderColor();
                        if (color != null)
                        {
                            hssfStyle.setRightBorderColor(color.getRGB());
                        }
                    }
                }
            }
            else if (merge != null)
            {
                Cell c = modelSheet.getCell(merge.getEndRow(),
                    merge.getEndColumn(), false);

                if (c != null)
                {
                    Style s = c.getStyle();

                    if (s != null && !s.isEmpty()
                        && !"none".equals(s.getRightBorderStyle())) //$NON-NLS-1$
                    {
                        hssfStyle.setRightBorder(getBorder(
                            s.getRightBorderWidth(), s.getRightBorderStyle()));

                        if (hssfStyle.getRightBorder() != RangeStyle.BorderNone)
                        {
                            color = s.getRightBorderColor();
                            if (color != null)
                            {
                                hssfStyle.setRightBorderColor(color.getRGB());
                            }
                        }
                    }
                }
            }
            if (style.getTopBorderStyle() != null
                && !"none".equals(style.getTopBorderStyle())) //$NON-NLS-1$
            {
                hssfStyle.setTopBorder(getBorder(style.getTopBorderWidth(),
                    style.getTopBorderStyle()));

                if (hssfStyle.getTopBorder() != RangeStyle.BorderNone)
                {
                    color = style.getTopBorderColor();
                    if (color != null)
                    {
                        hssfStyle.setTopBorderColor(color.getRGB());
                    }
                }
            }
            if (style.getBottomBorderStyle() != null
                && !"none".equals(style.getBottomBorderStyle())) //$NON-NLS-1$
            {
                hssfStyle
                    .setBottomBorder(getBorder(style.getBottomBorderWidth(),
                        style.getBottomBorderStyle()));

                if (hssfStyle.getBottomBorder() != RangeStyle.BorderNone)
                {
                    color = style.getBottomBorderColor();
                    if (color != null)
                    {
                        hssfStyle.setBottomBorderColor(color.getRGB());
                    }
                }
            }

            hssfStyle.setHorizontalAlignment(getAlign(style.getTextAlign(),
                true));
            hssfStyle.setVerticalAlignment(getAlign(style.getVerticalAlign(),
                false));
            hssfStyle.setWordWrap(true);

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
        }
        return hssfStyle;
    }

    private void setFont(Style style, boolean useLinkStyle, Color[] colorFlag,
        RangeStyle cellStyle)
    {
        Color forecolor;
        if (useLinkStyle)
        {
            forecolor = Color.BLUE;
        }
        else
        {
            Color color = style.getColor();
            forecolor = color == null ? Color.BLACK : color;
        }

        String fontName = style.getFontFamily() == null ? "Serif" //$NON-NLS-1$
            : style.getFontFamily();
        int fontSize = style.getFontSize() == 0 ? MIN_FONT_SIZE : style.getFontSize() / 50; // s.vladykin:
                                                                                 // magic
                                                                                 // font
                                                                                 // scale
                                                                                 // constant

        short underline = useLinkStyle ? (RangeStyle.UnderlineSingle) : (style
            .isTextUnderline() ? RangeStyle.UnderlineSingle
            : RangeStyle.UnderlineNone);
        boolean strikeout = style.isTextLineThrough();
        boolean boldweight = CSSConstants.CSS_BOLD_VALUE.equals(style
            .getFontWeight());
        boolean italic = (CSSConstants.CSS_OBLIQUE_VALUE.equals(style
            .getFontStyle()) || CSSConstants.CSS_ITALIC_VALUE.equals(style
            .getFontStyle()));

        cellStyle.setFontName(fontName);
        cellStyle.setFontColor(forecolor.getRGB());
        cellStyle.setFontSize(Math.max(MIN_FONT_SIZE, fontSize));
        cellStyle.setFontUnderline(underline);
        cellStyle.setFontStrikeout(strikeout);
        cellStyle.setFontBold(boldweight);
        cellStyle.setFontItalic(italic);
    }

    private short getBorder(int borderWidth, String borderStyle)
    {
        int width = (int) ((borderWidth + 500) / 1000d);

        switch (width)
        {
            case 0:
                return RangeStyle.BorderNone;
            case 1:
                {
                    if ("none".equals(borderStyle)) //$NON-NLS-1$
                    {
                        return RangeStyle.BorderNone;
                    }
                    if ("double".equals(borderStyle)) //$NON-NLS-1$
                    {
                        return RangeStyle.BorderDouble;
                    }
                    if ("solid".equals(borderStyle)) //$NON-NLS-1$
                    {
                        return RangeStyle.BorderThin;
                    }
                    if ("dashed".equals(borderStyle)) //$NON-NLS-1$
                    {
                        return RangeStyle.BorderDashed;
                    }
                    if ("dotted".equals(borderStyle)) //$NON-NLS-1$
                    {
                        return RangeStyle.BorderDotted;
                    }
                    return RangeStyle.BorderThin;
                }
            case 2:
            default:
                {
                    // equal or greater than 2.
                    if ("none".equals(borderStyle)) //$NON-NLS-1$
                    {
                        return RangeStyle.BorderNone;
                    }
                    if ("double".equals(borderStyle)) //$NON-NLS-1$
                    {
                        return RangeStyle.BorderDouble;
                    }
                    if ("solid".equals(borderStyle)) //$NON-NLS-1$
                    {
                        if (width == 2)
                        {
                            return RangeStyle.BorderMedium;
                        }
                        else
                        {
                            return RangeStyle.BorderThick;
                        }
                    }
                    if ("dashed".equals(borderStyle)) //$NON-NLS-1$
                    {
                        return RangeStyle.BorderDashed;
                    }
                    if ("dotted".equals(borderStyle)) //$NON-NLS-1$
                    {
                        return RangeStyle.BorderDotted;
                    }
                    if (width == 2)
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

    private short getAlign(String align, boolean horizontal)
    {
        if (horizontal)
        {
            short horizontalAlignment = RangeStyle.HorizontalAlignmentLeft;

            if ("center".equals(align)) //$NON-NLS-1$
            {
                horizontalAlignment = RangeStyle.HorizontalAlignmentCenter;
            }
            else if ("right".equals(align)) //$NON-NLS-1$
            {
                horizontalAlignment = RangeStyle.HorizontalAlignmentRight;
            }
            else if ("justify".equals(align)) //$NON-NLS-1$
            {
                horizontalAlignment = RangeStyle.HorizontalAlignmentJustify;
            }
            return horizontalAlignment;
        }
        else
        {
            short verticalAlignment = RangeStyle.VerticalAlignmentCenter;

            if ("top".equals(align)) //$NON-NLS-1$
            {
                verticalAlignment = RangeStyle.VerticalAlignmentTop;
            }
            else if ("bottom".equals(align)) //$NON-NLS-1$
            {
                verticalAlignment = RangeStyle.VerticalAlignmentBottom;
            }
            else if ("justify".equals(align)) //$NON-NLS-1$
            {
                verticalAlignment = RangeStyle.VerticalAlignmentJustify;
            }
            return verticalAlignment;
        }
    }
}
