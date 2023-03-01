package com.example.doctorvendorapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorvendorapp.adapter.AppointmentListAdapter;
import com.example.doctorvendorapp.adapter.ChamberListAdapter;
import com.example.doctorvendorapp.pojo.Appointments;
import com.example.doctorvendorapp.pojo.DoctorDetails;
import com.example.doctorvendorapp.pojo.addschedule.AddChamber;
import com.example.doctorvendorapp.sigin.LoginActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AppointmentListAdapter.OnCompleteListener, Appointment_Done_Dialog.AppointmentDialogListener, ChamberListAdapter.ChamberListAdapterListener {

    private static final int GALLERY_REQUEST_CODE = 848;
    private static final int PERMISSION_CODE = 8972;

    private boolean isPermissionGranted = false;

    private Uri ImageUrl_main;

    private FirebaseAuth auth;
    private FirebaseUser user;

    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference doctorRef;
    private DatabaseReference adminRef;
    private DatabaseReference patientRef;
    private DatabaseReference adminDoctorRef;
    private DatabaseReference completeRef;

    private DatabaseReference adapterRootRef;
    private DatabaseReference adapterUserRef;
    private DatabaseReference adapterDoctorChamberRef;
    private DatabaseReference adapterDoctorAppointmentRef;

    private StorageReference storageReference;

    private RecyclerView recyclerView;
    private ChamberListAdapter chamberListAdapter;
    private AppointmentListAdapter appointmentListAdapter;

    private List<AddChamber> chamberList = new ArrayList<AddChamber>();
    private List<Appointments> appointmentList = new ArrayList<Appointments>();

    private Toolbar toolbar;

    private TextView scheduleTV, appointmentTV, addNewSchedule;

    private ImageView profileImageIV;

    private TextView nameTV, designationTV, specialityTV, uploadPhotoTV, describeTV, savePhotoTV;

    private String firstName, lastName, department, register, university, experience, phone, email, type, photo, degree, doctorID, password;

    private DoctorDetails doctor = new DoctorDetails();

    private LoadingDialog dialog;

    private String appAppointmentKey, appDoctorID, appPatientID, appDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notify1", "notification", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        FirebaseMessaging.getInstance().subscribeToTopic("doctor");

        dialog = new LoadingDialog(MainActivity.this,"Loading...");
        dialog.show();

        storageReference = FirebaseStorage.getInstance().getReference();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("Doctor").child(user.getUid());
        doctorRef = userRef.child("Details");
        adminRef = rootRef.child("Admin");

        toolbar = (Toolbar) findViewById(R.id.toolbar_base);
        setSupportActionBar(toolbar);

        profileImageIV = findViewById(R.id.DoctorProfileImage);
        nameTV = findViewById(R.id.DoctorName);
        designationTV = findViewById(R.id.DoctorTitle);
        specialityTV = findViewById(R.id.DoctorSpeciality);
        uploadPhotoTV = findViewById(R.id.upload_Photo);
        savePhotoTV = findViewById(R.id.save_Photo);
        describeTV = findViewById(R.id.discribe);

        scheduleTV = findViewById(R.id.schedule);
        appointmentTV = findViewById(R.id.appointment);
        addNewSchedule = findViewById(R.id.add_new_schedule);

        recyclerView = findViewById(R.id.doctor_base_recycler);

        uploadPhotoTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
                if(isPermissionGranted){
                    openGallery();
                }else {
                    Toast.makeText(MainActivity.this, "Please Allow Permission", Toast.LENGTH_SHORT).show();
                }
            }
        });

        savePhotoTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                userRegistration();
            }
        });

        appointmentTV.setBackgroundColor(Color.parseColor("#FF5722"));
        scheduleTV.setBackgroundColor(Color.parseColor("#ECD0C8"));

        adapterRootRef = FirebaseDatabase.getInstance().getReference();
        adapterUserRef = adapterRootRef.child("Doctor").child(user.getUid());
        adapterDoctorChamberRef = adapterUserRef.child("ChamberList").child("All");
        adapterDoctorAppointmentRef = adapterUserRef.child("AppointmentList").child("Pending");


        adapterDoctorAppointmentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                appointmentList.clear();
                for (DataSnapshot cd: dataSnapshot.getChildren()){
                    Appointments chamber = cd.child("Value").getValue(Appointments.class);
                    appointmentList.add(chamber);
                }
                Collections.reverse(appointmentList);
                appointmentListAdapter = new AppointmentListAdapter(MainActivity.this, appointmentList);
                LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(llm);
                recyclerView.setAdapter(appointmentListAdapter);
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        appointmentTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appointmentTV.setBackgroundColor(Color.parseColor("#FF5722"));
                scheduleTV.setBackgroundColor(Color.parseColor("#ECD0C8"));
                addNewSchedule.setVisibility(View.GONE);

                adapterDoctorAppointmentRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        appointmentList.clear();
                        //describeTV.setText(dataSnapshot.getValue().toString());
                        for (DataSnapshot cd: dataSnapshot.getChildren()){
                            Appointments chamber = cd.child("Value").getValue(Appointments.class);
                            appointmentList.add(chamber);
                        }
                        Collections.reverse(appointmentList);
                        appointmentListAdapter = new AppointmentListAdapter(MainActivity.this, appointmentList);
                        LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
                        llm.setOrientation(LinearLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(llm);
                        recyclerView.setAdapter(appointmentListAdapter);
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        scheduleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.show();

                appointmentTV.setBackgroundColor(Color.parseColor("#ECD0C8"));
                scheduleTV.setBackgroundColor(Color.parseColor("#FF5722"));
                addNewSchedule.setVisibility(View.VISIBLE);

                adapterDoctorChamberRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        chamberList.clear();
                        for (DataSnapshot cd: dataSnapshot.getChildren()){
                            AddChamber chamber = cd.child("Details").getValue(AddChamber.class);
                            chamberList.add(chamber);
                        }
                        Collections.reverse(chamberList);
                        chamberListAdapter = new ChamberListAdapter(MainActivity.this, chamberList);
                        LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
                        llm.setOrientation(LinearLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(llm);
                        recyclerView.setAdapter(chamberListAdapter);
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        addNewSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddNewScheduleActivity.class);
                startActivity(intent);
                finish();
            }
        });

        doctorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DoctorDetails doc = dataSnapshot.getValue(DoctorDetails.class);

                firstName = doc.getFirstName();
                lastName = doc.getLastName();
                degree = doc.getDegree();
                department = doc.getDepartment();
                register = doc.getRegisterNumber();
                university = doc.getUniversity();
                experience = doc.getExperience();
                email = doc.getEmail();
                phone = doc.getPhone();
                photo = doc.getImageURI();

                doctorID = doc.getDoctorID();
                password = doc.getPassword();

                Uri photoUri = Uri.parse(photo);
                Picasso.get().load(photoUri).into(profileImageIV);

                nameTV.setText("Dr. "+firstName+" "+lastName);
                designationTV.setText(degree);
                specialityTV.setText(department);
                describeTV.setText(department+", "+university+", "+experience+", "+email+", "+phone);

                dialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void userRegistration() {
        storageReference = FirebaseStorage.getInstance().getReference();
        final Uri imageUri = ImageUrl_main;
        final StorageReference imageRef = storageReference.child("DoctorImage").child(imageUri.getLastPathSegment());
        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri downloadUri = task.getResult();
                String photoLink = downloadUri.toString();
                DoctorDetails details = new DoctorDetails(doctorID, firstName, lastName, degree, department, register, university, experience, phone, email, password, photoLink);
                userRef.child("Details").setValue(details).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);

                            String token = FirebaseInstanceId.getInstance().getToken();
                            Map<String, Object> tokenMap = new HashMap<>();
                            tokenMap.put("device_token", token);

                            rootRef.child("Users").child(user.getUid()).child("Token").setValue(tokenMap);

                            startActivity(intent);
                            finish();

                        } else {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed to registration", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == GALLERY_REQUEST_CODE){
            ImageUrl_main = data.getData();
            profileImageIV.setImageURI(ImageUrl_main);
            savePhotoTV.setVisibility(View.VISIBLE);
            uploadPhotoTV.setVisibility(View.GONE);
        }
    }

    private void checkPermission() {
        if ((ActivityCompat
                .checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) &&
                (ActivityCompat
                        .checkSelfPermission(MainActivity.this
                                ,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },PERMISSION_CODE);

        }else {
            isPermissionGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode ==PERMISSION_CODE){

            if((grantResults[0] ==PackageManager.PERMISSION_GRANTED
                    && grantResults[1] ==PackageManager.PERMISSION_GRANTED
            )){
                isPermissionGranted = true;
            }else {
                checkPermission();
            }
        }
    }

    @Override
    public void onSubmit() {
        dialog.show();
        adapterDoctorAppointmentRef.child(appAppointmentKey).child("Value").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Appointments appointment = dataSnapshot.getValue(Appointments.class);
                addOnAdmin(appointment);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addOnAdmin(final Appointments appointment) {
        completeRef.setValue(appointment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                adminDoctorRef.setValue(appointment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        adapterDoctorAppointmentRef.child(appAppointmentKey).removeValue();
                        patientRef = rootRef.child("Patient").child(appPatientID).child("AppointmentList").child(appAppointmentKey);
                        patientRef.removeValue();
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onDone(String appointmentKey, String doctorID, String patientID, String appointmentDate) {
        appAppointmentKey = appointmentKey;
        appDoctorID = doctorID;
        appPatientID = patientID;
        appDate = appointmentDate;

        adminDoctorRef = adminRef.child("Doctor Completed Appointment").child(doctorID).child(appDate).child(appAppointmentKey).child("Value");
        completeRef = userRef.child("AppointmentList").child("Completed").child(appointmentKey).child("Value");

        Appointment_Done_Dialog descriptionDialog = new Appointment_Done_Dialog();
        descriptionDialog.show(getSupportFragmentManager(), "Confirmation: ");
    }

    @Override
    public void onChamberUpdate(AddChamber chamber) {
        Intent intent = new Intent(MainActivity.this, AddNewScheduleActivity.class);
        intent.putExtra("ChamberDetails", chamber);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_item,menu);
        return true;
    }

    public void log_out(MenuItem item){
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("device_token", "");
        rootRef.child("Users").child(user.getUid()).child("Token").updateChildren(tokenMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                auth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
