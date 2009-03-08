/************************************************************************
 *
 *  L10n.java
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
 *  Version 1.0 (2009-03-05)
 *
 */

package writer2latex.xhtml;

// This class handles localized strings (used for navigation)
public class L10n {
    public final static int UP = 0;
    public final static int FIRST = 1;
    public final static int PREVIOUS = 2;
    public final static int NEXT = 3;
    public final static int LAST = 4;
    public final static int CONTENTS = 5;
    public final static int INDEX = 6;
    public final static int HOME = 7;
    public final static int DIRECTORY = 8;
    public final static int DOCUMENT = 9;

    private String sLocale="en-US";
	
    public void setLocale(String sLocale) {
        if (sLocale!=null) { this.sLocale = sLocale;}
    }
	
    public void setLocale(String sLanguage, String sCountry) {
        if (sLanguage!=null) {
            if (sCountry!=null) { sLocale = sLanguage + "-" + sCountry; }
            else  { sLocale = sLanguage; }
        }
    }
	
    public String get(int nString) {
        if (sLocale.startsWith("de")) { // german
            switch (nString) {
                case UP: return "Nach oben";
                case FIRST : return "Anfang";
                case PREVIOUS : return "Vorheriges";
                case NEXT : return "N\u00ccchstes";
                case LAST : return "Ende";
                case CONTENTS : return "Inhalte";
                case INDEX : return "Index";
                case HOME : return "Home";
                case DIRECTORY: return "Verzeichnis";
                case DOCUMENT: return "Dokument";
            }
        }
        if (sLocale.startsWith("fr")) { // french
            switch (nString) {
            	case UP: return "Haut";
            	case FIRST : return "D\u00e9but";
            	case PREVIOUS : return "Pr\u00e9c\u00e9dent";
            	case NEXT : return "Suivant";
            	case LAST : return "Dernier";
            	case CONTENTS : return "Contenus";
            	case INDEX : return "Index";
                case HOME : return "Documents Personnels";
            	case DIRECTORY: return "R\u00e9pertoire";
            	case DOCUMENT: return "Document";
            }
        }
        if (sLocale.startsWith("es")) { // spanish
            switch (nString) {
                case UP: return "Arriba";
                case FIRST : return "Primero";
                case PREVIOUS : return "Previo";
                case NEXT : return "Siguiente";
                case LAST : return "\u00daltimo";
                case CONTENTS : return "Contenido";
                case INDEX : return "\u00cdndice";
                case HOME : return "Inicio";
                case DIRECTORY: return "Directorio";
                case DOCUMENT: return "Documento";
            }
        }
        if (sLocale.startsWith("da")) { // danish
            switch (nString) {
                case UP: return "Op";
                case FIRST : return "F\u00F8rste";
                case PREVIOUS : return "Forrige";
                case NEXT : return "N\u00E6ste";
                case LAST : return "Sidste";
                case CONTENTS : return "Indhold";
                case INDEX : return "Stikord";
                case HOME : return "Hjem";
                case DIRECTORY: return "Mappe";
                case DOCUMENT: return "Dokument";
            }
        }
        if (sLocale.startsWith("ru")) { // russian
            switch (nString) {
            	case UP: return "\u0412\u0432\u0435\u0440\u0445";
            	case FIRST : return "\u041f\u0435\u0440\u0432\u0430\u044f";
            	case PREVIOUS : return "\u041f\u0440\u0435\u0434\u044b\u0434\u0443\u0449\u0430\u044f";
            	case NEXT : return "\u0421\u043b\u0435\u0434\u0443\u044e\u0449\u0430\u044f";
            	case LAST : return "\u041f\u043e\u0441\u043b\u0435\u0434\u043d\u044f\u044f";
            	case CONTENTS : return "\u0421\u043e\u0434\u0435\u0440\u0436\u0430\u043d\u0438\u0435";
            	case INDEX : return "\u0421\u043f\u0438\u0441\u043e\u043a";
            	case HOME : return "\u0414\u043e\u043c\u043e\u0439";
            	case DIRECTORY: return "\u0414\u0438\u0440\u0435\u043a\u0442\u043e\u0440\u0438\u044f";
            	case DOCUMENT: return "\u0414\u043e\u043a\u0443\u043c\u0435\u043d\u0442";
            }
        }
        if (sLocale.startsWith("uk")) { // ukrainian
            switch (nString) {
            	case UP: return "\u041d\u0430\u0433\u043e\u0440\u0443";
            	case FIRST : return "\u041f\u0435\u0440\u0448\u0430";
            	case PREVIOUS : return "\u041f\u043e\u043f\u0435\u0440\u0435\u0434\u043d\u044f";
            	case NEXT : return "\u041d\u0430\u0441\u0442\u0443\u043f\u043d\u0430";
            	case LAST : return "\u041e\u0441\u0442\u0430\u043d\u043d\u044f";
            	case CONTENTS : return "\u0417\u043c\u0456\u0441\u0442";
            	case INDEX : return "\u0421\u043f\u0438\u0441\u043e\u043a";
            	case HOME : return "\u0414\u043e\u0434\u043e\u043c\u0443";
            	case DIRECTORY: return "\u0422\u0435\u043a\u0430";
            	case DOCUMENT: return "\u0414\u043e\u043a\u0443\u043c\u0435\u043d\u0442";
            }
        }
        if (sLocale.startsWith("hr")) { // croatian
            switch (nString) {
                case UP: return "Up";
                case FIRST : return "Prvi";
                case PREVIOUS : return "Prethodan";
                case NEXT : return "slijede\u0107i";
                case LAST : return "Zadnji";
                case CONTENTS : return "Sadr\u017Eaj";
                case INDEX : return "Indeks";
                case DIRECTORY: return "Directory";
                case DOCUMENT: return "Document";
            }
        }
        // english - default
        switch (nString) {
            case UP: return "Up";
            case FIRST : return "First";
            case PREVIOUS : return "Previous";
            case NEXT : return "Next";
            case LAST: return "Last";
            case CONTENTS : return "Contents";
            case INDEX : return "Index";
            case HOME : return "Home";
            case DIRECTORY: return "Directory";
            case DOCUMENT: return "Document";
        }
        return "???";
    }
}
