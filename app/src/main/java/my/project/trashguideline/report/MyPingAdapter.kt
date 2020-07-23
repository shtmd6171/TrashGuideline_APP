package my.project.trashguideline.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_report_item.view.*
import my.project.trashguideline.R
import my.project.trashguideline.model.Board
import my.project.trashguideline.model.Report

abstract class MyPingAdapter(private val boards: ArrayList<Board>) :
    RecyclerView.Adapter<MyPingAdapter.DataViewHolder>() {

    abstract fun moveMaker(board: Board)

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(board: Board) {
            itemView.tv_ping_title.text = "이름 : " + board.content
            itemView.tv_ping_sub.text = "좌표정보 : " + board.latitude + " | " + board.longitude
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_myping_item, parent, false)
        return DataViewHolder(itemView)

    }

    override fun getItemCount(): Int {
        return boards.size
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(boards[position])
        holder.itemView.setOnClickListener {
            moveMaker(boards[position])
        }
    }


    fun addData(list: List<Board>) {
        boards.clear()
        boards.addAll(list)
    }

    fun removeItem(position: Int) {
        if (position < this.boards.size) {
            boards.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, this.boards.size)
        }
    }

}

