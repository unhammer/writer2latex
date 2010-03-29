/************************************************************************
*
*  ConfigurationDialogBase.java
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
*  Version 1.2 (2010-03-26)
*
*/ 

package org.openoffice.da.comp.w2lcommon.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sun.star.awt.XContainerWindowEventHandler;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.XWindow;
import com.sun.star.io.NotConnectedException;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ucb.CommandAbortedException;
import com.sun.star.ucb.XSimpleFileAccess2;
import com.sun.star.ui.dialogs.ExecutableDialogResults;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XStringSubstitution;

import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.lib.uno.adapter.XInputStreamToInputStreamAdapter;
import com.sun.star.lib.uno.adapter.XOutputStreamToOutputStreamAdapter;

import writer2latex.api.Config;
import writer2latex.api.ConverterFactory;

import org.openoffice.da.comp.w2lcommon.helper.DialogAccess;
import org.openoffice.da.comp.w2lcommon.helper.StyleNameProvider;

/** This is a base implementation of a uno component which supports several option pages
 *  with a single <code>XContainerWindowEventHandler</code>. The title of the dialogs
 *  are used to differentiate between the individual pages
 */
public abstract class ConfigurationDialogBase extends WeakBase implements XContainerWindowEventHandler {
	
	// The full path to the configuration file we handle
	private String sConfigFileName = null;
	
	// The component context
	protected XComponentContext xContext;
	
	// UNO simple file access service
	protected XSimpleFileAccess2 sfa2 = null;
	
	// Access to display names of the styles in the current document
	protected StyleNameProvider styleNameProvider = null;
	
	// The configuration implementation
	protected Config config;
	
	// The individual page handlers (the subclass must populate this)
	protected Map<String,PageHandler> pageHandlers = new HashMap<String,PageHandler>();
	
	// The subclass must provide these:
	
	// MIME type of the document type we configure
	protected abstract String getMIMEType();
	
	// The dialog library containing the "new" and "delete" dialogs
	protected abstract String getDialogLibraryName();
	
	// The file name used for persistent storage of the edited configuration
	protected abstract String getConfigFileName();

	/** Create a new <code>ConfigurationDialogBase</code> */
	public ConfigurationDialogBase(XComponentContext xContext) {
       this.xContext = xContext;

       // Get the SimpleFileAccess service
       try {
           Object sfaObject = xContext.getServiceManager().createInstanceWithContext(
               "com.sun.star.ucb.SimpleFileAccess", xContext);
           sfa2 = (XSimpleFileAccess2) UnoRuntime.queryInterface(XSimpleFileAccess2.class, sfaObject);
       }
       catch (com.sun.star.uno.Exception e) {
           // failed to get SimpleFileAccess service (should not happen)
       }

       // Create the config file name
       XStringSubstitution xPathSub = null;
       try {
           Object psObject = xContext.getServiceManager().createInstanceWithContext(
              "com.sun.star.util.PathSubstitution", xContext);
           xPathSub = (XStringSubstitution) UnoRuntime.queryInterface(XStringSubstitution.class, psObject);
           sConfigFileName = xPathSub.substituteVariables("$(user)/"+getConfigFileName(), false);
       }
       catch (com.sun.star.uno.Exception e) {
           // failed to get PathSubstitution service (should not happen)
       }
       
       // Create the configuration
       config = ConverterFactory.createConverter(getMIMEType()).getConfig();
       
       // Get the style name provider
       styleNameProvider = new StyleNameProvider(xContext);
	}
       	
	// Implement XContainerWindowEventHandler
	public boolean callHandlerMethod(XWindow xWindow, Object event, String sMethod)
		throws com.sun.star.lang.WrappedTargetException {
		XDialog xDialog = (XDialog)UnoRuntime.queryInterface(XDialog.class, xWindow);
		String sTitle = xDialog.getTitle();
	   
		if (!pageHandlers.containsKey(sTitle)) {
			throw new com.sun.star.lang.WrappedTargetException("Unknown dialog "+sTitle);
		}
	   
		DialogAccess dlg = new DialogAccess(xDialog);

		try {
			if (sMethod.equals("external_event") ) {
				return handleExternalEvent(dlg, sTitle, event);
			}
		}
		catch (com.sun.star.uno.RuntimeException e) {
			throw e;
		}
		catch (com.sun.star.uno.Exception e) {
			throw new com.sun.star.lang.WrappedTargetException(sMethod, this, e);
		}
		
		return pageHandlers.get(sTitle).handleEvent(dlg, sMethod);
	}
	
