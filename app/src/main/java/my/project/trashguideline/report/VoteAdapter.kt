package my.project.trashguideline.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_report_item.view.btn_apply
import kotlinx.android.synthetic.main.view_report_item.view.btn_deny
import kotlinx.android.synthetic.main.view_report_item.view.tv_ping_sub
import kotlinx.android.synthetic.main.view_report_item.view.tv_ping_title
import kotlinx.android.synthetic.main.view_vote_item.view.*
import my.project.trashguideline.R
import my.project.trashguideline.model.Report
import my.project.trashguideline.model.Vote
import my.project.trashguideline.utils.L

abstract class VoteAdapter(private val items: ArrayList<Vote>) :
    RecyclerView.Adapter<VoteAdapter.DataViewHolder>() {

    abstract fun moveMaker(vote: Vote)
    abstract fun selectedDeny(report: Report, position: Int)
    abstract fun selectedSuccess(report: Report, position: Int)
    abstract fun selectedReport(vote: Vote)

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(vote: Vote) {
            var report = vote.report
            var voteInterval = vote.timeInterval
            itemView.tv_ping_title.text = voteInterval
            itemView.tv_ping_sub.text = "좌표 정보 " + report.latitude + " , " + report.longitude

            setPercent(itemView, report)
        }

        private fun setPercent(itemView: View, report: Report) {

            var percent = report.voteUser.size
            var agree: Int = 0;
            var disagress: Int = 0;
            var agreePercent: Double = 0.0
            var disAgreePercent: Double = 0.0

            for ((key) in report.voteUser.entries) {
                var isVoteResult = report.voteUser[key]!!
                if (isVoteResult) {
                    agree += 1;
                } else {
                    disagress += 1;
                }
            }


            if (agree != 0) {
                agreePercent = (agree.toDouble() / percent.toDouble())
            }



            if (disagress != 0) {
                disAgreePercent = (disagress.toDouble() / percent.toDouble())
            }

            itemView.tv_apply_percent.text = (agreePercent * 100).toString() + "%"
            itemView.tv_deny_percent.text = (disAgreePercent * 100).toString() + "%"
            itemView.proressbar_percent.progress = (agreePercent * 100).toInt()
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_vote_item, parent, false)
        return DataViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(items[position])

        holder.itemView.btn_view_report.setOnClickListener {
            selectedReport(items[position])
        }

        holder.itemView.btn_apply.setOnClickListener {
            selectedSuccess(items[position].report, position)
        }
        holder.itemView.btn_deny.setOnClickListener {
            selectedDeny(items[position].report, position)
        }

        holder.itemView.setOnClickListener {
            moveMaker(items[position])
        }
    }


    fun addData(list: List<Vote>) {
        items.clear()
        items.addAll(list)
    }

    fun removeItem(position: Int) {
        if (position < this.items.size) {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, this.items.size)
        }
    }

}

