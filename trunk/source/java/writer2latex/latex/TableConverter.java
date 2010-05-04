/************************************************************************
 *
 *  TableConverter.java
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
 *  Version 1.2 (2010-04-29)
 *
 */

package writer2latex.latex;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import writer2latex.util.*;
import writer2latex.office.*;
import writer2latex.latex.util.BeforeAfter;
import writer2latex.latex.util.Context;

enum RowType {
	FIRST_HEAD, HEAD, BODY, FOOT, LAST_FOOT;
}

/** <p>This class converts OpenDocument tables to LaTeX.</p>
 *  <p>The following LaTeX packages are used; some of them are optional</p>
 *  <p>array.sty, longtable.sty, supertabular.sty, tabulary.sty, hhline.sty, 
 *  colortbl.sty.</p>
 *  <p>Options:</p>
 *  <ul>
 *    <li>use_longtable = true|false</li>
 *    <li>use_supertabular = true|false</li>
 *    <li>use_tabulary = true|false</li>
 *    <li>use_colortbl = true|false</li>
 *    <li>float_tables = true|false</li>
 *    <li>float_options = &lt;string&gt;</li>
 *    <li>table_content = accept|ignore|warning|error</li>
 *  </ul>
 *     
 */
public class TableConverter extends ConverterHelper {
    private boolean bNeedLongtable = false;
    private boolean bNeedSupertabular = false;
    private boolean bNeedTabulary = false;
    private boolean bNeedColortbl = false;
    private boolean bContainsTables = false;
	
    /** <p>Constructs a new <code>TableConverter</code>.</p>
     */
    public TableConverter(OfficeReader ofr, LaTeXConfig config,
        ConverterPalette palette) {
        super(ofr,config,palette);
    }

    public void appendDeclarations(LaTeXDocumentPortion pack, LaTeXDocumentPortion decl) {
        pack.append("\\usepackage{array}").nl(); // TODO: Make this optional
        if (bNeedLongtable) { pack.append("\\usepackage{longtable}").nl(); }
        if (bNeedSupertabular) { pack.append("\\usepackage{supertabular}").nl(); }
        if (bNeedTabulary) { pack.append("\\usepackage{tabulary}").nl(); }
        pack.append("\\usepackage{hhline}").nl(); // TODO: Make this optional
        if (bNeedColortbl) { pack.append("\\usepackage{colortbl}").nl(); }

        // Set padding for table cells (1mm is default in OOo!)
        // For vertical padding we can only specify a relative size
        if (bContainsTables) {
            decl.append("\\setlength\\tabcolsep{1mm}").nl();
            decl.append("\\renewcommand\\arraystretch{1.3}").nl();
        }
    }
	
    // Export a lonely table caption
    public void handleCaption(Element node, LaTeXDocumentPortion ldp, Context oc) {
        ldp.append("\\captionof{table}");
        palette.getCaptionCv().handleCaptionBody(node,ldp,oc,true);
    }
	
    /** <p> Process a table (table:table or table:sub-table tag)</p>
     * @param node The element containing the table
     * @param ldp the <code>LaTeXDocumentPortion</code> to which
     * LaTeX code should be added
     * @param oc the current context
     */
    public void handleTable(Element node, Element caption, boolean bCaptionAbove,
        LaTeXDocumentPortion ldp, Context oc) {

        // Export table, if allowed by the configuration
        switch (config.tableContent()) {
        case LaTeXConfig.ACCEPT:
            new SingleTableConverter().handleTable(node,caption,bCaptionAbove,ldp,oc);
            bContainsTables = true;
            break;
        case LaTeXConfig.IGNORE:
            // Ignore table silently
            break;
        case LaTeXConfig.WARNING:
            System.err.println("Warning: Tables are not allowed");
            break;
        case LaTeXConfig.ERROR:
            ldp.append("% Error in document: A table was ignored");
        }
    }
	
    // Inner class to convert a single table
    private class SingleTableConverter {
        private TableReader table;
        private TableFormatter formatter;
        private Element caption;
        private boolean bCaptionAbove;
        private BeforeAfter baTable;
        private BeforeAfter baTableAlign;
        private RowType[] rowTypes;
        
