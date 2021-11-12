package cn.edu.bistu.cs.myweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hp
 */
public class ShowConcernCity extends AppCompatActivity {
    private ListView concernList;
    private SQLiteDatabase db;
    private final List<String> cityNameList = new ArrayList<>();
    private final List<String> cityCodeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("我的关注");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_concern_city);
        // ListView点击事件处理
        concernList = findViewById(R.id.concern_list);
        concernList.setOnItemClickListener((parent, view, position, id) -> {
            String cityCode = cityCodeList.get(position);
            Intent intent = new Intent(ShowConcernCity.this, ShowWeather.class);
            intent.putExtra("searchCityCode", cityCode);
            startActivity(intent);
        });

        concernList.setOnItemLongClickListener((parent, view, position, id) -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(ShowConcernCity.this);
            dialog.setTitle("确定取消关注"+cityNameList.get(position)+"?");
            dialog.setPositiveButton("确定", (dialog1, which) -> {
                db.execSQL("delete from concern where city_code="+cityCodeList.get(position));
                refresh();
            });
            dialog.setNegativeButton("取消", null);
            dialog.create().show();
            return true;
        });
    }

    /**
     * 从数据库关注表中向List中读入城市ID与城市名
     * 用于界面的显示或跳转附加内容
     */
    @SuppressLint({"Range", "Recycle"})
    private void getConcernCity(){
        MyDbHelper myDbHelper = new MyDbHelper(this, "weather.db", null, 1);
        db = myDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from concern", null);
        while(cursor.moveToNext()){
            cityCodeList.add(cursor.getString(cursor.getColumnIndex("city_code")));
            cityNameList.add(cursor.getString(cursor.getColumnIndex("city_name")));
        }

        cursor.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 刷新显示
        refresh();

    }

    /**
     * 刷新
     * 即进数据从数据库中重新读取
     * 并为行迭代器的重新赋值重新加载
     */
    private void refresh() {
        cityCodeList.clear();
        cityNameList.clear();
        getConcernCity();
        ArrayAdapter<String> stringAdapter = new ArrayAdapter<>(
                ShowConcernCity.this, android.R.layout.simple_list_item_1, cityNameList);
        concernList.setAdapter(stringAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,0,"取消所有关注城市");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == 0){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("确定取消所有关注？");
            dialog.setPositiveButton("确定", (dialog1, which) -> {
                db.execSQL("drop table if exists concern");
                db.execSQL("create table concern(" +
                        "city_code String primary key not null, " +
                        "city_name String not null)");
                refresh();
                finish();
            });
            dialog.setNegativeButton("取消", null);
            dialog.create().show();
        }
        return super.onOptionsItemSelected(item);
    }
}