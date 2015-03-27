package de.lisemeitnerschule.liseapp.Internal;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


/**
 * Created by Pascal on 21.3.15.
 */
public class InternalContract {
    /**
     * The authority of the lentitems provider.
     */
    public static final String AUTHORITY =
            "de.lisemeitnerschule.liseapp.Internal.InternalContentProvider";
    /**
     * The content URI for the top-level
     * lentitems authority.
     */
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY);




    private static final String BASE_DIR_CONTENT_TYPE  = ContentResolver.CURSOR_DIR_BASE_TYPE  +"/vnd.de.lisemeitnerschule.liseapp.Internal.";
    private static final String BASE_ITEM_CONTENT_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +"/vnd.de.lisemeitnerschule.liseapp.Internal.";

    public static final class News  implements  BaseColumns{
        //Data Information
            public static final Uri CONTENT_URI =
                    Uri.withAppendedPath(
                            InternalContract.CONTENT_URI,
                            "news");

            public static final String TABLE = "news";
            //Content Type
                public static final String CONTENT_DIR_TYPE  = BASE_DIR_CONTENT_TYPE  + "News";

                public static final String CONTENT_ITEM_TYPE = BASE_ITEM_CONTENT_TYPE + "News";



        //Fields
        public static final String Title      = "title"   ;
        public static final String Date       = "date"    ;
        public static final String Image      = "image"   ;
        public static final String Teaser     = "teaser"  ;
        public static final String Text       = "text"    ;
        public static final String Categorys  = "category";
        public static final String Endtime    = "endtime" ;
        public static final String User       = "user   " ;
        public static final String Author     = "author   " ;


        public static final String[] PROJECTION_USER =
                {_ID, Title, Date,Teaser,Text,Image};

        public static final String[] PROJECTION_ALL =
                {_ID, Title, Date,Teaser,Text,Image,Categorys,Endtime,User};
        public static final String SORT_ORDER_DEFAULT =
                Date + " DSC";






    }


}
