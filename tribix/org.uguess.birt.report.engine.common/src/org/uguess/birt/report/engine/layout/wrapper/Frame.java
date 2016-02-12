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

package org.uguess.birt.report.engine.layout.wrapper;

import java.util.Iterator;

/**
 * This interface represents an abstract styled object with location and size.
 */
public interface Frame
{

	/**
	 * Returns the parent of this frame.
	 * 
	 * @return
	 */
	Frame getParent( );

	/**
	 * Returns the iterator for all child frames.
	 * 
	 * @return
	 */
	Iterator<Frame> iterator( );

	/**
	 * Returns the data associated with current frame.
	 * 
	 * @return
	 */
	Object getData( );

	/**
	 * Returns the style associated with current frame.
	 * 
	 * @return
	 */
	Style getStyle( );

	/**
	 * Returns the left position of current frame.
	 * 
	 * @return
	 */
	int getLeft( );

	/**
	 * Returns the top position of current frame.
	 * 
	 * @return
	 */
	int getTop( );

	/**
	 * Returns the right position of current frame.
	 * 
	 * @return
	 */
	int getRight( );

	/**
	 * Returns the bottom position of current frame.
	 * 
	 * @return
	 */
	int getBottom( );
}