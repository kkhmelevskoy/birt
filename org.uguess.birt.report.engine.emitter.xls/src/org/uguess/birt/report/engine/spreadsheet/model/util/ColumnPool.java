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

package org.uguess.birt.report.engine.spreadsheet.model.util;


import java.util.HashMap;

import org.uguess.birt.report.engine.layout.wrapper.Style;
import org.uguess.birt.report.engine.spreadsheet.model.Column;
import org.uguess.birt.report.engine.spreadsheet.model.impl.ColumnImpl;


/**
 * ColumnPool
 */
public class ColumnPool
{

    private HashMap<Column, Column> cols = new HashMap<Column, Column>();

    public ColumnPool()
    {
    }

    public Column getColumn(Column col)
    {
        Object obj = cols.get(col);
        if (obj == null)
        {
            cols.put(col, col);
        }
        else
        {
            col = (Column) obj;
        }
        return col;
    }

    public Column getColumn(Column col, Style style)
    {
        Column newCol = ColumnImpl.create(col);
        newCol.setStyle(style);
        return getColumn(newCol);
    }

    public Column getColumn(Column col, double width)
    {
        width = (width < 0) ? ModelConstants.DEFAULT_COLUMNWIDTH : width;

        Column newCol = ColumnImpl.create(col);
        newCol.setWidth(Math.abs(width));
        return getColumn(newCol);
    }
}