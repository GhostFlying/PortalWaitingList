package com.ghostflying.portalwaitinglist.fragment;


import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.ghostflying.portalwaitinglist.ObservableScrollView;
import com.ghostflying.portalwaitinglist.R;
import com.ghostflying.portalwaitinglist.model.PortalDetail;
import com.ghostflying.portalwaitinglist.model.PortalEvent;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;


public class ReDesignDetailFragment extends Fragment implements ObservableScrollView.Callbacks {
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
    private DateFormat localDateFormat;
    private int mPhotoHeightPixels;
    private int mHeaderHeightPixels;
    private boolean mHasPhoto;
    private float mMaxHeaderElevation;


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

        // events
        mDetailsContainer = view.findViewById(R.id.detail_container);
        addEventViews(clickedPortal, (ViewGroup)mDetailsContainer);

        mHasPhoto = true;

        // set color
        setStatusAndActionBarBg(clickedPortal);

        // set observer for views
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
        }
        return view;
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

    private void setStatusAndActionBarBg(PortalDetail portal){
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
}
