package com.ghostflying.portalwaitinglist;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.transition.ChangeBounds;
import android.transition.TransitionSet;

import com.ghostflying.portalwaitinglist.animation.PortalHeaderBackgroundTransition;
import com.ghostflying.portalwaitinglist.fragment.ReDesignDetailFragment;

import java.io.Serializable;


public class DetailActivity extends ActionBarActivity{

    public static final String ARG_CLICKED_PORTAL = "clickedItem";

    private TransitionSet mSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Serializable clickPortal = getIntent().getSerializableExtra(ARG_CLICKED_PORTAL);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, ReDesignDetailFragment.newInstance(clickPortal))
                    .commit();
        }
    }
}
