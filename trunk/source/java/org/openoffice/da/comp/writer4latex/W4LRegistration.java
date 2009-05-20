/************************************************************************
 *
 *  W4LRegistration.java
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
 *  Version 1.2 (2009-05-20) 
 *
 */ 
 
package org.openoffice.da.comp.writer4latex;

import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.registry.XRegistryKey;

import com.sun.star.comp.loader.FactoryHelper;
     
/** This class provides a static method to instantiate our uno components
 * on demand (__getServiceFactory()), and a static method to give
 * information about the components (__writeRegistryServiceInfo()).
 * Furthermore, it saves the XMultiServiceFactory provided to the
 * __getServiceFactory method for future reference by the componentes.
 */
public class W4LRegistration {
    
    public static XMultiServiceFactory xMultiServiceFactory;

    /**
     * Returns a factory for creating the service.
     * This method is called by the <code>JavaLoader</code>
     *
     * @return  returns a <code>XSingleServiceFactory</code> for creating the
     *          component
     *
     * @param   implName     the name of the implementation for which a
     *                       service is desired
     * @param   multiFactory the service manager to be used if needed
     * @param   regKey       the registryKey
     *
     * @see                  com.sun.star.comp.loader.JavaLoader
     */
    public static XSingleServiceFactory __getServiceFactory(String implName,
        XMultiServiceFactory multiFactory, XRegistryKey regKey) {
        xMultiServiceFactory = multiFactory;
        XSingleServiceFactory xSingleServiceFactory = null;
        if (implName.equals(Writer4LaTeX.__implementationName) ) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(Writer4LaTeX.class,
            Writer4LaTeX.__serviceName,
            multiFactory,						    
            regKey);
        }
        else if (implName.equals(TeXImportFilter.__implementationName) ) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(TeXImportFilter.class,
            TeXImportFilter.__serviceName,
            multiFactory,						    
            regKey);
        }
        else if (implName.equals(TeXDetectService.__implementationName) ) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(TeXDetectService.class,
            TeXDetectService.__serviceName,
            multiFactory,						    
            regKey);
        }
        else if (implName.equals(ConfigurationDialog.__implementationName) ) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(ConfigurationDialog.class,
            ConfigurationDialog.__serviceName,
            multiFactory,						    
            regKey);
        }
        else if (implName.equals(LogViewerDialog.__implementationName) ) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(LogViewerDialog.class,
            LogViewerDialog.__serviceName,
            multiFactory,						    
            regKey);
        }
        
        return xSingleServiceFactory;
    }
    
    /**
     * Writes the service information into the given registry key.
     * This method is called by the <code>JavaLoader</code>
     * <p>
     * @return  returns true if the operation succeeded
     * @param   regKey       the registryKey
     * @see                  com.sun.star.comp.loader.JavaLoader
     */
    public static boolean __writeRegistryServiceInfo(XRegistryKey regKey) {
        return
            FactoryHelper.writeRegistryServiceInfo(Writer4LaTeX.__implementationName,
                Writer4LaTeX.__serviceName, regKey) &
            FactoryHelper.writeRegistryServiceInfo(TeXImportFilter.__implementationName,
                        TeXImportFilter.__serviceName, regKey) &
            FactoryHelper.writeRegistryServiceInfo(TeXDetectService.__implementationName,
                        TeXDetectService.__serviceName, regKey) &
            FactoryHelper.writeRegistryServiceInfo(ConfigurationDialog.__implementationName,
                ConfigurationDialog.__serviceName, regKey) &
            FactoryHelper.writeRegistryServiceInfo(LogViewerDialog.__implementationName,
                LogViewerDialog.__serviceName, regKey);
    }
}