        // Return the paragraph style of the first paragraph/heading within this block text
        private String getFirstParStyle(Element node) {
        	Node child = node.getFirstChild();
        	while (child!=null) {
        		if (Misc.isElement(child, XMLString.TEXT_P) || Misc.isElement(child, XMLString.TEXT_H)) {
        			String sStyleName = Misc.getAttribute(child, XMLString.TEXT_STYLE_NAME);
        			if (sStyleName!=null) {
        				StyleWithProperties style = ofr.getParStyle(sStyleName);
        				if (style!=null) {
        					if (style.isAutomatic()) {
        						sStyleName = style.getParentName();
        					}
        					return ofr.getParStyles().getDisplayName(sStyleName);
        				}
        			}
        			return null;
        		}
        		else if (OfficeReader.isTextElement(child)) {
        			return getFirstParStyle((Element)child);
        		}
        		child = child.getNextSibling();
        	}
        	return null;
        }
        
        private boolean hasRowType(RowType test) {
        	for (RowType type : rowTypes) {
        		if (type==test) { return true; }
        	}
        	return false;
        }
        
        private void handleTable(Element node, Element caption, boolean bCaptionAbove,
        LaTeXDocumentPortion ldp, Context oc) {
            // Store the caption
            this.caption = caption;
            this.bCaptionAbove = bCaptionAbove;

            // Read the table
            table = ofr.getTableReader(node);
            
            if (palette.getMathmlCv().handleDisplayEquation(table,ldp)) { return; }
			
            // Get formatter and update flags according to formatter
            formatter = new TableFormatter(ofr,config,palette,table,!oc.isInMulticols(),oc.isInTable());
            bContainsTables = true;
            bNeedLongtable |= formatter.isLongtable();
            bNeedSupertabular |= formatter.isSupertabular();
            bNeedTabulary |= formatter.isTabulary();
            bNeedColortbl |= formatter.isColortbl();
			
            // Update the context
            Context ic = (Context) oc.clone();
            ic.setInTable(true);
            ic.setInSimpleTable(formatter.isSimple());
            // Never allow footnotes in tables
            // (longtable.sty *does* allow footnotes in body, but not in head -
            // only consistent solution is to disallow all footnotes)
            ic.setNoFootnotes(true);

            // Get table declarations
            baTable = new BeforeAfter();
            baTableAlign = new BeforeAfter();
            formatter.applyTableStyle(baTable,baTableAlign,config.floatTables() && !ic.isInFrame() && !table.isSubTable());
            
            // Identify the row types
            rowTypes = new RowType[table.getRowCount()];
            for (int nRow=0; nRow<table.getRowCount(); nRow++) {
            	// First collect the row type as defined in the document
            	if (nRow<table.getFirstBodyRow()) {
            		rowTypes[nRow] = RowType.HEAD;
            	}
            	else {
            		rowTypes[nRow] = RowType.BODY;
            	}
            	if (formatter.isLongtable() || formatter.isSupertabular()) {
            		// Then override with user defined row types where applicable
            		// (but only for multipage tables)
            		// The row type is determined from the first paragraph in the first cell
            		String sStyleName = getFirstParStyle(table.getCell(nRow, 0));
            		if (sStyleName!=null) {
            			if (sStyleName.equals(config.getTableFirstHeadStyle())) {
            				rowTypes[nRow] = RowType.FIRST_HEAD;
            			}
            			else if (sStyleName.equals(config.getTableHeadStyle())) {
            				rowTypes[nRow] = RowType.HEAD;
            			}
            			else if (sStyleName.equals(config.getTableFootStyle())) {
            				rowTypes[nRow] = RowType.FOOT;
            			}
            			else if (sStyleName.equals(config.getTableLastFootStyle())) {
            				rowTypes[nRow] = RowType.LAST_FOOT;
            			}
            		}
            	}
            }
			
            // Convert table
            if (formatter.isSupertabular()) {
                handleSupertabular(ldp,ic);
            }
            else if (formatter.isLongtable()) {
                handleLongtable(ldp,ic);
            }
            else if (config.floatTables() && !ic.isInFrame() && !table.isSubTable()) {
                handleTableFloat(ldp,ic);
            }
            else {
                handleTabular(ldp,ic);
            }
			
            // Insert any pending footnotes
            palette.getNoteCv().flushFootnotes(ldp,oc);
        }
		
