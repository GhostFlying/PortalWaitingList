package com.ghostflying.portalwaitinglist.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ghostflying.portalwaitinglist.ObservableScrollView;
import com.ghostflying.portalwaitinglist.R;
import com.ghostflying.portalwaitinglist.model.PortalDetail;
import com.ghostflying.portalwaitinglist.model.PortalEvent;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;


public class ReDesignDetailFragment extends Fragment
        implements ObservableScrollView.Callbacks {
    static final String ARG_CLICKED_PORTAL_NAME = "clickedPortal";

    PortalDetail clickedPortal;
    ObservableScrollView mScrollView;
    ImageView mPhotoView;
    View mPhotoViewContainer;
    View mHeaderBox;
    View mDetailsContainer;
    TextView mPortalName;
    TextView mPortalSummary;
    Toolbar mToolbar;
    View mPortalEventListContainer;
    private DateFormat localDateFormat;
    private int mPhotoHeightPixels;
    private int mHeaderHeightPixels;
    private boolean mHasPhoto;
    private float mMaxHeaderElevation;
    private String addressUrl;


    public static ReDesignDetailFragment newInstance(Serializable clickedPortal) {
        ReDesignDetailFragment fragment = new ReDesignDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CLICKED_PORTAL_NAME, clickedPortal);
        fragment.setArguments(args);
        return fragment;
    }

    public ReDesignDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        if (getArguments() != null) {
            clickedPortal = (PortalDetail)getArguments().getSerializable(ARG_CLICKED_PORTAL_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_re_design_detail, container, false);

        // toolbar
        mToolbar = (Toolbar)view.findViewById(R.id.detail_toolbar);
        mToolbar.setTitle("");
        ((ActionBarActivity)getActivity()).setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_up);
        // remove the elevation to make header unify
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mToolbar.setElevation(0);
        mScrollView = (ObservableScrollView)view.findViewById(R.id.scroll_view);
        mScrollView.addCallbacks(this);

        // Header
        mHeaderBox = view.findViewById(R.id.header_portal);
        mPhotoView = (ImageView)view.findViewById(R.id.portal_photo);
        mPhotoViewContainer = view.findViewById(R.id.portal_photo_container);
        mPortalName = (TextView)view.findViewById(R.id.portal_name);
        mPortalSummary = (TextView)view.findViewById(R.id.portal_status_in_detail);
        mPortalName.setText(clickedPortal.getName());
        mPortalSummary.setText(getSummaryText(clickedPortal));
        mMaxHeaderElevation = getResources().getDimensionPixelSize(
                R.dimen.portal_detail_max_header_elevation);

        // details
        mDetailsContainer = view.findViewById(R.id.detail_container);
        String address = clickedPortal.getAddress();
        if (address != null){
            ((TextView)view.findViewById(R.id.portal_address_in_detail))
                    .setText(address);
        }
        else {
            view.findViewById(R.id.portal_address_view_in_detail).setVisibility(View.GONE);
        }
        mPortalEventListContainer = view.findViewById(R.id.portal_event_list);
        addEventViews(clickedPortal, (ViewGroup)mPortalEventListContainer);

        // photo
        String photoUrl = clickedPortal.getImageUrl();
        if (photoUrl != null && photoUrl.startsWith("http")){
            mHasPhoto = true;
            // download and show the image of portal
            Picasso.with(getActivity())
                    .load(photoUrl.replaceFirst("http://", "https://"))
                    .placeholder(R.drawable.portal_photo_placeholder)
                    .resizeDimen(R.dimen.portal_detail_photo_width, R.dimen.portal_detail_photo_height)
                    .into(mPhotoView);
        }
        else {
            mHasPhoto = false;
        }
        // set color
        setColors(clickedPortal);

        // set observer for views
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
        }

        // menu
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_re_design_detail, menu);

        // check if there is available address url
        addressUrl = clickedPortal.getAddressUrl();
        if (addressUrl == null || !addressUrl.startsWith("http"))
            menu.removeItem(R.id.menu_item_view_map);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.menu_item_view_map:
                openUrl(addressUrl);
                return true;
            case R.id.menu_item_share:
                new ShareTask().execute();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openUrl(String url){
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(webIntent);
    }

    private void doShare(Bitmap generated){
        // save file to external
        File file = new File(getActivity().getExternalCacheDir(),
                "ScreenShot-" + Long.toString(new Date().getTime()) + ".png");
        try{
            FileOutputStream fOut = new FileOutputStream(file);
            generated.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        }
        catch (Exception e){
            handleException(e);
            return;
        }

        // share file to other apps
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        Uri uri = Uri.fromFile(file);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_TEXT, clickedPortal.getName());
        startActivity(Intent.createChooser(share, "Share Portal"));
    }

    private void handleException(Exception e){
        e.printStackTrace();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), R.string.write_error_toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap getBitmapFromView(View v) {
        // store the origin state
        int originWidth = v.getMeasuredWidth();
        int originHeight = v.getMeasuredHeight();
        // capture the view
        int specWidth = View.MeasureSpec.makeMeasureSpec(originWidth, View.MeasureSpec.EXACTLY);
        int specHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(specWidth, specHeight);
        Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        Canvas c = new Canvas(b);
        c.drawColor(getResources().getColor(R.color.default_background));
        v.draw(c);

        // restore the view
        specHeight = View.MeasureSpec.makeMeasureSpec(originHeight, View.MeasureSpec.EXACTLY);
        v.measure(specWidth, specHeight);
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        return b;
    }

    private void addEventViews(PortalDetail portalDetail, ViewGroup container){
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        for (PortalEvent event : portalDetail.getEvents()){
            View view = inflater.inflate(R.layout.portal_event_in_re_design_detail,
                    container, false);
            ((ImageView)view.findViewById(R.id.event_result)).setImageResource(
                    getEventResultIconResource(event.getOperationResult())
            );
            ((TextView)view.findViewById(R.id.event_description)).setText(getEventDescription(event));
            ((TextView)view.findViewById(R.id.event_date))
                    .setText(localDateFormat.format(event.getDate()) +
                    " (" + getDateDiffStr(event.getDate()) + getString(R.string.days_ago) + ")");
            container.addView(view);
        }
    }

    private String getEventDescription(PortalEvent event){
        String description = null;
        switch (event.getOperationType()){
            case SUBMISSION:
                description = getString(R.string.event_description_submission);
                break;
            case EDIT:
                description = getString(R.string.event_description_edit);
                break;
            case INVALID:
                description = getString(R.string.event_description_invalid);
                break;
        }
        switch (event.getOperationResult()){
            case PROPOSED:
                description += getString(R.string.event_description_proposed);
                break;
            case ACCEPTED:
                description += getString(R.string.event_description_accepted);
                break;
            case DUPLICATE:
            case REJECTED:
                description += getString(R.string.event_description_rejected);
                break;
        }
        return description;
    }

    private int getEventResultIconResource(PortalEvent.OperationResult result){
        switch (result){
            case PROPOSED:
                return R.drawable.ic_waiting;
            case ACCEPTED:
                return R.drawable.ic_accepted;
            case DUPLICATE:
            case REJECTED:
                return R.drawable.ic_rejected;
        }
        return R.drawable.ic_launcher;
    }

    private String getSummaryText(PortalDetail portalDetail){
        // return the summary text for portals
        // if portal is still waiting, the text is like Waiting for xxx days.
        // if portal is accepted/rejected, the text is like Accepted/Rejected xxx days ago.
        if (!portalDetail.isReviewed()){
            return getString(R.string.waiting) + getString(R.string.date_for) +
                    getDateDiffStr(portalDetail.getLastUpdated()) + getString(R.string.days);
        }
        else if (portalDetail.isAccepted()){
            return getString(R.string.accepted) + " " +
                    getDateDiffStr(portalDetail.getLastUpdated()) + " " + getString(R.string.day_ago);
        }
        else if (portalDetail.isRejected()){
            return getString(R.string.rejected) + " " +
                getDateDiffStr(portalDetail.getLastUpdated()) + " " + getString(R.string.day_ago);
        }
        else
            return getString(R.string.default_status);
    }

    private String getDateDiffStr(Date date){
        long diff = new Date().getTime() - date.getTime();
        int dayDiff = Math.round(diff / 1000 / 3600 / 24);
        return Integer.toString(dayDiff);
    }

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener
            = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            recomputePhotoAndScrollingMetrics();
        }
    };

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        // Reposition the header bar -- it's normally anchored to the top of the content,
        // but locks to the top of the screen on scroll
        // clone from Google IO
        int scrollY = mScrollView.getScrollY();

        float newTop = Math.max(mPhotoHeightPixels, scrollY);
        mHeaderBox.setTranslationY(newTop);

        float gapFillProgress = 1;
        if (mPhotoHeightPixels != 0) {
            gapFillProgress = Math.min(Math.max(getProgress(scrollY,
                    0,
                    mPhotoHeightPixels), 0), 1);
        }

        ViewCompat.setElevation(mHeaderBox, gapFillProgress * mMaxHeaderElevation);

        // Move background photo (parallax effect)
        mPhotoViewContainer.setTranslationY(scrollY * 0.5f);
    }

    private void recomputePhotoAndScrollingMetrics() {
        // clone from Google IO
        mHeaderHeightPixels = mHeaderBox.getHeight();

        mPhotoHeightPixels = 0;
        if (mHasPhoto) {
            mPhotoHeightPixels = mPhotoView.getHeight();
            mPhotoHeightPixels = Math.min(mPhotoHeightPixels, mScrollView.getHeight() * 2 / 3);
        }

        ViewGroup.LayoutParams lp;
        lp = mPhotoViewContainer.getLayoutParams();
        if (lp.height != mPhotoHeightPixels) {
            lp.height = mPhotoHeightPixels;
            mPhotoViewContainer.setLayoutParams(lp);
        }

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mDetailsContainer.getLayoutParams();
        if (mlp.topMargin != mHeaderHeightPixels + mPhotoHeightPixels) {
            mlp.topMargin = mHeaderHeightPixels + mPhotoHeightPixels;
            mDetailsContainer.setLayoutParams(mlp);
        }

        onScrollChanged(0, 0); // trigger scroll handling
    }

    private void setColors(PortalDetail portal){
        int actionBarBg;
        int statusBarBg;
        if (portal.isAccepted()){
            actionBarBg = getResources().getColor(R.color.portal_detail_action_bar_bg_accepted);
            statusBarBg = getResources().getColor(R.color.portal_detail_status_bar_bg_accepted);
        }
        else if (portal.isRejected()){
            actionBarBg = getResources().getColor(R.color.portal_detail_action_bar_bg_rejected);
            statusBarBg = getResources().getColor(R.color.portal_detail_status_bar_bg_rejected);
        }
        else {
            actionBarBg = getResources().getColor(R.color.portal_detail_action_bar_bg_waiting);
            statusBarBg = getResources().getColor(R.color.portal_detail_status_bar_bg_waiting);
        }

        mToolbar.setBackgroundColor(actionBarBg);
        mHeaderBox.setBackgroundColor(actionBarBg);
        mPhotoViewContainer.setBackgroundColor(statusBarBg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getActivity().getWindow().setStatusBarColor(statusBarBg);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mScrollView == null) {
            return;
        }

        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.removeGlobalOnLayoutListener(mGlobalLayoutListener);
        }
    }

    public float getProgress(int value, int min, int max) {
        if (min == max) {
            throw new IllegalArgumentException("Max (" + max + ") cannot equal min (" + min + ")");
        }

        return (value - min) / (float) (max - min);
    }

    public class ShareTask extends AsyncTask<Void, Void, Void> {
        Bitmap bitmap;
        ScreenShotDialogFragment dialogFragment;

        @Override
        protected void onPreExecute() {
            dialogFragment = ScreenShotDialogFragment.newInstance();
            dialogFragment.show(getFragmentManager(), null);
            bitmap = getBitmapFromView(getView());
        }

        @Override
        protected Void doInBackground(Void... params) {
            doShare(bitmap);
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            dialogFragment.dismiss();
        }
    }
}
