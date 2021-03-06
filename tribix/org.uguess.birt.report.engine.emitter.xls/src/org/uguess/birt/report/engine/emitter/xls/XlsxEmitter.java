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

package org.uguess.birt.report.engine.emitter.xls;


/**
 * XlsEmitter
 * 
 * @preserve
 */
public class XlsxEmitter extends ExcelEmitter
{
    public XlsxEmitter()
    {
        super();
    }
    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter#getOutputFormat
     * ()
     */
    public String getOutputFormat()
    {
        return XlsRenderer.XLSX_IDENTIFIER;
    }

    @Override
    protected XlsRenderer createRenderer(boolean legacyMode)
    {
        if (legacyMode)
        {
            return new XlsRenderer(XlsRenderer.XLSX_IDENTIFIER);
        }
        else
        {
            return new XlsRenderer2(XlsRenderer.XLSX_IDENTIFIER);
        }
    }
}