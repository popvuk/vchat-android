package database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by MiljanaMilena on 12/21/2017.
 */

public class DbAdapter {

    private final Context context;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public DbAdapter(Context ctx)
    {
        this.context = ctx;
        dbHelper = DatabaseHelper.getInstance(this.context);
    }

    public DbAdapter open() throws SQLException
    {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        dbHelper.close();
    }

    public SQLiteDatabase getDb()
    {
        return db;
    }
}
