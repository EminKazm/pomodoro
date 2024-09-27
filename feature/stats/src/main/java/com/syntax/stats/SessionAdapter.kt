// SessionAdapter.kt
package com.syntax.stats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.syntax.domain.entities.Session
import com.syntax.stats.R

class SessionAdapter(private val sessions: List<Session>) :
    RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewSession: TextView = itemView.findViewById(R.id.textViewSession)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.textViewSession.text = "Session ID: ${session.id}, " +
                "Work Duration: ${session.workduration} mins, " +
                "Break Duration: ${session.breakduration} mins"
    }

    override fun getItemCount(): Int {
        return sessions.size
    }
}
