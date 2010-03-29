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
*  Copyright: 2002-2010 by Henrik Just
*
*  All Rights Reserved.
* 
*  Version 1.2 (2010-03-26)
*
*/ 
package org.openoffice.da.comp.writer2xhtml;

import org.openoffice.da.comp.w2lcommon.filter.ConfigurationDialogBase;
import org.openoffice.da.comp.w2lcommon.helper.DialogAccess;

import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.XComponentContext;

public class ConfigurationDialog extends ConfigurationDialogBase implements XServiceInfo {

    // Implement the interface XServiceInfo

	/** The component will be registered under this name.
     */
    public static String __serviceName = "org.openoffice.da.writer2xhtml.ConfigurationDialog";

    /** The component should also have an implementation name.
     */
    public static String __implementationName = "org.openoffice.da.comp.writer2xhtml.ConfigurationDialog";
    
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
    
    // Configure the base class
    @Override protected String getMIMEType() { return "text/html"; }
    
    @Override protected String getDialogLibraryName() { return "W2XDialogs2"; }
    
    @Override protected String getConfigFileName() { return "writer2xhtml.xml"; }
    
    /** Construct a new <code>ConfigurationDialog</code> */
    public ConfigurationDialog(XComponentContext xContext) {
    	super(xContext);
    	
    	pageHandlers.put("General", new GeneralHandler());
    	//pageHandlers.put("Template", new TemplateHandler());
    	pageHandlers.put("Stylesheets", new StylesheetsHandler());
    	pageHandlers.put("Formatting", new FormattingHandler());
    	//pageHandlers.put("Styles1", new StylesPartIHandler());
    	//pageHandlers.put("Styles2", new StylesPartIIHandler());
    	pageHandlers.put("Formatting", new FormattingHandler());
    	pageHandlers.put("Content", new ContentHandler());
    }
    
    // Implement remaining method from XContainerWindowEventHandler
    public String[] getSupportedMethodNames() {
    	String[] sNames = { "EncodingChange" };
    	return sNames;
    }
    
    // the page handlers

    private class GeneralHandler extends PageHandler {
    	private final String[] sEncodingValues = { "UTF-8", "UTF-16", "ISO-8859-1", "US-ASCII" };
    	
    	@Override protected void setControls(DialogAccess dlg) {
    		checkBoxFromConfig(dlg, "NoDoctype", "no_doctype");
    		checkBoxFromConfig(dlg, "AddBOM", "add_bom");
    		listBoxFromConfig(dlg, "Encoding", "encoding", sEncodingValues, (short) 0);
    		
    		if ("true".equals(config.getOption("hexadecimal_entities"))) {
    			dlg.setListBoxSelectedItem("HexadecimalEntities", (short) 0);
    		}
    		else {
    			dlg.setListBoxSelectedItem("HexadecimalEntities", (short) 1);	
    		}
    		
    		checkBoxFromConfig(dlg, "UseNamedEntities", "use_named_entities");
    		checkBoxFromConfig(dlg, "Multilingual", "multilingual");
    		checkBoxFromConfig(dlg, "PrettyPrint", "pretty_print");
    		
    		updateControls(dlg);
    	}
    	
    	@Override protected void getControls(DialogAccess dlg) {
    		checkBoxToConfig(dlg, "NoDoctype", "no_doctype");
    		checkBoxToConfig(dlg, "AddBOM", "add_bom");
    		listBoxToConfig(dlg, "Encoding", "encoding", sEncodingValues);
    		
    		config.setOption("hexadecimal_entities", Boolean.toString(dlg.getListBoxSelectedItem("HexadecimalEntities")==(short)0));
    		
    		checkBoxToConfig(dlg, "UseNamedEntities", "use_named_entities");
    		checkBoxToConfig(dlg, "Multilingual", "multilingual");
    		checkBoxToConfig(dlg, "PrettyPrint", "pretty_print");    		
    	}
    	
    	@Override protected boolean handleEvent(DialogAccess dlg, String sMethod) {
    		if (sMethod.equals("EncodingChange")) {
    			updateControls(dlg);
    			return true;
    		}
    		return false;
    	}

    	private void updateControls(DialogAccess dlg) {
    		boolean bUnicode = dlg.getListBoxSelectedItem("Encoding")<2;
    		dlg.setControlEnabled("HexadecimalEntitiesLabel", !bUnicode);
    		dlg.setControlEnabled("HexadecimalEntities", !bUnicode);
    	}
    	
    }

    private class StylesheetsHandler extends PageHandler {
    	
    	@Override protected void setControls(DialogAccess dlg) {
    		dlg.setCheckBoxStateAsBoolean("UseCustomStylesheet", config.getOption("custom_stylesheet").length()>0);
    		textFieldFromConfig(dlg, "CustomStylesheet", "custom_stylesheet");
    		
    		useCustomStylesheetChange(dlg);
    		includeCustomStylesheetChange(dlg);
    	}
    	
