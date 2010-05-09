/************************************************************************
 *
 *  XhtmlConfig.java
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
 *  Version 1.2 (2010-05-09)
 *
 */

package writer2latex.xhtml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import writer2latex.api.ComplexOption;
import writer2latex.base.BooleanOption;
import writer2latex.base.IntegerOption;
import writer2latex.base.Option;
import writer2latex.util.Misc;

public class XhtmlConfig extends writer2latex.base.ConfigBase {
    // Implement configuration methods
    protected int getOptionCount() { return 42; }
    protected String getDefaultConfigPath() { return "/writer2latex/xhtml/config/"; }
	
    // Override setOption: To be backwards compatible, we must accept options
    // with the prefix xhtml_
    public void setOption(String sName,String sValue) {
        if (sName.startsWith("xhtml_")) { sName = sName.substring(6); }
        // this option has been renamed:
        if (sName.equals("keep_image_size")) { sName = "original_image_size"; }
        // this option has been renamed and extended:
        if (sName.equals("use_list_hack")) {
        	sName = "list_formatting";
        	if (sValue.equals("true")) { sValue = "css1_hack"; }
        	else { sValue = "css1"; }
        }
        super.setOption(sName, sValue);
    }
 
    // Formatting
    public static final int IGNORE_ALL = 0;
    public static final int IGNORE_STYLES = 1;
    public static final int IGNORE_HARD = 2;
    public static final int CONVERT_ALL = 3;
    
    // List formatting
    public static final int CSS1 = 0;
    public static final int CSS1_HACK = 1;
    public static final int HARD_LABELS = 2;

    // Formulas (for xhtml 1.0 strict)
    public static final int STARMATH = 0;
    public static final int LATEX = 1;
    public static final int IMAGE_STARMATH = 2;
    public static final int IMAGE_LATEX = 3;
    
    // Options
    private static final int IGNORE_HARD_LINE_BREAKS = 0;
    private static final int IGNORE_EMPTY_PARAGRAPHS = 1;
    private static final int IGNORE_DOUBLE_SPACES = 2;
    private static final int ORIGINAL_IMAGE_SIZE = 3;
    private static final int NO_DOCTYPE = 4;
    private static final int ADD_BOM = 5;
    private static final int ENCODING = 6;
    private static final int USE_NAMED_ENTITIES = 7;
    private static final int HEXADECIMAL_ENTITIES = 8;
    private static final int PRETTY_PRINT = 9;
    private static final int MULTILINGUAL = 10;
    private static final int TEMPLATE_IDS = 11;
    private static final int SEPARATE_STYLESHEET = 12;
    private static final int CUSTOM_STYLESHEET = 13;
    private static final int FORMATTING = 14;
    private static final int FRAME_FORMATTING = 15;
    private static final int SECTION_FORMATTING = 16;
    private static final int TABLE_FORMATTING = 17;
    private static final int IGNORE_TABLE_DIMENSIONS = 18;
    private static final int LIST_FORMATTING = 19;
    private static final int USE_DUBLIN_CORE = 20;
    private static final int NOTES = 21;
    private static final int CONVERT_TO_PX = 22;
    private static final int SCALING = 23;
    private static final int COLUMN_SCALING = 24;
    private static final int FLOAT_OBJECTS = 25;
    private static final int TABSTOP_STYLE = 26;
    private static final int FORMULAS = 27;
    private static final int SPLIT_LEVEL = 28;
    private static final int REPEAT_LEVELS = 29;
    private static final int CALC_SPLIT = 30;
    private static final int DISPLAY_HIDDEN_SHEETS = 31;
    private static final int DISPLAY_HIDDEN_ROWS_COLS = 32;
    private static final int DISPLAY_FILTERED_ROWS_COLS = 33;
    private static final int APPLY_PRINT_RANGES = 34;
    private static final int USE_TITLE_AS_HEADING = 35;
    private static final int USE_SHEET_NAMES_AS_HEADINGS = 36;
    private static final int XSLT_PATH = 37;
    private static final int SAVE_IMAGES_IN_SUBDIR = 38;
    private static final int UPLINK = 39;
    private static final int DIRECTORY_ICON = 40;
    private static final int DOCUMENT_ICON = 41;

    protected ComplexOption xheading = addComplexOption("heading-map");
    protected ComplexOption xpar = addComplexOption("paragraph-map");
    protected ComplexOption xtext = addComplexOption("text-map");
    protected ComplexOption xframe = addComplexOption("frame-map");
    protected ComplexOption xlist = addComplexOption("list-map");
    protected ComplexOption xattr = addComplexOption("text-attribute-map");
	
