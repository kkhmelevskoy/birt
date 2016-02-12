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
import org.uguess.birt.report.engine.spreadsheet.model.Cell;
import org.uguess.birt.report.engine.spreadsheet.model.MergeBlock;
import org.uguess.birt.report.engine.spreadsheet.model.impl.CellImpl;


/**
 * CellPool
 */
public class CellPool
{

    private HashMap<Cell, Cell> cells = new HashMap<Cell, Cell>();

    public CellPool()
    {
    }

    public Cell getCell(Cell cell)
    {
        Object obj = cells.get(cell);
        if (obj == null)
        {
            cells.put(cell, cell);
        }
        else
        {
            cell = (Cell) obj;
        }
        return cell;
    }

    public Cell getCell(Cell cell, Style style)
    {
        Cell newCell = CellImpl.create(cell);
        newCell.setStyle(style);
        return getCell(newCell);
    }

    public Cell getCell(Cell cell, MergeBlock merge)
    {
        if (cell.isEmpty() && merge.isEmpty())
        {
            return cell;
        }
        else
        {
            Cell newCell = CellImpl.create(cell);
            newCell.setMergeBlock(merge);
            return getCell(newCell);
        }
    }

    public Cell getCell(Cell cell, Object value)
    {
        boolean diff = false;
        Object thisValue = cell.getValue();
        if (thisValue == null)
        {
            if (value != null)
            {
                diff = true;
            }
        }
        else
        {
            diff = (!thisValue.equals(value));
        }

        if (diff)
        {
            Cell newCell = CellImpl.create(cell);
            newCell.setValue(value);
            cell = getCell(newCell);
        }
        return cell;
    }
}
