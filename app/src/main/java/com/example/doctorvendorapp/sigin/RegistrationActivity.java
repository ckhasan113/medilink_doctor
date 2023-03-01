package com.example.doctorvendorapp.sigin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.doctorvendorapp.LoadingDialog;
import com.example.doctorvendorapp.MainActivity;
import com.example.doctorvendorapp.R;
import com.example.doctorvendorapp.pojo.DoctorDetails;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 848;
    private static final int PERMISSION_CODE = 8972;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private DatabaseReference rootRef;
    private DatabaseReference userRef;

    private StorageReference storageReference;

    private Spinner degreeSp, departmentSP;

    private EditText firstNameEdt, lastNameEdt, registerEdt, universityEdt, experienceEdt, phoneEdt, emailEdt, passwordEdt, rePassEdt;

    private CardView signUp;

    private String firstName = "", lastName = "", department = "",register, university = "", experience = "", phone = "", email = "", password = "", rePass = "";

    private String  degree = "";

    private String doctorID;

    private String photoLink ="";

    private Uri ImageUrl_main;

    private CircleImageView addImage;

    private boolean isPermissionGranted = false;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        loadingDialog = new LoadingDialog(RegistrationActivity.this, "Loading...");

        firebaseAuth = FirebaseAuth.getInstance();

        addImage = findViewById(R.id.addimagebtn);
        firstNameEdt = findViewById(R.id.first_name);
        lastNameEdt = findViewById(R.id.last_name);
        registerEdt = findViewById(R.id.registerNumber);
        universityEdt = findViewById(R.id.university);
        experienceEdt = findViewById(R.id.experience);
        phoneEdt = findViewById(R.id.phone_number);
        emailEdt = findViewById(R.id.email);
        passwordEdt = findViewById(R.id.password);
        rePassEdt = findViewById(R.id.re_password);

        degreeSp = findViewById(R.id.isp_degree);
        departmentSP = findViewById(R.id.isp_speciality);

        signUp = findViewById(R.id.doctor_register);

        ArrayAdapter spinnerDegreeAdapter = ArrayAdapter.createFromResource(RegistrationActivity.this, R.array.degree, R.layout.spinner_item_select_model);

        spinnerDegreeAdapter.setDropDownViewResource(R.layout.spinner_item_drop_down_model);

        degreeSp.setAdapter(spinnerDegreeAdapter);

        degreeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0){
                    degree = degreeSp.getItemAtPosition(i).toString().trim();
                }else {
                    degree = "";
                    return;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter spinnerSpecialityAdapter = ArrayAdapter.createFromResource(RegistrationActivity.this, R.array.speciality_id, R.layout.spinner_item_select_model);
        spinnerDegreeAdapter.setDropDownViewResource(R.layout.spinner_item_drop_down_model);

        departmentSP.setAdapter(spinnerSpecialityAdapter);

        departmentSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0){
                    department = departmentSP.getItemAtPosition(i).toString().trim();
                }else {
                    department = "";
                    return;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
                if(isPermissionGranted){
                    openGallery();
                }else {
                    Toast.makeText(RegistrationActivity.this, "Please Allow Permission", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ImageUrl_main == null){
                    Toast.makeText(RegistrationActivity.this, "Select a profile picture", Toast.LENGTH_SHORT).show();
                    return;
                }

                firstName = firstNameEdt.getText().toString();
                if (firstName.isEmpty()){
                    firstNameEdt.setError(getString(R.string.required_field));
                    firstNameEdt.requestFocus();
                    return;
                }

                lastName = lastNameEdt.getText().toString();
                if (lastName.isEmpty()){
                    lastNameEdt.setError(getString(R.string.required_field));
                    lastNameEdt.requestFocus();
                    return;
                }

                if (degree.isEmpty()){
                    Toast.makeText(RegistrationActivity.this, "Select degree", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (department.isEmpty()){
                    Toast.makeText(RegistrationActivity.this, "Select speciality", Toast.LENGTH_SHORT).show();
                    return;
                }


                register = registerEdt.getText().toString();
                if (register.isEmpty()){
                    registerEdt.setError(getString(R.string.required_field));
                    registerEdt.requestFocus();
                    return;
                }

                university = universityEdt.getText().toString();
                if (university.isEmpty()){
                    universityEdt.setError(getString(R.string.required_field));
                    universityEdt.requestFocus();
                    return;
                }
                experience = experienceEdt.getText().toString();
                if (experience.isEmpty()){
                    experienceEdt.setError(getString(R.string.required_field));
                    experienceEdt.requestFocus();
                    return;
                }

                phone = phoneEdt.getText().toString();
                if (phone.isEmpty()){
                    phoneEdt.setError(getString(R.string.required_field));
                    phoneEdt.requestFocus();
                    return;
                }

                email = emailEdt.getText().toString();
                if (email.isEmpty()){
                    emailEdt.setError(getString(R.string.required_field));
                    emailEdt.requestFocus();
                    return;
                }

                password = passwordEdt.getText().toString()+"doctor";
                if (password.isEmpty()){
                    passwordEdt.setError(getString(R.string.required_field));
                    passwordEdt.requestFocus();
                    return;
                }
                rePass = rePassEdt.getText().toString()+"doctor";
                if (rePass.isEmpty()){
                    rePassEdt.setError(getString(R.string.required_field));
                    rePassEdt.requestFocus();
                    return;
                }

                if (!(rePass.equals(password))){
                    Toast.makeText(RegistrationActivity.this, "Password does not match", Toast.LENGTH_SHORT).show();
                    rePassEdt.setError("Enter same password");
                    rePassEdt.requestFocus();
                    return;
                }

                loadingDialog.show();

                registerUser();
            }
        });
    }

    private void registerUser() {
        try {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                firebaseAuth = FirebaseAuth.getInstance();
                                rootRef = FirebaseDatabase.getInstance().getReference();
                                user = firebaseAuth.getCurrentUser();
                                userRef = rootRef.child("Doctor").child(user.getUid());
                                doctorID = String.valueOf(user.getUid());
                                Toast.makeText(RegistrationActivity.this, "Successful", Toast.LENGTH_SHORT).show();

                                userRegistration();
                            }else {
                                Toast.makeText(RegistrationActivity.this, "Failed to register...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }catch (Exception e){
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
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
                photoLink = downloadUri.toString();
                DoctorDetails details = new DoctorDetails(doctorID, firstName, lastName, degree, department, register, university, experience, phone, email, password, photoLink);
                userRef.child("Details").setValue(details).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            loadingDialog.dismiss();
                            Toast.makeText(RegistrationActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);

                            String token = FirebaseInstanceId.getInstance().getToken();
                            Map<String, Object> tokenMap = new HashMap<>();
                            tokenMap.put("device_token", token);

                            rootRef.child("Users").child(user.getUid()).child("Token").setValue(tokenMap);

                            startActivity(intent);
                            finish();

                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(RegistrationActivity.this, "Failed to registration", Toast.LENGTH_SHORT).show();
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
            addImage.setImageURI(ImageUrl_main);
        }
    }

    private void checkPermission() {
        if ((ActivityCompat
                .checkSelfPermission(RegistrationActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) &&
                (ActivityCompat
                        .checkSelfPermission(RegistrationActivity.this
                                ,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(RegistrationActivity.this,
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
    public void onBackPressed() {
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
