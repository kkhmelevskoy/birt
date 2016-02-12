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

package org.uguess.birt.report.engine.emitter.rtf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.content.IAutoTextContent;
import org.eclipse.birt.report.engine.content.IPageContent;
import org.eclipse.birt.report.engine.content.IReportContent;
import org.eclipse.birt.report.engine.content.impl.PageContent;
import org.eclipse.birt.report.engine.emitter.IEmitterServices;
import org.eclipse.birt.report.engine.nLayout.area.IArea;
import org.eclipse.birt.report.engine.nLayout.area.IAreaVisitor;
import org.eclipse.birt.report.engine.nLayout.area.IContainerArea;
import org.eclipse.birt.report.engine.nLayout.area.IImageArea;
import org.eclipse.birt.report.engine.nLayout.area.ITemplateArea;
import org.eclipse.birt.report.engine.nLayout.area.ITextArea;
import org.eclipse.birt.report.engine.nLayout.area.impl.PageArea;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.uguess.birt.report.engine.layout.wrapper.Frame;
import org.uguess.birt.report.engine.layout.wrapper.impl.AreaFrame;
import org.uguess.birt.report.engine.util.EngineUtil;
import org.uguess.birt.report.engine.util.OptionParser;
import org.uguess.birt.report.engine.util.PageRangeChecker;

/**
 * RtfRenderer
 */
public class RtfRenderer implements IAreaVisitor
{

	public static final String DEFAULT_FILE_NAME = "report.rtf"; //$NON-NLS-1$

	private static final String RTF_IDENTIFIER = "rtf"; //$NON-NLS-1$

	private static final boolean DEBUG;

	static
	{
		DEBUG = System.getProperty( "debug" ) != null; //$NON-NLS-1$
	}

	private boolean exportBodyOnly = false;

	private boolean suppressUnknownImage = false;

	private boolean allowDiskCache = false;

	private int cacheThreshold;

	private int currentPageIndex;

	private PageRangeChecker rangeChecker;

	private boolean closeStream;

	private OutputStream output = null;

	private RtfWriter writer;

	private IEmitterServices services;

	private boolean firstPage;

	private Stack<Frame> frameStack;

	private Set<IArea> totalPageAreas;

	private long timeCounter;

	public String getOutputFormat( )
	{
		return RTF_IDENTIFIER;
	}

