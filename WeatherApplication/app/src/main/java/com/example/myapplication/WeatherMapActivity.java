package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;

public class WeatherMapActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback{
    private GoogleMap mMap;
    double t1h=-1;
    int nx=0, ny=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // 맵이 실행되면 onMapReady 실행
        mapFragment.getMapAsync(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }
    }

    private void setToGridXY(double px, double py) {
        double RE = 6371.00877;
        double GRID = 5.0;
        double SLAT1 = 30.0;
        double SLAT2 = 60.0;
        double OLON = 126.0;
        double OLAT = 38.0;
        double XO = 43;
        double YO = 136;
        double DEGRAD = Math.PI / 180.0;
        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        double ra = Math.tan(Math.PI * 0.25 + px * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = py * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        nx = (int)(Math.floor(ra * Math.sin(theta) + XO + 0.5));
        ny = (int)(Math.floor(ro - ra * Math.cos(theta) + YO + 0.5));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        // 구글에서 등록한 api와 엮어주기
        // 시작위치를 서울 시청으로 변경
        LatLng cityHall = new LatLng(37.566622, 126.978159); // 서울시청 위도와 경도
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(cityHall));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        // 시작시 마커 생성하기
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(cityHall);
        markerOptions.title("시청");
        markerOptions.snippet("서울 시청");
        // 생성된 마커 옵션을 지도에 표시
        googleMap.addMarker(markerOptions);
        //맵 로드 된 이후
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Toast.makeText(WeatherMapActivity.this, "Map로딩성공", Toast.LENGTH_SHORT).show();
            }
        });
        //카메라 이동 시작
        googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                Log.d("set>>","start");
            }
        });
        // 카메라 이동 중
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                Log.d("set>>","move");
            }
        });
        //맵 터치 이벤트 구현//
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions mOptions=new MarkerOptions();
                // 기존 마커 정리
                googleMap.clear();
                //마커 타이틀
                mOptions.title("기온");
                Double latitude=latLng.latitude; //위도
                Double longitude=latLng.longitude;//경도
                //LatLng:위도 경도 쌍을 나타냄
                mOptions.position(new LatLng(latitude, longitude));

                setToGridXY(latitude, longitude);
                Thread thdGetWeather = new Thread() { public void run() { getWeather(nx, ny); } };
                try { thdGetWeather.start(); thdGetWeather.join(); } catch (Exception e) { e.printStackTrace(); }

                mOptions.snippet(t1h + "℃");

                //마커(핀) 추가
                googleMap.addMarker(mOptions);
                googleMap.addMarker(mOptions).showInfoWindow();

                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Info window clicked",
                Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        String[] permissions = {
                // Manifest는 android를 import
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (String permission : permissions) {
            permissionCheck = this.checkSelfPermission(permission);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            this.requestPermissions(permissions, 1);
        }


    }

    private void getWeather(int nx, int ny) {
        String serviceKey = "TiVsCqid7B%2FP6q2kOLq0EPXv0sk1s%2FF1m411c9i0ZqhnUDFBTF1d6sgkV93yGKBxKzL0uQ3f8vgB4SW56o1EqQ%3D%3D";
        String urlStr = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastGrib";
        String baseDate, baseTime;
        Calendar calendar = Calendar.getInstance();

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
                if (temp.get("category").equals("T1H")) {
                    t1h = Double.parseDouble(temp.get("obsrValue").toString());
                    continue;
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        ;
    }
}
