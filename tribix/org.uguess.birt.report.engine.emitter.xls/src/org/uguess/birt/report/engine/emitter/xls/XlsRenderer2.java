/********************************************************************************
 * (C) Copyright 2000-2008, by Shawn Qualia. This library is free software; you
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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.birt.report.engine.content.IAutoTextContent;
import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.content.IHyperlinkAction;
import org.eclipse.birt.report.engine.content.IImageContent;
import org.eclipse.birt.report.engine.content.IReportContent;
import org.eclipse.birt.report.engine.content.ITextContent;
import org.eclipse.birt.report.engine.nLayout.area.IArea;
import org.eclipse.birt.report.engine.nLayout.area.IAreaVisitor;
import org.eclipse.birt.report.engine.nLayout.area.IContainerArea;
import org.eclipse.birt.report.engine.nLayout.area.IImageArea;
import org.eclipse.birt.report.engine.nLayout.area.ITemplateArea;
import org.eclipse.birt.report.engine.nLayout.area.ITextArea;
import org.eclipse.birt.report.engine.nLayout.area.impl.PageArea;
import org.eclipse.birt.report.engine.nLayout.area.style.BoxStyle;
import org.eclipse.birt.report.engine.nLayout.area.style.TextStyle;
import org.uguess.birt.report.engine.layout.wrapper.Frame;
import org.uguess.birt.report.engine.layout.wrapper.impl.HasArea;
import org.uguess.birt.report.engine.layout.wrapper.impl.MultiAreaFrame;
import org.uguess.birt.report.engine.util.EngineUtil;


/**
 * XlsRenderer2
 */
public class XlsRenderer2 extends XlsRenderer
{

    private Map<IContent, MultiAreaFrame> frameCache;
    private Stack<IArea> areaStack;
    private Stack<IArea> clipStack;
    private IArea bodyArea;

    public XlsRenderer2(String format)
    {
        super(format);
    }

    @Override
    public void start(IReportContent rc)
    {
        super.start(rc);

        frameCache = new HashMap<IContent, MultiAreaFrame>();
        areaStack = new Stack<IArea>();
        clipStack = new Stack<IArea>();
        bodyArea = null;
    }

    @Override
    public void end(IReportContent rc)
    {
        super.end(rc);

        frameCache.clear();
        frameCache = null;

        areaStack.clear();
        areaStack = null;

        clipStack.clear();
        clipStack = null;

        bodyArea = null;
    }

    @Override
    public void startContainer(IContainerArea container)
    {
        if (container.needClip())
        {
            clipStack.push(wrap2global(container));
        }

        super.startContainer(container);

        areaStack.push(container);
    }

    @Override
    public void endContainer(IContainerArea containerArea)
    {
        if (containerArea.needClip())
        {
            clipStack.pop();
        }

        areaStack.pop();

        Frame currentFrame = frameStack.peek();

        IArea area = unWrap(currentFrame.getData());

        if (area != containerArea)
        {
            return;
        }

        frameStack.pop();

        if (area instanceof PageArea)
        {
            resolveUnwantedContent(currentFrame);

            exportFrame(currentFrame);

            // clean up pagewise frame cache
            frameCache.clear();
        }
    }

    @Override
    protected boolean isTotalPageArea(Object element)
    {
        if (element != null && totalPageAreas != null)
        {
            IArea hostArea = unWrap(element);

            return totalPageAreas.contains(hostArea);
        }
        return false;
    }

    @Override
    public void visitAutoText(ITemplateArea templateArea)
    {
        buildFrame(templateArea, false);

        int type = EngineUtil.getTemplateType(templateArea);

        if (type == IAutoTextContent.TOTAL_PAGE
            || type == IAutoTextContent.UNFILTERED_TOTAL_PAGE)
        {
            Frame current = frameStack.peek();

            if (current != null)
            {
                if (totalPageAreas == null)
                {
                    totalPageAreas = new HashSet<IArea>();
                }

                totalPageAreas.add(unWrap(current.getData()));
            }
        }
    }

    private void resolveUnwantedContent(Frame frame)
    {
        int cnt = ((MultiAreaFrame) frame).getChildCount();

        if (cnt == 0)
        {
            return;
        }

        Object cellValue = unWrapWithType(frame.getData());

        if (cellValue instanceof IImageArea)
        {
            // TODO could be action content, which is not supported now
            ((MultiAreaFrame) frame).removeAllChildren();
            return;
        }
        else if (cellValue instanceof ITextArea)
        {
            // TODO could be action content, which is not supported now
            ((MultiAreaFrame) frame).removeAllChildren();
            return;
        }

        for (Iterator<Frame> itr = frame.iterator(); itr.hasNext();)
        {
            resolveUnwantedContent(itr.next());
        }
    }

