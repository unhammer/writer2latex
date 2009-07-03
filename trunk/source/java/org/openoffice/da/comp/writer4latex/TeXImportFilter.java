/************************************************************************
 *
 *  TeXImportFilter.java
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
 *  Version 1.2 (2009-06-19)
 *
 */ 
 
package org.openoffice.da.comp.writer4latex;

//import com.sun.star.lib.uno.helper.Factory;
import java.io.File;
import java.net.URI;

import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.document.XDocumentInsertable;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.task.XStatusIndicator;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.AnyConverter;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.XInitialization;
import com.sun.star.container.XNamed;
import com.sun.star.document.XImporter;
import com.sun.star.document.XFilter;

/** This class implements an import filter for TeX documents using TeX4ht
 *  It is thus an implementation of the service com.sun.star.document.ImportFilter
 */
public class TeXImportFilter extends WeakBase implements XInitialization, XNamed, XImporter, XFilter, XServiceInfo {

	// Constants
	
	// Identify this service
	public static final String __implementationName = TeXImportFilter.class.getName();
	public static final String __serviceName = "com.sun.star.document.ImportFilter"; 
	private static final String[] m_serviceNames = { __serviceName };

	// Possible states of the filtering process
	public static final int FILTERPROC_RUNNING = 0;
	public static final int FILTERPROC_BREAKING = 1;
	public static final int FILTERPROC_STOPPED = 2;

	// Global data
	
	// From constructor
    private final XComponentContext m_xContext;
    
    // The filter name
    private String m_sFilterName;
	
	// The target document for the import
	private com.sun.star.text.XTextDocument m_xTargetDoc;
	
	// The current state of the filtering process
	private int m_nState;
	
	/** Construct a new <code>TeXImportFilter</code>
	 * 
	 * @param xContext The Component Context
	 */
	public TeXImportFilter( XComponentContext xContext ) {
        m_xContext = xContext;
        m_sFilterName = "";
        m_xTargetDoc = null;
        m_nState = FILTERPROC_STOPPED;
    }
	
	// Implement com.sun.star.lang.XServiceInfo:
	public String getImplementationName() {
		return __implementationName;
	}
	
	public boolean supportsService( String sService ) {
		int len = m_serviceNames.length;

		for(int i=0; i < len; i++) {
			if (sService.equals(m_serviceNames[i]))
				return true;
		}
		return false;
	}

	public String[] getSupportedServiceNames() {
		return m_serviceNames;
	}
	
	// The following methods may be called from multiple threads (eg. if someone wants to cancel the filtering),
	// thus all access to class members must be synchronized
	
	// Implement XInitialization:
	public void initialize( Object[] arguments ) throws com.sun.star.uno.Exception {
		if (arguments.length>0) {
			// The first argument contains configuration data, from which we extract the filter name for further reference
			// We need this to know which flavour of TeX we're supposed to handle
			PropertyValue[] config = (PropertyValue[]) arguments[0];
			int nLen = config.length;
            for (int i=0; i<nLen; i++) {
            	if (config[i].Name.equals("Name")) {
            		synchronized(this) {
            			try {
            				m_sFilterName = AnyConverter.toString(config[i].Value);
            			}
            			catch(com.sun.star.lang.IllegalArgumentException exConvert) {
            				// ignore
            			}
            		}
            	}
            }
		}
	}
	
	// Implement XNamed
	public String getName() {
		synchronized(this) {
			return m_sFilterName;
		}
	}

	public void setName( String sName ) {
		// must be ignored as we cannot change the filter name
	}
	
	// Implement XImporter
	public void setTargetDocument( com.sun.star.lang.XComponent xDocument ) throws com.sun.star.lang.IllegalArgumentException {
		// If there's no target document we cannot import into it
		if (xDocument==null)
			throw new com.sun.star.lang.IllegalArgumentException("Null pointer");

		// And if it's not a text document we're out of luck too
		com.sun.star.lang.XServiceInfo xInfo = (com.sun.star.lang.XServiceInfo)UnoRuntime.queryInterface(
				com.sun.star.lang.XServiceInfo.class, xDocument);
		if (!xInfo.supportsService("com.sun.star.text.TextDocument"))
			throw new com.sun.star.lang.IllegalArgumentException("Wrong document type");

		// Otherwise set the target document
		synchronized(this) {
			m_xTargetDoc = (com.sun.star.text.XTextDocument)UnoRuntime.queryInterface(
				com.sun.star.text.XTextDocument.class, xDocument);
		}
	}

	// Implement XFilter:
	
