/************************************************************************
 *
 *  LaTeXImporter.java
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
 *  Version 1.2 (2009-05-01)
 *
 */ 

package org.openoffice.da.comp.writer4latex;

import java.io.File;
import java.net.URI;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public class LaTeXImporter {
	
	private XComponentContext xContext;
	
	private ExternalApps externalApps;
	
    public LaTeXImporter(XComponentContext xContext) {
        this.xContext = xContext;
        externalApps = new ExternalApps(xContext);
        externalApps.load();
    }
    
    public void importLaTeX(String sURL) {
    	// Get the LaTeX file
    	File file = null;
    	try {
    		file = new File(new URI(sURL));
    	}
    	catch (java.net.URISyntaxException e) {
    		// Could not use the URL provided
    		return;
    	}
    	
    	// Protect the ODT file if it already exists
    	String sBaseName = file.getName();
    	if (sBaseName.endsWith(".tex")) { sBaseName = sBaseName.substring(0, sBaseName.length()-4); }
    	File odtFile = new File(file.getParentFile(),sBaseName+".odt");
    	File tempFile = null;
    	if (odtFile.exists()) {
    		try {
    			tempFile = File.createTempFile("w4l", ".tmp", file.getParentFile());
    		}
    		catch (java.io.IOException e) {
    			// Failed to protect the ODT file, give up
    			return;
    		}
    		odtFile.renameTo(tempFile);
    	}

		externalApps.execute(ExternalApps.MK4HT, file.getName(), file.getParentFile(), true);

		// Assemble the URL of the ODT file
    	String sODTURL = sURL;
    	if (sODTURL.endsWith(".tex")) { sODTURL = sODTURL.substring(0, sODTURL.length()-4); }
    	sODTURL += ".odt";
    	
    	// Get the component loader
    	Object desktop;
    	try {
    		desktop = xContext.getServiceManager().createInstanceWithContext(
    				"com.sun.star.frame.Desktop", xContext);

    		XComponentLoader xComponentLoader = (XComponentLoader)UnoRuntime.queryInterface(
    				XComponentLoader.class, desktop);

    		// Load the file
    		PropertyValue[] props = new PropertyValue[1];
    		props[0] = new PropertyValue();
    		props[0].Name = "AsTemplate";
    		props[0].Value = new Boolean(true); 

    		xComponentLoader.loadComponentFromURL(sODTURL, "_blank", 0, props);
    	}
    	catch (com.sun.star.lang.IllegalArgumentException e) {
    		System.out.println("Fejler med illegalargumentexception");
    	}
    	catch (com.sun.star.io.IOException e) {
    		System.out.println("Fejler med ioexception");
    	}
    	catch (com.sun.star.uno.Exception e) {
    		System.out.println("Failed to get desktop");
    	}
    	finally {
			odtFile.delete();
			// Restore origninal ODT file, if any
    		if (tempFile!=null) {
    			tempFile.renameTo(odtFile);
    		}
    	}
    }

}
