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

package org.uguess.birt.report.engine.spreadsheet.wrapper;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.report.engine.css.engine.StyleConstants;
import org.eclipse.birt.report.engine.nLayout.area.IArea;
import org.eclipse.birt.report.engine.nLayout.area.impl.CellArea;
import org.eclipse.birt.report.engine.nLayout.area.impl.TableArea;
import org.uguess.birt.report.engine.emitter.xls.XlsRenderer2.AreaWrapper;
import org.uguess.birt.report.engine.layout.wrapper.Frame;
import org.uguess.birt.report.engine.layout.wrapper.Style;
import org.uguess.birt.report.engine.layout.wrapper.impl.LocalStyle;
import org.uguess.birt.report.engine.spreadsheet.model.Cell;
import org.uguess.birt.report.engine.spreadsheet.model.Sheet;
import org.uguess.birt.report.engine.spreadsheet.model.impl.SheetImpl;


public class Transformer
{

    private final static int PRECISION_THRESHOLD = 1;

    private int prec = PRECISION_THRESHOLD;
    private int fixedColumnWidth = -1;
    private int halfprec = prec / 2;

    public static Sheet toSheet(Frame frame)
    {
        return toSheet(frame, PRECISION_THRESHOLD);
    }

    public static Sheet toSheet(Frame frame, int precision)
    {
        return toSheet(frame, precision, -1);
    }

    public static Sheet toSheet(Frame frame, int precision, int fixedColumnWidth)
    {
        Transformer worker = new Transformer();
        worker.setPrecision(precision);
        worker.setFixedColumnWidth(fixedColumnWidth);
        return worker.frameToSheet(frame);
    }

    /**
     * The constructor.
     */
    private Transformer()
    {
    }

    private Sheet frameToSheet(Frame frame)
    {
        if (frame == null)
        {
            return null;
        }

        // TODO use real or automatic sheet name
        Sheet sheet = SheetImpl.create("x", frame.getStyle()); //$NON-NLS-1$
        Cell defaultCell = sheet.getDefaultCell();

        // compute fragments
        List<Integer> xFrags = new ArrayList<Integer>();
        List<Integer> yFrags = new ArrayList<Integer>();
        computeFragments(xFrags, yFrags, frame);

        if (fixedColumnWidth > 0 && xFrags.size() > 0)
        {
            // fill up the not-used columns
            Collections.sort(xFrags);

            int max = xFrags.get(xFrags.size() - 1);

            for (int i = 0; i < max; i += fixedColumnWidth)
            {
                Integer cut = new Integer(i);
                if (!xFrags.contains(cut))
                {
                    xFrags.add(cut);
                }
            }
        }

        Collections.sort(xFrags);
        Collections.sort(yFrags);

        // initialize sheet row/column by fragments count
        int old = 0;
        int current = 0;
        int xCellCount = xFrags.size() - 1;
        int yCellCount = yFrags.size() - 1;

        current = xFrags.get(0);
        for (int i = 0; i < xCellCount; i++)
        {
            old = current;
            current = xFrags.get(i + 1);
            sheet.setColumnWidth(i, (current - old));
        }

        current = yFrags.get(0);
        for (int i = 0; i < yCellCount; i++)
        {
            old = current;
            current = yFrags.get(i + 1);
            sheet.setRowHeight(i, (current - old));
        }

        // layout children
        layoutChildren(xFrags, yFrags, sheet, defaultCell, frame,
            frame.getStyle(), 0, 0);

        if (fixedColumnWidth > 0)
        {
            // fill up extra columns during layout
            int max = sheet.getActiveColumnRange();

            for (int i = xFrags.size() - 1; i <= max; i++)
            {
                sheet.setColumnWidth(i, fixedColumnWidth);
            }
        }

        return sheet;
    }

    private void setFixedColumnWidth(int width)
    {
        this.fixedColumnWidth = width;
    }

    private void setPrecision(int precision)
    {
        if (precision < PRECISION_THRESHOLD)
        {
            prec = PRECISION_THRESHOLD;
        }
        else
        {
            prec = precision;
        }
        halfprec = prec / 2;
    }

    private void computeFragments(List<Integer> xCuts, List<Integer> yCuts,
        Frame element)
    {
        computeFragments(xCuts, yCuts, element, 0, 0);
    }

    private IArea getArea(Frame frame)
    {
        if (frame == null)
        {
            return null;
        }

        Object data = frame.getData();
        if (data instanceof List)
        {
            data = ((List<?>) data).get(0);
        }

        IArea area = null;
        if (data instanceof AreaWrapper)
        {
            area = ((AreaWrapper) data).getArea();
        }
        else if (data instanceof IArea)
        {
            area = (IArea) data;
        }

        return area;
    }

