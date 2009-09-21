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
 *  Version 1.2 (2009-09-20)
 *
 */ 
 
package org.openoffice.da.comp.writer2latex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import com.sun.star.awt.XContainerWindowEventHandler;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.XWindow;
import com.sun.star.io.NotConnectedException;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.ucb.CommandAbortedException;
import com.sun.star.ucb.XSimpleFileAccess2;
import com.sun.star.ui.dialogs.ExecutableDialogResults;
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

    private XComponentContext xContext;
    private XSimpleFileAccess2 sfa2;
    private String sConfigFileName = null;
    Config config;
    String sCurrentText = null;
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
        this.xContext = xContext;

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
            // Documentclass page
            else if (sMethod.equals("NoPreambleChange")) {
                enableDocumentclassControls();
                return true;
            }
            // Headings page
            else if (sMethod.equals("MaxLevelChange")) {
                enableHeadingsControls();
                return true;
            }
            // Styles page
            // Formatting page
            else if (sMethod.equals("FormattingChange")) {
            	enableFormattingControls();
                return true;
            }
            else if (sMethod.equals("UseColorChange")) {
            	enableFormattingControls();
                return true;
            }
            else if (sMethod.equals("UseSoulChange")) {
            	enableFormattingControls();
                return true;
            }
            // Fonts page
            // Pages page
            else if (sMethod.equals("ExportGeometryChange")) {
            	enablePagesControls();
                return true;
            }
            else if (sMethod.equals("ExportHeaderAndFooterChange")) {
            	enablePagesControls();
                return true;
            }
            // Tables page
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
            // Figures page
            else if (sMethod.equals("NoImagesChange")) {
            	enableFiguresControls();
            	return true;
            }
            // Text and math page
            else if (sMethod.equals("NewSymbolClick")) {
            	appendItem("MathSymbolName");
            	return true;
            }
            else if (sMethod.equals("DeleteSymbolClick")) {
            	deleteCurrentItem("MathSymbolName");
            	return true;
            }
            else if (sMethod.equals("NewTextClick")) {
            	appendItem("TextInput");
            	return true;
            }
            else if (sMethod.equals("DeleteTextClick")) {
            	deleteCurrentItem("TextInput");
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
        String[] sNames = { "external_event", 
        		"NoPreambleChange", // Documentclass
        		"MaxLevelChange", // Headings
        		"FormattingChange", "UseColorChange", "UseSoulChange", // Formatting
        		"ExportGeometryChange", "ExportHeaderAndFooterChange", // Pages
        		"NoTablesChange", "UseSupertabularChange", "UseLongtableChange", // Tables
        		"NoImagesChange", // Images
        		"NewSymbolClick", "DeleteSymbolClick", "NewTextClick", "DeleteTextClick" // Text and Math
        };
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
    
    // Display a dialog
    private XDialog getDialog(String sDialogName) {
    	XMultiComponentFactory xMCF = xContext.getServiceManager();
    	try {
    		Object provider = xMCF.createInstanceWithContext(
    				"com.sun.star.awt.DialogProvider2", xContext);
    		XDialogProvider2 xDialogProvider = (XDialogProvider2)
    		UnoRuntime.queryInterface(XDialogProvider2.class, provider);
    		String sDialogUrl = "vnd.sun.star.script:"+sDialogName+"?location=application";
    		return xDialogProvider.createDialogWithHandler(sDialogUrl, this);
    	}
    	catch (Exception e) {
    		System.out.println(e.getMessage());
    		return null;
    	}
     }

    private boolean deleteItem() {
    	XDialog xDialog=getDialog("W2LDialogs2.DeleteDialog");
    	if (xDialog!=null) {
    		boolean bDelete = xDialog.execute()==ExecutableDialogResults.OK;
    		xDialog.endExecute();
    		return bDelete;
    	}
    	return false;
    }
    
    private void deleteCurrentItem(String sListName) {
    	String[] sItems = dlg.getListBoxStringItemList(sListName);
    	short nSelected = dlg.getListBoxSelectedItem(sListName);
    	if (nSelected>=0 && deleteItem()) {
    		int nOldLen = sItems.length;
    		String[] sNewItems = new String[nOldLen-1];
    		if (nSelected>0) {
    			System.arraycopy(sItems, 0, sNewItems, 0, nSelected);
    		}
    		if (nSelected<nOldLen-1) {
        		System.arraycopy(sItems, nSelected+1, sNewItems, nSelected, nOldLen-1-nSelected);
    		}
    		dlg.setListBoxStringItemList(sListName, sNewItems);
    		short nNewSelected = nSelected<nOldLen-1 ? nSelected : (short)(nSelected-1);
			dlg.setListBoxSelectedItem(sListName, nNewSelected);
    	}
    }
    
    private String newItem() {
    	XDialog xDialog=getDialog("W2LDialogs2.NewDialog");
    	if (xDialog!=null) {
    		String sResult = null;
    		if (xDialog.execute()==ExecutableDialogResults.OK) {
    			DialogAccess dlg = new DialogAccess(xDialog);
    			sResult = dlg.getTextFieldText("Name");
    		}
    		xDialog.endExecute();
    		return sResult;
    	}
    	return null;
    }
    
    private void appendItem(String sListName) {
    	String[] sItems = dlg.getListBoxStringItemList(sListName);
    	String sNewItem = newItem();
    	if (sNewItem!=null) {
    		int nOldLen = sItems.length;
    		String[] sNewItems = new String[nOldLen+1];
    		System.arraycopy(sItems, 0, sNewItems, 0, nOldLen);
    		sNewItems[nOldLen]=sNewItem;
    		dlg.setListBoxStringItemList(sListName, sNewItems);
    		dlg.setListBoxSelectedItem(sListName, (short)nOldLen);
    	}
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
    	else if ("Headings".equals(sTitle)) {
    		loadHeadings();
    	}
    	else if ("Styles".equals(sTitle)) {
    		loadStyles();
    	}
    	else if ("Formatting".equals(sTitle)) {
    		loadFormatting();
    	}
    	else if ("Fonts".equals(sTitle)) {
    		loadFonts();
    	}
    	else if ("Pages".equals(sTitle)) {
    		loadPages();
    	}
    	else if ("Tables".equals(sTitle)) {
    		loadTables();
    	}
    	else if ("Figures".equals(sTitle)) {
    		loadFigures();
    	}
    	else if ("TextAndMath".equals(sTitle)) {
    		loadTextAndMath();
    	}
    }
    
	// Change the config based on the controls
    private void getControls() {
    	if ("Documentclass".equals(sTitle)) {
    		saveDocumentclass();
    	}
    	else if ("Headings".equals(sTitle)) {
    		saveHeadings();
    	}
    	else if ("Styles".equals(sTitle)) {
    		saveStyles();
    	}
    	else if ("Formatting".equals(sTitle)) {
    		saveFormatting();
    	}
    	else if ("Fonts".equals(sTitle)) {
    		saveFonts();
    	}
    	else if ("Pages".equals(sTitle)) {
    		savePages();
    	}
    	else if ("Tables".equals(sTitle)) {
    		saveTables();
    	}
    	else if ("Figures".equals(sTitle)) {
    		saveFigures();
    	}
    	else if ("TextAndMath".equals(sTitle)) {
    		saveTextAndMath();
    	}    	
    }
    
    // The page "Documentclass"
    // This page handles the options no_preamble, documentclass, global_options and the custom-preamble
    
    private void loadDocumentclass() {
    	dlg.setCheckBoxStateAsBoolean("NoPreamble","true".equals(config.getOption("no_preamble")));
    	dlg.setTextFieldText("Documentclass",config.getOption("documentclass"));
    	dlg.setTextFieldText("GlobalOptions",config.getOption("global_options"));
    	//TODO: dlg.setTextFieldText("CustomPreamble",config.getLongOption("custom-preamble"));
    	enableDocumentclassControls();
    }
    
    private void saveDocumentclass() {
    	config.setOption("no_preamble", Boolean.toString(dlg.getCheckBoxStateAsBoolean("NoPreamble")));
    	config.setOption("documentclass", dlg.getTextFieldText("Documentclass"));
    	config.setOption("global_options", dlg.getTextFieldText("GlobalOptions"));
    	//TODO: config.setLongOption("custom-preamble", dlg.getTextFieldText("CustomPreamble"));
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
    
    // The page "Headings"
    // This page handles the heading map as well as the option no_index
    
    private void loadHeadings() {
    	// TODO: Load heading map
    	dlg.setCheckBoxStateAsBoolean("NoIndex","true".equals(config.getOption("no_index")));
    	enableHeadingsControls();
    }
    
    private void saveHeadings() {
    	// TODO: Save heading map
    	config.setOption("no_index", Boolean.toString(dlg.getCheckBoxStateAsBoolean("NoIndex")));    	
    }
    
    private void enableHeadingsControls() {
    	boolean bEnable = dlg.getListBoxSelectedItem("MaxLevel")>0;
    	dlg.setControlEnabled("WriterLevelLabel", bEnable);
    	dlg.setControlEnabled("WriterLevel", bEnable);
    	dlg.setControlEnabled("LaTeXLevelLabel", bEnable);
    	dlg.setControlEnabled("LaTeXLevel", bEnable);
    	dlg.setControlEnabled("LaTeXNameLabel", bEnable);
    	dlg.setControlEnabled("LaTeXName", bEnable);
    }
    
    // The page "Styles"
    // This page handles the various style maps as well as the option other_styles
    
    private void loadStyles() {
    	// TODO
    	enableStylesControls();
    }
    
    private void saveStyles() {
    	// TODO
    }
    
    private void enableStylesControls() {
    	// TODO
    }
    
    // The page "Formatting"
    // This page handles the options formatting, use_color, use_colortbl, use_soul, use_ulem,
    // use_hyperref, use_titlesec, use_titletoc
    
    private void loadFormatting() {
    	String sFormatting = config.getOption("formatting");
    	if ("ignore_all".equals(sFormatting)) {
    		dlg.setListBoxSelectedItem("Formatting", (short)0);
    	}
    	else if ("ignore_most".equals(sFormatting)) {
    		dlg.setListBoxSelectedItem("Formatting", (short)1);
    	}
    	else if ("convert_most".equals(sFormatting)) {
    		dlg.setListBoxSelectedItem("Formatting", (short)3);
    	}
    	else if ("convert_all".equals(sFormatting)) {
    		dlg.setListBoxSelectedItem("Formatting", (short)4);
    	}
    	else {
    		dlg.setListBoxSelectedItem("Formatting", (short)2);
    	}
    	
    	dlg.setCheckBoxStateAsBoolean("UseHyperref","true".equals(config.getOption("use_hyperref")));
    	dlg.setCheckBoxStateAsBoolean("UseColor","true".equals(config.getOption("use_color")));
    	dlg.setCheckBoxStateAsBoolean("UseColortbl","true".equals(config.getOption("use_colortbl")));
    	dlg.setCheckBoxStateAsBoolean("UseSoul","true".equals(config.getOption("use_soul")));
    	dlg.setCheckBoxStateAsBoolean("UseUlem","true".equals(config.getOption("use_ulem")));
    	dlg.setCheckBoxStateAsBoolean("UseTitlesec","true".equals(config.getOption("use_titlesec")));
    	dlg.setCheckBoxStateAsBoolean("UseTitletoc","true".equals(config.getOption("use_titletoc")));
    	
    	enableFormattingControls();
    }
    
    private void saveFormatting() {
    	switch (dlg.getListBoxSelectedItem("Formatting")) {
    	case 0: config.setOption("formatting", "ignore_all"); break;
    	case 1: config.setOption("formatting", "ignore_most"); break;
    	case 2: config.setOption("formatting", "convert_basic"); break;
    	case 3: config.setOption("formatting", "convert_most"); break;
    	case 4: config.setOption("formatting", "convert_all");
    	}
    	
    	config.setOption("use_hyperref", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseHyperref")));    	
    	config.setOption("use_color", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseColor")));    	
    	config.setOption("use_colortbl", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseColortbl")));    	
    	config.setOption("use_soul", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseSoul")));    	
    	config.setOption("use_ulem", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseUlem")));    	
    	config.setOption("use_titlesec", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseTitlesec")));    	
    	config.setOption("use_titletoc", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseTitletoc")));    		
    }
    
    private void enableFormattingControls() {
    	short nFormatting = dlg.getListBoxSelectedItem("Formatting");
    	boolean bUseColor = dlg.getCheckBoxStateAsBoolean("UseColor");
    	//boolean bUseSoul = dlg.getCheckBoxStateAsBoolean("UseSoul");
    	    	
    	dlg.setControlEnabled("UseColor", nFormatting>0);
    	dlg.setControlEnabled("UseColortbl", nFormatting>0 && bUseColor);
    	// Until implemented...
    	dlg.setControlEnabled("UseSoul", false);
    	dlg.setControlEnabled("UseUlem", nFormatting>0);
    	dlg.setControlEnabled("UseTitlesec", false);
    	dlg.setControlEnabled("UseTitletoc", false);
    	// After which it should be...
    	//dlg.setControlEnabled("UseSoul", nFormatting>0);
    	//dlg.setControlEnabled("UseUlem", nFormatting>0 && !bUseSoul);
    	//dlg.setControlEnabled("UseTitlesec", nFormatting>2);
    	//dlg.setControlEnabled("UseTitletoc", nFormatting>2);
    }
    
    // The page "Fonts"
    // This page handles the options use_fontspec, use_pifont, use_tipa, use_eurosym, use_wasysym,
    // use_ifsym, use_bbding
    
    private void loadFonts() {
    	System.out.println("Loading fonts, f.eks. use_pifont="+config.getOption("use_pifont"));
    	dlg.setCheckBoxStateAsBoolean("UseFontspec","true".equals(config.getOption("use_fontspec")));
    	dlg.setCheckBoxStateAsBoolean("UsePifont","true".equals(config.getOption("use_pifont")));
    	dlg.setCheckBoxStateAsBoolean("UseTipa","true".equals(config.getOption("use_tipa")));
    	dlg.setCheckBoxStateAsBoolean("UseEurosym","true".equals(config.getOption("use_eurosym")));
    	dlg.setCheckBoxStateAsBoolean("UseWasysym","true".equals(config.getOption("use_wasysym")));
    	dlg.setCheckBoxStateAsBoolean("UseIfsym","true".equals(config.getOption("use_ifsym")));
    	dlg.setCheckBoxStateAsBoolean("UseBbding","true".equals(config.getOption("use_bbding")));
    	
    	enableFontsControls();
    }
    
    private void saveFonts() {    	
    	config.setOption("use_fontspec", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseFontspec")));    	
    	config.setOption("use_pifont", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UsePifont")));    	
    	config.setOption("use_tipa", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseTipa")));    	
    	config.setOption("use_eurosym", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseEurosym")));    	
    	config.setOption("use_wasysym", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseWasysym")));    	
    	config.setOption("use_ifsym", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseIfsym")));    	
    	config.setOption("use_bbding", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseBbding")));    	    	
    }
    
    private void enableFontsControls() {
    	// Until implemented...
    	dlg.setControlEnabled("UseFontspec", false);
    	// Nothing to do
    }
    
    // The page "Pages"
    // This page handles the options page_formatting, use_geometry, use_fancyhdr, use_lastpage and use_endnotes
    
    private void loadPages() {
    	String sPageFormatting = config.getOption("page_formatting");
    	if ("ignore_all".equals(sPageFormatting)) {
    		dlg.setCheckBoxStateAsBoolean("ExportGeometry", false);
    		dlg.setCheckBoxStateAsBoolean("ExportHeaderFooter", false);
    	}
    	else if ("convert_geometry".equals(sPageFormatting)) {
    		dlg.setCheckBoxStateAsBoolean("ExportGeometry", true);
    		dlg.setCheckBoxStateAsBoolean("ExportHeaderFooter", false);
    	}
    	else if ("convert_header_footer".equals(sPageFormatting)) {
    		dlg.setCheckBoxStateAsBoolean("ExportGeometry", false);
    		dlg.setCheckBoxStateAsBoolean("ExportHeaderFooter", true);
    	}
    	else if ("convert_all".equals(sPageFormatting)) {
    		dlg.setCheckBoxStateAsBoolean("ExportGeometry", true);
    		dlg.setCheckBoxStateAsBoolean("ExportHeaderFooter", true);
    	}
    	
    	dlg.setCheckBoxStateAsBoolean("UseGeometry", "true".equals(config.getOption("use_geometry")));
    	dlg.setCheckBoxStateAsBoolean("UseFancyhdr", "true".equals(config.getOption("use_fancyhdr")));
    	dlg.setCheckBoxStateAsBoolean("UseLastpage", "true".equals(config.getOption("use_lastpage")));
    	dlg.setCheckBoxStateAsBoolean("UseEndnotes", "true".equals(config.getOption("use_endnotes")));
    	
    	enablePagesControls();
    }
    
    private void savePages() {
    	boolean bGeometry = dlg.getCheckBoxStateAsBoolean("ExportGeometry");
    	boolean bHeaderFooter = dlg.getCheckBoxStateAsBoolean("ExportHeaderFooter");
    	if (bGeometry && bHeaderFooter) {
    		config.setOption("page_formatting", "convert_all");
    	}
    	else if (bGeometry && !bHeaderFooter) {
    		config.setOption("page_formatting", "convert_geometry");
    	}
    	else if (!bGeometry && bHeaderFooter) {
    		config.setOption("page_formatting", "convert_header_footer");
    	}
    	else {
    		config.setOption("page_formatting", "ignore_all");
    	}
    	
    	config.setOption("use_geometry", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseGeometry")));
    	config.setOption("use_fancyhdr", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseFancyhdr")));
    	config.setOption("use_lastpage", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseLastpage")));
    	config.setOption("use_endnotes", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseEndnotes")));
    }
    
    private void enablePagesControls() {
    	boolean bExportGeometry = dlg.getCheckBoxStateAsBoolean("ExportGeometry");
    	dlg.setControlEnabled("UseGeometry",bExportGeometry);

    	boolean bExport = dlg.getCheckBoxStateAsBoolean("ExportHeaderFooter");
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
    
    // The page "Figures"
    // This page handles the options use_caption, align_frames, figure_sequence_name, image_content,
    // remove_graphics_extension and image_options
	// Limitation: Cannot handle the values "error" and "warning" for image_content
    
    private void loadFigures() {
    	dlg.setCheckBoxStateAsBoolean("UseCaption", "true".equals(config.getOption("use_caption")));
    	dlg.setCheckBoxStateAsBoolean("AlignFrames", "true".equals(config.getOption("align_frames")));
    	dlg.setTextFieldText("FigureSequenceName", config.getOption("figure_sequence_name"));
    	dlg.setCheckBoxStateAsBoolean("NoImages", !"accept".equals(config.getOption("image_content")));
    	dlg.setCheckBoxStateAsBoolean("RemoveGraphicsExtension", "true".equals(config.getOption("remove_graphics_extension")));
    	dlg.setTextFieldText("ImageOptions", config.getOption("image_options"));
    	enableFiguresControls();
     }
    
    private void saveFigures() {
    	config.setOption("use_caption", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseCaption")));
    	config.setOption("align_frames", Boolean.toString(dlg.getCheckBoxStateAsBoolean("AlignFrames")));
    	config.setOption("figure_sequence_name", dlg.getTextFieldText("FigureSequenceName"));
    	config.setOption("image_content", dlg.getCheckBoxStateAsBoolean("NoImages") ? "ignore" : "accept");
    	config.setOption("remove_graphics_extension", Boolean.toString(dlg.getCheckBoxStateAsBoolean("RemoveGraphicsExtension")));
    	config.setOption("image_options", dlg.getTextFieldText("ImageOptions"));
    }

    private void enableFiguresControls() {
    	boolean bNoImages = dlg.getCheckBoxStateAsBoolean("NoImages");
    	dlg.setControlEnabled("RemoveGraphicsExtension", !bNoImages);
    	dlg.setControlEnabled("ImageOptionsLabel", !bNoImages);
    	dlg.setControlEnabled("ImageOptions", !bNoImages);
    }
    
    // The page "TextAndMath"
    // This page handles the options use_ooomath and tabstop as well as the 
    // text replacements and math symbol definitions
    
    private void loadTextAndMath() {
    	Set<String> names = config.getComplexOptions("string-replace");
    	String[] sNames = new String[names.size()];
    	int i=0;
    	for (String s : names) {
    		sNames[i++] = s;
    	}
    	System.out.println("Found "+sNames.length+" string replacements");
    	dlg.setListBoxStringItemList("TextInput", sNames);
    	dlg.setListBoxSelectedItem("TextInput", (short)0);
    	enableTextAndMathControls();
    }
    
    private void saveTextAndMath() {
    	
    }
    
    private void enableTextAndMathControls() {
    	
    }
    


	
}



