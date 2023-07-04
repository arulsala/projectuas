package com.acer.sensorku;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private StringBuilder trackData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView titleTextView = findViewById(R.id.titleTextView);
        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);

        startButton.setOnClickListener(view -> {
            startTracking();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        });

        stopButton.setOnClickListener(view -> {
            stopTracking();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        });

        // Periksa izin lokasi
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Izin belum diberikan, tampilkan dialog permintaan izin
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
        }
    }

    private void startTracking() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Membuat objek LocationListener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Mendapatkan informasi lokasi baru
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Membuat data titik lokasi
                String locationData = String.format(Locale.getDefault(), "Latitude: %f, Longitude: %f", latitude, longitude);

                // Menyimpan data lokasi ke dalam StringBuilder
                trackData.append(locationData).append("\n");

                // Menampilkan data lokasi di logcat
                Log.d("Tracking", locationData);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Tidak perlu diimplementasikan
            }

            @Override
            public void onProviderEnabled(String provider) {
                // Tidak perlu diimplementasikan
            }

            @Override
            public void onProviderDisabled(String provider) {
                // Tidak perlu diimplementasikan
            }
        };

        // Meminta pembaruan lokasi setiap 1 meter dan 1 detik
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

        // Membuat StringBuilder untuk menyimpan data tracking
        trackData = new StringBuilder();
    }

    private void stopTracking() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
            saveTrackData();
        }
    }

    private void saveTrackData() {
        if (isExternalStorageWritable()) {
            // Membuat nama file dengan format tanggal dan waktu
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "TrackData_" + timeStamp + ".txt";

            // Memeriksa ketersediaan penyimpanan eksternal
            File trackFile = new File(getExternalFilesDir(null), fileName);

            try {
                // Membuat file dan menyimpan data tracking
                FileOutputStream fos = new FileOutputStream(trackFile);
                fos.write(trackData.toString().getBytes());
                fos.close();
                Toast.makeText(this, "Data tracking disimpan di " + trackFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Tidak dapat menyimpan data tracking.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan
                // Lakukan tindakan yang sesuai, seperti memulai tracking lokasi
                startTracking();
            } else {
                // Izin ditolak
                // Tampilkan pesan atau ambil tindakan yang sesuai
                Toast.makeText(this, "Izin akses lokasi ditolak.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
