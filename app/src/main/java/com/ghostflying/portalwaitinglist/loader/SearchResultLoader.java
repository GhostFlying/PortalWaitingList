package com.ghostflying.portalwaitinglist.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.ghostflying.portalwaitinglist.dao.datahelper.PortalEventHelper;
import com.ghostflying.portalwaitinglist.model.PortalDetail;
import com.ghostflying.portalwaitinglist.model.PortalEvent;
import com.ghostflying.portalwaitinglist.util.MailProcessUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ghostflying on 1/25/15.
 */
public class SearchResultLoader extends AsyncTaskLoader<PortalDetail> {
    String messageId;
    PortalDetail mDetail;

    public SearchResultLoader(Context context, String messageId){
        super(context);
        this.messageId = messageId;
    }

    @Override
    public PortalDetail loadInBackground() {
        PortalEventHelper mHelper = new PortalEventHelper(getContext());
        MailProcessUtil mUtil = MailProcessUtil.getInstance();
        List<PortalEvent> events = mHelper.getEventsByName(messageId);
        // if no matched event, return null.
        if (events.isEmpty())
            return null;
        List<PortalDetail> portals = new ArrayList<>();
        mUtil.mergeEvents(portals, events);
        return portals.get(0);
    }

    @Override
    public void deliverResult(PortalDetail detail){
        mDetail = detail;
        if (isStarted())
            super.deliverResult(detail);
    }

    @Override
    public void onStartLoading(){
        if (mDetail != null){
            deliverResult(mDetail);
        }

        if (takeContentChanged() || mDetail == null){
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset(){
        super.onReset();

        onStopLoading();
        mDetail = null;
    }
}
