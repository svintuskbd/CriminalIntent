package com.example.voytovich.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.UUID;

public class ViewPhotoActivity extends AppCompatActivity {

    private static final String EXTRA_CRIME_ID = "com.bignerdranch.android.criminalintent.crime_id";

    private ImageView mImageView;

    public static Intent newIntent(Context packageContext, UUID crimeId){
        Intent intent = new Intent(packageContext, ViewPhotoActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_image_crime);

        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        CrimeLab crimeLab = CrimeLab.get(this);
        Crime crime = crimeLab.getCrime(crimeId);
        File photoFile = crimeLab.getPhotoFile(crime);

        mImageView = (ImageView) findViewById(R.id.view_image_crime);
        if (photoFile != null) {
            Uri uri = Uri.fromFile(photoFile);
            mImageView.setImageURI(uri);
        }
        Toast.makeText(this, "qqq", Toast.LENGTH_LONG).show();
    }
}