    	@Override protected void getControls(DialogAccess dlg) {
    		if (dlg.getCheckBoxStateAsBoolean("UseCustomStylesheet")) {
        		textFieldToConfig(dlg, "CustomStylesheet", "custom_stylesheet");    			
    		}
    		else {
    			config.setOption("custom_stylesheet", "");
    		}
    	}
    	
    	@Override protected boolean handleEvent(DialogAccess dlg, String sMethod) {
    		if (sMethod.equals("UseCustomStylesheetChange")) {
    			useCustomStylesheetChange(dlg);
    			return true;
    		}
    		else if (sMethod.equals("IncludeCustomStylesheetChange")) {
    			includeCustomStylesheetChange(dlg);
    			return true;
    		}
    		else if (sMethod.equals("LoadButtonClick")) {
    			loadButtonClick(dlg);
    			return true;
    		}
    		return false;
    	}
    	
    	private void useCustomStylesheetChange(DialogAccess dlg) {
    		boolean bUseCustomStylesheet = dlg.getCheckBoxStateAsBoolean("UseCustomStylesheet");
    		dlg.setControlEnabled("CustomStylesheetLabel", bUseCustomStylesheet);
    		dlg.setControlEnabled("CustomStylesheet", bUseCustomStylesheet);
    	}
    	
    	private void includeCustomStylesheetChange(DialogAccess dlg) {
    		dlg.setControlEnabled("IncludedCustomStylesheet", dlg.getCheckBoxStateAsBoolean("IncludeCustomStylesheet"));
    	}
    	
    	private void loadButtonClick(DialogAccess dlg) {
    		// TODO
    	}
    	
    }

    private class FormattingHandler extends PageHandler {
    	private final String[] sExportValues = { "convert_all", "ignore_styles", "ignore_hard", "ignore_all" };
    	
    	@Override protected void setControls(DialogAccess dlg) {
    		listBoxFromConfig(dlg, "Formatting", "formatting", sExportValues, (short) 0);
    		listBoxFromConfig(dlg, "FrameFormatting", "frame_formatting", sExportValues, (short) 0);
    		
    		// OOo does not support styles for sections and tables, hence this simplified variant
    		dlg.setCheckBoxStateAsBoolean("SectionFormatting",
    			config.getOption("section_formatting").equals("convert_all") ||
    			config.getOption("section_formatting").equals("ignore_styles"));
    		dlg.setCheckBoxStateAsBoolean("TableFormatting",
        		config.getOption("table_formatting").equals("convert_all") ||
        		config.getOption("table_formatting").equals("ignore_styles"));
    		
    		checkBoxFromConfig(dlg, "IgnoreTableDimensions", "ignore_table_dimensions");
    		checkBoxFromConfig(dlg, "UseListHack", "use_list_hack");
    		checkBoxFromConfig(dlg, "ConvertToPx", "convert_to_px");
    		checkBoxFromConfig(dlg, "SeparateStylesheet", "separate_stylesheet");
    	}
    	
    	@Override protected void getControls(DialogAccess dlg) {
    		listBoxToConfig(dlg, "Formatting", "formatting", sExportValues);
    		listBoxToConfig(dlg, "FrameFormatting", "frame_formatting", sExportValues);
    		
    		config.setOption("section_formatting", dlg.getCheckBoxStateAsBoolean("SectionFormatting") ? "convert_all" : "ignore_all");
    		config.setOption("table_formatting", dlg.getCheckBoxStateAsBoolean("TableFormatting") ? "convert_all" : "ignore_all");
    		
    		checkBoxToConfig(dlg, "IgnoreTableDimensions", "ignore_table_dimensions");
    		checkBoxToConfig(dlg, "UseListHack", "use_list_hack");
    		checkBoxToConfig(dlg, "ConvertToPx", "convert_to_px");
    		checkBoxToConfig(dlg, "SeparateStylesheet", "separate_stylesheet");
    	}
    	
    	@Override protected boolean handleEvent(DialogAccess dlg, String sMethod) {
    		return false;
    	}
    	
    }

    private class ContentHandler extends PageHandler {
    	private final String[] sFormulaValues = { "image+starmath", "image+latex", "starmath", "latex" };
    	
    	@Override protected void setControls(DialogAccess dlg) {
    		listBoxFromConfig(dlg, "Formulas", "formulas", sFormulaValues, (short) 0);
    	}
    	
    	@Override protected void getControls(DialogAccess dlg) {
    		listBoxToConfig(dlg, "Formulas", "formulas", sFormulaValues);
    	}
    	
    	@Override protected boolean handleEvent(DialogAccess dlg, String sMethod) {
    		return false;
    	}
    	
    }
	

}
