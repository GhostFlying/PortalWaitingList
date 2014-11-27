package com.ghostflying.ingressmailanalysis;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ghostflying.ingressmailanalysis.data.PortalDetail;
import com.ghostflying.ingressmailanalysis.data.PortalEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ghostflying on 11/22/14.
 * <br>
 * Adapter for RecyclerView
 */
public class PortalListAdapter extends RecyclerView.Adapter<PortalListAdapter.ViewHolder>{
    ArrayList<PortalDetail> dataSet;
    SimpleDateFormat dateFormat;
    Date dateNow;

    public PortalListAdapter (ArrayList<PortalDetail> dataSet){
        this.dataSet = dataSet;
        dateFormat = new SimpleDateFormat("EEE, d MMM yyyy");
        dateNow = new Date();
    }

    /**
     * Create item view for each item.
     * @param viewGroup the view group
     * @param i item's position in recyclerview.
     * @return new ViewHolder defined by inner class.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.portal_list_item, viewGroup, false);
        return new ViewHolder(v);
    }

    /**
     * Set item view's value by the dataset.
     * @param viewHolder    the view holder for each item view.
     * @param position      the position of data in dataset.
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // get data.
        PortalDetail detail = dataSet.get(position);
        viewHolder.portalName.setText(detail.getName());
        viewHolder.portalLastUpdated.setText(getDateDiffStr(detail.getLastUpdated()));
        // set the event list
        ArrayList<PortalEvent> events = detail.getEvents();
        viewHolder.setEventCount(events.size());
        for (int i = 0; i < events.size(); i++){
            viewHolder.setEventIcon(i, getEventIcon(events.get(i).getOperationType(),
                    events.get(i).getOperationResult()));
            viewHolder.setEventDate(i, dateFormat.format(events.get(i).getDate()));
        }
    }

    private String getDateDiffStr(Date date){
        long diff = dateNow.getTime() - date.getTime();
        int dayDiff = Math.round(diff / 1000 / 3600 / 24);
        return Integer.toString(dayDiff);
    }

    /**
     * Get the icon drawable resource id by operation type and result.
     * @param operationType     the operation type.
     * @param operationResult   the operation result.
     * @return  the resource id.
     */
    private int getEventIcon(PortalEvent.OperationType operationType, PortalEvent.OperationResult operationResult){
        switch (operationResult){
            case PASSED:
                return R.drawable.ic_accepted;
            case REJECTED:
                return R.drawable.ic_rejected;
            case PROPOSED:
                switch (operationType){
                    case EDIT:
                        return R.drawable.ic_edit;
                    case SUBMIT:
                        return R.drawable.ic_proposed;
                }
            default:
                return R.drawable.ic_proposed;
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    /**
     * The class to hold each item view, offer interface to adjust item view.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{

        private static final int INITIAL_EVENT_NUMBER = 1;

        public TextView portalName;
        public TextView portalLastUpdated;
        public LinearLayout portalEventList;
        public ArrayList<View> portalEventItems;


        public ViewHolder(View itemView) {
            super(itemView);
            portalName = (TextView)itemView.findViewById(R.id.portal_name);
            portalLastUpdated = (TextView)itemView.findViewById(R.id.portal_last_updated);
            portalEventList = (LinearLayout)itemView.findViewById(R.id.portal_event_list);
            portalEventItems = new ArrayList<View>();
            addEventView(INITIAL_EVENT_NUMBER);
        }

        private void addEventView(int number){
            for (int i = 0; i < number; i++){
                View eventView = LayoutInflater.from(portalEventList.getContext())
                        .inflate(R.layout.portal_event_list_item, portalEventList, false);
                portalEventItems.add(eventView);
                portalEventList.addView(eventView);
            }
        }

        public void setEventCount(int totalCount){
            //add all needed view
            if (portalEventItems.size() < totalCount){
                addEventView(totalCount - portalEventItems.size());
            }

            //set all views' visible
            for (int i = 0; i < portalEventItems.size(); i++){
                if (i < totalCount)
                    portalEventItems.get(i).setVisibility(View.VISIBLE);
                else
                    portalEventItems.get(i).setVisibility(View.GONE);
            }
        }

        public void setEventIcon(int index, int drawableId){
            ((ImageView)portalEventItems.get(index).findViewById(R.id.event_type_image)).setImageResource(drawableId);
        }

        public void setEventDate(int index, String dateStr){
            ((TextView)portalEventItems.get(index).findViewById(R.id.event_date_text)).setText(dateStr);
        }
    }
}