	public void initialize( IEmitterServices services )
	{
		// init options
		rangeChecker = new PageRangeChecker( null );
		exportBodyOnly = false;
		allowDiskCache = false;
		cacheThreshold = 1024;
		suppressUnknownImage = false;
		closeStream = false;

		this.services = services;

		// parse emitter options.
		Object option = services.getEmitterConfig( ).get( RTF_IDENTIFIER );

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

	private void parseRendererOptions( Map rtfOption )
	{
		if ( rtfOption == null )
		{
			return;
		}

		Object value;

		value = rtfOption.get( RtfEmitterConfig.KEY_PAGE_RANGE );
		if ( value != null )
		{
			rangeChecker = new PageRangeChecker( OptionParser.parseString( value ) );
		}

		value = rtfOption.get( RtfEmitterConfig.KEY_EXPORT_BODY_ONLY );
		if ( value != null )
		{
			exportBodyOnly = OptionParser.parseBoolean( value );
		}

		value = rtfOption.get( RtfEmitterConfig.KEY_ALLOW_DISK_CACHE );
		if ( value != null )
		{
			allowDiskCache = OptionParser.parseBoolean( value );
		}

		value = rtfOption.get( RtfEmitterConfig.KEY_CACHE_THRESHOLD );
		if ( value != null )
		{
			cacheThreshold = OptionParser.parseInt( value );
		}

		value = rtfOption.get( RtfEmitterConfig.KEY_SUPPRESS_UNKNOWN_IMAGE );
		if ( value != null )
		{
			suppressUnknownImage = OptionParser.parseBoolean( value );
		}

		value = rtfOption.get( IRenderOption.CLOSE_OUTPUTSTREAM_ON_EXIT );
		if ( value != null )
		{
			closeStream = OptionParser.parseBoolean( value );
		}
	}

	public void start( IReportContent rc )
	{
		if ( DEBUG )
		{
			timeCounter = System.currentTimeMillis( );
		}

		this.frameStack = new Stack<Frame>( );
		currentPageIndex = 1;
		firstPage = true;

		try
		{
			writer = new RtfWriter( output,
					allowDiskCache,
					cacheThreshold,
					services );
		}
		catch ( IOException e )
		{
			e.printStackTrace( );
		}
	}

	public void end( IReportContent rc )
	{
		try
		{
			writer.close( );

			if ( closeStream )
			{
				output.close( );
			}
		}
		catch ( IOException e )
		{
			e.printStackTrace( );
		}

		writer = null;

		frameStack.clear( );
		frameStack = null;

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
			long span = System.currentTimeMillis( );

			if ( rangeChecker.checkRange( currentPageIndex ) )
			{
				PageArea pa = (PageArea) currentFrame.getData( );
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

					exportPage( bodyFrame, true, landscape );
				}
				else
				{
					// one page completed, process it.
					exportPage( currentFrame, false, landscape );
				}

				if ( DEBUG )
				{
					System.out.println( "------------export page[" //$NON-NLS-1$
							+ ( currentPageIndex )
							+ "] using " //$NON-NLS-1$
							+ ( System.currentTimeMillis( ) - span )
							+ " ms" ); //$NON-NLS-1$
				}
			}
			currentPageIndex++;

		}
	}

	private void exportPage( Frame frame, boolean bodyOnly, boolean landscape )
	{
		try
		{
			if ( firstPage )
			{
				firstPage = false;
				writer.setPageSize( frame );

				int ml = 0, mr = 0, mt = 0, mb = 0;

				if ( !bodyOnly )
				{
					PageArea pa = (PageArea) frame.getData( );

					IContainerArea ra = pa.getRoot( );
					ml = ra.getX( );
					mr = pa.getX( )
							+ pa.getWidth( )
							- ra.getX( )
							- ra.getWidth( );
					mt = ra.getY( );
					mb = pa.getY( )
							+ pa.getHeight( )
							- ra.getY( )
							- ra.getHeight( );
				}

				writer.setPageMargin( ml, mr, mt, mb );
				writer.newPage( true, landscape );
			}
			else
			{
				writer.newPage( false, landscape );
			}

			exportFrame( frame );
		}
		catch ( IOException e )
		{
			e.printStackTrace( );
		}
	}

	private void exportFrame( Frame shapeFrame ) throws IOException
	{
		Object area = shapeFrame.getData( );

		if ( area instanceof IImageArea )
		{
			// image object
			writer.writeImage( shapeFrame, suppressUnknownImage );
		}
		else if ( area instanceof ITextArea )
		{
			// text object
			if ( isTotalPageArea( shapeFrame.getData( ) ) )
			{
				writer.writeTotalText( shapeFrame );
			}
			else
			{
				writer.writeText( shapeFrame );
			}
		}
		else
		{
			// plain object
			writer.writePlain( shapeFrame );
		}

		for ( Iterator<Frame> itr = shapeFrame.iterator( ); itr.hasNext( ); )
		{
			Frame childFrame = itr.next( );

			exportFrame( childFrame );
		}

	}

	private boolean isTotalPageArea( Object element )
	{
		if ( element != null && totalPageAreas != null )
		{
			return totalPageAreas.contains( element );
		}
		return false;
	}

	/**
	 * Find child frame by specific data object(use "==" to compare)
	 * 
	 * @param parentFrame
	 * @param data
	 * @return
	 */
	private Frame locateChildFrame( Frame parentFrame, Object data )
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

	public void setTotalPage( ITextArea totalPage )
	{
		if ( totalPage != null )
		{
			String text = totalPage.getText( );

			if ( text != null )
			{
				writer.updateTotalText( text );
			}
		}
	}

	private void buildFrame( IArea area, boolean isContainer )
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
	}
}
