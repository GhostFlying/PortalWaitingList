package com.ghostflying.ingressmailanalysis;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ghostflying.ingressmailanalysis.data.Message;
import com.ghostflying.ingressmailanalysis.data.PortalDetail;
import com.ghostflying.ingressmailanalysis.data.PortalEvent;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import retrofit.RetrofitError;


public class MainActivity extends ActionBarActivity{

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // read all settings from storage.
        SettingUtil.readAllSettings(this);
        // If account is not set, usually user open this first time
        // turn to AuthIntent.
        if ((account = SettingUtil.getAccount()) == null){
            doAuth();
        }
        // Initial some members
        totalPortalDetails = new ArrayList<PortalDetail>();
        dbHelper = new PortalEventDbHelper(this);
        setContentView(R.layout.activity_main);
        setToolbar();
        setSwipeRefreshLayout();
        setRecyclerView();
        setDrawerLayout();
        //Initial storied data
        new InitialTask().execute();
    }

    private void doAuth() {
        Intent authIntent = new Intent(new Intent(this, AuthActivity.class));
        authIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(authIntent);
    }

    private void setDrawerLayout(){
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        findViewById(R.id.item_everything).setOnClickListener(sortAndFilterClickListener);
        findViewById(R.id.item_accepted).setOnClickListener(sortAndFilterClickListener);
        findViewById(R.id.item_rejected).setOnClickListener(sortAndFilterClickListener);
        findViewById(R.id.item_waiting).setOnClickListener(sortAndFilterClickListener);
        findViewById(R.id.item_smart_order).setOnClickListener(sortAndFilterClickListener);
        findViewById(R.id.item_asc_order).setOnClickListener(sortAndFilterClickListener);
        findViewById(R.id.item_desc_order).setOnClickListener(sortAndFilterClickListener);
        countEverything = (TextView)findViewById(R.id.count_everything);
        countAccepted = (TextView)findViewById(R.id.count_accepted);
        countRejected = (TextView)findViewById(R.id.count_rejected);
        countWaiting = (TextView)findViewById(R.id.count_waiting);
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

    private void setToolbar(){
        toolbar = (Toolbar)findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        setTitleBySetting(SettingUtil.getFilterMethod());
    }

    private void setTitleBySetting(SettingUtil.FilterMethod filterMethod){
        switch (filterMethod){
            case EVERYTHING:
                setTitle(R.string.everything);
                break;
            case ACCEPTED:
                setTitle(R.string.accepted);
                break;
            case REJECTED:
                setTitle(R.string.rejected);
                break;
            case WAITING:
                setTitle(R.string.waiting);
                break;
        }
    }

    private void setSwipeRefreshLayout(){
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
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

    private void setRecyclerView(){
        recyclerView = (RecyclerView)findViewById(R.id.portal_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        PortalListAdapter adapter = new PortalListAdapter(new ArrayList<PortalDetail>());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
    protected void onResume(){
        super.onResume();
        if (account == null){
            if ((account = SettingUtil.getAccount()) == null){
                this.finish();
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        SettingUtil.saveAllSettings();
    }

    /**
     * The initial task once the activity start.
     */
    private class InitialTask extends AsyncTask<Void, Void, Void>{
        static final String ASC_STR = " ASC";
        static final String DESC_STR = " DESC";
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
                    null
            );
            int portalNameIndex = cursor.getColumnIndex(PortalEventContract.PortalEvent.COLUMN_NAME_PORTAL_NAME);
            int operationTypeIndex = cursor.getColumnIndex(PortalEventContract.PortalEvent.COLUMN_NAME_OPERATION_TYPE);
            int operationResultIndex = cursor.getColumnIndex(PortalEventContract.PortalEvent.COLUMN_NAME_OPERATION_RESULT);
            int messageIdIndex = cursor.getColumnIndex(PortalEventContract.PortalEvent.COLUMN_NAME_MESSAGE_ID);
            int dateIndex = cursor.getColumnIndex(PortalEventContract.PortalEvent.COLUMN_NAME_DATE);
            while(cursor.moveToNext()){
                String name = cursor.getString(portalNameIndex);
                PortalEvent.OperationType operationType = PortalEvent.OperationType.values()[cursor.getInt(operationTypeIndex)];
                PortalEvent.OperationResult operationResult = PortalEvent.OperationResult.values()[cursor.getInt(operationResultIndex)];
                Date date = new Date(cursor.getLong(dateIndex));
                String messageId = cursor.getString(messageIdIndex);
                PortalEvent event = new PortalEvent(name, operationType, operationResult, date, messageId);
                portalEvents.add(event);
            }
            cursor.close();
            database.close();
            // merge stored events to empty portal detail.
            GMailServiceUtil util = GMailServiceUtil.getInstance();
            util.mergeEvents(totalPortalDetails, portalEvents);
            //update counts
            counts = util.getCounts(totalPortalDetails);
            // sort and filter the portal details
            util.filterAndSort(
                    SettingUtil.getFilterMethod(),
                    SettingUtil.getSortOrder(),
                    totalPortalDetails,
                    ((PortalListAdapter)recyclerView.getAdapter()).dataSet
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_SHORT).show();
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
                token = GoogleAuthUtil.getToken(getApplicationContext(), account, AuthActivity.SCOPE);
            }catch (Exception e){
                handleException(e);
                return null;
            }
            GMailServiceUtil util = GMailServiceUtil.getInstance();
            util.setToken(token);
            // query and convert all new messages.
            ArrayList<Message> newMessages;
            try{
                newMessages = util.getPortalMessages(dbHelper);
            }
            catch (Exception e){
                handleException(e);
                return null;
            }
            ArrayList<PortalEvent> newEvents = GMailServiceUtil.getInstance().analysisMessages(newMessages);

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
                db.insert(
                        PortalEventContract.PortalEvent.TABLE_NAME,
                        null,
                        values);
            }
            db.close();
            // merge new events to exist portal details
            util.mergeEvents(totalPortalDetails, newEvents);
            // update counts
            counts = util.getCounts(totalPortalDetails);
            // sort and filter the portal details
            util.filterAndSort(
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
            if (e instanceof GoogleAuthException) {
                showToast(R.string.auth_error);
                doAuth();
            } else if (e instanceof IOException) {
                showToast(R.string.network_error);
            } else if (e instanceof RetrofitError) {
                if (((RetrofitError) e).getResponse().getStatus() == 401) {
                    try {
                        showToast(R.string.auth_error);
                        GoogleAuthUtil.clearToken(getApplicationContext(), token);
                        doAuth();
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
    private class SortAndFilterTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            GMailServiceUtil.getInstance().filterAndSort(
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

}
