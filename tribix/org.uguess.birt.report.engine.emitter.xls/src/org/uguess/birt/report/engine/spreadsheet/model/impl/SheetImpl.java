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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.uguess.birt.report.engine.layout.wrapper.Style;
import org.uguess.birt.report.engine.spreadsheet.model.Cell;
import org.uguess.birt.report.engine.spreadsheet.model.Column;
import org.uguess.birt.report.engine.spreadsheet.model.MergeBlock;
import org.uguess.birt.report.engine.spreadsheet.model.Row;
import org.uguess.birt.report.engine.spreadsheet.model.Sheet;
import org.uguess.birt.report.engine.spreadsheet.model.util.CellPool;
import org.uguess.birt.report.engine.spreadsheet.model.util.ColumnPool;
import org.uguess.birt.report.engine.spreadsheet.model.util.ModelConstants;
import org.uguess.birt.report.engine.spreadsheet.model.util.RowPool;
import org.uguess.birt.report.engine.spreadsheet.wrapper.Coordinate;


/**
 * SheetImpl
 */
public class SheetImpl extends BlockImpl implements Sheet
{

    private String name;

    private HashMap<Integer, Row> rows;
    private HashMap<Integer, Column> cols;
    private HashMap<Long, Cell> cells;
    private LinkedList<MergeBlock> merges;

    private final Cell defaultCell;
    private final Row defaultRow;
    private final Column defaultColumn;

    private final CellPool cellPool;
    private final RowPool rowPool;
    private final ColumnPool columnPool;
    
    private HashMap<String, Coordinate> tableCoordMap;

    public static Sheet create(String name, Style style)
    {
        return new SheetImpl(name, style);
    }

    protected SheetImpl(String name, Style style)
    {
        super(style);

        this.name = name;
        rows = new HashMap<Integer, Row>();
        cols = new HashMap<Integer, Column>();
        cells = new HashMap<Long, Cell>();
        merges = new LinkedList<MergeBlock>();

        cellPool = new CellPool();
        rowPool = new RowPool();
        columnPool = new ColumnPool();

        defaultCell = CellImpl.create(null, getStyle());
        defaultRow = RowImpl.create(getStyle());
        defaultRow.setHeight(10);
        defaultColumn = ColumnImpl.create(getStyle());
        defaultColumn.setWidth(20);
        
        tableCoordMap = new HashMap<String, Coordinate>();
    }

    public int getActiveRowRange()
    {
        return getActiveRange(true);
    }

    public int getActiveColumnRange()
    {
        return getActiveRange(false);
    }

