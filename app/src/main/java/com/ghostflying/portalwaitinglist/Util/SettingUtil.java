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
    static final String FILTER_METHOD_NAME = "FilterMethod";
    static final String ACCOUNT_NAME = "account";
    static final String IF_SHOW_IMAGES = "IfShowImages";
    static final String READ_FIRST_EXCEPTION = "You must read all settings first.";
    static final boolean DEFAULT_IF_SHOW_IMAGES = true;
    private static SharedPreferences options;
    private static FilterMethod filterMethod;
    private static SortOrder sortOrder;
    private static String account;
    private static boolean ifShowImages;
    private static boolean isModified = false;

    /**
     * Read all settings from storage, and store them in the mem.
     * @param context   the context.
     */
    public static void readAllSettings(Context context){
        createSharedPreferences(context);
        if (!isModified){
            account = options.getString(ACCOUNT_NAME, null);
            filterMethod = FilterMethod.values()[options.getInt(FILTER_METHOD_NAME,
                    FilterMethod.EVERYTHING.ordinal())];
            sortOrder = SortOrder.values()[options.getInt(SORT_ORDER_NAME,
                    SortOrder.SMART_ORDER.ordinal())];
            ifShowImages = options.getBoolean(IF_SHOW_IMAGES, DEFAULT_IF_SHOW_IMAGES);
            isModified = false;
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
            editor.putInt(FILTER_METHOD_NAME, filterMethod.ordinal());
            editor.putInt(SORT_ORDER_NAME, sortOrder.ordinal());
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
    public static FilterMethod getFilterMethod(){
        checkRead();
        return filterMethod;
    }

    /**
     * Set the setting filterMethod.
     * @param filterMethod  filterMethod to be set.
     */
    public static void setFilterMethod(FilterMethod filterMethod){
        checkRead();
        isModified = true;
        SettingUtil.filterMethod = filterMethod;
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
     * Set the setting ifShowImages
     * @param ifShowImages  the ifShowImages to set.
     */
    public static void setIfShowImages(boolean ifShowImages){
        checkRead();
        isModified = true;
        SettingUtil.ifShowImages = ifShowImages;
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
        DATE_ASC, DATE_DESC, SMART_ORDER
    }

    public static enum FilterMethod{
        EVERYTHING, ACCEPTED, REJECTED, WAITING
    }
}
