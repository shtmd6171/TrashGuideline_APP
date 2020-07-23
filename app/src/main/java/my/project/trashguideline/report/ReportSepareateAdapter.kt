package my.project.trashguideline.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_report_item.view.*
import kotlinx.android.synthetic.main.view_report_item.view.tv_ping_sub
import kotlinx.android.synthetic.main.view_report_item.view.tv_ping_title
import kotlinx.android.synthetic.main.view_report_separate_item.view.*
import my.project.trashguideline.R
import my.project.trashguideline.model.Report

abstract class ReportSepareateAdapter(private val reports: ArrayList<Report>) :
    RecyclerView.Adapter<ReportSepareateAdapter.DataViewHolder>() {

    abstract fun moveMaker(report: Report)

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(report: Report) {
            itemView.tv_ping_title.text = report.content
            itemView.tv_ping_sub.text = "작성자:" + report.addedByUserName
            itemView.tv_ping_date.text = report.updateTimeStamp
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_report_separate_item, parent, false)
        return DataViewHolder(itemView)

    }

    override fun getItemCount(): Int {
        return reports.size
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(reports[position])

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

