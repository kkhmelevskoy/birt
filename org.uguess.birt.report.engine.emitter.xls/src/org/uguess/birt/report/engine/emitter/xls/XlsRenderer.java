/********************************************************************************
 * (C) Copyright 2000-2005, by Shawn Qualia.
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 ********************************************************************************/

package org.uguess.birt.report.engine.emitter.xls;


import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;

import org.eclipse.birt.chart.model.type.ScatterSeries;

import org.eclipse.birt.chart.model.type.AreaSeries;

import org.eclipse.birt.chart.model.attribute.Text;

import org.eclipse.birt.chart.model.type.BarSeries;

import org.eclipse.birt.chart.model.attribute.Anchor;

import org.eclipse.birt.chart.model.layout.Legend;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.parser.ASTAddNode;
import org.apache.commons.jexl.parser.ASTAndNode;
import org.apache.commons.jexl.parser.ASTArrayAccess;
import org.apache.commons.jexl.parser.ASTAssignment;
import org.apache.commons.jexl.parser.ASTBitwiseAndNode;
import org.apache.commons.jexl.parser.ASTBitwiseComplNode;
import org.apache.commons.jexl.parser.ASTBitwiseOrNode;
import org.apache.commons.jexl.parser.ASTBitwiseXorNode;
import org.apache.commons.jexl.parser.ASTBlock;
import org.apache.commons.jexl.parser.ASTDivNode;
import org.apache.commons.jexl.parser.ASTEQNode;
import org.apache.commons.jexl.parser.ASTEmptyFunction;
import org.apache.commons.jexl.parser.ASTExpression;
import org.apache.commons.jexl.parser.ASTExpressionExpression;
import org.apache.commons.jexl.parser.ASTFalseNode;
import org.apache.commons.jexl.parser.ASTFloatLiteral;
import org.apache.commons.jexl.parser.ASTForeachStatement;
import org.apache.commons.jexl.parser.ASTGENode;
import org.apache.commons.jexl.parser.ASTGTNode;
import org.apache.commons.jexl.parser.ASTIdentifier;
import org.apache.commons.jexl.parser.ASTIfStatement;
import org.apache.commons.jexl.parser.ASTIntegerLiteral;
import org.apache.commons.jexl.parser.ASTJexlScript;
import org.apache.commons.jexl.parser.ASTLENode;
import org.apache.commons.jexl.parser.ASTLTNode;
import org.apache.commons.jexl.parser.ASTMethod;
import org.apache.commons.jexl.parser.ASTModNode;
import org.apache.commons.jexl.parser.ASTMulNode;
import org.apache.commons.jexl.parser.ASTNENode;
import org.apache.commons.jexl.parser.ASTNotNode;
import org.apache.commons.jexl.parser.ASTNullLiteral;
import org.apache.commons.jexl.parser.ASTOrNode;
import org.apache.commons.jexl.parser.ASTReference;
import org.apache.commons.jexl.parser.ASTReferenceExpression;
import org.apache.commons.jexl.parser.ASTSizeFunction;
import org.apache.commons.jexl.parser.ASTSizeMethod;
import org.apache.commons.jexl.parser.ASTStatementExpression;
import org.apache.commons.jexl.parser.ASTStringLiteral;
import org.apache.commons.jexl.parser.ASTSubtractNode;
import org.apache.commons.jexl.parser.ASTTrueNode;
import org.apache.commons.jexl.parser.ASTUnaryMinusNode;
import org.apache.commons.jexl.parser.ASTWhileStatement;
import org.apache.commons.jexl.parser.Parser;
import org.apache.commons.jexl.parser.ParserVisitor;
import org.apache.commons.jexl.parser.SimpleNode;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.ColorDefinition;
import org.eclipse.birt.chart.model.attribute.Fill;
import org.eclipse.birt.chart.model.attribute.Marker;
import org.eclipse.birt.chart.model.attribute.MarkerType;
import org.eclipse.birt.chart.model.attribute.Palette;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.util.FillUtil;
import org.eclipse.birt.report.engine.api.IHTMLActionHandler;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.api.impl.Action;
import org.eclipse.birt.report.engine.content.IAutoTextContent;
import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.content.IHyperlinkAction;
import org.eclipse.birt.report.engine.content.IPageContent;
import org.eclipse.birt.report.engine.content.IReportContent;
import org.eclipse.birt.report.engine.content.impl.PageContent;
import org.eclipse.birt.report.engine.emitter.IEmitterServices;
import org.eclipse.birt.report.engine.ir.ReportElementDesign;
import org.eclipse.birt.report.engine.nLayout.area.IArea;
import org.eclipse.birt.report.engine.nLayout.area.IAreaVisitor;
import org.eclipse.birt.report.engine.nLayout.area.IContainerArea;
import org.eclipse.birt.report.engine.nLayout.area.IImageArea;
import org.eclipse.birt.report.engine.nLayout.area.ITemplateArea;
import org.eclipse.birt.report.engine.nLayout.area.ITextArea;
import org.eclipse.birt.report.engine.nLayout.area.impl.PageArea;
import org.eclipse.birt.report.model.api.ReportElementHandle;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.emf.common.util.EList;
import org.uguess.birt.report.engine.layout.wrapper.Frame;
import org.uguess.birt.report.engine.layout.wrapper.impl.AggregateFrame;
import org.uguess.birt.report.engine.layout.wrapper.impl.AreaFrame;
import org.uguess.birt.report.engine.spreadsheet.model.Cell;
import org.uguess.birt.report.engine.spreadsheet.model.MergeBlock;
import org.uguess.birt.report.engine.spreadsheet.model.Sheet;
import org.uguess.birt.report.engine.spreadsheet.wrapper.Transformer;
import org.uguess.birt.report.engine.util.EngineUtil;
import org.uguess.birt.report.engine.util.ImageUtil;
import org.uguess.birt.report.engine.util.OptionParser;
import org.uguess.birt.report.engine.util.PageRangeChecker;

import com.smartxls.ChartFormat;
import com.smartxls.ChartShape;
import com.smartxls.HyperLink;
import com.smartxls.PictureShape;
import com.smartxls.RangeStyle;
import com.smartxls.ShapeFormat;
import com.smartxls.WorkBook;


/**
 * XlsRenderer
 */
public class XlsRenderer implements IAreaVisitor
{

