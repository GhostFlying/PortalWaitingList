package com.ghostflying.portalwaitinglist.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ghostflying.portalwaitinglist.AuthActivity;
import com.ghostflying.portalwaitinglist.MyApp;
import com.ghostflying.portalwaitinglist.PortalEventContract;
import com.ghostflying.portalwaitinglist.PortalEventDbHelper;
import com.ghostflying.portalwaitinglist.PortalListAdapter;
import com.ghostflying.portalwaitinglist.R;
import com.ghostflying.portalwaitinglist.Util.GMailServiceUtil;
import com.ghostflying.portalwaitinglist.Util.MailProcessUtil;
import com.ghostflying.portalwaitinglist.Util.SettingUtil;
import com.ghostflying.portalwaitinglist.data.EditEvent;
import com.ghostflying.portalwaitinglist.data.Message;
import com.ghostflying.portalwaitinglist.data.PortalDetail;
import com.ghostflying.portalwaitinglist.data.PortalEvent;
import com.ghostflying.portalwaitinglist.data.SubmissionEvent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;

import retrofit.RetrofitError;


public class PortalListFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    Toolbar toolbar;
    String token;
    String account;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    PortalEventDbHelper dbHelper;
    DrawerLayout drawerLayout;
    ArrayList<PortalDetail> totalPortalDetails;
    TextView countEverything;
    TextView countAccepted;
    TextView countRejected;
    TextView countWaiting;
    boolean isInitialed = false;

    public static PortalListFragment newInstance() {
        PortalListFragment fragment = new PortalListFragment();
        return fragment;
    }

    public PortalListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_portal_list, container, false);
        // Initial some members
        account = SettingUtil.getAccount();
        dbHelper = new PortalEventDbHelper(getActivity());
        totalPortalDetails = new ArrayList<>();
        setDrawerLayout(view);
        setRecyclerView(view);
        setSwipeRefreshLayout(view);
        setToolbar(view);
        setHasOptionsMenu(true);
        // read stored data.
        new InitialTask().execute();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_filter) {
            drawerLayout.openDrawer(Gravity.RIGHT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onHiddenChanged(boolean hidden){
        super.onHiddenChanged(hidden);
        if (!hidden){
            ((ActionBarActivity)getActivity()).setSupportActionBar(toolbar);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }
    }

    private void setDrawerLayout(View v){
        drawerLayout = (DrawerLayout)v.findViewById(R.id.drawer_layout);

        v.findViewById(R.id.item_everything).setOnClickListener(sortAndFilterClickListener);
        v.findViewById(R.id.item_accepted).setOnClickListener(sortAndFilterClickListener);
        v.findViewById(R.id.item_rejected).setOnClickListener(sortAndFilterClickListener);
        v.findViewById(R.id.item_waiting).setOnClickListener(sortAndFilterClickListener);
        v.findViewById(R.id.item_smart_order).setOnClickListener(sortAndFilterClickListener);
        v.findViewById(R.id.item_asc_order).setOnClickListener(sortAndFilterClickListener);
        v.findViewById(R.id.item_desc_order).setOnClickListener(sortAndFilterClickListener);
        countEverything = (TextView)v.findViewById(R.id.count_everything);
        countAccepted = (TextView)v.findViewById(R.id.count_accepted);
        countRejected = (TextView)v.findViewById(R.id.count_rejected);
        countWaiting = (TextView)v.findViewById(R.id.count_waiting);
    }

    View.OnClickListener sortAndFilterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.item_everything:
                    SettingUtil.setFilterMethod(SettingUtil.FilterMethod.EVERYTHING);
                    break;
                case R.id.item_accepted:
                    SettingUtil.setFilterMethod(SettingUtil.FilterMethod.ACCEPTED);
                    break;
                case R.id.item_rejected:
                    SettingUtil.setFilterMethod(SettingUtil.FilterMethod.REJECTED);
                    break;
                case R.id.item_waiting:
                    SettingUtil.setFilterMethod(SettingUtil.FilterMethod.WAITING);
                    break;
                case R.id.item_smart_order:
                    SettingUtil.setSortOrder(SettingUtil.SortOrder.SMART_ORDER);
                    break;
                case R.id.item_asc_order:
                    SettingUtil.setSortOrder(SettingUtil.SortOrder.DATE_ASC);
                    break;
                case R.id.item_desc_order:
                    SettingUtil.setSortOrder(SettingUtil.SortOrder.DATE_DESC);
                    break;
                default:
            }
            new SortAndFilterTask().execute();
            drawerLayout.closeDrawer(Gravity.RIGHT);
        }
    };

    private void setToolbar(View v){
        toolbar = (Toolbar)v.findViewById(R.id.action_bar_in_list);
        setTitleBySetting(SettingUtil.getFilterMethod());
        ((ActionBarActivity)getActivity()).setSupportActionBar(toolbar);
    }

    private void setTitleBySetting(SettingUtil.FilterMethod filterMethod){
        switch (filterMethod){
            case EVERYTHING:
                toolbar.setTitle(R.string.everything);
                break;
            case ACCEPTED:
                toolbar.setTitle(R.string.accepted);
                break;
            case REJECTED:
                toolbar.setTitle(R.string.rejected);
                break;
            case WAITING:
                toolbar.setTitle(R.string.waiting);
                break;
        }
    }

    private void setSwipeRefreshLayout(View v){
        swipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_circle_color_one,
                R.color.refresh_circle_color_two,
                R.color.refresh_circle_color_three);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // execute the refresh task to query from GMail
                new RefreshTask().execute();
            }
        });
    }

    private void setRecyclerView(View v){
        recyclerView = (RecyclerView)v.findViewById(R.id.portal_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        final PortalListAdapter adapter = new PortalListAdapter(new ArrayList<PortalDetail>());
        adapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PortalDetail clickedPortal = adapter.dataSet.get(recyclerView.getChildPosition(v));
                mListener.portalItemClicked(clickedPortal);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    /**
     * The initial task once the activity start.
     */
    private class InitialTask extends AsyncTask<Void, Void, Void>{
        int[] counts;

        @Override
        protected Void doInBackground(Void... params) {
            // if no account is set, stop initial.
            if (account == null)
                return null;
            ArrayList<PortalEvent> portalEvents = new ArrayList<PortalEvent>();
            // query from SQLite
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            Cursor cursor = database.query(
                    PortalEventContract.PortalEvent.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    PortalEventContract.PortalEvent.COLUMN_NAME_DATE + " ASC"
            );
            int portalNameIndex = cursor.getColumnIndex(PortalEventContract.PortalEvent.COLUMN_NAME_PORTAL_NAME);
            int operationTypeIndex = cursor.getColumnIndex(PortalEventContract.PortalEvent.COLUMN_NAME_OPERATION_TYPE);
            int operationResultIndex = cursor.getColumnIndex(PortalEventContract.PortalEvent.COLUMN_NAME_OPERATION_RESULT);
            int messageIdIndex = cursor.getColumnIndex(PortalEventContract.PortalEvent.COLUMN_NAME_MESSAGE_ID);
            int dateIndex = cursor.getColumnIndex(PortalEventContract.PortalEvent.COLUMN_NAME_DATE);
            int imageUrlIndex = cursor.getColumnIndex(PortalEventContract.PortalEvent.COLUMN_NAME_IMAGE_URL);
            int addressIndex = cursor.getColumnIndex(PortalEventContract.PortalEvent.COLUMN_NAME_ADDRESS);
            int addressUrlIndex = cursor.getColumnIndex(PortalEventContract.PortalEvent.COLUMN_NAME_ADDRESS_URL);
            while(cursor.moveToNext()){
                PortalEvent event;
                String name = cursor.getString(portalNameIndex);
                PortalEvent.OperationType operationType = PortalEvent.OperationType.values()[cursor.getInt(operationTypeIndex)];
                PortalEvent.OperationResult operationResult = PortalEvent.OperationResult.values()[cursor.getInt(operationResultIndex)];
                Date date = new Date(cursor.getLong(dateIndex));
                String messageId = cursor.getString(messageIdIndex);
                if (operationType == PortalEvent.OperationType.SUBMISSION){
                    String imageUrl = cursor.getString(imageUrlIndex);
                    if (operationResult == PortalEvent.OperationResult.ACCEPTED){
                        String address = cursor.getString(addressIndex);
                        String addressUrl = cursor.getString(addressUrlIndex);
                        event = new SubmissionEvent(name,
                                operationResult,
                                date,
                                messageId,
                                imageUrl,
                                address,
                                addressUrl);
                    }
                    else {
                        event = new SubmissionEvent(name, operationResult, date, messageId, imageUrl);
                    }
                }
                else {
                    if (operationResult != PortalEvent.OperationResult.PROPOSED){
                        String address = cursor.getString(addressIndex);
                        String addressUrl = cursor.getString(addressUrlIndex);
                        event = new EditEvent(name, operationResult, date, messageId, address, addressUrl);
                    }
                    else {
                        event = new EditEvent(name, operationResult, date, messageId);
                    }
                }
                portalEvents.add(event);
            }
            cursor.close();
            database.close();
            // merge stored events to empty portal detail.
            MailProcessUtil processUtil = MailProcessUtil.getInstance();
            processUtil.mergeEvents(totalPortalDetails, portalEvents);
            //update counts
            counts = processUtil.getCounts(totalPortalDetails);
            // sort and filter the portal details
            processUtil.filterAndSort(
                    SettingUtil.getFilterMethod(),
                    SettingUtil.getSortOrder(),
                    totalPortalDetails,
                    ((PortalListAdapter) recyclerView.getAdapter()).dataSet
            );
            return null;
        }

        @Override
        protected void onPostExecute(Void param){
            // update UI.
            recyclerView.getAdapter().notifyDataSetChanged();
            updateCountText(counts);
            // set the flag, avoid refresh task run before initial done.
            isInitialed = true;

            if (account != null && totalPortalDetails.size() == 0){
                showToast(R.string.alert_to_refresh);
                swipeRefreshLayout.setRefreshing(true);
                new RefreshTask().execute();
            }
        }
    }

    private void showToast(final int resId){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(), resId, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * The refresh task to run once user calls to refresh data.
     */
    private class RefreshTask extends AsyncTask<Void, Void, Void>{
        int[] counts;

        @Override
        protected Void doInBackground(Void...params) {
            // wait until initial done.
            while (!isInitialed);
            try{
                token = GoogleAuthUtil.getToken(getActivity().getApplicationContext(), account, AuthActivity.SCOPE);
            }catch (Exception e){
                handleException(e);
                return null;
            }
            // Initial Utils
            GMailServiceUtil fetchUtil = GMailServiceUtil.getInstance(token);
            MailProcessUtil processUtil = MailProcessUtil.getInstance();
            // query and convert all new messages.
            ArrayList<Message> newMessages;
            try{
                newMessages = fetchUtil.getPortalMessages(dbHelper);
            }
            catch (Exception e){
                handleException(e);
                return null;
            }
            ArrayList<PortalEvent> newEvents = MailProcessUtil.getInstance().analysisMessages(newMessages);

            // write new messages to db.
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values;
            for (PortalEvent event : newEvents){
                values = new ContentValues();
                values.put(PortalEventContract.PortalEvent.COLUMN_NAME_PORTAL_NAME, event.getPortalName());
                values.put(PortalEventContract.PortalEvent.COLUMN_NAME_OPERATION_TYPE, event.getOperationType().ordinal());
                values.put(PortalEventContract.PortalEvent.COLUMN_NAME_OPERATION_RESULT, event.getOperationResult().ordinal());
                values.put(PortalEventContract.PortalEvent.COLUMN_NAME_DATE, event.getDate().getTime());
                values.put(PortalEventContract.PortalEvent.COLUMN_NAME_MESSAGE_ID, event.getMessageId());
                if (event instanceof SubmissionEvent && event.getOperationResult() != PortalEvent.OperationResult.ACCEPTED) {
                    values.put(PortalEventContract.PortalEvent.COLUMN_NAME_IMAGE_URL, ((SubmissionEvent) event).getPortalImageUrl());
                }
                else {
                    if (event.getOperationResult() != PortalEvent.OperationResult.PROPOSED){
                        values.put(PortalEventContract.PortalEvent.COLUMN_NAME_ADDRESS, event.getPortalAddress());
                        values.put(PortalEventContract.PortalEvent.COLUMN_NAME_ADDRESS_URL, event.getPortalAddressUrl());
                    }
                }
                db.insert(
                        PortalEventContract.PortalEvent.TABLE_NAME,
                        null,
                        values);
            }
            db.close();
            // merge new events to exist portal details
            processUtil.mergeEvents(totalPortalDetails, newEvents);
            // update counts
            counts = processUtil.getCounts(totalPortalDetails);
            // sort and filter the portal details
            processUtil.filterAndSort(
                    SettingUtil.getFilterMethod(),
                    SettingUtil.getSortOrder(),
                    totalPortalDetails,
                    ((PortalListAdapter) recyclerView.getAdapter()).dataSet
            );
            return null;
        }

        /**
         * Handle the exception during the refresh.
         * @param e the exception.
         */
        private void handleException(final Exception e){
            e.printStackTrace();
            Tracker t = ((MyApp)getActivity().getApplication()).getTracker();
            t.send(new HitBuilders.ExceptionBuilder()
                .setDescription(Log.getStackTraceString(e))
                .setFatal(true)
                .build()
            );
            if (e instanceof GoogleAuthException) {
                showToast(R.string.auth_error);
                mListener.doAuthInActivity();
            } else if (e instanceof IOException) {
                showToast(R.string.network_error);
            } else if (e instanceof RetrofitError) {
                if (((RetrofitError) e).getResponse() == null){
                    handleException(new IOException());
                }
                else if (((RetrofitError) e).getResponse().getStatus() == 401) {
                    try {
                        showToast(R.string.auth_error);
                        GoogleAuthUtil.clearToken(getActivity().getApplicationContext(), token);
                        mListener.doAuthInActivity();
                    } catch (Exception e1) {
                        handleException(e1);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(Void param){
            // update UI
            recyclerView.getAdapter().notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
            updateCountText(counts);
        }
    }

    /**
     * Async Task for sort or filter action.
     */
    private class SortAndFilterTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            MailProcessUtil.getInstance().filterAndSort(
                    SettingUtil.getFilterMethod(),
                    SettingUtil.getSortOrder(),
                    totalPortalDetails,
                    ((PortalListAdapter) recyclerView.getAdapter()).dataSet);
            return null;
        }

        @Override
        protected void onPostExecute(Void param){
            // update UI
            recyclerView.getAdapter().notifyDataSetChanged();
            setTitleBySetting(SettingUtil.getFilterMethod());
        }
    }

    private void updateCountText(int[] counts){
        if (counts!= null){
            int total = totalPortalDetails.size();
            countEverything.setText(Integer.toString(total));
            countAccepted.setText(Integer.toString(counts[0]));
            countRejected.setText(Integer.toString(counts[1]));
            countWaiting.setText(Integer.toString(total - counts[0] - counts[1]));
        }
    }

    public interface OnFragmentInteractionListener {
        public void doAuthInActivity();
        public void portalItemClicked(PortalDetail clickedPortal);
    }

}