    public XhtmlConfig() {
        super();
        // create options with default values
        options[IGNORE_HARD_LINE_BREAKS] = new BooleanOption("ignore_hard_line_breaks","false");
        options[IGNORE_EMPTY_PARAGRAPHS] = new BooleanOption("ignore_empty_paragraphs","false");
        options[IGNORE_DOUBLE_SPACES] = new BooleanOption("ignore_double_spaces","false");
        options[ORIGINAL_IMAGE_SIZE] = new BooleanOption("original_image_size","false");
        options[NO_DOCTYPE] = new BooleanOption("no_doctype","false");
        options[ADD_BOM] = new BooleanOption("add_bom","false");
        options[ENCODING] = new Option("encoding","UTF-8");
        options[USE_NAMED_ENTITIES] = new BooleanOption("use_named_entities","false");
        options[HEXADECIMAL_ENTITIES] = new BooleanOption("hexadecimal_entities","true");
        options[PRETTY_PRINT] = new BooleanOption("pretty_print","true");
        options[MULTILINGUAL] = new BooleanOption("multilingual","true");
        options[TEMPLATE_IDS] = new Option("template_ids","");
        options[SEPARATE_STYLESHEET] = new BooleanOption("separate_stylesheet","false");
        options[CUSTOM_STYLESHEET] = new Option("custom_stylesheet","");
        options[FORMATTING] = new XhtmlFormatOption("formatting","convert_all");
        options[FRAME_FORMATTING] = new XhtmlFormatOption("frame_formatting","convert_all");
        options[SECTION_FORMATTING] = new XhtmlFormatOption("section_formatting","convert_all");
        options[TABLE_FORMATTING] = new XhtmlFormatOption("table_formatting","convert_all");
        options[IGNORE_TABLE_DIMENSIONS] = new BooleanOption("ignore_table_dimensions","false");
        options[LIST_FORMATTING] = new IntegerOption("list_formatting","css1") {
        	@Override public void setString(String sValue) {
        		super.setString(sValue);
        		if ("css1_hack".equals(sValue)) { nValue = CSS1_HACK; }
        		else if ("hard_labels".equals(sValue)) { nValue = HARD_LABELS; }
        		else { nValue = CSS1; }
        	}
        };
        options[USE_DUBLIN_CORE] = new BooleanOption("use_dublin_core","true");
        options[NOTES] = new BooleanOption("notes","true");
        options[CONVERT_TO_PX] = new BooleanOption("convert_to_px","true");
        options[SCALING] = new Option("scaling","100%");
        options[COLUMN_SCALING] = new Option("column_scaling","100%");
        options[FLOAT_OBJECTS] = new BooleanOption("float_objects","true");
        options[TABSTOP_STYLE] = new Option("tabstop_style","");
        options[FORMULAS] = new IntegerOption("formulas","starmath") {
        	@Override public void setString(String sValue) {
        		super.setString(sValue);
        		if ("latex".equals(sValue)) { nValue = LATEX; }
        		else if ("image+latex".equals(sValue)) { nValue = IMAGE_LATEX; }
        		else if ("starmath".equals(sValue)) { nValue = 	STARMATH; }
        		else { nValue = IMAGE_STARMATH; }
        	}
        };
        options[SPLIT_LEVEL] = new IntegerOption("split_level","0") {
        	@Override public void setString(String sValue) {
                super.setString(sValue);
                nValue = Misc.getPosInteger(sValue,0);
            }
        };
        options[REPEAT_LEVELS] = new IntegerOption("repeat_levels","5") {
        	@Override public void setString(String sValue) {
                super.setString(sValue);
                nValue = Misc.getPosInteger(sValue,0);
            }
        };
        options[CALC_SPLIT] = new BooleanOption("calc_split","false");
        options[DISPLAY_HIDDEN_SHEETS] = new BooleanOption("display_hidden_sheets","false");
        options[DISPLAY_HIDDEN_ROWS_COLS] = new BooleanOption("display_hidden_rows_cols","false");
        options[DISPLAY_FILTERED_ROWS_COLS] = new BooleanOption("display_filtered_rows_cols","false");
        options[APPLY_PRINT_RANGES] = new BooleanOption("apply_print_ranges","false");
        options[USE_TITLE_AS_HEADING] = new BooleanOption("use_title_as_heading","true");
        options[USE_SHEET_NAMES_AS_HEADINGS] = new BooleanOption("use_sheet_names_as_headings","true");
        options[XSLT_PATH] = new Option("xslt_path","");
        options[SAVE_IMAGES_IN_SUBDIR] = new BooleanOption("save_images_in_subdir","false");
        options[UPLINK] = new Option("uplink","");
        options[DIRECTORY_ICON] = new Option("directory_icon","");
        options[DOCUMENT_ICON] = new Option("document_icon","");
    }
    
