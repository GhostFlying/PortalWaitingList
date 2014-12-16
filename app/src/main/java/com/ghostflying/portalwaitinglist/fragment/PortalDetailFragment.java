package com.ghostflying.portalwaitinglist.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ghostflying.portalwaitinglist.R;
import com.ghostflying.portalwaitinglist.data.PortalDetail;
import com.ghostflying.portalwaitinglist.data.PortalEvent;
import com.squareup.picasso.Picasso;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PortalDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PortalDetailFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    DateFormat localDateFormat;
    Toolbar toolbar;

    public static PortalDetailFragment newInstance() {
        PortalDetailFragment fragment = new PortalDetailFragment();
        return fragment;
    }

    public PortalDetailFragment() {
        // Required empty public constructor
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_portal_detail, container, false);
        final PortalDetail clickedPortal = mListener.getSelectedPortal();
        localDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

        setToolbar(view, clickedPortal);

        setStatusAndActionBarBg(clickedPortal);

        // set the text of the view.
        ((TextView)view.findViewById(R.id.last_updated_in_detail))
                .setText(getDateDiffStr(clickedPortal.getLastUpdated()));
        ((TextView)view.findViewById(R.id.portal_status_in_detail))
                .setText(getStatusTextResource(clickedPortal));
        String address = clickedPortal.getAddress();
        if (address != null){
            ((TextView)view.findViewById(R.id.portal_address_in_detail))
                    .setText(clickedPortal.getAddress());
            view.findViewById(R.id.portal_address_view_in_detail)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openUrl(clickedPortal.getAddressUrl());
                        }
                    });
        }
        else
            view.findViewById(R.id.portal_address_view_in_detail).setVisibility(View.GONE);
        // add the event list view
        addEventViews(clickedPortal, (LinearLayout)view.findViewById(R.id.event_list_in_detail));

        String imageUrl = clickedPortal.getImageUrl();
        if (imageUrl != null && imageUrl.startsWith("http")){
            // download and show the image of portal
            Picasso.with(getActivity())
                    .load(imageUrl.replaceFirst("http://", "https://"))
                    .error(R.drawable.network_error)
                    .into((ImageView)view.findViewById(R.id.portal_image_in_detail));
        }
        else {
            view.findViewById(R.id.portal_image_view_in_detail).setVisibility(View.GONE);
        }
        return view;
    }

    private void openUrl(String url){
        if (url.startsWith("http")){
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(webIntent);
        }
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

        toolbar.setBackgroundColor(actionBarBg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getActivity().getWindow().setStatusBarColor(statusBarBg);
    }

    private void setToolbar(View v, PortalDetail portalDetail){
        toolbar = (Toolbar)v.findViewById(R.id.action_bar_in_detail);
        ((ActionBarActivity)getActivity()).setSupportActionBar(toolbar);
        getActivity().setTitle(portalDetail.getName());
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            mListener.onUpButtonClicked();
            return true;
        }
        else if (id == R.id.menu_item_share){
            doShare();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doShare(){
        Bitmap generated = getBitmapFromView(getView());
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/ScreenShot.png", false);
            generated.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        String dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/ScreenShot.png";
        Uri uri = Uri.parse("file://" + dir);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_TEXT, toolbar.getTitle());
        startActivity(Intent.createChooser(share, "Share Portal"));
    }

    private Bitmap getBitmapFromView(View v) {
        // store the origin state
        int originWidth = v.getMeasuredWidth();
        int originHeight = v.getMeasuredHeight();
        // capture the view
        int specWidth = View.MeasureSpec.makeMeasureSpec(originWidth, View.MeasureSpec.EXACTLY);
        int specHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(specWidth, specHeight);
        Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight() + 20, Bitmap.Config.ARGB_8888);
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        Canvas c = new Canvas(b);
        c.drawColor(getResources().getColor(R.color.default_background));
        v.draw(c);

        // restore the view
        v.measure(originWidth, originHeight);
        v.layout(0, 0, originWidth, originHeight);
        return b;
    }

    private void addEventViews(PortalDetail portalDetail, ViewGroup container){
        for (PortalEvent event : portalDetail.getEvents()){
            View view = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.portal_event_item_in_detail, container, false);
            ((TextView)view.findViewById(R.id.text_operation_type))
                    .setText(getOperationTypeTextResource(event.getOperationType()));
            ((TextView)view.findViewById(R.id.text_operation_result))
                    .setText(getOperationResultTextResource(event.getOperationResult()));
            ((TextView)view.findViewById(R.id.text_operation_date))
                    .setText(localDateFormat.format(event.getDate()));
            container.addView(view);
        }
    }

    private int getOperationTypeTextResource(PortalEvent.OperationType type){
        switch (type){
            case SUBMISSION:
                return R.string.operation_type_submission;
            case EDIT:
                return R.string.operation_type_edit;
            case INVALID:
                return R.string.operation_type_invalid;
            default:
                return R.string.default_operation_type;
        }
    }

    private int getOperationResultTextResource(PortalEvent.OperationResult result){
        switch (result){
            case PROPOSED:
                return R.string.operation_result_proposed;
            case ACCEPTED:
                return R.string.operation_result_accepted;
            case REJECTED:
                return R.string.operation_result_rejected;
            case DUPLICATE:
                return R.string.operation_result_duplicate;
            default:
                return R.string.default_operation_result;
        }
    }

    private int getStatusTextResource(PortalDetail portalDetail){
        if (!portalDetail.isReviewed())
            return R.string.waiting;
        else if (portalDetail.isAccepted())
            return R.string.accept;
        else if (portalDetail.isRejected())
            return R.string.rejected;
        else
            return R.string.default_status;
    }

    private String getDateDiffStr(Date date){
        long diff = new Date().getTime() - date.getTime();
        int dayDiff = Math.round(diff / 1000 / 3600 / 24);
        return Integer.toString(dayDiff);
    }

    public interface OnFragmentInteractionListener {
        public PortalDetail getSelectedPortal();
        public void onUpButtonClicked();
    }


}
