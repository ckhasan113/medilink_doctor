package com.example.doctorvendorapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorvendorapp.R;
import com.example.doctorvendorapp.pojo.addschedule.AddChamber;

import java.util.List;

public class ChamberListAdapter extends RecyclerView.Adapter<ChamberListAdapter.ChamberListViewHolder> {

    private Context context;
    private List<AddChamber> chamberList;

    private ChamberListAdapterListener listener;

    public ChamberListAdapter(Context context, List<AddChamber> chamberList) {
        this.context = context;
        this.chamberList = chamberList;
        listener = (ChamberListAdapterListener) context;
    }

    @NonNull
    @Override
    public ChamberListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ChamberListViewHolder(LayoutInflater.from(context).inflate(R.layout.schedule_list_row_model, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChamberListViewHolder viewHolder, int i) {

        final AddChamber chamber = chamberList.get(i);

        viewHolder.addressTV.setText(chamber.getArea()+"\n"+chamber.getCity()+", House No. "+chamber.getHouse());
        viewHolder.timeTV.setText(chamber.getStart()+" - "+chamber.getEnd());
        if (chamber.getStatus().equals("pending")){
            viewHolder.statusIV.setImageResource(R.drawable.ic_pending);
        }else if (chamber.getStatus().equals("accepted")){
            viewHolder.statusIV.setImageResource(R.drawable.ic_accepted);
        }else if (chamber.getStatus().equals("rejected")){
            viewHolder.statusIV.setImageResource(R.drawable.ic_cancel);
        }

        viewHolder.chamberLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                listener.onChamberUpdate(chamber);
            }
        });

    }

    @Override
    public int getItemCount() {
        return chamberList.size();
    }

    class ChamberListViewHolder extends RecyclerView.ViewHolder{

        TextView addressTV, timeTV;
        ImageView statusIV;
        CardView chamberLayout;

        public ChamberListViewHolder(@NonNull View itemView) {
            super(itemView);
            addressTV = itemView.findViewById(R.id.chamber_schedule_addressTV);
            timeTV = itemView.findViewById(R.id.chamber_schedule_timeTV);
            statusIV = itemView.findViewById(R.id.booking_schedule_statusIV);
            chamberLayout = itemView.findViewById(R.id.getChamberDetails);
        }
    }

    public interface ChamberListAdapterListener{
        void onChamberUpdate(AddChamber chamber);
    }
}
