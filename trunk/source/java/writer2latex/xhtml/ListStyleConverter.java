/************************************************************************
 *
 *  ListStyleConverter.java
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
 *  Version 1.2 (2010-05-04)
 *
 */

package writer2latex.xhtml;

import java.util.Enumeration;
//import java.util.Hashtable;

import writer2latex.office.ListStyle;
import writer2latex.office.OfficeReader;
import writer2latex.office.OfficeStyleFamily;
import writer2latex.office.XMLString;
import writer2latex.util.CSVList;

/**
 * This class converts OpenDocument list styles to
 * CSS2 styles (currently, actually CSS1).
 */
public class ListStyleConverter extends StyleConverterHelper {

    /** Create a new <code>ListStyleConverter</code>
     *  @param ofr an <code>OfficeReader</code> to read style information from
     *  @param config the configuration to use
     *  @param converter the main <code>Converter</code> class
     *  @param nType the type of xhtml to use
     */
    public ListStyleConverter(OfficeReader ofr, XhtmlConfig config, Converter converter, int nType) {
        super(ofr,config,converter,nType);
        this.styleMap = config.getXListStyleMap();
        this.bConvertStyles = config.xhtmlFormatting()==XhtmlConfig.CONVERT_ALL || config.xhtmlFormatting()==XhtmlConfig.IGNORE_HARD;
        this.bConvertHard = config.xhtmlFormatting()==XhtmlConfig.CONVERT_ALL || config.xhtmlFormatting()==XhtmlConfig.IGNORE_STYLES;
    }

    public void applyStyle(int nLevel, String sStyleName, StyleInfo info) {
        ListStyle style = ofr.getListStyle(sStyleName);
        if (style!=null) {
            if (style.isAutomatic()) {
                applyStyle(nLevel,style.getParentName(),info);
                if (bConvertHard) { cssList(style,nLevel,info.props); }
            }
            else {
                String sDisplayName = style.getDisplayName();
                if (styleMap.contains(sDisplayName)) {
                    info.sTagName = styleMap.getElement(sDisplayName);
                    if (!"(none)".equals(styleMap.getCss(sDisplayName))) {
                        info.sClass = styleMap.getCss(sDisplayName);
                    }
                }
                else {
                info.sClass = "listlevel"+Integer.toString(nLevel)
                              +styleNames.getExportName(sDisplayName);
                }
            }
        }
    }
	
	

    /** <p>Convert style information for used styles</p>
     *  @param sIndent a String of spaces to add before each line
     */
    public String getStyleDeclarations(String sIndent) {
        if (bConvertStyles) {
            StringBuffer buf = new StringBuffer();
            Enumeration<String> names = styleNames.keys();
            while (names.hasMoreElements()) {
                String sDisplayName = names.nextElement();
                ListStyle style = (ListStyle)
                    getStyles().getStyleByDisplayName(sDisplayName);
                if (!style.isAutomatic()) {
                    for (int nLevel=1; nLevel<10; nLevel++) {
                        CSVList props = new CSVList(";");
                        cssList(style,nLevel,props);
                        buf.append(sIndent);
                        buf.append(".listlevel");
                        buf.append(nLevel);
                        buf.append(styleNames.getExportName(sDisplayName));
                        buf.append(" {");
                        buf.append(props.toString());
                        buf.append("}");
                        buf.append(config.prettyPrint() ? "\n" : " ");
                        if (config.useHardListNumbering()) {
                        	// Apply left margin and text indent to the paragraphs contained in the list
                        	CSVList parProps = new CSVList(";");
                        	cssListParMargins(style,nLevel,parProps);
                        	if (!parProps.isEmpty()) {
                        		buf.append(sIndent)
                        		.append(".listlevel")
                        		.append(nLevel)
                        		.append(styleNames.getExportName(sDisplayName))
                        		.append(" p {").append(parProps.toString()).append("}").append(config.prettyPrint() ? "\n" : " ");
                        	}
                        }
                    }
                }
            }
            return buf.toString();
        }
        else {
            return "";
        }
    }
	
