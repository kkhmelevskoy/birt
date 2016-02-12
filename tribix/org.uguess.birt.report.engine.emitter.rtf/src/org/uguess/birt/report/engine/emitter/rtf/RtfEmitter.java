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

import org.eclipse.birt.report.engine.content.IAutoTextContent;
import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.content.IPageContent;
import org.eclipse.birt.report.engine.content.IReportContent;
import org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter;
import org.eclipse.birt.report.engine.emitter.IEmitterServices;
import org.eclipse.birt.report.engine.nLayout.area.IArea;
import org.eclipse.birt.report.engine.nLayout.area.ITextArea;

/**
 * RtfEmitter
 */
public class RtfEmitter extends ContentEmitterAdapter
{

	private RtfRenderer renderer = new RtfRenderer( );

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter#initialize(org.eclipse.birt.report.engine.emitter.IEmitterServices)
	 */
	public void initialize( IEmitterServices service )
	{
		renderer.initialize( service );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter#getOutputFormat()
	 */
	public String getOutputFormat( )
	{
		return renderer.getOutputFormat( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter#start(org.eclipse.birt.report.engine.content.IReportContent)
	 */
	public void start( IReportContent report )
	{
		renderer.start( report );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter#end(org.eclipse.birt.report.engine.content.IReportContent)
	 */
	public void end( IReportContent report )
	{
		renderer.end( report );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter#startPage(org.eclipse.birt.report.engine.content.IPageContent)
	 */
	public void startPage( IPageContent page )
	{
		IArea pageArea = (IArea) page.getExtension( IContent.LAYOUT_EXTENSION );
		if ( pageArea != null )
		{
			pageArea.accept( renderer );
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.engine.emitter.ContentEmitterAdapter#startAutoText(org.eclipse.birt.report.engine.content.IAutoTextContent)
	 */
	public void startAutoText( IAutoTextContent autoText )
	{
		ITextArea totalPage = (ITextArea) autoText.getExtension( IContent.LAYOUT_EXTENSION );
		renderer.setTotalPage( totalPage );
	}

}
