package com.example.harmonychat.Adapters

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.harmonychat.Models.UserStatus
import com.example.harmonychat.R
import com.example.harmonychat.databinding.RowStatusBinding

class TopStatusAdapter(private val context: Context, private val userStatuses: ArrayList<UserStatus>) :
    RecyclerView.Adapter<TopStatusAdapter.TopStatusViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopStatusViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_status, parent, false)
        return TopStatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopStatusViewHolder, position: Int) {
        val userStatus = userStatuses[position]
        val lastStatus = userStatus.statuses?.get(userStatus.statuses!!.size - 1)

        holder.binding.statususername.text = userStatus.name
        if (lastStatus != null) {
            Glide.with(context).load(lastStatus.imageUrl).into(holder.binding.statusprofile)
        }

        holder.binding.statusprofile.setOnClickListener {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_image)

            val imageView = dialog.findViewById<ImageView>(R.id.dialogImageView)
            if (lastStatus != null) {
                Glide.with(context).load(lastStatus.imageUrl).into(imageView)
            }

            // إضافة استجابة عند النقر على الصورة لإعادتها إلى الحالة
            imageView.setOnClickListener {
                val imageUrl = lastStatus?.imageUrl
                if (imageUrl != null) {
                    // هنا يمكنك إجراء الإجراءات اللازمة لإعادة الصورة إلى الحالة الخاصة بالمستخدم
                    // يمكنك استخدام imageUrl لذلك
                    // قد تحتاج إلى إنشاء نشاط جديد لعرض الصورة أو إجراء إجراءات أخرى.
                }
            }

            dialog.show()
        }
    }

    override fun getItemCount(): Int {
        return userStatuses.size
    }

    inner class TopStatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RowStatusBinding.bind(itemView)
    }
}
