package edu.cuhk.csci3310.borrowfast;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.Result;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerPage extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    ZXingScannerView zXingScannerView;
    private FirebaseFirestore mFirestore;
    private FirebaseFirestore mFirestore2;
    private FirebaseFirestore mFirestore3;
    private String uid;
    private Query mQuery;
    private Query mQuery2;
    private Query mQuery3;
    private boolean canBorrow = true;
    final String[] bookDocumentID = new String[1];
    final String[] bookCopyID = new String[1];
    ArrayList<String> borrowedUID = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scanner_page);
        zXingScannerView = findViewById(R.id.ZXingScannerView_QRCode);
        FirebaseApp.initializeApp(this);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mFirestore3 = FirebaseFirestore.getInstance();
        mQuery3 = mFirestore3.collection("User").whereEqualTo("uid", uid).whereEqualTo("isadmin", true);
        mQuery3.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        System.out.println("66123");
                        //tvResult.setText(document.getString("title"));
                        Button returnButton = (Button) findViewById(R.id.Return);
                        returnButton.setVisibility(View.VISIBLE);


                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        //get the permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(this
                        , Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    100);
        }else{
            //if have the permission, open it
            openQRCamera();
        }


    }
    //open camera scanner
    private void openQRCamera(){
        if(zXingScannerView!=null){
            zXingScannerView.setResultHandler(this);
        }

        if(zXingScannerView!=null){
            zXingScannerView.startCamera();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults[0] ==0){
            openQRCamera();
        }else{
            Toast.makeText(this, "do not have the permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        zXingScannerView.stopCamera();
        super.onStop();
    }

    @Override
    public void handleResult(Result rawResult) {
        //tvResult.setText(rawResult.getText());

        TextView tvResult = findViewById(R.id.textView_Result);
        TextView titleC = findViewById(R.id.titleC);
        TextView yearC = findViewById(R.id.yearC);
        TextView authorC = findViewById(R.id.authorC);
        TextView titleT = findViewById(R.id.titleT);
        TextView yearT = findViewById(R.id.yearT);
        TextView authorT = findViewById(R.id.authorT);
        mFirestore = FirebaseFirestore.getInstance();
        mFirestore2 = FirebaseFirestore.getInstance();
        String result = rawResult.getText();
        int bid = Integer.parseInt(result);
        mQuery = mFirestore.collection("books").whereArrayContains("qr_code", bid);
        mQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        //tvResult.setText(document.getString("title"));
                        titleC.setText(document.getString("title"));
                        yearC.setText(document.getString("yearOfPublish"));
                        authorC.setText(document.getString("author"));
                        System.out.println("234");
                        System.out.println(document.getId());
                        bookDocumentID[0] = document.getId();
                        //borrowedUID = (ArrayList<String>) document.get("uid");


                        mQuery2 = mFirestore2.collection("books").document(bookDocumentID[0]).collection("bookCopyInfo").whereEqualTo("barcode", bid);
                        mQuery2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if(task.getResult().isEmpty()){
                                        System.out.println("345");
                                        canBorrow = false;
                                    }
                                    else{
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            System.out.println("123");
                                            //tvResult.setText(document.getString("title"));
                                            bookCopyID[0] = document.getId();
                                            System.out.println(bookCopyID[0]);
                                            canBorrow = true;
                                        }
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });

                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });




        tvResult.setVisibility(View.GONE);
        titleC.setVisibility(View.VISIBLE);
        yearC.setVisibility(View.VISIBLE);
        authorC.setVisibility(View.VISIBLE);
        titleT.setVisibility(View.VISIBLE);
        yearT.setVisibility(View.VISIBLE);
        authorT.setVisibility(View.VISIBLE);

        //continue to open camera
        //openQRCamera();

        Button borrow = (Button) findViewById(R.id.Borrow);
        borrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Borrow(view);
            }
        });

        Button cancel = (Button) findViewById(R.id.Cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cancel(view);
            }
        });

        Button returnButton = (Button) findViewById(R.id.Return);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnBook(view);
            }
        });
    }

    private void ReturnBook(View view) {
        mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection("books").document(bookDocumentID[0]).collection("bookCopyInfo").document(bookCopyID[0]).update("borrowedUID", "")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.v("LOGMessage", "ChangeSuccess");
                        Toast.makeText(ScannerPage.this, "Return Success", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(this, "do not have the permission", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.v("LOGMessage", "ChangeFail");
                        Toast.makeText(ScannerPage.this, "Return Fail", Toast.LENGTH_SHORT).show();
                    }
                });

        mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection("books").document(bookDocumentID[0]).update("borrowedUID", FieldValue.arrayRemove(uid))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.v("LOGMessage", "AddingSuccess");

                        //Toast.makeText(this, "do not have the permission", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.v("LOGMessage", "ChangeFail");

                    }
                });



        resetInitial();
        finish();
    }

    private void Cancel(View view) {

        resetInitial();
        finish();
    }

    private void Borrow(View view) {

        //String date = LocalDateTime.now().plusHours(8).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        //String dueDate = LocalDateTime.now().plusHours(8).plusDays(14).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Date date = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        Date dueDate = Date.from(LocalDateTime.now().plusDays(14).atZone(ZoneId.systemDefault()).toInstant());
        //date = date.plusDay(2);
        //DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        //String strDate = dateFormat.format(date);

        //System.out.println(date);
        //LocalDate myObj = LocalDate.now();
        if(canBorrow) {
            mFirestore = FirebaseFirestore.getInstance();
            mFirestore.collection("books").document(bookDocumentID[0]).collection("bookCopyInfo").document(bookCopyID[0]).update("borrowedUID", uid, "borrowedDate", date, "dueDate", dueDate, "renew_time", 0)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            Log.v("LOGMessage", "ChangeSuccess");
                            Toast.makeText(ScannerPage.this, "Borrow Success", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(this, "do not have the permission", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Log.v("LOGMessage", "ChangeFail");
                            Toast.makeText(ScannerPage.this, "Borrow Fail", Toast.LENGTH_SHORT).show();
                        }
                    });

            mFirestore = FirebaseFirestore.getInstance();
            mFirestore.collection("books").document(bookDocumentID[0]).update("borrowedUID", FieldValue.arrayUnion(uid))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            Log.v("LOGMessage", "AddingSuccess");

                            //Toast.makeText(this, "do not have the permission", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Log.v("LOGMessage", "ChangeFail");

                        }
                    });

        }
        else{
            Toast.makeText(ScannerPage.this, "The book cannot be borrowed", Toast.LENGTH_SHORT).show();
        }
        resetInitial();
        finish();
    }

    private void resetInitial() {
        TextView tvResult = findViewById(R.id.textView_Result);
        TextView titleC = findViewById(R.id.titleC);
        TextView yearC = findViewById(R.id.yearC);
        TextView authorC = findViewById(R.id.authorC);
        TextView titleT = findViewById(R.id.titleT);
        TextView yearT = findViewById(R.id.yearT);
        TextView authorT = findViewById(R.id.authorT);

        tvResult.setVisibility(View.VISIBLE);
        titleC.setVisibility(View.INVISIBLE);
        yearC.setVisibility(View.INVISIBLE);
        authorC.setVisibility(View.INVISIBLE);
        titleT.setVisibility(View.INVISIBLE);
        yearT.setVisibility(View.INVISIBLE);
        authorT.setVisibility(View.INVISIBLE);
    }

}