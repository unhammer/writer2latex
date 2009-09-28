/************************************************************************
 *
 *  LaTeXConfig.java
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
 *  Version 1.2 (2009-09-28)
 *
 */

package writer2latex.latex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import writer2latex.api.ComplexOption;
import writer2latex.base.BooleanOption;
import writer2latex.base.IntegerOption;
import writer2latex.base.Option;
import writer2latex.latex.util.HeadingMap;
import writer2latex.latex.i18n.ClassicI18n;
import writer2latex.latex.i18n.ReplacementTrie;
import writer2latex.latex.util.StyleMap;
import writer2latex.util.Misc;

public class LaTeXConfig extends writer2latex.base.ConfigBase {
	/////////////////////////////////////////////////////////////////////////
	// I. Define items needed by ConfigBase
	
    protected int getOptionCount() { return 63; }
    protected String getDefaultConfigPath() { return "/writer2latex/latex/config/"; } 
    
	/////////////////////////////////////////////////////////////////////////
	// II. Override getter and setter methods for simple options in order to: 
    //  - Treat the custom preamble like a regular option, even though the xml representation is different
    //  - Be backwards compatible (renamed the option keep_image_size) 
    
    @Override public void setOption(String sName,String sValue) {
    	if (sName.equals("custom-preamble")) {
    		sCustomPreamble = sValue;
    	}
    	else {
    		// this option has been renamed:
    		if (sName.equals("keep_image_size")) { sName = "original_image_size"; }
    		super.setOption(sName, sValue);
    	}
    }
    
    @Override public String getOption(String sName) {
    	if (sName.equals("custom-preamble")) {
    		return sCustomPreamble;
    	}
    	else {
    		return super.getOption(sName);
    	}
    }
    
	/////////////////////////////////////////////////////////////////////////
    // III. Declare all constants
    
    // Backend
    public static final int GENERIC = 0;
    public static final int DVIPS = 1;
    public static final int PDFTEX = 2;
    public static final int UNSPECIFIED = 3;
    public static final int XETEX = 4;
	
    // Formatting (must be ordered)
    public static final int IGNORE_ALL = 0;
    public static final int IGNORE_MOST = 1;
    public static final int CONVERT_BASIC = 2;
    public static final int CONVERT_MOST = 3;
    public static final int CONVERT_ALL = 4;
    // Page formatting
    public static final int CONVERT_HEADER_FOOTER = 5;
    public static final int CONVERT_GEOMETRY = 6;
    
    // Handling of other formatting
    public static final int IGNORE = 0;
    public static final int ACCEPT = 1;
    public static final int WARNING = 2;
    public static final int ERROR = 3;
	
    // Notes
    //public static final int IGNORE = 0;
    public static final int COMMENT = 1;
    public static final int PDFANNOTATION = 2;
    public static final int MARGINPAR = 3;
    public static final int CUSTOM = 4;
	
