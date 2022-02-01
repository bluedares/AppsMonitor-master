package com.eterno.joshspy.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.eterno.joshspy.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

  private LayoutInflater inflater;
  private ArrayList<NotificationItem> notificationList = new ArrayList<>();
  private boolean showPackageName = false;

  public NotificationAdapter(Context context, boolean showPackageName ) {
    inflater = LayoutInflater.from(context);
    this.showPackageName = showPackageName;
  }

  public void addNotification(NotificationItem notificationItem) {
    notificationList.add(notificationItem);
    notifyDataSetChanged();
  }

  public void setNotificationList(List<NotificationItem> notificationList) {
    this.notificationList.clear();
    this.notificationList.addAll(notificationList);
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
    if(showPackageName) {
      holder.name.setText(item.getPackageName());
      holder.name.setVisibility(View.VISIBLE);
    } else {
      holder.name.setVisibility(View.GONE);
    }
    holder.time.setText(item.getTime());
  }

  @Override
  public int getItemCount() {
    return notificationList.size();
  }

  class NotificationViewHolder extends RecyclerView.ViewHolder {

    TextView name;
    TextView time;
    LinearLayout mLayout;

    NotificationViewHolder(View itemView) {
      super(itemView);
      name = itemView.findViewById(R.id.name);
      time = itemView.findViewById(R.id.time);
      mLayout = itemView.findViewById(R.id.layout);
    }
  }

}
