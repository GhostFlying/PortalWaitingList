package com.ghostflying.portalwaitinglist;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;

import com.ghostflying.portalwaitinglist.fragment.ReDesignDetailFragment;
import com.ghostflying.portalwaitinglist.util.SettingUtil;


public class DetailActivity extends AppCompatActivity {

    public static final String ARG_CLICKED_PORTAL = "clickedItem";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Parcelable clickPortal = getIntent().getParcelableExtra(ARG_CLICKED_PORTAL);
        if (savedInstanceState == null) {
            Fragment fragment;
            if (clickPortal != null)
                fragment = ReDesignDetailFragment.newInstance(clickPortal);
            else{
                SettingUtil.getSettings(this);
                String data = getIntent().getDataString();
                String messageId = data.substring(data.lastIndexOf("/") + 1);
                fragment = ReDesignDetailFragment.newInstance(messageId);
            }
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }
}
