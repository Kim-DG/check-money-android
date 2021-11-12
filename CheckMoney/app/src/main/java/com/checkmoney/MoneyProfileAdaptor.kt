package com.checkmoney

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

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

        @SuppressLint("NotifyDataSetChanged")
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

            MoneyProfileDataList.datas

            text_detail.text = item.detail
            text_price.text = price
            text_category.text = item.category

            itemView.setOnClickListener {
                val dlg = Dialog(context)
                dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
                dlg.setContentView(R.layout.dialog_datepicker_edit)     //다이얼로그에 사용할 xml 파일을 불러옴
                dlg.show()

                val et_detail = dlg.findViewById<EditText>(R.id.et_detail)
                val et_price = dlg.findViewById<EditText>(R.id.et_price)
                val btn_edit = dlg.findViewById<Button>(R.id.btn_edit)
                val btn_delete = dlg.findViewById<Button>(R.id.btn_delete)
                val btn_cancle = dlg.findViewById<Button>(R.id.btn_cancel)
                val spinner = dlg.findViewById<Spinner>(R.id.spinner)
                val spinner2 = dlg.findViewById<Spinner>(R.id.spinner2)
                val text_alarm = dlg.findViewById<TextView>(R.id.text_alarm)

                val year : NumberPicker = dlg.findViewById(R.id.yearpicker_datepicker)
                val month : NumberPicker = dlg.findViewById(R.id.monthpicker_datepicker)
                val day : NumberPicker = dlg.findViewById(R.id.daypicker_datepicker)

                val adapter = ArrayAdapter(context, android.R.layout.simple_expandable_list_item_1, SpinnerArray.sData)
                spinner.adapter = adapter

                val adapter2 = ArrayAdapter(context,android.R.layout.simple_expandable_list_item_1, SpinnerArray.sData2)
                spinner2.adapter = adapter2

                year.wrapSelectorWheel = false
                month.wrapSelectorWheel = false
                day.wrapSelectorWheel = false

                //  최소값 설정
                year.minValue = 2021
                month.minValue = 1
                day.minValue = 1

                //  최대값 설정
                year.maxValue = 2025
                month.maxValue = 12
                day.maxValue = 31

                var positive = ""
                var category = ""

                val c = System.currentTimeMillis()
                val a = Date(c)
                val tf = SimpleDateFormat("yyyyMMddHHmmssSSZZ")
                val time = tf.format(a)

                spinner.setSelection(0)
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        category = SpinnerArray.sData[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        TODO("Not yet implemented")
                    }
                }

                spinner2.setSelection(0)
                spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if(SpinnerArray.sData2[position] == "지출"){
                            positive = "negative"
                        }
                        else
                            positive = "positive"
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        TODO("Not yet implemented")
                    }
                }
                btn_edit.setOnClickListener {
                    if(et_detail.text.toString() == "" || et_price.text.toString() == ""){
                        text_alarm.text = "사용내역과 금액을 입력해 주세요."
                    }
                    else {
                        val dod = datas[adapterPosition]
                        MoneyProfileDataList.datas.remove(datas[adapterPosition])

                        if(!MoneyProfileDataList.datas.any{it.date.type == 0 && it.date.year == dod.date.year && it.date.month == dod.date.month && it.date.day == dod.date.day && it.positive == dod.positive}) {
                            val data = MoneyProfileData(
                                date = Date(
                                    MoneyProfileData.DATE_TYPE,
                                    datas[adapterPosition].date.year,
                                    datas[adapterPosition].date.month,
                                    datas[adapterPosition].date.day,
                                    ""
                                ),detail = "",positive =dod.positive,price = 0, category =""
                            )
                            if(MoneyProfileDataList.datas.any{it == data}) {
                                MoneyProfileDataList.datas.remove(data)
                            }
                            Log.d("!!!!!!!!!!!!!!!!!!!",MoneyProfileDataList.datas.toString())
                        }

                        MoneyProfileDataList.datas.apply {
                            add(
                                MoneyProfileData(
                                    date = Date(
                                        MoneyProfileData.DATE_TYPE,
                                        String.format("%02d", year.value),
                                        String.format("%02d", month.value),
                                        String.format("%02d", day.value),
                                        ""
                                    ), detail = "", positive = positive,
                                    price = 0, category = ""
                                )
                            )
                            add(
                                MoneyProfileData(
                                    date = Date(
                                        MoneyProfileData.PRICE_TYPE,
                                        String.format("%02d", year.value),
                                        String.format("%02d", month.value),
                                        String.format("%02d", day.value),
                                        time
                                    ), detail = et_detail.text.toString(), positive = positive,
                                    price = et_price.text.toString().toLong(), category = category
                                )
                            )
                        }

                        MoneyProfileDataList.datas = MoneyProfileDataList.datas.distinct().toMutableList()
                        MoneyProfileDataList.datas.sortWith(compareByDescending<MoneyProfileData> { it.date.year }.thenByDescending { it.date.month }
                            .thenByDescending { it.date.day }.thenByDescending { it.date.type })
                        if(ListType.listype == ListType.TOTAL) {
                            Log.d("$$$$$$$$$$$$$$$$$$$$$",MoneyProfileDataList.datas.toString()+ListType.listype.toString())
                            val total_datas_list =
                                MoneyProfileDataList.datas.distinctBy { MoneyProfileData -> MoneyProfileData.date }
                            val filterDatas = total_datas_list.filter {
                                (it.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.year == (ThisTime.cal.get(
                                    Calendar.YEAR
                                ).toString()))
                            }.toMutableList()
                            this@MoneyProfileAdapter.datas = filterDatas
                            this@MoneyProfileAdapter.notifyDataSetChanged()
                        }
                        else if(ListType.listype == ListType.EXPENSE) {
                            val filterDatas = (MoneyProfileDataList.datas.filter {
                                (it.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.year == (ThisTime.cal.get(
                                    Calendar.YEAR
                                ).toString())) && (it.positive == "negative")
                            }).toMutableList()
                            this@MoneyProfileAdapter.datas = filterDatas
                            this@MoneyProfileAdapter.notifyDataSetChanged()
                        }
                        else {
                            val filterDatas = (MoneyProfileDataList.datas.filter {
                                (it.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.year == (ThisTime.cal.get(
                                    Calendar.YEAR
                                ).toString())) && (it.positive == "positive")
                            }).toMutableList()
                            this@MoneyProfileAdapter.datas = filterDatas
                            this@MoneyProfileAdapter.notifyDataSetChanged()
                        }

                        dlg.dismiss()
                        text_alarm.text = ""
                    }
                }
                btn_delete.setOnClickListener {
                    val dod = datas[adapterPosition]
                    MoneyProfileDataList.datas.remove(datas[adapterPosition])

                    if(!MoneyProfileDataList.datas.any{it.date.type == 0 && it.date.year == dod.date.year && it.date.month == dod.date.month && it.date.day == dod.date.day && it.positive == dod.positive}) {
                        val data = MoneyProfileData(
                            date = Date(
                                MoneyProfileData.DATE_TYPE,
                                datas[adapterPosition].date.year,
                                datas[adapterPosition].date.month,
                                datas[adapterPosition].date.day,
                                ""
                            ),detail = "",positive =dod.positive,price = 0, category =""
                        )
                        if(MoneyProfileDataList.datas.any{it == data}) {
                            MoneyProfileDataList.datas.remove(data)
                        }
                        Log.d("!!!!!!!!!!!!!!!!!!!",MoneyProfileDataList.datas.toString())
                    }

                    val cal = Calendar.getInstance()
                    if(ListType.listype == ListType.TOTAL) {
                        val total_datas_list =
                            MoneyProfileDataList.datas.distinctBy { MoneyProfileData -> MoneyProfileData.date }
                        val filterDatas = total_datas_list.filter {
                            (it.date.month == (cal.get(
                                Calendar.MONTH
                            ) + 1).toString()) && (it.date.year == (cal.get(Calendar.YEAR)
                                .toString()))
                        }.toMutableList()
                        this@MoneyProfileAdapter.datas = filterDatas
                        this@MoneyProfileAdapter.notifyDataSetChanged()
                    }
                    else if(ListType.listype == ListType.EXPENSE) {
                        val filterDatas = (MoneyProfileDataList.datas.filter {
                            (it.date.month == (cal.get(Calendar.MONTH) + 1).toString()) && (it.date.year == (cal.get(
                                Calendar.YEAR
                            ).toString())) && (it.positive == "negative")
                        }).toMutableList()
                        this@MoneyProfileAdapter.datas = filterDatas
                        this@MoneyProfileAdapter.notifyDataSetChanged()
                    }
                    else {
                        val filterDatas = (MoneyProfileDataList.datas.filter {
                            (it.date.month == (cal.get(Calendar.MONTH) + 1).toString()) && (it.date.year == (cal.get(
                                Calendar.YEAR
                            ).toString())) && (it.positive == "positive")
                        }).toMutableList()
                        this@MoneyProfileAdapter.datas = filterDatas
                        this@MoneyProfileAdapter.notifyDataSetChanged()
                    }
                    dlg.dismiss()
                }

                btn_cancle.setOnClickListener {
                    dlg.dismiss()
                }

            }
        }
    }

}