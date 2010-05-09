/************************************************************************
 *
 *  HeadingStyleConverter.java
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
 */package writer2latex.xhtml;

import writer2latex.office.OfficeReader;
import writer2latex.office.OfficeStyleFamily;
import writer2latex.office.StyleWithProperties;
import writer2latex.util.CSVList;

public class HeadingStyleConverter extends StyleConverterHelper {

	public HeadingStyleConverter(OfficeReader ofr, XhtmlConfig config,
			Converter converter, int nType) {
		super(ofr, config, converter, nType);
        this.styleMap = config.getXHeadingStyleMap();
        this.bConvertStyles = config.xhtmlFormatting()==XhtmlConfig.CONVERT_ALL || config.xhtmlFormatting()==XhtmlConfig.IGNORE_HARD;
        this.bConvertHard = config.xhtmlFormatting()==XhtmlConfig.CONVERT_ALL || config.xhtmlFormatting()==XhtmlConfig.IGNORE_STYLES;
	}

	@Override
	public String getStyleDeclarations(String sIndent) {
        if (bConvertStyles) {
        	StringBuffer buf = new StringBuffer();
        	for (int i=1; i<=6; i++) {
        		if (ofr.getHeadingStyle(i)!=null) {
        			CSVList props = new CSVList(";");
        			getParSc().applyProperties(ofr.getHeadingStyle(i),props,true);
        			props.addValue("clear","left");
        			buf.append(sIndent).append("h").append(i)
        			.append(" {").append(props.toString()).append("}").append(config.prettyPrint() ? "\n" : " ");
        		}
            }
            return buf.toString();
        }
        return "";
	}

	@Override
	public OfficeStyleFamily getStyles() {
		return ofr.getParStyles();
	}
	
	/** Apply a style on a heading
	 * 
	 * @param nLevel the heading level
	 * @param sStyleName the style name
	 * @param info add style information to this StyleInfo
	 */
	public void applyStyle(int nLevel, String sStyleName, StyleInfo info) {
        StyleWithProperties style = (StyleWithProperties) getStyles().getStyle(sStyleName);
        if (style!=null) {
            if (config.multilingual()) { applyLang(style,info); }
            applyDirection(style,info);
            if (style.isAutomatic()) {
                // Apply parent style + hard formatting
                applyStyle(nLevel, style.getParentName(),info);
                if (bConvertHard) { getParSc().applyProperties(style,info.props,false); }
            }
            else {
                String sDisplayName = style.getDisplayName();
                if (styleMap.contains(sDisplayName)) {
                    // Apply attributes as specified in style map from user
                    info.sTagName = styleMap.getBlockElement(sDisplayName);
                    if (!"(none)".equals(styleMap.getBlockCss(sDisplayName))) {
                        info.sClass = styleMap.getBlockCss(sDisplayName);
                    }
                }
                else {
                	// TODO: Apply style if different from main style for this level
                }
            }
        }
	}

	/** Apply an inner style on a heading. The inner style surrounds the text content, excluding the numbering label.
	 *  Inner styles are not an OpenDocument feature, but is provided as an additional style hook for own style sheets.
	 *  An inner style is only applied if there is an explicit style map for the style.
	 * 
	 * @param nLevel the heading level
	 * @param sStyleName the style name
	 * @param info add style information to this StyleInfo
	 */
	public void applyInnerStyle(int nLevel, String sStyleName, StyleInfo info) {
        StyleWithProperties style = (StyleWithProperties) getStyles().getStyle(sStyleName);
        if (style!=null) {
            if (style.isAutomatic()) {
                // Apply parent style
                applyInnerStyle(nLevel, style.getParentName(), info);
            }
            else {
                String sDisplayName = style.getDisplayName();
                if (styleMap.contains(sDisplayName)) {
                    // Apply attributes as specified in style map from user
                    info.sTagName = styleMap.getElement(sDisplayName);
                    if (!"(none)".equals(styleMap.getCss(sDisplayName))) {
                        info.sClass = styleMap.getCss(sDisplayName);
                    }
                }
            }
        }
	}

}