    // Options
    private static final int BACKEND = 0;
    private static final int NO_PREAMBLE = 1;
    private static final int NO_INDEX = 2;
    private static final int DOCUMENTCLASS = 3;
    private static final int GLOBAL_OPTIONS = 4;
    private static final int INPUTENCODING = 5;
    private static final int MULTILINGUAL = 6;
    private static final int GREEK_MATH = 7;
    private static final int USE_OOOMATH = 8;
    private static final int USE_PIFONT = 9;
    private static final int USE_IFSYM = 10;
    private static final int USE_WASYSYM = 11;
    private static final int USE_BBDING = 12;
    private static final int USE_EUROSYM = 13;
    private static final int USE_TIPA = 14;
    private static final int USE_COLOR = 15;
    private static final int USE_COLORTBL = 16;
    private static final int USE_GEOMETRY = 17;
    private static final int USE_FANCYHDR = 18;
    private static final int USE_HYPERREF = 19;
    private static final int USE_CAPTION = 20;
    private static final int USE_LONGTABLE = 21;
    private static final int USE_SUPERTABULAR = 22;
    private static final int USE_TABULARY = 23;
    private static final int USE_ENDNOTES = 24;
    private static final int USE_ULEM = 25;
    private static final int USE_LASTPAGE = 26;
    private static final int USE_TITLEREF = 27;
    private static final int USE_OOOREF = 28;
    private static final int USE_BIBTEX = 29;
    private static final int BIBTEX_STYLE = 30;
    private static final int EXTERNAL_BIBTEX_FILES = 31;
    private static final int FORMATTING = 32;
    private static final int PAGE_FORMATTING = 33;
    private static final int OTHER_STYLES = 34;
    private static final int IMAGE_CONTENT = 35;
	private static final int TABLE_CONTENT = 36;
	private static final int TABLE_FIRST_HEAD_STYLE = 37;
	private static final int TABLE_HEAD_STYLE = 38;
	private static final int TABLE_FOOT_STYLE = 39;
	private static final int TABLE_LAST_FOOT_STYLE = 40;
    private static final int IGNORE_HARD_PAGE_BREAKS = 41;
    private static final int IGNORE_HARD_LINE_BREAKS = 42;
    private static final int IGNORE_EMPTY_PARAGRAPHS = 43;
    private static final int IGNORE_DOUBLE_SPACES = 44;
    private static final int ALIGN_FRAMES = 45;
    private static final int FLOAT_FIGURES = 46; 
    private static final int FLOAT_TABLES = 47; 
    private static final int FLOAT_OPTIONS = 48;
    private static final int FIGURE_SEQUENCE_NAME = 49; 
    private static final int TABLE_SEQUENCE_NAME = 50; 
    private static final int IMAGE_OPTIONS = 51;
    private static final int REMOVE_GRAPHICS_EXTENSION = 52;
    private static final int ORIGINAL_IMAGE_SIZE = 53;
    private static final int SIMPLE_TABLE_LIMIT = 54;
    private static final int NOTES = 55;
    private static final int METADATA = 56;
    private static final int TABSTOP = 57;
    private static final int WRAP_LINES_AFTER = 58;
    private static final int SPLIT_LINKED_SECTIONS = 59;
    private static final int SPLIT_TOPLEVEL_SECTIONS = 60;
    private static final int SAVE_IMAGES_IN_SUBDIR = 61;
    private static final int DEBUG = 62;
    
	/////////////////////////////////////////////////////////////////////////
    // IV. Our options data

    private ComplexOption headingMap;
    private ComplexOption parMap;
    private ComplexOption parBlockMap;
    private ComplexOption listMap;
    private ComplexOption listItemMap;
    private ComplexOption textMap;
    private ComplexOption textAttrMap;
    private ComplexOption stringReplace;
    private ComplexOption mathSymbols;
    private String sCustomPreamble = "";
	
	/////////////////////////////////////////////////////////////////////////
    // V. The rather long constructor setting all defaults
    
