/************************************************************************
 *
 *  TeXify.java
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
 *  Version 1.2 (2009-03-30)
 *
 */ 
 
package org.openoffice.da.comp.writer4latex;

import java.io.File;
import java.io.IOException;

import com.sun.star.uno.XComponentContext;
       
/** This class builds LaTeX documents into dvi, postscript or pdf and displays
 *  the result.
 */
public final class TeXify {

    /** Backend format generic (dvi) */
    public static final short GENERIC = 1;

    /** Backend format dvips (postscript) */
    public static final short DVIPS = 2;

    /** Backend format pdfTeX (pdf) */
    public static final short PDFTEX = 3;

    // Define the applications to run for each backend
    private static final String[] genericTexify = {
        ExternalApps.LATEX, ExternalApps.BIBTEX, ExternalApps.MAKEINDEX,
        ExternalApps.LATEX, ExternalApps.MAKEINDEX, ExternalApps.LATEX };
    private static final String[] pdfTexify = {
        ExternalApps.PDFLATEX, ExternalApps.BIBTEX, ExternalApps.MAKEINDEX,
        ExternalApps.PDFLATEX, ExternalApps.MAKEINDEX, ExternalApps.PDFLATEX };
    private static final String[] dvipsTexify = {
        ExternalApps.LATEX, ExternalApps.BIBTEX, ExternalApps.MAKEINDEX,
        ExternalApps.LATEX, ExternalApps.MAKEINDEX, ExternalApps.LATEX,
        ExternalApps.DVIPS };

    // Global objects
    //private XComponentContext xContext;
    private ExternalApps externalApps; 
	
    public TeXify(XComponentContext xContext) {
        //this.xContext = xContext;
        externalApps = new ExternalApps(xContext);
    }
	
    /** Process a document
     *  @param file the LaTeX file to process
     *  @param nBackend the desired backend format (generic, dvips, pdftex)
     *  @param bView set the true if the result should be displayed in the viewer
     *  @throws IOException if the document cannot be read
     */
    public void process(File file, short nBackend, boolean bView) throws IOException {
        // Remove extension from file
        if (file.getName().endsWith(".tex")) {
            file = new File(file.getParentFile(),
                   file.getName().substring(0,file.getName().length()-4));
        }
        
        // Update external apps from registry
        externalApps.load();

        // Process LaTeX document
        if (nBackend==GENERIC) {
            doTeXify(genericTexify, file);
            if (externalApps.execute(ExternalApps.DVIVIEWER,
                new File(file.getParentFile(),file.getName()+".dvi").getPath(),
                file.getParentFile(), false)>0) {
                throw new IOException("Error executing dvi viewer");
            }
        }
        else if (nBackend==PDFTEX) {
            doTeXify(pdfTexify, file);
            if (externalApps.execute(ExternalApps.PDFVIEWER,
                new File(file.getParentFile(),file.getName()+".pdf").getPath(),
                file.getParentFile(), false)>0) {
                throw new IOException("Error executing pdf viewer");
            }
        }
        else if (nBackend==DVIPS) {
            doTeXify(dvipsTexify, file);
            if (externalApps.execute(ExternalApps.POSTSCRIPTVIEWER,
                new File(file.getParentFile(),file.getName()+".ps").getPath(),
                file.getParentFile(), false)>0) {
                throw new IOException("Error executing postscript viewer");
            }
        }

    }
	
    private void doTeXify(String[] sAppList, File file) throws IOException {
        for (int i=0; i<sAppList.length; i++) {
            // Execute external application
            int nReturnCode = externalApps.execute(
                sAppList[i], file.getName(), file.getParentFile(), true);
            if (nReturnCode>0) {
                //throw new IOException("Error executing "+sAppList[i]);
            } 
        }
    }

}