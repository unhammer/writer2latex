/************************************************************************
 *
 *  ConfigurationDialog.java
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
 *  Version 1.2 (2009-09-06)
 *
 */ 
 
package org.openoffice.da.comp.writer2latex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Vector;

import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XContainerWindowEventHandler;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XWindow;
import com.sun.star.beans.XPropertySet;
import com.sun.star.io.NotConnectedException;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.ucb.CommandAbortedException;
import com.sun.star.ucb.XSimpleFileAccess2;
import com.sun.star.ui.dialogs.ExecutableDialogResults;
import com.sun.star.ui.dialogs.XExecutableDialog;
import com.sun.star.ui.dialogs.XFilePicker;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XStringSubstitution;

import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.lib.uno.adapter.XInputStreamToInputStreamAdapter;
import com.sun.star.lib.uno.adapter.XOutputStreamToOutputStreamAdapter;

import writer2latex.api.Config;
import writer2latex.api.ConverterFactory;

import org.openoffice.da.comp.w2lcommon.helper.DialogAccess;

/** This class provides a uno component which implements the configuration
 *  of Writer2LaTeX. The same component is used for all pages - using the
 *  dialog title to distinguish between tha pages.
 */
public final class ConfigurationDialog extends WeakBase
    implements XServiceInfo, XContainerWindowEventHandler {

    //private XComponentContext xContext;
    private XSimpleFileAccess2 sfa2;
    private String sConfigFileName = null;
    Config config;
    private String sTitle = null;
    private DialogAccess dlg = null;
    
    /** The component will be registered under this name.
     */
    public static String __serviceName = "org.openoffice.da.writer2latex.ConfigurationDialog";

    /** The component should also have an implementation name.
     */
    public static String __implementationName = "org.openoffice.da.comp.writer2latex.ConfigurationDialog";

    /** Create a new ConfigurationDialog */
    public ConfigurationDialog(XComponentContext xContext) {
        //this.xContext = xContext;

        // Get the SimpleFileAccess service
        sfa2 = null;
        try {
            Object sfaObject = xContext.getServiceManager().createInstanceWithContext(
                "com.sun.star.ucb.SimpleFileAccess", xContext);
            sfa2 = (XSimpleFileAccess2) UnoRuntime.queryInterface(XSimpleFileAccess2.class, sfaObject);
        }
        catch (com.sun.star.uno.Exception e) {
            // failed to get SimpleFileAccess service (should not happen)
        }

        // Create the config file name
        XStringSubstitution xPathSub = null;
        try {
            Object psObject = xContext.getServiceManager().createInstanceWithContext(
               "com.sun.star.util.PathSubstitution", xContext);
            xPathSub = (XStringSubstitution) UnoRuntime.queryInterface(XStringSubstitution.class, psObject);
            sConfigFileName = xPathSub.substituteVariables("$(user)/writer2latex.xml", false);
        }
        catch (com.sun.star.uno.Exception e) {
            // failed to get PathSubstitution service (should not happen)
        }
        
        // Create the configuration
        config = ConverterFactory.createConverter("application/x-latex").getConfig();
    }
        	
    // Implement XContainerWindowEventHandler
    public boolean callHandlerMethod(XWindow xWindow, Object event, String sMethod)
        throws com.sun.star.lang.WrappedTargetException {
    	XDialog xDialog = (XDialog)UnoRuntime.queryInterface(XDialog.class, xWindow);
    	sTitle = xDialog.getTitle();
   		dlg = new DialogAccess(xDialog);

   		try {
            if (sMethod.equals("external_event") ){
                return handleExternalEvent(event);
            }
            else if (sMethod.equals("NoPreambleChange")) {
                enableDocumentclassControls();
                return true;
            }
            else if (sMethod.equals("ExportGeometryChange")) {
            	enablePagesControls();
                return true;
            }
            else if (sMethod.equals("ExportHeaderAndFooterChange")) {
            	enablePagesControls();
                return true;
            }
            else if (sMethod.equals("NoTablesChange")) {
            	enableTablesControls();
            	return true;
            }
            else if (sMethod.equals("UseSupertabularChange")) {
            	enableTablesControls();
            	return true;
            }
            else if (sMethod.equals("UseLongtableChange")) {
            	enableTablesControls();
            	return true;
            }
        }
        catch (com.sun.star.uno.RuntimeException e) {
            throw e;
        }
        catch (com.sun.star.uno.Exception e) {
            throw new com.sun.star.lang.WrappedTargetException(sMethod, this, e);
        }
        return false;
    }
	
    public String[] getSupportedMethodNames() {
        String[] sNames = { "external_event", "NoPreambleChange", "ExportGeometryChange", "ExportHeaderAndFooterChange", "NoTablesChange", "UseSupertabularChange", "UseLongtableChange" };
        return sNames;
    }
    
    // Implement the interface XServiceInfo
    public boolean supportsService(String sServiceName) {
        return sServiceName.equals(__serviceName);
    }

    public String getImplementationName() {
        return __implementationName;
    }
    
    public String[] getSupportedServiceNames() {
        String[] sSupportedServiceNames = { __serviceName };
        return sSupportedServiceNames;
    }
	
    // Private stuff    
    private boolean handleExternalEvent(Object aEventObject)
    throws com.sun.star.uno.Exception {
    	try {
    		String sMethod = AnyConverter.toString(aEventObject);
    		if (sMethod.equals("ok")) {
    			loadConfig();
    			getControls();
    			saveConfig();
    			return true;
    		} else if (sMethod.equals("back") || sMethod.equals("initialize")) {
    			loadConfig();
    			setControls();
    			return true;
    		}
    	}
    	catch (com.sun.star.lang.IllegalArgumentException e) {
    		throw new com.sun.star.lang.IllegalArgumentException(
    				"Method external_event requires a string in the event object argument.", this,(short) -1);
    	}
    	return false;
    }
    
	// Load the user configuration from file
    private void loadConfig() {
        if (sfa2!=null && sConfigFileName!=null) {
            try {
                XInputStream xIs = sfa2.openFileRead(sConfigFileName);
                if (xIs!=null) {
                    InputStream is = new XInputStreamToInputStreamAdapter(xIs);
                    config.read(is);
                    is.close();
                    xIs.closeInput();
                }
            }
            catch (IOException e) {
                // ignore
            }
            catch (NotConnectedException e) {
                // ignore
            }
            catch (CommandAbortedException e) {
                // ignore
            }
            catch (com.sun.star.uno.Exception e) {
                // ignore
            }
        }
    }
    
	// Save the user configuration
    private void saveConfig() {
        if (sfa2!=null && sConfigFileName!=null) {
            try {
            	// Remove the file if it exists
            	if (sfa2.exists(sConfigFileName)) {
            		sfa2.kill(sConfigFileName);
            	}
            	// Then write the new contents
                XOutputStream xOs = sfa2.openFileWrite(sConfigFileName);
                if (xOs!=null) {
                    OutputStream os = new XOutputStreamToOutputStreamAdapter(xOs);
                    config.write(os);
                    os.close();
                    xOs.closeOutput();
                }
            }
            catch (IOException e) {
                // ignore
            }
            catch (NotConnectedException e) {
                // ignore
            }
            catch (CommandAbortedException e) {
                // ignore
            }
            catch (com.sun.star.uno.Exception e) {
                // ignore
            }
        }
    }
    
	// Set controls based on the config
    private void setControls() {
    	if ("Documentclass".equals(sTitle)) {
    		loadDocumentclass();
    	}
    	else if ("Pages".equals(sTitle)) {
    		loadPages();
    	}
    	else if ("Tables".equals(sTitle)) {
    		loadTables();
    	}
    }
    
	// Change the config based on the controls
    private void getControls() {
    	if ("Documentclass".equals(sTitle)) {
    		saveDocumentclass();
    	}
    	else if ("Pages".equals(sTitle)) {
    		savePages();
    	}
    	else if ("Tables".equals(sTitle)) {
    		saveTables();
    	}
    }
    
    // The page "Documentclass"
    // This page handles the options no_preamble, documentclass, global_options and the custom-preamble
    
    private void loadDocumentclass() {
    	dlg.setCheckBoxStateAsBoolean("NoPreamble","true".equals(config.getOption("no_preamble")));
    	dlg.setTextFieldText("Documentclass",config.getOption("documentclass"));
    	dlg.setTextFieldText("GlobalOptions",config.getOption("global_options"));
    	//dlg.setTextFieldText("CustomPreamble",config.getLongOption("custom-preamble"));
    	enableDocumentclassControls();
    }
    
    private void saveDocumentclass() {
    	config.setOption("no_preamble", Boolean.toString(dlg.getCheckBoxStateAsBoolean("NoPreamble")));
    	config.setOption("documentclass", dlg.getTextFieldText("Documentclass"));
    	config.setOption("global_options", dlg.getTextFieldText("GlobalOptions"));
    	//config.setLongOption("custom-preamble", dlg.getTextFieldText("CustomPreamble"));
    }

    private void enableDocumentclassControls() {
    	boolean bPreamble = !dlg.getCheckBoxStateAsBoolean("NoPreamble");
    	dlg.setControlEnabled("DocumentclassLabel",bPreamble);
    	dlg.setControlEnabled("Documentclass",bPreamble);
    	dlg.setControlEnabled("GlobalOptionsLabel",bPreamble);
    	dlg.setControlEnabled("GlobalOptions",bPreamble);
    	dlg.setControlEnabled("CustomPreambleLabel",bPreamble);
    	dlg.setControlEnabled("CustomPreamble",bPreamble);
    }
    
    // The page "Pages"
    // This page handles the options page_formatting, use_geometry, use_fancyhdr, use_lastpage and use_endnotes
    
    private void loadPages() {
    	enablePagesControls();
    }
    
    private void savePages() {
    	
    }
    
    private void enablePagesControls() {
    	boolean bExportGeometry = dlg.getCheckBoxStateAsBoolean("ExportGeometry");
    	dlg.setControlEnabled("UseGeometry",bExportGeometry);

    	boolean bExport = dlg.getCheckBoxStateAsBoolean("ExportHeaderAndFooter");
    	dlg.setControlEnabled("UseFancyhdr",bExport);
    }
    
    // The page "Tables"
    // This page handles the options table_content, use_tabulary, use_colortbl, use_multirow, use_supertabular, use_longtable,
    // table_first_head_style, table_head_style, table_foot_style, table_last_foot_style
	// Limitation: Cannot handle the values "error" and "warning" for table_content
    
    private void loadTables() {
    	dlg.setCheckBoxStateAsBoolean("NoTables", !"accept".equals(config.getOption("table_content")));
    	dlg.setCheckBoxStateAsBoolean("UseTabulary", "true".equals(config.getOption("use_tabulary")));
    	//dlg.setCheckBoxStateAsBoolean("UseMultirow", "true".equals(config.getOption("use_multirow")));
    	dlg.setCheckBoxStateAsBoolean("UseSupertabular","true".equals(config.getOption("use_supertabular")));
    	dlg.setCheckBoxStateAsBoolean("UseLongtable", "true".equals(config.getOption("use_longtable")));
    	dlg.setTextFieldText("TableFirstHeadStyle", config.getOption("table_first_head_style"));
    	dlg.setTextFieldText("TableHeadStyle", config.getOption("table_head_style"));
    	dlg.setTextFieldText("TableFootStyle", config.getOption("table_foot_style"));
    	dlg.setTextFieldText("TableLastFootStyle", config.getOption("table_last_foot_style"));
    	dlg.setTextFieldText("TableSequenceName", config.getOption("table_sequence_name"));
    	enableTablesControls();
    }
    
    private void saveTables() {
    	config.setOption("table_content", dlg.getCheckBoxStateAsBoolean("NoTables") ? "ignore" : "accept");
    	config.setOption("use_tabulary", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseTabulary")));
    	//config.setOption("use_multirow", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseMultirow")));
    	config.setOption("use_supertabular", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseSupertabular")));
    	config.setOption("use_longtable", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseLongtable")));
    	config.setOption("table_first_head_style", dlg.getTextFieldText("TableFirstHeadStyle"));
    	config.setOption("table_head_style", dlg.getTextFieldText("TableHeadStyle"));
    	config.setOption("table_foot_style", dlg.getTextFieldText("TableFootStyle"));
    	config.setOption("table_last_foot_style", dlg.getTextFieldText("TableLastFootStyle"));
    	config.setOption("table_sequence_name", dlg.getTextFieldText("TableSequenceName"));
    }
    
    private void enableTablesControls() {
    	boolean bNoTables = dlg.getCheckBoxStateAsBoolean("NoTables");
    	boolean bSupertabular = dlg.getCheckBoxStateAsBoolean("UseSupertabular");
    	boolean bLongtable = dlg.getCheckBoxStateAsBoolean("UseLongtable");
    	dlg.setControlEnabled("UseTabulary", !bNoTables);
    	dlg.setControlEnabled("UseMultirow", false);
    	dlg.setControlEnabled("UseSupertabular", !bNoTables);
    	dlg.setControlEnabled("UseLongtable", !bNoTables && !bSupertabular);
    	dlg.setControlEnabled("TableFirstHeadLabel", !bNoTables && (bSupertabular || bLongtable));
    	dlg.setControlEnabled("TableFirstHeadStyle", !bNoTables && (bSupertabular || bLongtable));
    	dlg.setControlEnabled("TableHeadLabel", !bNoTables && (bSupertabular || bLongtable));
    	dlg.setControlEnabled("TableHeadStyle", !bNoTables && (bSupertabular || bLongtable));
    	dlg.setControlEnabled("TableFootLabel", !bNoTables && (bSupertabular || bLongtable));
    	dlg.setControlEnabled("TableFootStyle", !bNoTables && (bSupertabular || bLongtable));
    	dlg.setControlEnabled("TableLastFootLabel", !bNoTables && (bSupertabular || bLongtable));
    	dlg.setControlEnabled("TableLastFootStyle", !bNoTables && (bSupertabular || bLongtable));
    	dlg.setControlEnabled("TableSequenceLabel", !bNoTables);
    	dlg.setControlEnabled("TableSequenceName", !bNoTables);
    	
    }

	
}



