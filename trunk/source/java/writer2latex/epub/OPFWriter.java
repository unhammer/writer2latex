/************************************************************************
 *
 *  OPFWriter.java
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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import writer2latex.api.ContentEntry;
import writer2latex.api.ConverterResult;
import writer2latex.api.OutputFile;
import writer2latex.xmerge.NewDOMDocument;

/** This class writes an OPF-file for an EPUB document (see http://www.idpf.org/2007/opf/OPF_2.0_final_spec.html).
 */
public class OPFWriter extends NewDOMDocument {

	public OPFWriter(ConverterResult cr, String sUUID) {
		super("book", "opf");
		
        // create DOM
        Document contentDOM = null;
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            DOMImplementation domImpl = builder.getDOMImplementation();
            DocumentType doctype = domImpl.createDocumentType("package","",""); 
            contentDOM = domImpl.createDocument("http://www.idpf.org/2007/opf","package",doctype);
        }
        catch (ParserConfigurationException t) { // this should never happen
            throw new RuntimeException(t);
        }
        
        // Populate the DOM tree
        Element pack = contentDOM.getDocumentElement();
        pack.setAttribute("version", "2.0");
        pack.setAttribute("xmlns","http://www.idpf.org/2007/opf");
        pack.setAttribute("unique-identifier", "BookId");
        
        // Meta data, at least dc:title, dc:language and dc:identifier are required by the specification
        Element metadata = contentDOM.createElement("metadata");
        metadata.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
        metadata.setAttribute("xmlns:opf", "http://www.idpf.org/2007/opf");
        pack.appendChild(metadata);
        
        Element title = contentDOM.createElement("dc:title");
        metadata.appendChild(title);
        title.appendChild(contentDOM.createTextNode(cr.getMetaData().getTitle()));
        
        Element language = contentDOM.createElement("dc:language");
        metadata.appendChild(language);
        language.appendChild(contentDOM.createTextNode(cr.getMetaData().getLanguage()));
        
        Element identifier = contentDOM.createElement("dc:identifier");
        identifier.setAttribute("id", "BookId");
        identifier.setAttribute("opf:scheme", "UUID");
        metadata.appendChild(identifier);
        identifier.appendChild(contentDOM.createTextNode(sUUID));
        
        // Manifest must contain references to all the files in the XHTML converter result
        // Spine should contain references to all the master documents within the converter result
        Element manifest = contentDOM.createElement("manifest");
        pack.appendChild(manifest);
        
        Element spine = contentDOM.createElement("spine");
        spine.setAttribute("toc", "ncx");
        pack.appendChild(spine);
        
        int nMasterCount = 0;
        int nResourceCount = 0;
        Iterator<OutputFile> iterator = cr.iterator();
        while (iterator.hasNext()) {
        	OutputFile file = iterator.next();
        	Element item = contentDOM.createElement("item");
        	manifest.appendChild(item);
        	item.setAttribute("href",file.getFileName());
        	item.setAttribute("media-type", file.getMIMEType());
        	if (file.isMasterDocument()) {
        		String sId = "text"+(++nMasterCount);
        		item.setAttribute("id", sId);
        		
        		Element itemref = contentDOM.createElement("itemref");
        		itemref.setAttribute("idref", sId);
        		spine.appendChild(itemref);
        	}
        	else {
        		item.setAttribute("id", "resource"+(++nResourceCount));
        	}
        }
        
        Element item = contentDOM.createElement("item");
        item.setAttribute("href", "book.ncx");
        item.setAttribute("media-type", "application/x-dtbncx+xml");
        item.setAttribute("id", "ncx");
        manifest.appendChild(item);
        
        // The guide may contain references to some fundamental structural components
        Element guide = contentDOM.createElement("guide");
        pack.appendChild(guide);        
       	addGuideReference(contentDOM,guide,"title-page",cr.getTitlePageFile());
       	addGuideReference(contentDOM,guide,"text",cr.getTextFile());
       	addGuideReference(contentDOM,guide,"toc",cr.getTocFile());
       	addGuideReference(contentDOM,guide,"index",cr.getIndexFile());
       	addGuideReference(contentDOM,guide,"loi",cr.getLofFile());
       	addGuideReference(contentDOM,guide,"lot",cr.getLotFile());
       	addGuideReference(contentDOM,guide,"bibliography",cr.getBibliographyFile());
        
        setContentDOM(contentDOM);
	}
	
	private void addGuideReference(Document contentDOM, Element guide, String sType, ContentEntry entry) {
		if (entry!=null) {
			Element reference = contentDOM.createElement("reference");
			reference.setAttribute("type", sType);
			reference.setAttribute("title", entry.getTitle());
			String sHref = entry.getFile().getFileName();
			if (entry.getTarget()!=null) { sHref+="#"+entry.getTarget(); }
			reference.setAttribute("href", sHref);
			guide.appendChild(reference);
		}
	}

}
