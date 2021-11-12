package cn.edu.bistu.cs.myweather;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * @author hp
 */
public class MyDbHelper extends SQLiteOpenHelper {
    private final Context context;
    private static final String CREATE_TABLE = "create table weather(" +
            "id integer primary key not null,"+
            "data String not null)";
    private static final String CREATE_CONCERN_TABLE = "create table concern(" +
            "city_code String primary key not null, " +
            "city_name String not null)";

    public MyDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_CONCERN_TABLE);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Toast.makeText(context, "更新数据库成功", Toast.LENGTH_SHORT).show();
    }

}