    private void computeFragments(List<Integer> xCuts, List<Integer> yCuts,
        Frame element, int xOffset, int yOffset)
    {
        Coordinate rc = new Coordinate();
        Integer cut = null;

        rc.x1 = shear(element.getLeft() + xOffset);
        rc.y1 = shear(element.getTop() + yOffset);
        rc.x2 = shear(element.getRight() + xOffset);
        rc.y2 = shear(element.getBottom() + yOffset);

        // ignore zero width/height block
        if (rc.x1 == rc.x2 || rc.y1 == rc.y2)
        {
            return;
        }

        if (fixedColumnWidth > 0)
        {
            rc.x1 = shear(element.getLeft() + xOffset, fixedColumnWidth);
            rc.x2 = shear(element.getRight() + xOffset, fixedColumnWidth);
        }

        cut = new Integer(rc.x1);
        if (!xCuts.contains(cut))
        {
            xCuts.add(cut);
        }

        cut = new Integer(rc.x2);
        if (!xCuts.contains(cut))
        {
            xCuts.add(cut);
        }

        cut = new Integer(rc.y1);
        if (!yCuts.contains(cut))
        {
            yCuts.add(cut);
        }

        cut = new Integer(rc.y2);
        if (!yCuts.contains(cut))
        {
            yCuts.add(cut);
        }

        IArea area = getArea(element);
        if (area instanceof TableArea)
        {
            TableArea tableArea = (TableArea) area;
            System.out.println(tableArea.isCanShrink());
            System.out.println(tableArea.isGridDesign());
            System.out.println(tableArea.isIgnoreReordering());
            System.out.println(tableArea.isInlineStacking());
        }
        if (!(area instanceof CellArea))
        {
            for (Iterator<Frame> it = element.iterator(); it.hasNext();)
            {
                Frame frame = it.next();
                computeFragments(xCuts, yCuts, frame, xOffset, yOffset);
            }
        }
    }

    private boolean hasChildren(Frame elems)
    {
        return elems.iterator().hasNext();
    }

    private void setParentsStyle(Sheet sheet, Cell defaultCell,
        Coordinate coords, Style style)
    {
        int yi = 0;
        int xi = 0;
        Cell cell = null;

        yi = coords.y1;
        while (yi <= coords.y2)
        {
            xi = coords.x1;
            while (xi <= coords.x2)
            {
                cell = sheet.getCell(yi, xi, false);
                if (cell == defaultCell)
                {
                    LocalStyle newStyle = null;

                    // update edge cell of an element
                    if (xi != coords.x1)
                    {
                        if (newStyle == null)
                        {
                            newStyle = LocalStyle.create(style);
                        }

                        // set directly for optimization
                        newStyle.setProperty(
                            StyleConstants.STYLE_BORDER_LEFT_STYLE, null);
                    }
                    if (xi != coords.x2)
                    {
                        if (newStyle == null)
                        {
                            newStyle = LocalStyle.create(style);
                        }
                        newStyle.setProperty(
                            StyleConstants.STYLE_BORDER_RIGHT_STYLE, null);
                    }
                    if (yi != coords.y1)
                    {
                        if (newStyle == null)
                        {
                            newStyle = LocalStyle.create(style);
                        }
                        newStyle.setProperty(
                            StyleConstants.STYLE_BORDER_TOP_STYLE, null);
                    }
                    if (yi != coords.y2)
                    {
                        if (newStyle == null)
                        {
                            newStyle = LocalStyle.create(style);
                        }
                        newStyle.setProperty(
                            StyleConstants.STYLE_BORDER_BOTTOM_STYLE, null);
                    }

                    if (newStyle != null)
                    {
                        newStyle.revalidate();
                    }

                    sheet.setCell(yi, xi, null, newStyle != null ? newStyle
                        : style);
                }
                xi++;
            }
            yi++;
        }
    }

    private void setChildStyle(Sheet sheet, Coordinate coords, Style style)
    {
        int yi = 0;
        int xi = 0;

        yi = coords.y1;
        while (yi <= coords.y2)
        {
            xi = coords.x1;
            while (xi <= coords.x2)
            {
                LocalStyle newStyle = null;

                // update edge cell of an element
                if (xi != coords.x1)
                {
                    if (newStyle == null)
                    {
                        newStyle = LocalStyle.create(style);
                    }

                    // set directly for optimization
                    newStyle.setProperty(
                        StyleConstants.STYLE_BORDER_LEFT_STYLE, null);
                }
                if (xi != coords.x2)
                {
                    if (newStyle == null)
                    {
                        newStyle = LocalStyle.create(style);
                    }
                    newStyle.setProperty(
                        StyleConstants.STYLE_BORDER_RIGHT_STYLE, null);
                }
                if (yi != coords.y1)
                {
                    if (newStyle == null)
                    {
                        newStyle = LocalStyle.create(style);
                    }
                    newStyle.setProperty(StyleConstants.STYLE_BORDER_TOP_STYLE,
                        null);
                }
                if (yi != coords.y2)
                {
                    if (newStyle == null)
                    {
                        newStyle = LocalStyle.create(style);
                    }
                    newStyle.setProperty(
                        StyleConstants.STYLE_BORDER_BOTTOM_STYLE, null);
                }

                if (newStyle != null)
                {
                    newStyle.revalidate();
                }

                sheet
                    .setCell(yi, xi, null, newStyle != null ? newStyle : style);
                xi++;
            }
            yi++;
        }
    }

