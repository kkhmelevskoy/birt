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

import junit.framework.TestCase;

import org.uguess.birt.report.engine.util.PageRangeChecker;

/**
 * TestPageRangeChecker
 */
public class TestPageRangeChecker extends TestCase
{

	public void testOdd( )
	{
		PageRangeChecker checker = new PageRangeChecker( "odd" ); //$NON-NLS-1$

		assertTrue( checker.checkRange( 1 ) );
		assertTrue( checker.checkRange( 11 ) );

		assertFalse( checker.checkRange( -1 ) );
		assertFalse( checker.checkRange( 0 ) );
		assertFalse( checker.checkRange( 2 ) );
		assertFalse( checker.checkRange( -2 ) );
	}

	public void testEven( )
	{
		PageRangeChecker checker = new PageRangeChecker( "even" ); //$NON-NLS-1$

		assertFalse( checker.checkRange( 1 ) );
		assertFalse( checker.checkRange( 11 ) );
		assertFalse( checker.checkRange( -1 ) );

		assertTrue( checker.checkRange( 0 ) );
		assertTrue( checker.checkRange( 2 ) );
		assertTrue( checker.checkRange( -2 ) );
	}

	public void testSingle( )
	{
		PageRangeChecker checker = new PageRangeChecker( "1" ); //$NON-NLS-1$

		assertTrue( checker.checkRange( 1 ) );

		assertFalse( checker.checkRange( 2 ) );
		assertFalse( checker.checkRange( 0 ) );
	}

	public void testMultiSingle( )
	{
		PageRangeChecker checker = new PageRangeChecker( "1, 2, 4, 7" ); //$NON-NLS-1$

		assertTrue( checker.checkRange( 1 ) );
		assertTrue( checker.checkRange( 2 ) );
		assertTrue( checker.checkRange( 4 ) );
		assertTrue( checker.checkRange( 7 ) );

		assertFalse( checker.checkRange( 3 ) );
		assertFalse( checker.checkRange( 0 ) );
		assertFalse( checker.checkRange( 9 ) );
	}

	public void testRange( )
	{
		PageRangeChecker checker = new PageRangeChecker( "1-4" ); //$NON-NLS-1$

		assertTrue( checker.checkRange( 1 ) );
		assertTrue( checker.checkRange( 2 ) );
		assertTrue( checker.checkRange( 3 ) );
		assertTrue( checker.checkRange( 4 ) );

		assertFalse( checker.checkRange( 0 ) );
		assertFalse( checker.checkRange( 5 ) );
		assertFalse( checker.checkRange( 9 ) );

		checker = new PageRangeChecker( "4-2" ); //$NON-NLS-1$

		assertTrue( checker.checkRange( 2 ) );
		assertTrue( checker.checkRange( 3 ) );
		assertTrue( checker.checkRange( 4 ) );

		assertFalse( checker.checkRange( 0 ) );
		assertFalse( checker.checkRange( 5 ) );
		assertFalse( checker.checkRange( 9 ) );
	}

	public void testMultiRange( )
	{
		PageRangeChecker checker = new PageRangeChecker( "1-4, 9 - 8" ); //$NON-NLS-1$

		assertTrue( checker.checkRange( 1 ) );
		assertTrue( checker.checkRange( 2 ) );
		assertTrue( checker.checkRange( 3 ) );
		assertTrue( checker.checkRange( 4 ) );
		assertTrue( checker.checkRange( 8 ) );
		assertTrue( checker.checkRange( 9 ) );

		assertFalse( checker.checkRange( 0 ) );
		assertFalse( checker.checkRange( 5 ) );
		assertFalse( checker.checkRange( 10 ) );
	}

	public void testMixedRange( )
	{
		PageRangeChecker checker = new PageRangeChecker( "1, 4, 7-9, 12" ); //$NON-NLS-1$

		assertTrue( checker.checkRange( 1 ) );
		assertTrue( checker.checkRange( 4 ) );
		assertTrue( checker.checkRange( 8 ) );
		assertTrue( checker.checkRange( 9 ) );
		assertTrue( checker.checkRange( 12 ) );

		assertFalse( checker.checkRange( 0 ) );
		assertFalse( checker.checkRange( 2 ) );
		assertFalse( checker.checkRange( 5 ) );
		assertFalse( checker.checkRange( 10 ) );
	}
}