    /** Construct a new <code>LaTeXConfig</code> with default values for all options
     */
    public LaTeXConfig() {
        super();
        
        // create options with default values
        options[NO_PREAMBLE] = new BooleanOption("no_preamble","false");
        options[NO_INDEX] = new BooleanOption("no_index","false");
        options[DOCUMENTCLASS] = new Option("documentclass","article");
        options[GLOBAL_OPTIONS] = new Option("global_options","");
        options[BACKEND] = new IntegerOption("backend","pdftex") {
            public void setString(String sValue) {
                super.setString(sValue);
                if ("generic".equals(sValue)) nValue = GENERIC;
                else if ("dvips".equals(sValue)) nValue = DVIPS;
                else if ("pdftex".equals(sValue)) nValue = PDFTEX;
                else if ("unspecified".equals(sValue)) nValue = UNSPECIFIED;
                else if ("xetex".equals(sValue)) nValue = XETEX;
            }
        };
        options[INPUTENCODING] = new IntegerOption("inputencoding",ClassicI18n.writeInputenc(ClassicI18n.ASCII)) {
            public void setString(String sValue) {
                super.setString(sValue);
                nValue = ClassicI18n.readInputenc(sValue);
            }
        };
        options[MULTILINGUAL] = new BooleanOption("multilingual","true");
        options[GREEK_MATH] = new BooleanOption("greek_math","true");
        options[USE_OOOMATH] = new BooleanOption("use_ooomath","false");
        options[USE_PIFONT] = new BooleanOption("use_pifont","false");
        options[USE_IFSYM] = new BooleanOption("use_ifsym","false");
        options[USE_WASYSYM] = new BooleanOption("use_wasysym","false");
        options[USE_BBDING] = new BooleanOption("use_bbding","false");
        options[USE_EUROSYM] = new BooleanOption("use_eurosym","false");
        options[USE_TIPA] = new BooleanOption("use_tipa","false");
        options[USE_COLOR] = new BooleanOption("use_color","true");
        options[USE_COLORTBL] = new BooleanOption("use_colortbl","false");
        options[USE_GEOMETRY] = new BooleanOption("use_geometry","false");
        options[USE_FANCYHDR] = new BooleanOption("use_fancyhdr","false");
        options[USE_HYPERREF] = new BooleanOption("use_hyperref","true");
        options[USE_CAPTION] = new BooleanOption("use_caption","false");
        options[USE_LONGTABLE] = new BooleanOption("use_longtable","false");
        options[USE_SUPERTABULAR] = new BooleanOption("use_supertabular","true");
        options[USE_TABULARY] = new BooleanOption("use_tabulary","false");
        options[USE_ENDNOTES] = new BooleanOption("use_endnotes","false");
        options[USE_ULEM] = new BooleanOption("use_ulem","false");
        options[USE_LASTPAGE] = new BooleanOption("use_lastpage","false");
        options[USE_TITLEREF] = new BooleanOption("use_titleref","false");
        options[USE_OOOREF] = new BooleanOption("use_oooref","false");
        options[USE_BIBTEX] = new BooleanOption("use_bibtex","false");
        options[BIBTEX_STYLE] = new Option("bibtex_style","plain");
        options[EXTERNAL_BIBTEX_FILES] = new Option("external_bibtex_files","");
        options[FORMATTING] = new IntegerOption("formatting","convert_basic") {
            public void setString(String sValue) {
                super.setString(sValue);
                if ("convert_all".equals(sValue)) nValue = CONVERT_ALL;
                else if ("convert_most".equals(sValue)) nValue = CONVERT_MOST;
                else if ("convert_basic".equals(sValue)) nValue = CONVERT_BASIC;
                else if ("ignore_most".equals(sValue)) nValue = IGNORE_MOST;
                else if ("ignore_all".equals(sValue)) nValue = IGNORE_ALL;
            }
        };
        options[PAGE_FORMATTING] = new IntegerOption("page_formatting","convert_all") {
            public void setString(String sValue) {
                super.setString(sValue);
                if ("convert_all".equals(sValue)) nValue = CONVERT_ALL;
                else if ("convert_header_footer".equals(sValue)) nValue = CONVERT_HEADER_FOOTER;
                else if ("convert_geometry".equals(sValue)) nValue = CONVERT_GEOMETRY;
                else if ("ignore_all".equals(sValue)) nValue = IGNORE_ALL;
            }
        };
        options[OTHER_STYLES] = new ContentHandlingOption("other_styles","accept");
        options[IMAGE_CONTENT] = new ContentHandlingOption("image_content","accept");
        options[TABLE_CONTENT] = new ContentHandlingOption("table_content","accept");
        options[TABLE_FIRST_HEAD_STYLE] = new Option("table_first_head_style","");
        options[TABLE_HEAD_STYLE] = new Option("table_head_style","");
        options[TABLE_FOOT_STYLE] = new Option("table_foot_style","");
        options[TABLE_LAST_FOOT_STYLE] = new Option("table_last_foot_style","");
        options[IGNORE_HARD_PAGE_BREAKS] = new BooleanOption("ignore_hard_page_breaks","false");
        options[IGNORE_HARD_LINE_BREAKS] = new BooleanOption("ignore_hard_line_breaks","false");
        options[IGNORE_EMPTY_PARAGRAPHS] = new BooleanOption("ignore_empty_paragraphs","false");
        options[IGNORE_DOUBLE_SPACES] = new BooleanOption("ignore_double_spaces","false");
        options[ALIGN_FRAMES] = new BooleanOption("align_frames","true");
        options[FLOAT_FIGURES] = new BooleanOption("float_figures","false");
        options[FLOAT_TABLES] = new BooleanOption("float_tables","false");
        options[FLOAT_OPTIONS] = new Option("float_options","h");
        options[FIGURE_SEQUENCE_NAME] = new BooleanOption("figure_sequence_name","");
        options[TABLE_SEQUENCE_NAME] = new BooleanOption("table_sequence_name","");
        options[IMAGE_OPTIONS] = new Option("image_options","");
        options[REMOVE_GRAPHICS_EXTENSION] = new BooleanOption("remove_graphics_extension","false");
        options[ORIGINAL_IMAGE_SIZE] = new BooleanOption("original_image_size","false");
        options[SIMPLE_TABLE_LIMIT] = new IntegerOption("simple_table_limit","0") {
           public void setString(String sValue) {
               super.setString(sValue);
               nValue = Misc.getPosInteger(sValue,0);
           }
        };
        options[NOTES] = new IntegerOption("notes","comment") {
            public void setString(String sValue) {
                super.setString(sValue);
                if ("ignore".equals(sValue)) nValue = IGNORE;
                else if ("comment".equals(sValue)) nValue = COMMENT;
                else if ("pdfannotation".equals(sValue)) nValue = PDFANNOTATION;
                else if ("marginpar".equals(sValue)) nValue = MARGINPAR;
                else nValue = CUSTOM;
            }
        };
        options[METADATA] = new BooleanOption("metadata","true");
        options[TABSTOP] = new Option("tabstop","");
        options[WRAP_LINES_AFTER] = new IntegerOption("wrap_lines_after","72") {
            public void setString(String sValue) {
                super.setString(sValue);
                nValue = Misc.getPosInteger(sValue,0);
            }
        };
        options[SPLIT_LINKED_SECTIONS] = new BooleanOption("split_linked_sections","false");
        options[SPLIT_TOPLEVEL_SECTIONS] = new BooleanOption("split_toplevel_sections","false");
        options[SAVE_IMAGES_IN_SUBDIR] = new BooleanOption("save_images_in_subdir","false");
        options[DEBUG] = new BooleanOption("debug","false");

        // Complex options - heading map
        headingMap = addComplexOption("heading-map");
        Map<String,String> attr = new HashMap<String,String>();
        attr.put("name", "section");
        attr.put("level", "1");
        headingMap.put("1", attr);
        
        attr = new HashMap<String,String>();
        attr.put("name", "subsection");
        attr.put("level", "2");
        headingMap.put("2", attr);
        
        attr = new HashMap<String,String>();
        attr.put("name", "subsubsection");
        attr.put("level", "3");
        headingMap.put("3", attr);
        
        attr = new HashMap<String,String>();
        attr.put("name", "paragraph");
        attr.put("level", "4");
        headingMap.put("4", attr);
        
        attr = new HashMap<String,String>();
        attr.put("name", "subparagraph");
        attr.put("level", "5");
        headingMap.put("5", attr);
        
        // Complex options - style maps
        parMap = addComplexOption("paragraph-map");
        parBlockMap = addComplexOption("paragraph-block-map");
        listMap = addComplexOption("list-map");
        listItemMap = addComplexOption("list-item-map");
        textMap = addComplexOption("text-map");
        textAttrMap = addComplexOption("text-attribute-map");
        
        // Complex options - string replace
        stringReplace=addComplexOption("string-replace");
        
        // Standard string replace:
        // Fix french spacing; replace nonbreaking space 
        // right before em-dash, !, ?, : and ; (babel handles this)
        attr = new HashMap<String,String>();
        attr.put("fontenc", "any");
        attr.put("latex-code", " \u2014");
        stringReplace.put("\u00A0\u2014",attr);

        attr = new HashMap<String,String>();
        attr.put("fontenc", "any");
        attr.put("latex-code", " !");
        stringReplace.put("\u00A0!",attr);

        attr = new HashMap<String,String>();
        attr.put("fontenc", "any");
        attr.put("latex-code", " ?");
        stringReplace.put("\u00A0?",attr);

        attr = new HashMap<String,String>();
        attr.put("fontenc", "any");
        attr.put("latex-code", " :");
        stringReplace.put("\u00A0:",attr);

        attr = new HashMap<String,String>();
        attr.put("fontenc", "any");
        attr.put("latex-code", " ;");
        stringReplace.put("\u00A0;",attr);
        
        // Right after opening guillemet and right before closing  guillemet:
        // Here we must *keep* the non-breaking space
        // TODO: Use \og and \fg if the document contains french...
        //stringReplace.put("\u00AB\u00A0","\u00AB ",I18n.readFontencs("any"));
        //stringReplace.put("\u00A0\u00BB"," \u00BB",I18n.readFontencs("any"));
        
        // Complex options - math user defined symbols
        mathSymbols = addComplexOption("math-symbol-map");
    }
    