    private int getActiveRange(boolean isRow)
    {
        int max = 0;

        Iterator<Integer> itr = isRow ? rows.keySet().iterator() : cols
            .keySet().iterator();
        while (itr.hasNext())
        {
            int v = itr.next();
            max = Math.max(max, v);
        }

        Iterator<Entry<Long, Cell>> entryItr = cells.entrySet().iterator();
        while (entryItr.hasNext())
        {
            Entry<Long, Cell> e = entryItr.next();
            long rowcol = e.getKey();
            int v = isRow ? (int) (rowcol >> 32)
                : (int) (rowcol & 0x00000000ffffffff);
            max = Math.max(max, v);
        }
        return max;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    private Long getCellsHashKey(int row, int col)
    {
        return new Long((((long) row) << 32)
            | (((long) col) & 0x00000000ffffffff));
    }

    public Cell setCell(int row, int col, Object value, Style style)
    {
        return setCell(row, col, value, null, style);
    }

    public Cell setCell(int row, int col, Object value, String formula,
        Style style)
    {
        if (style == null)
        {
            if (value == null)
            {
                return defaultCell;
            }

            style = Style.EMPTON;
        }

        Cell cell = cellPool.getCell(CellImpl.create(value, formula, style));
        return setCell(row, col, cell, true);
    }

    public Cell getCell(int row, int col, boolean create)
    {
        Cell cell = null;
        Object obj = cells.get(getCellsHashKey(row, col));
        if (obj == null)
        {
            if (create)
            {
                cell = CellImpl.create(null, Style.EMPTON);
                cell = setCell(row, col, cellPool.getCell(cell), false);
            }
            else
            {
                cell = defaultCell;
            }
        }
        else
        {
            cell = (Cell) obj;
        }
        return cell;
    }

    public Cell getDefaultCell()
    {
        return defaultCell;
    }

    public Cell setCell(int row, int col, Object value)
    {
        Cell cell = null;
        if (value == null)
        {
            cell = getCell(row, col, false);
            if (cell != defaultCell)
            {
                cell = cellPool.getCell(cell, (Object) null);
                cell = setCell(row, col, cell, true);
            }
        }
        else
        {
            cell = cellPool.getCell(getCell(row, col, false), value);
            cell = setCell(row, col, cell, true);
        }
        return cell;
    }

    private Cell setCell(int row, int col, Cell cell, boolean careMerge)
    {
        MergeBlock merge = null;
        boolean merged = false;

        if (careMerge)
        {
            merge = getMergeBlock(row, col);
            merged = (!merge.isEmpty());
        }

        Long key = getCellsHashKey(row, col);
        if (cell.isEmpty())
        {
            if (merged)
            {
                cell = cellPool.getCell(cell, merge);
                cells.put(key, cell);
            }
            else
            {
                cells.remove(key);
                cell = defaultCell;
            }
        }
        else
        {
            if (merged)
            {
                cell = cellPool.getCell(cell, merge);
            }
            cells.put(key, cell);
        }
        return cell;
    }

    public Column getColumn(int col, boolean create)
    {
        Column c = null;
        Object obj = cols.get(new Integer(col));
        if (obj == null)
        {
            if (create)
            {
                c = new ColumnImpl(getStyle());
                c = setColumn(col, columnPool.getColumn(c));
            }
            else
            {
                c = defaultColumn;
            }
        }
        else
        {
            c = (Column) obj;
        }
        return c;
    }

    public Column getDefaultColumn()
    {
        return defaultColumn;
    }

    public double getColumnWidth(int col)
    {
        double width = getColumn(col, false).getWidth();
        if (width == ModelConstants.DEFAULT_COLUMNWIDTH)
        {
            width = defaultColumn.getWidth();
        }
        return width;
    }

    public Column setColumnWidth(int col, double width)
    {
        Column c = columnPool.getColumn(getColumn(col, true), width);
        return setColumn(col, c);
    }

    public Row getRow(int row, boolean create)
    {
        Row r = null;
        Object obj = rows.get(new Integer(row));
        if (obj == null)
        {
            if (create)
            {
                r = new RowImpl(getStyle());
                r = setRow(row, rowPool.getRow(r));
            }
            else
            {
                r = defaultRow;
            }
        }
        else
        {
            r = (Row) obj;
        }
        return r;
    }

    public Row getDefaultRow()
    {
        return defaultRow;
    }

    public double getRowHeight(int row)
    {
        double height = getRow(row, false).getHeight();
        if (height == ModelConstants.DEFAULT_ROWHEIGHT)
        {
            height = defaultRow.getHeight();
        }
        return height;
    }

    public Row setRowHeight(int row, double height)
    {
        Row r = rowPool.getRow(getRow(row, true), height);
        return setRow(row, r);
    }

    private Row setRow(int row, Row r)
    {
        rows.put(new Integer(row), r);
        return r;
    }

    private Column setColumn(int col, Column c)
    {
        cols.put(new Integer(col), c);
        return c;
    }

    public MergeBlock addMergeBlock(int startRow, int startColumn, int endRow,
        int endColumn)
    {
        MergeBlock merge = MergeBlockImpl.createEmpty();

        if (startRow == endRow && startColumn == endColumn)
        {
            // This is a single cell.
            return merge;
        }

        ArrayList<MergeBlock> removes = new ArrayList<MergeBlock>();
        MergeBlock mbNew = MergeBlockImpl.create(startRow, startColumn, endRow,
            endColumn);

        Iterator<MergeBlock> itr = merges.iterator();
        while (itr.hasNext())
        {
            merge = itr.next();
            if (mbNew.cross(merge))
            {
                if (mbNew.equals(merge))
                {
                    // if already a same merge block exists.
                    return merge;
                }
                else if (mbNew.include(merge))
                {
                    // if already an overlapped merge block exists, we
                    // should remove it.
                    removes.add(merge);
                    itr.remove();
                }
                else
                {
                    // can't merge with conflict merge blocks,
                    // restore removed and return empty.
                    merges.addAll(removes);
                    return MergeBlockImpl.createEmpty();
                }
            }
        }

        // Updates all cells within the removed merge blocks.
        for (int i = 0; i < removes.size(); i++)
        {
            merge = removes.get(i);
            updateCellMergeUnderMergeBlock(merge, MergeBlockImpl.createEmpty());
        }

        merge = mbNew;
        merges.add(merge);

        // Updates all cells within the new merge block.
        if (!merge.isEmpty())
        {
            updateCellMergeUnderMergeBlock(merge, merge);
        }
        return merge;
    }

    private void updateCellMergeUnderMergeBlock(MergeBlock mergeRange,
        MergeBlock mergeValue)
    {
        if (mergeRange.isEmpty())
        {
            return;
        }

        boolean create = !mergeValue.isEmpty();

        for (int i = mergeRange.getStartRow(); i <= mergeRange.getEndRow(); i++)
        {
            for (int j = mergeRange.getStartColumn(); j <= mergeRange
                .getEndColumn(); j++)
            {
                setCell(i, j,
                    cellPool.getCell(getCell(i, j, create), mergeValue), false);
            }
        }
    }

    public boolean isMerged(int row, int col)
    {
        return (!getMergeBlock(row, col).isEmpty());
    }

    public MergeBlock getMergeBlock(int row, int col)
    {
        Cell cell = getCell(row, col, false);
        if (cell != defaultCell && cell.isMerged())
        {
            return cell.getMergeBlock();
        }

        return MergeBlockImpl.createEmpty();
    }

    public MergeBlock removeMergeBlock(int row, int col)
    {
        if (!isMerged(row, col))
        {
            return MergeBlockImpl.createEmpty();
        }

        MergeBlock merge = MergeBlockImpl.createEmpty();
        Iterator<MergeBlock> itr = merges.iterator();
        while (itr.hasNext())
        {
            MergeBlock mg = itr.next();
            if (mg.include(row, col))
            {
                merge = mg;
                itr.remove();
                break;
            }
        }

        updateCellMergeUnderMergeBlock(merge, MergeBlockImpl.createEmpty());
        return merge;
    }

    public void removeAllMergeBlocks()
    {
        Iterator<MergeBlock> itr = merges.iterator();
        while (itr.hasNext())
        {
            MergeBlock merge = itr.next();
            updateCellMergeUnderMergeBlock(merge, MergeBlockImpl.createEmpty());
        }
        merges.clear();
    }

    public Iterator<MergeBlock> mergesIterator()
    {
        return merges.iterator();
    }
    
    @Override
    public Map<String, Coordinate> getTableCoords()
    {
        return tableCoordMap;
    }
    
    public void addTableCoord(String name, Coordinate coord)
    {
        tableCoordMap.put(name, coord);
    }
}