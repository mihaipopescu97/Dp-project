package com.example.dpproj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static DatabaseReference ref;
    private Spinner spinner;
    private ImageView imageView;

    private List<String> ids;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Activity created");

        ref = FirebaseDatabase.getInstance().getReference().child("Ids");
        spinner = findViewById(R.id.spinner);
        imageView = findViewById(R.id.imageView);
        ids = new ArrayList<>();
        adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, ids);
        spinner.setAdapter(adapter);
    }


    public void getIds(View view) {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ids.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    for(DataSnapshot d : ds.getChildren()) {
                        ids.add(d.getValue(String.class));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCanceled", databaseError.toException());
            }
        });
    }

    public void downloadImage(View view) {
        if(spinner.getSelectedItem() != null) {
            new DownloadTask(this).execute(spinner.getSelectedItem().toString());
        }
    }

    private static class DownloadTask extends AsyncTask<String, Void, Void> {
        static final String EXTENSION = ".jpg";
        WeakReference<MainActivity> weakReference;
        StorageReference storageReference;

        DownloadTask(MainActivity activity) {
            weakReference = new WeakReference<>(activity);
            storageReference = FirebaseStorage.getInstance().getReference();
        }

        @Override
        protected Void doInBackground(String... strings) {
            final MainActivity activity = weakReference.get();
            StorageReference reference = storageReference.child(strings[0] + EXTENSION);
            reference.getBytes(1024*1024)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            activity.imageView.setImageBitmap(bitmap);
                        }
                    });
            return null;
        }
    }
}
