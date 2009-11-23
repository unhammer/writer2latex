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
 *  Version 1.2 (2009-11-19)
 *
 */ 
 
package org.openoffice.da.comp.writer2latex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

import writer2latex.api.ComplexOption;
import writer2latex.api.Config;
import writer2latex.api.ConverterFactory;

import org.openoffice.da.comp.w2lcommon.helper.DialogAccess;
import org.openoffice.da.comp.w2lcommon.helper.StyleNameProvider;

/** This class provides a uno component which implements the configuration
 *  of Writer2LaTeX. The same component is used for all pages - using the
 *  dialog title to distinguish between tha pages.
 */
public final class ConfigurationDialog extends WeakBase
    implements XServiceInfo, XContainerWindowEventHandler {
	
	private String[] sFamilyNames = { "text", "paragraph", "paragraph-block", "list", "listitem" };
	private String[] sOOoFamilyNames = { "CharacterStyles", "ParagraphStyles", "ParagraphStyles", "NumberingStyles", "NumberingStyles" };
	private String[] sAttributeNames = { "bold", "italics", "small-caps", "superscript", "subscipt" };

    private XComponentContext xContext;
    private XSimpleFileAccess2 sfa2;
    private String sConfigFileName = null;
    Config config;
    // Local cache of complex options
    ComplexOption[] styleMap;
    ComplexOption attributeMap;
    ComplexOption headingMap;
    ComplexOption mathSymbols;
    ComplexOption stringReplace;
    short nCurrentFamily = -1;
    String sCurrentStyleName = null;
    short nCurrentAttribute = -1;
    short nCurrentWriterLevel = 0;
    String sCurrentMathSymbol = null;
    String sCurrentText = null;
    private String sTitle = null;
    private DialogAccess dlg = null;
    private StyleNameProvider styleNameProvider = null;
    private CustomSymbolNameProvider customSymbolNameProvider = null;
    
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
        
        // Initialize the local cache of complex options
        styleMap = new ComplexOption[5];
        for (int i=0; i<5; i++) { styleMap[i]=new ComplexOption(); }
        attributeMap = new ComplexOption();
        headingMap = new ComplexOption();
        mathSymbols = new ComplexOption();
        stringReplace = new ComplexOption();
        
        styleNameProvider = new StyleNameProvider(xContext);
        customSymbolNameProvider = new CustomSymbolNameProvider(xContext);
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
                updateDocumentclassControls();
                return true;
            }
            // Headings page
            else if (sMethod.equals("MaxLevelChange")) {
                updateHeadingsControls();
                return true;
            }
            else if (sMethod.equals("WriterLevelChange")) {
                updateHeadingsControls();
                return true;
            }
            else if (sMethod.equals("NoIndexChange")) {
                updateHeadingsControls();
                return true;
            }
            // Styles page
            else if (sMethod.equals("StyleFamilyChange")) {
            	updateStylesControls();
            	return true;
            }
            else if (sMethod.equals("StyleNameChange")) {
            	updateStylesControls();
            	return true;
            }
            else if (sMethod.equals("NewStyleClick")) {
            	newStyleClick();
            	return true;
            }
            else if (sMethod.equals("DeleteStyleClick")) {
            	deleteStyleClick();
            	return true;
            }
            else if (sMethod.equals("AddNextClick")) {
            	addNextClick();
            	return true;
            }
            else if (sMethod.equals("RemoveNextClick")) {
            	removeNextClick();
            	return true;
            }
            else if (sMethod.equals("LoadDefaultsClick")) {
            	loadDefaultsClick();
            	return true;
            }
            // Characters page
            else if (sMethod.equals("UseSoulChange")) {
            	updateCharactersControls();
                return true;
            }
            else if (sMethod.equals("FormattingAttributeChange")) {
            	updateCharactersControls();
                return true;
            }
            else if (sMethod.equals("CustomAttributeChange")) {
            	updateCharactersControls();
                return true;
            }
            // Fonts page
            // Pages page
            else if (sMethod.equals("ExportGeometryChange")) {
            	updatePagesControls();
                return true;
            }
            else if (sMethod.equals("ExportHeaderAndFooterChange")) {
            	updatePagesControls();
                return true;
            }
            // Tables page
            else if (sMethod.equals("NoTablesChange")) {
            	updateTablesControls();
            	return true;
            }
            else if (sMethod.equals("UseSupertabularChange")) {
            	updateTablesControls();
            	return true;
            }
            else if (sMethod.equals("UseLongtableChange")) {
            	updateTablesControls();
            	return true;
            }
            // Figures page
            else if (sMethod.equals("NoImagesChange")) {
            	updateFiguresControls();
            	return true;
            }
            // Text and math page
            else if (sMethod.equals("MathSymbolNameChange")) {
            	updateTextAndMathControls();
            	return true;
            }
            else if (sMethod.equals("NewSymbolClick")) {
            	newSymbolClick();
            	return true;
            }
            else if (sMethod.equals("DeleteSymbolClick")) {
            	deleteSymbolClick();
            	return true;
            }
            else if (sMethod.equals("TextInputChange")) {
            	updateTextAndMathControls();
            	return true;
            }
            else if (sMethod.equals("NewTextClick")) {
            	newTextClick();
            	return true;
            }
            else if (sMethod.equals("DeleteTextClick")) {
            	deleteTextClick();
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
        		"MaxLevelChange", "WriterLevelChange", "NoIndexChange", // Headings
        		"StyleFamilyChange", "StyleNameChange", "NewStyleClick", "DeleteStyleClick", "AddNextClick", "RemoveNextClick", "LoadDefaultsClick", // Styles
        		"UseSoulChange", "FormattingAttributeChange", "CustomAttributeChange", // Characters
        		"ExportGeometryChange", "ExportHeaderAndFooterChange", // Pages
        		"NoTablesChange", "UseSupertabularChange", "UseLongtableChange", // Tables
        		"NoImagesChange", // Images
        		"MathSymbolNameChange", "NewSymbolClick", "DeleteSymbolClick",
        		"TextInputChange", "NewTextClick", "DeleteTextClick" // Text and Math
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
    		}
    		else if (sMethod.equals("back") || sMethod.equals("initialize")) {
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
    		return null;
    	}
     }

    private boolean deleteItem(String sName) {
    	XDialog xDialog=getDialog("W2LDialogs2.DeleteDialog");
    	if (xDialog!=null) {
    		DialogAccess ddlg = new DialogAccess(xDialog);
    		String sLabel = ddlg.getLabelText("DeleteLabel");
    		sLabel = sLabel.replaceAll("%s", sName);
    		ddlg.setLabelText("DeleteLabel", sLabel);
    		boolean bDelete = xDialog.execute()==ExecutableDialogResults.OK;
    		xDialog.endExecute();
    		return bDelete;
    	}
    	return false;
    }
    
    private boolean deleteCurrentItem(String sListName) {
    	String[] sItems = dlg.getListBoxStringItemList(sListName);
    	short nSelected = dlg.getListBoxSelectedItem(sListName);
    	if (nSelected>=0 && deleteItem(sItems[nSelected])) {
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
			return true;
    	}
    	return false;
    }
    
    private String newItem(Set<String> suggestions) {
    	XDialog xDialog=getDialog("W2LDialogs2.NewDialog");
    	if (xDialog!=null) {
    		int nCount = suggestions.size();
    		String[] sItems = new String[nCount];
    		int i=0;
    		for (String s : suggestions) {
    			sItems[i++] = s;
    		}
    		sortStringArray(sItems);
    		DialogAccess ndlg = new DialogAccess(xDialog);
    		ndlg.setListBoxStringItemList("Name", sItems);
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
    
    private String appendItem(String sListName, Set<String> suggestions) {
    	String[] sItems = dlg.getListBoxStringItemList(sListName);
    	String sNewItem = newItem(suggestions);
    	if (sNewItem!=null) {
    		int nOldLen = sItems.length;
    		for (short i=0; i<nOldLen; i++) {
    			if (sNewItem.equals(sItems[i])) {
    				// Item already exists, select the existing one
    				dlg.setListBoxSelectedItem(sListName, i);
    				return null;
    			}
    		}
    		String[] sNewItems = new String[nOldLen+1];
    		System.arraycopy(sItems, 0, sNewItems, 0, nOldLen);
    		sNewItems[nOldLen]=sNewItem;
    		dlg.setListBoxStringItemList(sListName, sNewItems);
    		dlg.setListBoxSelectedItem(sListName, (short)nOldLen);
    	}
    	return sNewItem;
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
    	else if ("Characters".equals(sTitle)) {
    		loadCharacters();
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
    	else if ("Characters".equals(sTitle)) {
    		saveCharacters();
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
    	dlg.setTextFieldText("CustomPreamble",config.getOption("custom-preamble"));
    	updateDocumentclassControls();
    }
    
    private void saveDocumentclass() {
    	config.setOption("no_preamble", Boolean.toString(dlg.getCheckBoxStateAsBoolean("NoPreamble")));
    	config.setOption("documentclass", dlg.getTextFieldText("Documentclass"));
    	config.setOption("global_options", dlg.getTextFieldText("GlobalOptions"));
    	config.setOption("custom-preamble", dlg.getTextFieldText("CustomPreamble"));
    }

    private void updateDocumentclassControls() {
    	boolean bPreamble = !dlg.getCheckBoxStateAsBoolean("NoPreamble");
    	dlg.setControlEnabled("DocumentclassLabel",bPreamble);
    	dlg.setControlEnabled("Documentclass",bPreamble);
    	dlg.setControlEnabled("GlobalOptionsLabel",bPreamble);
    	dlg.setControlEnabled("GlobalOptions",bPreamble);
    	dlg.setControlEnabled("CustomPreambleLabel",bPreamble);
    	dlg.setControlEnabled("CustomPreamble",bPreamble);
    }
    
    // The page "Headings"
    // This page handles the heading map as well as the options no_index, use_titlesec and use_titletoc
    
    private void loadHeadings() {
    	// Load heading map from config
		headingMap.clear();
		headingMap.copyAll(config.getComplexOption("heading-map"));
		nCurrentWriterLevel = -1;
    	
    	// Determine the max level (from 0 to 10)
    	short nMaxLevel = 0;
    	while(nMaxLevel<10 && headingMap.containsKey(Integer.toString(nMaxLevel+1))) {
    		nMaxLevel++;
    	}
    	dlg.setListBoxSelectedItem("MaxLevel", nMaxLevel);
    	
    	dlg.setCheckBoxStateAsBoolean("UseTitlesec","true".equals(config.getOption("use_titlesec")));
    	dlg.setCheckBoxStateAsBoolean("NoIndex","true".equals(config.getOption("no_index")));
    	dlg.setCheckBoxStateAsBoolean("UseTitletoc","true".equals(config.getOption("use_titletoc")));

    	updateHeadingsControls();
    }
    
    private void saveHeadings() {
    	updateHeadingMap();
    	
    	// Save heading map to config
    	config.getComplexOption("heading-map").clear();
    	int nMaxLevel = dlg.getListBoxSelectedItem("MaxLevel");
		for (int i=1; i<=nMaxLevel; i++) {
			String sLevel = Integer.toString(i);
			config.getComplexOption("heading-map").copy(sLevel,headingMap.get(sLevel));
		}

    	// Save other controls to config
		config.setOption("use_titlesec", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseTitlesec")));    	
    	config.setOption("no_index", Boolean.toString(dlg.getCheckBoxStateAsBoolean("NoIndex")));
    	config.setOption("use_titletoc", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseTitletoc")));    		
    }
    
    private void updateHeadingMap() {
    	// Save the current writer level in our cache
    	if (nCurrentWriterLevel>-1) {
    		Map<String,String> attr = new HashMap<String,String>();
    		attr.put("name", dlg.getComboBoxText("LaTeXName"));
    		attr.put("level", dlg.getComboBoxText("LaTeXLevel"));
    		headingMap.put(Integer.toString(nCurrentWriterLevel+1), attr);
    	}
    }
    
    private void updateHeadingsControls() {
    	updateHeadingMap();
    	
    	// Adjust the presented writer levels to the max level
    	int nMaxLevel = dlg.getListBoxSelectedItem("MaxLevel");
    	nCurrentWriterLevel = dlg.getListBoxSelectedItem("WriterLevel");
    	String[] sWriterLevels = new String[nMaxLevel];
    	for (int i=0; i<nMaxLevel; i++) {
    		sWriterLevels[i]=Integer.toString(i+1);
    	}
    	dlg.setListBoxStringItemList("WriterLevel", sWriterLevels);
    	if (nCurrentWriterLevel+1>nMaxLevel) { nCurrentWriterLevel = (short)(nMaxLevel-1); }
    	else if (nCurrentWriterLevel<0 && nMaxLevel>0) { nCurrentWriterLevel=0; }
    	dlg.setListBoxSelectedItem("WriterLevel", nCurrentWriterLevel);
    	
    	// Load the values for the current level
    	if (nCurrentWriterLevel>-1) {
    		String sLevel = Integer.toString(nCurrentWriterLevel+1);
    		if (headingMap.containsKey(sLevel)) {
    			Map<String,String> attr = headingMap.get(sLevel);
    			dlg.setComboBoxText("LaTeXLevel", attr.containsKey("level") ? attr.get("level") : "");
    			dlg.setComboBoxText("LaTeXName", attr.containsKey("name") ? attr.get("name") : "");
    		}
    		else {
    			dlg.setListBoxSelectedItem("LaTeXLevel", (short)2);
    			dlg.setComboBoxText("LaTeXName", "");
    		}
    	}

    	boolean bupdate = dlg.getListBoxSelectedItem("MaxLevel")>0;
    	dlg.setControlEnabled("WriterLevelLabel", bupdate);
    	dlg.setControlEnabled("WriterLevel", bupdate);
    	dlg.setControlEnabled("LaTeXLevelLabel", bupdate);
    	dlg.setControlEnabled("LaTeXLevel", bupdate);
    	dlg.setControlEnabled("LaTeXNameLabel", bupdate);
    	dlg.setControlEnabled("LaTeXName", bupdate);
    	// Until implemented:
    	dlg.setControlEnabled("UseTitlesec", false);
    	//dlg.setControlEnabled("UseTitlesec", bupdate);

    	// Until implemented:
    	dlg.setControlEnabled("UseTitletoc", false);
    	//boolean bNoIndex = dlg.getCheckBoxStateAsBoolean("NoIndex");
    	//dlg.setControlEnabled("UseTitletoc", !bNoIndex);
    }
    
    // The page "Styles"
    // This page handles the various style maps as well as the options other_styles and formatting
	// Limitation: Cannot handle the values "error" and "warning" for other_styles
    
    private void loadStyles() {
    	// Display paragraph maps first
    	dlg.setListBoxSelectedItem("StyleFamily", (short)1);
    	nCurrentFamily = -1;
    	sCurrentStyleName = null;
    	
    	// Load style maps from config (translating keys to display names)
		for (int i=0; i<5; i++) {
			ComplexOption configMap = config.getComplexOption(sFamilyNames[i]+"-map"); 
			styleMap[i].clear();
	    	Map<String,String> displayNames = styleNameProvider.getDisplayNames(sOOoFamilyNames[i]);
			copyStyles(configMap, styleMap[i], displayNames);
		}
    	
		// Load other controls from config
		String sOtherStyles = config.getOption("other_styles");
		if ("accept".equals(sOtherStyles)) {
			dlg.setListBoxSelectedItem("OtherStyles", (short)1);
		}
		else {
			dlg.setListBoxSelectedItem("OtherStyles", (short)0);
		}

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

    	updateStylesControls();
    }
    
    private void saveStyles() {
    	updateStyleMaps();
    	
    	// Save style maps to config (translating keys back to internal names)
		for (int i=0; i<5; i++) {
			ComplexOption configMap = config.getComplexOption(sFamilyNames[i]+"-map"); 
			configMap.clear();
			Map<String,String> internalNames = styleNameProvider.getInternalNames(sOOoFamilyNames[i]);
			copyStyles(styleMap[i], configMap, internalNames);
		}

		// Save other controls to config
		switch (dlg.getListBoxSelectedItem("OtherStyles")) {
		case 0: config.setOption("other_styles", "ignore"); break;
		case 1: config.setOption("other_styles", "accept");
		}
    	
    	switch (dlg.getListBoxSelectedItem("Formatting")) {
    	case 0: config.setOption("formatting", "ignore_all"); break;
    	case 1: config.setOption("formatting", "ignore_most"); break;
    	case 2: config.setOption("formatting", "convert_basic"); break;
    	case 3: config.setOption("formatting", "convert_most"); break;
    	case 4: config.setOption("formatting", "convert_all");
    	}
    }
    
    private void updateStyleMaps() {
    	// Save the current style map, if any
    	if (nCurrentFamily>-1 && sCurrentStyleName!=null) {
    		Map<String,String> attr=new HashMap<String,String>();
    		attr.put("before", dlg.getTextFieldText("Before"));
    		attr.put("after", dlg.getTextFieldText("After"));
    		if (dlg.getControlEnabled("Next")) {
    			String[] sNextItems = dlg.getListBoxStringItemList("Next");
    			int nCount = sNextItems.length;
    			String sList = "";
    			for (int i=0; i<nCount; i++) {
    				if (i>0) sList+=";";
    				sList+=sNextItems[i];
    			}
    			attr.put("next", sList);
    		}
    		if (dlg.getControlEnabled("LineBreak")) {
    			attr.put("line-break", Boolean.toString(dlg.getCheckBoxStateAsBoolean("LineBreak")));
    		}
    		if (dlg.getControlEnabled("Verbatim")) {
    			attr.put("verbatim", Boolean.toString(dlg.getCheckBoxStateAsBoolean("Verbatim")));
    		}
    		styleMap[nCurrentFamily].put(sCurrentStyleName, attr);
    	}
    }
    
    private void updateStylesControls() {
    	updateStyleMaps();
    	
    	short nNewFamily = dlg.getListBoxSelectedItem("StyleFamily");
    	if (nNewFamily>-1 && nNewFamily!=nCurrentFamily) {
    		// The user has changed the family; load and display the corresponding style names
    		Set<String> items = styleMap[nNewFamily].keySet();
        	String[] sStyleNames = new String[items.size()];
        	int i=0;
        	for (String s : items) {
        		sStyleNames[i++] = s;
        	}
        	sortStringArray(sStyleNames);
        	dlg.setListBoxStringItemList("StyleName", sStyleNames);
        	dlg.setListBoxSelectedItem("StyleName", (short)Math.min(sStyleNames.length-1, 0));
        	dlg.setControlEnabled("NextLabel", nNewFamily==2);
        	dlg.setControlEnabled("Next", nNewFamily==2);
        	dlg.setControlEnabled("AddNextButton", nNewFamily==2);
        	dlg.setControlEnabled("RemoveNextButton", nNewFamily==2);
        	dlg.setControlEnabled("Verbatim", nNewFamily<2);
        	dlg.setControlEnabled("LineBreak", nNewFamily==1);
        	nCurrentFamily = nNewFamily;
    	}
    	
    	if (nCurrentFamily>-1) {
    		short nStyleNameItem = dlg.getListBoxSelectedItem("StyleName");
    		if (nStyleNameItem>=0) {
    			sCurrentStyleName = dlg.getListBoxStringItemList("StyleName")[nStyleNameItem];
    			
    			Map<String,String> attr = styleMap[nCurrentFamily].get(sCurrentStyleName);
    			dlg.setTextFieldText("Before", attr.containsKey("before") ? attr.get("before") : "");
    			dlg.setTextFieldText("After", attr.containsKey("after") ? attr.get("after") : "");
    			String[] sNextItems;
    			if (attr.containsKey("next")) {
    				sNextItems = attr.get("next").split(";");
    			}
    			else {
    				sNextItems = new String[0];
    			}
    			dlg.setListBoxStringItemList("Next", sNextItems);
            	dlg.setListBoxSelectedItem("Next", (short)Math.min(sNextItems.length-1, 0));
    			dlg.setCheckBoxStateAsBoolean("Verbatim", 
    					attr.containsKey("verbatim") ? "true".equals(attr.get("verbatim")) : false);
    			dlg.setCheckBoxStateAsBoolean("LineBreak",
    					attr.containsKey("line-break") ? "true".equals(attr.get("line-break")) : false);
    			dlg.setControlEnabled("DeleteStyleButton", true);
    		}
    		else {
    			sCurrentStyleName = null;
    			dlg.setTextFieldText("Before", "");
    			dlg.setTextFieldText("After", "");
    			dlg.setListBoxStringItemList("Next", new String[0]);
    			dlg.setCheckBoxStateAsBoolean("Verbatim", false);
    			dlg.setCheckBoxStateAsBoolean("LineBreak", false);
    			dlg.setControlEnabled("DeleteStyleButton", false);
    		}
    	}
    }
    
    private void newStyleClick() {
    	if (nCurrentFamily>-1) {
    		String sNewName = appendItem("StyleName",styleNameProvider.getInternalNames(sOOoFamilyNames[nCurrentFamily]).keySet());
    		if (sNewName!=null) {
    			Map<String,String> attr = new HashMap<String,String>();
    			attr.put("before", "");
    			attr.put("after", "");
    			attr.put("after", "");
    			attr.put("verbatim", "");
    			attr.put("line-break","");
    			styleMap[nCurrentFamily].put(sNewName, attr);
    		}
    		saveStyles();
    		updateStylesControls();
    	}
    }
    
    private void deleteStyleClick() {
    	if (nCurrentFamily>-1 && sCurrentStyleName!=null) {
    		if (deleteCurrentItem("StyleName")) {
    			styleMap[nCurrentFamily].remove(sCurrentStyleName);
    			sCurrentStyleName = null;
        		updateStylesControls();
    		}
    	}
    }
    
    private void addNextClick() {
		appendItem("Next",styleNameProvider.getInternalNames(sOOoFamilyNames[nCurrentFamily]).keySet());
		saveStyles();
		updateStylesControls();
    }
    
    private void removeNextClick() {
		deleteCurrentItem("Next");
		updateStylesControls();
    }
    
    private void loadDefaultsClick() {
		saveStyles();
		// Force update of the ui
		nCurrentFamily = -1;
		sCurrentStyleName = null;
		
		// Count styles that we will overwrite
    	Config clean = ConverterFactory.createConverter("application/x-latex").getConfig();
    	clean.readDefaultConfig("clean.xml");

    	int nCount = 0;
		for (int i=0; i<5; i++) {
			ComplexOption cleanMap = clean.getComplexOption(sFamilyNames[i]+"-map"); 
	    	Map<String,String> displayNames = styleNameProvider.getDisplayNames(sOOoFamilyNames[i]);
	    	for (String sName : cleanMap.keySet()) {
	    		String sDisplayName = (displayNames!=null && displayNames.containsKey(sName)) ? displayNames.get(sName) : ""; 
	    		if (styleMap[i].containsKey(sDisplayName)) { nCount++; }
	    	}
		}
		
		// Display confirmation dialog
		boolean bConfirm = false;
		XDialog xDialog=getDialog("W2LDialogs2.LoadDefaults");
    	if (xDialog!=null) {
    		DialogAccess ldlg = new DialogAccess(xDialog);
    		if (nCount>0) {
    			String sLabel = ldlg.getLabelText("OverwriteLabel");
    			sLabel = sLabel.replaceAll("%s", Integer.toString(nCount));
    			ldlg.setLabelText("OverwriteLabel", sLabel);
    		}
    		else {
    			ldlg.setLabelText("OverwriteLabel", "");
    		}
    		bConfirm = xDialog.execute()==ExecutableDialogResults.OK;
    		xDialog.endExecute();
    	}

		// Do the replacement
    	if (bConfirm) { 
    		for (int i=0; i<5; i++) {
    			ComplexOption cleanMap = clean.getComplexOption(sFamilyNames[i]+"-map"); 
    			Map<String,String> displayNames = styleNameProvider.getDisplayNames(sOOoFamilyNames[i]);
    			copyStyles(cleanMap, styleMap[i], displayNames);
    		}
    	}
		updateStylesControls();
    }
    
    private void copyStyles(ComplexOption source, ComplexOption target, Map<String,String> nameTranslation) {
    	for (String sName : source.keySet()) {
    		String sNewName = sName;
    		if (nameTranslation!=null && nameTranslation.containsKey(sName)) {
    			sNewName = nameTranslation.get(sName);
    		}
    		target.copy(sNewName, source.get(sName));
    	}
    }
    
    // The page "Characters"
    // This page handles the options use_color, use_soul, use_ulem and use_hyperref
    // In addition it handles style maps for formatting attributes
    
    private void loadCharacters() {
    	// Load attribute style map from config
		attributeMap.clear();
		attributeMap.copyAll(config.getComplexOption("text-attribute-map"));
		nCurrentAttribute = -1;
		dlg.setListBoxSelectedItem("FormattingAttribute", (short)0);

    	// Load other controls from config
    	dlg.setCheckBoxStateAsBoolean("UseHyperref","true".equals(config.getOption("use_hyperref")));
    	dlg.setCheckBoxStateAsBoolean("UseColor","true".equals(config.getOption("use_color")));
    	dlg.setCheckBoxStateAsBoolean("UseSoul","true".equals(config.getOption("use_soul")));
    	dlg.setCheckBoxStateAsBoolean("UseUlem","true".equals(config.getOption("use_ulem")));
    	
    	updateCharactersControls();
    }
    
    private void saveCharacters() {
    	updateCharactersMap();
    	
    	// Save the attribute style map to config
    	config.getComplexOption("text-attribute-map").clear();
    	for (String s : attributeMap.keySet()) {
    		if (!attributeMap.get(s).containsKey("deleted")) {
    			config.getComplexOption("text-attribute-map").copy(s, attributeMap.get(s));
    		}
    	}

		// Save other controls to config
    	config.setOption("use_hyperref", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseHyperref")));    	
    	config.setOption("use_color", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseColor")));    	
    	config.setOption("use_soul", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseSoul")));    	
    	config.setOption("use_ulem", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseUlem")));    
    }
    
    private void updateCharactersMap() {
    	// Save the current attribute map, if any
    	if (nCurrentAttribute>-1) {
    		HashMap<String,String> attr = new HashMap<String,String>();
    		if (!dlg.getCheckBoxStateAsBoolean("CustomAttribute")) {
    			// don't delete the map now, but defer this to the dialog is closed
    			attr.put("deleted", "true");
    		}
    		attr.put("before", dlg.getTextFieldText("Before"));
    		attr.put("after", dlg.getTextFieldText("After"));
    		attributeMap.put(sAttributeNames[nCurrentAttribute], attr);
    	}    	
    }
    
    private void updateCharactersControls() {
    	updateCharactersMap();
    	
    	short nNewAttribute = dlg.getListBoxSelectedItem("FormattingAttribute");
    	if (nNewAttribute>-1 && nCurrentAttribute!=nNewAttribute) {
    		String sName = sAttributeNames[nNewAttribute];
    		if (attributeMap.containsKey(sName)) {
    			Map<String,String> attr = attributeMap.get(sName);
    			dlg.setCheckBoxStateAsBoolean("CustomAttribute", !attr.containsKey("deleted"));
    			dlg.setTextFieldText("Before", attr.containsKey("before") ? attr.get("before") : "");
    			dlg.setTextFieldText("After", attr.containsKey("after") ? attr.get("after") : "");
    		}
    		else {
    			dlg.setCheckBoxStateAsBoolean("CustomAttribute", false);
    			dlg.setTextFieldText("Before", "");
    			dlg.setTextFieldText("After", "");
    		}
    		nCurrentAttribute = nNewAttribute;
    	}
    	
    	boolean bCustom = dlg.getCheckBoxStateAsBoolean("CustomAttribute");
    	dlg.setControlEnabled("Before", bCustom);
    	dlg.setControlEnabled("After", bCustom);
    	
    	// Until implemented...
    	dlg.setControlEnabled("UseSoul", false);
    	// After which it should be...
    	//boolean bUseSoul = dlg.getCheckBoxStateAsBoolean("UseSoul");   	    	
    	//dlg.setControlEnabled("UseUlem", !bUseSoul);
    }
    
    // The page "Fonts"
    // This page handles the options use_fontspec, use_pifont, use_tipa, use_eurosym, use_wasysym,
    // use_ifsym, use_bbding
    
    private void loadFonts() {
    	dlg.setCheckBoxStateAsBoolean("UseFontspec","true".equals(config.getOption("use_fontspec")));
    	dlg.setCheckBoxStateAsBoolean("UsePifont","true".equals(config.getOption("use_pifont")));
    	dlg.setCheckBoxStateAsBoolean("UseTipa","true".equals(config.getOption("use_tipa")));
    	dlg.setCheckBoxStateAsBoolean("UseEurosym","true".equals(config.getOption("use_eurosym")));
    	dlg.setCheckBoxStateAsBoolean("UseWasysym","true".equals(config.getOption("use_wasysym")));
    	dlg.setCheckBoxStateAsBoolean("UseIfsym","true".equals(config.getOption("use_ifsym")));
    	dlg.setCheckBoxStateAsBoolean("UseBbding","true".equals(config.getOption("use_bbding")));
    	
    	updateFontsControls();
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
    
    private void updateFontsControls() {
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
    	
    	updatePagesControls();
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
    
    private void updatePagesControls() {
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
    	dlg.setCheckBoxStateAsBoolean("UseColortbl","true".equals(config.getOption("use_colortbl")));
    	dlg.setCheckBoxStateAsBoolean("UseTabulary", "true".equals(config.getOption("use_tabulary")));
    	//dlg.setCheckBoxStateAsBoolean("UseMultirow", "true".equals(config.getOption("use_multirow")));
    	dlg.setCheckBoxStateAsBoolean("UseSupertabular","true".equals(config.getOption("use_supertabular")));
    	dlg.setCheckBoxStateAsBoolean("UseLongtable", "true".equals(config.getOption("use_longtable")));
    	dlg.setTextFieldText("TableFirstHeadStyle", config.getOption("table_first_head_style"));
    	dlg.setTextFieldText("TableHeadStyle", config.getOption("table_head_style"));
    	dlg.setTextFieldText("TableFootStyle", config.getOption("table_foot_style"));
    	dlg.setTextFieldText("TableLastFootStyle", config.getOption("table_last_foot_style"));
    	dlg.setTextFieldText("TableSequenceName", config.getOption("table_sequence_name"));
    	updateTablesControls();
    }
    
    private void saveTables() {
    	config.setOption("table_content", dlg.getCheckBoxStateAsBoolean("NoTables") ? "ignore" : "accept");
    	config.setOption("use_colortbl", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseColortbl")));    	
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
    
    private void updateTablesControls() {
    	boolean bNoTables = dlg.getCheckBoxStateAsBoolean("NoTables");
    	boolean bSupertabular = dlg.getCheckBoxStateAsBoolean("UseSupertabular");
    	boolean bLongtable = dlg.getCheckBoxStateAsBoolean("UseLongtable");
    	dlg.setControlEnabled("UseColortbl", !bNoTables);
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
    	updateFiguresControls();
     }
    
    private void saveFigures() {
    	config.setOption("use_caption", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseCaption")));
    	config.setOption("align_frames", Boolean.toString(dlg.getCheckBoxStateAsBoolean("AlignFrames")));
    	config.setOption("figure_sequence_name", dlg.getTextFieldText("FigureSequenceName"));
    	config.setOption("image_content", dlg.getCheckBoxStateAsBoolean("NoImages") ? "ignore" : "accept");
    	config.setOption("remove_graphics_extension", Boolean.toString(dlg.getCheckBoxStateAsBoolean("RemoveGraphicsExtension")));
    	config.setOption("image_options", dlg.getTextFieldText("ImageOptions"));
    }

    private void updateFiguresControls() {
    	boolean bNoImages = dlg.getCheckBoxStateAsBoolean("NoImages");
    	dlg.setControlEnabled("RemoveGraphicsExtension", !bNoImages);
    	dlg.setControlEnabled("ImageOptionsLabel", !bNoImages);
    	dlg.setControlEnabled("ImageOptions", !bNoImages);
    }
    
    // The page "TextAndMath"
    // This page handles the options use_ooomath and tabstop as well as the 
    // text replacements and math symbol definitions
    
    private void loadTextAndMath() {
		// Get math symbols from config
		if (mathSymbols!=null) { mathSymbols.clear(); }
		else { mathSymbols = new ComplexOption(); }
		mathSymbols.copyAll(config.getComplexOption("math-symbol-map"));
		sCurrentMathSymbol = null;
    	dlg.setListBoxStringItemList("MathSymbolName", sortStringSet(mathSymbols.keySet()));
    	dlg.setListBoxSelectedItem("MathSymbolName", (short)Math.min(0,mathSymbols.keySet().size()-1));

    	// Get string replace from config
    	if (stringReplace!=null) { stringReplace.clear(); }
		else { stringReplace = new ComplexOption(); }
		stringReplace.copyAll(config.getComplexOption("string-replace"));
		sCurrentText = null;
    	dlg.setListBoxStringItemList("TextInput", sortStringSet(stringReplace.keySet()));
    	dlg.setListBoxSelectedItem("TextInput", (short)Math.min(0,stringReplace.keySet().size()-1));
    	    	
    	// Get other options from config
    	dlg.setCheckBoxStateAsBoolean("UseOoomath","true".equals(config.getOption("use_ooomath")));
    	dlg.setTextFieldText("TabStopLaTeX", config.getOption("tabstop"));
    	
    	updateTextAndMathControls();
    }
    
    private void saveTextAndMath() {
    	updateTextAndMathMaps();
    	
    	// Save math symbols to config
		config.getComplexOption("math-symbol-map").clear();
		config.getComplexOption("math-symbol-map").copyAll(mathSymbols);

		// Save string replace to config
		config.getComplexOption("string-replace").clear();
		config.getComplexOption("string-replace").copyAll(stringReplace);
    	
		// Save other options to config
		config.setOption("use_ooomath", Boolean.toString(dlg.getCheckBoxStateAsBoolean("UseOoomath")));
    	config.setOption("tabstop", dlg.getTextFieldText("TabStopLaTeX"));
    }
    
    private void updateTextAndMathMaps() {
    	// Save the current math symbol in our cache
    	if (sCurrentMathSymbol!=null) {
    		Map<String,String> attr = new HashMap<String,String>();
    		attr.put("latex", dlg.getTextFieldText("MathLaTeX"));
    		mathSymbols.put(sCurrentMathSymbol, attr);
    	}

    	// Save the current string replace in our cache
    	if (sCurrentText!=null) {
    		Map<String,String> attr = new HashMap<String,String>();
    		attr.put("latex-code", dlg.getTextFieldText("LaTeX"));
    		attr.put("fontenc", "any");
    		stringReplace.put(sCurrentText, attr);
    	}
    }
    
    private void updateTextAndMathControls() {
    	updateTextAndMathMaps();
    	
    	// Get the current math symbol, if any
    	short nSymbolItem = dlg.getListBoxSelectedItem("MathSymbolName");
    	if (nSymbolItem>=0) {
    		sCurrentMathSymbol = dlg.getListBoxStringItemList("MathSymbolName")[nSymbolItem];
    		
    		Map<String,String> attributes = mathSymbols.get(sCurrentMathSymbol);
    		dlg.setTextFieldText("MathLaTeX", attributes.get("latex"));
    		dlg.setControlEnabled("DeleteSymbolButton", true);
    	}
    	else {
    		sCurrentMathSymbol = null;
    		dlg.setTextFieldText("MathLaTeX", "");
    		dlg.setControlEnabled("DeleteSymbolButton", false);
    	}
    	
    	// Get the current input string, if any
    	short nItem = dlg.getListBoxSelectedItem("TextInput");
    	if (nItem>=0) {
    		sCurrentText = dlg.getListBoxStringItemList("TextInput")[nItem];
    		
    		Map<String,String> attributes = stringReplace.get(sCurrentText);
    		dlg.setTextFieldText("LaTeX", attributes.get("latex-code"));
    		//dlg.setTextFieldText("Fontenc", attributes.get("fontenc"));
    		dlg.setControlEnabled("DeleteTextButton",
    				!"\u00A0!".equals(sCurrentText) && !"\u00A0?".equals(sCurrentText) && 
    				!"\u00A0:".equals(sCurrentText) && !"\u00A0;".equals(sCurrentText) &&
    				!"\u00A0\u2014".equals(sCurrentText));
    	}
    	else {
    		sCurrentText = null;
    		dlg.setTextFieldText("LaTeX", "");
    		//dlg.setTextFieldText("Fontenc", "any");
    		dlg.setControlEnabled("DeleteTextButton", false);
    	}
    	
    }
    
    private void newSymbolClick() {
    	String sNewName = appendItem("MathSymbolName",customSymbolNameProvider.getNames());
    	if (sNewName!=null) {
    		Map<String,String> attr = new HashMap<String,String>();
    		attr.put("latex", "");
    		mathSymbols.put(sNewName, attr);
    	}
    	saveTextAndMath();
    	updateTextAndMathControls();
    }
    
    private void deleteSymbolClick() {
    	if (deleteCurrentItem("MathSymbolName")) {
    		mathSymbols.remove(sCurrentMathSymbol);
    		sCurrentMathSymbol = null;
    		updateTextAndMathControls();
    	}
    }
    
    private void newTextClick() {
    	String sNewName = appendItem("TextInput", new HashSet<String>());
    	if (sNewName!=null) {
    		Map<String,String> attr = new HashMap<String,String>();
    		attr.put("latex-code", "");
    		attr.put("fontenc", "any");
    		stringReplace.put(sNewName, attr);
    	}
    	saveTextAndMath();
    	updateTextAndMathControls();	
    }
    
    private void deleteTextClick() {
    	if (deleteCurrentItem("TextInput")) {
    		stringReplace.remove(sCurrentText);
    		sCurrentText = null;
    		updateTextAndMathControls();
    	}
    }
    
    // Utilities
    private String[] sortStringSet(Set<String> theSet) {
    	String[] theArray = new String[theSet.size()];
    	int i=0;
    	for (String s : theSet) {
    		theArray[i++] = s;
    	}
    	sortStringArray(theArray);
    	return theArray;
    }
    
    private void sortStringArray(String[] theArray) {
    	// TODO: Get locale from OOo rather than the system
        Collator collator = Collator.getInstance();
    	Arrays.sort(theArray, collator);
    }
	
}
