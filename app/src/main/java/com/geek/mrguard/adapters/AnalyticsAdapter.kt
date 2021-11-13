package com.geek.mrguard.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.geek.mrguard.R
import com.geek.mrguard.UI.dashBoard.commonUser.DetailAnalyticsForUser
import com.geek.mrguard.data.policeAnalytics.PoliceAnalyticsData
import com.geek.mrguard.databinding.SingleRcvRequestBinding

class AnalyticsAdapter(private val policeDashboardData: PoliceAnalyticsData, private val isPolice: Boolean) :
    RecyclerView.Adapter<AnalyticsAdapter.PoliceAnalyticsViewHolder>() {
    open class PoliceAnalyticsViewHolder(itemView: SingleRcvRequestBinding) :
        ViewHolder(itemView.root) {
        var binding: SingleRcvRequestBinding = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoliceAnalyticsViewHolder {
        val binding: SingleRcvRequestBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.single_rcv_request, parent, false
        )
        return PoliceAnalyticsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PoliceAnalyticsViewHolder, position: Int) {
        val data = policeDashboardData.status[position]
        holder.binding.model = data
        holder.binding.status.text = data.status.uppercase().subSequence(0,0)

        when(data.status){
            "ongoing"->{
                holder.binding.status.text = "O"
                holder.binding.status.background = AppCompatResources.getDrawable(holder.itemView.context,R.color.blue)
            }
            "pending"->{
                holder.binding.status.text = "P"
                holder.binding.status.background = AppCompatResources.getDrawable(holder.itemView.context,R.color.absent_span)
            }
            "completed"->{
                holder.binding.status.text = "C"
                holder.binding.status.background = AppCompatResources.getDrawable(holder.itemView.context,R.color.green)
            }
        }

        try{
            if (isPolice) {
                holder.binding.policeDetail.text = data.victim.phone
            } else {
                Log.e("TAG", "onBindViewHolder: $data")
                holder.binding.policeDetail.text = data.police.phone
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
        holder.binding.selector.setOnClickListener{
            val intent = Intent(holder.itemView.context,DetailAnalyticsForUser::class.java)
            intent.putExtra("req_id",data._id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return policeDashboardData.status.size
    }
}