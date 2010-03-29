/************************************************************************
 *
 *  MIMETypes.java
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
 *  Version 1.2 (2010-03-29)
 *
 */

package writer2latex.api;

/* Some helpers to handle the MIME types used by OOo and Writer2LaTeX
 */

public class MIMETypes {
    // Various graphics formats, see
    // http://api.openoffice.org/docs/common/ref/com/sun/star/graphic/MediaProperties.html#MimeType
    public static final String PNG="image/png";
    public static final String JPEG="image/jpeg";
    public static final String GIF="image/gif";
    public static final String TIFF="image/tiff";
    public static final String BMP="image/bmp";
    public static final String WMF="image/x-wmf";
    public static final String EPS="image/x-eps";
    // MIME type for SVM has changed
    //public static final String SVM="image/x-svm";
    public static final String SVM="application/x-openoffice-gdimetafile;windows_formatname=\"GDIMetaFile\"";
    public static final String PDF="application/pdf";
	
    // Desitination formats
    public static final String XHTML="text/html";
    /** This is a fake Mime type, for internal use only */
    public static final String XHTML11="application/xhtml11";
    public static final String XHTML_MATHML="application/xhtml+xml";
    public static final String XHTML_MATHML_XSL="application/xml";
    public static final String EPUB="application/epub+zip";
    public static final String LATEX="application/x-latex";
    public static final String BIBTEX="application/x-bibtex";
    public static final String TEXT="text";
	
}