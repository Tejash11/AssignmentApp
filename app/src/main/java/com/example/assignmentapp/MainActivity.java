package com.example.assignmentapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    TextView downloadbutton, uploadbutton;
    EditText editText;
    StorageReference storageReference;
    DatabaseReference databaseReference;

//    String filepath = "GURU%20CARES.pdf";
//    URL url = null;
//    String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.filename);
        downloadbutton = findViewById(R.id.downloadbtn);
        uploadbutton = findViewById(R.id.uploadbtn);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("uploadPdf");

        uploadbutton.setEnabled(false);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPdf();
            }
        });

//        initViews();
//        setListeners();
    }

    private void selectPdf() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"PDF File Selected"),12);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 12 && resultCode == RESULT_OK && data!=null && data.getData()!=null)
        {
            uploadbutton.setEnabled(true);
            editText.setText(data.getDataString().substring(data.getDataString().lastIndexOf("/")+1));

            uploadbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadbutton.setBackground(ContextCompat.getDrawable(getBaseContext(),R.drawable.darkblueoutline));
                    uploadPDfFileFirebase(data.getData());
                }
            });
        }

    }
/*file loading progress dialog box with loading percentaga*/
    private void uploadPDfFileFirebase(Uri data) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("File Loading...");
        progressDialog.show();

        StorageReference reference = storageReference.child("upload"+System.currentTimeMillis()+".pdf");
        reference.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (! uriTask.isComplete());
                        Uri uri = uriTask.getResult();

                        insertPDF insertPDF = new insertPDF(editText.getText().toString(),uri.toString());
                        databaseReference.child(databaseReference.push().getKey()).setValue(insertPDF);
                        Toast.makeText(MainActivity.this,"File Uploaded Successfully",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100.0* snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressDialog.setMessage("File Uploading.."+(int)progress+"%");


            }
        });
    }

    public void downloadpdfbutton(View view) {
        startActivity(new Intent(getApplicationContext(),DownloadPdfActivity.class));
    }
}