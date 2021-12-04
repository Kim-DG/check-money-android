package com.checkmoney

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileAdapter(private val context: Context, private val access_token: String, private val refresh_token: String, private val user_email: String, private val accountid: Int,private  val layout: DrawerLayout) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

    val TAG = "ProfileAdapter"
    val TAG2 = "ProfileAdapter_API"
    val bearerAccessToken = "Bearer $access_token"
    var datas = mutableListOf<ProfileData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.wallet_recycler,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])

    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val text_wname: TextView = itemView.findViewById(R.id.text_wname)
        private val text_description: TextView = itemView.findViewById(R.id.text_description)
        private val text_edit: TextView = itemView.findViewById(R.id.text_edit)
        private val text_delete: TextView = itemView.findViewById(R.id.text_delete)

        @SuppressLint("NotifyDataSetChanged")
        fun bind(item: ProfileData) {
            text_wname.text = item.title
            text_description.text = item.description

            text_wname.setOnClickListener {
                Intent(context, WalletActivity::class.java).apply {
                    layout.closeDrawers()
                    MoneyProfileDataList.datas.clear()
                    putExtra("data",item)
                    putExtra("access_token",access_token)
                    putExtra("refresh_token",refresh_token)
                    putExtra("userId",user_email)
                    putExtra("accountId",item.id)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { context.startActivity(this) }
            }

            text_edit.setOnClickListener {
                val dlg = Dialog(context)
                dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
                dlg.setContentView(R.layout.wallet_edit_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dlg.show()

                val et_editname = dlg.findViewById<EditText>(R.id.et_editname)
                val et_editdescription = dlg.findViewById<EditText>(R.id.et_editdescription)
                val btn_edit = dlg.findViewById<Button>(R.id.btn_edit)
                val btn_cancle = dlg.findViewById<Button>(R.id.btn_cancel)

                btn_edit.setOnClickListener {
                    text_wname.text = "${et_editname?.text}"
                    text_description.text = "${et_editdescription?.text}"
                    item.title = "${et_editname?.text}"
                    item.description = "${et_editdescription?.text}"
                    val account = Account(item.title, item.description)
                    putAccount(account, item)
                    dlg.dismiss()
                }

                btn_cancle.setOnClickListener {
                    dlg.dismiss()
                }
            }

            text_delete.setOnClickListener {
                val dlg = Dialog(context)
                dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
                dlg.setContentView(R.layout.wallet_delete_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dlg.show()

                val btn_delete = dlg.findViewById<Button>(R.id.btn_delete)
                val btn_cancle = dlg.findViewById<Button>(R.id.btn_cancel)

                btn_delete.setOnClickListener {
                    datas.removeAt(adapterPosition)
                    notifyDataSetChanged()
                    deleteAccount(item)
                    dlg.dismiss()
                    if(accountid == item.id){
                        Intent(context, MainActivity::class.java).apply {
                            MoneyProfileDataList.datas.clear()
                            putExtra("access_token",access_token)
                            putExtra("refresh_token",refresh_token)
                            putExtra("userId",user_email)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }.run { context.startActivity(this) }
                    }
                }

                btn_cancle.setOnClickListener {
                    dlg.dismiss()
                }
            }
        }
    }

    private fun putAccount(account: Account, item: ProfileData) {
        RetrofitBuild.api.putAccount(bearerAccessToken, item.id, account).enqueue(object :
            Callback<Result> {
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

    private fun deleteAccount(item: ProfileData) {
        RetrofitBuild.api.deleteAccount(bearerAccessToken, item.id).enqueue(object : Callback<Result> {
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
