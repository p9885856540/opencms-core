/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsChpwd.java,v $
* Date   : $Date: 2002/07/12 09:54:58 $
* Version: $Revision: 1.9 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import javax.servlet.http.*;
import java.util.*;

/**
 * Template class for displaying the chpwd screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.9 $ $Date: 2002/07/12 09:54:58 $
 */

public class CmsChpwd extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the chpwd template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The chpwd template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {

        // the template to be displayed
        String template = null;
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        String oldpwd = (String)parameters.get(C_PARA_OLDPWD);
        String newpwd = (String)parameters.get(C_PARA_NEWPWD);
        String newpwdrepeat = (String)parameters.get(C_PARA_NEWPWDREPEAT);
        // a  password was given in the request so try to change it
        if((oldpwd != null) && (newpwd != null) && (newpwdrepeat != null)) {
            if(("".equals(oldpwd)) || ("".equals(newpwd)) || ("".equals(newpwdrepeat))) {
                xmlTemplateDocument.setData("details", "All fields must be filled.");
                template = "error";
            } else {
                // check if the new password and its repetition are identical
                if(newpwd.equals(newpwdrepeat)) {
                    // change the password
                    try {
                        cms.setPassword(cms.getRequestContext().currentUser().getName(),
                                        oldpwd, newpwd);
                        // return to the parameter dialog
                        try {
                            cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath() + C_WP_EXPLORER_PREFERENCES);
                        }
                        catch(Exception e) {
                            throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath()
                                    + C_WP_EXPLORER_PREFERENCES, CmsException.C_UNKNOWN_EXCEPTION, e);
                        }

                    // an error was thrown while setting the new password
                    }
                    catch(CmsException exp) {
                        // check if the old password was not correct
                        if((exp.getType() == CmsException.C_NO_USER)
                                || (exp.getType() == CmsException.C_NO_ACCESS)) {
                            xmlTemplateDocument.setData("details", Utils.getStackTrace(exp));
                            template = "error2";
                        } else {
                            if(exp.getType() == CmsException.C_INVALID_PASSWORD){
                                xmlTemplateDocument.setData("reasonOfError", exp.getMessage());
                                template = "error3";
                            }else{
                                throw exp;
                            }
                        }
                    }
                    //??? ednfal: when return null no error is shown!!!!
                    //return null;
                }
                else {
                    // the new passwords do not match
                    xmlTemplateDocument.setData("details", "The new passwords do not match.");
                    template = "error";
                }
            }
        }

        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
    }

    /**
     * Indicates if the results of this class are cacheable.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */

    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }
}