	////////////////////////////////////////////////////////////////////////////
    // VI. Provide methods to fill in the gaps in the supers read and write methods
    	
    protected void readInner(Element elm) {
        if (elm.getTagName().equals("heading-map")) {
        	// Unlike other complex options, a heading map is completely replaced
            headingMap.clear();
        	Node child = elm.getFirstChild();
        	while (child!=null) {
        		if (child.getNodeType()==Node.ELEMENT_NODE) {
        			Element childElm = (Element) child;
        			if (childElm.getTagName().equals("heading-level-map")) {
        				if (childElm.hasAttribute("writer-level")) {
        					Map<String,String> attr = new HashMap<String,String>();
        					attr.put("name",childElm.getAttribute("name"));
        					attr.put("level",childElm.getAttribute("level"));
        					headingMap.put(childElm.getAttribute("writer-level"), attr);
        				}        				
        			}
        		}
        		child = child.getNextSibling();
        	}
        }
        else if (elm.getTagName().equals("style-map")) {
            String sName = elm.getAttribute("name");
            String sFamily = elm.getAttribute("family");
            if (sFamily.length()==0) { // try old name
                sFamily = elm.getAttribute("class");
            }

            Map<String,String> attr = new HashMap<String,String>();
            attr.put("before", elm.getAttribute("before"));
            attr.put("after", elm.getAttribute("after"));
            
            if ("paragraph".equals(sFamily)) {
            	if (elm.hasAttribute("line-break")) { attr.put("line-break", elm.getAttribute("line-break")); }
            	if (elm.hasAttribute("verbatim")) { attr.put("verbatim", elm.getAttribute("verbatim")); }
                parMap.put(sName, attr);
            }
            if ("paragraph-block".equals(sFamily)) {
                attr.put("next", elm.getAttribute("next"));
            	if (elm.hasAttribute("verbatim")) { attr.put("verbatim", elm.getAttribute("verbatim")); }
                parBlockMap.put(sName, attr);
            }
            else if ("list".equals(sFamily)) {
            	listMap.put(sName, attr);
            }
            else if ("listitem".equals(sFamily)) {
                listItemMap.put(sName, attr);
            }
            else if ("text".equals(sFamily)) {
            	if (elm.hasAttribute("verbatim")) { attr.put("verbatim", elm.getAttribute("verbatim")); }
            	textMap.put(sName, attr);
            }
            else if ("text-attribute".equals(sFamily)) {
            	textAttrMap.put(sName, attr);
            }
        }
        else if (elm.getTagName().equals("string-replace")) {
            String sInput = elm.getAttribute("input");
            Map<String,String> attributes = new HashMap<String,String>();
            attributes.put("latex-code", elm.getAttribute("latex-code"));
            attributes.put("fontenc", elm.getAttribute("fontenc"));
            stringReplace.put(sInput,attributes);
        }
        else if (elm.getTagName().equals("math-symbol-map")) {
            String sName = elm.getAttribute("name");
            Map<String,String> attr = new HashMap<String,String>();
            attr.put("latex", elm.getAttribute("latex"));
            mathSymbols.put(sName, attr);
        }
        else if (elm.getTagName().equals("custom-preamble")) {
        	StringBuffer buf = new StringBuffer();
            Node child = elm.getFirstChild();
            while (child!=null) {
                if (child.getNodeType()==Node.TEXT_NODE) {
                    buf.append(child.getNodeValue());
                }
                child = child.getNextSibling();
            }
            sCustomPreamble = buf.toString();
        }
    }

