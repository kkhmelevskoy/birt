/********************************************************************************
 * (C) Copyright 2000-2008, by Shawn Qualia.
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

package org.uguess.birt.report.engine.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.nLayout.area.IArea;
import org.eclipse.birt.report.engine.nLayout.area.IAreaVisitor;
import org.eclipse.birt.report.engine.nLayout.area.IContainerArea;
import org.eclipse.birt.report.engine.nLayout.area.IImageArea;
import org.eclipse.birt.report.engine.nLayout.area.ITemplateArea;
import org.eclipse.birt.report.engine.nLayout.area.ITextArea;
import org.eclipse.birt.report.engine.nLayout.area.impl.ContainerArea;

/**
 * ContentLookupBuilder
 */
public class ContentLookupBuilder implements IAreaVisitor
{

	private Map<IArea, IContent> contentCache = new HashMap<IArea, IContent>( );
	private Stack<IContent> contentStack = new Stack<IContent>( );
	private IContent currentContent;

	public void reset( )
	{
		contentStack.clear( );
		contentCache.clear( );
		currentContent = null;
	}

	public Map<IArea, IContent> getMapping( )
	{
		return contentCache;
	}

	private void startContent( IArea area )
	{
		if ( area instanceof ContainerArea )
		{
			IContent ct = ( (ContainerArea) area ).getContent( );

			if ( ct != null )
			{
				currentContent = ct;

				contentStack.push( ct );
			}
		}
	}

	private void endContent( IArea area )
	{
		if ( area instanceof ContainerArea )
		{
			IContent ct = ( (ContainerArea) area ).getContent( );

			if ( ct != null )
			{
				contentStack.pop( );

				if ( !contentStack.empty( ) )
				{
					currentContent = contentStack.peek( );
				}
				else
				{
					currentContent = null;
				}
			}
		}
	}

	public void visitAutoText( ITemplateArea templateArea )
	{
		startContent( templateArea );

		if ( currentContent != null && templateArea != null )
		{
			contentCache.put( templateArea, currentContent );
		}

		endContent( templateArea );
	}

	public void visitContainer( IContainerArea containerArea )
	{
		startContent( containerArea );

		if ( currentContent != null && containerArea != null )
		{
			contentCache.put( containerArea, currentContent );
		}

		Iterator<IArea> iter = containerArea.getChildren( );

		while ( iter.hasNext( ) )
		{
			IArea child = iter.next( );
			child.accept( this );
		}

		endContent( containerArea );
	}

	public void visitImage( IImageArea imageArea )
	{
		startContent( imageArea );

		if ( currentContent != null && imageArea != null )
		{
			contentCache.put( imageArea, currentContent );
		}

		endContent( imageArea );
	}

	public void visitText( ITextArea textArea )
	{
		startContent( textArea );

		if ( currentContent != null && textArea != null )
		{
			contentCache.put( textArea, currentContent );
		}

		endContent( textArea );
	}

}
