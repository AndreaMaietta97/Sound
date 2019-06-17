package com.example.sound

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.item_list.view.*


class ListAdapter(
    private val list: ArrayList<Pair<Int, Int>>,
    val context: Context
) :
    RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_list,
                parent,
                false
            )
        )

    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            holder.dbView.text = list[position].first.toString()
            holder.vmView.text = list[position].second.toString()
        }
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dbView: TextView = view.dbView
        val vmView: TextView = view.vmView
    }
}