	/** Filter (import only) the document given by the media descriptor
	 *  According to the service contract, we should understand either of
	 *  the properties URL or InputStream, but currently we only use the former.
	 *  We also use the property StatusIndicator: OOo internally uses this to
	 *  pass around an XStatusIndicator instance, and if it's available we
	 *  use it to display a progress bar
	 *  
	 *  @param mediaDescriptor the Media Descriptor
	 */ 
	public boolean filter(com.sun.star.beans.PropertyValue[] mediaDescriptor) {
		// Extract values from the MediaDescriptor
		String sURL = null;
		XStatusIndicator xStatusIndicator = null;
		int nLength = mediaDescriptor.length;
		for (int i=0; i<nLength; i++) {
			try {
				if (mediaDescriptor[i].Name.equals("URL")) {
					sURL = AnyConverter.toString(mediaDescriptor[i].Value);
				}
				else if (mediaDescriptor[i].Name.equals("InputStream")) {
					// Ignore this currently
				}
				else if (mediaDescriptor[i].Name.equals("StatusIndicator")) {
					xStatusIndicator = (XStatusIndicator) AnyConverter.toObject(XStatusIndicator.class, mediaDescriptor[i].Value);
				}
			}
			catch (com.sun.star.lang.IllegalArgumentException e) {
				// AnyConverter failed to convert; ignore this
			}
		}

		if (sURL==null) {
			// Currently we need and URL to import
			return false;
		}

		// Copy the current value of the target document and mark the filtering process as running
        XTextDocument xText = null;
        synchronized(this) {
        	if (m_nState!=FILTERPROC_STOPPED) {
        		return false;
        	}
            xText = m_xTargetDoc;
            m_nState = FILTERPROC_RUNNING;
        }
        
        // Do the import
        boolean bResult = importTeX(xText,sURL,xStatusIndicator); 
        m_nState = FILTERPROC_STOPPED;
        return bResult;
   }

    /** Cancel the filtering process. This will not only trigger cancellation, but also wait until it's finished
     */
    public void cancel() {
    	// Change state to "breaking"
    	synchronized(this) {
    		if (m_nState==FILTERPROC_RUNNING) m_nState=FILTERPROC_BREAKING;
    	}

    	// And wait until state has changed to "stopped"
    	while (true) {
    		synchronized(this) {
    			if (m_nState==FILTERPROC_STOPPED)
    				break;
    		}
    	}
    }
    
    // Private helper methods
    /** Import a TeX document with TeX4ht
     *  @param xText into this document
     *  @param sURL from the TeX documetn given by this URL
     */
    public boolean importTeX(XTextDocument xText, String sURL, XStatusIndicator xStatus) {
    	int nStep = 0;
    	if (xStatus!=null) {
    		xStatus.start("Writer4LaTeX",10);
    		xStatus.setValue(++nStep);
    	}
   
    	// Get the LaTeX file
    	File file = null;
    	try {
    		file = new File(new URI(sURL));
    	}
    	catch (java.net.URISyntaxException e) {
    		// Could not use the URL provided
    		if (xStatus!=null) xStatus.end();
    		return false;
    	}
    	
    	if (xStatus!=null) { xStatus.setValue(++nStep); }
    	
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
        		if (xStatus!=null) xStatus.end();
    			return false;
    		}
    		odtFile.renameTo(tempFile);
    	}

    	if (xStatus!=null) { xStatus.setValue(++nStep); }

		// Execute TeX4ht
    	ExternalApps externalApps = new ExternalApps(m_xContext);
    	externalApps.load();

    	if (xStatus!=null) { xStatus.setValue(++nStep); }
    	
    	// Default is the filter org.openoffice.da.writer4latex.latex
    	String sCommand = "oolatex";
    	if ("org.openoffice.da.writer4latex.xelatex".equals(m_sFilterName)) {
    		sCommand = "ooxelatex";
    	}
    	
    	System.out.println("Executing tex4ht with command "+sCommand+" on file "+file.getName());
    	
    	externalApps.execute(ExternalApps.MK4HT, sCommand, file.getName(), file.getParentFile(), true);
		
    	if (xStatus!=null) { nStep+=5; xStatus.setValue(nStep); }

		// Assemble the URL of the ODT file
    	String sODTURL = sURL;
    	if (sODTURL.endsWith(".tex")) { sODTURL = sODTURL.substring(0, sODTURL.length()-4); }
    	sODTURL += ".odt";
    	
    	// This is the only good time to test if we should cancel the import
    	boolean bSuccess = true;
        synchronized(this) {
        	if (m_nState==FILTERPROC_BREAKING) bSuccess = false;
        }
        
        if (xStatus!=null) {
        	xStatus.end();
        }
    	
        if (bSuccess) {
        	// Load ODT file into the text document
        	XTextCursor xTextCursor = xText.getText().createTextCursor();
        	XDocumentInsertable xDocInsert = (XDocumentInsertable)
        	UnoRuntime.queryInterface(XDocumentInsertable.class, xTextCursor);
        	try {
        		PropertyValue[] props = new PropertyValue[0];
        		xDocInsert.insertDocumentFromURL(sODTURL, props);
        	}
        	catch (com.sun.star.lang.IllegalArgumentException e) {
        		bSuccess = false;
        	}
        	catch (com.sun.star.io.IOException e) {
        		bSuccess = false;
        	}
        }
        
		odtFile.delete();
		// Restore origninal ODT file, if any
		if (tempFile!=null) {
			tempFile.renameTo(odtFile);
		}
		
		return bSuccess;

    }


}    
