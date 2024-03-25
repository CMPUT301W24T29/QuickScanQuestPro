package com.example.quickscanquestpro;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.location.Location;
import android.media.Image;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.type.DateTime;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * Event is a representation of an event from the database, created by the {@link EventCreationFragment} and then stored in the Database by the DatabaseService
 * It contains a unique event ID, an organizer ID, and attributes for the all the data associated with an event. Contains getters and setters for those attributes.
 */
public class Event {
    private String id;
    private BitMatrix checkinQRCode;
    private Bitmap checkinQRImage;
    private BitMatrix promoQRCode;
    private Bitmap promoQRImage;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String organizerId;

    boolean isCheckedIn = false;

    private String eventBannerUrl;
    private String eventBannerPath;
    private static ArrayList<String> announcements = new ArrayList<String>();

    private String customCheckin;
    private String customPromo;

    // josh
    private ArrayList<User> attendees = new ArrayList<User>();
    private Bitmap eventBanner = null;
    private ArrayList<CheckIn> checkIns;


    /**
     * Constructor for the event that just takes an id, used when constructing the object during event creation.
     * @param id the id (unique) of the event, from database preferably
     */
    public Event(String id) {
        this.id = id;
        generateQR("both", id);
    }

    /**
     * Constructor for event that requires most attributes. Should only be used when testing an event, as it does not set all attributes.
     * @param id id of event
     * @param title title of event
     * @param description description of event
     * @param startDate day event starts
     * @param endDate day event ends
     * @param startTime time event starts
     * @param endTime time event ends
     * @param location address of event
     * @param organizer id of the organizer
     * @param announcements ArrayList of announcement strings
     */
    public Event(String id, String title, String description, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, String location, String organizer, ArrayList<String> announcements, String eventBannerUrl, String eventBannerPath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.organizerId = organizer;
        this.announcements = announcements;
        this.eventBannerUrl = eventBannerUrl;
        this.eventBannerPath = eventBannerPath;
        generateQR("both", id);
    }

