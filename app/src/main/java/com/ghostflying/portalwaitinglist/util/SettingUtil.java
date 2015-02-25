package com.ghostflying.portalwaitinglist.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.ghostflying.portalwaitinglist.R;

import java.util.Observer;

/**
 * Created by ghostflying on 11/22/14.
 * <br>
 * Class for settings get and set.
 */
public class SettingUtil {
    static final String SORT_ORDER_NAME = "SortOrder";
    static final String RESULT_FILTER_METHOD_NAME = "FilterMethod";
    static final String ACCOUNT_NAME = "account";
    static final String IF_SHOW_IMAGES_NAME = "IfShowImages";
    static final String SHORT_TIME_NAME = "ShortTime";
    static final String LONG_TIME_NAME = "LongTime";
    static final String IF_INVERSE_WAITING_IN_SMART_NAME = "IfInverseWaitingInSmart";
    static final String FORCE_CHINESE_NAME = "ForceChinese";
    static final String TYPE_FILTER_METHOD_NAME = "TypeFilterMethod";
    static final String SHOW_STATUS_IN_LIST_NAME = "ShowStatusInList";
    static final boolean DEFAULT_IF_SHOW_IMAGES = true;
    static final int DEFAULT_SHORT_TIME = 7;
    static final int DEFAULT_LONG_TIME = 365;
    static final boolean DEFAULT_IF_INVERSE_WAITING_IN_SMART = false;
    static final boolean DEFAULT_FORCE_CHINESE = false;
    static final int DEFAULT_TYPE_FILTER_METHOD = 0;
    static final boolean DEFAULT_SHOW_STATUS_IN_LIST = false;
    private static SharedPreferences options;
    private static Observer settingObserver;

    /**
     * Get shared Preferences.
     * @param context   the context.
     */
    public static void getSettings(Context context){
        createSharedPreferences(context);
    }

    /**
     * Get the setting account.
     * @return  account.
     */
    public static String getAccount(){
        return options.getString(ACCOUNT_NAME, null);
    }

    /**
     * Set the setting account.
     * @param account   the account to set.
     */
    public static void setAccount(String account){
        options.edit()
                .putString(ACCOUNT_NAME, account)
                .apply();
    }

    /**
     * Get the setting filterMethod.
     * @return  filterMethod.
     */
    public static ResultFilterMethod getResultFilterMethod(){
        return ResultFilterMethod.values()[
                options.getInt(RESULT_FILTER_METHOD_NAME,
                        ResultFilterMethod.EVERYTHING.ordinal())];
    }

    /**
     * Set the setting filterMethod.
     * @param resultFilterMethod  filterMethod to be set.
     */
    public static void setResultFilterMethod(ResultFilterMethod resultFilterMethod){
        if (resultFilterMethod != getResultFilterMethod()){
            options.edit().putInt(RESULT_FILTER_METHOD_NAME, resultFilterMethod.ordinal()).apply();
            notifyChange(resultFilterMethod);
        }
    }

    /**
     * Get the setting sortOrder.
     * @return  sortOrder.
     */
    public static SortOrder getSortOrder(){
        return SortOrder.values()[
                options.getInt(SORT_ORDER_NAME, SortOrder.SMART_ORDER.ordinal())];
    }

    /**
     * Set the setting sortOrder.
     * @param sortOrder the sortOrder to set.
     */
    public static void setSortOrder(SortOrder sortOrder){
        if (sortOrder != getSortOrder()){
            options.edit().putInt(SORT_ORDER_NAME, sortOrder.ordinal()).apply();
            notifyChange(sortOrder);
        }
    }

    /**
     * Get the setting ifShowImages.
     * @return  true if the images should be showed, otherwise false.
     */
    public static Boolean getIfShowImages(){
        return options.getBoolean(IF_SHOW_IMAGES_NAME, DEFAULT_IF_SHOW_IMAGES);
    }

    /**
     * Set the setting ifShowImagesName
     * @param ifShowImages  the ifShowImagesName to set.
     */
    public static void setIfShowImages(boolean ifShowImages){
        options.edit()
                .putBoolean(IF_SHOW_IMAGES_NAME, ifShowImages)
                .apply();
    }

    /**
     * Get the setting short time.
     * @return  the short time saved.
     */
    public static int getShortTime(){
        return options.getInt(SHORT_TIME_NAME, DEFAULT_SHORT_TIME);
    }