    protected void writeInner(Document dom) {
        // Write heading map
    	int nMaxLevel = 0;
    	while (nMaxLevel<10 && headingMap.get(Integer.toString(nMaxLevel+1))!=null) { nMaxLevel++; }
    	
        Element hmNode = dom.createElement("heading-map");
        // This attribute is not used anymore, but we keep it for backwards compatibility
        hmNode.setAttribute("max-level",Integer.toString(nMaxLevel));
        dom.getDocumentElement().appendChild(hmNode);
        for (int i=1; i<=nMaxLevel; i++) {
            Element hlmNode = dom.createElement("heading-level-map");
            String sWriterLevel = Integer.toString(i);
            hlmNode.setAttribute("writer-level",sWriterLevel);
            Map<String,String> attr = headingMap.get(sWriterLevel);
            hlmNode.setAttribute("name",attr.get("name"));
            hlmNode.setAttribute("level",attr.get("level"));
            hmNode.appendChild(hlmNode);
        }
        
    	// Write style maps
        writeStyleMap(dom,parMap,"paragraph");
        writeStyleMap(dom,parBlockMap,"paragraph-block");
        writeStyleMap(dom,listMap,"list");
        writeStyleMap(dom,listItemMap,"listitem");
        writeStyleMap(dom,textMap,"text");
        writeStyleMap(dom,textAttrMap,"text-attribute");

        // Write string replace
        Set<String> inputStrings = stringReplace.keySet();
        for (String sInput : inputStrings) {
        	Map<String,String> attributes = stringReplace.get(sInput);
            Element srNode = dom.createElement("string-replace");
            srNode.setAttribute("input",sInput);
            srNode.setAttribute("latex-code",attributes.get("latex-code"));
            srNode.setAttribute("fontenc",attributes.get("fontenc"));
            dom.getDocumentElement().appendChild(srNode);
        }
		
        // Write math symbol map
    	for (String sName : mathSymbols.keySet()) {
            String sLatex = mathSymbols.get(sName).get("latex");
            Element msNode = dom.createElement("math-symbol-map");
            msNode.setAttribute("name",sName);
	        msNode.setAttribute("latex",sLatex);
            dom.getDocumentElement().appendChild(msNode);
        }

    	// Write custom preamble
    	Element cp = dom.createElement("custom-preamble");
        cp.appendChild(dom.createTextNode( sCustomPreamble));
        dom.getDocumentElement().appendChild(cp);
    }

