package com.ghostflying.portalwaitinglist.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;

import com.ghostflying.portalwaitinglist.dao.datahelper.PortalEventHelper;
import com.ghostflying.portalwaitinglist.model.PortalDetail;
import com.ghostflying.portalwaitinglist.model.PortalEvent;
import com.ghostflying.portalwaitinglist.util.MailProcessUtil;
import com.ghostflying.portalwaitinglist.util.SettingUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ghostflying on 1/20/15.
 */
public class PortalListLoader extends AsyncTaskLoader<PortalListLoader.PortalListViewModel> {
    private long lastEventDate = 0;
    private List<PortalDetail> totalPortals;
    private PortalListViewModel mViewModel;
    private Action mAction;
    private PortalEventHelper mHelper;
    private ContentObserver mContentObserver;

    public PortalListLoader(Context context){
        super(context);
        totalPortals = new ArrayList<>();
        mAction = Action.GET_DATA;
        mContentObserver = new PartUpdateContentObserver();
    }

    @Override
    public PortalListViewModel loadInBackground() {
        if (mViewModel == null)
            mViewModel = new PortalListViewModel();
        switch (mAction){
            case SORT:
                doSort();
                break;
            case FILTER:
                doFilter();
                break;
            case GET_DATA:
            default:
                doGetData();
                break;
        }
        return mViewModel;
    }

    private void doGetData(){
        mHelper = new PortalEventHelper(getContext());
        MailProcessUtil mProcessUtil = MailProcessUtil.getInstance();
        Cursor mCursor = mHelper.getAll(lastEventDate);
        try {
            mHelper.setContentObserver(mContentObserver);
            List<PortalEvent> portalEvents = mHelper.fromCursor(mCursor);
            if (portalEvents.size() != 0){
                lastEventDate = portalEvents.get(portalEvents.size() - 1).getDate().getTime();
                mProcessUtil.mergeEvents(totalPortals, portalEvents);
            }
        }
        finally {
            mCursor.close();
        }
        mViewModel.counts = mProcessUtil.getCounts(totalPortals);
        doFilter();
        mViewModel.mAction = Action.GET_DATA;
    }

    private void doFilter(){
        MailProcessUtil mProcessUtil = MailProcessUtil.getInstance();
        mProcessUtil.filterAndSort(
                SettingUtil.getTypeFilterMethod(),
                SettingUtil.getResultFilterMethod(),
                SettingUtil.getSortOrder(),
                totalPortals,
                mViewModel.mPortals
        );
        mViewModel.mAction = Action.FILTER;
    }

    private void doSort(){
        MailProcessUtil mProcessUtil = MailProcessUtil.getInstance();
        mProcessUtil.sortPortalDetails(
                SettingUtil.getSortOrder(),
                totalPortals
        );
        mViewModel.mAction = Action.SORT;
    }

    @Override
    public void deliverResult(PortalListViewModel viewModel){
        if (isStarted()){
            super.deliverResult(viewModel);
        }
    }

    @Override
    protected void onStartLoading(){
        if (mViewModel != null){
            deliverResult(mViewModel);
        }

        if (takeContentChanged() || mViewModel == null){
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading(){
        cancelLoad();
        if (mHelper != null)
            mHelper.unregisterContentObserver(mContentObserver);
    }

    @Override
    protected void onReset(){
        super.onReset();

        onStopLoading();
        mViewModel = null;
    }

    public enum Action{
        GET_DATA, FILTER, SORT
    }

    public class PortalListViewModel{
        public List<PortalDetail> mPortals;
        public int[] counts;
        public Action mAction;

        public PortalListViewModel(){
            mPortals = new ArrayList<>();
        }
    }

    public class PartUpdateContentObserver extends ContentObserver{

        public PartUpdateContentObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            mAction = Action.GET_DATA;
            onContentChanged();
        }
    }
}
