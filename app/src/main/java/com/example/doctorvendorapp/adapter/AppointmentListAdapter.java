package com.example.doctorvendorapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorvendorapp.R;
import com.example.doctorvendorapp.pojo.Appointments;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AppointmentListAdapter extends RecyclerView.Adapter<AppointmentListAdapter.AppointmentListAdapterViewHolder> {

    private Context context;
    private List<Appointments> appointmentsList;
    private OnCompleteListener listener;

    public AppointmentListAdapter(Context context, List<Appointments> appointmentsList) {
        this.context = context;
        this.appointmentsList = appointmentsList;
        listener = (OnCompleteListener) context;
    }

    @NonNull
    @Override
    public AppointmentListAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new AppointmentListAdapterViewHolder(LayoutInflater.from(context).inflate(R.layout.appointment_row_model, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentListAdapterViewHolder holder, int i) {

        final Appointments appointment = appointmentsList.get(i);

        Uri photoUri = Uri.parse(appointment.getPatientImageURI());
        String firstName = appointment.getPatientFirstName();
        String lastName = appointment.getPatientLastName();
        String description = appointment.getDescription();
        String area = appointment.getChamberArea();
        String city = appointment.getChamberCity();

        Picasso.get().load(photoUri).into(holder.patientIV);
        holder.nameTV.setText("Mr. "+firstName+" "+lastName);
        holder.descriptionTV.setText("Description:\n"+description);
        holder.addressTV.setText(area+", "+city);
        holder.dateTV.setText(appointment.getAppointmentDate());

        //holder.addressTV.setText(appointment.getDoctorID()+", "+appointment.getAppointmentKey()+", "+appointment.getPatientID());

        holder.appointmentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onDone(appointment.getAppointmentKey(), appointment.getDoctorID(), appointment.getPatientID(), appointment.getAppointmentDate());
            }
        });

    }

    @Override
    public int getItemCount() {
        return appointmentsList.size();
    }

    class AppointmentListAdapterViewHolder extends RecyclerView.ViewHolder{

        ImageView patientIV;
        TextView nameTV, descriptionTV, addressTV, dateTV;
        CardView appointmentLayout;

        public AppointmentListAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            patientIV = itemView.findViewById(R.id.appointmentPatientIV);
            nameTV = itemView.findViewById(R.id.appointmentPatientNameTV);
            descriptionTV = itemView.findViewById(R.id.appointmentPatientDescriptionTV);
            addressTV = itemView.findViewById(R.id.appointmentChamberAddressTV);
            dateTV = itemView.findViewById(R.id.appointmentDateTV);
            appointmentLayout = itemView.findViewById(R.id.appointmentList);
        }
    }

    public interface OnCompleteListener{
        void onDone(String appointmentKey, String doctorID, String patientID, String appointmentDate);
    }
}
