package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import entity.Contact;

/**
 * Created by MiljanaMilena on 12/21/2017.
 */

public class ContactsTableQueries {

    private DbAdapter adapter;
    private SQLiteDatabase db;

    public ContactsTableQueries(Context context)
    {
        adapter = new DbAdapter(context);
    }

    public void open()
    {
        adapter.open();
        db = adapter.getDb();
    }

    public void close()
    {
        adapter.close();
    }

    public long insertContact(String username, String photo, String phone, String contact)
    {
        ContentValues val = new ContentValues();
        val.put(ContactsTable.USERNAME, username);
        val.put(ContactsTable.PHOTO, photo);
        val.put(ContactsTable.PHONE_NO, phone);
        val.put(ContactsTable.CONTACT_ID, contact);
        return db.insert(ContactsTable.TABLE_NAME, null, val);
    }

    public Cursor getAllContacts()
    {
        Cursor cursor = db.query(ContactsTable.TABLE_NAME, new String[]{ContactsTable.ID, ContactsTable.USERNAME, ContactsTable.PHONE_NO, ContactsTable.CHAT_ID, ContactsTable.PHOTO, ContactsTable.CONTACT_ID},
            null, null, null, null, null);

        return cursor;
    }

    public Cursor getContact(String conId) throws SQLException
    {
        Cursor cursor = db.query(ContactsTable.TABLE_NAME, new String[]{ContactsTable.ID, ContactsTable.USERNAME, ContactsTable.PHONE_NO,
        ContactsTable.CHAT_ID, ContactsTable.PHOTO, ContactsTable.CONTACT_ID}, ContactsTable.CONTACT_ID +"=?", new String[]{String.valueOf(conId)},null,null,null,null);

        return cursor;
    }

    public boolean deleteContact(long id)
    {
        return db.delete(ContactsTable.TABLE_NAME, ContactsTable.ID +"=?", new String[]{String.valueOf(id)}) > 0;

    }

    public void addChatId(String con, String chat)
    {
        ContentValues values = new ContentValues() ;
        values.put(ContactsTable.CHAT_ID, chat);
        db.update(ContactsTable.TABLE_NAME, values, ContactsTable.CONTACT_ID +"=?", new String[]{String.valueOf(con)});
    }

    }
