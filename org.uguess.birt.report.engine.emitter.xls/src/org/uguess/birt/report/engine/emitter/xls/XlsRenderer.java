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

import java.io.*;
import java.net.URL;
import java.util.*;

import com.smartxls.*;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.parser.*;
import org.eclipse.birt.report.engine.api.IHTMLActionHandler;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.api.impl.Action;
import org.eclipse.birt.report.engine.content.*;
import org.eclipse.birt.report.engine.content.impl.PageContent;
import org.eclipse.birt.report.engine.emitter.IEmitterServices;
import org.eclipse.birt.report.engine.ir.ReportElementDesign;
import org.eclipse.birt.report.engine.nLayout.area.*;
import org.eclipse.birt.report.engine.nLayout.area.impl.PageArea;
import org.eclipse.birt.report.model.api.ReportElementHandle;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
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
		DEBUG = System.getProperty( "debug" ) != null; //$NON-NLS-1$
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

	public void initialize( IEmitterServices services )
	{
		// init options.
		rangeChecker = new PageRangeChecker( null );
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
		Object option = services.getEmitterConfig( ).get( XLS_IDENTIFIER );
		if ( option instanceof Map )
		{
			parseRendererOptions( (Map) option );
		}
		else if ( option instanceof IRenderOption )
		{
			parseRendererOptions( ( (IRenderOption) option ).getOptions( ) );
		}

		// parse rendering options, this will overwrite the option by emitter
		// per rendering task.
		parseRendererOptions( services.getRenderOption( ).getOptions( ) );

		Object fd = services.getOption( IRenderOption.OUTPUT_FILE_NAME );
		File file = null;
		try
		{
			if ( fd != null )
			{
				file = new File( fd.toString( ) );

				File parent = file.getParentFile( );
				if ( parent != null && !parent.exists( ) )
				{
					parent.mkdirs( );
				}

				output = new FileOutputStream( file );
			}
		}
		catch ( FileNotFoundException fnfe )
		{
			fnfe.printStackTrace( );
		}

		if ( output == null )
		{
			Object value = services.getOption( IRenderOption.OUTPUT_STREAM );
			if ( value != null && value instanceof OutputStream )
			{
				output = (OutputStream) value;
			}

			else
			{
				try
				{
					file = new File( DEFAULT_FILE_NAME );
					output = new FileOutputStream( file );
				}
				catch ( FileNotFoundException e )
				{
					e.printStackTrace( );
				}
			}
		}
	}

	protected void parseRendererOptions( Map xlsOption )
	{
		if ( xlsOption == null )
		{
			return;
		}

		Object value;

		value = xlsOption.get( "dpi" ); //$NON-NLS-1$
		if ( value != null )
		{
			int pdpi = OptionParser.parseInt( value );

			if ( pdpi != -1 )
			{
				dpi = pdpi;
			}
		}

		value = xlsOption.get( XlsEmitterConfig.KEY_PAGE_RANGE );
		if ( value != null )
		{
			rangeChecker = new PageRangeChecker( OptionParser.parseString( value ) );
		}

		value = xlsOption.get( XlsEmitterConfig.KEY_REMOVE_EMPTY_ROW );
		if ( value != null )
		{
			removeEmptyRow = OptionParser.parseBoolean( value );
		}

		value = xlsOption.get( XlsEmitterConfig.KEY_SHOW_GRID_LINES );
		if ( value != null )
		{
			showGridLines = OptionParser.parseBoolean( value );
		}

		value = xlsOption.get( XlsEmitterConfig.KEY_FIXED_COLUMN_WIDTH );
		if ( value != null )
		{
			fixedColumnWidth = OptionParser.parseInt( value );
		}

		value = xlsOption.get( XlsEmitterConfig.KEY_EXPORT_BODY_ONLY );
		if ( value != null )
		{
			exportBodyOnly = OptionParser.parseBoolean( value );
		}

		value = xlsOption.get( XlsEmitterConfig.KEY_EXPORT_SINGLE_SHEET );
		if ( value != null )
		{
			exportSingleSheet = OptionParser.parseBoolean( value );
		}

		value = xlsOption.get( XlsEmitterConfig.KEY_SUPPRESS_UNKNOWN_IMAGE );
		if ( value != null )
		{
			suppressUnknownImage = OptionParser.parseBoolean( value );
		}

		value = xlsOption.get( XlsEmitterConfig.KEY_ENABLE_COMMENTS );
		if ( value != null )
		{
			enableComments = OptionParser.parseBoolean( value );
		}

		value = xlsOption.get( XlsEmitterConfig.KEY_SHEET_NAME_EXPR );
		if ( value != null )
		{
			sheetNameExpr = parseSheetNameExpression( OptionParser.parseString( value ) );
		}

		value = xlsOption.get( IRenderOption.CLOSE_OUTPUTSTREAM_ON_EXIT );
		if ( value != null )
		{
			closeStream = OptionParser.parseBoolean( value );
		}
	}

	private Expression parseSheetNameExpression( String expr )
	{
		Expression expression = null;

		if ( expr != null )
		{
			try
			{
				expr = expr.trim( );
				if ( !expr.endsWith( ";" ) ) //$NON-NLS-1$
				{
					expr += ";"; //$NON-NLS-1$
				}

				Parser parser = new Parser( new StringReader( ";" ) ); //$NON-NLS-1$

				SimpleNode tree = parser.parse( new StringReader( expr ) );

				SimpleNode node = (SimpleNode) tree.jjtGetChild( 0 );

				// pre-scan the expression tree to remove loop structure
				node.jjtAccept( new ParserVisitor( ) {

					public Object visit( ASTReference node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTSizeMethod node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTArrayAccess node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTMethod node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTForeachStatement node, Object data )
					{
						throw new UnsupportedOperationException( "Loop function is not allowed." ); //$NON-NLS-1$
					}

					public Object visit( ASTWhileStatement node, Object data )
					{
						throw new UnsupportedOperationException( "Loop function is not allowed." ); //$NON-NLS-1$
					}

					public Object visit( ASTIfStatement node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTReferenceExpression node,
							Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTStatementExpression node,
							Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTExpressionExpression node,
							Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTStringLiteral node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTFloatLiteral node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTIntegerLiteral node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTFalseNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTTrueNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTNullLiteral node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTNotNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTBitwiseComplNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTUnaryMinusNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTModNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTDivNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTMulNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTSubtractNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTAddNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTGENode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTLENode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTGTNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTLTNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTNENode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTEQNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTBitwiseAndNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTBitwiseXorNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTBitwiseOrNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTAndNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTOrNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTAssignment node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTExpression node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTIdentifier node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTSizeFunction node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTEmptyFunction node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTBlock node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( ASTJexlScript node, Object data )
					{
						return node.childrenAccept( this, data );
					}

					public Object visit( SimpleNode node, Object data )
					{
						return node.childrenAccept( this, data );
					}
				},
						null );

				expression = ExpressionFactory.createExpression( expr );
			}
			catch ( Exception e )
			{
				expression = null;

				e.printStackTrace( );
			}
		}

		return expression;
	}

	public void start( IReportContent rc )
	{
		if ( DEBUG )
		{
			timeCounter = System.currentTimeMillis( );
		}

		this.frameStack = new Stack<Frame>( );

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

		processor = new XlsStyleProcessor( workbook );
	}

	public void end( IReportContent rc )
	{
		if ( exportSingleSheet && singleContainer != null )
		{
			long span = System.currentTimeMillis( );

			// process single sheet here.
			Sheet modelSheet = Transformer.toSheet( singleContainer,
					1000,
					fixedColumnWidth * 1000 );
			exportSheet( modelSheet,
					Boolean.TRUE == singleContainer.getAttribute( ATTR_LANDSCAPE ) );

			if ( DEBUG )
			{
				System.out.println( "------------export total single sheet using " //$NON-NLS-1$
						+ ( System.currentTimeMillis( ) - span )
						+ " ms" ); //$NON-NLS-1$
			}
		}

		try
		{
			workbook.setSheet(0); // activate first sheet in report
			
			workbook.write( output );

			if ( closeStream )
			{
				output.close( );
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace( );
		}

		frameStack.clear( );

		processor.dispose( );
		processor = null;

		workbook = null;
		frameStack = null;
		singleContainer = null;

		if ( DEBUG )
		{
			System.out.println( "------------total exporting time using: " //$NON-NLS-1$
					+ ( System.currentTimeMillis( ) - timeCounter )
					+ " ms" ); //$NON-NLS-1$
		}
	}

	public void startContainer( IContainerArea container )
	{
		buildFrame( container, true );
	}

	public void endContainer( IContainerArea containerArea )
	{
		Frame currentFrame = frameStack.pop( );

		if ( currentFrame.getData( ) instanceof PageArea )
		{
			exportFrame( currentFrame );
		}
	}

	protected void exportFrame( Frame currentFrame )
	{
		long span = System.currentTimeMillis( );

		if ( rangeChecker.checkRange( currentPageIndex ) )
		{
			PageArea pa = (PageArea) getTranslatedElementValue( currentFrame.getData( ) );
			IPageContent content = (PageContent) pa.getContent( );
			boolean landscape = DesignChoiceConstants.PAGE_ORIENTATION_LANDSCAPE.equals( content.getOrientation( ) );

			if ( exportBodyOnly )
			{
				IContainerArea body = pa.getBody( );

				// locate body frame first
				Frame bodyFrame = locateChildFrame( currentFrame, body );

				if ( bodyFrame instanceof AreaFrame )
				{
					// adjust offsets
					( (AreaFrame) bodyFrame ).setXOffset( -bodyFrame.getLeft( ) );
					( (AreaFrame) bodyFrame ).setYOffset( -bodyFrame.getTop( ) );
				}

				if ( exportSingleSheet )
				{
					// aggregate only
					if ( singleContainer == null )
					{
						singleContainer = new AggregateFrame( );
						singleContainer.setRight( bodyFrame.getRight( )
								- bodyFrame.getLeft( ) );
						singleContainer.setBottom( bodyFrame.getBottom( )
								- bodyFrame.getTop( ) );

						if ( landscape )
						{
							singleContainer.putAttribute( ATTR_LANDSCAPE,
									Boolean.TRUE );
						}
					}
					else
					{
						int yOff = singleContainer.getBottom( );
						singleContainer.setBottom( yOff
								+ bodyFrame.getBottom( )
								- bodyFrame.getTop( ) );

						if ( bodyFrame instanceof AreaFrame )
						{
							( (AreaFrame) bodyFrame ).setYOffset( yOff
									+ ( (AreaFrame) bodyFrame ).getYOffset( ) );
						}
					}

					singleContainer.addChild( bodyFrame );
					if ( bodyFrame instanceof AreaFrame )
					{
						( (AreaFrame) bodyFrame ).setParent( singleContainer );
					}

					if ( DEBUG )
					{
						System.out.println( "------------aggregate sheet[" //$NON-NLS-1$
								+ ( currentPageIndex )
								+ "] using " //$NON-NLS-1$
								+ ( System.currentTimeMillis( ) - span )
								+ " ms" ); //$NON-NLS-1$
					}
				}
				else
				{
					// export body frame
					Sheet modelSheet = Transformer.toSheet( bodyFrame,
							1000,
							fixedColumnWidth * 1000 );
					exportSheet( modelSheet, landscape );

					if ( DEBUG )
					{
						System.out.println( "------------export sheet[" //$NON-NLS-1$
								+ ( currentPageIndex )
								+ "] using " //$NON-NLS-1$
								+ ( System.currentTimeMillis( ) - span )
								+ " ms" ); //$NON-NLS-1$
					}
				}
			}
			else
			{
				if ( exportSingleSheet )
				{
					// aggregate only
					if ( singleContainer == null )
					{
						singleContainer = new AggregateFrame( );
						singleContainer.setRight( currentFrame.getRight( )
								- currentFrame.getLeft( ) );
						singleContainer.setBottom( currentFrame.getBottom( )
								- currentFrame.getTop( ) );

						if ( landscape )
						{
							singleContainer.putAttribute( ATTR_LANDSCAPE,
									Boolean.TRUE );
						}
					}
					else
					{
						int yOff = singleContainer.getBottom( );
						singleContainer.setBottom( yOff
								+ currentFrame.getBottom( )
								- currentFrame.getTop( ) );

						if ( currentFrame instanceof AreaFrame )
						{
							( (AreaFrame) currentFrame ).setYOffset( yOff );
						}
					}

					singleContainer.addChild( currentFrame );
					if ( currentFrame instanceof AreaFrame )
					{
						( (AreaFrame) currentFrame ).setParent( singleContainer );
					}

					if ( DEBUG )
					{
						System.out.println( "------------aggregate sheet[" //$NON-NLS-1$
								+ ( currentPageIndex )
								+ "] using " //$NON-NLS-1$
								+ ( System.currentTimeMillis( ) - span )
								+ " ms" ); //$NON-NLS-1$
					}
				}
				else
				{
					// one page completed, process it.
					Sheet modelSheet = Transformer.toSheet( currentFrame,
							1000,
							fixedColumnWidth * 1000 );
					exportSheet( modelSheet, landscape );

					if ( DEBUG )
					{
						System.out.println( "------------export sheet[" //$NON-NLS-1$
								+ ( currentPageIndex )
								+ "] using " //$NON-NLS-1$
								+ ( System.currentTimeMillis( ) - span )
								+ " ms" ); //$NON-NLS-1$
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
	protected Frame locateChildFrame( Frame parentFrame, Object data )
	{
		for ( Iterator<Frame> itr = parentFrame.iterator( ); itr.hasNext( ); )
		{
			Frame fr = itr.next( );

			if ( fr.getData( ) == data )
			{
				return fr;
			}

			Frame cfr = locateChildFrame( fr, data );
			if ( cfr != null )
			{
				return cfr;
			}
		}

		return null;
	}

	protected boolean isEffectiveCell( Cell cell )
	{
		if ( cell.isEmpty( ) )
		{
			return false;
		}

		return true;
	}

	private String getSheetName( )
	{
		if ( sheetNameExpr != null )
		{
			try
			{
				JexlContext context = JexlHelper.createContext( );

				context.getVars( ).put( "pageIndex", currentPageIndex ); //$NON-NLS-1$
				context.getVars( ).put( "sheetIndex", //$NON-NLS-1$
						workbook.getNumSheets( ) );
				context.getVars( )
						.put( "rptContext", //$NON-NLS-1$
								new ManagedReportContext( services.getReportContext( ) ) );

				Object result = sheetNameExpr.evaluate( context );

				if ( result != null )
				{
					return result.toString( );
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace( );
			}
		}

		// default sheet name
		return "Sheet" + workbook.getNumSheets( ); //$NON-NLS-1$
	}

	final protected void exportSheet( Sheet modelSheet, boolean landscape )
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
	
	private void doExportSheet( Sheet modelSheet, boolean landscape ) throws Exception 
	{
		if (++sheetNum > 0)
		{		
			workbook.insertSheets(sheetNum, 1);
			workbook.setSheet(sheetNum);
		}
		
		workbook.setShowGridLines( showGridLines );

		if ( landscape )
		{
			workbook.setPrintLandscape( true );
		}

		try
		{
			workbook.setSheetName( workbook.getNumSheets( ) - 1,
					getSheetName( ) );
		}
		catch ( Exception e )
		{
			// if sheet name doesn't confrom to Excel's convention, ignore and
			// continue.
			e.printStackTrace( );
		}

		short columnCount = (short) ( Math.min( modelSheet.getActiveColumnRange( ),
				254 ) + 1 );
		int rowCount = modelSheet.getActiveRowRange( ) + 1;

		if ( columnCount + COMMENTS_WIDTH_IN_COLUMN < 255 )
		{
			commentsAnchorColumnIndex = columnCount;
		}
		else
		{
			commentsAnchorColumnIndex = 255 - COMMENTS_WIDTH_IN_COLUMN;
		}

		if ( rowCount + COMMENTS_HEIGHT_IN_ROW < 65535 )
		{
			commentsAnchorRowIndex = rowCount;
		}
		else
		{
			commentsAnchorRowIndex = 65535 - COMMENTS_HEIGHT_IN_ROW;
		}

		boolean[] nonBlankRow = new boolean[rowCount];

		if ( removeEmptyRow )
		{
			// check blank rows.
			for ( int i = 0; i < rowCount; i++ )
			{
				for ( int j = 0; j < columnCount; j++ )
				{
					Cell cell = modelSheet.getCell( i, j, false );

					if ( cell != null && isEffectiveCell( cell ) )
					{
						nonBlankRow[i] = true;
						break;
					}
				}
			}
		}

		double width = 0;
		for ( short i = 1; i < columnCount + 1; i++ )
		{
			width = modelSheet.getColumnWidth( i - 1 )
					/ ( 1000 * baseCharWidth );

			workbook.setColWidth( i - 1 , (short) ( width * 256 ) );
		}

		RangeStyle emptyCellStyle = processor.getEmptyCellStyle( false );
		
		for ( short y = 0; y < rowCount; y++ )
		{
			if ( !removeEmptyRow || nonBlankRow[y] )
			{
				double height = modelSheet.getRowHeight( y ) / 1000d;// + 2;

				// System.out.println( "row height " + y + ": " + height );

				height *= 20; // s.vladykin: magic empirical coefficient
				
				workbook.setRowHeight( y, (int) height );

				top: for ( short x = 0; x < columnCount; x++ )
				{
					System.out.println("set empty " + y + " " + x);
					
					try 
					{
						workbook.setRangeStyle( emptyCellStyle, y, x, y, x );
					} 
					catch(Exception e) 
					{
						e.printStackTrace();
						
						continue;
					}
				}
			}
			else
			{
				workbook.setRowHeight( y, 0 );
				for ( short x = 0; x < columnCount; x++ )
				{
					System.out.println("empty " + y + " " + x);
					
					workbook.setRangeStyle( emptyCellStyle, y, x, y, x );
				}
			}
		}
		
		List<MergeBlock> merged = new ArrayList<MergeBlock>();
		
		for ( short y = 0; y < rowCount; y++ )
		{
			if ( !removeEmptyRow || nonBlankRow[y] )
			{
				top: for ( short x = 0; x < columnCount; x++ )
				{
					Cell element = modelSheet.getCell( y, x, false );
					if ( element != null )
					{
						for (MergeBlock m : merged) 
						{
							if ( m.include( y, x ) ) 
							{
								continue top;
							}
						}
			
						MergeBlock mb = exportCell( element,
								x,
								y,
								modelSheet );
						
						if ( mb != null ) 
						{
							merged.add( mb );
						}
					}
				}
			}
		}
	}
	
	protected MergeBlock exportCell( Cell element, short x, short y, Sheet modelSheet ) throws Exception
	{
		if ( element.isEmpty( ) )
		{
			return null;
		}

		short x2 = x;
		short y2 = y;

		MergeBlock mb = null;
		
		if ( element.isMerged( ) )
		{
			Iterator<MergeBlock> it = modelSheet.mergesIterator( );
			while ( it.hasNext( ) )
			{
				MergeBlock merge = it.next( );
				if ( merge.include( y, x ) )
				{
					if ( x == merge.getStartColumn( )
							&& y == merge.getStartRow( ) )
					{
						x2 = (short) ( x + merge.getColumnSpan( ) );
						y2 = (short) ( y + merge.getRowSpan( ) );
						
						if ( x != x2 || y != y2 )
							mb = merge;

						break;
					}
				}
			}
		}

		Object cellValue = getTranslatedElementValue( element.getValue( ) );
		boolean useHyperLinkStyle = false;

		XlsCell cell = new XlsCell(workbook.getSheet(), x, y, x2, y2 );
		
		if ( cellValue instanceof ITextArea )
		{
			useHyperLinkStyle = handleHyperLink( (IArea) cellValue, cell );
		}

		RangeStyle cellStyle = processor.getCellStyle( element.getStyle( ),
				useHyperLinkStyle, mb != null);
		
		if (mb != null)
			System.out.println("exp " + y + " " + x + " - " + y2 + " " + x2);
		else 
			System.out.println("exp " + y + " " + x );
			
		workbook.setRangeStyle( cellStyle, y, x, y2, x2 );

		exportCellData( element, cell );
		
		return mb;
	}

	protected Object getTranslatedElementValue( Object value )
	{
		return value;
	}

	protected void exportCellData( Cell element, XlsCell cell) throws Exception
	{
		if ( isTotalPageArea( element.getValue( ) ) )
		{
			if ( totalCells == null )
			{
				totalCells = new HashSet<XlsCell>( );
			}
			totalCells.add( cell );
		}

		Object cellValue = getTranslatedElementValue( element.getValue( ) );

		if ( cellValue instanceof IImageArea )
		{
			exportImage( (IImageArea) cellValue, cell );
		}
		else if ( cellValue instanceof ITextArea )
		{
			exportText( (ITextArea) cellValue, cell );
		}

		if ( enableComments && cellValue instanceof IArea )
		{
			handleComments( (IArea) cellValue, cell );
		}
	}

	protected void exportImage( IImageArea image, XlsCell cell) throws Exception
	{
		PictureShape shape = loadPicture(image, cell);

		if ( shape != null )
		{
			ShapeFormat format = shape.getFormat();
			format.setPlacementStyle(ShapeFormat.PlacementMove);
			shape.setFormat();
		}
		else if ( !suppressUnknownImage )
		{
			workbook.setText( cell.y, cell.x, "<<Unsupported Image>>" ); //$NON-NLS-1$
		}
	}

	protected void exportText( ITextArea text, XlsCell cell ) throws Exception
	{
		exportText( text.getText( ), cell );
	}

	protected void exportText( String csCellText, XlsCell cell ) throws Exception
	{
		Double csNumberValue = null;

		try
		{
			csNumberValue = Double.parseDouble( csCellText );
		}
		catch ( NumberFormatException e )
		{
			csNumberValue = null;
		}

		if ( csNumberValue == null || csNumberValue.isNaN( ) )
		{
			workbook.setText(cell.sheet, cell.y, cell.x, csCellText );
		}
		else
		{
			workbook.setNumber(cell.sheet, cell.y, cell.x,  csNumberValue );
		}
	}

	protected boolean handleHyperLink( IArea area, XlsCell cell )
	{
		IHyperlinkAction hlAction = area.getAction( );

		if ( hlAction != null )
		{
			try
			{
				IReportRunnable runnable = services.getReportRunnable( );
				String systemId = runnable == null ? null
						: runnable.getReportName( );

				Action act = new Action( systemId, hlAction );

				String link = null;
				String tooltip = EngineUtil.getActionTooltip( hlAction );

				Object ac = services.getOption( RenderOption.ACTION_HANDLER );

				if ( ac instanceof IHTMLActionHandler )
				{
					link = ( (IHTMLActionHandler) ac ).getURL( act,
							services.getReportContext( ) );
				}
				else
				{
					link = hlAction.getHyperlink( );
				}

				if ( link != null )
				{
					workbook.addHyperlink(cell.y, cell.x, cell.y2, cell.x2, link, HyperLink.kURLAbs, tooltip);

					return true;
				}
			}
			catch ( Exception e )
			{
				System.out.println( "create hyperlink failed." ); //$NON-NLS-1$

				e.printStackTrace( );
			}
		}

		return false;
	}

	protected boolean handleComments( IArea area, XlsCell cell ) throws Exception
	{
		IContent content = contentCache.get( area );

		if ( content != null )
		{
			Object sourceObj = content.getGenerateBy( );

			if ( sourceObj instanceof ReportElementDesign )
			{
				sourceObj = ( (ReportElementDesign) sourceObj ).getHandle( );

				if ( sourceObj instanceof ReportElementHandle )
				{
					String comments = ( (ReportElementHandle) sourceObj ).getComments( );

					if ( comments != null && comments.length( ) > 0 )
					{						
						workbook.addComment(cell.y, cell.x, comments, null);

						return true;
					}
				}
			}
		}

		return false;
	}

	protected PictureShape loadPicture( IImageArea imageArea, XlsCell cell ) throws Exception
	{
		byte[] data = imageArea.getImageData( );

		if ( data == null )
		{
			String url = imageArea.getImageUrl( );

			data = loadPictureData( url );
		}

		if ( data != null )
		{
			int type = processor.getPictureType( data );
			if ( type == -1 )
			{
				try
				{
					// try convert to png format
					data = ImageUtil.convertImage( data, "png" ); //$NON-NLS-1$
				}
				catch ( IOException e )
				{
					return null;
				}
			}
			
			return workbook.addPicture( cell.x, cell.y, cell.x2 + 1, cell.y2 + 1, data );
		}

		return null;
	}

	private byte[] loadPictureData( String url )
	{
		if ( url != null )
		{
			BufferedInputStream bis = null;
			try
			{
				bis = new BufferedInputStream( new URL( url ).openStream( ) );
				ByteArrayOutputStream bos = new ByteArrayOutputStream( );

				byte[] buf = new byte[1024];
				int count = bis.read( buf );
				while ( count != -1 )
				{
					bos.write( buf, 0, count );

					count = bis.read( buf );
				}

				return bos.toByteArray( );
			}
			catch ( Exception e )
			{
				e.printStackTrace( );
			}
			finally
			{
				if ( bis != null )
				{
					try
					{
						bis.close( );
					}
					catch ( IOException e )
					{
						e.printStackTrace( );
					}
				}
			}
		}
		return null;
	}

	public void visitText( ITextArea textArea )
	{
		buildFrame( textArea, false );
	}

	public void visitImage( IImageArea imageArea )
	{
		buildFrame( imageArea, false );
	}

	public void visitAutoText( ITemplateArea templateArea )
	{
		buildFrame( templateArea, false );

		int type = EngineUtil.getTemplateType( templateArea );

		if ( type == IAutoTextContent.TOTAL_PAGE
				|| type == IAutoTextContent.UNFILTERED_TOTAL_PAGE )
		{
			if ( totalPageAreas == null )
			{
				totalPageAreas = new HashSet<IArea>( );
			}

			totalPageAreas.add( templateArea );
		}
	}

	public void visitContainer( IContainerArea containerArea )
	{
		startContainer( containerArea );

		Iterator iter = containerArea.getChildren( );
		while ( iter.hasNext( ) )
		{
			IArea child = (IArea) iter.next( );
			child.accept( this );
		}

		endContainer( containerArea );
	}

	protected boolean isTotalPageArea( Object element )
	{
		if ( element != null && totalPageAreas != null )
		{
			return totalPageAreas.contains( element );
		}
		return false;
	}

	public void setTotalPage( ITextArea totalPage )
	{
		if ( totalPage != null && totalCells != null )
		{
			String text = totalPage.getText( );

			for ( Iterator<XlsCell> itr = totalCells.iterator( ); itr.hasNext( ); )
			{
				XlsCell cell = itr.next( );

				try 
				{
					exportText( text, cell );
				} 
				catch (Exception e) 
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

	protected void buildFrame( IArea area, boolean isContainer )
	{
		AreaFrame frame = new AreaFrame( area );

		AreaFrame parentFrame = frameStack.isEmpty( ) ? null
				: (AreaFrame) frameStack.peek( );
		frame.setParent( parentFrame );
		if ( parentFrame != null )
		{
			parentFrame.addChild( frame );
		}

		if ( isContainer )
		{
			frameStack.push( frame );
		}

		// System.out.println(frame);
	}
	
	
	/**
	 * Represents cell on xls sheet.
	 */
	private static class XlsCell {

		/**
		 * x, y - top left column and row
		 * x2, y2 - bottom right column and row
		 */
		final int x, y, x2, y2;
		
		/**
		 * Sheet number in workbook
		 */
		final int sheet;
		
		XlsCell(int sheet, int x, int y, int x2, int y2) {
			this.sheet = sheet;
			this.x = x;
			this.y = y;
			this.x2 = x2;
			this.y2 = y2;
		}

		@Override
		public int hashCode() {
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
		public boolean equals(Object obj) {
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