package com.ghostflying.portalwaitinglist;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.transition.ChangeBounds;
import android.transition.TransitionSet;

import com.ghostflying.portalwaitinglist.animation.PortalHeaderBackgroundTransition;
import com.ghostflying.portalwaitinglist.fragment.ReDesignDetailFragment;
import com.ghostflying.portalwaitinglist.util.SettingUtil;


public class DetailActivity extends ActionBarActivity {

    public static final String ARG_CLICKED_PORTAL = "clickedItem";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // animation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            TransitionSet enterSet = new TransitionSet();
            enterSet.setOrdering(TransitionSet.ORDERING_TOGETHER);
            enterSet.addTransition(new ChangeBounds());
            PortalHeaderBackgroundTransition headerTransition = new PortalHeaderBackgroundTransition();
            headerTransition.setMode(0);
            enterSet.addTransition(headerTransition);
            getWindow().setSharedElementEnterTransition(enterSet);
            TransitionSet returnSet = new TransitionSet();
            returnSet.addTransition(new ChangeBounds());
            headerTransition = new PortalHeaderBackgroundTransition();
            headerTransition.setMode(1);
            returnSet.addTransition(headerTransition);
            getWindow().setSharedElementReturnTransition(returnSet);
        }
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
