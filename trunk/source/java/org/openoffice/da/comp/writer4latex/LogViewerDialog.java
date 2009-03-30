/************************************************************************
 *
 *  LogViewerDialog.java
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
 *  Version 1.2 (2009-03-26)
 *
 */ 
 
package org.openoffice.da.comp.writer4latex;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.sun.star.awt.XDialog;
import com.sun.star.uno.XComponentContext;

import org.openoffice.da.comp.w2lcommon.helper.DialogBase;

/** This class provides a uno component which displays logfiles
 */
public class LogViewerDialog extends DialogBase 
    implements com.sun.star.lang.XInitialization {

    /** The component will be registered under this name.
     */
    public static String __serviceName = "org.openoffice.da.writer4latex.LogViewerDialog";

    /** The component should also have an implementation name.
     */
    public static String __implementationName = "org.openoffice.da.comp.writer4latex.LogViewerDialog";

    /** Return the name of the library containing the dialog
     */
    public String getDialogLibraryName() {
        return "W4LDialogs";
    }
	
    private String sBaseUrl = null;
    private String sLaTeXLog = null;
    private String sBibTeXLog = null;
    private String sMakeindexLog = null;

    /** Return the name of the dialog within the library
     */
    public String getDialogName() {
        return "LogViewer";
    }
	
    public void initialize() {
        if (sBaseUrl!=null) {
            sLaTeXLog = readTextFile(sBaseUrl+".log");
            sBibTeXLog = readTextFile(sBaseUrl+".blg");
            sMakeindexLog = readTextFile(sBaseUrl+".ilg");
            setComboBoxText("LogContents",sLaTeXLog);
        }
    }
	
    public void finalize() {
    }

    /** Create a new LogViewerDialog */
    public LogViewerDialog(XComponentContext xContext) {
        super(xContext);
    }
	
    // Implement com.sun.star.lang.XInitialization
    public void initialize( Object[] object )
        throws com.sun.star.uno.Exception {
        if ( object.length > 0 ) {
            if (object[0] instanceof String) {
                sBaseUrl = (String) object[0];
            }
        }
    }

   // Implement XDialogEventHandler
    public boolean callHandlerMethod(XDialog xDialog, Object event, String sMethod) {
        if (sMethod.equals("ViewLaTeXLog")) {
            setComboBoxText("LogContents", sLaTeXLog);
        }
        else if (sMethod.equals("ViewBibTeXLog")) {
            setComboBoxText("LogContents", sBibTeXLog);
        }
        else if (sMethod.equals("ViewMakeindexLog")) {
            setComboBoxText("LogContents", sMakeindexLog);
        }
        return true;
    }
	
    public String[] getSupportedMethodNames() {
        String[] sNames = { "ViewLaTeXLog", "ViewBibTeXLog", "ViewMakeindexLog" };
        return sNames;
    }
	
    // Utility methods
	
    private String readTextFile(String sUrl) {
        StringBuffer buf = new StringBuffer();
        try {
            File file = new File(new URI(sUrl));
            if (file.exists() && file.isFile()) {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
                int n;
                do {
                    n = isr.read();
                    if (n>-1) { buf.append((char)n); }
                }
                while (n>-1);
                isr.close();
            }
        }
        catch (URISyntaxException e) {
            return "";
        }
        catch (IOException e) {
            return "";
        }
        return buf.toString();
    }
	
}



