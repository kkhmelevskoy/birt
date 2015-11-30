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


import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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
import org.eclipse.birt.chart.computation.DataSetIterator;
import org.eclipse.birt.chart.computation.IConstants;
import org.eclipse.birt.chart.computation.ValueFormatter;
import org.eclipse.birt.chart.device.DisplayAdapter;
import org.eclipse.birt.chart.device.IDisplayServer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.ColorDefinition;
import org.eclipse.birt.chart.model.attribute.DataPointComponent;
import org.eclipse.birt.chart.model.attribute.Fill;
import org.eclipse.birt.chart.model.attribute.FormatSpecifier;
import org.eclipse.birt.chart.model.attribute.JavaNumberFormatSpecifier;
import org.eclipse.birt.chart.model.attribute.LineAttributes;
import org.eclipse.birt.chart.model.attribute.LineStyle;
import org.eclipse.birt.chart.model.attribute.Marker;
import org.eclipse.birt.chart.model.attribute.MarkerType;
import org.eclipse.birt.chart.model.attribute.NumberFormatSpecifier;
import org.eclipse.birt.chart.model.attribute.Palette;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.Text;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Grid;
import org.eclipse.birt.chart.model.component.Scale;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.data.DataSet;
import org.eclipse.birt.chart.model.data.NumberDataElement;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.type.AreaSeries;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.ScatterSeries;
import org.eclipse.birt.chart.reportitem.ChartReportItemImpl;
import org.eclipse.birt.chart.util.ChartUtil;
import org.eclipse.birt.chart.util.FillUtil;
import org.eclipse.birt.report.engine.api.IHTMLActionHandler;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.api.impl.Action;
import org.eclipse.birt.report.engine.content.IAutoTextContent;
import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.content.IHyperlinkAction;
import org.eclipse.birt.report.engine.content.IListContent;
import org.eclipse.birt.report.engine.content.IListGroupContent;
import org.eclipse.birt.report.engine.content.IPageContent;
import org.eclipse.birt.report.engine.content.IReportContent;
import org.eclipse.birt.report.engine.content.ITableContent;
import org.eclipse.birt.report.engine.content.ITableGroupContent;
import org.eclipse.birt.report.engine.content.impl.PageContent;
import org.eclipse.birt.report.engine.content.impl.ReportContent;
import org.eclipse.birt.report.engine.emitter.IEmitterServices;
import org.eclipse.birt.report.engine.ir.DimensionType;
import org.eclipse.birt.report.engine.ir.GroupDesign;
import org.eclipse.birt.report.engine.ir.ReportElementDesign;
import org.eclipse.birt.report.engine.nLayout.area.IArea;
import org.eclipse.birt.report.engine.nLayout.area.IAreaVisitor;
import org.eclipse.birt.report.engine.nLayout.area.IContainerArea;
import org.eclipse.birt.report.engine.nLayout.area.IImageArea;
import org.eclipse.birt.report.engine.nLayout.area.ITemplateArea;
import org.eclipse.birt.report.engine.nLayout.area.ITextArea;
import org.eclipse.birt.report.engine.nLayout.area.impl.ContainerArea;
import org.eclipse.birt.report.engine.nLayout.area.impl.ImageArea;
import org.eclipse.birt.report.engine.nLayout.area.impl.PageArea;
import org.eclipse.birt.report.model.api.ReportElementHandle;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.core.DesignElement;
import org.eclipse.emf.common.util.EList;
import org.uguess.birt.report.engine.layout.wrapper.Frame;
import org.uguess.birt.report.engine.layout.wrapper.impl.AggregateFrame;
import org.uguess.birt.report.engine.layout.wrapper.impl.AreaFrame;
import org.uguess.birt.report.engine.spreadsheet.model.Cell;
import org.uguess.birt.report.engine.spreadsheet.model.MergeBlock;
import org.uguess.birt.report.engine.spreadsheet.model.Sheet;
import org.uguess.birt.report.engine.spreadsheet.wrapper.Coordinate;
import org.uguess.birt.report.engine.spreadsheet.wrapper.Transformer;
import org.uguess.birt.report.engine.util.EngineUtil;
import org.uguess.birt.report.engine.util.ImageUtil;
import org.uguess.birt.report.engine.util.OptionParser;
import org.uguess.birt.report.engine.util.PageRangeChecker;

