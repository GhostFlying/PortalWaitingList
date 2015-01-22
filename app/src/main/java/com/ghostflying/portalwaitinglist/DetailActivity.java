package com.ghostflying.portalwaitinglist;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.transition.TransitionSet;

import com.ghostflying.portalwaitinglist.fragment.ReDesignDetailFragment;


public class DetailActivity extends ActionBarActivity{

    public static final String ARG_CLICKED_PORTAL = "clickedItem";

    private TransitionSet mSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Parcelable clickPortal = getIntent().getParcelableExtra(ARG_CLICKED_PORTAL);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, ReDesignDetailFragment.newInstance(clickPortal))
                    .commit();
        }
    }
}
