package com.example.kiemtrath;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

public class BookProvider  extends ContentProvider {
    static  final String PROVIDER_NAME="com.example.kiemtrath.BookProvider";
    static final String URL="content://"+PROVIDER_NAME+"/books";
    static  final Uri CONTENT_URI=Uri.parse(URL);

    static  final String  ID="id";
    static  final  String NAME="name";
    static  final  String AUTHOR="author";

    private  static HashMap<String,String>BOOKS_PROJECTION_MAP;

    static  final  int BOOKS=1;
    static final int BOOK_ID=2;

    static  final UriMatcher uriMatcher;
    static {
        uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "books",BOOKS);
        uriMatcher.addURI(PROVIDER_NAME,"books/#",BOOK_ID);
    }
    private SQLiteDatabase db;
    static final String DATABASE_NAME="BookManager";
    static final String BOOKS_TABLE_NAME="books";
    static final int DATABASE_VERSION=1;
    static  final String CREATE_DB_TABLE =
            " CREATE TABLE " + BOOKS_TABLE_NAME+"("
            +ID+" INTEGER PRIMARY KEY, "
            +NAME+" TEXT NOT NULL, "
            +AUTHOR+" TEXT NOT NULL);";

    private  static  class  DatabaseHelper extends SQLiteOpenHelper{
        public DatabaseHelper(Context context) {
            super(context,DATABASE_NAME,null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+BOOKS_TABLE_NAME);
            onCreate(db);
        }
    }
    @Override
    public boolean onCreate() {
        Context context=getContext();
        DatabaseHelper dbHelper=new DatabaseHelper(context);
        db=dbHelper.getWritableDatabase();
        return (db==null)?false:true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qb=new SQLiteQueryBuilder();
        qb.setTables(BOOKS_TABLE_NAME);
        switch (uriMatcher.match(uri)){
            case BOOKS:
                qb.setProjectionMap(BOOKS_PROJECTION_MAP);
                break;
            case BOOK_ID:
                qb.appendWhere(ID+"="+uri.getPathSegments().get(1));
                break;
        }
        if (sortOrder==null||sortOrder==""){
            sortOrder=NAME;
        }
        Cursor c=qb.query(db,projection,selection,selectionArgs,null,null,sortOrder);
        c.setNotificationUri(getContext().getContentResolver(),uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        long rowID=db.insert(BOOKS_TABLE_NAME,"",values);
        if(rowID>0){
            Uri _uri= ContentUris.withAppendedId(CONTENT_URI,rowID);
            getContext().getContentResolver().notifyChange(_uri,null);
            return _uri;
        }
        throw new SQLException("Faile to add a record into"+uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count=0;
        switch (uriMatcher.match(uri)){
            case BOOKS:
                count=db.delete(BOOKS_TABLE_NAME,selection,selectionArgs);
                break;
            case BOOK_ID:
                String id=uri.getPathSegments().get(1);
                count=db.delete(BOOKS_TABLE_NAME, ID + " = " + id +(!TextUtils.isEmpty(selection) ?"AND ("+ selection +')':""),selectionArgs);
                break;
            default:
                throw  new  IllegalArgumentException("Unkown URI"+uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count=0;
        switch (uriMatcher.match(uri)){
            case BOOKS:
                count=db.update(BOOKS_TABLE_NAME,values,selection,selectionArgs);
                break;
            case BOOK_ID:
                count=db.update(BOOKS_TABLE_NAME, values,ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ?"AND ("+ selection +')':""),selectionArgs);
                break;
            default:
                throw  new  IllegalArgumentException("Unkown URI"+uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }
}
