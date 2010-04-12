/************************************************************************
 *
 *  FilePicker.java
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

package org.openoffice.da.comp.w2lcommon.helper;

import com.sun.star.lang.XComponent;
import com.sun.star.ui.dialogs.ExecutableDialogResults;
import com.sun.star.ui.dialogs.XExecutableDialog;
import com.sun.star.ui.dialogs.XFilePicker;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public class FilePicker {
	
	private XComponentContext xContext; 
	
	/** Convenience wrapper class for the UNO file picker service
	 * 
	 *  @param xContext the UNO component context from which the file picker can be created
	 */
	public FilePicker(XComponentContext xContext) {
        this.xContext = xContext;
	}
	
	/** Get a user selected path with a file picker
	 * 
	 * @return the path or null if the dialog is canceled
	 */
	public String getPath() {
		// Create FilePicker
		Object filePicker = null;
		try {
			filePicker = xContext.getServiceManager().createInstanceWithContext("com.sun.star.ui.dialogs.FilePicker", xContext);
		}
		catch (com.sun.star.uno.Exception e) {
			return null;
		}

		// Display the FilePicker
		XFilePicker xFilePicker = (XFilePicker) UnoRuntime.queryInterface(XFilePicker.class, filePicker);
		XExecutableDialog xExecutable = (XExecutableDialog) UnoRuntime.queryInterface(XExecutableDialog.class, xFilePicker);

		// Get the path
		String sPath = null;
		
		if (xExecutable.execute() == ExecutableDialogResults.OK) {
			String[] sPathList = xFilePicker.getFiles();
			if (sPathList.length > 0) {
				sPath = sPathList[0];
			}     
		}

		// Dispose the file picker
		XComponent xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xFilePicker);
		xComponent.dispose();

		return sPath;
	}

}
