/********************************************************************************
 * (C) Copyright 2000-2010, by Shawn Qualia. This library is free software; you
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


import java.util.Locale;

import org.eclipse.birt.report.engine.api.script.IReportContext;


/**
 * A special context object simulates the IReportContext interface but
 * eliminates all potential unsecured operations.
 */
public class ManagedReportContext
{

    private IReportContext cxt;

    ManagedReportContext(IReportContext cxt)
    {
        this.cxt = cxt;
    }

    public Locale getLocale()
    {
        return cxt == null ? null : cxt.getLocale();
    }

    public Object getGlobalVariable(String name)
    {
        return cxt == null ? null : cxt.getGlobalVariable(name);
    }

    public Object getPageVariable(String name)
    {
        return cxt == null ? null : cxt.getPageVariable(name);
    }

    public Object getPersistentGlobalVariable(String name)
    {
        return cxt == null ? null : cxt.getPersistentGlobalVariable(name);
    }

    public Object getParameterDisplayText(String name)
    {
        return cxt == null ? null : cxt.getParameterDisplayText(name);
    }

    public Object getParameterValue(String name)
    {
        return cxt == null ? null : cxt.getParameterValue(name);
    }

}