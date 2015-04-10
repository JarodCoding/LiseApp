package de.lisemeitnerschule.liseapp.Internal;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.sql.SQLException;

/**
 * Created by Pascal on 21.3.15.
 */


public class InternalContentProvider extends ContentProvider {

    InternalDatabaseHelper databaseHelper;
    @Override
    public boolean onCreate() {
        databaseHelper = new InternalDatabaseHelper(getContext());
        return true;
    }
    private static final UriMatcher URI_MATCHER;
        //URI IDs
            private static final int NEWS_LIST = 1;
            private static final int NEWS_ID = 2;

        // prepare the UriMatcher
            static {
                URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
                URI_MATCHER.addURI(InternalContract.AUTHORITY,
                        "news",
                        NEWS_LIST);
                URI_MATCHER.addURI(InternalContract.AUTHORITY,
                        "news/#",
                        NEWS_ID);
            }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (URI_MATCHER.match(uri)) {
            case NEWS_LIST:
                builder.setTables(InternalDatabaseHelper.TBL_NEWS);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = InternalContract.News.SORT_ORDER_DEFAULT;
                }
                break;
            case NEWS_ID:
                builder.setTables(InternalDatabaseHelper.TBL_NEWS);
                builder.appendWhere(InternalContract.News._ID + " = " +
                        uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI: " + uri);
        }
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor =
                builder.query(
                        db,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
        cursor.setNotificationUri(
                getContext().getContentResolver(),
                uri);
        return cursor;
    }

    /*public News getNews(String id){
        Cursor cursor = query(InternalContract.News.CONTENT_URI, InternalContract.News.PROJECTION_ALL,null,null,null);
    }*/
    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)){
            case NEWS_LIST:
                return InternalContract.News.CONTENT_DIR_TYPE;
            case NEWS_ID:
                return InternalContract.News.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values){
        switch (URI_MATCHER.match(uri)) {
            case NEWS_LIST:
                long id = 0;
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                id = db.insertWithOnConflict(InternalDatabaseHelper.TBL_NEWS,null,values,SQLiteDatabase.CONFLICT_REPLACE); //replace existing values
                try {
                    return getUriForId(id, uri);
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to insert into the Database",e);
                }


            default:
                throw new IllegalArgumentException(
                        "Unsupported URI for insertion: " + uri);

        }
    }
    private Uri getUriForId(long id, Uri uri) throws SQLException {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
                getContext().
                        getContentResolver().
                        notifyChange(itemUri, null);
            return itemUri;

        }
        // s.th. went wrong:
        throw new SQLException(
                "Problem while inserting into uri: " + uri);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int delCount;
        switch (URI_MATCHER.match(uri)) {
            case NEWS_LIST:
                delCount = db.delete(
                        InternalDatabaseHelper.TBL_NEWS,
                        selection,
                        selectionArgs);
                break;
            case NEWS_ID:
                String idStr = uri.getLastPathSegment();
                String where = InternalContract.News._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = db.delete(
                        InternalDatabaseHelper.TBL_NEWS,
                        where,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // notify all listeners of changes:
        if (delCount > 0 ) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return delCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int updateCount;
        switch (URI_MATCHER.match(uri)) {
            case NEWS_LIST:
                updateCount = db.update(
                        InternalDatabaseHelper.TBL_NEWS,
                        values,
                        selection,
                        selectionArgs);
                break;
            case NEWS_ID:
                String idStr = uri.getLastPathSegment();
                String where = InternalContract.News._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(
                        InternalDatabaseHelper.TBL_NEWS,
                        values,
                        where,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // notify all listeners of changes:
        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }
}