    private void writeStyleMap(Document dom, ComplexOption co, String sFamily) {
    	for (String sName : co.keySet()) {
    		Map<String,String> attr = co.get(sName);
            Element smNode = dom.createElement("style-map");
            smNode.setAttribute("name",sName);
	        smNode.setAttribute("family",sFamily);
            smNode.setAttribute("before",attr.containsKey("before") ? attr.get("before") : "");
            smNode.setAttribute("after",attr.containsKey("after") ? attr.get("after") : "");
            if (attr.containsKey("next")) {
                smNode.setAttribute("next",attr.get("next"));
            }
            if (attr.containsKey("line-break")) {
                smNode.setAttribute("line-break",attr.get("line-break"));
            }
            if (attr.containsKey("verbatim")) {
                smNode.setAttribute("verbatim",attr.get("verbatim"));
            }
            dom.getDocumentElement().appendChild(smNode);
        }
    }
	
	/////////////////////////////////////////////////////////////////////////
    // VII. Convenience accessor methods
    
    public HeadingMap getHeadingMap() {
    	int nMaxLevel = 0;
    	while (nMaxLevel<10 && headingMap.get(Integer.toString(nMaxLevel+1))!=null) { nMaxLevel++; }

    	HeadingMap map = new HeadingMap(nMaxLevel);
        for (int i=1; i<=nMaxLevel; i++) {
            String sWriterLevel = Integer.toString(i);
            Map<String,String> attr = headingMap.get(sWriterLevel);
            String sName = attr.get("name");
            int nLevel = Misc.getPosInteger(attr.get("level"),0);
            map.setLevelData(i, sName, nLevel);
        }
        return map;
    }
    
    // Get style maps
    public StyleMap getParStyleMap() { return getStyleMap(parMap); }
    public StyleMap getParBlockStyleMap() { return getStyleMap(parBlockMap); }
    public StyleMap getListStyleMap() { return getStyleMap(listMap); }
    public StyleMap getListItemStyleMap() { return getStyleMap(listItemMap); }
    public StyleMap getTextAttributeStyleMap() { return getStyleMap(textAttrMap); }
    public StyleMap getTextStyleMap() { return getStyleMap(textMap); }
    