    /**
     * Set the setting short time.
     * @param shortTime the short time to set.
     */
    public static void setShortTime(int shortTime){
        options.edit()
                .putInt(SHORT_TIME_NAME, DEFAULT_SHORT_TIME)
                .apply();
    }

    /**
     * Get the setting long time.
     * @return  the long time saved.
     */
    public static int getLongTime(){
        return options.getInt(LONG_TIME_NAME, DEFAULT_LONG_TIME);
    }

    /**
     * Set the setting long time.
     * @param longTime  the long time to set.
     */
    public static void setLongTime(int longTime){
        options.edit()
                .putInt(LONG_TIME_NAME, longTime)
                .apply();
    }

    /**
     * Get the setting if inverse waiting list in smart order.
     * @return  true if inverse, otherwise false.
     */
    public static boolean getIfInverseWaitingInSmart(){
        return options.getBoolean(IF_INVERSE_WAITING_IN_SMART_NAME, DEFAULT_IF_INVERSE_WAITING_IN_SMART);
    }

    /**
     * Set the setting if inverse waiting list in smart order.
     * @param ifInverseWaitingInSmart   the value to set.
     */
    public static void setIfInverseWaitingInSmart(boolean ifInverseWaitingInSmart){
        options.edit()
                .putBoolean(IF_INVERSE_WAITING_IN_SMART_NAME, ifInverseWaitingInSmart)
                .apply();
    }

    /**
     * Get the setting if treat all names of portals as Chinese..
     * @return  true if treat as Chinese, otherwise false.
     */
    public static boolean getForceChinese(){
        return options.getBoolean(FORCE_CHINESE_NAME, DEFAULT_FORCE_CHINESE);
    }

    /**
     * Set the setting if treat all names of portals as Chinese..
     * @param forceChinese   the value to set.
     */
    public static void setForceChinese(boolean forceChinese){
        options.edit()
                .putBoolean(FORCE_CHINESE_NAME, forceChinese)
                .apply();
    }

    /**
     * Get the setting type filter method.
     * @return  the type filter method.
     */
    public static TypeFilterMethod getTypeFilterMethod(){
        return TypeFilterMethod.values()[
                options.getInt(TYPE_FILTER_METHOD_NAME, DEFAULT_TYPE_FILTER_METHOD)
        ];
    }

    /**
     * Set the setting type filter method.
     * @param typeFilterMethod  the method set to filter by type.
     */
    public static void setTypeFilterMethod(TypeFilterMethod typeFilterMethod){
        if (typeFilterMethod != getTypeFilterMethod()){
            options.edit()
                    .putInt(TYPE_FILTER_METHOD_NAME, typeFilterMethod.ordinal())
                    .apply();
            notifyChange(typeFilterMethod);
        }
    }

    /**
     * Get the setting if show status in list.
     * @return  the setting show status in list..
     */
    public static boolean getShowStatusInList(){
        return options.getBoolean(SHOW_STATUS_IN_LIST_NAME, DEFAULT_SHOW_STATUS_IN_LIST);
    }

    /**
     * Set the setting show status in list.
     * @param showStatusInList  the value to set.
     */
    public static void setShowStatusInList(boolean showStatusInList){
        options.edit()
                .putBoolean(SHOW_STATUS_IN_LIST_NAME, showStatusInList)
                .apply();
    }

    /**
     * Create the SharedPreferences.
     * @param context   the context.
     */
    private static void createSharedPreferences(Context context){
        if (options == null)
            options = context.getSharedPreferences(context.getString(R.string.preference_name), Context.MODE_PRIVATE);
    }

    public static void registerObserver(Observer observer){
        settingObserver = observer;
    }

    public static void unregisterObserver(){
        settingObserver = null;
    }

    private static void notifyChange(Object data){
        if (settingObserver != null)
            settingObserver.update(null, data);
    }

    /**
     * Stay for debug.
     * @param context   the context.
     */
    public static void clearSetting(Context context){
        createSharedPreferences(context);
        options.edit().clear().commit();
    }

    public static enum SortOrder{
        LAST_DATE_ASC,
        LAST_DATE_DESC,
        SMART_ORDER,
        ALPHABETICAL,
        PROPOSED_DATE_ASC,
        PROPOSED_DATE_DESC
    }

    public static enum ResultFilterMethod {
        EVERYTHING, ACCEPTED, REJECTED, WAITING
    }

    public static enum TypeFilterMethod {
        ALL, SUBMISSION, EDIT
    }
}
