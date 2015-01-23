package com.ghostflying.portalwaitinglist;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;

import com.ghostflying.portalwaitinglist.fragment.ReDesignDetailFragment;


public class DetailActivity extends ActionBarActivity{

    public static final String ARG_CLICKED_PORTAL = "clickedItem";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Parcelable clickPortal = getIntent().getParcelableExtra(ARG_CLICKED_PORTAL);
        if (savedInstanceState == null) {
            ReDesignDetailFragment fragment;
            if (clickPortal != null)
                fragment = ReDesignDetailFragment.newInstance(clickPortal);
            else{
                String data = getIntent().getDataString();
                fragment = ReDesignDetailFragment.newInstance(
                        data.substring(data.lastIndexOf("/" + 1))
                );
            }
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }
}
