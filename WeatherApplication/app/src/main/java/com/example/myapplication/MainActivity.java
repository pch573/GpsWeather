package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.simple.ItemList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.location.GpsStatus.GPS_EVENT_STARTED;
import static android.location.GpsStatus.GPS_EVENT_STOPPED;

public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private MainPagerAdapter pagerAdapter;
    private double px, py;
    private int nx, ny;
    private String addr = "현재 위치";
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //변수 초기화
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //viewPager 초기화
        viewPager = findViewById(R.id.viewPager);
        //pageAdapter 설정
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        //페이지 추가
        addFragment();
    }

    public void addFragment() {
        if (pagerAdapter.getCount() < 1) {
            MainFragment mainFragment = new MainFragment();
            Bundle bundle = new Bundle();
            if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);

            if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, gpsLocationListener);
            }
            px = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
            py = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
            setToGridXY(px, py);

            bundle.putInt("nx", nx);
            bundle.putInt("ny", ny);
            bundle.putString("addr", addr);
            mainFragment.setArguments(bundle);

            pagerAdapter.addItem(mainFragment);
            pagerAdapter.notifyDataSetChanged();
        }
        else {
            Intent intent = new Intent(getApplicationContext(), PositionSelectActivity.class);
            startActivityForResult(intent, 1);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String result = data.getStringExtra("result");
        nx = Integer.parseInt(result.substring(0, 3));
        ny = Integer.parseInt(result.substring(3, 6));
        addr = result.substring(6, result.length());

        MainFragment mainFragment = new MainFragment();
        Bundle bundle = new Bundle();

        bundle.putInt("nx", nx);
        bundle.putInt("ny", ny);
        bundle.putString("addr", addr);
        mainFragment.setArguments(bundle);

        pagerAdapter.addItem(mainFragment);
        pagerAdapter.notifyDataSetChanged();
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

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            px = location.getLatitude();
            py = location.getLongitude();
        }
        public void onStatusChanged(String provider, int status, Bundle extras) { }
        public void onProviderEnabled(String provider) { }
        public void onProviderDisabled(String provider) { }
    };
}
