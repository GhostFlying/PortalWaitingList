package com.ghostflying.portalwaitinglist.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
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
import com.ghostflying.portalwaitinglist.R;
import com.ghostflying.portalwaitinglist.SettingActivity;
import com.ghostflying.portalwaitinglist.dao.datahelper.PortalEventHelper;
import com.ghostflying.portalwaitinglist.loader.PortalListLoader;
import com.ghostflying.portalwaitinglist.model.Message;
import com.ghostflying.portalwaitinglist.model.PortalDetail;
import com.ghostflying.portalwaitinglist.model.PortalEvent;
import com.ghostflying.portalwaitinglist.recyclerviewHelper.PortalListAdapter;
import com.ghostflying.portalwaitinglist.util.GMailServiceUtil;
import com.ghostflying.portalwaitinglist.util.MailProcessUtil;
import com.ghostflying.portalwaitinglist.util.SearchUtil;
import com.ghostflying.portalwaitinglist.util.SettingUtil;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;


public class PortalListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<PortalListLoader.PortalListViewModel> {

    private OnFragmentInteractionListener mListener;

    Toolbar toolbar;
    String token;
    String account;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    DrawerLayout drawerLayout;
    List<PortalDetail> totalPortalDetails;
    TextView countEverything;
    TextView countAccepted;
    TextView countRejected;
    TextView countWaiting;
    TextView totalPortals;
    TextView totalSubmission;
    TextView totalEdit;
    View selectedType;
    SearchTask searchTask;
    MenuItem searchItem;

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
        totalPortalDetails = new ArrayList<>();
        setToolbar(view);
        setDrawerLayout(view);
        setRecyclerView(view);
        setSwipeRefreshLayout(view);
        setHasOptionsMenu(true);
        switchActionBarColorBySetting();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        // set search view
        searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                new SearchTask().execute(newText);
                return true;
            }

        });
        // prefer to use this instead of setOnCloseListener
        // see http://developer.android.com/guide/topics/ui/actionbar.html#ActionView
        MenuItemCompat.setOnActionExpandListener(
                menu.findItem(R.id.action_search),
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        getLoaderManager().getLoader(0).onContentChanged();
                        return true;
                    }
                }
        );
        super.onCreateOptionsMenu(menu, inflater);
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
    public void onResume(){
        super.onResume();
        // read stored data.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStop (){
        searchItem.collapseActionView();
        super.onStop();
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
                    getActivity().overridePendingTransition(R.animator.setting_swap_in_bottom, R.animator.setting_swap_out_bottom);
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
                mListener.portalItemClicked(clickedPortal, v);
            }
        });
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // when return from setting activity and the filter or sort related
        // params are changed, do filter and sort.
        if (requestCode == SettingActivity.REQUEST_SETTING
                && resultCode == SettingActivity.RESULT_OK){
            getLoaderManager().getLoader(0).onContentChanged();
        }
    }

    @Override
    public Loader<PortalListLoader.PortalListViewModel> onCreateLoader(int id, Bundle args) {
        return new PortalListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<PortalListLoader.PortalListViewModel> loader,
                               PortalListLoader.PortalListViewModel data) {
        ((PortalListAdapter)recyclerView.getAdapter()).setDataSet(data.mPortals);
        switch (data.mAction){
            case GET_DATA:
                updateCountText(data.counts);
                if (account != null && data.mPortals.size() == 0){
                    showToast(R.string.alert_to_refresh);
                    swipeRefreshLayout.setRefreshing(true);
                    new RefreshTask().execute();
                }
                totalPortalDetails = data.totalPortals;
                break;
            case SORT:
                setTitleBySetting();
                break;
            case FILTER:
                setTitleBySetting();
                switchActionBarColorBySetting();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<PortalListLoader.PortalListViewModel> loader) {

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
        String refreshResult;

        @Override
        protected Void doInBackground(Void...params) {
            if (getToken())
                return null;
            // Initial Utils
            GMailServiceUtil fetchUtil = GMailServiceUtil.getInstance(token, false);
            MailProcessUtil processUtil = MailProcessUtil.getInstance();
            PortalEventHelper dbHelper = new PortalEventHelper(getActivity());
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

            PortalEventHelper mHelper = new PortalEventHelper(getActivity());
            mHelper.bulkInsert(newEvents);
            int[] eventCounts = MailProcessUtil.getInstance().getEventCountsInLastProcess();
            generateRefreshResultSummary(eventCounts);
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
            swipeRefreshLayout.setRefreshing(false);
            showRefreshResult(refreshResult);
        }
    }

    private class SearchTask extends AsyncTask<String, Void, Void>{

        @Override
        protected void onPreExecute (){
            if (searchTask != null){
                searchTask.cancel(true);
            }
            searchTask = this;
        }

        @Override
        protected Void doInBackground(String... params) {
            SearchUtil.searchByPortalName(
                    totalPortalDetails,
                    ((PortalListAdapter)recyclerView.getAdapter()).dataSet,
                    params[0]
            );
            return null;
        }

        @Override
        protected void onPostExecute(Void param){
            super.onPostExecute(param);
            recyclerView.getAdapter().notifyDataSetChanged();
            searchTask = null;
        }

        @Override
        protected void onCancelled(Void result){
            searchTask = null;
        }
    }

    private void updateCountText(int[] counts){
        if (counts!= null){
            countEverything.setText(Integer.toString(counts[0]));
            countAccepted.setText(Integer.toString(counts[1]));
            countRejected.setText(Integer.toString(counts[2]));
            countWaiting.setText(Integer.toString(counts[3]));

            // navigation
            totalPortals.setText(Integer.toString(counts[0]));
            totalSubmission.setText(Integer.toString(counts[4]));
            totalEdit.setText(Integer.toString(counts[5]));
        }
    }

    public interface OnFragmentInteractionListener {
        public void doAuthInActivity();
        public void portalItemClicked(PortalDetail clickedPortal, View clickedView);
    }

}