    private boolean adjustOverlap(Sheet sheet, Coordinate rc)
    {
        int yi = 0;
        int xi = 0;

        int x2max = rc.x2;
        int y2max = rc.y2;

        boolean xext = false;
        boolean yext = false;
        boolean done = false;

        Cell defaultCell = sheet.getDefaultCell();

        // get the first unmerged cell
        yi = rc.y1;
        FIRST: while (yi <= rc.y2)
        {
            xi = rc.x1;
            while (xi <= rc.x2)
            {
                Cell cell = sheet.getCell(yi, xi, false);
                if (cell == defaultCell && !cell.isMerged())
                {
                    xext = true;
                    yext = true;
                    done = true;
                    break FIRST;
                }
                xi++;
            }
            yi++;
        }

        rc.x1 = xi;
        rc.y1 = yi;
        rc.x2 = rc.x1;
        rc.y2 = rc.y1;

        while (xext || yext)
        {
            // check x extends
            if (xext)
            {
                if (rc.x2 < x2max)
                {
                    xi = rc.x2 + 1;
                    for (yi = rc.y1; yi <= rc.y2; yi++)
                    {
                        Cell cell = sheet.getCell(yi, xi, false);
                        if (cell != defaultCell || cell.isMerged())
                        {
                            xext = false;
                            break;
                        }
                    }
                    if (xext)
                    {
                        rc.x2++;
                    }
                }
                else
                {
                    xext = false;
                }
            }

            // check y extends
            if (yext)
            {
                if (rc.y2 < y2max)
                {
                    yi = rc.y2 + 1;
                    for (xi = rc.x1; xi <= rc.x2; xi++)
                    {
                        Cell cell = sheet.getCell(yi, xi, false);
                        if (cell != defaultCell || cell.isMerged())
                        {
                            yext = false;
                            break;
                        }
                    }
                    if (yext)
                    {
                        rc.y2++;
                    }
                }
                else
                {
                    yext = false;
                }
            }
        }

        return done;
    }

    private void mergeOverlap(Sheet sheet, Frame element, Coordinate coords,
        Object data, Style style)
    {
        if (coords.x1 == coords.x2 && coords.y1 == coords.y2)
        {
            // A single cell case.
            sheet.setCell(coords.y1, coords.x1, data, style);
        }
        else if (coords.x2 < coords.x1 || coords.y2 < coords.y1)
        {
            // Invalid case.
            System.out.println("[WARNING]:Invalid coordinate detected: Right(" //$NON-NLS-1$
                + coords.x2 + ") < Left(" //$NON-NLS-1$
                + coords.x1 + ") or Bottom(" //$NON-NLS-1$
                + coords.y2 + ") < Top(" //$NON-NLS-1$
                + coords.y1 + ")"); //$NON-NLS-1$
        }
        else
        {
            if (sheet.addMergeBlock(coords.y1, coords.x1, coords.y2, coords.x2)
                .isEmpty())
            {
                // Can't merge
                System.out.println("[WARNING]:Coordinate" //$NON-NLS-1$
                    + coords + " is overlapped"); //$NON-NLS-1$
            }
            else
            {
                setChildStyle(sheet, coords, style);
                sheet.setCell(coords.y1, coords.x1, data);
            }
        }
    }

