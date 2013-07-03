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


/**
 * This interface represents a generic book object.
 */
public interface Book extends Block
{

    /**
     * @return Returns the name of this book.
     */
    String getName();

    /**
     * Sets the book name.
     * 
     * @param name
     */
    void setName(String name);

    /**
     * Add new sheet with specified name.
     * 
     * @param anchor the existing sheet object or the existing sheet name or an
     *            index number. If this is a valid sheet object or name, the new
     *            sheet will be placed after it; if this is an index, the sheet
     *            will be inserted at this indexed position; in other cases, the
     *            sheet will be appended at the end.
     * @param name unique name of the sheet.
     * @return
     */
    Sheet addSheet(Object anchor, String name);

    /**
     * Add an existing sheet to the book.
     * 
     * @param anchor the existing sheet object or the existing sheet name or an
     *            index number. If this is a valid sheet object or name, the new
     *            sheet will be placed after it; if this is an index, the sheet
     *            will be inserted at this indexed position; in other cases, the
     *            sheet will be appended at the end.
     * @param name an existing sheet object.
     * @return
     */
    Sheet addSheet(Object anchor, Sheet sheet);

    /**
     * @param index could be a sheet name or sheet index.
     * @return
     */
    Sheet getSheet(Object index);

    /**
     * @param sheet could be a Sheet object, or sheet name, or sheet index.
     * @return
     */
    Sheet removeSheet(Object sheet);

    /**
     * @param sheet could be a Sheet object, or sheet name, or sheet index.
     * @param newName
     * @return
     */
    boolean renameSheet(Object sheet, String newName);

    /**
     * @return Returns the sheet count in this book.
     */
    int getSheetCount();

    /**
     * Removes all the sheets in this book.
     */
    void removeAllSheets();
}