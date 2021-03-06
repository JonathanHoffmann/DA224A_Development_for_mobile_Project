package com.blue_red.bensinpriser;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blue_red.bensinpriser.model.FuelStation;
import com.blue_red.bensinpriser.utils.CommonUtils;
import com.blue_red.bensinpriser.utils.DividerItemDecoration;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecViewActivity extends AppCompatActivity implements FuelStationAdapter.Callback {

    @BindView(R.id.mRecyclerView)
    RecyclerView mRecyclerView;
    FuelStationAdapter mFuelStationAdapter;
    private String jsondata;
    private SeekBar seekBar;
    private TextView distTV;
    private CheckBox checkboxUnlimited;
    private int distCheck = 10;
    private Spinner spinnerFuel;
    private Spinner spinnerSort;
    private String sort = "Pris Asc";
    private String controlsort;
    private String fuel = "Bensin 95";
    private String controlfuel;
    LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recview);

        ButterKnife.bind(this);
        setUpRecView();
        Intent intent = getIntent();
        jsondata = intent.getExtras().getString("string");

        setUpUI();
    }
    private void changeTextViewRange()
    {
        String temp;
        if(distCheck==-1)
        {
            temp="unlimited";
        }
        else
        {
            temp= String.valueOf(distCheck) + "km";
        }
        distTV.setText("Distance: " + temp);
    }

    private void setUpUI() {
        distTV = findViewById(R.id.disttextView);
        changeTextViewRange();
        setUpSpinnerFuel();
        setUpSpinnerSort();
        setUpSeekbar();
        setUpCheckbox();
    }

    private void setUpCheckbox() {
        checkboxUnlimited = findViewById(R.id.checkBoxUnlimited);
        checkboxUnlimited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkboxUnlimited.isChecked()) {
                    distCheck = -1;
                    seekBar.setEnabled(false);
                } else {
                    distCheck = 10;
                    seekBar.setEnabled(true);
                }
                changeTextViewRange();
                setUpRecView();
            }
        });
    }

    private void setUpRecView() {
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        Drawable dividerDrawable = ContextCompat.getDrawable(this, R.drawable.divider_drawable);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(dividerDrawable));
        mFuelStationAdapter = new FuelStationAdapter(fuel, new ArrayList<>());

        prepareContent();
    }

    private void setUpSpinnerFuel() {
        //https://stackoverflow.com/questions/13377361/how-to-create-a-drop-down-list
        spinnerFuel = findViewById(R.id.FuelSpinner);
//create a list of items for the spinner.
        String[] items = new String[]{"Bensin 95", "Bensin 98", "Diesel", "Ethanol 85"};
//create an adapter to describe how the items are displayed, adapters are used in several places in android.
//There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
//set the spinners adapter to the previously created one.
        spinnerFuel.setAdapter(adapter);
        spinnerFuel.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                controlfuel = (String) spinnerFuel.getSelectedItem();
                if (!controlfuel.equals(fuel)) {
                    fuel = controlfuel;
                    setUpRecView();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setUpSpinnerSort() {
        spinnerSort = findViewById(R.id.SortSpinner);
        String[] sortitems = new String[]{"Pris Asc", "Pris Desc", "Distans Asc", "Distans Desc"};
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sortitems);
        spinnerSort.setAdapter(a);
        spinnerSort.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                controlsort = (String) spinnerSort.getSelectedItem();
                if (!controlsort.equals(sort)) {
                    sort = controlsort;
                    setUpRecView();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpSeekbar() {
        seekBar = findViewById(R.id.seekBar);
        //https://abhiandroid.com/ui/seekbar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distCheck = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
                changeTextViewRange();
                setUpRecView();
            }
        });
    }

    private void prepareContent() {
        //Qualifiers for finding information in String
        String qb98 = "type=bensin98, price=";
        String qb95 = "type=bensin95, price=";
        String qe85 = "type=ethanol85, price=";
        String qdiesel = "type=diesel, price=";
        String qlat = "latitude=";
        String qlng = "longitude=";
        String qname = "stationName=";
        String qlogo = "logoURL=";
        String qcompanyURL = "companyURL=";
        String qcompanyName = "companyName=";

        CommonUtils.showLoading(RecViewActivity.this);
        new Handler().postDelayed(() -> {
            //prepare data and show loading
            CommonUtils.hideLoading();
            ArrayList<FuelStation> mFuelStations = new ArrayList<>();
            StringBuilder sb = new StringBuilder(jsondata);
            for (int i = 0; i < sb.length(); i++) {
                if (sb.charAt(i) == '(') {
                    int counter = 1;
                    int j;
                    for (j = i + 1; counter != 0; j++) {
                        if (sb.charAt(j) == '(') {
                            counter++;
                        } else if (sb.charAt(j) == ')') {
                            counter--;
                        }
                    }
                    //variables needed to make a FuelStation Object
                    double lng = findDoubleInString(qlng, ',', sb);
                    double lat = findDoubleInString(qlat, ',', sb);
                    double b98 = findDoubleInString(qb98, ')', sb);
                    double b95 = findDoubleInString(qb95, ')', sb);
                    double diesel = findDoubleInString(qdiesel, ')', sb);
                    double e85 = findDoubleInString(qe85, ')', sb);
                    String name = findStringinString(qname, ')', sb);
                    String url = findStringinString(qlogo, ')', sb);
                    String companyname = findStringinString(qcompanyName, ',', sb);
                    String companyURL = findStringinString(qcompanyURL, ',', sb);

                    sb.delete(0, j);
                    i = 0;
                    float dist = distanceCalc(lat, lng);

                    double pricetemp;
                    if (fuel.equals("Bensin 95")) {
                        pricetemp = b95;
                    } else if (fuel.equals("Bensin 98")) {
                        pricetemp = b98;
                    } else if (fuel.equals("Diesel")) {
                        pricetemp = diesel;
                    } else if (fuel.equals("Ethanol 85")) {
                        pricetemp = e85;
                    } else {
                        pricetemp = -1;
                    }
                    if ((dist <= distCheck || distCheck == -1) && pricetemp >= 0) {
                        mFuelStations.add(new FuelStation(companyname, companyURL, url, name, b95, b98, diesel, e85, lat, lng, dist));
                    }
                }
            }

            mFuelStations = sortStations(mFuelStations);

            mFuelStationAdapter.addItems(mFuelStations);
            mRecyclerView.setAdapter(mFuelStationAdapter);
        }, 2000);
    }

    private String findStringinString(String startSigns, char endSign, StringBuilder sb) {
        //http://www.java2s.com/Tutorials/Java/String/How_to_search_a_StringBuilder_from_start_or_from_the_end.htm
        String tempvalue = "";
        int pos = sb.indexOf(startSigns) + startSigns.length();
        for (int k = pos; sb.charAt(k) != endSign; k++) {
            tempvalue += sb.charAt(k);
        }
        return tempvalue;
    }

    private double findDoubleInString(String startSigns, char endSign, StringBuilder sb) {
        double output = -1;
        String tempvalue = "";
        int pos = sb.indexOf(startSigns) + startSigns.length();
        for (int k = pos; sb.charAt(k) != endSign; k++) {
            tempvalue += sb.charAt(k);
        }
        try {
            output = Double.parseDouble(tempvalue);
        } catch (Exception e) {
            System.err.println(e);
        }
        return output;
    }

    private ArrayList<FuelStation> sortStations(ArrayList<FuelStation> mFuelStations) {
        ArrayList<FuelStation> tempstations = new ArrayList<>();
        if (sort.equals("Pris Asc") || sort.equals("Pris Desc")) {
            int temp = mFuelStations.size();
            for (int i = 0; i < temp; i++) {
                double low = 999999999;
                int pos = 0;
                for (int j = 0; j < mFuelStations.size(); j++) {
                    double pricetemp;
                    if (fuel.equals("Bensin 95")) {
                        pricetemp = mFuelStations.get(j).getmBensin95();
                    } else if (fuel.equals("Bensin 98")) {
                        pricetemp = mFuelStations.get(j).getmBensin98();
                    } else if (fuel.equals("Diesel")) {
                        pricetemp = mFuelStations.get(j).getmDiesel();
                    } else if (fuel.equals("Ethanol 85")) {
                        pricetemp = mFuelStations.get(j).getmEthanol85();
                    } else {
                        pricetemp = -1;
                    }

                    if (pricetemp < low) {
                        low = pricetemp;
                        pos = j;
                    }
                }
                tempstations.add(mFuelStations.get(pos));
                mFuelStations.remove(pos);
            }
            if (sort.equals("Pris Desc")) {
                ArrayList<FuelStation> tempstation2 = new ArrayList<>(tempstations);
                tempstations.removeAll(tempstations);
                for (int i = tempstation2.size() - 1; i >= 0; i--) {
                    System.out.println(i);
                    tempstations.add(tempstation2.get(i));
                }
            }
        } else {
            int temp = mFuelStations.size();
            for (int i = 0; i < temp; i++) {
                double high = 0;
                int pos = 0;
                for (int j = 0; j < mFuelStations.size(); j++) {
                    if (mFuelStations.get(j).getmDistance() > high) {
                        high = mFuelStations.get(j).getmDistance();
                        pos = j;
                    }
                }
                tempstations.add(mFuelStations.get(pos));
                mFuelStations.remove(pos);
            }
            if (sort.equals("Distans Asc")) {
                ArrayList<FuelStation> tempstation2 = new ArrayList<>(tempstations);
                tempstations.removeAll(tempstations);
                for (int i = tempstation2.size() - 1; i >= 0; i--) {
                    System.out.println(i);
                    tempstations.add(tempstation2.get(i));
                }
            }
        }
        return tempstations;
    }

    @Override
    public void onEmptyViewRetryClick() {
        prepareContent();
    }

    private float distanceCalc(double lat1, double lon1) {
        //https://stackoverflow.com/questions/2227292/how-to-get-latitude-and-longitude-of-the-mobile-device-in-android
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Permission
        if (ContextCompat.checkSelfPermission(RecViewActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RecViewActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(RecViewActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(RecViewActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        Location current = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //https://developer.android.com/reference/android/location/Location
        //https://www.programcreek.com/java-api-examples/?class=android.location.Location&method=distanceBetween
        float[] dist = new float[1];
        Location.distanceBetween(current.getLatitude(), current.getLongitude(), lat1, lon1, dist);
        return (dist[0] / 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(RecViewActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}