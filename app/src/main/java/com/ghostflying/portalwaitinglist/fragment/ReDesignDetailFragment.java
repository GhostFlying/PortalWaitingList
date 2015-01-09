package com.ghostflying.portalwaitinglist.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.ghostflying.portalwaitinglist.ObservableScrollView;
import com.ghostflying.portalwaitinglist.R;
import com.ghostflying.portalwaitinglist.model.PortalDetail;

import java.io.Serializable;


public class ReDesignDetailFragment extends Fragment implements ObservableScrollView.Callbacks {
    static final String ARG_CLICKED_PORTAL_NAME = "clickedPortal";

    PortalDetail clickedPortal;
    ObservableScrollView mScrollView;
    ImageView mPhotoView;
    View mPhotoViewContainer;
    View mHeaderBox;
    View mDetailsContainer;
    Toolbar mToolbar;
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
        if (getArguments() != null) {
            clickedPortal = (PortalDetail)getArguments().getSerializable(ARG_CLICKED_PORTAL_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_re_design_detail, container, false);
        mToolbar = (Toolbar)view.findViewById(R.id.detail_toolbar);
        ((ActionBarActivity)getActivity()).setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_up);
        mScrollView = (ObservableScrollView)view.findViewById(R.id.scroll_view);
        mScrollView.addCallbacks(this);
        mHeaderBox = view.findViewById(R.id.header_portal);
        mPhotoView = (ImageView)view.findViewById(R.id.portal_photo);
        mHasPhoto = true;
        mPhotoViewContainer = view.findViewById(R.id.portal_photo_container);
        mDetailsContainer = view.findViewById(R.id.detail_container);
        mMaxHeaderElevation = getResources().getDimensionPixelSize(
                R.dimen.portal_detail_max_header_elevation);
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
        }
        return view;
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
