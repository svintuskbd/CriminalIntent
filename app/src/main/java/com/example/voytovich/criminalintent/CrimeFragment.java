package com.example.voytovich.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    public static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_DELETE = "DialogDelete";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;

    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private EditText mDetails;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mDeleteCrime;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallSuspect;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        if (mTitleField != null) {
            mTitleField.setText(mCrime.getTitle());
            mTitleField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence c, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence c, int start, int before, int count) {
                    mCrime.setTitle(c.toString());
                }

                @Override
                public void afterTextChanged(Editable c) {

                }
            });
        }

        mDetails = (EditText) v.findViewById(R.id.crime_details);
        if (mDetails != null) {
            mDetails.setText(mCrime.getDetails());
            mDetails.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence c, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence c, int start, int before, int count) {
                    mCrime.setDetails(c.toString());
                }

                @Override
                public void afterTextChanged(Editable c) {

                }
            });
        }

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        if (mDateButton != null) {
            updateDate();
            mDateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getFragmentManager();
                    DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                    dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                    dialog.show(manager, DIALOG_DATE);
                }
            });
        }

        mDeleteCrime = (Button) v.findViewById(R.id.crime_delete);
        if (mDeleteCrime != null) {
            mDeleteCrime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getFragmentManager();
                    DeleteCrimeFragment dialog = DeleteCrimeFragment.newInstance(mCrime.getId());
                    dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                    dialog.show(manager, DIALOG_DELETE);
                }
            });
        }

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        if (mSolvedCheckBox != null) {
            mSolvedCheckBox.setChecked(mCrime.isSolved());
            mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mCrime.setSolved(isChecked);
                }
            });
        }

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        if (mReportButton != null) {
            mReportButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v){
                    ShareCompat.IntentBuilder shareIntent = ShareCompat.IntentBuilder.from(getActivity());
                    shareIntent.setType("text/plain");
                    shareIntent.setText(getCrimeReport());
                    shareIntent.setSubject(getString(R.string.crime_report_suspect));
                    shareIntent.setChooserTitle(getString(R.string.send_report));
                    Intent i = shareIntent.getIntent();

//                    Intent i = new Intent(Intent.ACTION_SEND);
//                    i.setType("text/plain");
//                    i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
//                    i.putExtra(Intent.EXTRA_SUBJECT,
//                            getString(R.string.crime_report_suspect));
//                    i = Intent.createChooser(i, getString(R.string.send_report));
                    startActivity(i);
                }
            });
        }

        mCallSuspect = (Button) v.findViewById(R.id.call_suspect);
        if (mCallSuspect != null) {
            mCallSuspect.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String phone = getPhoneSuspect(mCrime.getSuspectId());
                    if (phone != null) {
                        Uri call = Uri.parse("tel:" + phone);
                        Intent i = new Intent(Intent.ACTION_DIAL);
                        i.setData(call);
                        startActivity(i);
                    }
                }
            });
        }

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);

        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        if (mSuspectButton != null){
            mSuspectButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                }
            });
        }

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;

        if (canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = ViewPhotoActivity.newIntent(getActivity(), mCrime.getId());
                startActivity(i);
            }
        });

        updatePhotoView();

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();

            String[] queryFields = new String[] {
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts._ID
            };

            Cursor c = getActivity().getContentResolver()
                .query(contactUri, queryFields, null, null, null);

            try {
                if (c.getCount() == 0){
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(0);
                String suspect_id = c.getString(1);
                mCrime.setSuspect(suspect);
                mCrime.setSuspectId(suspect_id);
                mSuspectButton.setText(suspect);
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            updatePhotoView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }

    private String getPhoneSuspect(String suspect_id){
        String phone = null;
        Cursor cursor = null;

        try {
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
            cursor = getActivity().getContentResolver()
                .query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{suspect_id},
                    null
                );
            while (cursor != null && cursor.moveToNext()) {
                phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            Toast.makeText(getActivity(), phone, Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return phone;
    }

    private void updateDate() {
        mDateButton.setText(Crime.getFormatingData(mCrime.getDate()));
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }
}
