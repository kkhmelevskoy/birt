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

import java.lang.reflect.Method;

import org.eclipse.birt.core.framework.IBundle;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.uguess.birt.report.engine.util.EngineUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "org.uguess.birt.report.engine.emitter.rtf"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator( )
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start( BundleContext context ) throws Exception
	{
		super.start( context );
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop( BundleContext context ) throws Exception
	{
		plugin = null;
		super.stop( context );
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault( )
	{
		return plugin;
	}

	static String getVersion( )
	{
		if ( plugin != null )
		{
			return EngineUtil.getBundleVersion( plugin.getBundle( ) );
		}

		IBundle bd = Platform.getBundle( PLUGIN_ID );

		if ( bd != null )
		{
			try
			{
				Method mtd = bd.getClass( ).getDeclaredMethod( "getVersion", //$NON-NLS-1$
						(Class<?>[]) null );

				if ( mtd != null )
				{
					mtd.setAccessible( true );

					Object result = mtd.invoke( bd, (Object[]) null );

					return String.valueOf( result );
				}
			}
			catch ( Exception e )
			{
				// eat it
			}
		}

		return "?.?"; //$NON-NLS-1$
	}
}
