package com.ghostflying.portalwaitinglist;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.ghostflying.portalwaitinglist.Util.SettingUtil;
import com.ghostflying.portalwaitinglist.data.PortalDetail;
import com.ghostflying.portalwaitinglist.fragment.PortalDetailFragment;
import com.ghostflying.portalwaitinglist.fragment.PortalListFragment;


public class MainActivity extends ActionBarActivity
        implements PortalListFragment.OnFragmentInteractionListener,
                   PortalDetailFragment.OnFragmentInteractionListener{

    String account;
    Fragment portalListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // read all settings from storage.
        SettingUtil.readAllSettings(this);
        // If account is not set, usually user open this first time
        // turn to AuthIntent.
        if ((account = SettingUtil.getAccount()) == null){
            doAuth();
        }
        setContentView(R.layout.activity_main);
        portalListFragment = PortalListFragment.newInstance();
        getFragmentManager()
                .beginTransaction()
                .add(R.id.content_layout, portalListFragment)
                .commit();
    }

    private void doAuth() {
        Intent authIntent = new Intent(new Intent(this, AuthActivity.class));
        authIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(authIntent);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (account == null){
            if ((account = SettingUtil.getAccount()) == null){
                this.finish();
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        SettingUtil.saveAllSettings();
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() != 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void doAuthInActivity() {
        doAuth();
    }

    @Override
    public void portalItemClicked(PortalDetail clickedPortal) {
        getFragmentManager()
                .beginTransaction()
                .hide(portalListFragment)
                .add(R.id.content_layout, PortalDetailFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public PortalDetail getSelectedPortal() {
        return null;
    }
}
