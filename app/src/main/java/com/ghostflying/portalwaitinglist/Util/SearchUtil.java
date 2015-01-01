package com.ghostflying.portalwaitinglist.Util;

import com.ghostflying.portalwaitinglist.data.PortalDetail;

import java.util.ArrayList;

/**
 * Created by ghostflying on 1/1/15.
 */
public class SearchUtil {

    public static void searchByPortalName(
            ArrayList<PortalDetail> allPortals,
            ArrayList<PortalDetail> resultPortals,
            String queryText){
        resultPortals.clear();
        if (queryText.equals("")){
            MailProcessUtil.getInstance().filterAndSort(
                    SettingUtil.TypeFilterMethod.ALL,
                    SettingUtil.ResultFilterMethod.EVERYTHING,
                    SettingUtil.SortOrder.LAST_DATE_DESC,
                    allPortals,
                    resultPortals
            );
        }
        else {
            for (PortalDetail eachPortal : allPortals){
                if (eachPortal.getName().contains(queryText))
                    resultPortals.add(eachPortal);
            }
        }
    }
}
