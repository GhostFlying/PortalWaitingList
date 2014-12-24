package com.ghostflying.portalwaitinglist.Util;

import android.content.Context;
import android.content.SharedPreferences;

import com.ghostflying.portalwaitinglist.R;

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
    static final String READ_FIRST_EXCEPTION = "You must read all settings first.";
    static final boolean DEFAULT_IF_SHOW_IMAGES = true;
    static final int DEFAULT_SHORT_TIME = 7;
    static final int DEFAULT_LONG_TIME = 365;
    static final boolean DEFAULT_IF_INVERSE_WAITING_IN_SMART = false;
    static final boolean DEFAULT_FORCE_CHINESE = false;
    static final int DEFAULT_TYPE_FILTER_METHOD = 0;
    private static SharedPreferences options;
    private static ResultFilterMethod resultFilterMethod;
    private static SortOrder sortOrder;
    private static String account;
    private static boolean ifShowImages;
    private static int shortTime;
    private static int longTime;
    private static boolean ifInverseWaitingInSmart;
    private static boolean forceChinese;
    private static TypeFilterMethod typeFilterMethod;
    private static boolean isModified = false;

    /**
     * Read all settings from storage, and store them in the mem.
     * @param context   the context.
     */
    public static void readAllSettings(Context context){
        createSharedPreferences(context);
        if (!isModified){
            account = options.getString(ACCOUNT_NAME, null);
            resultFilterMethod = ResultFilterMethod.values()[options.getInt(RESULT_FILTER_METHOD_NAME,
                    ResultFilterMethod.EVERYTHING.ordinal())];
            sortOrder = SortOrder.values()[options.getInt(SORT_ORDER_NAME,
                    SortOrder.SMART_ORDER.ordinal())];
            ifShowImages = options.getBoolean(IF_SHOW_IMAGES_NAME, DEFAULT_IF_SHOW_IMAGES);
            shortTime = options.getInt(SHORT_TIME_NAME, DEFAULT_SHORT_TIME);
            longTime = options.getInt(LONG_TIME_NAME, DEFAULT_LONG_TIME);
            ifInverseWaitingInSmart = options.getBoolean(
                    IF_INVERSE_WAITING_IN_SMART_NAME, DEFAULT_IF_INVERSE_WAITING_IN_SMART);
            forceChinese = options.getBoolean(FORCE_CHINESE_NAME, DEFAULT_FORCE_CHINESE);
            typeFilterMethod = TypeFilterMethod.values()[
                    options.getInt(TYPE_FILTER_METHOD_NAME, DEFAULT_TYPE_FILTER_METHOD)];
        }
    }

    /**
     * Save all settings to storage.
     */
    public static void saveAllSettings(){
        checkRead();
        // avoid to override exist settings.
        if (isModified){
            SharedPreferences.Editor editor = options.edit();
            editor.putString(ACCOUNT_NAME, account);
            editor.putInt(RESULT_FILTER_METHOD_NAME, resultFilterMethod.ordinal());
            editor.putInt(SORT_ORDER_NAME, sortOrder.ordinal());
            editor.putBoolean(IF_SHOW_IMAGES_NAME, ifShowImages);
            editor.putInt(SHORT_TIME_NAME, shortTime);
            editor.putInt(LONG_TIME_NAME, longTime);
            editor.putBoolean(IF_INVERSE_WAITING_IN_SMART_NAME, ifInverseWaitingInSmart);
            editor.putBoolean(FORCE_CHINESE_NAME, forceChinese);
            editor.putInt(TYPE_FILTER_METHOD_NAME, typeFilterMethod.ordinal());
            editor.apply();
        }
    }

    /**
     * Get the setting account.
     * @return  account.
     */
    public static String getAccount(){
        checkRead();
        return account;
    }

    /**
     * Set the setting account.
     * @param account   the account to set.
     */
    public static void setAccount(String account){
        checkRead();
        isModified = true;
        SettingUtil.account = account;
    }

    /**
     * Get the setting filterMethod.
     * @return  filterMethod.
     */
    public static ResultFilterMethod getResultFilterMethod(){
        checkRead();
        return resultFilterMethod;
    }

    /**
     * Set the setting filterMethod.
     * @param resultFilterMethod  filterMethod to be set.
     */
    public static void setResultFilterMethod(ResultFilterMethod resultFilterMethod){
        checkRead();
        isModified = true;
        SettingUtil.resultFilterMethod = resultFilterMethod;
    }

    /**
     * Get the setting sortOrder.
     * @return  sortOrder.
     */
    public static SortOrder getSortOrder(){
        checkRead();
        return sortOrder;
    }

    /**
     * Set the setting sortOrder.
     * @param sortOrder the sortOrder to set.
     */
    public static void setSortOrder(SortOrder sortOrder){
        checkRead();
        isModified = true;
        SettingUtil.sortOrder = sortOrder;
    }

    /**
     * Get the setting ifShowImages.
     * @return  true if the images should be showed, otherwise false.
     */
    public static Boolean getIfShowImages(){
        checkRead();
        return ifShowImages;
    }

    /**
     * Set the setting ifShowImagesName
     * @param ifShowImages  the ifShowImagesName to set.
     */
    public static void setIfShowImages(boolean ifShowImages){
        checkRead();
        isModified = true;
        SettingUtil.ifShowImages = ifShowImages;
    }

    /**
     * Get the setting short time.
     * @return  the short time saved.
     */
    public static int getShortTime(){
        checkRead();
        return shortTime;
    }

    /**
     * Set the setting short time.
     * @param shortTime the short time to set.
     */
    public static void setShortTime(int shortTime){
        checkRead();
        isModified = true;
        SettingUtil.shortTime = shortTime;
    }

    /**
     * Get the setting long time.
     * @return  the long time saved.
     */
    public static int getLongTime(){
        checkRead();
        return longTime;
    }

    /**
     * Set the setting long time.
     * @param longTime  the long time to set.
     */
    public static void setLongTime(int longTime){
        checkRead();
        isModified = true;
        SettingUtil.longTime = longTime;
    }

    /**
     * Get the setting if inverse waiting list in smart order.
     * @return  true if inverse, otherwise false.
     */
    public static boolean getIfInverseWaitingInSmart(){
        checkRead();
        return ifInverseWaitingInSmart;
    }

    /**
     * Set the setting if inverse waiting list in smart order.
     * @param ifInverseWaitingInSmart   the value to set.
     */
    public static void setIfInverseWaitingInSmart(boolean ifInverseWaitingInSmart){
        checkRead();
        isModified = true;
        SettingUtil.ifInverseWaitingInSmart = ifInverseWaitingInSmart;
    }

    /**
     * Get the setting if treat all names of portals as Chinese..
     * @return  true if treat as Chinese, otherwise false.
     */
    public static boolean getForceChinese(){
        checkRead();
        return forceChinese;
    }

    /**
     * Set the setting if treat all names of portals as Chinese..
     * @param forceChinese   the value to set.
     */
    public static void setForceChinese(boolean forceChinese){
        checkRead();
        isModified = true;
        SettingUtil.forceChinese = forceChinese;
    }

    /**
     * Get the setting type filter method.
     * @return  the type filter method.
     */
    public static TypeFilterMethod getTypeFilterMethod(){
        checkRead();
        return typeFilterMethod;
    }

    /**
     * Set the setting type filter method.
     * @param typeFilterMethod  the method set to filter by type.
     */
    public static void setTypeFilterMethod(TypeFilterMethod typeFilterMethod){
        checkRead();
        isModified = true;
        SettingUtil.typeFilterMethod = typeFilterMethod;
    }

    /**
     * Check if setting is read first.
     */
    private static void checkRead(){
        if (options == null)
            throw new UnsupportedOperationException(READ_FIRST_EXCEPTION);
    }

    /**
     * Create the SharedPreferences.
     * @param context   the context.
     */
    private static void createSharedPreferences(Context context){
        if (options == null)
            options = context.getSharedPreferences(context.getString(R.string.preference_name), Context.MODE_PRIVATE);
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
