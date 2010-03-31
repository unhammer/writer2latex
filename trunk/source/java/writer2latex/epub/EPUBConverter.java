/************************************************************************
 *
 *  EPUBConverter.java
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
 *  version 1.2 (2010-03-28)
 *
 */

package writer2latex.epub;

import java.io.IOException;
import java.io.InputStream;

import writer2latex.api.ConverterResult;
import writer2latex.base.ConverterResultImpl;
import writer2latex.xhtml.Xhtml11Converter;


/** This class converts an OpenDocument file to an EPUB document.
 */
public final class EPUBConverter extends Xhtml11Converter {
                        
    // Constructor
    public EPUBConverter() {
        super();
    }
	
    @Override public ConverterResult convert(InputStream is, String sTargetFileName) throws IOException {
    	setOPS(true);
    	ConverterResult xhtmlResult = super.convert(is, sTargetFileName);
    	
    	ConverterResultImpl epubResult = new ConverterResultImpl();
    	epubResult.addDocument(new EPUBWriter(xhtmlResult,sTargetFileName));
    	epubResult.setMetaData(xhtmlResult.getMetaData());
    	return epubResult;
    }

}