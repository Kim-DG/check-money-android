package com.checkmoney

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView

class ProfileAdapter(private val context: Context, private val access_token: String, private val refresh_token: String, private val user_email: String) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

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
        private val text_edit: TextView = itemView.findViewById(R.id.text_edit)
        private val text_delete: TextView = itemView.findViewById(R.id.text_delete)

        @SuppressLint("NotifyDataSetChanged")
        fun bind(item: ProfileData) {
            text_wname.text = item.name

            text_wname.setOnClickListener {
                Intent(context, WalletActivity::class.java).apply {
                    MoneyProfileDataList.datas.clear()
                    putExtra("data",item)
                    putExtra("access_token",access_token)
                    putExtra("refresh_token",refresh_token)
                    putExtra("userId",user_email)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { context.startActivity(this) }
            }

            text_edit.setOnClickListener {
                val dlg = Dialog(context)
                dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
                dlg.setContentView(R.layout.wallet_edit_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴
                dlg.show()

                val et_editname = dlg.findViewById<EditText>(R.id.et_editname)
                val btn_edit = dlg.findViewById<Button>(R.id.btn_edit)
                val btn_cancle = dlg.findViewById<Button>(R.id.btn_cancel)

                btn_edit.setOnClickListener {
                    text_wname.text = "${et_editname?.text}"
                    item.name = "${et_editname?.text}"
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
                dlg.show()

                val btn_delete = dlg.findViewById<Button>(R.id.btn_delete)
                val btn_cancle = dlg.findViewById<Button>(R.id.btn_cancel)

                btn_delete.setOnClickListener {
                    datas.removeAt(adapterPosition)
                    notifyDataSetChanged()
                    dlg.dismiss()
                }

                btn_cancle.setOnClickListener {
                    dlg.dismiss()
                }
            }
        }
    }
}