package com.ghostflying.portalwaitinglist;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.ghostflying.portalwaitinglist.Util.SettingUtil;


public class SettingActivity extends ActionBarActivity {
    CheckBox imageToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initialViews();
    }

    private void initialViews(){
        // action bar
        Toolbar toolbar = (Toolbar)findViewById(R.id.action_bar_in_setting);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // portal image setting
        imageToggle = (CheckBox)findViewById(R.id.setting_image_toggle);
        imageToggle.setChecked(SettingUtil.getIfShowImages());
        imageToggle.setOnCheckedChangeListener(onCheckedChangeListener);
        findViewById(R.id.setting_show_image_line).setOnClickListener(onClickListener);
    }

    // on click listener for all need
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.setting_show_image_line:
                    imageToggle.setChecked(!imageToggle.isChecked());
                    break;
            }
        }
    };

    // check changed listener
    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()){
                case R.id.setting_image_toggle:
                    SettingUtil.setIfShowImages(isChecked);
                    break;
            }
        }
    };
}
