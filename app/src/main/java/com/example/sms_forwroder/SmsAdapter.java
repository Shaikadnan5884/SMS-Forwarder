package com.example.sms_forwroder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.ViewHolder> {

    private List<SmsMessageModel> messages;
    private String forwardNumber;

    public SmsAdapter(List<SmsMessageModel> messages, String forwardNumber) {
        this.messages = messages;
        this.forwardNumber = forwardNumber;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sms, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SmsMessageModel message = messages.get(position);
        holder.tvMessageBody.setText(message.getBody());
        
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(message.getTimestamp());
        String date = DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();
        
        holder.tvMessageDetails.setText("From: " + message.getSender() + " | " + date);

        holder.btnWhatsApp.setOnClickListener(v -> {
            shareToWhatsApp(v.getContext(), message);
        });
    }

    private void shareToWhatsApp(Context context, SmsMessageModel message) {
        try {
            String text = "Forwarded SMS\nFrom: " + message.getSender() + "\n\n" + message.getBody();
            
            // If we have a stored forward number, we can try to target it
            String url = "https://api.whatsapp.com/send?phone=" + forwardNumber + "&text=" + URLEncoder.encode(text, "UTF-8");
            
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(context, "WhatsApp not installed or error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateData(List<SmsMessageModel> newMessages, String newForwardNumber) {
        this.messages = newMessages;
        this.forwardNumber = newForwardNumber;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageBody, tvMessageDetails;
        ImageButton btnWhatsApp;

        ViewHolder(View itemView) {
            super(itemView);
            tvMessageBody = itemView.findViewById(R.id.tvMessageBody);
            tvMessageDetails = itemView.findViewById(R.id.tvMessageDetails);
            btnWhatsApp = itemView.findViewById(R.id.btnWhatsApp);
        }
    }
}
