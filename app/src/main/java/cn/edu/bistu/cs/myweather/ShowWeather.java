package cn.edu.bistu.cs.myweather;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author hp
 */
public class ShowWeather extends AppCompatActivity {
    private final static int DAYS = 7;
    private TextView cityNameView, weatherTypeView, currentTmpView, tmpRangeView, detailView;
    private final List<TextView> othersDayNameView = new ArrayList<>(DAYS);
    private final List<TextView> othersDayWeatherTypeView = new ArrayList<>(DAYS);
    private final List<TextView> othersDayTmpRangeView = new ArrayList<>(DAYS);
    private Button refresh, focus;

    private String cityName;
    private String weatherType;
    private String currentTmp;
    private String tmpRange;
    private String humidity;
    private String quality;
    private String sunrise;
    private String sunset;
    private String aqi;
    private String fx;
    private String fl;
    private String suggestion;
    private String notice;
    private String updateTime;

    private final List<String> othersDayName = new ArrayList<>();
    private final List<String> othersDayWeatherType = new ArrayList<>();
    private final List<String> othersDayTmpRange = new ArrayList<>();

    private SQLiteDatabase db;
    private String cityCode;

    private boolean isConcerned = false;
    private boolean existedData = false;


    @SuppressLint({"Range","Recycle"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_weather);
        init();

        // ???????????????????????????????????????????????????????????????????????????
        Cursor cursor = db.query("concern", new String[]{"city_code"},
                "city_code=?", new String[]{cityCode}, null, null, null);
        if(cursor.getCount() != 0){
            isConcerned = true;
            focus.setText("????????????");
        }
        cursor.close();

        // ??????????????????????????????????????????????????????
        refresh.setOnClickListener(v -> {
            sendRequestWithOkHttp();
            Toast.makeText(this, "???????????????", Toast.LENGTH_SHORT).show();
        });

        // ????????????????????????????????????????????????
        focus.setOnClickListener(v -> {
            if(!isConcerned){
                ContentValues values = new ContentValues();
                values.put("city_code", cityCode);
                values.put("city_name", cityName);
                db.insert("concern", null, values);
                focus.setText("????????????");
                Toast.makeText(this, "???????????????", Toast.LENGTH_SHORT).show();
            }
            else{
                db.delete("concern", "city_code=?", new String[]{cityCode});
                focus.setText("??????");
                Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show();

            }
            isConcerned = !isConcerned;
        });

        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        Cursor weatherCursor = db.query("weather", new String[]{"data"},
                "id=?", new String[]{cityCode}, null, null, null);
        if (weatherCursor.moveToNext()) {
            String data;
            do {
                data = weatherCursor.getString(weatherCursor.getColumnIndex("data"));
            } while (weatherCursor.moveToNext());
            Log.e("Get data from db:", data);
            existedData = true;
            showData(data);
        }
        else{
            sendRequestWithOkHttp();
        }
    }


    /**
     * ???????????????
     */
    private void init() {
        cityNameView = findViewById(R.id.city_name);
        weatherTypeView = findViewById(R.id.weather_type);
        currentTmpView = findViewById(R.id.current_tmp);
        tmpRangeView = findViewById(R.id.tmp_range);
        detailView = findViewById(R.id.detail);
        refresh = findViewById(R.id.refresh);
        focus = findViewById(R.id.focus);
        for(int i = 0; i < DAYS; i++){
            int tempNameId = getResources().getIdentifier("day_"+i+"_name", "id", getPackageName());
            othersDayNameView.add(findViewById(tempNameId));
            int tempTypeId = getResources().getIdentifier("day_"+i+"_weather_type", "id", getPackageName());
            othersDayWeatherTypeView.add(findViewById(tempTypeId));
            int tempRangeId = getResources().getIdentifier("day_"+i+"_tmp_range", "id", getPackageName());
            othersDayTmpRangeView.add(findViewById(tempRangeId));
        }

        MyDbHelper myDbHelper = new MyDbHelper(this, "weather.db", null, 1);
        db = myDbHelper.getWritableDatabase();

        Intent intent = getIntent();
        cityCode = intent.getStringExtra("searchCityCode");
//        Log.e("CityCode:", cityCode);

    }

