/************************************************************************
 *
 *  ConfigurationDialog.java
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
 *  Version 1.2 (2010-04-12)
 *
 */ 
 
package org.openoffice.da.comp.writer4latex;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XContainerWindowEventHandler;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.XWindow;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.ui.dialogs.ExecutableDialogResults;
import com.sun.star.ui.dialogs.XExecutableDialog;
import com.sun.star.ui.dialogs.XFilePicker;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import com.sun.star.lib.uno.helper.WeakBase;

import org.openoffice.da.comp.w2lcommon.helper.DialogAccess;
import org.openoffice.da.comp.w2lcommon.helper.FilePicker;

/** This class provides a uno component which implements the configuration
 *  of Writer4LaTeX
 */
public final class ConfigurationDialog
    extends WeakBase
    implements XServiceInfo, XContainerWindowEventHandler {

    private XComponentContext xContext;
    private FilePicker filePicker;
    
    private ExternalApps externalApps;
    
    /** The component will be registered under this name.
     */
    public static String __serviceName = "org.openoffice.da.writer4latex.ConfigurationDialog";

    /** The component should also have an implementation name.
     */
    public static String __implementationName = "org.openoffice.da.comp.writer4latex.ConfigurationDialog";

    /** Create a new ConfigurationDialog */
    public ConfigurationDialog(XComponentContext xContext) {
        this.xContext = xContext;
        externalApps = new ExternalApps(xContext);
        filePicker = new FilePicker(xContext);
    }
	
    // Implement XContainerWindowEventHandler
    public boolean callHandlerMethod(XWindow xWindow, Object event, String sMethod)
        throws com.sun.star.lang.WrappedTargetException {
        try {
            if (sMethod.equals("external_event") ){
                return handleExternalEvent(xWindow, event);
            }
            else if (sMethod.equals("ApplicationChange")) {
                return changeApplication(xWindow);
            }
            else if (sMethod.equals("BrowseClick")) {
                return browseForExecutable(xWindow);
            }
            else if (sMethod.equals("ExecutableUnfocus")) {
                return updateApplication(xWindow);
            }
            else if (sMethod.equals("OptionsUnfocus")) {
                return updateApplication(xWindow);
            }
            else if (sMethod.equals("AutomaticClick")) {
                return autoConfigure(xWindow);
            }
        }
        catch (com.sun.star.uno.RuntimeException e) {
            throw e;
        }
        catch (com.sun.star.uno.Exception e) {
            throw new com.sun.star.lang.WrappedTargetException(sMethod, this, e);
        }
        return false;
    }
	
    public String[] getSupportedMethodNames() {
        String[] sNames = { "external_event", "ApplicationChange", "BrowseClick", "ExecutableUnfocus", "OptionsUnfocus", "AutomaticClick" };
        return sNames;
    }
    
    // Implement the interface XServiceInfo
    public boolean supportsService(String sServiceName) {
        return sServiceName.equals(__serviceName);
    }

    public String getImplementationName() {
        return __implementationName;
    }
    
    public String[] getSupportedServiceNames() {
        String[] sSupportedServiceNames = { __serviceName };
        return sSupportedServiceNames;
    }
	
    // Private stuff
    
    private boolean handleExternalEvent(com.sun.star.awt.XWindow xWindow, Object aEventObject)
        throws com.sun.star.uno.Exception {
        try {
            String sMethod = AnyConverter.toString(aEventObject);
            if (sMethod.equals("ok")) {
                externalApps.save();
                return true;
            } else if (sMethod.equals("back") || sMethod.equals("initialize")) {
                externalApps.load();
                return changeApplication(xWindow);
            }
        }
        catch (com.sun.star.lang.IllegalArgumentException e) {
            throw new com.sun.star.lang.IllegalArgumentException(
            "Method external_event requires a string in the event object argument.", this,(short) -1);
        }
        return false;
    }
	
    private boolean changeApplication(XWindow xWindow) {
        String sAppName = getSelectedAppName(xWindow);
        if (sAppName!=null) {
            String[] s = externalApps.getApplication(sAppName);
            setComboBoxText(xWindow, "Executable", s[0]);
            setComboBoxText(xWindow, "Options", s[1]);
        }
        return true;
    }
	
    private boolean browseForExecutable(XWindow xWindow) {
    	String sPath = filePicker.getPath();
    	if (sPath!=null) {
    		try {
				setComboBoxText(xWindow, "Executable", new File(new URI(sPath)).getCanonicalPath());
			}
    		catch (IOException e) {
			}
    		catch (URISyntaxException e) {
			}
    		updateApplication(xWindow);
    	}     
    	return true;
    }
	
    private boolean updateApplication(XWindow xWindow) {
        String sAppName = getSelectedAppName(xWindow);
        if (sAppName!=null) {
            externalApps.setApplication(sAppName, getComboBoxText(xWindow, "Executable"), getComboBoxText(xWindow, "Options"));
        }
        return true;
    }
    
    // Unix: Test to determine wether a certain application is available in the OS
    // Requires "which", hence unix only
    private boolean hasApp(String sAppName) {
        try {
			Vector<String> command = new Vector<String>();
			command.add("which");
			command.add(sAppName);
			
            ProcessBuilder pb = new ProcessBuilder(command);
            Process proc = pb.start();        

            // Gobble the error stream of the application
            StreamGobbler errorGobbler = new 
                StreamGobbler(proc.getErrorStream(), "ERROR");            
            
            // Gooble the output stream of the application
            StreamGobbler outputGobbler = new 
                StreamGobbler(proc.getInputStream(), "OUTPUT");
                
            errorGobbler.start();
            outputGobbler.start();
                                    
            // The application exists if the process exits with 0
            return proc.waitFor()==0;
        }
        catch (InterruptedException e) {
            return false;
        }
        catch (IOException e) {
            return false;
        }
    }
    
    // Unix: Configure a certain application testing the availability
    private boolean configureApp(String sName, String sAppName, String sArguments) {
    	if (hasApp(sAppName)) {
    		externalApps.setApplication(sName, sAppName, sArguments);
    		return true;
    	}
    	else {
    		externalApps.setApplication(sName, "???", "???");
    		return false;
    	}
    }
    
    // Unix: Configure a certain application, reporting the availability
    private boolean configureApp(String sName, String sAppName, String sArguments, StringBuffer info) {
    	if (hasApp(sAppName)) {
    		externalApps.setApplication(sName, sAppName, sArguments);
    		info.append("Found "+sAppName+" - OK\n");
    		return true;
    	}
    	else {
    		externalApps.setApplication(sName, "???", "???");
    		info.append("Failed to find "+sAppName+"\n");
    		return false;
    	}
    }
    
    // Unix: Configure a certain application testing the availability
    // This variant uses an array of potential apps
    private boolean configureApp(String sName, String[] sAppNames, String sArguments, StringBuffer info) {
    	for (String sAppName : sAppNames) {
    		if (configureApp(sName, sAppName, sArguments)) {
    			info.append("Found "+sName+": "+sAppName+" - OK\n");
    			return true;
    		}
    	}
    	info.append("Failed to find "+sName+"\n");
    	return false;
    }
    
    // Windows: Test that the given path contains a given executable
    private boolean containsExecutable(String sPath,String sExecutable) {
    	File dir = new File(sPath);
    	if (dir.exists() && dir.canRead()) {
    		File exe = new File(dir,sExecutable);
    		return exe.exists();
    	}
    	return false;
    }
	
    // Windows: Configure a certain MikTeX application
    private boolean configureMikTeX(String sPath, String sName, String sAppName, String sArguments, StringBuffer info, boolean bRequired) {
    	File app = new File(new File(sPath),sAppName+".exe");
    	if (app.exists()) {
    		externalApps.setApplication(sName, sAppName, sArguments);
			info.append("  Found "+sName+": "+sAppName+" - OK\n");
			return true;
    	}
    	else if (bRequired) {
    		externalApps.setApplication(sName, "???", "???");
			info.append("  Failed to find "+sName+"\n");    		
    	}
    	return false;
    }

    // Configure the applications automatically (OS dependent)
    private boolean autoConfigure(XWindow xWindow) {
		String sOsName = System.getProperty("os.name");
		String sOsVersion = System.getProperty("os.version");
		String sOsArch = System.getProperty("os.arch");
		StringBuffer info = new StringBuffer();
		info.append("Results of configuration:\n\n");
		info.append("Your system identifies itself as "+sOsName+" version "+sOsVersion+ " (" + sOsArch +")\n\n");
    	if (sOsName.startsWith("Windows")) {
    		// Assume MikTeX
    		String sMikTeXPath = null;
    		String[] sPaths = System.getenv("PATH").split(";");
    		for (String s : sPaths) {
    			if (s.toLowerCase().indexOf("miktex")>-1 && containsExecutable(s,"latex.exe")) {
    				sMikTeXPath = s;
    				break;
    			}
    		}
    		if (sMikTeXPath==null) {
        		for (String s : sPaths) {
        			if (containsExecutable(s,"latex.exe")) {
        				sMikTeXPath = s;
        				break;
        			}
        		}    			
    		}

    		boolean bFoundTexworks = false;
    		if (sMikTeXPath!=null) {
    			info.append("Found MikTeX\n");
    			configureMikTeX(sMikTeXPath, ExternalApps.LATEX, "latex", "--interaction=batchmode %s", info, true);
    			configureMikTeX(sMikTeXPath, ExternalApps.PDFLATEX, "pdflatex", "--interaction=batchmode %s", info, true);
    			configureMikTeX(sMikTeXPath, ExternalApps.XELATEX, "xelatex", "--interaction=batchmode %s", info, true);
    			configureMikTeX(sMikTeXPath, ExternalApps.DVIPS, "dvips", "%s", info, true);
    			configureMikTeX(sMikTeXPath, ExternalApps.BIBTEX, "bibtex", "%s", info, true);
    			configureMikTeX(sMikTeXPath, ExternalApps.MAKEINDEX, "makeindex", "%s", info, true);
    			configureMikTeX(sMikTeXPath, ExternalApps.MK4HT, "mk4ht", "%c %s", info, true);
    			configureMikTeX(sMikTeXPath, ExternalApps.DVIVIEWER, "yap", "--single-instance %s", info, true);
    			// MikTeX 2.8 provides texworks for pdf viewing
    			bFoundTexworks = configureMikTeX(sMikTeXPath, ExternalApps.PDFVIEWER, "texworks", "%s", info, true);
    		}
    		else {
    			info.append("Failed to find MikTeX\n");
    			info.append("Writer4LaTeX has been configured to work if MikTeX is added to your path\n");
    			externalApps.setApplication(ExternalApps.LATEX, "latex", "--interaction=batchmode %s");
    			externalApps.setApplication(ExternalApps.PDFLATEX, "pdflatex", "--interaction=batchmode %s");
    			externalApps.setApplication(ExternalApps.XELATEX, "xelatex", "--interaction=batchmode %s");
    			externalApps.setApplication(ExternalApps.DVIPS, "dvips", "%s");
    			externalApps.setApplication(ExternalApps.BIBTEX, "bibtex", "%s");
    			externalApps.setApplication(ExternalApps.MAKEINDEX, "makeindex", "%s");
    			externalApps.setApplication(ExternalApps.MK4HT, "mk4ht", "%c %s");
    			externalApps.setApplication(ExternalApps.DVIVIEWER, "yap", "--single-instance %s");
    		}
    		info.append("\n");
    		
    		// Assume gsview for pdf and ps
    		String sGsview = null;
    		String sProgramFiles = System.getenv("ProgramFiles");
    		if (sProgramFiles!=null) {
    			if (containsExecutable(sProgramFiles+"\\ghostgum\\gsview","gsview32.exe")) {
    				sGsview = sProgramFiles+"\\ghostgum\\gsview\\gsview32.exe";
    			}
    		}
    		
    		if (sGsview!=null) {
    			info.append("Found gsview - OK\n");
    		}
    		else {
    			info.append("Failed to find gsview\n");
    			sGsview = "gsview32.exe"; // at least this helps a bit..
    		}
    		if (!bFoundTexworks) {
    			externalApps.setApplication(ExternalApps.PDFVIEWER, sGsview, "-e \"%s\"");
    		}
    		externalApps.setApplication(ExternalApps.POSTSCRIPTVIEWER, sGsview, "-e \"%s\"");  

    	}
    	else { // Assume a unix-like system supporting the "which" command
    		configureApp(ExternalApps.LATEX, "latex", "--interaction=batchmode %s",info);
    		configureApp(ExternalApps.PDFLATEX, "pdflatex", "--interaction=batchmode %s",info);
    		configureApp(ExternalApps.XELATEX, "xelatex", "--interaction=batchmode %s",info);
    		configureApp(ExternalApps.DVIPS, "dvips", "%s",info);
    		configureApp(ExternalApps.BIBTEX, "bibtex", "%s",info);
    		configureApp(ExternalApps.MAKEINDEX, "makeindex", "%s",info);
    		configureApp(ExternalApps.MK4HT, "mk4ht", "%c %s",info);    		
    		// We have several possible viewers
    		String[] sDviViewers = {"evince", "okular", "xdvi"};
    		configureApp(ExternalApps.DVIVIEWER, sDviViewers, "%s",info);
    		String[] sPdfViewers =  {"evince", "okular", "xpdf"};
    		configureApp(ExternalApps.PDFVIEWER, sPdfViewers, "%s",info);
    		String[] sPsViewers =  {"evince", "okular", "ghostview"};
    		configureApp(ExternalApps.POSTSCRIPTVIEWER, sPsViewers, "%s",info);
    		
    	}
    	// Maybe add some info for Ubuntu users
    	// sudo apt-get install texlive
    	// sudo apt-get install texlive-xetex
    	// sudo apt-get install texlive-latex-extra
    	// sudo apt-get install tex4ht
		displayAutoConfigInfo(info.toString());
    	changeApplication(xWindow);
        return true;
    }
	
    private String getSelectedAppName(XWindow xWindow) {
        short nItem = getListBoxSelectedItem(xWindow, "Application");
        //String sAppName = null;
        switch (nItem) {
            case 0: return ExternalApps.LATEX;
            case 1: return ExternalApps.PDFLATEX;
            case 2: return ExternalApps.XELATEX;
            case 3: return ExternalApps.DVIPS;
            case 4: return ExternalApps.BIBTEX;
            case 5: return ExternalApps.MAKEINDEX;
            case 6: return ExternalApps.MK4HT;
            case 7: return ExternalApps.DVIVIEWER;
            case 8: return ExternalApps.PDFVIEWER;
            case 9: return ExternalApps.POSTSCRIPTVIEWER;
        }
        return "???";
    }
    
    private XDialog getDialog(String sDialogName) {
    	XMultiComponentFactory xMCF = xContext.getServiceManager();
    	try {
    		Object provider = xMCF.createInstanceWithContext(
    				"com.sun.star.awt.DialogProvider2", xContext);
    		XDialogProvider2 xDialogProvider = (XDialogProvider2)
    		UnoRuntime.queryInterface(XDialogProvider2.class, provider);
    		String sDialogUrl = "vnd.sun.star.script:"+sDialogName+"?location=application";
    		return xDialogProvider.createDialogWithHandler(sDialogUrl, this);
    	}
    	catch (Exception e) {
    		return null;
    	}
     }

    
    private void displayAutoConfigInfo(String sText) {
    	XDialog xDialog = getDialog("W4LDialogs.AutoConfigInfo");
    	if (xDialog!=null) {
    		DialogAccess info = new DialogAccess(xDialog);
    		info.setTextFieldText("Info", sText);
    		xDialog.execute();
    		xDialog.endExecute();
    	}
    }
    
    // Some helpers copied from DialogBase
    private XPropertySet getControlProperties(XWindow xWindow, String sControlName) {
        XControlContainer xContainer = (XControlContainer)
            UnoRuntime.queryInterface(XControlContainer.class, xWindow);
        XControl xControl = xContainer.getControl(sControlName);
        XControlModel xModel = xControl.getModel();
        XPropertySet xPropertySet = (XPropertySet)
            UnoRuntime.queryInterface(XPropertySet.class, xModel);
        return xPropertySet;
    }

    private String getComboBoxText(XWindow xWindow, String sControlName) {
        // Returns the text of a combobox
        XPropertySet xPropertySet = getControlProperties(xWindow, sControlName);
        try {
            return (String) xPropertySet.getPropertyValue("Text");
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a combo
            return "";
        }
    }
	
    private void setComboBoxText(XWindow xWindow, String sControlName, String sText) {
        XPropertySet xPropertySet = getControlProperties(xWindow, sControlName);
        try {
            xPropertySet.setPropertyValue("Text", sText);
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a combo box or
            // nText is an illegal value
        }
    }
    
    private short getListBoxSelectedItem(XWindow xWindow, String sControlName) {
        // Returns the first selected element in case of a multiselection
        XPropertySet xPropertySet = getControlProperties(xWindow, sControlName);
        try {
            short[] selection = (short[]) xPropertySet.getPropertyValue("SelectedItems");
            return selection[0];
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a list box
            return -1;
        }
    }
	

	

	
}