        private void handleSupertabular(LaTeXDocumentPortion ldp, Context oc) {
            ldp.append(baTableAlign.getBefore());

            // Caption
            if (caption!=null) {
                handleCaption(bCaptionAbove ? "\\topcaption" : "\\bottomcaption", ldp, oc);
            }

            // Table head
            ldp.append("\\tablefirsthead{");
            if (hasRowType(RowType.FIRST_HEAD)) {
            	handleRows(ldp,oc,RowType.FIRST_HEAD,true,false);
            }
            else {
            	handleRows(ldp,oc,RowType.HEAD,true,false);
            }
            ldp.append("}\n");
            ldp.append("\\tablehead{");
            handleRows(ldp,oc,RowType.HEAD,true,false);
            ldp.append("}\n");
            
            // Table foot
            ldp.append("\\tabletail{");
            handleRows(ldp,oc,RowType.FOOT,true,true);
            ldp.append("}\n");
            ldp.append("\\tablelasttail{");
            if (hasRowType(RowType.LAST_FOOT)) {
            	handleRows(ldp,oc,RowType.LAST_FOOT,true,true);
            }
            else {
            	handleRows(ldp,oc,RowType.FOOT,true,true);
            }
            ldp.append("}\n");

            // The table body
            handleHyperTarget(ldp);
            ldp.append(baTable.getBefore()).nl();
            handleRows(ldp,oc,RowType.BODY,true,!hasRowType(RowType.FOOT) && !hasRowType(RowType.LAST_FOOT));
            ldp.nl().append(baTable.getAfter()).nl();

            ldp.append(baTableAlign.getAfter());
        }
		
        private void handleLongtable(LaTeXDocumentPortion ldp, Context oc) {
            handleHyperTarget(ldp);
            ldp.append(baTable.getBefore()).nl();

            // First head
            if (caption!=null && bCaptionAbove) {
            	// If there's a caption above, we must use \endfirsthead
            	// and have to repeat the head if there's no first head
                handleCaption("\\caption",ldp,oc);
                ldp.append("\\\\").nl();
                if (hasRowType(RowType.FIRST_HEAD)) {
                	handleRows(ldp,oc,RowType.FIRST_HEAD,true,true);
                }
                else {
                	handleRows(ldp,oc,RowType.HEAD,true,true);            	
                }
                ldp.nl().append("\\endfirsthead").nl();
            }
            else if (hasRowType(RowType.FIRST_HEAD)) {
            	// Otherwise we only need it if the table contains a first head
            	handleRows(ldp,oc,RowType.FIRST_HEAD,true,true);
                ldp.nl().append("\\endfirsthead").nl();            	
            }
			
            // Head
            handleRows(ldp,oc,RowType.HEAD,true,true);
            ldp.nl().append("\\endhead").nl();
            
            // Foot
            handleRows(ldp,oc,RowType.FOOT,false,true);
            ldp.nl().append("\\endfoot").nl();

            // Last foot
            if (caption!=null && !bCaptionAbove) {
            	// If there's a caption below, we must use \endlastfoot
            	// and have to repeat the foot if there's no last foot
                if (hasRowType(RowType.LAST_FOOT)) {
                	handleRows(ldp,oc,RowType.LAST_FOOT,false,true);
                    ldp.nl();
                }
                else if (hasRowType(RowType.FOOT)){
                	handleRows(ldp,oc,RowType.FOOT,false,true);            	
                    ldp.nl();
                }
                handleCaption("\\caption",ldp,oc);
                ldp.append("\\endlastfoot").nl();
            }
            else if (hasRowType(RowType.LAST_FOOT)) {
            	// Otherwise we only need it if the table contains a last foot
            	handleRows(ldp,oc,RowType.LAST_FOOT,false,true);
                ldp.nl().append("\\endlastfoot").nl();            	
            }
			
            // Body
            handleRows(ldp,oc,RowType.BODY,!hasRowType(RowType.HEAD) && !hasRowType(RowType.FIRST_HEAD),true);
			
            ldp.nl().append(baTable.getAfter()).nl();
        }
		
        private void handleTableFloat(LaTeXDocumentPortion ldp, Context oc) {
            ldp.append("\\begin{table}");
            if (config.getFloatOptions().length()>0) {
                ldp.append("[").append(config.getFloatOptions()).append("]");
            }
            ldp.nl();
			
            ldp.append(baTableAlign.getBefore());
		
            // Caption above
            if (caption!=null && bCaptionAbove) {
                handleCaption("\\caption",ldp,oc);
            }

            // The table
            handleHyperTarget(ldp);
            ldp.append(baTable.getBefore()).nl();
            handleRows(ldp,oc,RowType.HEAD,true,true);
            ldp.nl();
            handleRows(ldp,oc,RowType.BODY,!hasRowType(RowType.HEAD),true);
            ldp.append(baTable.getAfter()).nl();
			
            // Caption below
            if (caption!=null && !bCaptionAbove) {
                handleCaption("\\caption",ldp,oc);
            }
			
            ldp.nl().append(baTableAlign.getAfter());

            ldp.append("\\end{table}").nl();
        }
		
