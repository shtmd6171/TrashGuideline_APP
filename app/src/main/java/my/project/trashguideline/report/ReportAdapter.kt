package my.project.trashguideline.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_report_item.view.*
import my.project.trashguideline.R
import my.project.trashguideline.model.Report

abstract class ReportAdapter(private val reports: ArrayList<Report>) :
    RecyclerView.Adapter<ReportAdapter.DataViewHolder>() {

    abstract fun moveMaker(report: Report)
    abstract fun selectedDeny(report: Report,position: Int)
    abstract fun selectedSuccess(report: Report,position: Int)
    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(report: Report) {
            itemView.tv_ping_title.text = "리포트 사유 : " + report.content
            itemView.tv_ping_sub.text = "리포트 시간 : " + report.updateTimeStamp
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_report_item, parent, false)
        return DataViewHolder(itemView)

    }

    override fun getItemCount(): Int {
        return reports.size
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(reports[position])

        holder.itemView.btn_apply.setOnClickListener {
            selectedSuccess(reports[position],position)
        }
        holder.itemView.btn_deny.setOnClickListener {
            selectedDeny(reports[position],position)
        }

        holder.itemView.setOnClickListener {
            moveMaker(reports[position])
        }
    }


    fun addData(list: List<Report>) {
        reports.clear()
        reports.addAll(list)
    }

    fun removeItem(position: Int) {
        if (position < this.reports.size) {
            reports.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, this.reports.size)
        }
    }

}

