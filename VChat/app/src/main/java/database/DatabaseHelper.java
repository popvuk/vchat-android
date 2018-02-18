package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by MiljanaMilena on 12/20/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper sInstance;

    public static final String DATABASE_NAME = "chatup.db";
    public static final int DATABASE_VERSION = 4;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private static final String CREATE_TABLE_CONTACTS = "CREATE TABLE " + ContactsTable.TABLE_NAME
            + "(" + ContactsTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ContactsTable.USERNAME + " TEXT NOT NULL,"
            + ContactsTable.CHAT_ID + " TEXT ,"
            + ContactsTable.PHOTO + " TEXT,"
            + ContactsTable.CONTACT_ID + " TEXT NOT NULL,"
            + ContactsTable.PHONE_NO +" TEXT NOT NULL" + ")";

    private static final String DROP_TABLE_CONTACTS = "DROP TABLE IF EXISTS "+ContactsTable.TABLE_NAME;


    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_TABLE_CONTACTS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(DROP_TABLE_CONTACTS);
        onCreate(db);
    }

}