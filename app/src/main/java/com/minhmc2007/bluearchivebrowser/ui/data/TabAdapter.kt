package com.minhmc2007.bluearchivebrowser.ui.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minhmc2007.bluearchivebrowser.R

class TabAdapter(private val tabs: List<Tab>, private val onItemClick: (Tab) -> Unit) :
    RecyclerView.Adapter<TabAdapter.TabViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return TabViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        val tab = tabs[position]
        holder.bind(tab)
        holder.itemView.setOnClickListener { onItemClick(tab) }
    }

    override fun getItemCount(): Int = tabs.size

    class TabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(tab: Tab) {
            titleTextView.text = tab.title
        }
    }
}
