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
 *  Version 1.2 (2009-06-19)
 *
 */ 
 
package org.openoffice.da.comp.writer4latex;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertyAccess;
import com.sun.star.beans.XPropertySet;
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
import org.openoffice.da.comp.w2lcommon.helper.RegistryHelper;
import org.openoffice.da.comp.w2lcommon.helper.XPropertySetHelper;
       
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
                	updateMediaPropertiesSilent();
                    process();
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
            else if (sBackend=="xetex") {
                texify.process(file, TeXify.XETEX, true);
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
    private void prepareMediaProperties() {
        // Create inital media properties
        mediaProps = new PropertyValue[2];
        mediaProps[0] = new PropertyValue();
        mediaProps[0].Name = "FilterName";
        mediaProps[0].Value = "org.openoffice.da.writer2latex";
        mediaProps[1] = new PropertyValue();
        mediaProps[1].Name = "Overwrite";
        mediaProps[1].Value = "true";    	
    }
    
    private boolean updateMediaProperties() {
    	prepareMediaProperties();
    	
        try {
            // Display options dialog
            Object dialog = m_xContext.getServiceManager()
                .createInstanceWithContext("org.openoffice.da.writer2latex.LaTeXOptionsDialog", m_xContext);
            
            // If Writer2LaTeX is not installed, this will return null
            if (dialog==null) {
            	mediaProps = null;
            	MessageBox msgBox = new MessageBox(m_xContext, m_xFrame);
                msgBox.showMessage("Writer4LaTeX Error","Please install Writer2LaTeX version 1.0 or later");
            	return false;
            }

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
    
    private String getOptionAsString(XPropertySet xProps, String sName) {
    	Object value = XPropertySetHelper.getPropertyValue(xProps, sName);
        // Try to convert the value to a string
        if (value instanceof String) return (String) value;
        else if (value instanceof Boolean) return ((Boolean) value).toString();
        else if (value instanceof Integer) return ((Integer) value).toString();
        else if (value instanceof Short) return ((Short) value).toString();
        else return null;
    }
    
    private void loadOption(XPropertySet xProps, PropertyHelper filterData, String sRegName, String sOptionName) {
        String sValue = getOptionAsString(xProps,sRegName);
        if (sValue!=null) {
        	// Set the filter data
        	filterData.put(sOptionName, sValue);
        }
    }
    
    // Read the configuration directly from the registry rather than using the dialog
    // TODO: Should probably do some refactoring in the Options dialogs to avoid this solution
    private void updateMediaPropertiesSilent() {
    	prepareMediaProperties();

    	RegistryHelper registry = new RegistryHelper(m_xContext);
    	
    	// Read the stored settings from the registry rather than displaying a dialog
    	try {
    		// Prepare registry view
    		Object view = registry.getRegistryView("/org.openoffice.da.Writer2LaTeX.Options/LaTeXOptions",true);
    		XPropertySet xProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,view);
    		
            PropertyHelper filterData = new PropertyHelper();
            
            // Read the configuration file
            short nConfig = XPropertySetHelper.getPropertyValueAsShort(xProps, "Config");
            switch (nConfig) {
            case 0: filterData.put("ConfigURL","*ultraclean.xml"); break;
            case 1: filterData.put("ConfigURL","*clean.xml"); break;
            case 2: filterData.put("ConfigURL","*default.xml"); break;
            case 3: filterData.put("ConfigURL","*pdfprint.xml"); break;
            case 4: filterData.put("ConfigURL","*pdfscreen.xml"); break;
            case 5: filterData.put("ConfigURL","$(user)/writer2latex.xml");
            		filterData.put("AutoCreate","true"); break;
            default:
            	loadOption(xProps,filterData,"ConfigName","ConfigURL");
            }
            
    		// Read the options
    		// General
    		short nBackend = XPropertySetHelper.getPropertyValueAsShort(xProps,"Backend");
    		switch (nBackend) {
    		case 0: filterData.put("backend","generic"); break;
    		case 1: filterData.put("backend","pdftex"); break; 
    		case 2: filterData.put("backend","dvips"); break; 
    		case 3: filterData.put("backend","xetex"); break; 
    		case 4: filterData.put("backend","unspecified"); 
    		}
    		short nInputencoding = XPropertySetHelper.getPropertyValueAsShort(xProps,"Inputencoding");
    		switch (nInputencoding) {
    		case 0: filterData.put("inputencoding", "ascii"); break;
    		case 1: filterData.put("inputencoding", "latin1"); break;
    		case 2: filterData.put("inputencoding", "latin2"); break;
    		case 3: filterData.put("inputencoding", "iso-8859-7"); break;
    		case 4: filterData.put("inputencoding", "cp1250"); break;
    		case 5: filterData.put("inputencoding", "cp1251"); break;
    		case 6: filterData.put("inputencoding", "koi8-r"); break;
    		case 7: filterData.put("inputencoding", "utf8");
    		}
    		loadOption(xProps,filterData,"Multilingual","multilingual");
    		loadOption(xProps,filterData,"GreekMath","greek_math");
    		loadOption(xProps,filterData,"AdditionalSymbols","use_pifont");
    		loadOption(xProps,filterData,"AdditionalSymbols","use_ifsym");
    		loadOption(xProps,filterData,"AdditionalSymbols","use_wasysym");
    		loadOption(xProps,filterData,"AdditionalSymbols","use_eurosym");
    		loadOption(xProps,filterData,"AdditionalSymbols","use_tipa");

    		// Bibliography
    		loadOption(xProps,filterData,"UseBibtex","use_bibtex");
    		loadOption(xProps,filterData,"BibtexStyle","bibtex_style");

    		// Files
    		boolean bWrapLines = XPropertySetHelper.getPropertyValueAsBoolean(xProps,"WrapLines");
    		if (bWrapLines) {
    			loadOption(xProps,filterData,"WrapLinesAfter","wrap_lines_after");
    		}
    		else {
    			filterData.put("wrap_lines_after", "0");
    		}
    		loadOption(xProps,filterData,"SplitLinkedSections","split_linked_sections");
    		loadOption(xProps,filterData,"SplitToplevelSections","split_toplevel_sections");
    		loadOption(xProps,filterData,"SaveImagesInSubdir","save_images_in_subdir");

    		// Special content
    		short nNotes = XPropertySetHelper.getPropertyValueAsShort(xProps, "Notes");
    		switch (nNotes) {
    		case 0: filterData.put("notes","ignore"); break;
    		case 1: filterData.put("notes","comment"); break;
    		case 2: filterData.put("notes","pdfannotation"); break;
    		case 3: filterData.put("notes","marginpar");
    		}
    		loadOption(xProps,filterData,"Metadata","metadata");

    		// Figures and tables
    		loadOption(xProps,filterData,"OriginalImageSize","original_image_size");
    		boolean bOptimizeSimpleTables = XPropertySetHelper.getPropertyValueAsBoolean(xProps,"OptimizeSimpleTables");
    		if (bOptimizeSimpleTables) {
        		loadOption(xProps,filterData,"SimpleTableLimit","simple_table_limit");    			
    		}
    		else {
    			filterData.put("simple_table_limit", "0");
    		}
    		loadOption(xProps,filterData,"FloatTables","float_tables");
    		loadOption(xProps,filterData,"FloatFigures","float_figures");
    		loadOption(xProps,filterData,"FloatOptions","float_options");
    	    short nFloatOptions = XPropertySetHelper.getPropertyValueAsShort(xProps, "FloatOptions");
    	    switch (nFloatOptions) {
    	    case 0: filterData.put("float_options", ""); break;
    	    case 1: filterData.put("float_options", "tp"); break;
    	    case 2: filterData.put("float_options", "bp"); break;
    	    case 3: filterData.put("float_options", "htp"); break;
    	    case 4: filterData.put("float_options", "hbp");
    	    }

    		// AutoCorrect
    		loadOption(xProps,filterData,"IgnoreHardPageBreaks","ignore_hard_page_breaks");
    		loadOption(xProps,filterData,"IgnoreHardLineBreaks","ignore_hard_line_breaks");
    		loadOption(xProps,filterData,"IgnoreEmptyParagraphs","ignore_empty_paragraphs");
    		loadOption(xProps,filterData,"IgnoreDoubleSpaces","ignore_empty_spaces");
    		
    		registry.disposeRegistryView(view);
    		
            // Update the media properties with the FilterData
            PropertyHelper helper = new PropertyHelper(mediaProps);
            helper.put("FilterData",filterData.toArray());
            mediaProps = helper.toArray();

    	}
    	catch (com.sun.star.uno.Exception e) {
    		// Failed to get registry view, ignore
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
            if ((c>='a' && c<='z') || (c>='A' && c<='Z') || (c>='0' && c<='9')) {
                sResult += Character.toString(c);
            }
            else {
            	switch (c) {
            	case '.': sResult += "."; break;
            	case '-': sResult += "-"; break;
            	case ' ' : sResult += "-"; break;
            	case '_' : sResult += "-"; break;
            	// Replace accented and national characters
            	case '\u00c0' : sResult += "A"; break;
            	case '\u00c1' : sResult += "A"; break;
            	case '\u00c2' : sResult += "A"; break;
            	case '\u00c3' : sResult += "A"; break;
            	case '\u00c4' : sResult += "AE"; break;
            	case '\u00c5' : sResult += "AA"; break;
            	case '\u00c6' : sResult += "AE"; break;
            	case '\u00c7' : sResult += "C"; break;
            	case '\u00c8' : sResult += "E"; break;
            	case '\u00c9' : sResult += "E"; break;
            	case '\u00ca' : sResult += "E"; break;
            	case '\u00cb' : sResult += "E"; break;
            	case '\u00cc' : sResult += "I"; break;
            	case '\u00cd' : sResult += "I"; break;
            	case '\u00ce' : sResult += "I"; break;
            	case '\u00cf' : sResult += "I"; break;
            	case '\u00d0' : sResult += "D"; break;
            	case '\u00d1' : sResult += "N"; break;
            	case '\u00d2' : sResult += "O"; break;
            	case '\u00d3' : sResult += "O"; break;
            	case '\u00d4' : sResult += "O"; break;
            	case '\u00d5' : sResult += "O"; break;
            	case '\u00d6' : sResult += "OE"; break;
            	case '\u00d8' : sResult += "OE"; break;
            	case '\u00d9' : sResult += "U"; break;
            	case '\u00da' : sResult += "U"; break;
            	case '\u00db' : sResult += "U"; break;
            	case '\u00dc' : sResult += "UE"; break;
            	case '\u00dd' : sResult += "Y"; break;
            	case '\u00df' : sResult += "sz"; break;
            	case '\u00e0' : sResult += "a"; break;
            	case '\u00e1' : sResult += "a"; break;
            	case '\u00e2' : sResult += "a"; break;
            	case '\u00e3' : sResult += "a"; break;
            	case '\u00e4' : sResult += "ae"; break;
            	case '\u00e5' : sResult += "aa"; break;
            	case '\u00e6' : sResult += "ae"; break;
            	case '\u00e7' : sResult += "c"; break;
            	case '\u00e8' : sResult += "e"; break;
            	case '\u00e9' : sResult += "e"; break;
            	case '\u00ea' : sResult += "e"; break;
            	case '\u00eb' : sResult += "e"; break;
            	case '\u00ec' : sResult += "i"; break;
            	case '\u00ed' : sResult += "i"; break;
            	case '\u00ee' : sResult += "i"; break;
            	case '\u00ef' : sResult += "i"; break;
            	case '\u00f0' : sResult += "d"; break;
            	case '\u00f1' : sResult += "n"; break;
            	case '\u00f2' : sResult += "o"; break;
            	case '\u00f3' : sResult += "o"; break;
            	case '\u00f4' : sResult += "o"; break;
            	case '\u00f5' : sResult += "o"; break;
            	case '\u00f6' : sResult += "oe"; break;
            	case '\u00f8' : sResult += "oe"; break;
            	case '\u00f9' : sResult += "u"; break;
            	case '\u00fa' : sResult += "u"; break;
            	case '\u00fb' : sResult += "u"; break;
            	case '\u00fc' : sResult += "ue"; break;
            	case '\u00fd' : sResult += "y"; break;
            	case '\u00ff' : sResult += "y"; break;
            	}
            }
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