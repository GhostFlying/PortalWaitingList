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
import java.util.Observable;
import java.util.Observer;

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
    private Observer mSettingObserver;

    public PortalListLoader(Context context){
        super(context);
        totalPortals = new ArrayList<>();
        mAction = Action.GET_DATA;
        mContentObserver = new PartUpdateContentObserver();
        mSettingObserver = new SettingObserver();
    }

    @Override
    public PortalListViewModel loadInBackground() {
        PortalListViewModel viewModel = new PortalListViewModel();
        if (mViewModel != null)
            viewModel.counts = mViewModel.counts;
        switch (mAction){
            case FILTER:
                doFilter(viewModel);
                break;
            case GET_DATA:
                doGetData(viewModel);
                break;
            case SORT:
            default:
                doSort(viewModel);
                break;
        }
        return viewModel;
    }

    private void doGetData(PortalListViewModel viewModel){
        if (mHelper == null)
            mHelper = new PortalEventHelper(getContext());
        MailProcessUtil mProcessUtil = MailProcessUtil.getInstance();
        Cursor mCursor = mHelper.getAll(lastEventDate);
        try {
            List<PortalEvent> portalEvents = mHelper.fromCursor(mCursor);
            if (portalEvents.size() != 0){
                lastEventDate = portalEvents.get(portalEvents.size() - 1).getDate().getTime();
                mProcessUtil.mergeEvents(totalPortals, portalEvents);
            }
        }
        finally {
            mCursor.close();
        }
        viewModel.counts = mProcessUtil.getCounts(totalPortals);
        doFilter(viewModel);
        viewModel.mAction = Action.GET_DATA;
    }

    private void doFilter(PortalListViewModel viewModel){
        MailProcessUtil mProcessUtil = MailProcessUtil.getInstance();
        mProcessUtil.filterAndSort(
                SettingUtil.getTypeFilterMethod(),
                SettingUtil.getResultFilterMethod(),
                SettingUtil.getSortOrder(),
                totalPortals,
                viewModel.mPortals
        );
        viewModel.mAction = Action.FILTER;
    }

    private void doSort(PortalListViewModel viewModel){
        MailProcessUtil mProcessUtil = MailProcessUtil.getInstance();
        viewModel.mPortals = mViewModel.mPortals;
        mProcessUtil.sortPortalDetails(
                SettingUtil.getSortOrder(),
                viewModel.mPortals
        );
        viewModel.mAction = Action.SORT;
    }

    @Override
    public void deliverResult(PortalListViewModel viewModel){
        mViewModel = viewModel;
        if (isStarted()){
            super.deliverResult(viewModel);
        }
    }

    @Override
    protected void onStartLoading(){
        if (mViewModel != null){
            deliverResult(mViewModel);
        }

        SettingUtil.registerObserver(mSettingObserver);
        mHelper = new PortalEventHelper(getContext());
        mHelper.setContentObserver(mContentObserver);

        if (takeContentChanged() || mViewModel == null){
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading(){
        cancelLoad();
        SettingUtil.unregisterObserver();
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

    public class SettingObserver implements Observer{

        @Override
        public void update(Observable observable, Object data) {
            if (data instanceof SettingUtil.SortOrder){
                mAction = Action.SORT;
            }
            else {
                mAction = Action.FILTER;
            }
            onContentChanged();
        }
    }
}
