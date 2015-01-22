package com.ghostflying.portalwaitinglist.recyclerviewHelper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ghostflying.portalwaitinglist.R;
import com.ghostflying.portalwaitinglist.model.PortalDetail;
import com.ghostflying.portalwaitinglist.model.PortalEvent;
import com.ghostflying.portalwaitinglist.util.SettingUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ghostflying on 11/22/14.
 * <br>
 * Adapter for RecyclerView
 */
public class PortalListAdapter extends RecyclerView.Adapter<PortalListAdapter.ViewHolder>{
    public List<PortalDetail> dataSet;
    DateFormat localeDateFormat;
    Date dateNow;
    View.OnClickListener onClickListener;

    public PortalListAdapter (List<PortalDetail> dataSet){
        this.dataSet = dataSet;
        localeDateFormat = DateFormat.getDateInstance(DateFormat.FULL);
        dateNow = new Date();
    }

    public void setDataSet(List<PortalDetail> data){
        dataSet = data;
        notifyDataSetChanged();
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
        if (onClickListener != null)
            v.setOnClickListener(onClickListener);
        return new ViewHolder(v);
    }

    /**
     * Set the callback of clock event.
     * @param onClickListener   the callback.
     */
    public void setOnItemClickListener(View.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
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
        List<PortalEvent> events = detail.getEvents();
        viewHolder.setEventCount(events.size());
        for (int i = 0; i < events.size(); i++){
            viewHolder.setEventIcon(i, getEventIcon(events.get(i).getOperationType(),
                    events.get(i).getOperationResult()));
            viewHolder.setEventDate(i, localeDateFormat.format(events.get(i).getDate()));
        }
        // set the status icon in title
        if (SettingUtil.getShowStatusInList()){
            viewHolder.portalStatus.setImageResource(
                    getStatusIcon(events.get(events.size() - 1).getOperationResult())
            );
            viewHolder.portalStatus.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.portalStatus.setVisibility(View.GONE);
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
            case ACCEPTED:
                return R.drawable.ic_accepted;
            case REJECTED:
                return R.drawable.ic_rejected;
            case DUPLICATE:
                return R.drawable.ic_rejected;
            case PROPOSED:
                switch (operationType){
                    case EDIT:
                        return R.drawable.ic_edit;
                    case SUBMISSION:
                        return R.drawable.ic_proposed;
                    case INVALID:
                        return R.drawable.ic_edit;
                }
            default:
                return R.drawable.ic_proposed;
        }
    }

    /**
     * Get the icon drawable resource id by operation result. The method only return accepted,
     * rejected or waiting icon.
     * @param operationResult   the operation result.
     * @return  the resource id.
     */
    private int getStatusIcon(PortalEvent.OperationResult operationResult){
        switch (operationResult){
            case ACCEPTED:
                return R.drawable.ic_accepted;
            case REJECTED:
                return R.drawable.ic_rejected;
            case DUPLICATE:
                return R.drawable.ic_rejected;
            case PROPOSED:
                return R.drawable.ic_waiting;
            default:
                return R.drawable.ic_waiting;
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
        public ImageView portalStatus;
        public LinearLayout portalEventList;
        public ArrayList<View> portalEventItems;


        public ViewHolder(View itemView) {
            super(itemView);
            portalName = (TextView)itemView.findViewById(R.id.portal_name);
            portalLastUpdated = (TextView)itemView.findViewById(R.id.portal_last_updated);
            portalStatus = (ImageView)itemView.findViewById(R.id.portal_status_in_list);
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
