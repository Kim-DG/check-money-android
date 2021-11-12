package com.checkmoney

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

class MoneyProfileAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var datas = mutableListOf<MoneyProfileData>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View?
        return when (viewType){
            MoneyProfileData.PRICE_TYPE -> {
                view = LayoutInflater.from(context).inflate(R.layout.price_recycler, parent, false)
                PriceViewHolder(view)
            }
            MoneyProfileData.DATE_TYPE -> {
                view = LayoutInflater.from(context).inflate(R.layout.date_recycler, parent, false)
                DateViewHolder(view)
            }
            else -> throw RuntimeException("알 수 없는 뷰 타입 에러")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return datas[position].date.type
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val obj = datas[position]

        when (obj.date.type) {
            MoneyProfileData.PRICE_TYPE -> (holder as PriceViewHolder).bind(datas[position])
            MoneyProfileData.DATE_TYPE -> (holder as DateViewHolder).bind(datas[position])
        }
    }

    inner class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val text_date: TextView = itemView.findViewById(R.id.text_date)
        @SuppressLint("SetTextI18n")
        fun bind(item: MoneyProfileData) {
            text_date.text = item.date.month + "/" + item.date.day
        }
    }

    inner class PriceViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val text_detail: TextView = itemView.findViewById(R.id.text_detail)
        private val text_price: TextView = itemView.findViewById(R.id.text_price)
        private val text_category: TextView = itemView.findViewById(R.id.text_category)

        fun bind(item: MoneyProfileData) {
            val format = DecimalFormat("#,###")
            var price = ""
            if (item.positive == "positive") {
                price = "+" + format.format(item.price)
                text_price.setTextColor(
                    ContextCompat.getColor(context,
                    R.color.logoBlue
                ))
            }
            else if(item.positive == "negative"){
                price = "-" + format.format(item.price)
                text_price.setTextColor(
                    ContextCompat.getColor(context,
                        R.color.red
                    ))
            }

            text_detail.text = item.detail
            text_price.text = price
            text_category.text = item.category

            itemView.setOnClickListener {
            }
        }
    }
}