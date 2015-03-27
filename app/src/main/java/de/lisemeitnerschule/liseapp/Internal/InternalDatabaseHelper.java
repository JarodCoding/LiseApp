package de.lisemeitnerschule.liseapp.Internal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Pascal on 23.3.15.
 */
public class InternalDatabaseHelper extends SQLiteOpenHelper {
    //General Information
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "LiseInternal.db";


    public InternalDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    //TABLES
        public static final String TBL_NEWS = "news";
        public static final String[] Tables = new String[]{TBL_NEWS};

    //SQL COMMANDS
        private static final String SQL_DELETE
            = "drop table "+TBL_NEWS+";";

        private static final String SQL_CREATE
            = "create table "+TBL_NEWS+"("     +
                InternalContract.News._ID      + "INT     PRIMARY KEY NOT NULL,"+
                InternalContract.News.Date     + "INT                 NOT NULL,"+
                InternalContract.News.Endtime  + "INT                 NOT NULL,"+
                InternalContract.News.Title    + "VARCHAR             NOT NULL,"+
                InternalContract.News.Teaser   + "VARCHAR             NOT NULL,"+
                InternalContract.News.Text     + "VARCHAR             NOT NULL,"+
                InternalContract.News.Category + "VARCHAR                     ,"+
                InternalContract.News.Image    + "VARCHAR                      "+
                ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
