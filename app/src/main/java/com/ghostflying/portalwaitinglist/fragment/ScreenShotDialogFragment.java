package com.ghostflying.portalwaitinglist.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import com.ghostflying.portalwaitinglist.R;

/**
 * Created by ghostflying on 1/12/15.
 */
public class ScreenShotDialogFragment extends DialogFragment {

    public static ScreenShotDialogFragment newInstance() {
        return new ScreenShotDialogFragment();
    }

    public ScreenShotDialogFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_screenshot_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view);
        return builder.create();
    }
}