    private StyleMap getStyleMap(ComplexOption co) {
    	StyleMap map = new StyleMap();
    	for (String sName : co.keySet()) {
    		Map<String,String> attr = co.get(sName);
    		String sBefore = attr.containsKey("before") ? attr.get("before") : "";
    		String sAfter = attr.containsKey("after") ? attr.get("after") : "";
    		String sNext = attr.containsKey("next") ? attr.get("next") : "";
    		boolean bLineBreak = !"false".equals(attr.get("line-break"));
    		boolean bVerbatim = "true".equals(attr.get("verbatim"));
    		map.put(sName, sBefore, sAfter, sNext, bLineBreak, bVerbatim);
    	}
    	return map;
    }


    // Return current string replace as a trie
    public ReplacementTrie getStringReplace() {
        ReplacementTrie trie = new ReplacementTrie();
        for (String sInput : stringReplace.keySet()) {
        	Map<String,String> attributes = stringReplace.get(sInput);
            String sLaTeXCode = attributes.get("latex-code");
            String sFontenc = attributes.get("fontenc");
            trie.put(sInput,sLaTeXCode!=null ? sLaTeXCode : "",
            		 ClassicI18n.readFontencs(sFontenc!=null ? sFontenc : "any"));
        }
        return trie;
    }
    
    // Get the math symbols as a simple Map
    public Map<String, String> getMathSymbols() {
    	Map<String,String> map = new HashMap<String,String>();
    	for (String sName : mathSymbols.keySet()) {
    		String sLatex = mathSymbols.get(sName).get("latex");
    		map.put(sName, sLatex);
    	}
    	return map;
    }

    // Get the custom preamble
    public String getCustomPreamble() { return sCustomPreamble; }

    // Common options
    public boolean debug() { return ((BooleanOption) options[DEBUG]).getValue(); }

    // General options
    public String getDocumentclass() { return options[DOCUMENTCLASS].getString(); }
    public String getGlobalOptions() { return options[GLOBAL_OPTIONS].getString(); }
    public int getBackend() { return ((IntegerOption) options[BACKEND]).getValue(); }
    public int getInputencoding() { return ((IntegerOption) options[INPUTENCODING]).getValue(); }
    public boolean multilingual() { return ((BooleanOption) options[MULTILINGUAL]).getValue(); }
    public boolean greekMath() { return ((BooleanOption) options[GREEK_MATH]).getValue(); }
    public boolean noPreamble() { return ((BooleanOption) options[NO_PREAMBLE]).getValue(); }
    public boolean noIndex() { return ((BooleanOption) options[NO_INDEX]).getValue(); }
	
    // Package options
    public boolean useOoomath() { return ((BooleanOption) options[USE_OOOMATH]).getValue(); }
    public boolean usePifont() { return ((BooleanOption) options[USE_PIFONT]).getValue(); }
    public boolean useIfsym() { return ((BooleanOption) options[USE_IFSYM]).getValue(); }
    public boolean useWasysym() { return ((BooleanOption) options[USE_WASYSYM]).getValue(); }
    public boolean useBbding() { return ((BooleanOption) options[USE_BBDING]).getValue(); }
    public boolean useEurosym() { return ((BooleanOption) options[USE_EUROSYM]).getValue(); }
    public boolean useTipa() { return ((BooleanOption) options[USE_TIPA]).getValue(); }
    public boolean useColor() { return ((BooleanOption) options[USE_COLOR]).getValue(); }
    public boolean useColortbl() { return ((BooleanOption) options[USE_COLORTBL]).getValue(); }
    public boolean useGeometry() { return ((BooleanOption) options[USE_GEOMETRY]).getValue(); }
    public boolean useFancyhdr() { return ((BooleanOption) options[USE_FANCYHDR]).getValue(); }
    public boolean useHyperref() { return ((BooleanOption) options[USE_HYPERREF]).getValue(); }
    public boolean useCaption() { return ((BooleanOption) options[USE_CAPTION]).getValue(); }
    public boolean useLongtable() { return ((BooleanOption) options[USE_LONGTABLE]).getValue(); }
    public boolean useSupertabular() { return ((BooleanOption) options[USE_SUPERTABULAR]).getValue(); }
    public boolean useTabulary() { return ((BooleanOption) options[USE_TABULARY]).getValue(); }
    public boolean useEndnotes() { return ((BooleanOption) options[USE_ENDNOTES]).getValue(); }
    public boolean useUlem() { return ((BooleanOption) options[USE_ULEM]).getValue(); }
    public boolean useLastpage() { return ((BooleanOption) options[USE_LASTPAGE]).getValue(); }
    public boolean useTitleref() { return ((BooleanOption) options[USE_TITLEREF]).getValue(); }
    public boolean useOooref() { return ((BooleanOption) options[USE_OOOREF]).getValue(); }
    public boolean useBibtex() { return ((BooleanOption) options[USE_BIBTEX]).getValue(); }
    public String bibtexStyle() { return options[BIBTEX_STYLE].getString(); }
    public String externalBibtexFiles() { return options[EXTERNAL_BIBTEX_FILES].getString(); }
	
