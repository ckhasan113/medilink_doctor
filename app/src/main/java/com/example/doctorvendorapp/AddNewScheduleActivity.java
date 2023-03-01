package com.example.doctorvendorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.doctorvendorapp.pojo.addschedule.AddChamber;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddNewScheduleActivity extends AppCompatActivity implements View.OnClickListener{

    private SimpleDateFormat selectedDateOfMonth = new SimpleDateFormat("dd", Locale.getDefault());

    private SimpleDateFormat dateFormatForDateOfMonth = new SimpleDateFormat("dd", Locale.getDefault());
    private SimpleDateFormat dateFormatForMonthOfMonth = new SimpleDateFormat("MM", Locale.getDefault());
    private SimpleDateFormat dateFormatForYearOfMonth = new SimpleDateFormat("yyyy", Locale.getDefault());

    private FirebaseUser user;

    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference chamberRef;

    private EditText chamberNameEdt, patientCountEdt, cityEdt, areaEdt, blockEdt, roadEdt, houseEdt, floorEdt, roomEdt, zipEdt, countryEdt, feeEdt;
    private TextView startTimeTV, endTimeTV, datesTV, statusTV;

    private Button addNewBtn, updateBtn;

    private String chamberName, sTime, eTime, patientCount, city, area, block, road, house, floor, room, zip, country, fee, doctorID, id, status = "pending";

    private List<String> days = new ArrayList<String>();
    private List<String> dates = new ArrayList<String>();

    private int mHour, mMinute;

    private LoadingDialog dialog;

    private CompactCalendarView calendarView;

    private boolean doubleDateCheaker = false;

    private AddChamber chamber;

    private String monthString;
    private String yearString;
    private String dateString;
    private String fullDateString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_schedule);

        user = FirebaseAuth.getInstance().getCurrentUser();

        chamber = (AddChamber) getIntent().getSerializableExtra("ChamberDetails");

        doctorID = user.getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("Doctor").child(user.getUid());
        chamberRef = userRef.child("ChamberList").child("All");

        dialog = new LoadingDialog(this, "Loading....");

        datesTV = findViewById(R.id.dates);
        chamberNameEdt = findViewById(R.id.ch_hospital);
        patientCountEdt = findViewById(R.id.patient_count);
        cityEdt = findViewById(R.id.ch_city);
        areaEdt = findViewById(R.id.ch_area);
        blockEdt = findViewById(R.id.ch_block);
        roadEdt = findViewById(R.id.ch_road);
        houseEdt = findViewById(R.id.ch_house);
        floorEdt = findViewById(R.id.ch_floor);
        roomEdt = findViewById(R.id.ch_room);
        zipEdt = findViewById(R.id.ch_zip);
        countryEdt = findViewById(R.id.ch_country_id);
        feeEdt = findViewById(R.id.ch_fee);

        startTimeTV = findViewById(R.id.stTime);
        endTimeTV = findViewById(R.id.endTime);

        statusTV = findViewById(R.id.chamberStatusTV);

        calendarView = findViewById(R.id.selectDateCalender_view);

        calendarView.setUseThreeLetterAbbreviation(true);
        calendarView.setFirstDayOfWeek(Calendar.SATURDAY);
        calendarView.shouldScrollMonth(false);

        addNewBtn = findViewById(R.id.add_new_schedule);
        updateBtn = findViewById(R.id.update_schedule);

        startTimeTV.setOnClickListener(this);
        endTimeTV.setOnClickListener(this);

        datesTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarView.setVisibility(View.VISIBLE);
            }
        });

        dateString = dateFormatForDateOfMonth.format(Calendar.getInstance().getTime());
        monthString = dateFormatForMonthOfMonth.format(Calendar.getInstance().getTime());
        yearString = dateFormatForYearOfMonth.format(Calendar.getInstance().getTime());
        fullDateString = dateString+"/"+monthString+"/"+yearString;

        days.clear();
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                String selectedDate = selectedDateOfMonth.format(dateClicked);
                long selectedDateColor = dateClicked.getTime();
                dateString = dateFormatForDateOfMonth.format(dateClicked);
                for (String d: days){

                    if (d.equals(dateString)){
                        long startDate = 0L;
                        try {

                            fullDateString = d+"/"+monthString+"/"+yearString;
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            Date date = sdf.parse(fullDateString);
                            startDate = date.getTime();
                            dates.add(dateString);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Event ev = new Event(Color.GREEN, startDate, "Doctor Available");
                        calendarView.removeEvent(ev);
                    }
                }
                Event ev1 = new Event(Color.parseColor("#4FBD54"), selectedDateColor, "Date selected");
                doubleDateCheaker = false;
                calendarView.setCurrentSelectedDayBackgroundColor(Color.parseColor("#4CAF50"));
                for (String ds: days){
                    if (ds.equals(selectedDate)){
                        Toast.makeText(AddNewScheduleActivity.this, ds+" is removed", Toast.LENGTH_SHORT).show();
                        days.remove(ds);
                        calendarView.removeEvent(ev1);
                        calendarView.setCurrentSelectedDayBackgroundColor(Color.parseColor("#E46F6D"));
                        doubleDateCheaker = true;
                        break;
                    }else {
                        calendarView.setCurrentSelectedDayBackgroundColor(Color.parseColor("#4CAF50"));
                        doubleDateCheaker = false;
                    }
                }
                if (!doubleDateCheaker){
                    calendarView.addEvent(ev1);
                    Toast.makeText(AddNewScheduleActivity.this, selectedDate+" is selected", Toast.LENGTH_SHORT).show();
                    days.add(selectedDate);
                }
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {

            }

        });

        if (chamber == null){
        }else {
            statusTV.setVisibility(View.VISIBLE);
            calendarView.setVisibility(View.VISIBLE);
            addNewBtn.setVisibility(View.GONE);
            updateBtn.setVisibility(View.VISIBLE);
            id = chamber.getAdd_ch_id();
            chamberName = chamber.getChamberName();
            days = chamber.getDateList();
            sTime = chamber.getStart();
            eTime = chamber.getEnd();
            patientCount = chamber.getPatient_count();
            house = chamber.getHouse();
            floor = chamber.getFloor();
            room = chamber.getRoom();
            block = chamber.getBlock();
            road = chamber.getRoad();
            area = chamber.getArea();
            city = chamber.getCity();
            zip = chamber.getZip();
            country = chamber.getCountry_id();
            fee = chamber.getFees();
            status = chamber.getStatus();
            doctorID = chamber.getDoctorID();

            for (String d: days){

                long startDate = 0L;
                try {

                    fullDateString = d+"/"+monthString+"/"+yearString;
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = sdf.parse(fullDateString);
                    startDate = date.getTime();
                    dates.add(dateString);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Event ev = new Event(Color.GREEN, startDate, "Doctor Available");
                calendarView.addEvent(ev);
            }

            if (chamber.getStatus().equals("pending")){
                statusTV.setText("This chamber is not approved yet");
            }else if (chamber.getStatus().equals("accepted")){
                statusTV.setText("This chamber is approved");
            }else if (chamber.getStatus().equals("rejected")){
                statusTV.setText("This chamber is not approved");
            }
            if (chamberName.equals("null")){

            }else {
                chamberNameEdt.setText(chamberName);
            }
            startTimeTV.setText(sTime);
            endTimeTV.setText(eTime);
            patientCountEdt.setText(patientCount);
            cityEdt.setText(city);
            areaEdt.setText(area);
            if (block.equals("null")){

            }else {
                blockEdt.setText(block);
            }
            roadEdt.setText(road);
            houseEdt.setText(house);
            floorEdt.setText(floor);
            roomEdt.setText(room);
            if (zip.equals("null")){

            }else {
                zipEdt.setText(zip);
            }
            if (country.equals("null")){

            }else {
                countryEdt.setText(country);
            }
            feeEdt.setText(fee);
        }

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chamberName = chamberNameEdt.getText().toString().trim();
                patientCount = patientCountEdt.getText().toString().trim();
                city = cityEdt.getText().toString().trim();
                area = areaEdt.getText().toString().trim();
                block = blockEdt.getText().toString().trim();
                road = roadEdt.getText().toString().trim();
                house = houseEdt.getText().toString().trim();
                floor = floorEdt.getText().toString().trim();
                room = roomEdt.getText().toString().trim();
                zip = zipEdt.getText().toString().trim();
                country = countryEdt.getText().toString().trim();
                fee = feeEdt.getText().toString().trim();
                addChamberInfo();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddNewScheduleActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void addSchedule(View view) {
        chamberName = chamberNameEdt.getText().toString().trim();
        patientCount = patientCountEdt.getText().toString().trim();
        city = cityEdt.getText().toString().trim();
        area = areaEdt.getText().toString().trim();
        block = blockEdt.getText().toString().trim();
        road = roadEdt.getText().toString().trim();
        house = houseEdt.getText().toString().trim();
        floor = floorEdt.getText().toString().trim();
        room = roomEdt.getText().toString().trim();
        zip = zipEdt.getText().toString().trim();
        country = countryEdt.getText().toString().trim();
        fee = feeEdt.getText().toString().trim();

        if (chamberName.isEmpty()) {
            chamberName = "null";
        }

        if (patientCount.isEmpty()) {
            patientCountEdt.setError(getString(R.string.required_field));
            patientCountEdt.requestFocus();
            return;
        }

        if (city.isEmpty()) {
            cityEdt.setError(getString(R.string.required_field));
            cityEdt.requestFocus();
            return;
        }

        if (area.isEmpty()) {
            areaEdt.setError(getString(R.string.required_field));
            areaEdt.requestFocus();
            return;
        }

        if (block.isEmpty()) {
            block = "null";
        }

        if (road.isEmpty()) {
            roadEdt.setError(getString(R.string.required_field));
            roadEdt.requestFocus();
            return;
        }

        if (house.isEmpty()) {
            houseEdt.setError(getString(R.string.required_field));
            houseEdt.requestFocus();
            return;
        }

        if (floor.isEmpty()) {
            floorEdt.setError(getString(R.string.required_field));
            floorEdt.requestFocus();
            return;
        }

        if (room.isEmpty()) {
            roomEdt.setError(getString(R.string.required_field));
            roomEdt.requestFocus();
            return;
        }

        if (zip.isEmpty()) {
            zip = "null";
        }

        if (country.isEmpty()) {
            country = "null";
        }

        if (fee.isEmpty()) {
            feeEdt.setError(getString(R.string.required_field));
            feeEdt.requestFocus();
            return;
        }

        if (days.size() == 0){
            Toast.makeText(this, "Select dates", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sTime.isEmpty() || eTime.isEmpty()) {
            Toast.makeText(this, "Time Schedule missing...!", Toast.LENGTH_SHORT).show();
        } else {
            dialog.show();
            id = chamberRef.push().getKey();
            addChamberInfo();
        }
    }

    private void addChamberInfo() {

        final AddChamber chamber = new AddChamber(id, chamberName, days, sTime, eTime, patientCount, house, floor, room, block, road, area, city, zip, country, fee, status, doctorID);

        chamberRef.child(id).child("Details").setValue(chamber).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    rootRef.child("Admin").child("New Chamber").child(id).child("Details").setValue(chamber).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                dialog.dismiss();
                                Toast.makeText(AddNewScheduleActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AddNewScheduleActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(AddNewScheduleActivity.this, "Failed to add chamber", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                } else {
                    Toast.makeText(AddNewScheduleActivity.this, "Failed to add chamber", Toast.LENGTH_SHORT).show();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.stTime) {
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            if (hourOfDay > 11) {
                                if (hourOfDay == 12) {
                                    startTimeTV.setText(hourOfDay + ":" + minute + " PM");
                                    sTime = startTimeTV.getText().toString();
                                } else {
                                    int cou = hourOfDay - 12;
                                    startTimeTV.setText(cou + ":" + minute + " PM");
                                    sTime = startTimeTV.getText().toString();
                                }


                            } else {
                                startTimeTV.setText(hourOfDay + ":" + minute + " AM");
                                sTime = startTimeTV.getText().toString();
                            }

                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();

        } else if (view.getId() == R.id.endTime) {
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            if (hourOfDay > 11) {
                                if (hourOfDay == 12) {
                                    endTimeTV.setText(hourOfDay + ":" + minute + " PM");
                                    eTime = endTimeTV.getText().toString();
                                } else {
                                    int cou = hourOfDay - 12;
                                    endTimeTV.setText(cou + ":" + minute + " PM");
                                    eTime = endTimeTV.getText().toString();
                                }


                            } else {
                                endTimeTV.setText(hourOfDay + ":" + minute + " AM");
                                eTime = endTimeTV.getText().toString();
                            }
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();

        }
    }

}
