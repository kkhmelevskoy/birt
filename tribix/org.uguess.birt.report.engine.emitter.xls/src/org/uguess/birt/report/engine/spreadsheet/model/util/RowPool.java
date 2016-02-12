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
import org.uguess.birt.report.engine.spreadsheet.model.Row;
import org.uguess.birt.report.engine.spreadsheet.model.impl.RowImpl;


/**
 * RowPool
 */
public class RowPool
{

    private HashMap<Row, Row> rows = new HashMap<Row, Row>();

    public RowPool()
    {
    }

    public Row getRow(Row row)
    {
        Object obj = rows.get(row);
        if (obj == null)
        {
            rows.put(row, row);
        }
        else
        {
            row = (Row) obj;
        }
        return row;
    }

    public Row getRow(Row row, Style style)
    {
        Row newRow = RowImpl.create(row);
        newRow.setStyle(style);
        return getRow(newRow);
    }

    public Row getRow(Row row, double height)
    {
        height = (height < 0) ? ModelConstants.DEFAULT_ROWHEIGHT : height;

        Row newRow = RowImpl.create(row);
        newRow.setHeight(height);
        return getRow(newRow);
    }
}