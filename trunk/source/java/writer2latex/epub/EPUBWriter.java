/************************************************************************
 *
 *  EPUBWriter.java
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
 *  version 1.2 (2010-03-31)
 *
 */

package writer2latex.epub;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import writer2latex.api.ConverterResult;
import writer2latex.api.OutputFile;
import writer2latex.util.Misc;

/** This class repackages an XHTML document into EPUB format.
 *  Some filenames are hard wired in this implementation: The main directory is OEBPS and
 *  the OPF and NCX files are book.opf and book.ncx respectively 
 */
public class EPUBWriter implements OutputFile {
	
	private static final byte[] mimeBytes = { 'a', 'p', 'p', 'l', 'i', 'c', 'a', 't', 'i', 'o', 'n', '/',
		'e', 'p', 'u', 'b', '+', 'z', 'i', 'p'};
	
	private ConverterResult xhtmlResult;
	private String sFileName;
	
	public EPUBWriter(ConverterResult xhtmlResult, String sFileName) {
		this.xhtmlResult = xhtmlResult;
		this.sFileName = Misc.removeExtension(sFileName);
	}

	public String getFileName() {
		return sFileName+".epub";
	}

	public String getMIMEType() {
		return "application/epub+zip";
	}

	public boolean isMasterDocument() {
		return true;
	}

	public void write(OutputStream os) throws IOException {
		// Create a universal unique ID
		String sUUID = UUID.randomUUID().toString(); 
		
		ZipOutputStream zos = new ZipOutputStream(os);
		
		// Write uncompressed MIME type as first entry
		ZipEntry mimeEntry = new ZipEntry("mimetype");
		mimeEntry.setMethod(ZipEntry.STORED);
		mimeEntry.setCrc(0x2CAB616F);
		mimeEntry.setSize(mimeBytes.length);
		zos.putNextEntry(mimeEntry);
		zos.write(mimeBytes, 0, mimeBytes.length);
		zos.closeEntry();
		
		// Write container entry next
		OutputFile containerWriter = new ContainerWriter();
		ZipEntry containerEntry = new ZipEntry("META-INF/container.xml");
		zos.putNextEntry(containerEntry);
		writeZipEntry(containerWriter,zos);
		zos.closeEntry();
		
		// Then manifest
		OutputFile manifest = new OPFWriter(xhtmlResult, sUUID);
		ZipEntry manifestEntry = new ZipEntry("OEBPS/book.opf");
		zos.putNextEntry(manifestEntry);
		writeZipEntry(manifest,zos);
		zos.closeEntry();
		
		// And content table
		OutputFile ncx = new NCXWriter(xhtmlResult, sUUID);
		ZipEntry ncxEntry = new ZipEntry("OEBPS/book.ncx");
		zos.putNextEntry(ncxEntry);
		writeZipEntry(ncx,zos);
		zos.closeEntry();
		
		// Finally XHTML content
		Iterator<OutputFile> iter = xhtmlResult.iterator();
		while (iter.hasNext()) {
			OutputFile file = iter.next();
			ZipEntry entry = new ZipEntry("OEBPS/"+file.getFileName());
			zos.putNextEntry(entry);
			writeZipEntry(file, zos);
			zos.closeEntry();
		}
		
		zos.close();
	}
	
	private void writeZipEntry(OutputFile file, ZipOutputStream zos) throws IOException {
		// TODO: Fix this waste of memory :-)
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		file.write(baos);
		byte[] content = baos.toByteArray();
		zos.write(content, 0, content.length);		
	}

}
