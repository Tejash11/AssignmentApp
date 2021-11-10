package com.example.assignmentapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DownloadPdfActivity extends AppCompatActivity {
    ListView pdfdownload;
    DatabaseReference databaseReference;
    List<insertPDF> uploadPdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_pdf);

        pdfdownload = findViewById(R.id.downloadpdflistview);
        uploadPdf = new ArrayList<>();
        
        retrievePdfFiles();

        pdfdownload.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                insertPDF insertPDF = uploadPdf.get(position);

                Intent intent = new Intent(Intent.ACTION_VIEW);//to show All pdf files
                intent.setType("application/pdf");
                intent.setData(Uri.parse(insertPDF.getUrl()));
                startActivity(intent);
            }
        });
    }

    private void retrievePdfFiles() {
        databaseReference = FirebaseDatabase.getInstance().getReference("uploadPdf");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    insertPDF insertPDF = ds.getValue(com.example.assignmentapp.insertPDF.class);
                    uploadPdf.add(insertPDF);
                }

                String[] uploadname = new String[uploadPdf.size()];

                for (int i=0 ; i<uploadname.length;i++)
                {
                    uploadname[i] = uploadPdf.get(i).getName();
                }
                /*referencing data to listview*/
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_1,uploadname){
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView textView = (TextView) view
                                .findViewById(android.R.id.text1);
                        textView.setTextColor(Color.BLACK);
                        textView.setTextSize(20);
                        return view;
                    }
                };

                pdfdownload.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}