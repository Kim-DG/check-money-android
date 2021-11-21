package com.checkmoney

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.checkmoney.account.CalTotal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MoneyProfileAdapter(private val context: Context, private val calTotal: CalTotal, private val accountId: Int, private val accessToken: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var datas = mutableListOf<MoneyProfileData>()
    val TAG = "MoneyProfileAdapter"
    val TAG2 = "MoneyProfileAdapter_API"
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
            text_date.text = item.date.date.month + "/" + item.date.date.day
        }
    }

    inner class PriceViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val text_detail: TextView = itemView.findViewById(R.id.text_detail)
        private val text_price: TextView = itemView.findViewById(R.id.text_price)
        private val text_category: TextView = itemView.findViewById(R.id.text_category)

        @SuppressLint("NotifyDataSetChanged", "SimpleDateFormat")
        fun bind(item: MoneyProfileData) {
            val format = DecimalFormat("#,###")
            var price = ""
            if (item.is_consumption == 0) {
                price = "+" + format.format(item.price)
                text_price.setTextColor(
                    ContextCompat.getColor(context,
                    R.color.logoBlue
                ))
            }
            else if(item.is_consumption == 1){
                price = "-" + format.format(item.price)
                text_price.setTextColor(
                    ContextCompat.getColor(context,
                        R.color.red
                    ))
            }

            MoneyProfileDataList.datas

            text_detail.text = item.detail
            text_price.text = price
            text_category.text = category.category[item.category]

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

                year.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                month.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                day.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

                //  최소값 설정
                year.minValue = 2021
                month.minValue = 1
                day.minValue = 1

                //  최대값 설정
                year.maxValue = 2025
                month.maxValue = 12
                day.maxValue = 31

                var is_consumtion = 0
                var category = 0

                val c = System.currentTimeMillis()
                val a = Date(c)
                val tf = SimpleDateFormat("yyyyMMddHHmmssSSZZ")
                val time = tf.format(a)

                val yf = SimpleDateFormat("yyyy")
                val mf = SimpleDateFormat("MM")
                val qf = SimpleDateFormat("dd")

                val strYear = yf.format(a)
                val strMonth = mf.format(a)
                val strDay = qf.format(a)

                year.value = strYear.toInt()
                month.value = strMonth.toInt()
                day.value = strDay.toInt()

                spinner.setSelection(0)
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        category = position
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
                            is_consumtion = 1
                        }
                        else
                            is_consumtion = 0
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

                        //동일한 날짜의 내역이 없으면 날짜 삭제
                        if(!MoneyProfileDataList.datas.any{it.date.type == 0 && it.date.date.year == dod.date.date.year && it.date.date.month == dod.date.date.month && it.date.date.day == dod.date.date.day && it.is_consumption == dod.is_consumption}) {
                            val data = MoneyProfileData(
                                is_consumption = dod.is_consumption, price = 0, detail = "", date = DateType(
                                    id = -1, MoneyProfileData.DATE_TYPE,
                                    Date(datas[adapterPosition].date.date.year,
                                    datas[adapterPosition].date.date.month,
                                    datas[adapterPosition].date.date.day,
                                    )
                                ),category = 0,account_id = accountId
                            )
                            if(MoneyProfileDataList.datas.any{it == data}) {
                                MoneyProfileDataList.datas.remove(data)
                            }
                        }

                        MoneyProfileDataList.datas.apply {
                            add(
                                MoneyProfileData(
                                    is_consumption = is_consumtion, price = 0, detail = "", date = DateType(
                                        id = -1, MoneyProfileData.DATE_TYPE,
                                        Date(String.format("%02d", year.value),
                                        String.format("%02d", month.value),
                                        String.format("%02d", day.value))
                                    ), category = 0, account_id = accountId
                                )
                            )
                            add(
                                MoneyProfileData(
                                    is_consumption = is_consumtion, price = et_price.text.toString().toInt(),
                                    detail = et_detail.text.toString(), date = DateType(
                                        id = item.date.id, MoneyProfileData.PRICE_TYPE,
                                        Date(String.format("%02d", year.value),
                                        String.format("%02d", month.value),
                                        String.format("%02d", day.value))
                                    ), category = category, account_id = accountId
                                )
                            )
                            putTransaction(accessToken,item, EditTransaction(is_consumption = is_consumtion, price = et_price.text.toString().toInt(),
                                detail = et_detail.text.toString(), date = "${year.value}-${month.value}-${day.value}",category = category))
                            calTotal.calTotal(item.is_consumption,item.price,is_consumtion,et_price.text.toString().toInt())
                        }

                        MoneyProfileDataList.datas = MoneyProfileDataList.datas.distinct().toMutableList()
                        MoneyProfileDataList.datas.sortWith(compareByDescending<MoneyProfileData> { it.date.date.year }.thenByDescending { it.date.date.month }
                            .thenByDescending { it.date.date.day }.thenByDescending { it.date.type })
                        if(ListType.listype == ListType.TOTAL) {
                            val total_datas_list =
                                MoneyProfileDataList.datas.distinctBy { MoneyProfileData -> MoneyProfileData.date }
                            val filterDatas = total_datas_list.filter {
                                (it.date.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.date.year == (ThisTime.cal.get(
                                    Calendar.YEAR
                                ).toString()))
                            }.toMutableList()
                            this@MoneyProfileAdapter.datas = filterDatas
                            this@MoneyProfileAdapter.notifyDataSetChanged()
                        }
                        else if(ListType.listype == ListType.EXPENSE) {
                            val filterDatas = (MoneyProfileDataList.datas.filter {
                                (it.date.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.date.year == (ThisTime.cal.get(
                                    Calendar.YEAR
                                ).toString())) && (it.is_consumption == 1)
                            }).toMutableList()
                            this@MoneyProfileAdapter.datas = filterDatas
                            this@MoneyProfileAdapter.notifyDataSetChanged()
                        }
                        else {
                            val filterDatas = (MoneyProfileDataList.datas.filter {
                                (it.date.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.date.year == (ThisTime.cal.get(
                                    Calendar.YEAR
                                ).toString())) && (it.is_consumption == 0)
                            }).toMutableList()
                            this@MoneyProfileAdapter.datas = filterDatas
                            this@MoneyProfileAdapter.notifyDataSetChanged()
                        }

                        dlg.dismiss()
                        text_alarm.text = ""
                    }
                }
                btn_delete.setOnClickListener {
                    val deletedlg = Dialog(context)
                    deletedlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
                    deletedlg.setContentView(R.layout.wallet_delete_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴
                    deletedlg.show()

                    val btn_delete_confirm = deletedlg.findViewById<Button>(R.id.btn_delete)
                    val btn_delete_cancle = deletedlg.findViewById<Button>(R.id.btn_cancel)

                    btn_delete_confirm.setOnClickListener {
                        deleteTransaction(accessToken,item)
                        val dod = datas[adapterPosition]
                        MoneyProfileDataList.datas.remove(datas[adapterPosition])

                        if(!MoneyProfileDataList.datas.any{it.date.type == 0 && it.date.date.year == dod.date.date.year && it.date.date.month == dod.date.date.month && it.date.date.day == dod.date.date.day && it.is_consumption == dod.is_consumption}) {
                            val data = MoneyProfileData(
                                is_consumption = dod.is_consumption, price = 0, detail = "",date = DateType(
                                    id = -1, MoneyProfileData.DATE_TYPE,
                                    Date(datas[adapterPosition].date.date.year,
                                        datas[adapterPosition].date.date.month,
                                        datas[adapterPosition].date.date.day)
                                ), category = 0, account_id = accountId
                            )
                            if(MoneyProfileDataList.datas.any{it == data}) {
                                MoneyProfileDataList.datas.remove(data)
                            }
                        }
                        calTotal.calTotal(item.is_consumption,item.price,0,0)
                        val cal = Calendar.getInstance()
                        if(ListType.listype == ListType.TOTAL) {
                            val total_datas_list =
                                MoneyProfileDataList.datas.distinctBy { MoneyProfileData -> MoneyProfileData.date }
                            val filterDatas = total_datas_list.filter {
                                (it.date.date.month == (cal.get(
                                    Calendar.MONTH
                                ) + 1).toString()) && (it.date.date.year == (cal.get(Calendar.YEAR)
                                    .toString()))
                            }.toMutableList()
                            this@MoneyProfileAdapter.datas = filterDatas
                            this@MoneyProfileAdapter.notifyDataSetChanged()
                        }
                        else if(ListType.listype == ListType.EXPENSE) {
                            val filterDatas = (MoneyProfileDataList.datas.filter {
                                (it.date.date.month == (cal.get(Calendar.MONTH) + 1).toString()) && (it.date.date.year == (cal.get(
                                    Calendar.YEAR
                                ).toString())) && (it.is_consumption == 1)
                            }).toMutableList()
                            this@MoneyProfileAdapter.datas = filterDatas
                            this@MoneyProfileAdapter.notifyDataSetChanged()
                        }
                        else {
                            val filterDatas = (MoneyProfileDataList.datas.filter {
                                (it.date.date.month == (cal.get(Calendar.MONTH) + 1).toString()) && (it.date.date.year == (cal.get(
                                    Calendar.YEAR
                                ).toString())) && (it.is_consumption == 0)
                            }).toMutableList()
                            this@MoneyProfileAdapter.datas = filterDatas
                            this@MoneyProfileAdapter.notifyDataSetChanged()
                        }
                        deletedlg.dismiss()
                        dlg.dismiss()
                    }
                    btn_delete_cancle.setOnClickListener {
                        deletedlg.dismiss()
                    }
                }

                btn_cancle.setOnClickListener {
                    dlg.dismiss()
                }

            }
        }
    }

    private fun putTransaction(accessToken: String, item: MoneyProfileData, transaction: EditTransaction) {
        RetrofitBuild.api.putTransaction(accessToken, item.date.id, transaction).enqueue(object :
            Callback<Result> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                } else { // code == 400
                    Log.d(TAG2, "연결실패")
                }
            }
            override fun onFailure(call: Call<Result>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }

    private fun deleteTransaction(accessToken: String, item: MoneyProfileData) {
        RetrofitBuild.api.deleteTransaction(accessToken, item.date.id).enqueue(object : Callback<Result> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                } else { // code == 400
                    Log.d(TAG2, "연결실패")
                }
            }
            override fun onFailure(call: Call<Result>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }
}