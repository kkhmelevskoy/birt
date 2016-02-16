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


import java.util.Map;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.content.IAutoTextContent;
import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.content.IPageContent;
import org.eclipse.birt.report.engine.content.IReportContent;
import org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter;
import org.eclipse.birt.report.engine.emitter.IEmitterServices;
import org.eclipse.birt.report.engine.nLayout.area.IArea;
import org.eclipse.birt.report.engine.nLayout.area.ITextArea;
import org.uguess.birt.report.engine.util.ContentLookupBuilder;
import org.uguess.birt.report.engine.util.OptionParser;


/**
 * XlsEmitter
 * 
 * @preserve
 */
public abstract class ExcelEmitter extends ContentEmitterAdapter
{

    private XlsRenderer renderer;
    private ContentLookupBuilder builder;
    private boolean singlePageMode = false;
    
    public ExcelEmitter()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter#initialize
     * (org.eclipse.birt.report.engine.emitter.IEmitterServices)
     */
    public void initialize(IEmitterServices service)
    {
        Object legacyOption = null;
        Object singleOption = null;

        Object emitterOption = service.getEmitterConfig().get(
            XlsRenderer.XLS_IDENTIFIER);
        if (emitterOption instanceof Map)
        {
            legacyOption = ((Map) emitterOption)
                .get(XlsEmitterConfig.KEY_LEGACY_MODE);
            singleOption = ((Map) emitterOption)
                .get(XlsEmitterConfig.KEY_EXPORT_SINGLE_SHEET);
        }

        IRenderOption renderOption = service.getRenderOption();
        if (renderOption != null)
        {
            Object value = renderOption
                .getOption(XlsEmitterConfig.KEY_LEGACY_MODE);

            if (value != null)
            {
                legacyOption = value;
            }

            value = renderOption
                .getOption(XlsEmitterConfig.KEY_EXPORT_SINGLE_SHEET);

            if (value != null)
            {
                singleOption = value;
            }
        }

        singlePageMode = singleOption != null
            && OptionParser.parseBoolean(singleOption);

        boolean legacyMode = legacyOption != null
            && OptionParser.parseBoolean(legacyOption);
        
        renderer = createRenderer(legacyMode);

        renderer.initialize(service);

        builder = new ContentLookupBuilder();
    }
    
    protected abstract XlsRenderer createRenderer(boolean legacyMode);

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter#start(org
     * .eclipse.birt.report.engine.content.IReportContent)
     */
    public void start(IReportContent report)
    {
        renderer.start(report);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter#end(org.
     * eclipse.birt.report.engine.content.IReportContent)
     */
    public void end(IReportContent report)
    {
        renderer.end(report);

        builder.reset();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter#startPage
     * (org.eclipse.birt.report.engine.content.IPageContent)
     */
    public void startPage(IPageContent page)
    {
        IArea pageArea = (IArea) page.getExtension(IContent.LAYOUT_EXTENSION);
        if (pageArea != null)
        {
            // reset the cache for multi-page mode
            if (!singlePageMode)
            {
                builder.reset();
            }

            pageArea.accept(builder);

            renderer.contentCache = builder.getMapping();

            pageArea.accept(renderer);
        }

    }
    
    @Override
    public void endPage(IPageContent page) throws BirtException
    {
        super.endPage(page);

        if (!singlePageMode)
        {
            builder.reset();
        }

        renderer.contentCache = null;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter#startAutoText
     * (org.eclipse.birt.report.engine.content.IAutoTextContent)
     */
    public void startAutoText(IAutoTextContent autoText)
    {
        ITextArea totalPage = (ITextArea) autoText
            .getExtension(IContent.LAYOUT_EXTENSION);
        renderer.setTotalPage(totalPage);
    }
}