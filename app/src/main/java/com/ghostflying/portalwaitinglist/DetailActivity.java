package com.ghostflying.portalwaitinglist;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.transition.ChangeBounds;
import android.transition.TransitionSet;

import com.ghostflying.portalwaitinglist.animation.PortalHeaderBackgroundTransition;
import com.ghostflying.portalwaitinglist.fragment.ReDesignDetailFragment;

import java.io.Serializable;


public class DetailActivity extends ActionBarActivity
        implements ReDesignDetailFragment.OnFragmentInteractionListener {

    public static final String ARG_CLICKED_PORTAL = "clickedItem";

    private TransitionSet mSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setSharedElementTransition(true);
        Serializable clickPortal = getIntent().getSerializableExtra(ARG_CLICKED_PORTAL);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, ReDesignDetailFragment.newInstance(clickPortal))
                    .commit();
        }
    }

    @Override
    public void onBackPressed(){
        setSharedElementTransition(false);
        super.onBackPressed();
    }

    private void setSharedElementTransition(boolean isEnter){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            if (isEnter){
                mSet = new TransitionSet();
                mSet.setOrdering(TransitionSet.ORDERING_TOGETHER);
                mSet.addTransition(new ChangeBounds());
                PortalHeaderBackgroundTransition mBackgroundTransition = new PortalHeaderBackgroundTransition();
                mSet.addTransition(mBackgroundTransition);
                getWindow().setSharedElementEnterTransition(mSet);
            }
            else {
                ((PortalHeaderBackgroundTransition)mSet.getTransitionAt(1)).setMode(1);
            }
        }
    }

    @Override
    public void onNavigationClicked() {
        setSharedElementTransition(false);
    }
}
