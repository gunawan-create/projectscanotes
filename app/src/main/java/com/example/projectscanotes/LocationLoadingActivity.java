package com.example.projectscanotes;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class LocationLoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_loading);

        new Thread(() -> {

            detectActualLocation(); // proses berat dipindah ke background

            try {
                Thread.sleep(2000); // biar splash kelihatan
            } catch (Exception ignored) {}

            runOnUiThread(() -> {
                startActivity(new Intent(this, WelcomeActivity.class));
                finish();
            });

        }).start();
    }

    private void detectActualLocation() {
        try {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) {
                loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            SharedPreferences pref = getSharedPreferences("UserSetting", MODE_PRIVATE);
            String finalLocation = "Jakarta, Indonesia";

            if (loc != null) {
                double lat = loc.getLatitude();
                double lon = loc.getLongitude();

                if (lat > 30 && lat < 40 && lon > 130 && lon < 150) {
                    finalLocation = "Tokyo, Japan";
                } else if (lat > 50 && lat < 60 && lon > -5 && lon < 5) {
                    finalLocation = "London, UK";
                }

                pref.edit().putString("location", finalLocation).apply();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
