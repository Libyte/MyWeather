package cn.edu.bistu.cs.myweather;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hp
 */
public class MainActivity extends AppCompatActivity {
    private final List<Integer> idList = new ArrayList<>();
    private final List<String> cityNameList = new ArrayList<>();
    private final List<String> cityCodeList = new ArrayList<>();
    private Button search, back, myConcern;
    private EditText cityCodeInput;
    private ListView provinceList;
    private String jsonData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("My weather");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setAction();

        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(
                MainActivity.this, android.R.layout.simple_list_item_1, cityNameList);
        provinceList.setAdapter(stringArrayAdapter);

        provinceList.setOnItemClickListener((parent, view, position, id) -> {
            int cityId = idList.get(position);
            String cityCode = cityCodeList.get(position);
            // 空字符串表示当前城市等级仍可以向下划分，根据此城市/省ID找下一级辖区
            if("".equals(cityCode)){
                getCityInfo(jsonData, cityId);
                provinceList.setAdapter(stringArrayAdapter);
                // 重新选择按钮可见性与动作
                back.setVisibility(View.VISIBLE);
                back.setOnClickListener(v -> {
                    getCityInfo(jsonData, 0);
                    provinceList.setAdapter(stringArrayAdapter);
                    back.setVisibility(View.INVISIBLE);

                });
            }
            else{
                Intent intent = new Intent(MainActivity.this, ShowWeather.class);
                intent.putExtra("searchCityCode", cityCode);
                startActivity(intent);
            }
        });
    }


    /**
     * 初始化控件
     */
    private void init() {
        search = findViewById(R.id.search);
        back = findViewById(R.id.back);
        back.setVisibility(View.INVISIBLE);
        myConcern = findViewById(R.id.concern);
        cityCodeInput = findViewById(R.id.search_input);
        provinceList = findViewById(R.id.province_list);
        jsonData = getJson(this);
        getCityInfo(jsonData, 0);
    }

    /**
     * 设置查询与关注的点击事件
     */
    private void setAction() {
        search.setOnClickListener(v -> {
            String cityCode = cityCodeInput.getText().toString();
            if (cityCode.length() != 9) {
                Toast.makeText(MainActivity.this, "输入城市代码应为9位", Toast.LENGTH_SHORT).show();
            }
            else if(!jsonData.contains(cityCode)){
                Toast.makeText(this, "输入城市编码不存在，请重新输入！", Toast.LENGTH_SHORT).show();
            }
            else {
                Intent intent = new Intent(MainActivity.this, ShowWeather.class);
                intent.putExtra("searchCityCode", cityCode);
                startActivity(intent);
            }
        });

        myConcern.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ShowConcernCity.class);
            startActivity(intent);
        });

    }

    /**
     * 针对存放城市编号的city.json文件进行读取成字符串
     * @param context 当前界面
     * @return city.json文件内容对应的字符串
     */
    private static String getJson(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = context.getResources().openRawResource(R.raw.city);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        try{

            String line;
            while((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line);
            }
            inputStream.close();
            bufferedReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 根据json文件格式的字符串进行解析
     * @param jsonData city.json文件内容的字符串
     * @param level 对应的等级，国家为0，省对应自己的ID，根据此找下一级的辖区
     */
    private void getCityInfo(String jsonData, int level){
        idList.clear();
        cityNameList.clear();
        cityCodeList.clear();
        try{
            JSONArray jsonArray = new JSONArray(jsonData);
            for(int i = 0; i< jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int id = jsonObject.getInt("id");
                int pid = jsonObject.getInt("pid");
                String cityCode = jsonObject.getString("city_code");
                String cityName = jsonObject.getString("city_name");
                if(pid == level){
                    idList.add(id);
                    cityCodeList.add(cityCode);
                    cityNameList.add(cityName);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder backAction = new AlertDialog.Builder(MainActivity.this);
        backAction.setTitle("确定退出My Weather？");
        backAction.setPositiveButton("确定", (dialog, which) -> super.onBackPressed());
        backAction.setNegativeButton("取消", null);
        backAction.create().show();
    }
}

