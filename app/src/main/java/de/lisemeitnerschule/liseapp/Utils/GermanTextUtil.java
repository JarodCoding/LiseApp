package de.lisemeitnerschule.liseapp.Utils;

import android.text.TextUtils;

public class GermanTextUtil {
    public static String encodeHTML(String str){
        return replaceSpecialCharactersWithHTMLExpressions(TextUtils.htmlEncode(str));
    }
    public static String replaceSpecialCharactersWithNormalCharacters(String str){
        str =  str.replace("ü","ue");
        str =  str.replace("ö","oe");
        str =  str.replace("ä","ae");
        str =  str.replace("Ü","Ee");
        str =  str.replace("Ö","Oe");
        str =  str.replace("Ä","Ae");
        return str;
    }
    public static String replaceSpecialCharactersWithHTMLExpressions(String str){
        str =  str.replace("ü","&uuml;");
        str =  str.replace("ö","&ouml;");
        str =  str.replace("ä","&auml;");
        str =  str.replace("Ü","&Uuml;");
        str =  str.replace("Ö","&Ouml;");
        str =  str.replace("Ä","&Auml;");
        return str;
    }
}
