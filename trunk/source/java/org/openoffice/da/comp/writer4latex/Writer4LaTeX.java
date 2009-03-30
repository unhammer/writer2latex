/************************************************************************
 *
 *  Writer4LaTeX.java
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
 *  Version 1.2 (2009-03-30)
 *
 */ 
 
package org.openoffice.da.comp.writer4latex;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertyAccess;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.task.XStatusIndicator;
import com.sun.star.task.XStatusIndicatorFactory;
import com.sun.star.ui.dialogs.ExecutableDialogResults;
import com.sun.star.ui.dialogs.XExecutableDialog;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import org.openoffice.da.comp.w2lcommon.helper.MessageBox;
import org.openoffice.da.comp.w2lcommon.helper.PropertyHelper;
       
/** This class implements the ui (dispatch) commands provided by Writer4LaTeX.
 *  The actual processing is done by the three core classes <code>TeXify</code>,
 *  <code>LaTeXImporter</code> and <code>BibTeXImporter</code>
 */
public final class Writer4LaTeX extends WeakBase
    implements com.sun.star.lang.XServiceInfo,
    com.sun.star.frame.XDispatchProvider,
    com.sun.star.lang.XInitialization,
    com.sun.star.frame.XDispatch {
	
    private static final String PROTOCOL = "org.openoffice.da.writer4latex:";
    
    // From constructor+initialization
    private final XComponentContext m_xContext;
    private XFrame m_xFrame;
    private XModel xModel = null;
	
    // Global data
    private TeXify texify = null;
    private PropertyValue[] mediaProps = null;
    private String sBasePath = null;
    private String sBaseFileName = null;

    public static final String __implementationName = Writer4LaTeX.class.getName();
    public static final String __serviceName = "com.sun.star.frame.ProtocolHandler"; 
    private static final String[] m_serviceNames = { __serviceName };
      
    public Writer4LaTeX( XComponentContext xContext ) {
        m_xContext = xContext;
    }
	
    // com.sun.star.lang.XInitialization:
    public void initialize( Object[] object )
        throws com.sun.star.uno.Exception {
        if ( object.length > 0 ) {
            // The first item is the current frame
            m_xFrame = (com.sun.star.frame.XFrame) UnoRuntime.queryInterface(
            com.sun.star.frame.XFrame.class, object[0]);
            // Get the model for the document from the frame
            XController xController = m_xFrame.getController();
            if (xController!=null) {
                xModel = xController.getModel();
            }
        }
    }
	
    // com.sun.star.lang.XServiceInfo:
    public String getImplementationName() {
        return __implementationName;
    }

    public boolean supportsService( String sService ) {
        int len = m_serviceNames.length;

        for( int i=0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }

	
    // com.sun.star.frame.XDispatchProvider:
    public com.sun.star.frame.XDispatch queryDispatch( com.sun.star.util.URL aURL,
        String sTargetFrameName, int iSearchFlags ) {
        if ( aURL.Protocol.compareTo(PROTOCOL) == 0 ) {
            if ( aURL.Path.compareTo("ProcessDocument") == 0 )
                return this;
            else if ( aURL.Path.compareTo("ProcessDirectly") == 0 )
                return this;
            else if ( aURL.Path.compareTo("ViewLog") == 0 )
                return this;
            else if ( aURL.Path.compareTo("UseBibTeX") == 0 )
                return this;
            else if ( aURL.Path.compareTo("ImportBibTeX") == 0 )
                return this;
            else if ( aURL.Path.compareTo("ImportLaTeX") == 0 )
                return this;
        }
        return null;
    }

    public com.sun.star.frame.XDispatch[] queryDispatches(
    com.sun.star.frame.DispatchDescriptor[] seqDescriptors ) {
        int nCount = seqDescriptors.length;
        com.sun.star.frame.XDispatch[] seqDispatcher =
        new com.sun.star.frame.XDispatch[seqDescriptors.length];

        for( int i=0; i < nCount; ++i ) {
            seqDispatcher[i] = queryDispatch(seqDescriptors[i].FeatureURL,
            seqDescriptors[i].FrameName,
            seqDescriptors[i].SearchFlags );
        }
        return seqDispatcher;
    }


    // com.sun.star.frame.XDispatch:
    public void dispatch( com.sun.star.util.URL aURL,
        com.sun.star.beans.PropertyValue[] aArguments ) {
        if ( aURL.Protocol.compareTo(PROTOCOL) == 0 ) {
            if ( aURL.Path.compareTo("ProcessDocument") == 0 ) {
                if (updateLocation()) {
                    if (updateMediaProperties()) {
                        process();
                    }
                }
                else {
                    warnNotSaved();
                }
                return;
            }
            else if ( aURL.Path.compareTo("ProcessDirectly") == 0 ) {
                if (updateLocation()) {
                    if (mediaProps!=null || updateMediaProperties()) {
                        process();
                    }
                }
                else {
                    warnNotSaved();
                }
                return;
            }
            else if ( aURL.Path.compareTo("ViewLog") == 0 ) {
                viewLog();
                return;
            }
            else if ( aURL.Path.compareTo("UseBibTeX") == 0 ) {
                org.openoffice.da.comp.w2lcommon.helper.MessageBox msgBox = new org.openoffice.da.comp.w2lcommon.helper.MessageBox(m_xContext);
                msgBox.showMessage("Writer4LaTeX", "This feature has not been implemented yet");
                return;
            }
            else if ( aURL.Path.compareTo("ImportBibTeX") == 0 ) {
                org.openoffice.da.comp.w2lcommon.helper.MessageBox msgBox = new org.openoffice.da.comp.w2lcommon.helper.MessageBox(m_xContext);
                msgBox.showMessage("Writer4LaTeX", "This feature has not been implemented yet");
                return;
            }
            else if ( aURL.Path.compareTo("ImportLaTeX") == 0 ) {
                org.openoffice.da.comp.w2lcommon.helper.MessageBox msgBox = new org.openoffice.da.comp.w2lcommon.helper.MessageBox(m_xContext);
                msgBox.showMessage("Writer4LaTeX", "This feature has not been implemented yet");
                return;
            }
        }
    }

    public void addStatusListener( com.sun.star.frame.XStatusListener xControl,
    com.sun.star.util.URL aURL ) {
    }

    public void removeStatusListener( com.sun.star.frame.XStatusListener xControl,
    com.sun.star.util.URL aURL ) {
    }
	
    // The actual commands...
	
    private void process() {
        // Create a (somewhat coarse grained) status indicator/progress bar
        XStatusIndicatorFactory xFactory = (com.sun.star.task.XStatusIndicatorFactory)
            UnoRuntime.queryInterface(com.sun.star.task.XStatusIndicatorFactory.class, m_xFrame);
        XStatusIndicator xStatus = xFactory.createStatusIndicator();
        xStatus.start("Writer4LaTeX",10);
        xStatus.setValue(1); // At least we have started, that's 10% :-)
        
        try {
            // Convert to LaTeX
            String sTargetUrl = sBasePath+sBaseFileName+".tex";
            XStorable xStorable = (XStorable) UnoRuntime.queryInterface(XStorable.class, xModel);
            xStorable.storeToURL(sTargetUrl, mediaProps);
        }
        catch (com.sun.star.io.IOException e) {
            xStatus.end();
            MessageBox msgBox = new MessageBox(m_xContext, m_xFrame);
            msgBox.showMessage("Writer4LaTeX Error","Failed to export document to LaTeX");
            return;
        }
		
        xStatus.setValue(6); // Export is finished, that's more than half :-)

        // Get the backend from the media properties
        String sBackend = "generic";
        Object filterData = (new PropertyHelper(mediaProps)).get("FilterData");
        if (filterData instanceof PropertyValue[]) {
            Object backend = (new PropertyHelper((PropertyValue[])filterData)).get("backend");
            if (backend instanceof String) {
                sBackend = (String) backend;
            }
        }
		
        if (texify==null) { texify = new TeXify(m_xContext); }
        File file = new File(urlToFile(sBasePath),sBaseFileName);
		
        try {
            if (sBackend=="pdftex") {
                texify.process(file, TeXify.PDFTEX, true);
            }
            else if (sBackend=="dvips") {
                texify.process(file, TeXify.DVIPS, true);
            }
            else if (sBackend=="generic") {
                texify.process(file, TeXify.GENERIC, true);
            }
        }
        catch (IOException e) {
            MessageBox msgBox = new MessageBox(m_xContext, m_xFrame);
            msgBox.showMessage("Writer4LaTeX Error",e.getMessage());
        }
		
        xStatus.setValue(10); // The user will not really see this...
        xStatus.end();
    }
	
    private void viewLog() {
        if (updateLocation()) {
            // Execute the log viewer dialog
            try {
                Object[] args = new Object[1];
                args[0] = sBasePath+sBaseFileName;
                Object dialog = m_xContext.getServiceManager()
                    .createInstanceWithArgumentsAndContext(
                    "org.openoffice.da.writer4latex.LogViewerDialog", args, m_xContext);
                XExecutableDialog xDialog = (XExecutableDialog)
                    UnoRuntime.queryInterface(XExecutableDialog.class, dialog);
                if (xDialog.execute()==ExecutableDialogResults.OK) {
                    // Closed with the close button
                }
            }
            catch (com.sun.star.uno.Exception e) {
            }
        }
        else {
            warnNotSaved();
        }

    }

    // Some utility methods
	
    private boolean updateMediaProperties() {
        // Create inital media properties
        mediaProps = new PropertyValue[2];
        mediaProps[0] = new PropertyValue();
        mediaProps[0].Name = "FilterName";
        mediaProps[0].Value = "org.openoffice.da.writer2latex";
        mediaProps[1] = new PropertyValue();
        mediaProps[1].Name = "Overwrite";
        mediaProps[1].Value = "true";

        try {
            // Display options dialog
            Object dialog = m_xContext.getServiceManager()
                .createInstanceWithContext("org.openoffice.da.writer2latex.LaTeXOptionsDialog", m_xContext);

            XPropertyAccess xPropertyAccess = (XPropertyAccess)
                UnoRuntime.queryInterface(XPropertyAccess.class, dialog);
            xPropertyAccess.setPropertyValues(mediaProps);

            XExecutableDialog xDialog = (XExecutableDialog)
                UnoRuntime.queryInterface(XExecutableDialog.class, dialog);
            if (xDialog.execute()==ExecutableDialogResults.OK) {
                mediaProps = xPropertyAccess.getPropertyValues();
                return true;
            }
            else {
                mediaProps = null;
                return false;
            }
        }
        catch (com.sun.star.beans.UnknownPropertyException e) {
            // setPropertyValues will not fail..
            mediaProps = null;
            return false;
        }
        catch (com.sun.star.uno.Exception e) {
            // getServiceManager will not fail..
            mediaProps = null;
            return false;
        }
    }
	
    private boolean updateLocation() {
        String sDocumentUrl = xModel.getURL();
        if (sDocumentUrl.length()!=0) {
            // Get the file name (without extension)
            File f = urlToFile(sDocumentUrl);
            sBaseFileName = f.getName();
            int iDot = sBaseFileName.lastIndexOf(".");
            if (iDot>-1) { // remove extension
                sBaseFileName = sBaseFileName.substring(0,iDot);
            }
            sBaseFileName=makeTeXSafe(sBaseFileName);

            // Get the path
            int iSlash = sDocumentUrl.lastIndexOf("/");
            if (iSlash>-1) {
                sBasePath = sDocumentUrl.substring(0,iSlash+1);
            }
            else {
                sBasePath = "";
            }
			
            return true;
        }
        else {
            return false;
        }
    }

    private void warnNotSaved() {
        MessageBox msgBox = new MessageBox(m_xContext, m_xFrame);
        msgBox.showMessage("Document not saved!","Please save the document before processing the file");
    }
	
    private String makeTeXSafe(String sArgument) {
        String sResult = "";
        for (int i=0; i<sArgument.length(); i++) {
            char c = sArgument.charAt(i);
            if ((c>='a' && c<='z') || (c>='A' && c<='Z') || (c>='0' && c<='9') || c=='-' || c=='.') {
                sResult += Character.toString(c);
            }
            // TODO: Create replacement table for other latin characters..
            else if (c==' ') { sResult += "-"; }
            else if (c=='\u00c6') { sResult += "AE"; }
            else if (c=='\u00d8') { sResult += "OE"; }
            else if (c=='\u00c5') { sResult += "AA"; }
            else if (c=='\u00e6') { sResult += "ae"; }
            else if (c=='\u00f8') { sResult += "oe"; }
            else if (c=='\u00e5') { sResult += "aa"; }
        }
        if (sResult.length()==0) { return "writer4latex"; }
        else { return sResult; }
    }
	
    private File urlToFile(String sUrl) {
        try {
            return new File(new URI(sUrl));
        }
        catch (URISyntaxException e) {
            return new File(".");
        }
    }
	
    /*private String urlToPath(String sUrl) {
        try {
            return (new File(new URI(sUrl))).getCanonicalPath();
        }
        catch (URISyntaxException e) {
            return ".";
        }
        catch (IOException e) {
            return ".";
        }
    }*/
	
}