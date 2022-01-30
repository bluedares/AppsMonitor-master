package com.eterno.appmonitor.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.eterno.appmonitor.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

  private LayoutInflater inflater;
  private ArrayList<NotificationItem> notificationList = new ArrayList<>();

  public NotificationAdapter(Context context) {
    inflater = LayoutInflater.from(context);
  }

  public void addNotification(NotificationItem notificationItem) {
    notificationList.add(notificationItem);
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public NotificationAdapter.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = inflater.inflate(R.layout.item_notification_detail, parent, false);
    return new NotificationViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull NotificationAdapter.NotificationViewHolder holder, int position) {
    NotificationItem item = notificationList.get(position);
    holder.name.setText(item.getPackageName());
  }

  @Override
  public int getItemCount() {
    return notificationList.size();
  }

  class NotificationViewHolder extends RecyclerView.ViewHolder {

    TextView name;
    LinearLayout mLayout;

    NotificationViewHolder(View itemView) {
      super(itemView);
      name = itemView.findViewById(R.id.name);
      mLayout = itemView.findViewById(R.id.layout);
    }
  }

}