	protected void readInner(Element elm) {
        if (elm.getTagName().equals("xhtml-style-map")) {
            String sName = elm.getAttribute("name");
            String sFamily = elm.getAttribute("family");
            if (sFamily.length()==0) { // try old name
                sFamily = elm.getAttribute("class");
            }
            String sElement = elm.getAttribute("element");
            String sCss = elm.getAttribute("css");
            if (sCss.length()==0) { sCss="(none)"; }
            Map<String,String> attr = new HashMap<String,String>();
            attr.put("element", sElement);
            attr.put("css", sCss);
            String sBlockElement = elm.getAttribute("block-element");
            String sBlockCss = elm.getAttribute("block-css");
            if (sBlockCss.length()==0) { sBlockCss="(none)"; }

            if ("heading".equals(sFamily)) {
                attr.put("block-element", sBlockElement);
                attr.put("block-css", sBlockCss);
                xheading.put(sName,attr);
            }
            if ("paragraph".equals(sFamily)) {
                attr.put("block-element", sBlockElement);
                attr.put("block-css", sBlockCss);
                xpar.put(sName,attr);
            }
            else if ("text".equals(sFamily)) {
                xtext.put(sName,attr);
            }
            else if ("frame".equals(sFamily)) {
                xframe.put(sName,attr);
            }
            else if ("list".equals(sFamily)) {
                xlist.put(sName,attr);
            }
            else if ("attribute".equals(sFamily)) {
                xattr.put(sName,attr);
            }
        }
    }

    protected void writeInner(Document dom) {
        writeXStyleMap(dom,xheading,"heading");
        writeXStyleMap(dom,xpar,"paragraph");
        writeXStyleMap(dom,xtext,"text");
        writeXStyleMap(dom,xlist,"list");
        writeXStyleMap(dom,xframe,"frame");
        writeXStyleMap(dom,xattr,"attribute");
    }
	
    private void writeXStyleMap(Document dom, ComplexOption option, String sFamily) {
        Iterator<String> iter = option.keySet().iterator();
        while (iter.hasNext()) {
            String sName = iter.next();
            Element smNode = dom.createElement("xhtml-style-map");
            smNode.setAttribute("name",sName);
	        smNode.setAttribute("family",sFamily);
            Map<String,String> attr = option.get(sName);
            smNode.setAttribute("element",attr.get("element"));
            smNode.setAttribute("css",attr.get("css"));
            if (attr.containsKey("block-element")) smNode.setAttribute("block-element",attr.get("block-element"));
            if (attr.containsKey("block-css")) smNode.setAttribute("block-css",attr.get("block-css"));
            dom.getDocumentElement().appendChild(smNode);
        }
    }

