/************************************************************************
 *
 *  TeXDetectService.java
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License version 2.1, as published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA  02111-1307  USA
 *
 *  Copyright: 2002-2009 by Henrik Just
 *
 *  All Rights Reserved.
 * 
 *  Version 1.2 (2009-05-20)
 *
 */ 

package org.openoffice.da.comp.writer4latex;

import com.sun.star.lib.uno.helper.WeakBase;

import com.sun.star.beans.PropertyValue;
import com.sun.star.document.XExtendedFilterDetection;
import com.sun.star.task.XStatusIndicator;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.XComponentContext;

/** This class provides detect services for TeX documents
 *  It is thus an implementation of the service com.sun.star.document.ExtendedTypeDetection
 */

public class TeXDetectService extends WeakBase implements XExtendedFilterDetection {
	
	// Constants
	
	// Identify this service
	public static final String __implementationName = TeXDetectService.class.getName();
	public static final String __serviceName = "com.sun.star.document.ExtendedTypeDetection"; 
	private static final String[] m_serviceNames = { __serviceName };
	
	// From constructor+initialization
    private final XComponentContext m_xContext;

	/** Construct a new <code>TeXDetectService</code>
	 * 
	 * @param xContext The Component Context
	 */
	public TeXDetectService( XComponentContext xContext ) {
        m_xContext = xContext;
    }
	
	// Implement XExtendedFilterDetection
	public String detect(PropertyValue[][] mediaDescriptor) {
		// Read the media properties
		String sURL = null;
		String sTypeName = null;
		if (mediaDescriptor.length>0) {
			int nLength = mediaDescriptor[0].length;
			for (int i=0; i<nLength; i++) {
				try {
					if (mediaDescriptor[0][i].Name.equals("URL")) {
						sURL = AnyConverter.toString(mediaDescriptor[0][i].Value);
					}
					else if (mediaDescriptor[0][i].Name.equals("TypeName")) {
						sTypeName = AnyConverter.toString(mediaDescriptor[0][i].Value);
					}
				}
				catch (com.sun.star.lang.IllegalArgumentException e) {
					// AnyConverter failed to convert; ignore this
				}
			}
		}
		
		if ("org.openoffice.da.writer4latex.LaTeX_File".equals(sTypeName)) {
			return sTypeName;
		}
		else {
			return "";
		}
				
	}


}
