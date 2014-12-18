package com.ghostflying.portalwaitinglist.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import com.ghostflying.portalwaitinglist.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DaysPickerDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DaysPickerDialogFragment extends DialogFragment {

    // the fragment initialization parameters
    private static final String ARG_MIN = "min";
    private static final String ARG_MAX = "max";
    private static final String ARG_CURRENT = "current";
    private static final String ARG_TITLE = "title";

    private int mMin;
    private int mMax;
    private int mCurrent;
    private int mTitle;
    private DialogButtonClickListener mListener;


    /**
     * Create new instance
     *
     * @param current   current value to be set.
     * @param min       resId of min value allowed.
     * @param max       resId of max value allowed.
     * @param title     resId of the title of the dialog.
     * @return A new instance of fragment NumberPickerDialogFragment.
     */
    public static DaysPickerDialogFragment newInstance(int current, int min, int max, int title) {
        DaysPickerDialogFragment fragment = new DaysPickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CURRENT, current);
        args.putInt(ARG_MIN, min);
        args.putInt(ARG_MAX, max);
        args.putInt(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    public DaysPickerDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DialogButtonClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogButtonClickListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrent = getArguments().getInt(ARG_CURRENT);
            mMin = getArguments().getInt(ARG_MIN);
            mMax = getArguments().getInt(ARG_MAX);
            mTitle = getArguments().getInt(ARG_TITLE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_days_picker_dialog, null);
        final NumberPicker numberPicker = (NumberPicker) v.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(getResources().getInteger(mMax));
        numberPicker.setMinValue(getResources().getInteger(mMin));
        numberPicker.setValue(mCurrent);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

            }
        });
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(mTitle))
                .setView(v)
                .setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onPositiveButtonClick(numberPicker.getValue(), mTitle);
                    }
                })
                .setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onNegativeButtonClick(numberPicker.getValue(), mTitle);
                    }
                });
        return builder.create();
    }

    /**
     * Interface to interactive with activity.
     */
    public interface DialogButtonClickListener{
        public void onPositiveButtonClick(int value, int title);
        public void onNegativeButtonClick(int value, int title);
    }
}
