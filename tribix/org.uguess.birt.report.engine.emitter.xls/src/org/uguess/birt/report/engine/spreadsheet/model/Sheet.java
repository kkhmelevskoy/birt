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

package org.uguess.birt.report.engine.spreadsheet.model;


import java.util.Iterator;
import java.util.Map;

import org.uguess.birt.report.engine.layout.wrapper.Style;
import org.uguess.birt.report.engine.spreadsheet.wrapper.Coordinate;

/**
 * This interface represents a sheet object.
 */
public interface Sheet extends Block
{

    /**
     * Returns the maximum active row index.
     * 
     * @return
     */
    int getActiveRowRange();

    /**
     * Returns the maximum active column index.
     * 
     * @return
     */
    int getActiveColumnRange();

    /**
     * Returns the name of current sheet.
     * 
     * @return
     */
    String getName();

    /**
     * Sets name of current sheet.
     * 
     * @param name
     */
    void setName(String name);

    /**
     * Returns the row object in given row index.
     * 
     * @param row Row index.
     * @param create If this is true, a new row object will be created if it's
     *            not already existing.
     * @return
     */
    Row getRow(int row, boolean create);

    /**
     * Returns a default row representation.
     * 
     * @return
     */
    Row getDefaultRow();

    /**
     * Returns the height of specific row.
     * 
     * @param row
     * @return
     */
    double getRowHeight(int row);

    /**
     * Sets the height of specific row.
     * 
     * @param row
     * @param height
     * @return
     */
    Row setRowHeight(int row, double height);

    /**
     * Return the column object in given index.
     * 
     * @param col Column index.
     * @param create If this is true, a new column object will be created if
     *            it's not already existing.
     * @return
     */
    Column getColumn(int col, boolean create);

    /**
     * Returns a default column representation.
     * 
     * @return
     */
    Column getDefaultColumn();

    /**
     * Returns the width of specific column.
     * 
     * @param col
     * @return
     */
    double getColumnWidth(int col);

    /**
     * Sets the width of specific column.
     * 
     * @param col
     * @param width
     * @return
     */
    Column setColumnWidth(int col, double width);

    /**
     * Sets cell value in given row, column.
     * 
     * @param row
     * @param col
     * @param value
     * @return
     */
    Cell setCell(int row, int col, Object value);

    /**
     * Sets cell value and style in given row, column.
     * 
     * @param row
     * @param col
     * @param value
     * @param style
     * @return
     */
    Cell setCell(int row, int col, Object value, Style style);

    /**
     * Sets cell value, formula and style in given row, column.
     * 
     * @param row
     * @param col
     * @param value
     * @param formula
     * @param style
     * @return
     */
    Cell setCell(int row, int col, Object value, String formula, Style style);

    /**
     * Returns the cell in given row, column.
     * 
     * @param row
     * @param col
     * @param create If this is true, a new cell will be created if it's not
     *            already existing.
     * @return
     */
    Cell getCell(int row, int col, boolean create);

    /**
     * Returns a default cell representation.
     * 
     * @return
     */
    Cell getDefaultCell();

    /**
     * Adds a new merge block.
     * 
     * @param startRow
     * @param startColumn
     * @param endRow
     * @param endColumn
     * @return
     */
    MergeBlock addMergeBlock(int startRow, int startColumn, int endRow,
        int endColumn);

    /**
     * Returns if the cell in given row, column is merged.
     * 
     * @param row
     * @param col
     * @return
     */
    boolean isMerged(int row, int col);

    /**
     * Returns the merge block in given row, column.
     * 
     * @param row
     * @param col
     * @return
     */
    MergeBlock getMergeBlock(int row, int col);

    /**
     * Returns the iterator for all merge blocks.
     * 
     * @return
     */
    Iterator<MergeBlock> mergesIterator();
    
    Map<String, Coordinate> getTableCoords();
    
    void addTableCoord(String name, Coordinate coord);
}