	private boolean handleExternalEvent(DialogAccess dlg, String sTitle, Object aEventObject) throws com.sun.star.uno.Exception {
		try {
			String sMethod = AnyConverter.toString(aEventObject);
			if (sMethod.equals("ok")) {
				loadConfig(); // The file may have been changed by other pages, thus we reload
				pageHandlers.get(sTitle).getControls(dlg);
				saveConfig();
				return true;
			}
			else if (sMethod.equals("back") || sMethod.equals("initialize")) {
				loadConfig();
				pageHandlers.get(sTitle).setControls(dlg);
				return true;
			}
		}
		catch (com.sun.star.lang.IllegalArgumentException e) {
			throw new com.sun.star.lang.IllegalArgumentException(
				"Method external_event requires a string in the event object argument.", this,(short) -1);
		}
		return false;
	}
	
	// Load the user configuration from file
	private void loadConfig() {
		if (sfa2!=null && sConfigFileName!=null) {
			try {
				XInputStream xIs = sfa2.openFileRead(sConfigFileName);
	            if (xIs!=null) {
	            	InputStream is = new XInputStreamToInputStreamAdapter(xIs);
	                config.read(is);
	                is.close();
	                xIs.closeInput();
	            }
	        }
	        catch (IOException e) {
	            // ignore
	        }
	        catch (NotConnectedException e) {
	            // ignore
	        }
	        catch (CommandAbortedException e) {
	            // ignore
	        }
	        catch (com.sun.star.uno.Exception e) {
	            // ignore
	        }
	    }
	}
	   
	// Save the user configuration
	private void saveConfig() {
		if (sfa2!=null && sConfigFileName!=null) {
			try {
				//Remove the file if it exists
	           	if (sfa2.exists(sConfigFileName)) {
	           		sfa2.kill(sConfigFileName);
	           	}
	           	// Then write the new contents
	            XOutputStream xOs = sfa2.openFileWrite(sConfigFileName);
	            if (xOs!=null) {
	            	OutputStream os = new XOutputStreamToOutputStreamAdapter(xOs);
	                config.write(os);
	                os.close();
	                xOs.closeOutput();
	            }
	        }
	        catch (IOException e) {
	            // ignore
	        }
	        catch (NotConnectedException e) {
	            // ignore
	        }
	        catch (CommandAbortedException e) {
	            // ignore
	        }
	        catch (com.sun.star.uno.Exception e) {
	            // ignore
	        }
	    }
	}
	
	// Inner class to handle the individual option pages
	protected abstract class PageHandler {
		protected abstract void getControls(DialogAccess dlg);
		
		protected abstract void setControls(DialogAccess dlg);
		
		protected abstract boolean handleEvent(DialogAccess dlg, String sMethodName);

		// Methods to handle user controlled lists
		protected XDialog getDialog(String sDialogName) {
			XMultiComponentFactory xMCF = xContext.getServiceManager();
		   	try {
		   		Object provider = xMCF.createInstanceWithContext(
		   				"com.sun.star.awt.DialogProvider2", xContext);
		   		XDialogProvider2 xDialogProvider = (XDialogProvider2)
		   		UnoRuntime.queryInterface(XDialogProvider2.class, provider);
		   		String sDialogUrl = "vnd.sun.star.script:"+sDialogName+"?location=application";
		   		return xDialogProvider.createDialog(sDialogUrl);
		   	}
		   	catch (Exception e) {
		   		return null;
		   	}
		}

		private boolean deleteItem(String sName) {
			XDialog xDialog=getDialog(getDialogLibraryName()+".DeleteDialog");
		   	if (xDialog!=null) {
		   		DialogAccess ddlg = new DialogAccess(xDialog);
		   		String sLabel = ddlg.getLabelText("DeleteLabel");
		   		sLabel = sLabel.replaceAll("%s", sName);
		   		ddlg.setLabelText("DeleteLabel", sLabel);
		   		boolean bDelete = xDialog.execute()==ExecutableDialogResults.OK;
		   		xDialog.endExecute();
		   		return bDelete;
		   	}
		   	return false;
		}
		   
