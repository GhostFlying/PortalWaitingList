package com.ghostflying.portalwaitinglist;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.ghostflying.portalwaitinglist.Util.SettingUtil;
import com.ghostflying.portalwaitinglist.fragment.BaseAlertDialogFragment;
import com.ghostflying.portalwaitinglist.fragment.DaysPickerDialogFragment;


public class SettingActivity extends ActionBarActivity
        implements BaseAlertDialogFragment.OnFragmentInteractionListener{
    public static final int REQUEST_SETTING = 1;
    CheckBox imageToggle;
    CheckBox inverseToggle;
    CheckBox forceChineseToggle;
    CheckBox showStatusInListToggle;
    TextView shortTimeValue;
    TextView longTimeValue;
    TextView smartOrderSortValue;

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

        // show status in list
        showStatusInListToggle = (CheckBox)findViewById(R.id.setting_show_status_in_list_toggle);
        showStatusInListToggle.setChecked(SettingUtil.getShowStatusInList());
        showStatusInListToggle.setOnCheckedChangeListener(onCheckedChangeListener);
        findViewById(R.id.setting_show_status_in_list_line).setOnClickListener(onClickListener);

        // default sort
        inverseToggle = (CheckBox)findViewById(R.id.setting_smart_order_inverse_toggle);
        inverseToggle.setChecked(SettingUtil.getIfInverseWaitingInSmart());
        inverseToggle.setOnCheckedChangeListener(onCheckedChangeListener);
        updateSmartOrderSortValue();
        findViewById(R.id.setting_inverse_sort_line).setOnClickListener(onClickListener);

        // short time setting
        findViewById(R.id.setting_short_time_line).setOnClickListener(onClickListener);
        updateShortTimeValue();

        // long time setting
        findViewById(R.id.setting_long_time_line).setOnClickListener(onClickListener);
        updateLongTimeValue();

        // other sort
        findViewById(R.id.setting_force_chinese_line).setOnClickListener(onClickListener);
        forceChineseToggle = (CheckBox)findViewById(R.id.setting_force_chinese_toggle);
        forceChineseToggle.setChecked(SettingUtil.getForceChinese());
        forceChineseToggle.setOnCheckedChangeListener(onCheckedChangeListener);

        // version code
        try{
            ((TextView)findViewById(R.id.version_name)).setText(
                    getResources().getString(R.string.setting_version) +
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        }
        catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
    }

    private void updateShortTimeValue(){
        if (shortTimeValue == null)
            shortTimeValue = (TextView)findViewById(R.id.setting_smart_order_short_time_value);
        shortTimeValue.setText(Integer.toString(SettingUtil.getShortTime()));
    }

    private void updateLongTimeValue(){
        if (longTimeValue == null)
            longTimeValue = (TextView)findViewById(R.id.setting_smart_order_long_time_value);
        longTimeValue.setText(Integer.toString(SettingUtil.getLongTime()));
    }

    private void updateSmartOrderSortValue(){
        if (smartOrderSortValue == null)
            smartOrderSortValue = (TextView)findViewById(R.id.setting_smart_order_sort_value);
        if (SettingUtil.getIfInverseWaitingInSmart())
            smartOrderSortValue.setText(R.string.last_desc_order);
        else
            smartOrderSortValue.setText(R.string.last_asc_order);
    }

    // on click listener for all need
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.setting_show_image_line:
                    imageToggle.setChecked(!imageToggle.isChecked());
                    break;
                case R.id.setting_short_time_line:
                    DaysPickerDialogFragment shortFragment =
                            DaysPickerDialogFragment
                                    .newInstance(
                                            SettingUtil.getShortTime(),
                                            R.integer.setting_short_time_min,
                                            R.integer.setting_short_time_max,
                                            R.string.setting_short_time_dialog_title
                                    );
                    shortFragment.show(getFragmentManager(), null);
                    break;
                case R.id.setting_long_time_line:
                    DaysPickerDialogFragment longFragment =
                            DaysPickerDialogFragment
                                    .newInstance(
                                            SettingUtil.getLongTime(),
                                            R.integer.setting_long_time_min,
                                            R.integer.setting_long_time_max,
                                            R.string.setting_long_time_dialog_title
                                    );
                    longFragment.show(getFragmentManager(), null);
                    break;
                case R.id.setting_inverse_sort_line:
                    inverseToggle.setChecked(!inverseToggle.isChecked());
                    break;
                case R.id.setting_force_chinese_line:
                    forceChineseToggle.setChecked(!forceChineseToggle.isChecked());
                    break;
                case R.id.setting_show_status_in_list_line:
                    showStatusInListToggle.setChecked(!showStatusInListToggle.isChecked());
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
                case R.id.setting_smart_order_inverse_toggle:
                    SettingUtil.setIfInverseWaitingInSmart(isChecked);
                    updateSmartOrderSortValue();
                    setNeedFilterOrSort();
                    break;
                case R.id.setting_force_chinese_toggle:
                    SettingUtil.setForceChinese(isChecked);
                    setNeedFilterOrSort();
                    break;
                case R.id.setting_show_status_in_list_toggle:
                    SettingUtil.setShowStatusInList(isChecked);
                    setNeedFilterOrSort();
                    break;
            }
        }
    };

    @Override
    public void onPositiveButtonClick(int value, int title) {
        switch (title){
            case R.string.setting_short_time_dialog_title:
                SettingUtil.setShortTime(value);
                updateShortTimeValue();
                setNeedFilterOrSort();
                break;
            case R.string.setting_long_time_dialog_title:
                SettingUtil.setLongTime(value);
                updateLongTimeValue();
                setNeedFilterOrSort();
                break;
        }
    }

    private void setNeedFilterOrSort() {
        // set result to filter or sort date in main activity
        setResult(RESULT_OK);
    }

    @Override
    public void onNegativeButtonClick(int value, int title) {

    }
}