    private Coordinate getElementCoords(List<Integer> xCuts,
        List<Integer> yCuts, Frame element, int xOffset, int yOffset,
        Sheet sheet, Cell defaultCell)
    {
        Coordinate coords = new Coordinate();
        coords.x1 = shear(element.getLeft() + xOffset);
        coords.y1 = shear(element.getTop() + yOffset);
        coords.x2 = shear(element.getRight() + xOffset);
        coords.y2 = shear(element.getBottom() + yOffset);

        // ignore zero width/height block
        if (coords.x1 == coords.x2 || coords.y1 == coords.y2)
        {
            return null;
        }

        if (fixedColumnWidth > 0)
        {
            coords.x1 = shear(element.getLeft() + xOffset, fixedColumnWidth);
            coords.x2 = shear(element.getRight() + xOffset, fixedColumnWidth);
        }

        coords.normalize();

        coords.x1 = xCuts.indexOf(new Integer(coords.x1));
        coords.y1 = yCuts.indexOf(new Integer(coords.y1));
        coords.x2 = -1 + xCuts.indexOf(new Integer(coords.x2));
        coords.y2 = -1 + yCuts.indexOf(new Integer(coords.y2));

        if (fixedColumnWidth > 0)
        {
            // Move overlaped cell to right side
            Cell cell = sheet.getCell(coords.y1, coords.x1, false);
            while (cell != defaultCell)
            {
                coords.x1++;
                coords.x2++;
                cell = sheet.getCell(coords.y1, coords.x1, false);
            }

            // adjust missed single cell case
            if (coords.x1 == coords.x2 + 1)
            {
                coords.x2 = coords.x1;
            }

        }

        return coords;
    }

    private Coordinate cellCoordinate;

    private void layoutChildren(List<Integer> xCuts, List<Integer> yCuts,
        Sheet sheet, Cell defaultCell, Frame parentElement, Style currentStyle,
        int xOffset, int yOffset)
    {
        Frame element = null;
        Coordinate coord = null;
        Style style = null;

        for (Iterator<Frame> it = parentElement.iterator(); it.hasNext();)
        {
            element = it.next();
            IArea area = getArea(element);

            if (area instanceof TableArea)
            {
                TableArea tableArea = (TableArea) area;
                System.out.println(tableArea.isCanShrink());
                System.out.println(tableArea.isGridDesign());
                System.out.println(tableArea.isIgnoreReordering());
                System.out.println(tableArea.isInlineStacking());
            }

            if (cellCoordinate != null)
            {
                coord = cellCoordinate;
            }
            else
            {
                coord = getElementCoords(xCuts, yCuts, element, xOffset,
                    yOffset, sheet, defaultCell);
            }

            if (area instanceof CellArea)
            {
                cellCoordinate = coord;
            }

            // skip zero size element.
            if (coord == null)
            {
                continue;
            }

            style = LocalStyle.create(new Style[] {element.getStyle(),
                currentStyle});

            if (hasChildren(element))
            {
                layoutChildren(xCuts, yCuts, sheet, defaultCell, element,
                    style, xOffset, yOffset);

                setParentsStyle(sheet, defaultCell, coord, style);
            }
            else
            {
                Cell cell = sheet.getCell(coord.y1, coord.x1, false);
                Object data = element.getData();

                if (coord.x1 == coord.x2 && coord.y1 == coord.y2)
                {
                    // A single cell case.
                    if (cell != defaultCell)
                    {
                        System.out
                            .println("[WARNING]:An existing single cell detected"); //$NON-NLS-1$
                    }
                    sheet.setCell(coord.y1, coord.x1, data, style);
                }
                else if (coord.x2 < coord.x1 || coord.y2 < coord.y1)
                {
                    // Invalid case.
                    System.out
                        .println("[WARNING]:Invalid coordinate detected: Right(" //$NON-NLS-1$
                            + coord.x2 + ") < Left(" //$NON-NLS-1$
                            + coord.x1 + ") or Bottom(" //$NON-NLS-1$
                            + coord.y2 + ") < Top(" //$NON-NLS-1$
                            + coord.y1 + ")"); //$NON-NLS-1$
                }
                else
                {
                    // Try merge first.
                    if (sheet.addMergeBlock(coord.y1, coord.x1, coord.y2,
                        coord.x2).isEmpty())
                    {
                        // Can't merge
                        System.out.println("[WARNING]:Coordinate" //$NON-NLS-1$
                            + coord + " is overlapped"); //$NON-NLS-1$

                        if (adjustOverlap(sheet, coord))
                        {
                            // merge the new block
                            mergeOverlap(sheet, element, coord, data, style);
                        }
                    }
                    else
                    {
                        setChildStyle(sheet, coord, style);
                        sheet.setCell(coord.y1, coord.x1, data);
                    }
                }
            }
            cellCoordinate = null;
        }
    }

    private int shear(int pos)
    {
        return shear(pos, prec);
    }

    private int shear(int pos, int precison)
    {
        int x;
        if (precison > 1)
        {
            x = pos % precison;
            if (x > halfprec)
            {
                x -= precison;
            }
            pos -= x;
        }
        return pos;

    }

    static class Coordinate
    {

        int x1, y1, x2, y2;

        void normalize()
        {
            int temp;
            if (x1 > x2)
            {
                temp = x1;
                x1 = x2;
                x2 = temp;
            }
            if (y1 > y2)
            {
                temp = y1;
                y1 = y2;
                y2 = temp;
            }
        }

        public String toString()
        {
            return "[" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }
    }

}