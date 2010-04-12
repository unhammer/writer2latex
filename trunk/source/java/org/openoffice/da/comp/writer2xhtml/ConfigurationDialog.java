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
*  Version 1.2 (2010-04-09)
*
*/ 

package org.openoffice.da.comp.writer2xhtml;

import java.util.Map;

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
    	pageHandlers.put("Template", new TemplateHandler());
    	pageHandlers.put("Stylesheets", new StylesheetsHandler());
    	pageHandlers.put("Formatting", new FormattingHandler());
    	pageHandlers.put("Styles1", new Styles1Handler());
    	pageHandlers.put("Styles2", new Styles2Handler());
    	pageHandlers.put("Formatting", new FormattingHandler());
    	pageHandlers.put("Content", new ContentHandler());
    }
    
    // Implement remaining method from XContainerWindowEventHandler
    public String[] getSupportedMethodNames() {
    	String[] sNames = { "EncodingChange", // General
    			"CustomTemplateChange", "LoadTemplateClick", // Template
    			"UseCustomStylesheetChange", "IncludeCustomStylesheetClick", "LoadStylesheetClick", // Stylesheet
    			"StyleFamilyChange", "StyleNameChange", "NewStyleClick", "DeleteStyleClick", "LoadDefaultsClick" // Styles1
    	};
    	return sNames;
    }
    
    // the page handlers
	private final String[] sCharElements = { "span", "abbr", "acronym", "b", "big", "cite", "code", "del", "dfn", "em", "i",
			"ins", "kbd", "samp", "small", "strong", "sub", "sup", "tt", "var", "q" };

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
    
    private class TemplateHandler extends CustomFileHandler {
    	
    	protected String getSuffix() {
    		return "Template";
    	}
    	
    	protected String getFileName() {
    		return "writer2xhtml-template.xhtml";
    	}
    	
    	protected void useCustomInner(DialogAccess dlg, boolean bEnable) {
    		dlg.setControlEnabled("ContentIdLabel", bEnable);
    		dlg.setControlEnabled("ContentId", bEnable);
    		dlg.setControlEnabled("HeaderIdLabel", bEnable);
    		dlg.setControlEnabled("HeaderId", bEnable);
    		dlg.setControlEnabled("FooterIdLabel", bEnable);
    		dlg.setControlEnabled("FooterId", bEnable);
    		dlg.setControlEnabled("PanelIdLabel", bEnable);
    		dlg.setControlEnabled("PanelId", bEnable);
    	}

    	@Override protected void setControls(DialogAccess dlg) {
    		super.setControls(dlg);
    		String[] sCustomIds = config.getOption("template_ids").split(",");
    		if (sCustomIds.length>0) { dlg.setComboBoxText("ContentId", sCustomIds[0]); }
    		if (sCustomIds.length>1) { dlg.setComboBoxText("HeaderId", sCustomIds[1]); }
    		if (sCustomIds.length>2) { dlg.setComboBoxText("FooterId", sCustomIds[2]); }
    		if (sCustomIds.length>3) { dlg.setComboBoxText("PanelId", sCustomIds[3]); }
    	}

    	@Override protected void getControls(DialogAccess dlg) {
    		super.getControls(dlg);
    		config.setOption("template_ids",
    				dlg.getComboBoxText("ContentId").trim()+","+
    				dlg.getComboBoxText("HeaderId").trim()+","+
    				dlg.getComboBoxText("FooterId").trim()+","+
    				dlg.getComboBoxText("PanelId").trim());
    	}
    	
    }

    private class StylesheetsHandler extends CustomFileHandler {
    	
    	protected String getSuffix() {
    		return "Stylesheet";
    	}
    	
    	protected String getFileName() {
    		return "writer2xhtml-styles.css";
    	}
    	
    	protected void useCustomInner(DialogAccess dlg, boolean bEnable) {
    	}

    	
    	@Override protected void setControls(DialogAccess dlg) {
    		super.setControls(dlg);
    		dlg.setCheckBoxStateAsBoolean("LinkCustomStylesheet", config.getOption("custom_stylesheet").length()>0);
    		textFieldFromConfig(dlg, "CustomStylesheetURL", "custom_stylesheet");
    		
    		linkCustomStylesheetChange(dlg);
    	}
    	
    	@Override protected void getControls(DialogAccess dlg) {
    		super.getControls(dlg);
    		if (dlg.getCheckBoxStateAsBoolean("LinkCustomStylesheet")) {
        		textFieldToConfig(dlg, "CustomStylesheetURL", "custom_stylesheet");    			
    		}
    		else {
    			config.setOption("custom_stylesheet", "");
    		}
    	}
    	
    	@Override protected boolean handleEvent(DialogAccess dlg, String sMethod) {
    		if (super.handleEvent(dlg, sMethod)) {
    			return true;
    		}
    		if (sMethod.equals("LinkCustomStylesheetChange")) {
    			linkCustomStylesheetChange(dlg);
    			return true;
    		}
    		return false;
    	}
    	
    	private void linkCustomStylesheetChange(DialogAccess dlg) {
    		boolean bLinkCustomStylesheet = dlg.getCheckBoxStateAsBoolean("LinkCustomStylesheet");
    		dlg.setControlEnabled("CustomStylesheetURLLabel", bLinkCustomStylesheet);
    		dlg.setControlEnabled("CustomStylesheetURL", bLinkCustomStylesheet);
    	}
    	
    }
    
    private class Styles1Handler extends StylesPageHandler {
    	private final String[] sXhtmlFamilyNames = { "text", "paragraph", "list", "frame" };
    	private final String[] sXhtmlOOoFamilyNames = { "CharacterStyles", "ParagraphStyles", "NumberingStyles", "FrameStyles" };
    	
    	private final String[] sParElements = { "p", "h1", "h2", "h3", "h4", "h5", "h6", "address", "dd", "dt", "pre" };
    	private final String[] sParBlockElements = { "div", "blockquote", "dl" };
    	private final String[] sEmpty = { };
    	
    	private String[][] sElements = new String[4][];
    	private String[][] sBlockElements = new String[4][];
    	
    	protected Styles1Handler() {
    		super(4);
    		sFamilyNames = sXhtmlFamilyNames;
    		sOOoFamilyNames = sXhtmlOOoFamilyNames;
 
    		sElements[0] = sCharElements;
    		sElements[1] = sParElements;
    		sElements[2] = sEmpty;
    		sElements[3] = sEmpty;
    		
    		sBlockElements[0] = sEmpty;
    		sBlockElements[1] = sParBlockElements;
    		sBlockElements[2] = sEmpty;
    		sBlockElements[3] = sEmpty;
    	}
    	
    	protected String getDefaultConfigName() {
    		return "cleanxhtml.xml";
    	}
		
		protected void setControls(DialogAccess dlg, Map<String,String> attr) {
			if (!attr.containsKey("element")) { attr.put("element", ""); }
			if (!attr.containsKey("css")) { attr.put("css", ""); }
			dlg.setComboBoxText("Element", attr.get("element"));
			dlg.setTextFieldText("Css", none2empty(attr.get("css")));
			if (nCurrentFamily==1) {
				if (!attr.containsKey("block-element")) { attr.put("block-element", ""); }
				if (!attr.containsKey("block-css")) { attr.put("block-css", ""); }
				dlg.setComboBoxText("BlockElement", attr.get("block-element"));
				dlg.setTextFieldText("BlockCss", none2empty(attr.get("block-css")));
			}
			else {
				dlg.setComboBoxText("BlockElement", "");
				dlg.setTextFieldText("BlockCss", "");								
			}
		}
		
		protected void getControls(DialogAccess dlg, Map<String,String> attr) {
			attr.put("element", dlg.getComboBoxText("Element"));
			attr.put("css", empty2none(dlg.getTextFieldText("Css")));
			if (nCurrentFamily==1) {
				attr.put("block-element", dlg.getComboBoxText("BlockElement"));
				attr.put("block-css", empty2none(dlg.getTextFieldText("BlockCss")));
			}
		}
		
		protected void clearControls(DialogAccess dlg) {
			dlg.setComboBoxText("Element", "");
			dlg.setTextFieldText("Css", "");
			dlg.setComboBoxText("BlockElement", "");
			dlg.setTextFieldText("BlockCss", "");
		}
		
		protected void prepareControls(DialogAccess dlg) {
			dlg.setListBoxStringItemList("Element", sElements[nCurrentFamily]);
			dlg.setListBoxStringItemList("BlockElement", sBlockElements[nCurrentFamily]);
			dlg.setControlEnabled("Element", nCurrentFamily<=1);			
			dlg.setControlEnabled("BlockElement", nCurrentFamily==1);
			dlg.setControlEnabled("BlockCss", nCurrentFamily==1);		
		}
	}
    
    private class Styles2Handler extends AttributePageHandler {
    	private String[] sXhtmlAttributeNames = { "bold", "italics", "fixed", "superscript", "subscript", "underline", "overstrike" };
    	
    	public Styles2Handler() {
    		sAttributeNames = sXhtmlAttributeNames;
    	}
    	
    	@Override public void setControls(DialogAccess dlg) {
    		super.setControls(dlg);
    		textFieldFromConfig(dlg,"TabstopStyle","tabstop_style");
    	}
    	
    	@Override public void getControls(DialogAccess dlg) {
    		super.getControls(dlg);
    		textFieldToConfig(dlg,"TabstopStyle","tabstop_style");
    	}
    	
    	protected void setControls(DialogAccess dlg, Map<String,String> attr) {
    		if (!attr.containsKey("element")) { attr.put("element", ""); }
    		if (!attr.containsKey("css")) { attr.put("css", ""); }
    		dlg.setListBoxStringItemList("Element", sCharElements);
    		dlg.setComboBoxText("Element", attr.get("element"));
    		dlg.setTextFieldText("Css", none2empty(attr.get("css")));
    	}
    	
    	protected void getControls(DialogAccess dlg, Map<String,String> attr) {
    		attr.put("element", dlg.getComboBoxText("Element"));
    		attr.put("css", empty2none(dlg.getTextFieldText("Css")));
    	}
    	
    	protected void prepareControls(DialogAccess dlg, boolean bEnable) {
    		dlg.setControlEnabled("ElementLabel", bEnable);
    		dlg.setControlEnabled("Element", bEnable);
    		dlg.setControlEnabled("CssLabel", bEnable);
    		dlg.setControlEnabled("Css", bEnable);
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
    		//TODO: These have been postponed
    		//checkBoxFromConfig(dlg, "ConvertToPx", "convert_to_px");
    		//checkBoxFromConfig(dlg, "SeparateStylesheet", "separate_stylesheet");
    	}
    	
    	@Override protected void getControls(DialogAccess dlg) {
    		listBoxToConfig(dlg, "Formatting", "formatting", sExportValues);
    		listBoxToConfig(dlg, "FrameFormatting", "frame_formatting", sExportValues);
    		
    		config.setOption("section_formatting", dlg.getCheckBoxStateAsBoolean("SectionFormatting") ? "convert_all" : "ignore_all");
    		config.setOption("table_formatting", dlg.getCheckBoxStateAsBoolean("TableFormatting") ? "convert_all" : "ignore_all");
    		
    		checkBoxToConfig(dlg, "IgnoreTableDimensions", "ignore_table_dimensions");
    		checkBoxToConfig(dlg, "UseListHack", "use_list_hack");
    		//TODO: These have been postponed
    		//checkBoxToConfig(dlg, "ConvertToPx", "convert_to_px");
    		//checkBoxToConfig(dlg, "SeparateStylesheet", "separate_stylesheet");
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
    
    private String none2empty(String s) {
    	return s.equals("(none)") ? "" : s;
    }
    
    private String empty2none(String s) {
    	String t = s.trim();
    	return t.length()==0 ? "(none)" : t;
    }
	

}
