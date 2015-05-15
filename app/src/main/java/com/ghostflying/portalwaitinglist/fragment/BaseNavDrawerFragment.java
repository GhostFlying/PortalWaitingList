package com.ghostflying.portalwaitinglist.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.ghostflying.portalwaitinglist.R;
import com.ghostflying.portalwaitinglist.SettingActivity;
import com.ghostflying.portalwaitinglist.util.SettingUtil;

/**
 * Created by ghostflying on 2/25/15.
 */
public abstract class BaseNavDrawerFragment extends Fragment {
    protected DrawerLayout drawerLayout;
    protected Toolbar toolbar;
    protected TextView totalPortals;
    protected TextView totalSubmission;
    protected TextView totalEdit;
    protected View selectedType;

    protected void setToolbar(View view){
        toolbar = (Toolbar)view.findViewById(R.id.action_bar_in_list);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected void setNavDrawer(View view) {
        drawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        // set the status bar bg when nav do not open
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.primary_dark));
        // handle the home button
        ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(getActivity(),
                        drawerLayout,
                        toolbar,
                        R.string.app_name,
                        R.string.app_name);
        actionBarDrawerToggle.syncState();
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        // last select from setting
        switch (SettingUtil.getTypeFilterMethod()) {
            case ALL:
                selectedType = view.findViewById(R.id.navigation_item_all);
                break;
            case SUBMISSION:
                selectedType = view.findViewById(R.id.navigation_item_submission);
                break;
            case EDIT:
                selectedType = view.findViewById(R.id.navigation_item_edit);
                break;
            default:
                selectedType = view.findViewById(R.id.navigation_item_all);
        }
        selectedType.setSelected(true);

        setTypeFilterOutListFragment(view);

        // other in navigation
        view.findViewById(R.id.navigation_item_setting).setOnClickListener(navigationDrawerClickListener);
        view.findViewById(R.id.navigation_item_feedback).setOnClickListener(navigationDrawerClickListener);
        totalPortals = (TextView) view.findViewById(R.id.navigation_drawer_total_portals);
        totalEdit = (TextView) view.findViewById(R.id.navigation_drawer_total_edit);
        totalSubmission = (TextView) view.findViewById(R.id.navigation_drawer_total_submission);

        // set the user avatar and account name
        if (SettingUtil.getAccount() != null) {
            ((TextView) view.findViewById(R.id.account_name)).setText(SettingUtil.getAccount());
            ((TextView) view.findViewById(R.id.user_avatar)).setText(
                    SettingUtil.getAccount().toUpperCase().substring(0, 1)
            );
        }
    }

    protected void setTypeFilterOutListFragment(View view){
        // type filter
        view.findViewById(R.id.navigation_item_all).setOnClickListener(getTypeFilterClickListener());
        view.findViewById(R.id.navigation_item_submission).setOnClickListener(getTypeFilterClickListener());
        view.findViewById(R.id.navigation_item_edit).setOnClickListener(getTypeFilterClickListener());
    }

    private View.OnClickListener navigationDrawerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.navigation_item_setting:
                    Intent setting = new Intent(getActivity(), SettingActivity.class);
                    startActivityForResult(setting, SettingActivity.REQUEST_SETTING);
                    getActivity().overridePendingTransition(R.animator.setting_swap_in_bottom, R.animator.setting_swap_out_bottom);
                    break;
                case R.id.navigation_item_feedback:
                    Intent mailIntent = new Intent(
                            Intent.ACTION_SENDTO,
                            Uri.fromParts("mailto", getString(R.string.author_mail), null)
                    );
                    mailIntent.putExtra(Intent.EXTRA_SUBJECT,
                            getString(R.string.navigation_drawer_feedback_subject));
                    startActivity(Intent.createChooser(
                            mailIntent, getString(R.string.navigation_drawer_send)));
                    break;
            }
        }
    };

    abstract View.OnClickListener getTypeFilterClickListener();
}
