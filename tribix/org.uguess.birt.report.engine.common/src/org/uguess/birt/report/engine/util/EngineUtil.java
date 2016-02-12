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

import java.lang.reflect.Method;

import org.eclipse.birt.report.engine.content.IHyperlinkAction;
import org.eclipse.birt.report.engine.nLayout.area.ITemplateArea;
import org.osgi.framework.Bundle;

/**
 * This class contains some common shared methods for report engine operations.
 */
public class EngineUtil
{

	private static Method mtdGetTooltip = null;
	private static Method mtdGetTemplateType = null;

	static
	{
		try
		{
			mtdGetTooltip = IHyperlinkAction.class.getDeclaredMethod( "getTooltip", //$NON-NLS-1$
					(Class[]) null );
		}
		catch ( Exception e )
		{
			e.printStackTrace( );
		}

		try
		{
			mtdGetTemplateType = ITemplateArea.class.getDeclaredMethod( "getType", //$NON-NLS-1$
					(Class[]) null );
		}
		catch ( Exception e )
		{
			e.printStackTrace( );
		}
	}

	private EngineUtil( )
	{
	}

	/**
	 * Adapter method for <code>IHyperLinkAction.getTooltip()</code>.
	 */
	public static String getActionTooltip( IHyperlinkAction action )
	{
		if ( mtdGetTooltip != null )
		{
			try
			{
				return (String) mtdGetTooltip.invoke( action, (Object[]) null );
			}
			catch ( Exception e )
			{
				e.printStackTrace( );
			}
		}
		return null;
	}

	/**
	 * Adapter method for <code>ITemplateArea.getType()</code>.
	 */
	public static int getTemplateType( ITemplateArea area )
	{
		if ( mtdGetTemplateType != null )
		{
			try
			{
				return (Integer) mtdGetTemplateType.invoke( area,
						(Object[]) null );
			}
			catch ( Exception e )
			{
				e.printStackTrace( );
			}
		}
		return -1;
	}

	public static String getBundleVersion( Bundle bundle )
	{
		if ( bundle != null )
		{
			return String.valueOf( bundle.getHeaders( )
					.get( org.osgi.framework.Constants.BUNDLE_VERSION ) );
		}
		return ""; //$NON-NLS-1$
	}

}
