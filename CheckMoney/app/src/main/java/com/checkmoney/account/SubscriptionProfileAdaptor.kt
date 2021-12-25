package com.checkmoney

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

class SubscriptionProfileAdaptor(
    private val context: Context,
    private val accountId: Int,
    private val access_token: String,
) : RecyclerView.Adapter<SubscriptionProfileAdaptor.ViewHolder>() {

    val TAG = "SubscriptionProfileAdapter"
    val TAG2 = "SubscriptionProfileAdapter_API"
    var datas = mutableListOf<TransactionModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.recycler_subscription, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val textDate: TextView = itemView.findViewById(R.id.text_date)
        private val textDetail: TextView = itemView.findViewById(R.id.text_detail)
        private val textPrice: TextView = itemView.findViewById(R.id.text_price)
        private val textCategory: TextView = itemView.findViewById(R.id.text_category)


        @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
        fun bind(item: TransactionModel) {
            val format = DecimalFormat("#,###")
            var price = ""

            if (item.is_consumption == 0) {
                price = format.format(item.price) + " 원"
                textPrice.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.logoBlue
                    )
                )
            } else if (item.is_consumption == 1) {
                price = format.format(item.price) + " 원"
                textPrice.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.red
                    )
                )
            }
            textDate.text = "매월 " + item.date.split("-")[2] + " 일"
            textCategory.text = category.category[item.category]
            textDetail.text = item.detail
            textPrice.text = price

        }


    }
}