    // Formatting options
    public int formatting() { return ((IntegerOption) options[FORMATTING]).getValue(); }
    public int pageFormatting() { return ((IntegerOption) options[PAGE_FORMATTING]).getValue(); }
    public int otherStyles() { return ((IntegerOption) options[OTHER_STYLES]).getValue(); }
    public int imageContent() { return ((IntegerOption) options[IMAGE_CONTENT]).getValue(); }
    public int tableContent() { return ((IntegerOption) options[TABLE_CONTENT]).getValue(); }
    public String getTableFirstHeadStyle() { return options[TABLE_FIRST_HEAD_STYLE].getString(); }
    public String getTableHeadStyle() { return options[TABLE_HEAD_STYLE].getString(); }
    public String getTableFootStyle() { return options[TABLE_FOOT_STYLE].getString(); }
    public String getTableLastFootStyle() { return options[TABLE_LAST_FOOT_STYLE].getString(); }
    public boolean ignoreHardPageBreaks() { return ((BooleanOption) options[IGNORE_HARD_PAGE_BREAKS]).getValue(); }
    public boolean ignoreHardLineBreaks() { return ((BooleanOption) options[IGNORE_HARD_LINE_BREAKS]).getValue(); }
    public boolean ignoreEmptyParagraphs() { return ((BooleanOption) options[IGNORE_EMPTY_PARAGRAPHS]).getValue(); }
    public boolean ignoreDoubleSpaces() { return ((BooleanOption) options[IGNORE_DOUBLE_SPACES]).getValue(); }

    // Graphics options
    public boolean alignFrames() { return ((BooleanOption) options[ALIGN_FRAMES]).getValue(); }
    public boolean floatFigures() { return ((BooleanOption) options[FLOAT_FIGURES]).getValue(); }
    public boolean floatTables() { return ((BooleanOption) options[FLOAT_TABLES]).getValue(); }
    public String getFloatOptions() { return options[FLOAT_OPTIONS].getString(); }
    public String getFigureSequenceName() { return options[FIGURE_SEQUENCE_NAME].getString(); }
    public String getTableSequenceName() { return options[TABLE_SEQUENCE_NAME].getString(); }
    public String getImageOptions() { return options[IMAGE_OPTIONS].getString(); }
    public boolean removeGraphicsExtension() { return ((BooleanOption) options[REMOVE_GRAPHICS_EXTENSION]).getValue(); }
    public boolean originalImageSize() { return ((BooleanOption) options[ORIGINAL_IMAGE_SIZE]).getValue(); }
	
    // Tables
    public int getSimpleTableLimit() { return ((IntegerOption) options[SIMPLE_TABLE_LIMIT]).getValue(); }
	
    // Notes
    public int notes() { return ((IntegerOption) options[NOTES]).getValue(); }
    public String getNotesCommand() { return options[NOTES].getString(); }
	
    // Metadata
    public boolean metadata() { return ((BooleanOption) options[METADATA]).getValue(); }
	
    // Tab stops
    public String getTabstop() { return options[TABSTOP].getString(); }
	
    // Files
    public int getWrapLinesAfter() { return ((IntegerOption) options[WRAP_LINES_AFTER]).getValue(); }
    public boolean splitLinkedSections() { return ((BooleanOption) options[SPLIT_LINKED_SECTIONS]).getValue(); }
    public boolean splitToplevelSections() { return ((BooleanOption) options[SPLIT_TOPLEVEL_SECTIONS]).getValue(); }
    public boolean saveImagesInSubdir() { return ((BooleanOption) options[SAVE_IMAGES_IN_SUBDIR]).getValue(); }
	
}

