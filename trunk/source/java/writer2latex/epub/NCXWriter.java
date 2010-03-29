/************************************************************************
 *
 *  NCXWriter.java
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
 *  Copyright: 2001-2010 by Henrik Just
 *
 *  All Rights Reserved.
 * 
 *  version 1.2 (2010-03-29)
 *
 */

package writer2latex.epub;

import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import writer2latex.api.ContentEntry;
import writer2latex.api.ConverterResult;
import writer2latex.util.Misc;
import writer2latex.xmerge.NewDOMDocument;

/** This class creates the required NXC file for an EPUB document
 *  (see http://www.idpf.org/2007/opf/OPF_2.0_final_spec.html#Section2.4)
 * 
 */
public class NCXWriter extends NewDOMDocument {
	
	public NCXWriter(ConverterResult cr, String sUUID, String sFileName) {
		super(Misc.removeExtension(sFileName), "ncx");
		
        // create DOM
        Document contentDOM = null;
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            DOMImplementation domImpl = builder.getDOMImplementation();
            DocumentType doctype = domImpl.createDocumentType("ncx","",""); 
            contentDOM = domImpl.createDocument("http://www.daisy.org/z3986/2005/ncx/","ncx",doctype);
        }
        catch (ParserConfigurationException t) { // this should never happen
            throw new RuntimeException(t);
        }
        
        System.out.println("populating the ncx");
        
        // Populate the DOM tree
        Element ncx = contentDOM.getDocumentElement();
        ncx.setAttribute("version", "2005-1");
        ncx.setAttribute("xml:lang", cr.getMetaData().getLanguage());
        ncx.setAttribute("xmlns","http://www.daisy.org/z3986/2005/ncx/");
        
        // The head has four required meta data items
        Element head = contentDOM.createElement("head");
        ncx.appendChild(head);
        
        Element uid = contentDOM.createElement("meta");
        uid.setAttribute("name","dtb:uid");
        uid.setAttribute("content", sUUID);
        head.appendChild(uid);
        
        Element depth = contentDOM.createElement("meta");
        depth.setAttribute("name","dtb:depth");
        depth.setAttribute("content", "1");
        head.appendChild(depth);
        
        Element totalPageCount = contentDOM.createElement("meta");
        totalPageCount.setAttribute("name","dtb:totalPageCount");
        totalPageCount.setAttribute("content", "0");
        head.appendChild(totalPageCount);

        Element maxPageNumber = contentDOM.createElement("meta");
        maxPageNumber.setAttribute("name","dtb:maxPageNumber");
        maxPageNumber.setAttribute("content", "0");
        head.appendChild(maxPageNumber);
        
        // The ncx must contain a docTitle element
        Element docTitle = contentDOM.createElement("docTitle");
        ncx.appendChild(docTitle);
        Element docTitleText = contentDOM.createElement("text");
        docTitle.appendChild(docTitleText);
        docTitleText.appendChild(contentDOM.createTextNode(cr.getMetaData().getTitle()));
        
        // Build the navMap from the content table in the converter result
        Element navMap = contentDOM.createElement("navMap");
        ncx.appendChild(navMap);
        
        int nPlayOrder = 0;
        Iterator<ContentEntry> content = cr.getContent().iterator();
        while (content.hasNext()) {
        	ContentEntry entry = content.next();
        	//if (entry.getLevel()==1) {
        	System.out.println("Found content entry "+entry.getTitle());
        		Element navPoint = contentDOM.createElement("navPoint");
        		navPoint.setAttribute("playOrder", Integer.toString(++nPlayOrder));
        		navPoint.setAttribute("id", "text"+nPlayOrder);
        		navMap.appendChild(navPoint);
        		
        		Element navLabel = contentDOM.createElement("navLabel");
        		navPoint.appendChild(navLabel);
        		Element navLabelText = contentDOM.createElement("text");
        		navLabel.appendChild(navLabelText);
        		navLabelText.appendChild(contentDOM.createTextNode(entry.getTitle()));
        		
        		Element navPointContent = contentDOM.createElement("content");
    			String sHref = entry.getFile().getFileName();
    			if (entry.getTarget()!=null) { sHref+="#"+entry.getTarget(); }
        		navPointContent.setAttribute("src", sHref);
        		navPoint.appendChild(navPointContent);
        	//}
        }
        
        setContentDOM(contentDOM);
        
        System.out.println("finished populating the ncx");

        
	}
}