    /** Get the family of list styles
     *  @return the style family
     */
    public OfficeStyleFamily getStyles() {
        return ofr.getListStyles();
    }
	
    private void cssList(ListStyle style, int nLevel, CSVList props){
        // translates "list" style properties for a particular level
        // Mozilla does not seem to support the "marker" mechanism of CSS2
        // so we will stick with the simpler CSS1-like list style properties
        props.addValue("margin-top","0");
        props.addValue("margin-bottom","0");
        if (!config.useHardListNumbering()) {
        	// Export the numbering to CSS1
        	String sLevelType = style.getLevelType(nLevel);
        	if (XMLString.TEXT_LIST_LEVEL_STYLE_NUMBER.equals(sLevelType)) {
        		// Numbering style, get number format
        		String sNumFormat = style.getLevelProperty(nLevel,XMLString.STYLE_NUM_FORMAT);
        		if ("1".equals(sNumFormat)) { props.addValue("list-style-type","decimal"); }
        		else if ("i".equals(sNumFormat)) { props.addValue("list-style-type","lower-roman"); }
        		else if ("I".equals(sNumFormat)) { props.addValue("list-style-type","upper-roman"); }
        		else if ("a".equals(sNumFormat)) { props.addValue("list-style-type","lower-alpha"); }
        		else if ("A".equals(sNumFormat)) { props.addValue("list-style-type","upper-alpha"); }
        	}
        	else if (XMLString.TEXT_LIST_LEVEL_STYLE_BULLET.equals(sLevelType)) {
        		// Bullet. We can only choose from disc, bullet and square
        		switch (nLevel % 3) {
        		case 1: props.addValue("list-style-type","disc"); break;
        		case 2: props.addValue("list-style-type","bullet"); break;
        		case 0: props.addValue("list-style-type","square"); break;
        		}
        	}
        	else if (XMLString.TEXT_LIST_LEVEL_STYLE_IMAGE.equals(sLevelType)) {
        		// Image. TODO: Handle embedded images
        		String sHref = style.getLevelProperty(nLevel,XMLString.XLINK_HREF);
        		if (sHref!=null) { props.addValue("list-style-image","url('"+sHref+"')"); }
        	}
        }
        else {
        	// No numbering generated by the list; we add hard numbering to the paragraph
        	props.addValue("list-style-type:none");

        	// In this case we also set the left margin for the list
        	// For real styles the margins are applied to the paragraphs
        	// This is more tricky for hard styles, so we use a default left margin on the list
        	if (style.isAutomatic() && nLevel>1) {
        		props.addValue("margin-left", "2em");
        	}
        	else {
        		props.addValue("margin-left","0");
        	}

        	// Also reset the padding (some browsers use a non-zero default value)
        	props.addValue("padding-left", "0");
        }
        
        // We don't want floats to pass a list to the left (Mozilla and IE both
        //handles this terribly!)
        props.addValue("clear:left");
    }
    
    private void cssListParMargins(ListStyle style, int nLevel, CSVList props){
		// Instead margin is applied to the paragraphs in the list, more precisely the list style defines a
    	// left margin and a text indent to *replace* the values from the paragraph style
		String sMarginLeft = style.getLevelStyleProperty(nLevel, XMLString.FO_MARGIN_LEFT);
		if (sMarginLeft!=null) { 
			props.addValue("margin-left", sMarginLeft);
		}
		else {
			props.addValue("margin-left", "0");
		}
		String sTextIndent = style.getLevelStyleProperty(nLevel, XMLString.FO_TEXT_INDENT);
		if (sTextIndent!=null) { 
			props.addValue("text-indent", sTextIndent);
		}
		else {
			props.addValue("text-indent", "0");
		}
    }


	
}
