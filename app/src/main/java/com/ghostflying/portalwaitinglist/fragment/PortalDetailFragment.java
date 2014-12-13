package com.ghostflying.portalwaitinglist.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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
        PortalDetail clickedPortal = mListener.getSelectedPortal();
        localDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

        setToolbar(view, clickedPortal);

        // set the text of the view.
        ((TextView)view.findViewById(R.id.last_updated_in_detail))
                .setText(getDateDiffStr(clickedPortal.getLastUpdated()));
        ((TextView)view.findViewById(R.id.portal_status_in_detail))
                .setText(getStatusTextResource(clickedPortal));
        String address = clickedPortal.getAddress();
        if (address != null)
            ((TextView)view.findViewById(R.id.portal_address_in_detail))
                    .setText(clickedPortal.getAddress());
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

    private void setToolbar(View v, PortalDetail portalDetail){
        toolbar = (Toolbar)v.findViewById(R.id.action_bar_in_detail);
        ((ActionBarActivity)getActivity()).setSupportActionBar(toolbar);
        getActivity().setTitle(portalDetail.getName());
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            mListener.onUpButtonClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