    public static final String DEFAULT_FILE_NAME = "report.xls"; //$NON-NLS-1$
    protected static final String XLS_IDENTIFIER = "xls"; //$NON-NLS-1$
    protected static final int COMMENTS_WIDTH_IN_COLUMN = 3;
    protected static final int COMMENTS_HEIGHT_IN_ROW = 5;
    private static final String ATTR_LANDSCAPE = "landscape"; //$NON-NLS-1$
    private static final boolean DEBUG;

    static
    {
        DEBUG = System.getProperty("debug") != null; //$NON-NLS-1$
    }

    protected boolean removeEmptyRow = false;
    protected boolean showGridLines = false;
    protected boolean exportBodyOnly = false;
    protected boolean suppressUnknownImage = false;
    protected boolean exportSingleSheet = false;
    protected boolean enableComments = true;
    protected Expression sheetNameExpr;
    protected int fixedColumnWidth = -1;
    private int dpi;
    private double baseCharWidth;
    private AggregateFrame singleContainer;
    private PageRangeChecker rangeChecker;
    private int currentPageIndex;
    private int commentsAnchorColumnIndex, commentsAnchorRowIndex;
    private boolean closeStream;
    private OutputStream output = null;
    private WorkBook workbook;
    private int sheetNum = -1;
    private XlsStyleProcessor processor;
    protected Stack<Frame> frameStack;
    protected Set<XlsCell> totalCells;
    protected Set<IArea> totalPageAreas;
    protected IEmitterServices services;
    private long timeCounter;
    Map<IArea, IContent> contentCache;

    private int chartCount = 0;
    private List<XlsCell> chartCells = new ArrayList<XlsCell>();
    private List<GeneratedChartState> chartStates = new ArrayList<GeneratedChartState>();

