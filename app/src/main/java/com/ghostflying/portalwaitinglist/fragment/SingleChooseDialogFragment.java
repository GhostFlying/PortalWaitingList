package com.ghostflying.portalwaitinglist.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.ghostflying.portalwaitinglist.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SingleChooseDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SingleChooseDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SingleChooseDialogFragment extends BaseAlertDialogFragment {
    // the fragment initialization parameters
    private static final String ARG_TITLE = "title";
    private static final String ARG_ITEMS = "items";
    private static final String ARG_ITEM_CHECKED = "checked";

    private int mTitle;
    private int mItems;
    private int mChecked;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title the resoucce id of the title of the dialog.
     * @param items the resource id of items to choose.
     * @return A new instance of fragment SingleChooseDialogFragment.
     */
    public static SingleChooseDialogFragment newInstance(int title, int items, int checked) {
        SingleChooseDialogFragment fragment = new SingleChooseDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE, title);
        args.putInt(ARG_ITEMS, items);
        args.putInt(ARG_ITEM_CHECKED, checked);
        fragment.setArguments(args);
        return fragment;
    }

    public SingleChooseDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getInt(ARG_TITLE);
            mItems = getArguments().getInt(ARG_ITEMS);
            mChecked = getArguments().getInt(ARG_ITEM_CHECKED);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        return new AlertDialog.Builder(getActivity())
                .setTitle(mTitle)
                .setSingleChoiceItems(mItems, mChecked, onClickListener)
                .setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onPositiveButtonClick(mChecked, mTitle);
                    }
                })
                .setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
    }

    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mChecked = which;
        }
    };
}