    /**
     * ??????????????????????????????????????????????????????json??????????????????
     */
    private void sendRequestWithOkHttp() {
        new Thread(() ->{
            try{
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://t.weather.itboy.net/api/weather/city/" + cityCode).build();
                Response response = client.newCall(request).execute();
                String responseData = Objects.requireNonNull(response.body()).string();
                Log.e("Get data from api:", responseData);
                showData(responseData);

            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * ??????????????????
     * ??????runOnUiThread???UI??????????????????????????????
     * @param responseData ???????????????????????????json???????????????
     */
    private void showData(String responseData) {
        runOnUiThread(() ->{
            readDataFromJson(responseData);
            String detailIfo = "??????:" + humidity + "    ????????????:" + aqi + "    ????????????:" + quality + "\n" +
                    "????????????:" + sunrise + "                 ????????????:" + sunset + "\n" +
                    "??????:" + fl + "                             ??????:" + fx + "\n" +
                    "????????????:" + suggestion + "\n" + "??????:" + notice + "\n" + "????????????:" + updateTime;
            cityNameView.setText(cityName);
            weatherTypeView.setText(weatherType);
            tmpRangeView.setText(tmpRange);
            currentTmpView.setText(currentTmp);
            detailView.setText(detailIfo);

            for(int i = 0; i < DAYS; i++){
                othersDayNameView.get(i).setText(othersDayName.get(i));
                othersDayWeatherTypeView.get(i).setText(othersDayWeatherType.get(i));
                othersDayTmpRangeView.get(i).setText(othersDayTmpRange.get(i));
            }

        });


    }

    /**
     * ????????????????????????json???????????????????????????
     * ???????????????????????????
     * ????????????????????????
     * @param responseData ???????????????????????????json???????????????
     */
    private void readDataFromJson(String responseData) {
        othersDayName.clear();
        othersDayTmpRange.clear();
        othersDayWeatherType.clear();

        // ??????????????????????????????????????????
        if(responseData.length() < 100){
            Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
            finish();
        }

        Data data = JSON.parseObject(responseData, Data.class);
        Data.CityInfoDTO cityInfo = data.getCityInfo();
        cityName = cityInfo.getCity();
        updateTime = cityInfo.getUpdateTime();

        Data.DataDTO dataDTO = data.getData();
        Data.DataDTO.ForecastDTO today = dataDTO.getForecast().get(0);
        weatherType = today.getType();
        tmpRange = today.getLow().substring(2)+"~"+today.getHigh().substring(2);
        currentTmp = dataDTO.getWendu()+"???";
        humidity = dataDTO.getShidu();
        quality = dataDTO.getQuality();
        suggestion = dataDTO.getGanmao();

        aqi = String.valueOf(today.getAqi());
        sunrise = today.getSunrise();
        sunset = today.getSunset();
        fl = today.getFl();
        fx = today.getFx();
        notice = today.getNotice();

        Data.DataDTO.YesterdayDTO yesterday = dataDTO.getYesterday();
        List<Data.DataDTO.ForecastDTO> forecast = dataDTO.getForecast();

        othersDayName.add("??????");
        othersDayWeatherType.add(yesterday.getType());
        othersDayTmpRange.add(yesterday.getHigh().substring(2)+"~"+yesterday.getLow().substring(2));
        // ???????????????????????????
        for(int i = 1; i < DAYS; i++){
            othersDayName.add(forecast.get(i-1).getWeek());
            othersDayWeatherType.add(forecast.get(i-1).getType());
            othersDayTmpRange.add(forecast.get(i-1).getHigh().substring(2)+"~"+forecast.get(i-1).getLow().substring(2));
        }
        othersDayName.set(1, "??????");

        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????
        if(existedData){
            db.delete("weather", "id=?", new String[]{cityCode});
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", cityCode);
        contentValues.put("data", responseData);
        db.insert("weather", null, contentValues);
        existedData = true;

    }

}