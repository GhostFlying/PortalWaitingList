package com.ghostflying.portalwaitinglist.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
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
import com.ghostflying.portalwaitinglist.SettingActivity;
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
    TextView totalPortals;
    TextView totalSubmission;
    TextView totalEdit;
    View selectedType;
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
        setToolbar(view);
        setDrawerLayout(view);
        setRecyclerView(view);
        setSwipeRefreshLayout(view);
        setHasOptionsMenu(true);
        switchActionBarColorBySetting();
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

    private void setDrawerLayout(View v){
        drawerLayout = (DrawerLayout)v.findViewById(R.id.drawer_layout);
        // set the status bar bg when nav do not open
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.primary_dark));

        // handle the home button
        ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(getActivity(),
                        drawerLayout,
                        toolbar,
                        R.string.app_name,
                        R.string.app_name);
        actionBarDrawerToggle.syncState();
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        // result filter
        v.findViewById(R.id.item_everything).setOnClickListener(resultFilterClickListener);
        v.findViewById(R.id.item_accepted).setOnClickListener(resultFilterClickListener);
        v.findViewById(R.id.item_rejected).setOnClickListener(resultFilterClickListener);
        v.findViewById(R.id.item_waiting).setOnClickListener(resultFilterClickListener);

        // sort
        v.findViewById(R.id.item_smart_order).setOnClickListener(sortClickListener);
        v.findViewById(R.id.item_last_asc_order).setOnClickListener(sortClickListener);
        v.findViewById(R.id.item_last_desc_order).setOnClickListener(sortClickListener);
        v.findViewById(R.id.item_proposed_asc_order).setOnClickListener(sortClickListener);
        v.findViewById(R.id.item_proposed_desc_order).setOnClickListener(sortClickListener);
        v.findViewById(R.id.item_alphabetical_order).setOnClickListener(sortClickListener);

        // type filter
        v.findViewById(R.id.navigation_item_all).setOnClickListener(typeFilterClickListener);
        v.findViewById(R.id.navigation_item_submission).setOnClickListener(typeFilterClickListener);
        v.findViewById(R.id.navigation_item_edit).setOnClickListener(typeFilterClickListener);


        // select from setting
        switch (SettingUtil.getTypeFilterMethod()){
            case ALL:
                selectedType = v.findViewById(R.id.navigation_item_all);
                break;
            case SUBMISSION:
                selectedType = v.findViewById(R.id.navigation_item_submission);
                break;
            case EDIT:
                selectedType = v.findViewById(R.id.navigation_item_edit);
                break;
            default:
                selectedType = v.findViewById(R.id.navigation_item_all);
        }
        selectedType.setSelected(true);

        // other in navigation
        v.findViewById(R.id.navigation_item_setting).setOnClickListener(navigationDrawerClickListener);
        v.findViewById(R.id.navigation_item_feedback).setOnClickListener(navigationDrawerClickListener);
        totalPortals = (TextView)v.findViewById(R.id.navigation_drawer_total_portals);
        totalEdit = (TextView)v.findViewById(R.id.navigation_drawer_total_edit);
        totalSubmission = (TextView)v.findViewById(R.id.navigation_drawer_total_submission);

        // set the user avatar and account name
        if (SettingUtil.getAccount() != null){
            ((TextView)v.findViewById(R.id.account_name)).setText(SettingUtil.getAccount());
            ((TextView)v.findViewById(R.id.user_avatar)).setText(
                    SettingUtil.getAccount().toUpperCase().substring(0 ,1)
            );
        }


        countEverything = (TextView)v.findViewById(R.id.count_everything);
        countAccepted = (TextView)v.findViewById(R.id.count_accepted);
        countRejected = (TextView)v.findViewById(R.id.count_rejected);
        countWaiting = (TextView)v.findViewById(R.id.count_waiting);
    }

    View.OnClickListener navigationDrawerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.navigation_item_setting:
                    Intent setting = new Intent(getActivity(), SettingActivity.class);
                    startActivityForResult(setting, SettingActivity.REQUEST_SETTING);
                    break;
                case R.id.navigation_item_feedback:
                    Intent mailIntent = new Intent(
                            Intent.ACTION_SENDTO,
                            Uri.fromParts("mailto", getString(R.string.author_mail), null)
                    );
                    mailIntent.putExtra(Intent.EXTRA_SUBJECT,
                            getString(R.string.navigation_drawer_feedback_subject));
                    startActivity(Intent.createChooser(
                            mailIntent, getString(R.string.navigation_drawer_send)));
                    break;
            }
        }
    };

    View.OnClickListener sortClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.item_smart_order:
                    SettingUtil.setSortOrder(SettingUtil.SortOrder.SMART_ORDER);
                    break;
                case R.id.item_last_asc_order:
                    SettingUtil.setSortOrder(SettingUtil.SortOrder.LAST_DATE_ASC);
                    break;
                case R.id.item_last_desc_order:
                    SettingUtil.setSortOrder(SettingUtil.SortOrder.LAST_DATE_DESC);
                    break;
                case R.id.item_alphabetical_order:
                    SettingUtil.setSortOrder(SettingUtil.SortOrder.ALPHABETICAL);
                    break;
                case R.id.item_proposed_asc_order:
                    SettingUtil.setSortOrder(SettingUtil.SortOrder.PROPOSED_DATE_ASC);
                    break;
                case R.id.item_proposed_desc_order:
                    SettingUtil.setSortOrder(SettingUtil.SortOrder.PROPOSED_DATE_DESC);
                    break;
                default:
            }
            new SortTask().execute();
            drawerLayout.closeDrawer(Gravity.RIGHT);
        }
    };

    View.OnClickListener resultFilterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.item_everything:
                    SettingUtil.setResultFilterMethod(SettingUtil.ResultFilterMethod.EVERYTHING);
                    break;
                case R.id.item_accepted:
                    SettingUtil.setResultFilterMethod(SettingUtil.ResultFilterMethod.ACCEPTED);
                    break;
                case R.id.item_rejected:
                    SettingUtil.setResultFilterMethod(SettingUtil.ResultFilterMethod.REJECTED);
                    break;
                case R.id.item_waiting:
                    SettingUtil.setResultFilterMethod(SettingUtil.ResultFilterMethod.WAITING);
                    break;
                default:
            }
            new FilterTask().execute();
            drawerLayout.closeDrawer(Gravity.RIGHT);
        }
    };

    View.OnClickListener typeFilterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.navigation_item_all:
                    SettingUtil.setTypeFilterMethod(SettingUtil.TypeFilterMethod.ALL);
                    break;
                case R.id.navigation_item_submission:
                    SettingUtil.setTypeFilterMethod(SettingUtil.TypeFilterMethod.SUBMISSION);
                    break;
                case R.id.navigation_item_edit:
                    SettingUtil.setTypeFilterMethod(SettingUtil.TypeFilterMethod.EDIT);
                    break;
                default:
            }
            // reset selected before
            if (selectedType != null)
                selectedType.setSelected(false);
            // select this
            v.setSelected(true);
            selectedType = v;
            new FilterTask().execute();
            drawerLayout.closeDrawer(Gravity.LEFT);
        }
    };

    private void switchActionBarColorBySetting(){
        int actionBarBg;
        int statusBarBg;
        switch (SettingUtil.getResultFilterMethod()){
            case EVERYTHING:
                actionBarBg = R.color.portal_list_action_bar_bg_everything;
                statusBarBg = R.color.portal_list_status_bar_bg_everything;
                break;
            case ACCEPTED:
                actionBarBg = R.color.portal_list_action_bar_bg_accepted;
                statusBarBg = R.color.portal_list_status_bar_bg_accepted;
                break;
            case REJECTED:
                actionBarBg = R.color.portal_list_action_bar_bg_rejected;
                statusBarBg = R.color.portal_list_status_bar_bg_rejected;
                break;
            case WAITING:
                actionBarBg = R.color.portal_list_action_bar_bg_waiting;
                statusBarBg = R.color.portal_list_status_bar_bg_waiting;
                break;
            default:
                actionBarBg = R.color.primary;
                statusBarBg = R.color.primary_dark;
        }
        toolbar.setBackgroundResource(actionBarBg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            drawerLayout.setStatusBarBackground(statusBarBg);
    }

    private void setToolbar(View v){
        toolbar = (Toolbar)v.findViewById(R.id.action_bar_in_list);
        setTitleBySetting();
        ((ActionBarActivity)getActivity()).setSupportActionBar(toolbar);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setTitleBySetting(){
        String title = "";
        switch (SettingUtil.getTypeFilterMethod()){
            case ALL:
                title = getResources().getString(R.string.navigation_drawer_all_portals);
                break;
            case EDIT:
                title = getResources().getString(R.string.navigation_drawer_edit_portals);
                break;
            case SUBMISSION:
                title = getResources().getString(R.string.navigation_drawer_submission_portals);
                break;
        }

        title += " - ";

        switch (SettingUtil.getSortOrder()){
            case SMART_ORDER:
                title += getString(R.string.smart_order);
                break;
            case LAST_DATE_ASC:
                title += getString(R.string.last_asc_order);
                break;
            case LAST_DATE_DESC:
                title += getString(R.string.last_desc_order);
                break;
            case ALPHABETICAL:
                title += getString(R.string.alphabetical_order);
                break;
            case PROPOSED_DATE_ASC:
                title += getString(R.string.proposed_asc_order);
                break;
            case PROPOSED_DATE_DESC:
                title += getString(R.string.proposed_desc_order);
                break;
        }

        toolbar.setTitle(title);
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // when return from setting activity and the filter or sort related
        // params are changed, do filter and sort.
        if (requestCode == SettingActivity.REQUEST_SETTING
                && resultCode == SettingActivity.RESULT_OK)
            new SortTask().execute();
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
            //update portalCounts
            counts = processUtil.getCounts(totalPortalDetails);
            // sort and filter the portal details
            processUtil.filterAndSort(
                    SettingUtil.getTypeFilterMethod(),
                    SettingUtil.getResultFilterMethod(),
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
        int[] portalCounts;
        String refreshResult;

        @Override
        protected Void doInBackground(Void...params) {
            // wait until initial done.
            while (!isInitialed);
            if (getToken())
                return null;
            // Initial Utils
            GMailServiceUtil fetchUtil = GMailServiceUtil.getInstance(token, false);
            MailProcessUtil processUtil = MailProcessUtil.getInstance();
            // query and convert all new messages.
            ArrayList<Message> newMessages;
            try{
                newMessages = fetchUtil.getPortalMessages(dbHelper);
            }
            catch (Exception e){
                if (handleException(e))
                    return null;
                else {
                    // if the exception is not fatal, as the invalid token
                    // handleException will clear the token, and getToken here again.
                    if (getToken())
                        return null;
                    // now the new token is obtained
                    // regenerate the util
                    fetchUtil = GMailServiceUtil.getInstance(token, true);
                    // reFetch the messages
                   try {
                       newMessages = fetchUtil.getPortalMessages(dbHelper);
                   }
                   catch (Exception e1){
                       // no matter the exception is fatal, refresh stop.
                       handleException(e1, true);
                       return null;
                   }
                }
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
            // update Counts
            portalCounts = processUtil.getCounts(totalPortalDetails);
            int[] eventCounts = processUtil.getEventCountsInLastProcess();
            generateRefreshResultSummary(eventCounts);

            // sort and filter the portal details
            processUtil.filterAndSort(
                    SettingUtil.getTypeFilterMethod(),
                    SettingUtil.getResultFilterMethod(),
                    SettingUtil.getSortOrder(),
                    totalPortalDetails,
                    ((PortalListAdapter) recyclerView.getAdapter()).dataSet
            );
            return null;
        }

        private void generateRefreshResultSummary(int[] eventCounts) {
            // generate the refresh result text
            refreshResult = getString(R.string.refresh_done);
            boolean anythingNew = false;
            if (eventCounts[0] != 0){
                anythingNew = true;
                refreshResult += "\n"
                        + Integer.toString(eventCounts[0])
                        + getString(R.string.refresh_result_proposed);
            }
            if (eventCounts[1] != 0){
                anythingNew = true;
                refreshResult += "\n"
                        + Integer.toString(eventCounts[1])
                        + getString(R.string.refresh_result_accepted);
            }
            if (eventCounts[2] != 0){
                anythingNew = true;
                refreshResult += "\n"
                        + Integer.toString(eventCounts[2])
                        + getString(R.string.refresh_result_rejected);
            }
            if (!anythingNew){
                refreshResult += "\n"
                        + getString(R.string.refresh_result_nothing);
            }
        }

        private boolean getToken() {
            try{
                token = GoogleAuthUtil.getToken(getActivity().getApplicationContext(), account, AuthActivity.SCOPE);
            }catch (Exception e){
                handleException(e);
                return true;
            }
            return false;
        }

        /**
         * Handle the exception during the refresh.
         * @param e the exception.
         * @return true if the exception is fatal, otherwise false
         */
        private boolean handleException(final Exception e, boolean retry){
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
                        GoogleAuthUtil.clearToken(getActivity().getApplicationContext(), token);
                        // if this is the retry, call the auth activity.
                        if (retry){
                            showToast(R.string.auth_error);
                            mListener.doAuthInActivity();
                        }
                        return false;
                    } catch (Exception e1) {
                        handleException(e1);
                    }
                }
            }
            return true;
        }

        private boolean handleException(final Exception e){
            return handleException(e, false);
        }

        private void showRefreshResult(String result){
            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                    result,
                    Toast.LENGTH_LONG);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            v.setGravity(Gravity.CENTER);
            toast.show();
        }

        @Override
        protected void onPostExecute(Void param){
            // update UI
            recyclerView.getAdapter().notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
            updateCountText(portalCounts);
            showRefreshResult(refreshResult);
        }
    }

    /**
     * Async Task for sort or filter action.
     */
    private abstract class SortAndFilterBaseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected abstract Void doInBackground(Void... params);

        @Override
        protected void onPostExecute(Void param){
            // update UI
            recyclerView.getAdapter().notifyDataSetChanged();
            setTitleBySetting();
        }
    }

    private class FilterTask extends SortAndFilterBaseTask{

        @Override
        protected Void doInBackground(Void... params) {
            MailProcessUtil.getInstance().filterAndSort(
                    SettingUtil.getTypeFilterMethod(),
                    SettingUtil.getResultFilterMethod(),
                    SettingUtil.getSortOrder(),
                    totalPortalDetails,
                    ((PortalListAdapter) recyclerView.getAdapter()).dataSet);
            return null;
        }

        @Override
        protected void onPostExecute(Void param){
            super.onPostExecute(param);
            switchActionBarColorBySetting();
        }
    }

    private class SortTask extends SortAndFilterBaseTask{

        @Override
        protected Void doInBackground(Void... params) {
            MailProcessUtil.getInstance()
                    .sortPortalDetails(
                            SettingUtil.getSortOrder(),
                            ((PortalListAdapter) recyclerView.getAdapter()).dataSet
                    );
            return null;
        }
    }

    private void updateCountText(int[] counts){
        if (counts!= null){
            int total = totalPortalDetails.size();
            countEverything.setText(Integer.toString(total));
            countAccepted.setText(Integer.toString(counts[0]));
            countRejected.setText(Integer.toString(counts[1]));
            countWaiting.setText(Integer.toString(total - counts[0] - counts[1]));

            // navigation
            totalPortals.setText(Integer.toString(total));
            totalSubmission.setText(Integer.toString(counts[2]));
            totalEdit.setText(Integer.toString(counts[3]));
        }
    }

    public interface OnFragmentInteractionListener {
        public void doAuthInActivity();
        public void portalItemClicked(PortalDetail clickedPortal);
    }

}
