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
import org.uguess.birt.report.engine.spreadsheet.model.Row;
import org.uguess.birt.report.engine.spreadsheet.model.util.ModelConstants;


/**
 * RowImpl
 */
public class RowImpl extends BlockImpl implements Row
{

    private double height;

    public static Row create(Row row)
    {
        RowImpl ri = new RowImpl(row.getStyle());
        ri.height = row.getHeight();
        return ri;
    }

    public static Row create(Style style)
    {
        RowImpl ri = new RowImpl(style);
        return ri;
    }

    protected RowImpl(Style style)
    {
        super(style);
        height = ModelConstants.DEFAULT_ROWHEIGHT;
    }

    public int hashCode()
    {
        int hash = super.hashCode() ^ (int) height;
        return hash;
    }

    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (obj == null || !(obj instanceof Row))
        {
            return false;
        }

        Row row = (Row) obj;
        return height == row.getHeight() && super.equals(row);
    }

    public double getHeight()
    {
        return height;
    }

    public void setHeight(double height)
    {
        this.height = (height < 0) ? ModelConstants.DEFAULT_ROWHEIGHT : height;
    }

    public String toString()
    {
        return super.toString() + "[" + height + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
