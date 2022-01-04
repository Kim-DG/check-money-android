package com.checkmoney

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

class SubscriptionProfileAdaptor(
    private val context: Context,
    private val accountId: Int,
) : RecyclerView.Adapter<SubscriptionProfileAdaptor.ViewHolder>() {

    val TAG = "SubscriptionProfileAdapter"
    val TAG2 = "SubscriptionProfileAdapter_API"
    val bearerAccessToken = "Bearer ${tokens.access_token}"
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


        @SuppressLint("NotifyDataSetChanged", "SetTextI18n", "SimpleDateFormat")
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

            itemView.setOnClickListener {
                val dlg = Dialog(context)
                dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
                dlg.setContentView(R.layout.dialog_datepicker_subs_edit)     //다이얼로그에 사용할 xml 파일을 불러옴
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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

                val adapter = ArrayAdapter(context, R.layout.spinner_layout, SpinnerArray.sData)
                spinner.adapter = adapter

                /*
                val adapter2 = ArrayAdapter(context, R.layout.spinner_layout, SpinnerArray.sData2)
                spinner2.adapter = adapter2

                 */

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

                //var is_consumtion = 0
                var category = 0

                val longTime = System.currentTimeMillis()
                val dateTime = Date(longTime)

                val yf = SimpleDateFormat("yyyy")
                val mf = SimpleDateFormat("MM")
                val qf = SimpleDateFormat("dd")

                val strYear = yf.format(dateTime)
                val strMonth = mf.format(dateTime)
                val strDay = qf.format(dateTime)

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

                /*
                spinner2.setSelection(1)
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
                */

                btn_edit.setOnClickListener {
                    if (et_detail.text.toString() == "" || et_price.text.toString() == "") {
                        text_alarm.text = "사용내역과 금액을 입력해 주세요."
                    } else {
                        putSubscriptions(bearerAccessToken,item,EditTransaction(
                            is_consumption = 1,
                            price = et_price.text.toString().toInt(),
                            detail = et_detail.text.toString(),
                            date = "${year.value}-${month.value}-${day.value}",
                            category = category
                        ))
                        SubsProfileDataList.datas[adapterPosition].category = category
                        SubsProfileDataList.datas[adapterPosition].date = "${year.value}-${month.value}-${day.value}"
                        SubsProfileDataList.datas[adapterPosition].detail = et_detail.text.toString()
                        SubsProfileDataList.datas[adapterPosition].price = et_price.text.toString().toInt()
                        this@SubscriptionProfileAdaptor.notifyDataSetChanged()
                        dlg.dismiss()
                    }
                }
                btn_delete.setOnClickListener {
                    val deleteDlg = Dialog(context)
                    deleteDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
                    deleteDlg.setContentView(R.layout.dialog_wallet_delete)     //다이얼로그에 사용할 xml 파일을 불러옴
                    deleteDlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    deleteDlg.show()

                    val btnDeleteConfirm = deleteDlg.findViewById<Button>(R.id.btn_delete)
                    val btnDeleteCancel = deleteDlg.findViewById<Button>(R.id.btn_cancel)

                    btnDeleteConfirm.setOnClickListener {
                        deleteSubscriptions(bearerAccessToken,item)
                        SubsProfileDataList.datas.remove(datas[adapterPosition])
                        this@SubscriptionProfileAdaptor.notifyDataSetChanged()
                        deleteDlg.dismiss()
                        dlg.dismiss()
                    }

                    btnDeleteCancel.setOnClickListener {
                        deleteDlg.dismiss()
                    }
                }
                btn_cancle.setOnClickListener {
                    dlg.dismiss()
                }
            }
        }
    }

    //-----------------------------------------------------------------------
    //                            Rest Api function
    //-----------------------------------------------------------------------
    // 내역 수정
    private fun putSubscriptions(accessToken: String, item: TransactionModel, subscription: EditTransaction) {
        RetrofitBuild.api.putSubscriptions(accessToken, accountId, item.id, subscription).enqueue(object :
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

    // 내역 삭제
    private fun deleteSubscriptions(accessToken: String, item: TransactionModel) {
        RetrofitBuild.api.deleteSubscriptions(accessToken, accountId, item.id).enqueue(object :
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
}

