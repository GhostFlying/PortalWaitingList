package com.ghostflying.portalwaitinglist.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ghostflying.portalwaitinglist.R;
import com.ghostflying.portalwaitinglist.data.PortalDetail;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PortalDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PortalDetailFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public static PortalDetailFragment newInstance() {
        PortalDetailFragment fragment = new PortalDetailFragment();
        return fragment;
    }

    public PortalDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_portal_detail, container, false);
    }

    public interface OnFragmentInteractionListener {
        public PortalDetail getSelectedPortal();
    }


}