        private void handleTabular(LaTeXDocumentPortion ldp, Context oc) {
            ldp.append(baTableAlign.getBefore());

            // Caption above
            if (caption!=null && bCaptionAbove) {
                TableConverter.this.handleCaption(caption,ldp,oc);
            }

            // The table
            handleHyperTarget(ldp);
            ldp.append(baTable.getBefore()).nl();
            handleRows(ldp,oc,RowType.HEAD,true,true);
            ldp.nl();
            handleRows(ldp,oc,RowType.BODY,!hasRowType(RowType.HEAD),true);
            ldp.nl().append(baTable.getAfter()).nl();
			
            // Caption below
            if (caption!=null && !bCaptionAbove) {
                TableConverter.this.handleCaption(caption,ldp,oc);
            }

            ldp.append(baTableAlign.getAfter());
        }
		
        private void handleCaption(String sCommand, LaTeXDocumentPortion ldp, Context oc) {
            ldp.append(sCommand);
            palette.getCaptionCv().handleCaptionBody(caption,ldp,oc,false);
        }
		
        private void handleHyperTarget(LaTeXDocumentPortion ldp) {
            // We may need a hyperlink target
            if (!table.isSubTable()) {
                palette.getFieldCv().addTarget(table.getTableName(),"|table",ldp);
            }
        }
		
        private void handleRows(LaTeXDocumentPortion ldp, Context oc, RowType rowType, boolean bLineBefore, boolean bLineAfter) {
            int nRowCount = table.getRowCount();
            int nColCount = table.getColCount();
            boolean bFirst = true;
            int nPreviousRow = -1;
            for (int nRow=0; nRow<nRowCount; nRow++) {
            	if (rowTypes[nRow]==rowType) {
            		// Add interrow material from previous row, if any
            		if (nPreviousRow>-1) {
            			ldp.append(formatter.getInterrowMaterial(nPreviousRow+1)).nl();
            		}
            		nPreviousRow = nRow;

            		// If it's the first row, add top interrow material
            		if (bFirst && bLineBefore) {
            			String sInter = formatter.getInterrowMaterial(nRow);
            			if (sInter.length()>0) { ldp.append(sInter).nl(); }
            			bFirst=false;
            		}
            		// Export columns in this row
            		Context icRow = (Context) oc.clone();
            		BeforeAfter baRow = new BeforeAfter();
            		formatter.applyRowStyle(nRow,baRow,icRow);
            		if (!baRow.isEmpty()) {
            			ldp.append(baRow.getBefore());
            			if (!formatter.isSimple()) { ldp.nl(); }
            		}   
            		int nCol = 0;
            		while (nCol<nColCount) {
            			Element cell = (Element) table.getCell(nRow,nCol);
            			if (XMLString.TABLE_TABLE_CELL.equals(cell.getNodeName())) {
            				Context icCell = (Context) icRow.clone();
            				BeforeAfter baCell = new BeforeAfter();
            				formatter.applyCellStyle(nRow,nCol,baCell,icCell);
            				ldp.append(baCell.getBefore());
            				if (nCol==nColCount-1) { icCell.setInLastTableColumn(true); }
            				palette.getBlockCv().traverseBlockText(cell,ldp,icCell);
            				ldp.append(baCell.getAfter());
            			}
            			// Otherwise ignore; the cell is covered by a \multicolumn entry.
            			// (table:covered-table-cell)
            			int nColSpan = Misc.getPosInteger(cell.getAttribute(
            					XMLString.TABLE_NUMBER_COLUMNS_SPANNED),1);
            			if (nCol+nColSpan<nColCount) {
            				if (formatter.isSimple()) { ldp.append(" & "); }
            				else { ldp.append(" &").nl(); }
            			}
            			nCol+=nColSpan;
            		}
            		ldp.append("\\\\");
            	}
            }
    		// Add interrow material from last row, if required
            if (nPreviousRow>-1 && bLineAfter) {
            	ldp.append(formatter.getInterrowMaterial(nPreviousRow+1));
            }

        }
	
    }

    
}
