package com.example.quickscanquestpro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import android.content.ClipData;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private QRCodeScanner qrCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_qr_scanner);

        PreviewView previewView = findViewById(R.id.cameraFeed);
        qrCodeScanner = new QRCodeScanner(this, previewView);
        qrCodeScanner.startCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qrCodeScanner.shutdown();
    }
}