package com.ghostflying.portalwaitinglist.util;

import com.ghostflying.portalwaitinglist.model.PortalDetail;

import java.util.List;

/**
 * Created by ghostflying on 1/1/15.
 */
public class SearchUtil {

    public static void searchByPortalName(
            List<PortalDetail> allPortals,
            List<PortalDetail> resultPortals,
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
