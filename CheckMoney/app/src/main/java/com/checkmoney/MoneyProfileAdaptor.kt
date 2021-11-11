package com.checkmoney

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

class MoneyProfileAdapter(private val context: Context) : RecyclerView.Adapter<MoneyProfileAdapter.ViewHolder>() {

    var datas = mutableListOf<MoneyProfileData>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.price_recycler,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val text_detail: TextView = itemView.findViewById(R.id.text_detail)
        private val text_price: TextView = itemView.findViewById(R.id.text_price)
        private val text_category: TextView = itemView.findViewById(R.id.text_category)

        fun bind(item: MoneyProfileData) {
            val format = DecimalFormat("#,###")
            val price = format.format(item.price)

            text_detail.text = item.detail
            text_price.text = price
            text_category.text = item.category

            itemView.setOnClickListener {
            }
        }
    }
}