package com.example.voytovich.criminalintent;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import java.util.List;
import java.util.UUID;

public class DeleteCrimeFragment extends DialogFragment {
    private static final String CRIME_ID = "crime_id";

    public static DeleteCrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(CRIME_ID, crimeId);

        DeleteCrimeFragment fragment = new DeleteCrimeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final UUID crimeId = (UUID) getArguments().getSerializable(CRIME_ID);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_delete, null);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_fragment_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCrime(crimeId);

                    }
                })
                .setView(v)
                .create();
    }

    private void deleteCrime(UUID crimeId) {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        Crime crime = crimeLab.getCrime(crimeId);
        crimeLab.deleteCrime(crime);
        getTargetFragment().getActivity().finish();
    }
}
