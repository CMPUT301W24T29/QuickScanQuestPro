package com.example.quickscanquestpro;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.auth.User;
import com.google.type.DateTime;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Event {
    private Integer id;
    private BitMatrix checkinQRCode;
    private Bitmap checkinQRImage;
    private BitMatrix promoQRCode;
    private Bitmap promoQRImage;
    private String title;
    private String description;
    private Date startDate;
    private Date endDate;
    private Time startTime;
    private Time endTime;
    private String location;
    private User organizer;
    private ArrayList<String> announcements;
    private Bitmap eventBanner = null;

    public Event(Integer id) {
        this.id = id;
        generateQR("both", id);
    }

    public void generateQR(String qrType, Integer id) {
        MultiFormatWriter mfWriter = new MultiFormatWriter();

        if (Objects.equals(qrType, "checkin") || Objects.equals(qrType, "both")) {
            try {
                checkinQRCode = mfWriter.encode("c" + id.toString(), BarcodeFormat.QR_CODE, 400, 400);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                checkinQRImage = barcodeEncoder.createBitmap(checkinQRCode);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }

        if (Objects.equals(qrType, "promo") || Objects.equals(qrType, "both")) {
            try {
                promoQRCode = mfWriter.encode("p" + id.toString(), BarcodeFormat.QR_CODE, 400, 400);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                promoQRImage = barcodeEncoder.createBitmap(promoQRCode);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BitMatrix getCheckinQRCode() {
        return checkinQRCode;
    }

    public void setCheckinQRCode(BitMatrix checkinQRCode) {
        this.checkinQRCode = checkinQRCode;
    }

    public Bitmap getCheckinQRImage() {
        return checkinQRImage;
    }

    public void setCheckinQRImage(Bitmap checkinQRImage) {
        this.checkinQRImage = checkinQRImage;
    }

    public BitMatrix getPromoQRCode() {
        return promoQRCode;
    }

    public void setPromoQRCode(BitMatrix promoQRCode) {
        this.promoQRCode = promoQRCode;
    }

    public Bitmap getPromoQRImage() {
        return promoQRImage;
    }

    public void setPromoQRImage(Bitmap promoQRImage) {
        this.promoQRImage = promoQRImage;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public Date getStartDate() {
        return startDate;
    }
    public Date getEndDate() {
        return endDate;
    }
    public Time getStartTime() {
        return startTime;
    }
    public Time getEndTime() {
        return endTime;
    }
    public String getLocation() {
        return location;
    }
    public ArrayList<String> getAnnouncements() {
        return announcements;
    }
    public Bitmap getEventBanner() {
        return eventBanner;
    }
}