    @SuppressWarnings("unchecked")
    private IArea unWrap(Object data)
    {
        if (data instanceof AreaWrapper)
        {
            return ((AreaWrapper) data).area;
        }

        if (data instanceof IArea)
        {
            return (IArea) data;
        }

        if (data instanceof List)
        {
            return unWrap(((List) data).get(0));
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private IArea unWrapWithType(Object data)
    {
        if (data instanceof AreaWrapper)
        {
            return ((AreaWrapper) data).area;
        }

        if (data instanceof IArea)
        {
            return (IArea) data;
        }

        if (data instanceof List)
        {
            return wrapWithType((List) data);
        }

        return null;
    }

    private IArea wrapWithType(List<IArea> areas)
    {
        IArea hostArea = areas.get(0);

        hostArea = unWrapWithType(hostArea);

        IContent hostContent = contentCache.get(hostArea);

        if (hostContent instanceof ITextContent)
        {
            for (int i = 0; i < areas.size(); i++)
            {
                IArea ar = unWrap(areas.get(i));

                if (ar instanceof ITextArea)
                {
                    TextAreaWrapper textWrapper = new TextAreaWrapper(
                        (ITextArea) ar);
                    textWrapper.text = ((ITextContent) hostContent).getText();

                    return textWrapper;
                }
            }
        }
        else if (hostContent instanceof IImageContent)
        {
            for (int i = 0; i < areas.size(); i++)
            {
                IArea ar = unWrap(areas.get(i));

                if (ar instanceof IImageArea)
                {
                    // return first image
                    return ar;
                }
            }
        }

        return hostArea;
    }

    @Override
    protected Object getTranslatedElementValue(Object value)
    {
        return unWrapWithType(value);
    }

    @Override
    protected void buildFrame(IArea area, boolean isContainer)
    {
        if (area instanceof PageArea)
        {
            // record body area to support export_body_only option
            bodyArea = ((PageArea) area).getBody();
        }

        if (area == bodyArea)
        {
            // create body frame
            buildNewFrame(wrap2global(area), isContainer);
            bodyArea = null;
        }
        else
        {
            IContent cnt = contentCache.get(area);

            if (cnt != null)
            {
                MultiAreaFrame frame = frameCache.get(cnt);

                if (frame != null)
                {
                    frame.addSubArea(wrap2global(area));
                }
                else
                {
                    frame = buildNewFrame(wrap2global(area), isContainer);

                    frameCache.put(cnt, frame);
                }
            }
        }
    }

    @Override
    protected Frame locateChildFrame(Frame parentFrame, Object data)
    {
        for (Iterator<Frame> itr = parentFrame.iterator(); itr.hasNext();)
        {
            Frame fr = itr.next();

            if (unWrap(fr.getData()) == data)
            {
                return fr;
            }

            Frame cfr = locateChildFrame(fr, data);
            if (cfr != null)
            {
                return cfr;
            }
        }

        return null;
    }

    private IArea wrap2global(IArea child)
    {
        if (areaStack.isEmpty())
        {
            return child;
        }

        AreaWrapper warea;

        if (child instanceof IContainerArea)
        {
            warea = new ContainerAreaWrapper((IContainerArea) child);
        }
        else if (child instanceof ITextArea)
        {
            warea = new TextAreaWrapper((ITextArea) child);
        }
        else
        {
            warea = new AreaWrapper(child);
        }

        for (int i = areaStack.size() - 1; i >= 0; i--)
        {
            IArea stackArea = areaStack.get(i);

            warea.x += stackArea.getX();
            warea.y += stackArea.getY();
        }

        return warea;
    }

    private MultiAreaFrame buildNewFrame(IArea area, boolean isContainer)
    {
        MultiAreaFrame frame = new MultiAreaFrame(area);

        if (!clipStack.isEmpty())
        {
            frame.setClip(clipStack.peek());
        }

        MultiAreaFrame parentFrame = frameStack.isEmpty() ? null
            : (MultiAreaFrame) frameStack.peek();
        frame.setParent(parentFrame);

        if (parentFrame != null)
        {
            parentFrame.addChild(frame);
        }

        if (isContainer)
        {
            frameStack.push(frame);
        }

        // System.out.println(frame);

        return frame;
    }

    /**
     * AreaWrapper
     */
    public static class AreaWrapper implements IArea, HasArea
    {

        protected IArea area;
        private int x, y;

        public AreaWrapper(IArea area)
        {
            this.area = area;

            x = area.getX();
            y = area.getY();
        }

        @Override
        public IArea getArea()
        {
            return area;
        }

        public void accept(IAreaVisitor visitor)
        {
            area.accept(visitor);
        }

        public int getHeight()
        {
            return area.getHeight();
        }

        public float getScale()
        {
            return area.getScale();
        }

        public int getWidth()
        {
            return area.getWidth();
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }

        public IHyperlinkAction getAction()
        {
            return area.getAction();
        }

        public String getBookmark()
        {
            return area.getBookmark();
        }
    }

    /**
     * ContainerAreaWrapper
     */
    static class ContainerAreaWrapper extends AreaWrapper implements
            IContainerArea
    {

        ContainerAreaWrapper(IContainerArea area)
        {
            super(area);
        }

        public void addChild(IArea child)
        {
            ((IContainerArea) area).addChild(child);
        }

        public BoxStyle getBoxStyle()
        {
            return ((IContainerArea) area).getBoxStyle();
        }

        public String getHelpText()
        {
            return ((IContainerArea) area).getHelpText();
        }

        public Iterator<IArea> getChildren()
        {
            return ((IContainerArea) area).getChildren();
        }

        public int getChildrenCount()
        {
            return ((IContainerArea) area).getChildrenCount();
        }

        public boolean needClip()
        {
            return ((IContainerArea) area).needClip();
        }

        public void setNeedClip(boolean needClip)
        {
            ((IContainerArea) area).setNeedClip(needClip);
        }

    }

    /**
     * TextAreaWrapper
     */
    static class TextAreaWrapper extends AreaWrapper implements ITextArea
    {

        private String text;
        private String logicText;

        TextAreaWrapper(ITextArea area)
        {
            super(area);

            this.text = area.getText();
            this.logicText = area.getLogicalOrderText();
        }

        public String getText()
        {
            if (" ".equals(text))
            {
                text = "";
            }

            return text;
        }

        public String getLogicalOrderText()
        {
            if (" ".equals(logicText))
            {
                logicText = "";
            }

            return logicText;
        }

        public TextStyle getTextStyle()
        {
            return ((ITextArea) area).getTextStyle();
        }

        public boolean needClip()
        {
            return ((ITextArea) area).needClip();
        }
    }
}
