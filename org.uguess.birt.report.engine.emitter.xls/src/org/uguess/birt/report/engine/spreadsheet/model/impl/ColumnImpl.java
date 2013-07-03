/********************************************************************************
 * (C) Copyright 2000-2005, by Shawn Qualia. This library is free software; you
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

package org.uguess.birt.report.engine.spreadsheet.model.impl;


import org.uguess.birt.report.engine.layout.wrapper.Style;
import org.uguess.birt.report.engine.spreadsheet.model.Column;
import org.uguess.birt.report.engine.spreadsheet.model.util.ModelConstants;


/**
 * ColumnImpl
 */
public class ColumnImpl extends BlockImpl implements Column
{

    private double width;

    public static Column create(Column col)
    {
        ColumnImpl ci = new ColumnImpl(col.getStyle());
        ci.width = col.getWidth();
        return ci;
    }

    public static Column create(Style style)
    {
        ColumnImpl ci = new ColumnImpl(style);
        return ci;
    }

    protected ColumnImpl(Style style)
    {
        super(style);
        width = ModelConstants.DEFAULT_COLUMNWIDTH;
    }

    public int hashCode()
    {
        int hash = super.hashCode() ^ (int) width;
        return hash;
    }

    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj == null || !(obj instanceof Column))
        {
            return false;
        }

        Column col = (Column) obj;
        return width == col.getWidth() && super.equals(col);
    }

    public double getWidth()
    {
        return width;
    }

    public void setWidth(double width)
    {
        this.width = (width < 0) ? ModelConstants.DEFAULT_COLUMNWIDTH : width;
    }

    public String toString()
    {
        return super.toString() + "[" + width + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
