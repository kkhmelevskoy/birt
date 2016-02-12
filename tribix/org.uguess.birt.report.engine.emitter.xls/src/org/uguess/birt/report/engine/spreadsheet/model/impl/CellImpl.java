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
import org.uguess.birt.report.engine.spreadsheet.model.Cell;
import org.uguess.birt.report.engine.spreadsheet.model.MergeBlock;


/**
 * CellImpl
 */
public class CellImpl extends BlockImpl implements Cell
{

    private Object value;
    private String formula;
    private MergeBlock merge;

    public static Cell create(Object value, Style style)
    {
        CellImpl ci = new CellImpl(style);
        ci.value = value;
        return ci;
    }

    public static Cell create(Object value, String formula, Style style)
    {
        CellImpl ci = new CellImpl(style);
        ci.value = value;
        ci.formula = formula;
        return ci;
    }

    public static Cell create(Cell cell)
    {
        CellImpl ci = new CellImpl(cell.getStyle());
        ci.setMergeBlock(cell.getMergeBlock());
        ci.value = cell.getValue();
        ci.formula = cell.getFormula();
        return ci;
    }

    protected CellImpl(Style style)
    {
        super(style);

        this.merge = MergeBlockImpl.createEmpty();
    }

    public int hashCode()
    {
        int hash = super.hashCode();
        if (value != null)
        {
            hash = hash ^ (value.hashCode());
        }
        if (formula != null)
        {
            hash = hash ^ (formula.hashCode());
        }
        if (!merge.isEmpty())
        {
            hash ^= merge.hashCode();
        }
        return hash;
    }

    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (obj == null || !(obj instanceof Cell))
        {
            return false;
        }

        Cell cell = (Cell) obj;
        if (!super.equals(cell))
        {
            return false;
        }

        if (value == null)
        {
            if (cell.getValue() != null)
            {
                return false;
            }
        }
        else if (!value.equals(cell.getValue()))
        {
            return false;
        }

        if (formula == null)
        {
            if (cell.getFormula() != null)
            {
                return false;
            }
        }
        else if (!formula.equals(cell.getFormula()))
        {
            return false;
        }

        if (!merge.equals(cell.getMergeBlock()))
        {
            return false;
        }
        return true;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public String getFormula()
    {
        return formula;
    }

    public void setFormula(String formula)
    {
        this.formula = formula;
    }

    public String getText()
    {
        if (value == null)
        {
            return ""; //$NON-NLS-1$
        }
        return value.toString();
    }

    public boolean isMerged()
    {
        return (!merge.isEmpty());
    }

    public MergeBlock getMergeBlock()
    {
        return merge;
    }

    public void setMergeBlock(MergeBlock merge)
    {
        this.merge = (merge == null) ? MergeBlockImpl.createEmpty() : merge;
    }

    public boolean isEmpty()
    {
        return value == null && formula == null && merge.isEmpty()
            && super.isEmpty();
    }

    public String toString()
    {
        return super.toString() + "[" + value + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