		private boolean deleteCurrentItem(DialogAccess dlg, String sListName) {
		   	String[] sItems = dlg.getListBoxStringItemList(sListName);
		   	short nSelected = dlg.getListBoxSelectedItem(sListName);
		   	if (nSelected>=0 && deleteItem(sItems[nSelected])) {
		   		int nOldLen = sItems.length;
		   		String[] sNewItems = new String[nOldLen-1];
		   		if (nSelected>0) {
		   			System.arraycopy(sItems, 0, sNewItems, 0, nSelected);
		   		}
		   		if (nSelected<nOldLen-1) {
		       		System.arraycopy(sItems, nSelected+1, sNewItems, nSelected, nOldLen-1-nSelected);
		   		}
		   		dlg.setListBoxStringItemList(sListName, sNewItems);
		   		short nNewSelected = nSelected<nOldLen-1 ? nSelected : (short)(nSelected-1);
					dlg.setListBoxSelectedItem(sListName, nNewSelected);
					return true;
		   	}
		   	return false;
		}
		   
		private String newItem(Set<String> suggestions) {
		   	XDialog xDialog=getDialog(getDialogLibraryName()+".NewDialog");
		   	if (xDialog!=null) {
		   		int nCount = suggestions.size();
		   		String[] sItems = new String[nCount];
		   		int i=0;
		   		for (String s : suggestions) {
		   			sItems[i++] = s;
		   		}
		   		sortStringArray(sItems);
		   		DialogAccess ndlg = new DialogAccess(xDialog);
		   		ndlg.setListBoxStringItemList("Name", sItems);
		   		String sResult = null;
		   		if (xDialog.execute()==ExecutableDialogResults.OK) {
		   			DialogAccess dlg = new DialogAccess(xDialog);
		   			sResult = dlg.getTextFieldText("Name");
		   		}
		   		xDialog.endExecute();
		   		return sResult;
		   	}
		   	return null;
		}
		   
		private String appendItem(DialogAccess dlg, String sListName, Set<String> suggestions) {
		   	String[] sItems = dlg.getListBoxStringItemList(sListName);
		   	String sNewItem = newItem(suggestions);
		   	if (sNewItem!=null) {
		   		int nOldLen = sItems.length;
		   		for (short i=0; i<nOldLen; i++) {
		   			if (sNewItem.equals(sItems[i])) {
		   				// Item already exists, select the existing one
		   				dlg.setListBoxSelectedItem(sListName, i);
		   				return null;
		   			}
		   		}
		   		String[] sNewItems = new String[nOldLen+1];
		   		System.arraycopy(sItems, 0, sNewItems, 0, nOldLen);
		   		sNewItems[nOldLen]=sNewItem;
		   		dlg.setListBoxStringItemList(sListName, sNewItems);
		   		dlg.setListBoxSelectedItem(sListName, (short)nOldLen);
		   	}
		   	return sNewItem;
		}
		
		protected void sortStringArray(String[] theArray) {
			// TODO: Get locale from OOo rather than the system
			Collator collator = Collator.getInstance();
			Arrays.sort(theArray, collator);
		}
		
		// Methods to set and get controls based on config
		protected void checkBoxFromConfig(DialogAccess dlg, String sCheckBoxName, String sConfigName) {
			dlg.setCheckBoxStateAsBoolean(sCheckBoxName, "true".equals(config.getOption(sConfigName)));
		}
		
		protected void checkBoxToConfig(DialogAccess dlg, String sCheckBoxName, String sConfigName) {
			config.setOption(sConfigName, Boolean.toString(dlg.getCheckBoxStateAsBoolean(sCheckBoxName)));
		}
		
		protected void textFieldFromConfig(DialogAccess dlg, String sTextBoxName, String sConfigName) {
			dlg.setTextFieldText(sTextBoxName, config.getOption(sConfigName));	
		}
		
		protected void textFieldToConfig(DialogAccess dlg, String sTextBoxName, String sConfigName) {
			config.setOption(sConfigName, dlg.getTextFieldText(sTextBoxName));
		}
		
		protected void listBoxFromConfig(DialogAccess dlg, String sListBoxName, String sConfigName, String[] sConfigValues, short nDefault) {
			String sCurrentValue = config.getOption(sConfigName);
			int nCount = sConfigValues.length;
			for (short i=0; i<nCount; i++) {
				if (sConfigValues[i].equals(sCurrentValue)) {
					dlg.setListBoxSelectedItem(sListBoxName, i);
					return;
				}
			}
			dlg.setListBoxSelectedItem(sListBoxName, nDefault);
		}
		
		protected void listBoxToConfig(DialogAccess dlg, String sListBoxName, String sConfigName, String[] sConfigValues) {
			config.setOption(sConfigName, sConfigValues[dlg.getListBoxSelectedItem(sListBoxName)]);
		}
				
	}
	
}