    public void initialize(IEmitterServices services)
    {
        // init options.
        rangeChecker = new PageRangeChecker(null);
        removeEmptyRow = false;
        showGridLines = false;
        fixedColumnWidth = -1;
        exportBodyOnly = false;
        exportSingleSheet = false;
        dpi = 96;
        suppressUnknownImage = false;
        enableComments = true;
        sheetNameExpr = null;
        closeStream = false;

        this.services = services;

        // parse emitter options.
        Object option = services.getEmitterConfig().get(XLS_IDENTIFIER);
        if (option instanceof Map)
        {
            parseRendererOptions((Map) option);
        }
        else if (option instanceof IRenderOption)
        {
            parseRendererOptions(((IRenderOption) option).getOptions());
        }

        // parse rendering options, this will overwrite the option by emitter
        // per rendering task.
        parseRendererOptions(services.getRenderOption().getOptions());

        Object fd = services.getOption(IRenderOption.OUTPUT_FILE_NAME);
        File file = null;
        try
        {
            if (fd != null)
            {
                file = new File(fd.toString());

                File parent = file.getParentFile();
                if (parent != null && !parent.exists())
                {
                    parent.mkdirs();
                }

                output = new FileOutputStream(file);
            }
        }
        catch (FileNotFoundException fnfe)
        {
            fnfe.printStackTrace();
        }

        if (output == null)
        {
            Object value = services.getOption(IRenderOption.OUTPUT_STREAM);
            if (value != null && value instanceof OutputStream)
            {
                output = (OutputStream) value;
            }

            else
            {
                try
                {
                    file = new File(DEFAULT_FILE_NAME);
                    output = new FileOutputStream(file);
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void parseRendererOptions(Map xlsOption)
    {
        if (xlsOption == null)
        {
            return;
        }

        Object value;

        value = xlsOption.get("dpi"); //$NON-NLS-1$
        if (value != null)
        {
            int pdpi = OptionParser.parseInt(value);

            if (pdpi != -1)
            {
                dpi = pdpi;
            }
        }

        value = xlsOption.get(XlsEmitterConfig.KEY_PAGE_RANGE);
        if (value != null)
        {
            rangeChecker = new PageRangeChecker(OptionParser.parseString(value));
        }

        value = xlsOption.get(XlsEmitterConfig.KEY_REMOVE_EMPTY_ROW);
        if (value != null)
        {
            removeEmptyRow = OptionParser.parseBoolean(value);
        }

        value = xlsOption.get(XlsEmitterConfig.KEY_SHOW_GRID_LINES);
        if (value != null)
        {
            showGridLines = OptionParser.parseBoolean(value);
        }

        value = xlsOption.get(XlsEmitterConfig.KEY_FIXED_COLUMN_WIDTH);
        if (value != null)
        {
            fixedColumnWidth = OptionParser.parseInt(value);
        }

        value = xlsOption.get(XlsEmitterConfig.KEY_EXPORT_BODY_ONLY);
        if (value != null)
        {
            exportBodyOnly = OptionParser.parseBoolean(value);
        }

        value = xlsOption.get(XlsEmitterConfig.KEY_EXPORT_SINGLE_SHEET);
        if (value != null)
        {
            exportSingleSheet = OptionParser.parseBoolean(value);
        }

        value = xlsOption.get(XlsEmitterConfig.KEY_SUPPRESS_UNKNOWN_IMAGE);
        if (value != null)
        {
            suppressUnknownImage = OptionParser.parseBoolean(value);
        }

        value = xlsOption.get(XlsEmitterConfig.KEY_ENABLE_COMMENTS);
        if (value != null)
        {
            enableComments = OptionParser.parseBoolean(value);
        }

        value = xlsOption.get(XlsEmitterConfig.KEY_SHEET_NAME_EXPR);
        if (value != null)
        {
            sheetNameExpr = parseSheetNameExpression(OptionParser
                .parseString(value));
        }

        value = xlsOption.get(IRenderOption.CLOSE_OUTPUTSTREAM_ON_EXIT);
        if (value != null)
        {
            closeStream = OptionParser.parseBoolean(value);
        }
    }

    private Expression parseSheetNameExpression(String expr)
    {
        Expression expression = null;

        if (expr != null)
        {
            try
            {
                expr = expr.trim();
                if (!expr.endsWith(";")) //$NON-NLS-1$
                {
                    expr += ";"; //$NON-NLS-1$
                }

                Parser parser = new Parser(new StringReader(";")); //$NON-NLS-1$

                SimpleNode tree = parser.parse(new StringReader(expr));

                SimpleNode node = (SimpleNode) tree.jjtGetChild(0);

                // pre-scan the expression tree to remove loop structure
                node.jjtAccept(new ParserVisitor()
                {

                    public Object visit(ASTReference node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTSizeMethod node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTArrayAccess node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTMethod node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTForeachStatement node, Object data)
                    {
                        throw new UnsupportedOperationException(
                            "Loop function is not allowed."); //$NON-NLS-1$
                    }

                    public Object visit(ASTWhileStatement node, Object data)
                    {
                        throw new UnsupportedOperationException(
                            "Loop function is not allowed."); //$NON-NLS-1$
                    }

                    public Object visit(ASTIfStatement node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTReferenceExpression node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTStatementExpression node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTExpressionExpression node,
                        Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTStringLiteral node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTFloatLiteral node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTIntegerLiteral node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTFalseNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTTrueNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTNullLiteral node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTNotNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTBitwiseComplNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTUnaryMinusNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTModNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTDivNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTMulNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTSubtractNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTAddNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTGENode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTLENode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTGTNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTLTNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTNENode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTEQNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTBitwiseAndNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTBitwiseXorNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTBitwiseOrNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTAndNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTOrNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTAssignment node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTExpression node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTIdentifier node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTSizeFunction node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTEmptyFunction node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTBlock node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(ASTJexlScript node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }

                    public Object visit(SimpleNode node, Object data)
                    {
                        return node.childrenAccept(this, data);
                    }
                }, null);

                expression = ExpressionFactory.createExpression(expr);
            }
            catch (Exception e)
            {
                expression = null;

                e.printStackTrace();
            }
        }

        return expression;
    }

    public void start(IReportContent rc)
    {
        if (DEBUG)
        {
            timeCounter = System.currentTimeMillis();
        }

        this.frameStack = new Stack<Frame>();

        workbook = new WorkBook();

        currentPageIndex = 1;

        baseCharWidth = 7 * 72d / dpi;

        // Font bookFont = null;
        //
        // if ( workbook.getNumberOfFonts( ) > 0 )
        // {
        // HSSFFont font = workbook.getFontAt( (short) 0 );
        //
        // bookFont = new Font( font.getFontName( ),
        // Font.PLAIN,
        // font.getFontHeightInPoints( ) );
        // }
        // else
        // {
        // bookFont = new Font( "Serif", Font.PLAIN, 10 ); //$NON-NLS-1$
        // }
        //
        // Graphics2D g2d = null;
        //
        // try
        // {
        // BufferedImage bi = new BufferedImage( 1,
        // 1,
        // BufferedImage.TYPE_INT_ARGB );
        // g2d = (Graphics2D) bi.getGraphics( );
        // FontMetrics fm = g2d.getFontMetrics( bookFont );
        // double charWidth = fm.charWidth( '0' );
        //
        // baseCharWidth = charWidth;// * dpi / 72d;
        //
        // bi.flush( );
        // }
        // catch ( Exception e )
        // {
        // baseCharWidth = 6;// * dpi / 72d;
        // }
        // finally
        // {
        // if ( g2d != null )
        // {
        // g2d.dispose( );
        // }
        // }

        processor = new XlsStyleProcessor(workbook);
    }

    public void end(IReportContent rc)
    {
        if (exportSingleSheet && singleContainer != null)
        {
            long span = System.currentTimeMillis();

            // process single sheet here.
            Sheet modelSheet = Transformer.toSheet(singleContainer, 1000,
                fixedColumnWidth * 1000);
            exportSheet(modelSheet,
                Boolean.TRUE == singleContainer.getAttribute(ATTR_LANDSCAPE));

            if (DEBUG)
            {
                System.out
                    .println("------------export total single sheet using " //$NON-NLS-1$
                        + (System.currentTimeMillis() - span) + " ms"); //$NON-NLS-1$
            }
        }

        if (chartCount > 0)
        {
            for (int i = 0; i < chartCount; i++)
            {
                try
                {
                    exportChart(i, chartStates.get(i), chartCells.get(i));
                }
                catch (Exception e)
                {
                	System.err.println(i);
                    e.printStackTrace();
                }
            }
        }

        try
        {
            workbook.setSheet(0); // activate first sheet in report

            workbook.write(output);

            if (closeStream)
            {
                output.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        frameStack.clear();

        processor.dispose();
        processor = null;

        workbook = null;
        frameStack = null;
        singleContainer = null;

        if (DEBUG)
        {
            System.out.println("------------total exporting time using: " //$NON-NLS-1$
                + (System.currentTimeMillis() - timeCounter) + " ms"); //$NON-NLS-1$
        }
    }

    public void startContainer(IContainerArea container)
    {
        buildFrame(container, true);
    }

    public void endContainer(IContainerArea containerArea)
    {
        Frame currentFrame = frameStack.pop();

        if (currentFrame.getData() instanceof PageArea)
        {
            exportFrame(currentFrame);
        }
    }

    protected void exportFrame(Frame currentFrame)
    {
        long span = System.currentTimeMillis();

        if (rangeChecker.checkRange(currentPageIndex))
        {
            PageArea pa = (PageArea) getTranslatedElementValue(currentFrame
                .getData());
            IPageContent content = (PageContent) pa.getContent();
            boolean landscape = DesignChoiceConstants.PAGE_ORIENTATION_LANDSCAPE
                .equals(content.getOrientation());

            if (exportBodyOnly)
            {
                IContainerArea body = pa.getBody();

                // locate body frame first
                Frame bodyFrame = locateChildFrame(currentFrame, body);

                if (bodyFrame instanceof AreaFrame)
                {
                    // adjust offsets
                    ((AreaFrame) bodyFrame).setXOffset(-bodyFrame.getLeft());
                    ((AreaFrame) bodyFrame).setYOffset(-bodyFrame.getTop());
                }

                if (exportSingleSheet)
                {
                    // aggregate only
                    if (singleContainer == null)
                    {
                        singleContainer = new AggregateFrame();
                        singleContainer.setRight(bodyFrame.getRight()
                            - bodyFrame.getLeft());
                        singleContainer.setBottom(bodyFrame.getBottom()
                            - bodyFrame.getTop());

                        if (landscape)
                        {
                            singleContainer.putAttribute(ATTR_LANDSCAPE,
                                Boolean.TRUE);
                        }
                    }
                    else
                    {
                        int yOff = singleContainer.getBottom();
                        singleContainer.setBottom(yOff + bodyFrame.getBottom()
                            - bodyFrame.getTop());

                        if (bodyFrame instanceof AreaFrame)
                        {
                            ((AreaFrame) bodyFrame).setYOffset(yOff
                                + ((AreaFrame) bodyFrame).getYOffset());
                        }
                    }

                    singleContainer.addChild(bodyFrame);
                    if (bodyFrame instanceof AreaFrame)
                    {
                        ((AreaFrame) bodyFrame).setParent(singleContainer);
                    }

                    if (DEBUG)
                    {
                        System.out.println("------------aggregate sheet[" //$NON-NLS-1$
                            + (currentPageIndex) + "] using " //$NON-NLS-1$
                            + (System.currentTimeMillis() - span) + " ms"); //$NON-NLS-1$
                    }
                }
                else
                {
                    // export body frame
                    Sheet modelSheet = Transformer.toSheet(bodyFrame, 1000,
                        fixedColumnWidth * 1000);
                    exportSheet(modelSheet, landscape);

                    if (DEBUG)
                    {
                        System.out.println("------------export sheet[" //$NON-NLS-1$
                            + (currentPageIndex) + "] using " //$NON-NLS-1$
                            + (System.currentTimeMillis() - span) + " ms"); //$NON-NLS-1$
                    }
                }
            }
            else
            {
                if (exportSingleSheet)
                {
                    // aggregate only
                    if (singleContainer == null)
                    {
                        singleContainer = new AggregateFrame();
                        singleContainer.setRight(currentFrame.getRight()
                            - currentFrame.getLeft());
                        singleContainer.setBottom(currentFrame.getBottom()
                            - currentFrame.getTop());

                        if (landscape)
                        {
                            singleContainer.putAttribute(ATTR_LANDSCAPE,
                                Boolean.TRUE);
                        }
                    }
                    else
                    {
                        int yOff = singleContainer.getBottom();
                        singleContainer.setBottom(yOff
                            + currentFrame.getBottom() - currentFrame.getTop());

                        if (currentFrame instanceof AreaFrame)
                        {
                            ((AreaFrame) currentFrame).setYOffset(yOff);
                        }
                    }

                    singleContainer.addChild(currentFrame);
                    if (currentFrame instanceof AreaFrame)
                    {
                        ((AreaFrame) currentFrame).setParent(singleContainer);
                    }

                    if (DEBUG)
                    {
                        System.out.println("------------aggregate sheet[" //$NON-NLS-1$
                            + (currentPageIndex) + "] using " //$NON-NLS-1$
                            + (System.currentTimeMillis() - span) + " ms"); //$NON-NLS-1$
                    }
                }
                else
                {
                    // one page completed, process it.
                    Sheet modelSheet = Transformer.toSheet(currentFrame, 1000,
                        fixedColumnWidth * 1000);
                    exportSheet(modelSheet, landscape);

                    if (DEBUG)
                    {
                        System.out.println("------------export sheet[" //$NON-NLS-1$
                            + (currentPageIndex) + "] using " //$NON-NLS-1$
                            + (System.currentTimeMillis() - span) + " ms"); //$NON-NLS-1$
                    }
                }
            }
        }
        currentPageIndex++;

    }

    /**
     * Find child frame by specific data object(use "==" to compare)
     * 
     * @param parentFrame
     * @param data
     * @return
     */
    protected Frame locateChildFrame(Frame parentFrame, Object data)
    {
        for (Iterator<Frame> itr = parentFrame.iterator(); itr.hasNext();)
        {
            Frame fr = itr.next();

            if (fr.getData() == data)
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

    protected boolean isEffectiveCell(Cell cell)
    {
        if (cell.isEmpty())
        {
            return false;
        }

        return true;
    }

    private String getSheetName()
    {
        if (sheetNameExpr != null)
        {
            try
            {
                JexlContext context = JexlHelper.createContext();

                context.getVars().put("pageIndex", currentPageIndex); //$NON-NLS-1$
                context.getVars().put("sheetIndex", //$NON-NLS-1$
                    workbook.getNumSheets());
                context.getVars().put("rptContext", //$NON-NLS-1$
                    new ManagedReportContext(services.getReportContext()));

                Object result = sheetNameExpr.evaluate(context);

                if (result != null)
                {
                    return result.toString();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        // default sheet name
        return "Sheet" + workbook.getNumSheets(); //$NON-NLS-1$
    }

    final protected void exportSheet(Sheet modelSheet, boolean landscape)
    {
        try
        {
            doExportSheet(modelSheet, landscape);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void doExportSheet(Sheet modelSheet, boolean landscape)
        throws Exception
    {
        if (++sheetNum > 0)
        {
            workbook.insertSheets(sheetNum, 1);
            workbook.setSheet(sheetNum);
        }

        workbook.setShowGridLines(showGridLines);

        if (landscape)
        {
            workbook.setPrintLandscape(true);
        }

        try
        {
            workbook.setSheetName(workbook.getNumSheets() - 1, getSheetName());
        }
        catch (Exception e)
        {
            // if sheet name doesn't confrom to Excel's convention, ignore and
            // continue.
            e.printStackTrace();
        }

        short columnCount = (short) (Math.min(
            modelSheet.getActiveColumnRange(), 254) + 1);
        int rowCount = modelSheet.getActiveRowRange() + 1;

        if (columnCount + COMMENTS_WIDTH_IN_COLUMN < 255)
        {
            commentsAnchorColumnIndex = columnCount;
        }
        else
        {
            commentsAnchorColumnIndex = 255 - COMMENTS_WIDTH_IN_COLUMN;
        }

        if (rowCount + COMMENTS_HEIGHT_IN_ROW < 65535)
        {
            commentsAnchorRowIndex = rowCount;
        }
        else
        {
            commentsAnchorRowIndex = 65535 - COMMENTS_HEIGHT_IN_ROW;
        }

        boolean[] nonBlankRow = new boolean[rowCount];

        if (removeEmptyRow)
        {
            // check blank rows.
            for (int i = 0; i < rowCount; i++)
            {
                for (int j = 0; j < columnCount; j++)
                {
                    Cell cell = modelSheet.getCell(i, j, false);

                    if (cell != null && isEffectiveCell(cell))
                    {
                        nonBlankRow[i] = true;
                        break;
                    }
                }
            }
        }

        for (short i = 0; i < columnCount; i++)
        {
            double width = modelSheet.getColumnWidth(i)
                / (1000 * baseCharWidth);

            workbook.setColWidth(i, (short) (width * 256));
        }

        RangeStyle emptyCellStyle = processor.getEmptyCellStyle(false);

        for (short y = 0; y < rowCount; y++)
        {
            if (!removeEmptyRow || nonBlankRow[y])
            {
                double height = modelSheet.getRowHeight(y) / 1000d;// + 2;

                // System.out.println( "row height " + y + ": " + height );

                height *= 22; // s.vladykin: magic empirical coefficient

                workbook.setRowHeight(y, (int) height);
            }
            else
            {
                workbook.setRowHeight(y, 0);
            }

            for (short x = 0; x < columnCount; x++)
            {
                try
                {
                    workbook.setRangeStyle(emptyCellStyle, y, x, y, x);
                }
                catch (Exception e)
                {
                    e.printStackTrace();

                    continue;
                }
            }
        }

        Deque<MergeBlock> merged = new LinkedList<MergeBlock>();

        for (short y = 0; y < rowCount; y++)
        {
            if (!removeEmptyRow || nonBlankRow[y])
            {
                top: for (short x = 0; x < columnCount; x++)
                {
                    Cell element = modelSheet.getCell(y, x, false);
                    if (element != null)
                    {
                        for (MergeBlock m : merged)
                        {
                            if (m.include(y, x))
                            {
                                continue top;
                            }
                        }

                        MergeBlock mb = exportCell(element, x, y, modelSheet,
                            sheetNum);

                        if (mb != null)
                        {
                            merged.addFirst(mb);
                        }
                    }
                }
            }
        }
    }

    protected MergeBlock exportCell(Cell element, short x, short y,
        Sheet modelSheet, int sheet) throws Exception
    {
        if (element.isEmpty())
        {
            return null;
        }

        short x2 = x;
        short y2 = y;

        MergeBlock mb = null;

        if (element.isMerged())
        {
            Iterator<MergeBlock> it = modelSheet.mergesIterator();
            while (it.hasNext())
            {
                MergeBlock merge = it.next();
                if (merge.include(y, x))
                {
                    if (x == merge.getStartColumn() && y == merge.getStartRow())
                    {
                        x2 = (short) (x + merge.getColumnSpan());
                        y2 = (short) (y + merge.getRowSpan());

                        if (x != x2 || y != y2)
                            mb = merge;

                        break;
                    }
                }
            }
        }

        Object cellValue = getTranslatedElementValue(element.getValue());
        boolean useHyperLinkStyle = false;

        XlsCell cell = new XlsCell(sheet, x, y, x2, y2);

        if (cellValue instanceof ITextArea)
        {
            useHyperLinkStyle = handleHyperLink((IArea) cellValue, cell);
        }

        RangeStyle cellStyle = processor.getCellStyle(element, x, y,
            modelSheet, useHyperLinkStyle, mb);

        workbook.setRangeStyle(cellStyle, y, x, y2, x2);

        exportCellData(element, cell);

        return mb;
    }

    protected Object getTranslatedElementValue(Object value)
    {
        return value;
    }

    protected void exportCellData(Cell element, XlsCell cell) throws Exception
    {
        if (isTotalPageArea(element.getValue()))
        {
            if (totalCells == null)
            {
                totalCells = new HashSet<XlsCell>();
            }
            totalCells.add(cell);
        }

        Object cellValue = getTranslatedElementValue(element.getValue());

        if (cellValue instanceof IImageArea)
        {
            Object generatedChartState = ((IImageArea) cellValue)
                .getGeneratedChartState();
            if (generatedChartState instanceof GeneratedChartState)
            {
                // Экспортируем графики в конце чтобы не сбивать настройку
                // страниц, поскольку при экспорте вставляются страницы с
                // данными графиков
                chartCount++;
                chartCells.add(cell);
                chartStates.add((GeneratedChartState) generatedChartState);

            }
            else
            {
                exportImage((IImageArea) cellValue, cell);
            }
        }
        else if (cellValue instanceof ITextArea)
        {
            exportText((ITextArea) cellValue, cell);
        }

        if (enableComments && cellValue instanceof IArea)
        {
            handleComments((IArea) cellValue, cell);
        }
    }
    
    private double[] toDoubles(Object[] os) {
    	double[] result = new double[os.length];
    	
    	for (int i = 0; i < os.length; i++)
    		result[i] = os[i] == null ? Double.NaN : ((Number)os[i]).doubleValue();
    	 
    	return result;
    }

    private boolean writeChartValue(WorkBook workbook, int row, int col, Object val) throws Exception 
    {
    	if (val == null)
    		return true;
    	
    	if (val instanceof Number) 
    	{
			Number n = (Number) val;
			
			workbook.setNumber(row, col, n.doubleValue());
			
			return true;
		}
    	
		workbook.setText(row, col, val.toString());
    
		return false;
    }
    
    private static String chartDataSheetName(String title, int chartIndex) {
    	chartIndex++;
    	
    	// Replace illegal characters with space.
    	title = title.replace(':', ' ');
    	title = title.replace('\\',' ');
    	title = title.replace('/', ' ');
    	title = title.replace('?', ' ');
    	title = title.replace('*', ' ');
    	title = title.replace('?', ' ');
    	
    	title = "График " + chartIndex + " - " + title; // Use chart index to make sheet name unique.
    	
    	if (title.length() > 31)
    		title = title.substring(0, 31); // Make sure that title is not too long for excel sheet name.
    	
    	return title;
    }
    
    private static boolean legendVisisble(Legend legend) {
    	return legend != null;
    }
    
    private static short legendPosition(Legend legend) {
    	switch (legend.getAnchor()) {
			case NORTH_EAST_LITERAL:
				return ChartShape.LegendPlacementBottomLeftCorner;
			case EAST_LITERAL:
				return ChartShape.LegendPlacementLeft;
			case SOUTH_EAST_LITERAL:
				return ChartShape.LegendPlacementTopLeftCorner;
			case SOUTH_LITERAL:
				return ChartShape.LegendPlacementTop;
			case SOUTH_WEST_LITERAL:
				return ChartShape.LegendPlacementTopRightCorner;
			case WEST_LITERAL:
				return ChartShape.LegendPlacementRight;
			case NORTH_WEST_LITERAL:
				return ChartShape.LegendPlacementBottomRightCorner;
			case NORTH_LITERAL:
				return ChartShape.LegendPlacementBottom;
		}
    	
    	return ChartShape.LegendPlacementBottom;
    }
    
    private static int color(ColorDefinition d) {
    	return new Color(d.getRed(), d.getGreen(), d.getBlue()).getRGB();
    }
    
    private void exportChart(int chartIndex, GeneratedChartState generatedChartState,
        XlsCell cell) throws Exception
    {
        workbook.setSheet(cell.sheet);
        ChartShape chart = workbook.addChart(cell.x, cell.y, cell.x2 + 1,
            cell.y2 + 1);
        
        ChartFormat chartFormat = chart.getPlotFormat();
        chartFormat.setSolid();
        chartFormat.setForeColor(Color.WHITE.getRGB());
        chart.setPlotFormat(chartFormat);
    	
        Chart chartModel = generatedChartState.getChartModel();
        String chartType = chartModel.getType();
        String chartSubType = chartModel.getSubType();
        
        short type;
        boolean stacked = false;
        
        if ("Line Chart".equals(chartType))
        {
        	type = ChartShape.Line;
        }
        else if ("Bar Chart".equals(chartType)) 
        {
        	type = ChartShape.Column;
        	
        	stacked = "Stacked".equals(chartSubType);
        }
        else if ("Area Chart".equals(chartType)) 
        {
        	type = ChartShape.Area;
        }
        else if ("Scatter Chart".equals(chartType))
        {
        	type = ChartShape.Scatter;
        }
        else 
        {
        	throw new IllegalStateException(chartType + " is not supported.");
        }
        
        ChartWithAxes chartWithAxes = (ChartWithAxes) chartModel;
        
        // Создаем страницу для данных графика
        sheetNum++;
        workbook.insertSheets(sheetNum, 1);
        
        Text c = chartWithAxes.getTitle().getLabel().getCaption();
        
        ChartFormat format = chart.getTitleFormat();
        
        format.setFontColor(color(c.getColor()));
        format.setFontBold(c.getFont().isBold());
        format.setFontItalic(c.getFont().isItalic());
        format.setFontName(c.getFont().getName());
        format.setFontSizeInPoints(c.getFont().getSize());

        chart.setTitleFormat(format);
        
        String chartTitle = c.getValue();
        
        chart.setTitle(chartTitle);
        
        String sheetName = chartDataSheetName(chartTitle, chartIndex);
        
        workbook.setSheetName(sheetNum, sheetName);
        workbook.setSheet(sheetNum);

        // support only one x axis
        Axis[] baseAxes = chartWithAxes.getBaseAxes();
        Axis xAxis = baseAxes[0];
        
        Axis[] yAxes = chartWithAxes.getOrthogonalAxes(xAxis, true);
        chart.setYAxisCount(yAxes.length);

        ArrayList<Axis> allAxes = new ArrayList<Axis>();
        allAxes.add(xAxis);
        allAxes.addAll(Arrays.asList(yAxes));
        
        int yAxises = 0;
        int col = 0;
        int seriesLen = 0;
        ArrayList<Integer> seriesYAxises = new ArrayList<Integer>();
        ArrayList<String> seriesNames = new ArrayList<String>();
        ArrayList<Series> ySerieses = new ArrayList<Series>();
        ArrayList<SeriesDefinition> ySeriesDefinitions = new ArrayList<SeriesDefinition>();
        
        for (Axis axis : allAxes)
        {
        	String axisTitle = axis.getTitle().getCaption().getValue();

        	if (axisTitle != null && axisTitle.length() != 0 && 
        			!"X-Axis Title".equals(axisTitle) && !"Y-Axis Title".equals(axisTitle))
        		chart.setAxisTitle(col == 0 ? ChartShape.XAxis : ChartShape.YAxis, yAxises, axisTitle);
        	
        	if (col != 0)
        		yAxises++;
        	else if (axis.getType() == AxisType.LOGARITHMIC_LITERAL)
        		chart.setLogScale(true);
        	
        	int beginCol = col;
        	
            EList<SeriesDefinition> seriesDefinitions = axis.getSeriesDefinitions();

            for (SeriesDefinition seriesDefinition : seriesDefinitions)
            {
                for (Series series : seriesDefinition.getRunTimeSeries())
                {
                	String seriesTitle = series.getSeriesIdentifier().toString();
                	
                	workbook.setText(1, col, seriesTitle);
                	
                    Object[] values = (Object[]) series.getDataSet().getValues();

                    if (seriesLen < values.length)
                    	seriesLen = values.length;
                    
                    for (int i = 0; i < values.length; i++) 
                    {
                    	writeChartValue(workbook, 2 + i, col, values[i]);
                    }
                    
                    if (col != 0) {
                    	seriesYAxises.add(yAxises);
                    	seriesNames.add(seriesTitle);
                    	ySeriesDefinitions.add(seriesDefinition);
                    	ySerieses.add(series);
                    }
                    
                    col++;
                }
            }
            
            if (beginCol + 1 < col) 
            {   
            	// Merge cells on title if needed.
	            RangeStyle rangeStyle = workbook.getRangeStyle(0, beginCol, 0, col - 1);
	            rangeStyle.setMergeCells(true);
	            rangeStyle.setHorizontalAlignment(RangeStyle.HorizontalAlignmentCenter);
	            workbook.setRangeStyle(rangeStyle, 0, beginCol, 0, col - 1);
            }
            
            if (axisTitle != null && axisTitle.length() != 0 && 
            		!"X-Axis Title".equals(axisTitle) && !"Y-Axis Title".equals(axisTitle))
            	workbook.setText(0, beginCol, axisTitle);
        }
        
        for (int i = 0; i <= allAxes.size(); i++)
        {
            workbook.setColWidthAutoSize(i, true);
        }
        
        chart.setChartType(ChartShape.Combination);

        String xFormula = sheetName + "!" + workbook.formatRCNr(2, 0, true) + ":" + 
        		workbook.formatRCNr(1 + seriesLen, 0, true);

        for (int x = 1; x < col; x++) {
        	String yFormula = sheetName + "!" + workbook.formatRCNr(2, x, true) + ":" + 
	        		workbook.formatRCNr(1 + seriesLen, x, true);
	        
	        int series = x - 1;
        	
        	chart.addSeries();
        	
        	if (type == ChartShape.Scatter || type == ChartShape.Bubble)
        		chart.setSeriesXValueFormula(series, xFormula);
	        chart.setSeriesYValueFormula(series, yFormula);
	        
	        chart.setSeriesYAxisIndex(series, seriesYAxises.get(series) - 1);
	        
	        chart.setSeriesName(series, seriesNames.get(series));
	        
			Series ySeries = ySerieses.get(series);
            SeriesDefinition ySeriesDefinition = ySeriesDefinitions.get(series);

            chart.setSeriesType(series, seriesType(ySeries));
            
            try {
	            ChartFormat seriesFormat = chart.getSeriesFormat(series);
	            
	            if (ySeries instanceof LineSeries)
	            {
	                LineSeries yLineSeries = (LineSeries) ySeries;
	                int seriesIndex = ySeriesDefinition.getRunTimeSeries()
	                    .indexOf(ySeries);
	                EList<Marker> markers = yLineSeries.getMarkers();
	                Marker marker = markers.get(seriesIndex % markers.size());
	                MarkerType mtype = marker.getType();
	                switch (mtype.getValue())
	                {
	                    case MarkerType.CROSSHAIR:
	                        break;
	                    case MarkerType.TRIANGLE:
	                        break;
	                    case MarkerType.BOX:
	                        break;
	                    case MarkerType.CIRCLE:
	                        seriesFormat
	                            .setMarkerStyle(ChartFormat.MarkerCircle);
	                        break;
	                    case MarkerType.ICON:
	                        break;
	                    case MarkerType.NABLA:
	                        break;
	                    case MarkerType.DIAMOND:
	                        seriesFormat
	                            .setMarkerStyle(ChartFormat.MarkerDiamond);
	                        break;
	                    case MarkerType.FOUR_DIAMONDS:
	                        break;
	                    case MarkerType.ELLIPSE:
	                        break;
	                    case MarkerType.SEMI_CIRCLE:
	                        break;
	                    case MarkerType.HEXAGON:
	                        break;
	                    case MarkerType.RECTANGLE:
	                        break;
	                    case MarkerType.STAR:
	                        seriesFormat.setMarkerStyle(ChartFormat.MarkerStar);
	                        break;
	                    case MarkerType.COLUMN:
	                        break;
	                    case MarkerType.CROSS:
	                        break;
	                }
	                ColorDefinition color;
	                if (((LineSeries) ySeries).isPaletteLineColor())
	                {
	                    Palette seriesPalette = ySeriesDefinition
	                        .getSeriesPalette();
	                    Fill paletteFill = FillUtil.getPaletteFill(
	                        seriesPalette.getEntries(), seriesIndex);
	                    color = FillUtil.getColor(paletteFill);
	                }
	                else
	                {
	                    color = ((LineSeries) ySeries).getLineAttributes()
	                        .getColor();
	                }
	                int rgb = new Color(color.getRed(), color.getGreen(), color.getBlue()).getRGB();
	                seriesFormat.setLineColor(rgb);
	                seriesFormat.setMarkerBackground(rgb);
	                seriesFormat.setMarkerForeground(Color.BLACK.getRGB());
	            }
	            else
	            {
	            	int seriesIndex = ySeriesDefinition.getRunTimeSeries()
	                        .indexOf(ySeries);
	            	Palette seriesPalette = ySeriesDefinition
	                        .getSeriesPalette();
	                    Fill paletteFill = FillUtil.getPaletteFill(
	                        seriesPalette.getEntries(), seriesIndex);
	                ColorDefinition color = FillUtil.getColor(paletteFill);
	            	
	                int rgb = new Color(color.getRed(), color.getGreen(), color.getBlue()).getRGB();
	                
	            	seriesFormat.setForeColor(rgb);
	            	seriesFormat.setBackColor(rgb);
	            }
	            
	            chart.setSeriesFormat(series, seriesFormat);
            }
            catch(Exception e) 
            {
            	e.printStackTrace();
            }
        }
        
        if (chart.getChartType() != ChartShape.Scatter && chart.getChartType() != ChartShape.Bubble) 
        	chart.setCategoryFormula(xFormula);

        if (stacked) {
        	chart.setPlotStacked(true);
        	chart.setBarGapRatio(-100);
        }

        Legend legend = chartWithAxes.getLegend();
        
        if (legendVisisble(legend))
        	chart.setLegendPosition(legendPosition(legend));
        else
        	chart.setLegendVisible(false);
        
        /*
        for (int i = 0; i < ySeriesCount; i++)
        {
            chart.addSeries();
            
            if (chart.getChartType() == ChartShape.Scatter)
            	chart.setSeriesXValueFormula(i, sheetName + "!" + xRanges[i]);
            	
            chart.setSeriesYValueFormula(i, sheetName + "!" + yRanges[i]);
            chart.setSeriesYAxisIndex(i, yAxisIndexes.get(i));

            
        }

        chart.setTitle(chartTitle);
        chart.setAxisTitle(ChartShape.XAxis, 0, xAxis.getTitle()
            .getCaption().getValue());

        AxisType type = xAxis.getType();
        switch (type)
        {
            case LINEAR_LITERAL:
                break;
            case DATE_TIME_LITERAL:
                break;
            case TEXT_LITERAL:
                break;
            case LOGARITHMIC_LITERAL:
                chart.setLogScale(true);
                break;
        }
*/
        
    }
    
    private static short seriesType(Series s) {
    	if (s instanceof BarSeries) 
    		return ChartShape.Column;
		
    	if (s instanceof AreaSeries) 
    		return ChartShape.Area;
    	
    	if (s instanceof ScatterSeries) 
    		return ChartShape.Scatter;

    	if (s instanceof LineSeries)
    		return ChartShape.Line;
    	
    	throw new IllegalStateException("Not supported " + s.getClass().getName());
    }

    protected void exportImage(IImageArea image, XlsCell cell) throws Exception
    {
        PictureShape shape = loadPicture(image, cell);

        if (shape != null)
        {
            ShapeFormat format = shape.getFormat();
            format.setPlacementStyle(ShapeFormat.PlacementMove);
            shape.setFormat();
        }
        else if (!suppressUnknownImage)
        {
            workbook.setText(cell.y, cell.x, "<<Unsupported Image>>"); //$NON-NLS-1$
        }
    }

    protected void exportText(ITextArea text, XlsCell cell) throws Exception
    {
        exportText(text.getText(), cell);
    }

    protected void exportText(String csCellText, XlsCell cell) throws Exception
    {
        Double csNumberValue;
        try
        {
            csNumberValue = Double.parseDouble(csCellText.replace(',', '.'));
        }
        catch (NumberFormatException e)
        {
            csNumberValue = null;
        }

        BigDecimal bigValue = null;
        try
        {
            bigValue = new BigDecimal(csCellText.replace(',', '.'));
        }
        catch (NumberFormatException e)
        {
            bigValue = null;
        }

        if (csNumberValue == null || csNumberValue.isNaN())
        {
            workbook.setText(cell.sheet, cell.y, cell.x, csCellText);
        }
        else
        {
            if (bigValue == null || bigValue.precision() < 20)
            {
                workbook.setNumber(cell.sheet, cell.y, cell.x, csNumberValue);
            }
            else
            {
                workbook.setText(cell.sheet, cell.y, cell.x, csCellText);
            }
        }
    }

    protected boolean handleHyperLink(IArea area, XlsCell cell)
    {
        IHyperlinkAction hlAction = area.getAction();

        if (hlAction != null)
        {
            try
            {
                IReportRunnable runnable = services.getReportRunnable();
                String systemId = runnable == null ? null : runnable
                    .getReportName();

                Action act = new Action(systemId, hlAction);

                String link = null;
                String tooltip = EngineUtil.getActionTooltip(hlAction);

                Object ac = services.getOption(RenderOption.ACTION_HANDLER);

                if (ac instanceof IHTMLActionHandler)
                {
                    link = ((IHTMLActionHandler) ac).getURL(act,
                        services.getReportContext());
                }
                else
                {
                    link = hlAction.getHyperlink();
                }

                if (link != null)
                {
                    workbook.addHyperlink(cell.y, cell.x, cell.y2, cell.x2,
                        link, HyperLink.kURLAbs, tooltip);

                    return true;
                }
            }
            catch (Exception e)
            {
                System.out.println("create hyperlink failed."); //$NON-NLS-1$

                e.printStackTrace();
            }
        }

        return false;
    }

    protected boolean handleComments(IArea area, XlsCell cell) throws Exception
    {
        IContent content = contentCache.get(area);

        if (content != null)
        {
            Object sourceObj = content.getGenerateBy();

            if (sourceObj instanceof ReportElementDesign)
            {
                sourceObj = ((ReportElementDesign) sourceObj).getHandle();

                if (sourceObj instanceof ReportElementHandle)
                {
                    String comments = ((ReportElementHandle) sourceObj)
                        .getComments();

                    if (comments != null && comments.length() > 0)
                    {
                        workbook.addComment(cell.y, cell.x, comments, null);

                        return true;
                    }
                }
            }
        }

        return false;
    }

    protected PictureShape loadPicture(IImageArea imageArea, XlsCell cell)
        throws Exception
    {
        byte[] data = imageArea.getImageData();

        if (data == null)
        {
            String url = imageArea.getImageUrl();

            data = loadPictureData(url);
        }

        if (data != null)
        {
            int type = processor.getPictureType(data);
            if (type == -1)
            {
                try
                {
                    // try convert to png format
                    data = ImageUtil.convertImage(data, "png"); //$NON-NLS-1$
                }
                catch (IOException e)
                {
                    return null;
                }
            }

            return workbook.addPicture(cell.x, cell.y, cell.x2 + 1,
                cell.y2 + 1, data);
        }

        return null;
    }

    private byte[] loadPictureData(String url)
    {
        if (url != null)
        {
            BufferedInputStream bis = null;
            try
            {
                bis = new BufferedInputStream(new URL(url).openStream());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                byte[] buf = new byte[1024];
                int count = bis.read(buf);
                while (count != -1)
                {
                    bos.write(buf, 0, count);

                    count = bis.read(buf);
                }

                return bos.toByteArray();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (bis != null)
                {
                    try
                    {
                        bis.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public void visitText(ITextArea textArea)
    {
        buildFrame(textArea, false);
    }

    public void visitImage(IImageArea imageArea)
    {
        buildFrame(imageArea, false);
    }

    public void visitAutoText(ITemplateArea templateArea)
    {
        buildFrame(templateArea, false);

        int type = EngineUtil.getTemplateType(templateArea);

        if (type == IAutoTextContent.TOTAL_PAGE
            || type == IAutoTextContent.UNFILTERED_TOTAL_PAGE)
        {
            if (totalPageAreas == null)
            {
                totalPageAreas = new HashSet<IArea>();
            }

            totalPageAreas.add(templateArea);
        }
    }

    public void visitContainer(IContainerArea containerArea)
    {
        startContainer(containerArea);

        Iterator iter = containerArea.getChildren();
        while (iter.hasNext())
        {
            IArea child = (IArea) iter.next();
            child.accept(this);
        }

        endContainer(containerArea);
    }

    protected boolean isTotalPageArea(Object element)
    {
        if (element != null && totalPageAreas != null)
        {
            return totalPageAreas.contains(element);
        }
        return false;
    }

    public void setTotalPage(ITextArea totalPage)
    {
        if (totalPage != null && totalCells != null)
        {
            String text = totalPage.getText();

            for (Iterator<XlsCell> itr = totalCells.iterator(); itr.hasNext();)
            {
                XlsCell cell = itr.next();

                try
                {
                    exportText(text, cell);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    protected void buildFrame(IArea area, boolean isContainer)
    {
        AreaFrame frame = new AreaFrame(area);

        AreaFrame parentFrame = frameStack.isEmpty() ? null
            : (AreaFrame) frameStack.peek();
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
    }

    /**
     * Represents cell on xls sheet.
     */
    private static class XlsCell
    {

        /**
         * x, y - top left column and row x2, y2 - bottom right column and row
         */
        final int x, y, x2, y2;

        /**
         * Sheet number in workbook
         */
        final int sheet;

        XlsCell(int sheet, int x, int y, int x2, int y2)
        {
            this.sheet = sheet;
            this.x = x;
            this.y = y;
            this.x2 = x2;
            this.y2 = y2;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + sheet;
            result = prime * result + x;
            result = prime * result + x2;
            result = prime * result + y;
            result = prime * result + y2;
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            XlsCell other = (XlsCell) obj;
            if (sheet != other.sheet)
                return false;
            if (x != other.x)
                return false;
            if (x2 != other.x2)
                return false;
            if (y != other.y)
                return false;
            if (y2 != other.y2)
                return false;
            return true;
        }
    }
}