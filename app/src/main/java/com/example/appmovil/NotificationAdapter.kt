package com.example.appmovil

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(private val notifications: List<Notification>) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.iconNotification)
        val title: TextView = view.findViewById(R.id.textTitle)
        val message: TextView = view.findViewById(R.id.textMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.title.text = notification.title
        holder.message.text = notification.message

        if (notification.type == NotificationType.HEART) {
            holder.icon.setImageResource(R.drawable.ic_notifications)
            holder.icon.setColorFilter(android.graphics.Color.parseColor("#E91E63")) // Pink/Red
        } else {
            holder.icon.setImageResource(R.drawable.ic_notifications)
             holder.icon.setColorFilter(android.graphics.Color.parseColor("#2196F3")) // Blue
        }
    }

    override fun getItemCount() = notifications.size
}