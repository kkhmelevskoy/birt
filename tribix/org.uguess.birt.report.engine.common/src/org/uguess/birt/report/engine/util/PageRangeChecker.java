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

package org.uguess.birt.report.engine.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * PageRangeChecker
 */
public class PageRangeChecker
{

	/**
	 * Constant value indicates to output all odd number pages.
	 */
	public final static String ALL_ODD_PAGE = "odd"; //$NON-NLS-1$

	/**
	 * Constant value indicates to output all even number pages.
	 */
	public final static String ALL_EVEN_PAGE = "even"; //$NON-NLS-1$

	private boolean cached = false;
	private boolean oddMode = false;
	private boolean evenMode = false;
	private List<Range> rangeList;

	private String pageRange;

	public PageRangeChecker( String pageRange )
	{
		this.pageRange = pageRange;
	}

	public boolean checkRange( int pageNumber )
	{
		if ( cached )
		{
			return true;
		}

		if ( pageRange == null || pageRange.trim( ).length( ) == 0 )
		{
			cached = true;
			return true;
		}

		if ( !evenMode && !oddMode && rangeList == null )
		{
			if ( ALL_EVEN_PAGE.equalsIgnoreCase( pageRange ) )
			{
				evenMode = true;
			}
			else if ( ALL_ODD_PAGE.equalsIgnoreCase( pageRange ) )
			{
				oddMode = true;
			}
			else
			{
				rangeList = new ArrayList<Range>( );

				StringTokenizer st = new StringTokenizer( pageRange, "," ); //$NON-NLS-1$
				while ( st.hasMoreTokens( ) )
				{
					String cpg = st.nextToken( ).trim( );
					int sepdx = cpg.indexOf( "-" ); //$NON-NLS-1$

					if ( sepdx < 0 )
					{
						try
						{
							int ipg = Integer.parseInt( cpg );

							rangeList.add( new Range( ipg ) );
						}
						catch ( Exception _ )
						{
							// ignore illegal case
						}
					}
					else
					{
						try
						{
							String spg = cpg.substring( 0, sepdx ).trim( );
							String epg = cpg.substring( sepdx + 1 ).trim( );

							int ispg = Integer.parseInt( spg );
							int iepg = Integer.parseInt( epg );

							rangeList.add( new Range( ispg, iepg ) );
						}
						catch ( Exception _ )
						{
							// ignore illegal case
						}
					}
				}
			}
		}

		if ( evenMode )
		{
			return pageNumber % 2 == 0;
		}
		else if ( oddMode )
		{
			return pageNumber % 2 != 0;
		}
		else if ( rangeList != null )
		{
			for ( Iterator<Range> itr = rangeList.iterator( ); itr.hasNext( ); )
			{
				Range rg = itr.next( );

				if ( rg.contains( pageNumber ) )
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Range
	 */
	static class Range
	{

		int start, end;

		Range( int single )
		{
			start = end = single;
		}

		Range( int start, int end )
		{
			if ( start > end )
			{
				this.start = end;
				this.end = start;
			}
			else
			{
				this.start = start;
				this.end = end;
			}
		}

		boolean contains( int number )
		{
			return number >= start && number <= end;
		}
	}

}
