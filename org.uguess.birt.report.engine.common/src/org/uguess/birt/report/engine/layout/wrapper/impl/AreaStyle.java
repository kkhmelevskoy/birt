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

import org.eclipse.birt.report.engine.content.IStyle;
import org.eclipse.birt.report.engine.css.engine.value.css.CSSConstants;
import org.eclipse.birt.report.engine.layout.pdf.font.FontInfo;
import org.eclipse.birt.report.engine.nLayout.area.IArea;
import org.eclipse.birt.report.engine.nLayout.area.IContainerArea;
import org.eclipse.birt.report.engine.nLayout.area.ITextArea;
import org.eclipse.birt.report.engine.nLayout.area.impl.CellArea;
import org.eclipse.birt.report.engine.nLayout.area.style.BackgroundImageInfo;
import org.eclipse.birt.report.engine.nLayout.area.style.BorderInfo;
import org.eclipse.birt.report.engine.nLayout.area.style.BoxStyle;
import org.eclipse.birt.report.engine.nLayout.area.style.TextStyle;
import org.uguess.birt.report.engine.layout.wrapper.Style;


/**
 * AreaStyle
 */
public final class AreaStyle implements Style
{

    private BoxStyle box;
    private TextStyle text;
    private String vAlign;

    static Style populate(IArea area)
    {
        BoxStyle box = null;
        TextStyle text = null;
        String vAlign = null;

        if (area instanceof IContainerArea)
        {
            box = ((IContainerArea) area).getBoxStyle();
        }

        if (area instanceof ITextArea)
        {
            text = ((ITextArea) area).getTextStyle();
        }

        if (area instanceof HasArea)
        {
            IArea realArea = ((HasArea) area).getArea();
            if (realArea instanceof CellArea)
            {
                IStyle style = ((CellArea) realArea).getContent().getStyle();
                vAlign = style.getVerticalAlign();
            }
        }

        box = checkEmpty(box);
        text = checkEmpty(text);

        if (text != null || box != null || vAlign != null)
        {
            return new AreaStyle(box, text, vAlign);
        }

        return null;
    }

    private static BoxStyle checkEmpty(BoxStyle bs)
    {
        // TODO this is an experimental check, may change accodring to BIRT
        // code change
        if (bs != null)
        {
            if (bs.getBackgroundColor() == null
                && bs.getBackgroundImage() == null
                && bs.getBottomBorder() == null && bs.getTopBorder() == null
                && bs.getLeftBorder() == null && bs.getRightBorder() == null)
            {
                return null;
            }
        }

        return bs;
    }

    private static TextStyle checkEmpty(TextStyle ts)
    {
        // TODO check?
        return ts;
    }

    AreaStyle(BoxStyle box, TextStyle text, String vAlign)
    {
        this.box = box;
        this.text = text;
        this.vAlign = vAlign;
    }

    public Color getBackgroundColor()
    {
        if (box != null)
        {
            return box.getBackgroundColor();
        }
        return null;
    }

    public String getBackgroundImage()
    {
        if (box != null)
        {
            BackgroundImageInfo info = box.getBackgroundImage();

            if (info != null)
            {
                return info.getUrl();
            }
        }
        return null;
    }

    public String getBackgroundRepeat()
    {
        if (box != null)
        {
            BackgroundImageInfo info = box.getBackgroundImage();

            if (info != null)
            {
                int mode = info.getRepeatedMode();

                switch (mode)
                {
                    case BackgroundImageInfo.NO_REPEAT:
                        return CSSConstants.CSS_NO_REPEAT_VALUE;
                    case BackgroundImageInfo.REPEAT:
                        return CSSConstants.CSS_REPEAT_VALUE;
                    case BackgroundImageInfo.REPEAT_X:
                        return CSSConstants.CSS_REPEAT_X_VALUE;
                    case BackgroundImageInfo.REPEAT_Y:
                        return CSSConstants.CSS_REPEAT_Y_VALUE;
                }
            }
        }
        return null;
    }

    public Color getBottomBorderColor()
    {
        if (box != null)
        {
            return box.getBottomBorderColor();
        }
        return null;
    }