    // Convenience accessor methods
    public boolean ignoreHardLineBreaks() { return ((BooleanOption) options[IGNORE_HARD_LINE_BREAKS]).getValue(); }
    public boolean ignoreEmptyParagraphs() { return ((BooleanOption) options[IGNORE_EMPTY_PARAGRAPHS]).getValue(); }
    public boolean ignoreDoubleSpaces() { return ((BooleanOption) options[IGNORE_DOUBLE_SPACES]).getValue(); }
    public boolean originalImageSize() { return ((BooleanOption) options[ORIGINAL_IMAGE_SIZE]).getValue(); }
    public boolean xhtmlNoDoctype() { return ((BooleanOption) options[NO_DOCTYPE]).getValue(); }
    public boolean xhtmlAddBOM() { return ((BooleanOption) options[ADD_BOM]).getValue(); }
    public String xhtmlEncoding() { return options[ENCODING].getString(); }
    public boolean useNamedEntities() { return ((BooleanOption) options[USE_NAMED_ENTITIES]).getValue(); }
    public boolean hexadecimalEntities() { return ((BooleanOption) options[HEXADECIMAL_ENTITIES]).getValue(); }
    public boolean prettyPrint() { return ((BooleanOption) options[PRETTY_PRINT]).getValue(); }
    public boolean multilingual() { return ((BooleanOption) options[MULTILINGUAL]).getValue(); }
    public String templateIds() { return options[TEMPLATE_IDS].getString(); }
    public boolean separateStylesheet() { return ((BooleanOption) options[SEPARATE_STYLESHEET]).getValue(); }
    public String xhtmlCustomStylesheet() { return options[CUSTOM_STYLESHEET].getString(); }
    public int xhtmlFormatting() { return ((XhtmlFormatOption) options[FORMATTING]).getValue(); }
    public int xhtmlFrameFormatting() { return ((XhtmlFormatOption) options[FRAME_FORMATTING]).getValue(); }
    public int xhtmlSectionFormatting() { return ((XhtmlFormatOption) options[SECTION_FORMATTING]).getValue(); }
    public int xhtmlTableFormatting() { return ((XhtmlFormatOption) options[TABLE_FORMATTING]).getValue(); }
    public boolean xhtmlIgnoreTableDimensions() { return ((BooleanOption) options[IGNORE_TABLE_DIMENSIONS]).getValue(); }
    public int listFormatting() { return ((IntegerOption) options[LIST_FORMATTING]).getValue(); }
    public boolean xhtmlUseDublinCore() { return ((BooleanOption) options[USE_DUBLIN_CORE]).getValue(); }
    public boolean xhtmlNotes() { return ((BooleanOption) options[NOTES]).getValue(); }
    public boolean xhtmlConvertToPx() { return ((BooleanOption) options[CONVERT_TO_PX]).getValue(); }
    public String getXhtmlScaling() { return options[SCALING].getString(); }
    public String getXhtmlColumnScaling() { return options[COLUMN_SCALING].getString(); }
    public boolean xhtmlFloatObjects() { return ((BooleanOption) options[FLOAT_OBJECTS]).getValue(); }
    public String getXhtmlTabstopStyle() { return options[TABSTOP_STYLE].getString(); }
    public int formulas() { return ((IntegerOption) options[FORMULAS]).getValue(); }
    public int getXhtmlSplitLevel() { return ((IntegerOption) options[SPLIT_LEVEL]).getValue(); }
    public int getXhtmlRepeatLevels() { return ((IntegerOption) options[REPEAT_LEVELS]).getValue(); }
    public boolean xhtmlCalcSplit() { return ((BooleanOption) options[CALC_SPLIT]).getValue(); }
    public boolean xhtmlDisplayHiddenSheets() { return ((BooleanOption) options[DISPLAY_HIDDEN_SHEETS]).getValue(); }
    public boolean displayHiddenRowsCols() { return ((BooleanOption) options[DISPLAY_HIDDEN_ROWS_COLS]).getValue(); }
    public boolean displayFilteredRowsCols() { return ((BooleanOption) options[DISPLAY_FILTERED_ROWS_COLS]).getValue(); }
    public boolean applyPrintRanges() { return ((BooleanOption) options[APPLY_PRINT_RANGES]).getValue(); }
    public boolean xhtmlUseTitleAsHeading() { return ((BooleanOption) options[USE_TITLE_AS_HEADING]).getValue(); }
    public boolean xhtmlUseSheetNamesAsHeadings() { return ((BooleanOption) options[USE_SHEET_NAMES_AS_HEADINGS]).getValue(); }
    public String getXsltPath() { return options[XSLT_PATH].getString(); }
    public boolean saveImagesInSubdir() { return ((BooleanOption) options[SAVE_IMAGES_IN_SUBDIR]).getValue(); }
    public String getXhtmlUplink() { return options[UPLINK].getString(); }
    public String getXhtmlDirectoryIcon() { return options[DIRECTORY_ICON].getString(); }
    public String getXhtmlDocumentIcon() { return options[DOCUMENT_ICON].getString(); }
	
    public XhtmlStyleMap getXParStyleMap() { return getStyleMap(xpar); }
    public XhtmlStyleMap getXHeadingStyleMap() { return getStyleMap(xheading); }
    public XhtmlStyleMap getXTextStyleMap() { return getStyleMap(xtext); }
    public XhtmlStyleMap getXFrameStyleMap() { return getStyleMap(xframe); }
    public XhtmlStyleMap getXListStyleMap() { return getStyleMap(xlist); }
    public XhtmlStyleMap getXAttrStyleMap() { return getStyleMap(xattr); }
	
    private XhtmlStyleMap getStyleMap(ComplexOption co) {
    	XhtmlStyleMap map = new XhtmlStyleMap();
    	for (String sName : co.keySet()) {
    		Map<String,String> attr = co.get(sName);
    		String sElement = attr.containsKey("element") ? attr.get("element") : "";
    		String sCss = attr.containsKey("css") ? attr.get("css") : "";
    		String sBlockElement = attr.containsKey("block-element") ? attr.get("block-element") : "";
    		String sBlockCss = attr.containsKey("block-css") ? attr.get("block-css") : "";
    		map.put(sName, sBlockElement, sBlockCss, sElement, sCss);
    	}
    	return map;

    }
}

