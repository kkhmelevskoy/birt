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


import org.uguess.birt.report.engine.spreadsheet.model.MergeBlock;


/**
 * MergeBlockImpl
 */
public class MergeBlockImpl implements MergeBlock
{

    private static MergeBlock EMPTY_BLOCK = MergeBlockImpl.create(-1, -1, -1,
        -1);

    private int col, row;
    private int colSpan, rowSpan;

    public static MergeBlock createEmpty()
    {
        return EMPTY_BLOCK;
    }

    public static MergeBlock create(int startRow, int startColumn, int endRow,
        int endColumn)
    {
        MergeBlockImpl mbl = new MergeBlockImpl();
        mbl.col = Math.min(startColumn, endColumn);
        mbl.colSpan = Math.max(startColumn, endColumn) - mbl.col;
        mbl.row = Math.min(startRow, endRow);
        mbl.rowSpan = Math.max(startRow, endRow) - mbl.row;
        return mbl;
    }

    protected MergeBlockImpl()
    {
    }

    public boolean isEmpty()
    {
        return this == EMPTY_BLOCK;
    }

    public int getEndColumn()
    {
        return col + colSpan;
    }

    public int getEndRow()
    {
        return row + rowSpan;
    }

    public boolean include(int row, int col)
    {
        int w = colSpan;
        int h = rowSpan;
        if ((w | h) < 0)
        {
            return false;
        }

        int x = this.col;
        int y = this.row;
        if (col < x || row < y)
        {
            return false;
        }
        w += x;
        h += y;

        return ((w < x || w >= col) && (h < y || h >= row));
    }

    public boolean include(MergeBlock merge)
    {
        return include(merge.getStartRow(), merge.getStartColumn())
            && include(merge.getEndRow(), merge.getEndColumn());
    }

    public boolean cross(MergeBlock merge)
    {
        int tw = this.colSpan;
        int th = this.rowSpan;
        int rw = merge.getColumnSpan();
        int rh = merge.getRowSpan();
        int tx = this.col;
        int ty = this.row;
        int rx = merge.getStartColumn();
        int ry = merge.getStartRow();
        rw += rx;
        rh += ry;
        tw += tx;
        th += ty;

        return ((rw < rx || rw >= tx) && (rh < ry || rh >= ty)
            && (tw < tx || tw >= rx) && (th < ty || th >= ry));
    }

    public int getStartRow()
    {
        return row;
    }

    public int getStartColumn()
    {
        return col;
    }

    public int getRowSpan()
    {
        return rowSpan;
    }

    public int getColumnSpan()
    {
        return colSpan;
    }

    public int hashCode()
    {
        int hash = row;
        hash = 31 * hash + col;
        hash = 31 * hash + rowSpan;
        hash = 31 * hash + colSpan;
        return hash;
    }

    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (obj == null || !(obj instanceof MergeBlockImpl))
        {
            return false;
        }

        MergeBlockImpl that = (MergeBlockImpl) obj;
        return this.row == that.row && this.col == that.col
            && this.rowSpan == that.rowSpan && this.colSpan == that.colSpan;
    }

}
