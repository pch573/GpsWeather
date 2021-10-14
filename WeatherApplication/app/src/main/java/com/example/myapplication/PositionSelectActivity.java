package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PositionSelectActivity extends AppCompatActivity {
    private EditText editText;
    private ListView listView;
    private ArrayList<Position> posArr = new ArrayList<>();
    private ArrayList<String> addrList = new ArrayList<>();
    private ArrayList<String> searchedAddrList = new ArrayList<>();
    private Map<String, Integer> map = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_select);
        editText = (EditText) findViewById(R.id.editText);
        listView = (ListView) findViewById(R.id.listView);
        ArrayList<String> list = new ArrayList<>();
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, searchedAddrList);;

        Thread thread = new Thread() { public void run() { getPosArr(); }};
        try { thread.start(); thread.join(); } catch(Exception e) { finish(); }

        for (int i=0; i<posArr.size(); i++) {
            map.put(posArr.get(i).addr.toString(), i);
            addrList.add(posArr.get(i).addr.toString());
        }

        listView.setAdapter(adapter);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = editText.getText().toString();
                searchedAddrList.clear();
                for(int i = 0; i < addrList.size(); i++)
                    if (addrList.get(i).contains(str))
                        searchedAddrList.add(addrList.get(i));
                adapter.notifyDataSetChanged();
            }
            @Override
            public void afterTextChanged(Editable arg0) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int index = map.get(searchedAddrList.get(position));
                String result = String.format("%03d", posArr.get(index).nx) + String.format("%03d", posArr.get(index).ny) + searchedAddrList.get(position);

                Intent intent = new Intent();
                intent.putExtra("result", result);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    private void getPosArr() {
        String urlStr = "http://", dest = "/weatherapp/DBConnection";
        String ip = "192.168.0.3", port = "8080";

        urlStr += ip + ':' + port + dest;
        System.out.println(urlStr);
        try {
            URL url = new URL(urlStr);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder result = new StringBuilder();
            String line;

            while((line = bufferedReader.readLine()) != null)
                result.append(line);

            JSONParser parser = new JSONParser();
            JSONArray arr = (JSONArray)parser.parse(result.toString());

            JSONObject jsonTemp;
            for(int i = 0 ; i < arr.size(); i++) {
                Position posTemp = new Position();
                jsonTemp = (JSONObject)arr.get(i);
                posTemp.addr = jsonTemp.get("addr").toString();
                posTemp.nx = Integer.parseInt(jsonTemp.get("nx").toString());
                posTemp.ny = Integer.parseInt(jsonTemp.get("ny").toString());
                posArr.add(posTemp);
            }
        } catch (Exception e) { System.exit(0); };

    }
}