import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.util.ULocale;
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
    private static final int PRECISION = 1000;
    private static final int MAX_SHEET_NAME_LENGTH = 31;
    public static final String DEFAULT_FILE_NAME = "report.xls"; //$NON-NLS-1$
    protected static final String XLS_IDENTIFIER = "xls"; //$NON-NLS-1$
    protected static final String XLSX_IDENTIFIER = "xlsx"; //$NON-NLS-1$
    protected static final int COMMENTS_WIDTH_IN_COLUMN = 3;
    protected static final int COMMENTS_HEIGHT_IN_ROW = 5;
    private static final String ATTR_LANDSCAPE = "landscape"; //$NON-NLS-1$
    private static final String ATTR_PAGE_CONTENT = "pageContent";
    private static final boolean DEBUG;

    private static final String DATA_BINDING_REF = "dataBindingRef";
    private static final int MAX_DECIMAL_LENGTH = 3;

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
    private ULocale ulocale;
    private int sheetNum = -1;
    private XlsStyleProcessor processor;
    protected Stack<Frame> frameStack;
    protected Set<XlsCell> totalCells;
    protected Set<IArea> totalPageAreas;
    protected IEmitterServices services;
    private long timeCounter;
    Map<IArea, IContent> contentCache;

    private List<XlsCell> chartCells = new ArrayList<XlsCell>();
    private List<GeneratedChartState> chartStates = new ArrayList<GeneratedChartState>();
    private HashMap<Integer, Integer> doneCharts = new HashMap<Integer, Integer>();

    private ChartUtil.CacheDateFormat cacheDateFormat;

    private ArrayList<Sheet> modelSheets = new ArrayList<Sheet>();

    private short rowShift;
    private short columnShift;
    private String format;

    public XlsRenderer(String format)
    {
        this.format = format;
    }

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
        Object option = services.getEmitterConfig().get(format);
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
        removeEmptyRow = true;

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
        chartCells.clear();
        chartStates.clear();
        modelSheets.clear();
        doneCharts.clear();

        if (DEBUG)
        {
            timeCounter = System.currentTimeMillis();
        }

        ulocale = ULocale.forLocale(((ReportContent) rc).getExecutionContext()
            .getLocale());

        cacheDateFormat = new ChartUtil.CacheDateFormat(ulocale);

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
            Sheet modelSheet = Transformer.toSheet(singleContainer, PRECISION,
                fixedColumnWidth * PRECISION);
            exportSheet(modelSheet,
                Boolean.TRUE == singleContainer.getAttribute(ATTR_LANDSCAPE),
                (IPageContent) singleContainer.getAttribute(ATTR_PAGE_CONTENT));

            if (DEBUG)
            {
                System.out
                    .println("------------export total single sheet using " //$NON-NLS-1$
                        + (System.currentTimeMillis() - span) + " ms"); //$NON-NLS-1$
            }
        }

        for (int i = 0; i < chartCells.size(); i++)
        {
            XlsCell cell = chartCells.get(i);

            try
            {
                exportChart(i, chartStates.get(i), cell);
            }
            catch (Exception e)
            {
                try
                {
                    Integer cellSheet = Integer.valueOf(cell.sheet);
                    doneCharts.put(cellSheet, Integer.valueOf(doneCharts.get(
                        cellSheet).intValue() - 1));
                    workbook.removeChart(doneCharts.get(cellSheet).intValue());
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                e.printStackTrace(new PrintStream(stream));

                exportText(stream.toString(), cell);

                try
                {
                    RangeStyle style = workbook.getRangeStyle();
                    style.setFontSize(20 * 8);
                    workbook.setRangeStyle(style, cell.y - rowShift, cell.x
                        - columnShift, cell.y2 + 1 - rowShift, cell.x2 + 1
                        - columnShift);
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            }
        }

        try
        {
            workbook.setSheet(0); // activate first sheet in report

            if (XLSX_IDENTIFIER.equals(format))
            {
                workbook.writeXLSX(output);
            }
            else
            {
                workbook.write(output);
            }

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
                        singleContainer
                            .putAttribute(ATTR_PAGE_CONTENT, content);

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
                    Sheet modelSheet = Transformer.toSheet(bodyFrame,
                        PRECISION, fixedColumnWidth * PRECISION);
                    exportSheet(modelSheet, landscape, content);

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
                        singleContainer
                            .putAttribute(ATTR_PAGE_CONTENT, content);
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
                    Sheet modelSheet = Transformer.toSheet(currentFrame,
                        PRECISION, fixedColumnWidth * PRECISION);
                    exportSheet(modelSheet, landscape, content);

                    if (DEBUG)
                    {
                        System.out.println("------------export sheet[" //$NON-NLS-1$
                            + (currentPageIndex) + "] using " //$NON-NLS-1$
                            + (System.currentTimeMillis() - span) + " ms"); //$NON-NLS-1$
                    }
                }
            }
            String sheetName = getPageTitle(pa);

            if (sheetName == null && currentPageIndex == 1)
            {
                sheetName = pa.getContent().getReportContent().getTitle();
            }

            if (sheetName != null)
            {
                if (sheetName.length() > MAX_SHEET_NAME_LENGTH)
                {
                    sheetName = sheetName.substring(0, MAX_SHEET_NAME_LENGTH);
                }
                try
                {
                    workbook.setSheetName(currentPageIndex - 1, sheetName);
                }
                catch (Exception e)
                {
                    if (e.getMessage().contains(
                        "Duplicate sheet names are not allowed."))
                    {
                        // do nothing, no critical error
                    }
                    else
                    {
                        throw new RuntimeException(e);
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
        // if (cell.isEmpty())
        if (cell.getValue() == null)
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

    final protected void exportSheet(Sheet modelSheet, boolean landscape,
        IPageContent content)
    {
        try
        {
            doExportSheet(modelSheet, landscape, content);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void doExportSheet(Sheet modelSheet, boolean landscape,
        IPageContent content) throws Exception
    {
        modelSheets.add(modelSheet);

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
        boolean[] emptyColumn = new boolean[columnCount];
        rowShift = 0;
        columnShift = 0;

        int rowTo = rowCount - 1;
        int columnTo = columnCount - 1;

        if (removeEmptyRow)
        {
            // check blank rows.
            for (int row = 0; row < rowCount; row++)
            {
                for (int col = 0; col < columnCount; col++)
                {
                    Cell cell = modelSheet.getCell(row, col, false);

                    if (cell != null && isEffectiveCell(cell))
                    {
                        nonBlankRow[row] = true;
                        break;
                    }
                }
            }

            for (int row = 0; row < rowCount && !nonBlankRow[row]; row++)
            {
                rowShift++;
            }

            for (int row = rowCount - 1; row >= 0 && !nonBlankRow[row]; row--)
            {
                rowTo = row;
            }

            // check blank columns
            for (int col = 0; col < columnCount; col++)
            {
                emptyColumn[col] = true;

                for (int row = rowShift; row <= rowTo; row++)
                {
                    Cell cell = modelSheet.getCell(row, col, false);

                    if (cell != null && isEffectiveCell(cell))
                    {
                        emptyColumn[col] = false;
                        break;
                    }
                }
            }

            for (int col = 0; col < columnCount && emptyColumn[col]; col++)
            {
                columnShift++;
            }

            for (int col = columnCount - 1; col >= 0 && emptyColumn[col]; col--)
            {
                columnTo = col;
            }

            for (int y = rowCount - 1; y >= rowShift; y--)
            {
                if (nonBlankRow[y])
                {
                    rowCount = y + 1;
                    break;
                }
            }
        }

        for (short i = columnShift; i <= columnTo; i++)
        {
            double width = modelSheet.getColumnWidth(i)
                / (1000 * baseCharWidth);
            workbook.setColWidth(i - columnShift, (short) (width * 256));
        }

        RangeStyle emptyCellStyle = processor.getEmptyCellStyle(false);

        for (short y = (short) (rowTo); y >= rowShift; y--)
        {
            if (!removeEmptyRow || nonBlankRow[y])
            {
                double height = modelSheet.getRowHeight(y) / 1000d;// + 2;

                height *= 22; // s.vladykin: magic empirical coefficient

                workbook.setRowHeight(y - rowShift, (int) height);
            }
            else
            {
                workbook.setRowHeight(y - rowShift, 0);
            }

            for (short x = columnShift; x <= columnTo; x++)
            {
                try
                {
                    workbook.setRangeStyle(emptyCellStyle, y - rowShift, x
                        - columnShift, y - rowShift, x - columnShift);
                }
                catch (Exception e)
                {
                    e.printStackTrace();

                    continue;
                }
            }
        }

        Deque<MergeBlock> merged = new LinkedList<MergeBlock>();

        for (short y = rowShift; y <= rowTo; y++)
        {
            if (!removeEmptyRow || nonBlankRow[y])
            {
                top: for (short x = columnShift; x <= columnTo; x++)
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
                            sheetNum, ((y + 1) == rowCount));

                        if (mb != null)
                        {
                            merged.addFirst(mb);
                        }
                    }
                }
            }
        }

        workbook.setPrintPaperSize(SmartxlsPaperSize.paperSize(content,
            SmartxlsPaperSize.kPaperA4));

        workbook.setPrintLeftMargin(content.getMarginLeft().convertTo(
            DimensionType.UNITS_IN));
        workbook.setPrintRightMargin(content.getMarginRight().convertTo(
            DimensionType.UNITS_IN));
        workbook.setPrintTopMargin(content.getMarginTop().convertTo(
            DimensionType.UNITS_IN));
        workbook.setPrintBottomMargin(content.getMarginBottom().convertTo(
            DimensionType.UNITS_IN));

        double printHeaderMargin = content.getHeaderHeight().convertTo(
            DimensionType.UNITS_IN);
        double printFooterMargin = content.getFooterHeight().convertTo(
            DimensionType.UNITS_IN);
        workbook.setPrintHeaderMargin(printHeaderMargin);
        workbook.setPrintFooterMargin(printFooterMargin);
        if (printHeaderMargin == 0)
        {
            workbook.setPrintHeader("");
        }
        if (printFooterMargin == 0)
        {
            workbook.setPrintFooter("");
        }

        workbook.setPrintScaleFitToPage(true);
        double printPageWidth = landscape ? 297 : 210;
        double printPageHeight = landscape ? 210 : 297;
        double printWidth = content.getPageWidth().convertTo(
            DimensionType.UNITS_MM);
        double printHeight = content.getPageHeight().convertTo(
            DimensionType.UNITS_MM);
        int hPages = (int) Math.ceil(printWidth / printPageWidth);
        int vPages = (int) Math.ceil(printHeight / printPageHeight);

        workbook.setPrintScaleFitHPages(hPages);
        workbook.setPrintScaleFitVPages(vPages);
    }

    protected MergeBlock exportCell(Cell element, short x, short y,
        Sheet modelSheet, int sheet, boolean isLastRow) throws Exception
    {
        if (element.isEmpty())
        {
            return null;
        }

        short x2 = x;
        short y2 = y;

        MergeBlock mb = null;

        if (!isLastRow && element.isMerged())
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

        workbook.setRangeStyle(cellStyle, y - rowShift, x - columnShift, y2
            - rowShift, x2 - columnShift);

        exportCellData(element, modelSheet, cell);

        return mb;
    }

    protected Object getTranslatedElementValue(Object value)
    {
        return value;
    }

    protected void exportCellData(Cell element, Sheet modelSheet, XlsCell cell)
        throws Exception
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
                // Экспортируем графики в конце
                // чтобы не сбивать настройку
                // страниц, поскольку при экспорте
                // вставляются страницы с
                // данными графиков
                chartCells.add(cell);
                chartStates.add((GeneratedChartState) generatedChartState);

            }
            else
            {
                exportImage((IImageArea) cellValue, modelSheet, cell);
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

    private static double roundTo(Number n, int fractionDigits)
    {
        double res = n.doubleValue();

        double scale = 1;

        for (int i = 0; i < fractionDigits; i++)
        {
            scale *= 10;
        }

        return Math.round(res * scale) / scale;
    }

    /**
     * @param workbook
     * @param row
     * @param col
     * @param val
     * @param valType {@link IConstants#NUMERICAL}, {@link IConstants#DATE_TIME}
     *            , {@link IConstants#TEXT}
     * @param formatSpec
     * @param oCachedJavaFormatter
     * @throws Exception
     */
    private void writeChartValue(WorkBook workbook, int row, int col,
        Object val, int valType, FormatSpecifier formatSpec,
        Object oCachedJavaFormatter) throws Exception
    {
        if (val == null)
        {
            return;
        }

        if (valType == IConstants.NUMERICAL && val instanceof Number)
        {
            Number n = (Number) val;

            if (formatSpec instanceof NumberFormatSpecifier)
            {
                NumberFormatSpecifier nfs = (NumberFormatSpecifier) formatSpec;

                if (nfs.isSetFractionDigits())
                {
                    n = roundTo(n, nfs.getFractionDigits());
                }
            }
            else if (formatSpec instanceof JavaNumberFormatSpecifier)
            {
                JavaNumberFormatSpecifier nfs = (JavaNumberFormatSpecifier) formatSpec;

                DecimalFormat df = (DecimalFormat) DecimalFormat
                    .getInstance(ulocale);

                df.applyPattern(nfs.getPattern());

                String tmp = df.format(n);

                n = df.parse(tmp);
            }

            workbook.setNumber(row - rowShift, col - columnShift,
                n.doubleValue());

            RangeStyle rangeStyle = workbook.getRangeStyle(row - rowShift, col
                - columnShift, row - rowShift, col - columnShift);
            rangeStyle.setCustomFormat(getNumberFormat(n.doubleValue()));
            workbook.setRangeStyle(rangeStyle, row - rowShift, col
                - columnShift, row - rowShift, col - columnShift);
        }
        else
        {
            String str = ValueFormatter.format(val, formatSpec, ulocale,
                oCachedJavaFormatter);

            workbook.setText(row - rowShift, col - columnShift, str);
        }
    }

    private static String chartDataSheetName(String prefix, String title,
        int chartIndex)
    {
        return chartDataSheetName(prefix, title, chartIndex, false);
    }

    private static String chartDataSheetName(String prefix, String title,
        int chartIndex, boolean shrinkPrefix)
    {
        // StringBuilder name = new StringBuilder("График " + chartIndex);
        StringBuilder name = new StringBuilder(prefix != null ? 
            (shrinkPrefix ? abbreviate(prefix) : prefix) + " - "
            : ""); // Use chart index to make sheet name unique.

        if (title != null)
        {
            // Replace illegal characters with space.
            title = title.replace(':', ' ');
            title = title.replace('\\', ' ');
            title = title.replace('/', ' ');
            title = title.replace('?', ' ');
            title = title.replace('*', ' ');
            title = title.replace('?', ' ');

            name.append(title);
        }
        else
        {
            name.append("График " + (chartIndex + 1));
        }

        if (name.length() > MAX_SHEET_NAME_LENGTH)
        {
            // Make sure that name is not too long for excel sheet name.
            if (!shrinkPrefix)
            {
                return chartDataSheetName(prefix, title, chartIndex, true);
            }
            else
            {
                name.setLength(MAX_SHEET_NAME_LENGTH);
            }
        }

        return name.toString();
    }

    private static String abbreviate(String text)
    {   
        String s = text;
        while (s.indexOf("  ") > -1)
        {
            s = text.replace("  ", " ");
        }
        
        StringBuffer buffer = new StringBuffer();
        String[] words = s.split(" ");
        
        for (int i = 0; i < words.length; i++) {
            buffer.append(words[i].substring(0, 1).toUpperCase());
        }
        
        return buffer.toString();
    }

    private static boolean legendVisisble(Legend legend)
    {
        return legend != null && legend.isVisible();
    }

    private static short legendPosition(Legend legend)
    {
        switch (legend.getPosition())
        {
            case LEFT_LITERAL:
                return ChartShape.LegendPlacementLeft;
            case ABOVE_LITERAL:
                return ChartShape.LegendPlacementTop;
            case RIGHT_LITERAL:
                return ChartShape.LegendPlacementRight;
            default:
                return ChartShape.LegendPlacementBottom;
        }
    }

    private static int color(ColorDefinition d)
    {
        return new Color(d.getRed(), d.getGreen(), d.getBlue()).getRGB();
    }

    private static <X> X[] reverse(X[] arr)
    {
        // if (arr == null || arr.length == 1)
        // return arr;
        //
        // int i = 0, j = arr.length - 1;
        //
        // while (i < j) {
        // X tmp = arr[i];
        // arr[i++] = arr[j];
        // arr[j--] = tmp;
        // }

        return arr;
    }

    private void applyTextFormat(Text c, ChartFormat format) throws Exception
    {
        format.setFontColor(color(c.getColor()));
        format.setFontBold(c.getFont().isBold());
        format.setFontItalic(c.getFont().isItalic());
        format
            .setFontName("sansserif".equals(c.getFont().getName()) ? "sans-serif"
                : c.getFont().getName());
        format.setFontSizeInPoints(c.getFont().getSize());
        format.setTextRotation((int) c.getFont().getRotation());
    }

    private static String externalizedMessage(
        GeneratedChartState generatedChartState, String sChartKey)
    {
        try
        {
            return generatedChartState.getRunTimeContext().externalizedMessage(
                sChartKey);
        }
        catch (Exception e)
        {
            e.printStackTrace();

            return sChartKey;
        }
    }

    private void exportChart(int chartIndex,
        GeneratedChartState generatedChartState, XlsCell cell) throws Exception
    {
        workbook.setSheet(cell.sheet);
        ChartShape chart = workbook.addChart(cell.x - columnShift, cell.y
            - rowShift, cell.x2 + 1 - columnShift, cell.y2 + 1 - rowShift);

        Integer cellSheet = Integer.valueOf(cell.sheet);

        if (doneCharts.containsKey(cellSheet))
        {
            doneCharts.put(cellSheet,
                Integer.valueOf(doneCharts.get(cellSheet).intValue() + 1));
        }
        else
        {
            doneCharts.put(cellSheet, Integer.valueOf(1));
        }

        ChartFormat chartFormat = chart.getPlotFormat();
        chartFormat.setSolid();
        chartFormat.setForeColor(Color.WHITE.getRGB());
        chart.setPlotFormat(chartFormat);

        Chart chartModel = generatedChartState.getChartModel();
        String chartType = chartModel.getType();
        String chartSubType = chartModel.getSubType();

        short type;
        short chType = ChartShape.Combination;
        boolean stacked = "Stacked".equals(chartSubType);

        if ("Line Chart".equals(chartType))
        {
            type = ChartShape.Line;
        }
        else if ("Bar Chart".equals(chartType))
        {
            type = ChartShape.Column;
        }
        else if ("Area Chart".equals(chartType))
        {
            type = ChartShape.Area;
        }
        else if ("Scatter Chart".equals(chartType))
        {
            type = ChartShape.Scatter;
            chType = type;
        }
        else
        {
            throw new IllegalStateException(chartType + " is not supported.");
        }

        chart.setChartType(chType);

        ChartWithAxes chartWithAxes = (ChartWithAxes) chartModel;
        String chartTitle = null;

        if (chartWithAxes.getTitle().isVisible())
        {
            Text c = chartWithAxes.getTitle().getLabel().getCaption();

            ChartFormat format = chart.getTitleFormat();

            applyTextFormat(c, format);

            chart.setTitleFormat(format);

            chartTitle = c.getValue();
            chartTitle = externalizedMessage(generatedChartState, chartTitle);

            chart.setTitle(chartTitle);
        }

        // Создаем страницу для данных графика
        boolean isChartNeedExtraDataSheet = isChartNeedExtraDataSheet(workbook,
            cell, generatedChartState);

        String sheetName = null;

        if (isChartNeedExtraDataSheet)
        {
            sheetNum++;
            workbook.insertSheets(sheetNum, 1);

            sheetName = chartDataSheetName(workbook.getSheetName(cell.sheet),
                chartTitle, chartIndex);

            workbook.setSheetName(sheetNum, sheetName);
            workbook.setSheet(sheetNum);
        }
        else
        {
            sheetName = workbook.getSheetName(cell.sheet);
        }

        // support only one x axis
        Axis[] baseAxes = chartWithAxes.getBaseAxes();
        Axis xAxis = baseAxes[0];

        Axis[] yAxes = chartWithAxes.getOrthogonalAxes(xAxis, true);
        chart.setYAxisCount(yAxes.length);

        ArrayList<Axis> allAxes = new ArrayList<Axis>();
        allAxes.add(xAxis);
        yAxes = reverse(yAxes);
        allAxes.addAll(Arrays.asList(yAxes));

        int yAxises = 0;
        int col = 0;
        int seriesLen = 0;

        ArrayList<SeriesDefinition> ySeriesDefinitions = new ArrayList<SeriesDefinition>();
        ArrayList<Integer> seriesYAxises = new ArrayList<Integer>();
        ArrayList<String> seriesNames = new ArrayList<String>();
        ArrayList<Series> ySerieses = new ArrayList<Series>();

        String xFormula = null;
        ArrayList<String> yFormulas = new ArrayList<String>();

        for (Axis axis : allAxes)
        {
            Text c = axis.getTitle().getCaption();
            String axisTitle = c.getValue();
            axisTitle = externalizedMessage(generatedChartState, axisTitle);

            short xyAxis = col == 0 ? ChartShape.XAxis : ChartShape.YAxis;

            ChartFormat axisFormat = chart.getAxisTitleFormat(xyAxis, yAxises);
            applyTextFormat(c, axisFormat);
            chart.setAxisTitleFormat(xyAxis, yAxises, axisFormat);

            FormatSpecifier axisFormatSpec = axis.getFormatSpecifier();

            if (axis.getLabel().isSetVisible() && axis.getLabel().isVisible()
                || allAxes.size() > 2)
            {
                c = axis.getLabel().getCaption();

                // chart.setMajorGridVisible(xyAxis, yAxises,
                // isGridVisible(axis.getMajorGrid()));
                // chart.setMinorGridVisible(xyAxis, yAxises,
                // isGridVisible(axis.getMinorGrid()));

                // if (xyAxis == ChartShape.XAxis)
                // {
                // chart.setAxisScaleLabelUnit(xyAxis, 0, 0);
                // }

                axisFormat = chart.getAxisFormat(xyAxis, yAxises);
                applyTextFormat(c, axisFormat);

                if (axis.getTitle().isVisible() && axisTitle != null
                    && axisTitle.length() != 0
                    && !"X-Axis Title".equals(axisTitle)
                    && !"Y-Axis Title".equals(axisTitle))
                {
                    chart.setAxisTitle(xyAxis, yAxises, axisTitle);
                }

                if (!"X-Axis Title".equals(axisTitle))
                {
                    applyFormatSpecifier(axisFormat, axisFormatSpec);
                }

                if (axis.getType() == AxisType.LINEAR_LITERAL)
                {
                    boolean isMinSet = false;
                    boolean isMaxSet = false;
                    double min = 0;
                    double max = 0;
                    Scale scale = axis.getScale();

                    if (scale.getMin() instanceof NumberDataElement)
                    {
                        NumberDataElement numberDataElement = (NumberDataElement) scale
                            .getMin();

                        if (numberDataElement.isSetValue())
                        {
                            try
                            {
                                chart.setAutoMinimumScale(xyAxis, yAxises,
                                    false);
                                min = numberDataElement.getValue();
                                isMinSet = true;
                            }
                            catch (Exception e)
                            {
                                // e.printStackTrace();
                            }
                        }
                    }

                    if (scale.getMax() instanceof NumberDataElement)
                    {
                        NumberDataElement numberDataElement = (NumberDataElement) scale
                            .getMax();

                        if (numberDataElement.isSetValue())
                        {
                            try
                            {
                                chart.setAutoMaximumScale(xyAxis, yAxises,
                                    false);
                                max = numberDataElement.getValue();
                                isMaxSet = true;
                            }
                            catch (Exception e)
                            {
                                // e.printStackTrace();
                            }
                        }
                    }

                    if (isMinSet || isMaxSet)
                    {
                        chart.setScaleValueRange(xyAxis, yAxises, min, max);
                    }
                }

                chart.setAxisFormat(xyAxis, yAxises, axisFormat);
            }
            else
            {
                chart.setAxisVisible(xyAxis, yAxises, false);
            }

            if (col != 0)
                yAxises++;
            else if (axis.getType() == AxisType.LOGARITHMIC_LITERAL)
                chart.setLogScale(true);

            int beginCol = col;

            EList<SeriesDefinition> unsortedSeriesDefinitions = axis
                .getSeriesDefinitions();
            SeriesDefinition[] sortedSeriesDefinitions = unsortedSeriesDefinitions
                .toArray(new SeriesDefinition[unsortedSeriesDefinitions.size()]);

            Arrays.sort(sortedSeriesDefinitions,
                new Comparator<SeriesDefinition>()
                {
                    @Override
                    public int compare(SeriesDefinition o1, SeriesDefinition o2)
                    {
                        return Integer.valueOf(o1.getZOrder()).compareTo(
                            Integer.valueOf(o2.getZOrder()));
                    }
                });

            for (SeriesDefinition seriesDefinition : sortedSeriesDefinitions)
            {
                for (Series series : seriesDefinition.getRunTimeSeries())
                {
                    String seriesTitle = series.getSeriesIdentifier()
                        .toString();
                    seriesTitle = externalizedMessage(generatedChartState,
                        seriesTitle);

                    if (isChartNeedExtraDataSheet)
                    {
                        workbook.setText(1, col, seriesTitle);
                    }

                    DataSet dataSet = series.getDataSet();

                    DataSetIterator it = new DataSetIterator(dataSet);

                    Object cachedFormat = null;

                    if (it.getDataType() == IConstants.DATE_TIME)
                    {
                        int dateUnit = ChartUtil.computeDateTimeCategoryUnit(
                            chartModel, it);

                        cachedFormat = cacheDateFormat.get(dateUnit);
                    }

                    Object[] values = (Object[]) dataSet.getValues();

                    if (!isChartNeedExtraDataSheet)
                    {
                        String chartDataSource = getChartDataSource(generatedChartState);

                        if (chartDataSource != null)
                        {
                            Sheet sheet = modelSheets.get(cell.sheet);

                            Coordinate tableCoord = sheet
                                .getTableCoord(chartDataSource);
                            Coordinate dataCoord = null;

                            if (tableCoord != null)
                            {
                                Object[] formattedValues = new Object[values.length];

                                for (int i = 0; i < values.length; i++)
                                {
                                    formattedValues[i] = convertChartValue(
                                        workbook, 0, 0, values[i],
                                        it.getDataType(), axisFormatSpec,
                                        cachedFormat);
                                }

                                dataCoord = getDataCoord(workbook, cell.sheet,
                                    tableCoord, formattedValues);

                                if (dataCoord != null)
                                {
                                    if (col == 0)
                                    {
                                        xFormula = getChartSeriesFormula(
                                            sheetName, dataCoord.getY1()
                                                - rowShift, dataCoord.getX1()
                                                - columnShift,
                                            dataCoord.getY2() - rowShift,
                                            dataCoord.getX2() - columnShift);
                                    }
                                    else
                                    {
                                        yFormulas.add(getChartSeriesFormula(
                                            sheetName, dataCoord.getY1()
                                                - rowShift, dataCoord.getX1()
                                                - columnShift,
                                            dataCoord.getY2() - rowShift,
                                            dataCoord.getX2() - columnShift));
                                    }
                                }
                            }
                        }
                    }

                    if (seriesLen < values.length)
                        seriesLen = values.length;

                    if (isChartNeedExtraDataSheet)
                    {
                        for (int i = 0; i < values.length; i++)
                        {
                            writeChartValue(workbook, 2 + i + rowShift, col
                                + columnShift, values[i], it.getDataType(),
                                axisFormatSpec, cachedFormat);
                        }
                    }

                    if (col != 0)
                    {
                        seriesYAxises.add(yAxises);
                        seriesNames.add(seriesTitle);
                        ySeriesDefinitions.add(seriesDefinition);
                        ySerieses.add(series);
                    }

                    col++;
                }
            }

            if (isChartNeedExtraDataSheet)
            {
                if (beginCol + 1 < col)
                {
                    // Merge cells on title if needed.
                    RangeStyle rangeStyle = workbook.getRangeStyle(0, beginCol,
                        0, col - 1);
                    rangeStyle.setMergeCells(true);
                    rangeStyle
                        .setHorizontalAlignment(RangeStyle.HorizontalAlignmentCenter);
                    workbook.setRangeStyle(rangeStyle, 0, beginCol, 0, col - 1);
                }

                if (axisTitle != null && axisTitle.length() != 0
                    && !"X-Axis Title".equals(axisTitle)
                    && !"Y-Axis Title".equals(axisTitle))
                {
                    workbook.setText(0, beginCol, axisTitle);
                }
            }
        }

        if (isChartNeedExtraDataSheet)
        {
            for (int i = 0; i <= allAxes.size(); i++)
            {
                workbook.setColWidthAutoSize(i, true);
            }
        }

        if (isChartNeedExtraDataSheet)
        {
            xFormula = getChartSeriesFormula(sheetName, 2, 0, 1 + seriesLen, 0);
        }

        for (int x = 1; x < col; x++)
        {
            String yFormula;

            if (isChartNeedExtraDataSheet)
            {
                yFormula = getChartSeriesFormula(sheetName, 2, x,
                    1 + seriesLen, x);
            }
            else
            {
                yFormula = yFormulas.remove(0);
            }

            int series = x - 1;

            chart.addSeries();

            if (type == ChartShape.Scatter || type == ChartShape.Bubble)
            {
                chart.setSeriesXValueFormula(series, xFormula);
            }
            chart.setSeriesYValueFormula(series, yFormula);

            chart.setSeriesYAxisIndex(series, seriesYAxises.get(series) - 1);
            chart.setSeriesName(series, seriesNames.get(series));

            Series ySeries = ySerieses.get(series);
            SeriesDefinition ySeriesDefinition = ySeriesDefinitions.get(series);

            if (type != ChartShape.Scatter)
            {
                chart.setSeriesType(series, seriesType(ySeries));
            }

            try
            {
                ChartFormat seriesFormat = chart.getSeriesFormat(series);
                ChartFormat labelFormat = chart.getDataLabelFormat(series);

                // applySeriesLabels(ySeries, seriesFormat);
                applySeriesLabels(ySeries, labelFormat);

                FormatSpecifier dataPointFormatSpec = getDataPointFormat(ySeries);

                applyFormatSpecifier(labelFormat, dataPointFormatSpec);

                if (ySeries instanceof LineSeries)
                {
                    LineSeries yLineSeries = (LineSeries) ySeries;
                    int seriesIndex = ySeriesDefinition.getRunTimeSeries()
                        .indexOf(ySeries);

                    ColorDefinition color;
                    if (yLineSeries.isPaletteLineColor())
                    {
                        Palette seriesPalette = ySeriesDefinition
                            .getSeriesPalette();
                        Fill paletteFill = FillUtil.getPaletteFill(
                            seriesPalette.getEntries(), seriesIndex);
                        color = FillUtil.getColor(paletteFill);
                    }
                    else
                    {
                        color = yLineSeries.getLineAttributes().getColor();
                    }
                    int rgb = new Color(color.getRed(), color.getGreen(),
                        color.getBlue()).getRGB();

                    EList<Marker> markers = yLineSeries.getMarkers();

                    Marker marker = markers.get(seriesIndex % markers.size());

                    applySeriesMarker(marker, rgb, seriesFormat);

                    if (!yLineSeries.getLineAttributes().isVisible())
                    {
                        seriesFormat.setLineNone();
                    }
                    else
                    {
                        seriesFormat.setLineStyle(getLineStyle(yLineSeries
                            .getLineAttributes().getStyle()));
                        seriesFormat.setLineColor(rgb);

                        if (yLineSeries.getLineAttributes().isSetThickness())
                        {
                            seriesFormat.setLineWeight(getLineWight(yLineSeries
                                .getLineAttributes().getThickness()));
                        }
                    }
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

                    int rgb = new Color(color.getRed(), color.getGreen(),
                        color.getBlue()).getRGB();

                    seriesFormat.setSolid();
                    seriesFormat.setForeColor(rgb);
                    seriesFormat.setBackColor(rgb);
                }

                chart.setSeriesFormat(series, seriesFormat);
                chart.setDataLabelFormat(series, labelFormat);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (chart.getChartType() != ChartShape.Scatter
            && chart.getChartType() != ChartShape.Bubble && xFormula != null)
        {
            chart.setCategoryFormula(xFormula);
        }

        if (stacked)
        {
            chart.setPlotStacked(true);
            chart.setBarGapRatio(-100);
        }

        Legend legend = chartWithAxes.getLegend();

        if (legendVisisble(legend))
        {
            chart.setLegendPosition(legendPosition(legend));

            ChartFormat legendFormat = chart.getLegendFormat();
            LineAttributes legendOutline = legend.getOutline();

            if (legendOutline.isSetVisible() && legendOutline.isVisible())
            {
                legendFormat
                    .setLineStyle(getLineStyle(legendOutline.getStyle()));

                if (legendOutline.isSetThickness())
                {
                    legendFormat.setLineWeight(getLineWight(legendOutline
                        .getThickness()));
                }
            }
            else
            {
                legendFormat.setLineNone();
            }

            chart.setLegendFormat(legendFormat);
        }
        else
        {
            chart.setLegendVisible(false);
        }

        /*
         * for (int i = 0; i < ySeriesCount; i++) { chart.addSeries(); if
         * (chart.getChartType() == ChartShape.Scatter)
         * chart.setSeriesXValueFormula(i, sheetName + "!" + xRanges[i]);
         * chart.setSeriesYValueFormula(i, sheetName + "!" + yRanges[i]);
         * chart.setSeriesYAxisIndex(i, yAxisIndexes.get(i)); }
         * chart.setTitle(chartTitle); chart.setAxisTitle(ChartShape.XAxis, 0,
         * xAxis.getTitle() .getCaption().getValue()); AxisType type =
         * xAxis.getType(); switch (type) { case LINEAR_LITERAL: break; case
         * DATE_TIME_LITERAL: break; case TEXT_LITERAL: break; case
         * LOGARITHMIC_LITERAL: chart.setLogScale(true); break; }
         */
    }

    private void applyFormatSpecifier(ChartFormat chartFormat,
        FormatSpecifier formatSpec) throws Exception
    {
        if (formatSpec instanceof NumberFormatSpecifier)
        {
            NumberFormatSpecifier nfs = (NumberFormatSpecifier) formatSpec;

            if (nfs.isSetFractionDigits())
            {
                String fractionMask = getFormatMask(nfs);

                if (fractionMask.length() > 0)
                {
                    chartFormat.setCustomFormat("# ##0."
                        + fractionMask.toString());
                }
                else
                {
                    chartFormat.setCustomFormat("# ##0");
                }
            }
        }
    }

    private String getFormatMask(NumberFormatSpecifier nfs)
    {
        int numDigits = nfs.getFractionDigits();
        StringBuilder fraction = new StringBuilder();

        for (int i = 0; i < numDigits; i++)
        {
            fraction.append("0");
        }

        return fraction.toString();
    }

    private FormatSpecifier getDataPointFormat(Series series)
    {
        EList<DataPointComponent> dataPoints = series.getDataPoint()
            .getComponents();

        if (dataPoints.size() > 0)
        {
            return dataPoints.get(0).getFormatSpecifier();
        }

        return null;
    }

    private short getLineStyle(LineStyle style)
    {
        switch (style)
        {
            case SOLID_LITERAL:
                return ChartFormat.LineSolid;
            case DASHED_LITERAL:
                return ChartFormat.LineDash;
            case DOTTED_LITERAL:
                return ChartFormat.LineDot;
            case DASH_DOTTED_LITERAL:
                return ChartFormat.LineDashDot;

            default:
                return ChartFormat.LineSolid;
        }
    }

    private int getLineWight(int thickness)
    {
        switch (thickness)
        {
            case 1:
                return ChartFormat.Hairline;
            case 2:
                return ChartFormat.Narrow;
            case 3:
                return ChartFormat.Medium;
            case 4:
                return ChartFormat.Wide;

            default:
                return ChartFormat.Medium;
        }
    }

    private void applySeriesMarker(Marker marker, int rgb,
        ChartFormat seriesFormat) throws Exception
    {
        if (marker.isSetVisible() && marker.isVisible())
        {
            seriesFormat.setMarkerBackground(rgb);
            seriesFormat.setMarkerForeground(rgb);

            MarkerType mtype = marker.getType();

            switch (mtype.getValue())
            {
                case MarkerType.CROSSHAIR:
                    seriesFormat.setMarkerStyle(ChartFormat.MarkerPlus);
                    break;

                case MarkerType.NABLA:
                case MarkerType.TRIANGLE:
                    seriesFormat.setMarkerStyle(ChartFormat.MarkerTriangle);
                    break;

                case MarkerType.RECTANGLE:
                case MarkerType.BOX:
                case MarkerType.COLUMN:
                    seriesFormat.setMarkerStyle(ChartFormat.MarkerSquare);
                    break;

                case MarkerType.CIRCLE:
                case MarkerType.ELLIPSE:
                    seriesFormat.setMarkerStyle(ChartFormat.MarkerCircle);
                    break;

                case MarkerType.FOUR_DIAMONDS:
                case MarkerType.DIAMOND:
                    seriesFormat.setMarkerStyle(ChartFormat.MarkerDiamond);
                    break;

                case MarkerType.STAR:
                    seriesFormat.setMarkerStyle(ChartFormat.MarkerStar);
                    break;

                case MarkerType.CROSS:
                    seriesFormat.setMarkerStyle(ChartFormat.MarkerX);
                    break;

                case MarkerType.SEMI_CIRCLE:
                    break;

                case MarkerType.HEXAGON:
                    break;

                case MarkerType.ICON:
                    break;
            }
        }
        else
        {
            seriesFormat.setMarkerStyle(ChartFormat.MarkerNone); // ChartFormat.MarkerNone
        }
    }

    private void applySeriesLabels(Series series, ChartFormat seriesFormat)
        throws Exception
    {
        if (series.getLabel().isVisible())
        {
            seriesFormat.setDataLabelType(ChartFormat.DataLabelValue);

            Position position = series.getLabelPosition();

            switch (position)
            {
                case LEFT_LITERAL:
                    seriesFormat
                        .setDataLabelPosition(ChartFormat.DataLabelPositionLeft);
                    break;

                case ABOVE_LITERAL:
                    seriesFormat
                        .setDataLabelPosition(ChartFormat.DataLabelPositionAbove);
                    break;

                case RIGHT_LITERAL:
                    seriesFormat
                        .setDataLabelPosition(ChartFormat.DataLabelPositionRight);
                    break;

                case BELOW_LITERAL:
                    seriesFormat
                        .setDataLabelPosition(ChartFormat.DataLabelPositionBelow);
                    break;

                case INSIDE_LITERAL:
                    seriesFormat
                        .setDataLabelPosition(ChartFormat.DataLabelPositionCenter);
                    break;

                default:
                    seriesFormat
                        .setDataLabelPosition(ChartFormat.DataLabelPositionAuto);
            }

            applyTextFormat(series.getLabel().getCaption(), seriesFormat);
        }
    }

    private static short seriesType(Series s)
    {
        if (s instanceof BarSeries)
            return ChartShape.Column;

        if (s instanceof AreaSeries)
            return ChartShape.Area;

        if (s instanceof ScatterSeries)
            return ChartShape.Scatter;

        if (s instanceof LineSeries)
            return ChartShape.Line;

        throw new IllegalStateException("Not supported "
            + s.getClass().getName());
    }

    protected void exportImage(IImageArea image, Sheet modelSheet, XlsCell cell)
        throws Exception
    {
        PictureShape shape = loadPicture(image, modelSheet, cell);

        if (shape != null)
        {
            ShapeFormat format = shape.getFormat();
            format.setPlacementStyle(ShapeFormat.PlacementMove);
            shape.setFormat();
        }
        else if (!suppressUnknownImage)
        {
            workbook.setText(cell.y - rowShift, cell.x - columnShift,
                "<<Unsupported Image>>"); //$NON-NLS-1$
        }
    }

    protected void exportText(ITextArea text, XlsCell cell)
    {
        exportText(text.getText(), cell);
    }

    protected void exportText(String csCellText, XlsCell cell)
    {
        exportText(csCellText, cell, false);
    }

    protected void exportText(String csCellText, XlsCell cell, boolean isPercent)
    {
        try
        {
            if (csCellText != null && !csCellText.isEmpty())
            {
                Double csNumberValue;
                try
                {
                    csNumberValue = Double.parseDouble(csCellText.replace(',',
                        '.'));
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

                if (csNumberValue != null && !csNumberValue.isNaN()
                    && (bigValue == null || bigValue.precision() < 20))
                {
                    workbook.setNumber(cell.sheet, cell.y - rowShift, cell.x
                        - columnShift, csNumberValue.doubleValue()
                        / (isPercent ? 100 : 1));

                    RangeStyle rangeStyle = workbook.getRangeStyle(cell.y
                        - rowShift, cell.x - columnShift, cell.y - rowShift,
                        cell.x - columnShift);
                    rangeStyle.setCustomFormat(getNumberFormat(csNumberValue)
                        + (isPercent ? "%" : ""));
                    workbook.setRangeStyle(rangeStyle, cell.y - rowShift,
                        cell.x - columnShift, cell.y - rowShift, cell.x
                            - columnShift);
                }
                else
                {
                    if (isPercent(csCellText))
                    {
                        String trimValue = csCellText.trim();
                        exportText(
                            trimValue.substring(0, trimValue.length() - 1),
                            cell, true);
                    }
                    else
                    {
                        workbook.setText(cell.sheet, cell.y - rowShift, cell.x
                            - columnShift, csCellText);
                    }
                }
            }
        }
        catch (Exception e)
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(stream));

            try
            {
                workbook.setText(cell.sheet, cell.y - rowShift, cell.x
                    - columnShift, stream.toString());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
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
                    workbook.addHyperlink(cell.y - rowShift, cell.x
                        - columnShift, cell.y2 - rowShift, cell.x2
                        - columnShift, link, HyperLink.kURLAbs, tooltip);

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
                        workbook.addComment(cell.y - rowShift, cell.x
                            - columnShift, comments, null);

                        return true;
                    }
                }
            }
        }

        return false;
    }

    protected PictureShape loadPicture(IImageArea imageArea, Sheet modelSheet,
        XlsCell cell) throws Exception
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

            double cellWidth = 0;
            double cellHeight = 0;
            for (int i = cell.x; i <= cell.x2; i++)
            {
                cellWidth += modelSheet.getColumnWidth(i);
            }
            for (int i = cell.y; i <= cell.y2; i++)
            {
                cellHeight += modelSheet.getRowHeight(i);
            }

            double x = cell.x - columnShift;
            double y = cell.y - rowShift;
            if (imageArea instanceof ImageArea)
            {
                x += ((ImageArea) imageArea).getParent().getX() / cellWidth;
                y += ((ImageArea) imageArea).getParent().getY() / cellHeight;
            }

            double width = imageArea.getWidth() / cellWidth;
            double height = imageArea.getHeight() / cellHeight;

            PictureShape picture = workbook.addPicture(x, y, x + width, y
                + height, data);

            return picture;
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
                exportText(text, itr.next());
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

    private static Object getFieldAsPublic(Object obj, Class<?> clazz,
        String field) throws SecurityException, NoSuchFieldException,
        IllegalArgumentException, IllegalAccessException
    {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);

        return f.get(obj);
    }

    @SuppressWarnings("rawtypes")
    private String getChartDataSource(GeneratedChartState generatedChartState)
    {
        IDisplayServer _ids = generatedChartState.getDisplayServer();

        try
        {
            Object resourceFinder = getFieldAsPublic(_ids,
                DisplayAdapter.class, "resourceFinder");
            Object handle = getFieldAsPublic(resourceFinder,
                ChartReportItemImpl.class, "handle");
            Object element = getFieldAsPublic(handle,
                ReportElementHandle.class, "element");
            HashMap propValues = (HashMap) getFieldAsPublic(element,
                DesignElement.class, "propValues");

            if (propValues.containsKey(DATA_BINDING_REF))
            {
                return propValues.get(DATA_BINDING_REF).toString();
            }
            // else if (propValues.containsKey("dataSet"))
            // {
            // return propValues.get("dataSet").toString();
            // }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            return null;
        }

        return null;
    }

    private Coordinate getDataCoord(WorkBook workbook, int sheet,
        Coordinate tableCoord, Object[] values) throws Exception
    {
        for (int row = tableCoord.getY1(); row <= tableCoord.getY2(); row++)
        {
            for (int col = tableCoord.getX1(); col <= tableCoord.getX2(); col++)
            {
                if (equals(values[0],
                    workbook.getText(sheet, row - rowShift, col - columnShift)))
                {
                    int k = 1;

                    while (k < values.length
                        && equals(
                            values[k],
                            workbook.getText(sheet, row + k - rowShift, col
                                - columnShift)))
                    {
                        k++;
                    }

                    if (k == values.length)
                    {
                        return new Coordinate(col, row, col, row + k - 1);
                    }
                }
            }
        }

        return null;
    }

    private static boolean equals(Object o1, Object o2)
    {
        return ((o1 != null) && (o2 != null) && (o1.equals(o2)))
            || ((o1 == null) && (o2 == null));
    }

    /**
     * @param workbook
     * @param cell
     * @param generatedChartState
     * @return
     * @throws ChartException
     * @throws IllegalArgumentException
     */
    private boolean isChartNeedExtraDataSheet(WorkBook workbook, XlsCell cell,
        GeneratedChartState generatedChartState)
        throws IllegalArgumentException, ChartException
    {
        Chart chartModel = generatedChartState.getChartModel();
        ChartWithAxes chartWithAxes = (ChartWithAxes) chartModel;

        Axis[] baseAxes = chartWithAxes.getBaseAxes();
        Axis xAxis = baseAxes[0];
        Axis[] yAxes = chartWithAxes.getOrthogonalAxes(xAxis, true);

        ArrayList<Axis> allAxes = new ArrayList<Axis>();
        allAxes.add(xAxis);
        allAxes.addAll(Arrays.asList(yAxes));

        for (Axis axis : allAxes)
        {
            FormatSpecifier axisFormatSpec = axis.getFormatSpecifier();
            EList<SeriesDefinition> seriesDefinitions = axis
                .getSeriesDefinitions();

            for (SeriesDefinition seriesDefinition : seriesDefinitions)
            {
                for (Series series : seriesDefinition.getRunTimeSeries())
                {
                    DataSet dataSet = series.getDataSet();
                    DataSetIterator it = new DataSetIterator(dataSet);

                    Object cachedFormat = null;

                    if (it.getDataType() == IConstants.DATE_TIME)
                    {
                        int dateUnit = ChartUtil.computeDateTimeCategoryUnit(
                            chartModel, it);

                        cachedFormat = cacheDateFormat.get(dateUnit);
                    }

                    Object[] values = (Object[]) dataSet.getValues();

                    String chartDataSource = getChartDataSource(generatedChartState);

                    if (chartDataSource != null)
                    {
                        Sheet sheet = modelSheets.get(cell.sheet);

                        Coordinate tableCoord = sheet
                            .getTableCoord(chartDataSource);

                        if (tableCoord != null)
                        {
                            try
                            {
                                Object[] formattedValues = new Object[values.length];

                                for (int i = 0; i < values.length; i++)
                                {
                                    formattedValues[i] = convertChartValue(
                                        workbook, 0, 0, values[i],
                                        it.getDataType(), axisFormatSpec,
                                        cachedFormat);
                                }

                                Coordinate dataCoord = getDataCoord(workbook,
                                    cell.sheet, tableCoord, formattedValues);

                                if (dataCoord == null)
                                {
                                    return true;
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                return true;
                            }
                        }
                        else
                        {
                            return true;
                        }
                    }
                    else
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private String getChartSeriesFormula(String sheetName, int row1, int col1,
        int row2, int col2) throws Exception
    {
        return sheetName + "!" + workbook.formatRCNr(row1, col1, true) + ":"
            + workbook.formatRCNr(row2, col2, true);
    }

    private String convertChartValue(WorkBook workbook, int row, int col,
        Object val, int valType, FormatSpecifier formatSpec,
        Object oCachedJavaFormatter) throws Exception
    {
        String oldValue = workbook.getText(0, 0);

        writeChartValue(workbook, 0 + rowShift, 0 + columnShift, val, valType,
            formatSpec, oCachedJavaFormatter);

        String result = workbook.getText(0, 0);
        workbook.setText(0, 0, oldValue);

        return result;
    }

    private boolean isGridVisible(Grid grid)
    {
        return grid.getLineAttributes().isSetVisible()
            && grid.getLineAttributes().isVisible();
    }

    private String getPageTitle(PageArea pageArea)
    {
        List<String> sheetNames = new ArrayList<String>();
        findSheetNames(pageArea.getBody(), sheetNames);

        return sheetNames.isEmpty() ? null : sheetNames
            .get(sheetNames.size() - 1);
    }

    private void findSheetNames(IContainerArea container,
        List<String> sheetNames)
    {
        boolean useOnlyToc = false;
        String sheetName = null;

        if (container instanceof ContainerArea)
        {
            IContent content = ((ContainerArea) container).getContent();
            if (content instanceof IListContent
                || content instanceof ITableContent)
            {
                if (!useOnlyToc)
                {
                    sheetName = content.getName();
                }
                if (sheetName == null && content.getTOC() != null)
                {
                    sheetName = content.getTOC().toString();
                    useOnlyToc = true;
                }
            }
            else if (content instanceof IListGroupContent
                || content instanceof ITableGroupContent)
            {
                Object groupDesignObject = content.getGenerateBy();
                if (groupDesignObject instanceof GroupDesign)
                {
                    GroupDesign groupDesign = (GroupDesign) groupDesignObject;
                    if (DesignChoiceConstants.PAGE_BREAK_BEFORE_ALWAYS
                        .equals(groupDesign.getPageBreakBefore())
                        || DesignChoiceConstants.PAGE_BREAK_BEFORE_ALWAYS_EXCLUDING_FIRST
                            .equals(groupDesign.getPageBreakBefore())
                        || DesignChoiceConstants.PAGE_BREAK_AFTER_ALWAYS
                            .equals(groupDesign.getPageBreakAfter())
                        || DesignChoiceConstants.PAGE_BREAK_AFTER_ALWAYS_EXCLUDING_LAST
                            .equals(groupDesign.getPageBreakAfter()))
                    {
                        if (content.getTOC() != null)
                        {
                            sheetName = content.getTOC().toString();
                            useOnlyToc = true;
                        }
                    }
                }
            }

            if (sheetName != null)
            {
                sheetNames.add(sheetName);
            }
        }

        if (container != null)
        {
            Iterator<IArea> children = container.getChildren();

            while (children.hasNext())
            {
                IArea area = children.next();

                if (area instanceof IContainerArea)
                {
                    findSheetNames((IContainerArea) area, sheetNames);
                }
            }
        }
    }

    private String getNumberFormat(double d)
    {
        StringBuilder buffer = new StringBuilder("#,##0");

        int decimalLength = getDecimalLength(d);

        for (int i = 0; i < Math.min(decimalLength, MAX_DECIMAL_LENGTH); i++)
        {
            if (i == 0)
            {
                buffer.append(".0");
            }
            else
            {
                buffer.append("#");
            }
        }

        return buffer.toString();
    }

    private int getDecimalLength(double d)
    {
        String decimal = String.valueOf(d).split("\\.")[1];

        if ("0".equals(decimal))
        {
            return 0;
        }
        else
        {
            return decimal.length();
        }
    }

    private static boolean isPercent(String value)
    {
        if (value != null && !value.trim().equals(""))
        {
            String trimValue = value.trim();
            int index = trimValue.indexOf('%');

            if (index == trimValue.length() - 1)
            {
                return convert2Double(trimValue.substring(0, index)) != null;
            }
        }

        return false;
    }

    private static Double convert2Double(String value)
    {
        Double csNumberValue = null;

        try
        {
            csNumberValue = Double.parseDouble(value.replace(',', '.'));
        }
        catch (NumberFormatException e)
        {
            csNumberValue = null;
        }

        return csNumberValue;
    }
}