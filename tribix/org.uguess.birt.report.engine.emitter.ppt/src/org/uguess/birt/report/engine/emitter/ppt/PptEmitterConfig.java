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

package org.uguess.birt.report.engine.emitter.ppt;

/**
 * PptEmitterConfig
 */
public final class PptEmitterConfig
{

	// prevent from instanciate.
	private PptEmitterConfig( )
	{
	}

	/**
	 * Represents the page range for output. Examples: "1,2,3", "1-8",
	 * "1,4,7-8", or "odd" for odd number pages, "even" for even number pages.
	 * The value should be a String object.
	 */
	public final static String KEY_PAGE_RANGE = "page_range"; //$NON-NLS-1$

	/**
	 * Represents if export the page body only, e.g. not include page
	 * header/footer. The value should be a Boolean object.
	 */
	public final static String KEY_EXPORT_BODY_ONLY = "export_body_only"; //$NON-NLS-1$

	/**
	 * Represents if ignores the image element with unkonw format in the ouput.
	 * Default will replace it with a text element with warning message. The
	 * value should be a Boolean object.
	 */
	public final static String KEY_SUPPRESS_UNKNOWN_IMAGE = "suppress_unknown_image"; //$NON-NLS-1$

	/**
	 * Represents if use mimic borders. The value should be a Boolean object.
	 */
	public final static String KEY_MIMIC_BORDER = "mimic_border"; //$NON-NLS-1$

}