    private String mapBorderStyle(int style)
    {
        switch (style)
        {
            case BorderInfo.BORDER_STYLE_DASHED:
                return CSSConstants.CSS_DASHED_VALUE;
            case BorderInfo.BORDER_STYLE_DOTTED:
                return CSSConstants.CSS_DOTTED_VALUE;
            case BorderInfo.BORDER_STYLE_DOUBLE:
                return CSSConstants.CSS_DOUBLE_VALUE;
            case BorderInfo.BORDER_STYLE_GROOVE:
                return CSSConstants.CSS_GROOVE_VALUE;
            case BorderInfo.BORDER_STYLE_HIDDEN:
                return CSSConstants.CSS_HIDDEN_VALUE;
            case BorderInfo.BORDER_STYLE_INSET:
                return CSSConstants.CSS_INSET_VALUE;
            case BorderInfo.BORDER_STYLE_OUTSET:
                return CSSConstants.CSS_OUTSET_VALUE;
            case BorderInfo.BORDER_STYLE_RIDGE:
                return CSSConstants.CSS_RIDGE_VALUE;
            case BorderInfo.BORDER_STYLE_SOLID:
                return CSSConstants.CSS_SOLID_VALUE;
                // case BorderInfo.BORDER_STYLE_NONE :
                // return CSSConstants.CSS_NONE_VALUE;
        }
        return null;
    }

    public String getBottomBorderStyle()
    {
        if (box != null)
        {
            return mapBorderStyle(box.getBottomBorderStyle());
        }
        return null;
    }

    public int getBottomBorderWidth()
    {
        if (box != null)
        {
            return box.getBottomBorderWidth();
        }
        return 0;
    }

    public Color getColor()
    {
        if (text != null)
        {
            return text.getColor();
        }
        return null;
    }

    public String getFontFamily()
    {
        if (text != null)
        {
            FontInfo info = text.getFontInfo();

            if (info != null)
            {
                return info.getFontName();
            }
        }
        return null;
    }

    public int getFontSize()
    {
        if (text != null)
        {
            return text.getFontSize();
        }
        return 0;
    }

    public String getFontStyle()
    {
        if (text != null)
        {
            FontInfo info = text.getFontInfo();

            if (info != null)
            {
                int style = info.getFontStyle();

                // italic
                if ((style & 0x2) != 0)
                {
                    return CSSConstants.CSS_ITALIC_VALUE;
                }
            }
        }
        return null;
    }

    public String getFontWeight()
    {
        if (text != null)
        {
            FontInfo info = text.getFontInfo();

            if (info != null)
            {
                int style = info.getFontStyle();

                // bold
                if ((style & 0x1) != 0)
                {
                    return CSSConstants.CSS_BOLD_VALUE;
                }
            }
        }
        return null;
    }

    public Color getLeftBorderColor()
    {
        if (box != null)
        {
            return box.getLeftBorderColor();
        }
        return null;
    }

    public String getLeftBorderStyle()
    {
        if (box != null)
        {
            return mapBorderStyle(box.getLeftBorderStyle());
        }
        return null;
    }

    public int getLeftBorderWidth()
    {
        if (box != null)
        {
            return box.getLeftBorderWidth();
        }
        return 0;
    }

    public Color getRightBorderColor()
    {
        if (box != null)
        {
            return box.getRightBorderColor();
        }
        return null;
    }

    public String getRightBorderStyle()
    {
        if (box != null)
        {
            return mapBorderStyle(box.getRightBorderStyle());
        }
        return null;
    }

    public int getRightBorderWidth()
    {
        if (box != null)
        {
            return box.getRightBorderWidth();
        }
        return 0;
    }

    public String getTextAlign()
    {
        if (text != null)
        {
            if (text.getAlign() != null)
            {
                return text.getAlign().getCssText();
            }
        }
        return null;
    }

    public Color getTopBorderColor()
    {
        if (box != null)
        {
            return box.getTopBorderColor();
        }
        return null;
    }

    public String getTopBorderStyle()
    {
        if (box != null)
        {
            return mapBorderStyle(box.getTopBorderStyle());
        }
        return null;
    }

    public int getTopBorderWidth()
    {
        if (box != null)
        {
            return box.getTopBorderWidth();
        }
        return 0;
    }

    public String getVerticalAlign()
    {
        return vAlign;
    }

    public boolean isEmpty()
    {
        return false;
    }

    public boolean isTextLineThrough()
    {
        if (text != null)
        {
            return text.isLinethrough();
        }
        return false;
    }

    public boolean isTextOverline()
    {
        if (text != null)
        {
            return text.isOverline();
        }
        return false;
    }

    public boolean isTextUnderline()
    {
        if (text != null)
        {
            return text.isUnderline();
        }
        return false;
    }

}
