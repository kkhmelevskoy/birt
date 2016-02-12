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


import java.util.LinkedList;

import org.uguess.birt.report.engine.layout.wrapper.Style;
import org.uguess.birt.report.engine.spreadsheet.model.Book;
import org.uguess.birt.report.engine.spreadsheet.model.Sheet;


/**
 * BookImpl
 */
public class BookImpl extends BlockImpl implements Book
{

    private String name;
    private LinkedList<Sheet> sheets;

    public static Book create(String name)
    {
        BookImpl bi = new BookImpl(name);
        return bi;
    }

    protected BookImpl(String name)
    {
        super(Style.EMPTON);

        this.name = name;
        sheets = new LinkedList<Sheet>();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Sheet addSheet(Object anchor, Sheet sheet)
    {
        if (sheet == null)
        {
            throw new IllegalArgumentException("Invalid sheet object"); //$NON-NLS-1$
        }

        int index = getInsertPoint(anchor);
        if (index < 0 || index > sheets.size())
        {
            index = sheets.size();
        }

        sheets.add(index, sheet);
        return sheet;
    }

    public Sheet addSheet(Object anchor, String name)
    {
        int index = getInsertPoint(anchor);
        if (index < 0 || index > sheets.size())
        {
            index = sheets.size();
        }

        Sheet sheet = SheetImpl.create(name, Style.EMPTON);
        sheets.add(index, sheet);
        return sheet;
    }

    public Sheet getSheet(Object objIndex)
    {
        int index = getSheetIndex(objIndex);

        Sheet sheet = null;
        if (index >= 0 && index < sheets.size())
        {
            sheet = sheets.get(index);
        }

        return sheet;
    }

    public int getSheetCount()
    {
        return sheets.size();
    }

    public void removeAllSheets()
    {
        sheets.clear();
    }

    private int getInsertPoint(Object anchor)
    {
        int index = getSheetIndex(anchor);

        if (!(anchor instanceof Number) && index >= 0)
        {
            index++;
        }

        return index;
    }

    private int getSheetIndex(Object anchor)
    {
        int index = -1;
        if (anchor instanceof Sheet)
        {
            index = sheets.indexOf(anchor);
        }
        else if (anchor instanceof String)
        {
            String name = (String) anchor;
            for (int i = 0; i < sheets.size(); i++)
            {
                Sheet sheet = sheets.get(i);
                if ((name == null && sheet.getName() == null)
                    || (name != null && name.equals(sheet.getName())))
                {
                    index = i;
                    break;
                }
            }
        }
        else if (anchor instanceof Number)
        {
            index = ((Number) anchor).intValue();
        }
        else
        {
            throw new IllegalArgumentException("Invalid index value"); //$NON-NLS-1$
        }

        return index;
    }

    public Sheet removeSheet(Object obj)
    {
        int index = getSheetIndex(obj);

        Sheet sheet = null;
        if (index >= 0 && index < sheets.size())
        {
            sheet = sheets.remove(index);
        }

        return sheet;
    }

    public boolean renameSheet(Object obj, String newName)
    {
        int index = getSheetIndex(obj);

        if (index >= 0 && index < sheets.size())
        {
            Sheet sheet = sheets.get(index);
            sheet.setName(newName);
            return true;
        }

        return false;
    }
}