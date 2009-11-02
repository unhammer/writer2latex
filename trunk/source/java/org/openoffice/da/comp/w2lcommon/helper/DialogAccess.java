/************************************************************************
 *
 *  DialogAccess.java
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
 *  Version 1.2 (2009-11-02)
 *
 */ 

package org.openoffice.da.comp.w2lcommon.helper;

import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.beans.XPropertySet;
import com.sun.star.uno.UnoRuntime;


/** This class provides some convenient methods to access a uno dialog
 */
public class DialogAccess {
	
	/** The XDialog containing the controls. The subclass must override this */
	private Object xDialog = null;
	
    // State of a checkbox
    
	public static final short CHECKBOX_NOT_CHECKED = 0;
    public static final short CHECKBOX_CHECKED = 1;
    public static final short CHECKBOX_DONT_KNOW = 2;
    
    public DialogAccess(XDialog xDialog) {
    	this.xDialog = xDialog;
    }

    //////////////////////////////////////////////////////////////////////////
    // Helpers to access controls in the dialog (to be used by the subclass)
    // Note: The helpers fail silently if an exception occurs. Could query the
    // the ClassId property for the control type and check that the property
    // exists to ensure a correct behaviour in all cases, but as long as the
    // helpers are used correctly, this doesn't really matter.
	
    // Get the properties of a named control in the dialog
    public XPropertySet getControlProperties(String sControlName) {
        XControlContainer xContainer = (XControlContainer)
            UnoRuntime.queryInterface(XControlContainer.class, xDialog);
        XControl xControl = xContainer.getControl(sControlName);
        XControlModel xModel = xControl.getModel();
        XPropertySet xPropertySet = (XPropertySet)
            UnoRuntime.queryInterface(XPropertySet.class, xModel);
        return xPropertySet;
    }

    
    public boolean getControlEnabled(String sControlName) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            return ((Boolean) xPropertySet.getPropertyValue("Enabled")).booleanValue();
        }
        catch (Exception e) {
            // Will fail if the control does not exist
        	return false;
        }
    }
	
    public void setControlEnabled(String sControlName, boolean bEnabled) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            xPropertySet.setPropertyValue("Enabled", new Boolean(bEnabled));
        }
        catch (Exception e) {
            // Will fail if the control does not exist
        }
    }
	
    public short getCheckBoxState(String sControlName) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            return ((Short) xPropertySet.getPropertyValue("State")).shortValue();
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a checkbox
            return CHECKBOX_DONT_KNOW;
        }
    }
	
    public boolean getCheckBoxStateAsBoolean(String sControlName) {
	    return getCheckBoxState(sControlName)==CHECKBOX_CHECKED;
    }
	
    public void setCheckBoxState(String sControlName, short nState) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            xPropertySet.setPropertyValue("State",new Short(nState));
        }
        catch (Exception e) {
            // will fail if the control does not exist or is not a checkbox or
            // nState has an illegal value
        }
    }
	
    public void setCheckBoxStateAsBoolean(String sControlName, boolean bChecked) {
	    setCheckBoxState(sControlName,bChecked ? CHECKBOX_CHECKED : CHECKBOX_NOT_CHECKED);
    }
	
    public String[] getListBoxStringItemList(String sControlName) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            return (String[]) xPropertySet.getPropertyValue("StringItemList");
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a list box
            return new String[0];
        }
    }
	
    public void setListBoxStringItemList(String sControlName, String[] items) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            xPropertySet.setPropertyValue("StringItemList",items);
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a list box
        }
    }
	
    public short getListBoxSelectedItem(String sControlName) {
        // Returns the first selected element in case of a multiselection
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            short[] selection = (short[]) xPropertySet.getPropertyValue("SelectedItems");
            return selection[0];
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a list box
            return -1;
        }
    }
	
    public void setListBoxSelectedItem(String sControlName, short nIndex) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            short[] selection = new short[1];
            selection[0] = nIndex;
            xPropertySet.setPropertyValue("SelectedItems",selection);
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a list box or
            // nIndex is an illegal value
        }
    }
	
    public short getListBoxLineCount(String sControlName) {
        // Returns the first selected element in case of a multiselection
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            return ((Short) xPropertySet.getPropertyValue("LineCount")).shortValue();
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a list box
            return 0;
        }
    }
	
    public void setListBoxLineCount(String sControlName, short nLineCount) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            xPropertySet.setPropertyValue("LineCount",new Short(nLineCount));
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a list box or
            // nLineCount is an illegal value
        }
    }
	
    public String getComboBoxText(String sControlName) {
        // Returns the text of a combobox
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            return (String) xPropertySet.getPropertyValue("Text");
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a combo
            return "";
        }
    }
	
    public void setComboBoxText(String sControlName, String sText) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            xPropertySet.setPropertyValue("Text", sText);
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a combo box or
            // nText is an illegal value
        }
    }
	
    public String getTextFieldText(String sControlName) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            return (String) xPropertySet.getPropertyValue("Text");
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a text field
            return "";
        }
    }
	
    public void setTextFieldText(String sControlName, String sText) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            xPropertySet.setPropertyValue("Text",sText);
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a text field
        }
    }
	
    public String getFormattedFieldText(String sControlName) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            return (String) xPropertySet.getPropertyValue("Text");
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a formatted field
            return "";
        }
    }
	
    public void setFormattedFieldText(String sControlName, String sText) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            xPropertySet.setPropertyValue("Text",sText);
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a formatted field
        }
    }
	
    public int getNumericFieldValue(String sControlName) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            return ((Double) xPropertySet.getPropertyValue("Value")).intValue();
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a numeric field
            return 0;
        }
    }
	
    public void setNumericFieldValue(String sControlName, int nValue) {
        XPropertySet xPropertySet = getControlProperties(sControlName);
        try {
            xPropertySet.setPropertyValue("Value",new Double(nValue));
        }
        catch (Exception e) {
            // Will fail if the control does not exist or is not a numeric field
        }
    }
	
}