    /**
     * Generates a QR that depends on the type passed, or creates both if passed "both"
     * @param qrType a string of what type qr to generate and store in the event. valid values are "both", "checkin", or "promo"
     * @param id the id of the event to use during generation
     */
    public void generateQR(String qrType, String id) {
        MultiFormatWriter mfWriter = new MultiFormatWriter();

        if (Objects.equals(qrType, "checkin") || Objects.equals(qrType, "both")) {
            try {
                if (customCheckin != null) {
                    checkinQRCode = mfWriter.encode(customCheckin, BarcodeFormat.QR_CODE, 400, 400);
                } else {
                    checkinQRCode = mfWriter.encode("c" + id, BarcodeFormat.QR_CODE, 400, 400);
                }
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                checkinQRImage = barcodeEncoder.createBitmap(checkinQRCode);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }

        if (Objects.equals(qrType, "promo") || Objects.equals(qrType, "both")) {
            try {
                if (customPromo != null) {
                    promoQRCode = mfWriter.encode(customPromo, BarcodeFormat.QR_CODE, 400, 400);
                } else {
                    promoQRCode = mfWriter.encode("p" + id, BarcodeFormat.QR_CODE, 400, 400);
                }
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                promoQRImage = barcodeEncoder.createBitmap(promoQRCode);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkIn()
    {
        this.isCheckedIn = true;
    }

    public boolean isCheckedIn()
    {
        return isCheckedIn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public void setEventBanner(Bitmap eventBanner) {
        this.eventBanner = eventBanner;
    }

    public void addAttendee(User user) {
        attendees.add(user);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public String getOrganizerId() {
        return organizerId;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public LocalTime getStartTime() {
        return startTime;
    }
    public LocalTime getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public ArrayList<String> getAnnouncements() {
        return announcements;
    }

    public ArrayList<User> getAttendees() {
        return attendees;
    }

    public Bitmap getEventBanner() {
        return eventBanner;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getEventBannerUrl(){
        return eventBannerUrl;
    }

    public String getEventBannerPath()
    {
        return eventBannerPath;
    }

    public void setEventBannerUrl(String eventBannerUrl)
    {
        this.eventBannerUrl = eventBannerUrl;
    }

    public void setEventBannerPath(String eventBannerPath){
        this.eventBannerPath = eventBannerPath;
    }

    public void setAnnouncements(ArrayList<String> announcements) {
        this.announcements = announcements;
    }

    public String getCustomCheckin() {
        return customCheckin;
    }

    public void setCustomCheckin(String customCheckin) {
        if (customCheckin != null) {
            this.customCheckin = customCheckin;
            generateQR("checkin", customCheckin);
        }
    }

    public String getCustomPromo() {
        return customPromo;
    }

    public void setCustomPromo(String customPromo) {
        if (customPromo != null) {
            this.customPromo = customPromo;
            generateQR("promo", customPromo);
        }
    }

    /**
     * Used to create and return a test event as a class function / static method.
     * @param eventID the ID to use during event creation
     * @return returns an Event with pre-filled attributes
     */
    public static Event createTestEvent(String eventID) {
        String testTitle = "Old Strathcona Summer Rib Fest";
        String testDescription = "Come join us for the 2021 Old Strathcona Summer Rib Fest! Enjoy a variety of delicious ribs, live music, and more!";

        LocalTime testStartTime = LocalTime.of(11, 0, 0);
        LocalTime testEndTime = LocalTime.of(21, 0, 0);
        LocalDate testStartDate = LocalDate.of(2021, 7, 16);
        LocalDate testEndDate = LocalDate.of(2021, 7, 18);
        String testLocation = "Edmonton, AB - 10310 83 Ave NW, Edmonton, AB T6E 2C6";
        announcements.add("• The Old Strathcona Summer Rib Fest is now open! Come join us for a day of fun and delicious ribs!");
        announcements.add("• We are excited to announce that we will be having a live band at the event!");
        announcements.add("• We are running out of ribs! Come get them while they last!");
        announcements.add("• Restocking ribs! We will be back in 30 minutes!");
        announcements.add("• Buy 1 rack of ribs, get the second rack 50% off!");
        announcements.add("• We are now closed for the day. Thank you to everyone who came out to the event!");

        Event event = new Event(eventID, testTitle, testDescription, testStartDate, testEndDate, testStartTime, testEndTime, testLocation, "alastair", announcements, null, null);

        Bitmap bmp = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.rgb(255,0,0));
        event.setEventBanner(bmp);

        return event;
    }

    /**
     * creates an onclick listener to upload an image to the event and set its result to an imageview for displaying the uploaded image.
     * @param fragment the fragment (usually 'this') that the button is in
     * @param imageView the imageview to display the uploaded image in
     * @return View.OnClickListener to use with a button
     */
    public View.OnClickListener uploadPhoto(Fragment fragment, ImageView imageView) {
        MainActivity mainActivity = (MainActivity) fragment.getActivity();

        // onclick listener for the button to upload a picture
        ActivityResultLauncher<String> mGetContent = fragment.registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                Bitmap newBitmap = null;
                ContentResolver contentResolver = mainActivity.getContentResolver();
                try {
                    if(Build.VERSION.SDK_INT < 28) {
                        newBitmap = MediaStore.Images.Media.getBitmap(contentResolver, result);
                    } else {
                        ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, result);
                        newBitmap = ImageDecoder.decodeBitmap(source);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.setEventBanner(newBitmap);
                imageView.setImageBitmap(newBitmap);
                imageView.setVisibility(View.VISIBLE);
            }
        });

        return new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        };
    }

    public void setCheckIns(ArrayList<CheckIn> checkIns) {
        this.checkIns = checkIns;
    }
    public ArrayList<CheckIn> getCheckIns() {
        return checkIns;
    }

    /**
     * Goes through the event's checkIns, and counts up how many times each user id has checked
     * into the event. Then associates the user's name with how many times they've checked in. If there
     * is currently no user associated with that id, then shows Unknown User.
     * @param users a list of all users from the database, to get names from by comparing ids
     * @return a list of lists, with the first element of each inner list being the name of the
     * user who checked in, and the second element is how many times they checked in to the event
     */
    public ArrayList<ArrayList<Object>> countAttendees(List<User> users) {
        ArrayList<CheckIn> checkIns = this.getCheckIns();

        if (checkIns == null) {
            return null;
        }

        ArrayList<ArrayList<Object>> outputList = new ArrayList<>();

        for (CheckIn checkIn : checkIns) {
            boolean found = false;
            for (ArrayList<Object> outputs : outputList) {
                if (outputs.get(0).equals(checkIn.getUserId())) {
                    outputs.set(1, (int) outputs.get(1) + 1);
                    found = true;
                    break; // Break the loop once the user ID is found
                }
            }
            if (!found) {
                ArrayList<Object> innerList = new ArrayList<>();
                innerList.add(checkIn.getUserId());
                innerList.add(1);
                outputList.add(innerList);
            }
        }
        for (ArrayList<Object> output : outputList) {
            boolean userFound = false; // Flag to track if the user is found
            for (User user : users) {
                if (output.get(0).equals(user.getUserId())) {
                    Log.d("Event", "User found: " + user.getName());
                    output.set(0, user.getName());
                    userFound = true; // Set the flag to true if the user is found
                    break; // Break the loop once the user is found
                }
            }
            if (!userFound) {
                output.set(0, "Unknown User");
            }
        }
        return outputList;
    }
}
