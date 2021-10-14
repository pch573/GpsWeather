package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainFragment extends Fragment {
    private ImageView ivWeather;
    private ImageView ivMap;
    private ImageView ivSelectPosition;
    private TextView tvAddr;
    private TextView tvWeather;
    private TextView tvTemp;
    private RecyclerView wcItemRecyclerView;
    private WeatherCastViewAdapter adapter;

    private ArrayList<WeatherCastItem> wcItemList;

    private int nx = 0, ny = 0;
    private String addr = "현재 위치";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment, container, false);

        if (getArguments() != null) {
            Bundle args = getArguments();

            nx = args.getInt("nx");
            ny = args.getInt("ny");
            addr = args.getString("addr");

            ivWeather = (ImageView) view.findViewById(R.id.ivWeather);
            ivMap = (ImageView) view.findViewById(R.id.ivMap);
            ivSelectPosition = (ImageView) view.findViewById(R.id.ivSelectPosition);
            tvAddr = (TextView) view.findViewById(R.id.tvAddr);
            tvWeather = (TextView) view.findViewById(R.id.tvWeather);
            tvTemp = (TextView) view.findViewById(R.id.tvTemp);
            wcItemRecyclerView = (RecyclerView) view.findViewById(R.id.wcItemRecyclerView);
            wcItemList = new ArrayList<>();

            wcItemRecyclerView.setLayoutManager(new LinearLayoutManager(container.getContext(), LinearLayoutManager.HORIZONTAL, false));
            adapter = new WeatherCastViewAdapter(container.getContext(), wcItemList);
            wcItemRecyclerView.setAdapter(adapter);

            Thread thdGetWeather = new Thread() {
                public void run() {
                    getWeather(nx, ny);
                    getWeatherCast(nx, ny);
                }
            };
            try {
                thdGetWeather.start();
                thdGetWeather.join();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ivMap.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), WeatherMapActivity.class);
                    startActivity(intent);
                }
            });

            ivSelectPosition.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ((MainActivity) getActivity()).addFragment();
                }
            });
        }

        return view;
    }

    private void getWeather(int nx, int ny) {
        String serviceKey = "TiVsCqid7B%2FP6q2kOLq0EPXv0sk1s%2FF1m411c9i0ZqhnUDFBTF1d6sgkV93yGKBxKzL0uQ3f8vgB4SW56o1EqQ%3D%3D";
        String urlStr = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastGrib";
        String baseDate, baseTime;
        Calendar calendar = Calendar.getInstance();
        int pty = -999;
        double t1h = -999;

        if (Calendar.HOUR_OF_DAY < 0) {
            calendar.add(Calendar.DATE, -1);
            baseTime = "2300";
        } else {
            if (calendar.get(Calendar.MINUTE) < 30)
                baseTime = String.format("%04d", (calendar.get(Calendar.HOUR_OF_DAY) - 1) * 100);
            else
                baseTime = String.format("%04d", (calendar.get(Calendar.HOUR_OF_DAY)) * 100);
        }

        baseDate = calendar.get(Calendar.YEAR) + String.format("%02d", (calendar.get(Calendar.MONTH)) + 1) + String.format("%02d", (calendar.get(Calendar.DAY_OF_MONTH)));
        urlStr += "?ServiceKey=" + serviceKey + "&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + nx + "&ny=" + ny + "&numOfRows=10&pageNo=1&_type=json";

        try {
            URL url = new URL(urlStr);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null)
                result.append(line);

            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(result.toString());
            JSONObject parse_response = (JSONObject) obj.get("response");
            JSONObject parse_body = (JSONObject) parse_response.get("body");
            JSONObject parse_items = (JSONObject) parse_body.get("items");
            JSONArray parse_item = (JSONArray) parse_items.get("item");
            JSONObject temp;

            for (int i = 0; i < parse_item.size(); i++) {
                temp = (JSONObject) parse_item.get(i);
                if (temp.get("category").equals("PTY"))
                    pty = Integer.parseInt(temp.get("obsrValue").toString());
                else if (temp.get("category").equals("T1H"))
                    t1h = Double.parseDouble(temp.get("obsrValue").toString());
            }

            tvAddr.setText(addr);
            switch (pty) {
                case 0:
                    ivWeather.setImageResource(R.drawable.sun);
                    tvWeather.setText("맑음");
                    break;
                case 1:
                    ivWeather.setImageResource(R.drawable.rain);
                    tvWeather.setText("비");
                    break;
                case 2:
                    ivWeather.setImageResource(R.drawable.rainsnow);
                    tvWeather.setText("비/눈");
                    break;
                case 3:
                    ivWeather.setImageResource(R.drawable.snow);
                    tvWeather.setText("눈");
                    break;
                case 4:
                    ivWeather.setImageResource(R.drawable.shower);
                    tvWeather.setText("소나기");
                    break;
            }

            if (t1h <= -900 || t1h >= 900)
                tvTemp.setText("현재 기온 측정불가");
            else
                tvTemp.setText(t1h + "℃");

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void getWeatherCast(int nx, int ny) {
        String serviceKey = "TiVsCqid7B%2FP6q2kOLq0EPXv0sk1s%2FF1m411c9i0ZqhnUDFBTF1d6sgkV93yGKBxKzL0uQ3f8vgB4SW56o1EqQ%3D%3D";
        String urlStr = "http://www.kma.go.kr/wid/queryDFS.jsp?gridx=" + nx + "&gridy=" + ny;
        int pty = -1;
        int tmpDay = -1, tmpTime = -1;
        Drawable tmpIcon = null;
        double tmpTemp = -1.0;

        DocumentBuilderFactory dbFactoty = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        Document doc = null;

        try {
            dBuilder = dbFactoty.newDocumentBuilder();
            doc = dBuilder.parse(urlStr);
            NodeList nodeList = doc.getElementsByTagName("data");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Element element = (Element) node;

                pty = Integer.parseInt(((Element) node).getElementsByTagName("pty").item(0).getChildNodes().item(0).getNodeValue());
                tmpDay = Integer.parseInt(((Element) node).getElementsByTagName("day").item(0).getChildNodes().item(0).getNodeValue());
                tmpTime = Integer.parseInt(((Element) node).getElementsByTagName("hour").item(0).getChildNodes().item(0).getNodeValue());
                tmpTemp = Double.parseDouble(((Element) node).getElementsByTagName("temp").item(0).getChildNodes().item(0).getNodeValue());

                wcItemList.add(new WeatherCastItem(pty, tmpDay, tmpTime, tmpTemp));
            }
        } catch (Exception e) {
            System.out.println(e);
        };
    }

}