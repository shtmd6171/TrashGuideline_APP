package my.project.trashguideline

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_guideline_page.*
import my.project.trashguideline.model.Board
import my.project.trashguideline.model.Report
import my.project.trashguideline.report.ReportAdapter
import my.project.trashguideline.utils.*

class GuideLine : Fragment() {


    private val progress: ProgressBar by bindView(R.id.progressBar)
    private val reportListView: RecyclerView by bindView(R.id.recyclerView)
    private var reportAdapter: ReportAdapter? = null
    private lateinit var databaseReportReference: DatabaseReference
    private lateinit var databaseBoardReference: DatabaseReference
    private var mainActivity : MainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = activity as MainActivity?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_guideline_page, container, false)
        ButterKt.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        L.i("::::")
        setDataBase()
        setupUI()


    }

    private fun setDataBase() {
        databaseBoardReference = FirebaseDatabase.getInstance().reference.child(Const.DB_BOARD)
        databaseReportReference = FirebaseDatabase.getInstance().reference.child(Const.DB_REPORT)
    }

    private fun setupUI() {
        recyclerView.layoutManager = LinearLayoutManager(activity)

        //arrayListOf 나 mutableListOf를 통해 초기화를 한다.

        reportAdapter = object : ReportAdapter(arrayListOf()) {
            override fun moveMaker(report: Report) {
                mainActivity?.pushNewFragment(report)
            }

            override fun selectedDeny(report: Report, position: Int) {
                //리포트 화면에서 거절버튼 누를시.. 해당한다.
                L.i("[selectedDeny] $report")
                databaseBoardReference.child(report.boardChildKey)
                    .runTransaction(object : Transaction.Handler {
                        override fun doTransaction(mutableData: MutableData): Transaction.Result {
                            val board = mutableData.getValue(Board::class.java)
                                ?: return Transaction.success(mutableData)
                            board.reportCount = 0
                            mutableData.value = board

                            return Transaction.success(mutableData)
                        }

                        override fun onComplete(
                            databaseError: DatabaseError?,
                            p1: Boolean,
                            dataSnapshot: DataSnapshot?
                        ) {
                            if (dataSnapshot != null) {
                                MessageUtils.showLongToastMsg(activity,"리포트 카운트를 초기화 하였습니다.")
                            }

                        }

                    })
            }

            override fun selectedSuccess(report: Report, position: Int) {
                //리포트 화면에서 승인버튼 누를시.. 해당한다.
                L.i("[selectedSuccess] $report")
                databaseBoardReference.child(report.boardChildKey).removeValue()
                databaseReportReference.child(report.reportChildKey).removeValue()
                reportAdapter?.removeItem(position)
            }


        }

        reportListView.run {
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
            isNestedScrollingEnabled = false
            adapter = reportAdapter;
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        ButterKt.unbind(this)
    }

    public fun onReportLoad() {
        L.i("[onReportLoad]")
        progress.visibility = View.VISIBLE

        databaseReportReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var reportList: ArrayList<Report> = ArrayList()

                for (item in dataSnapshot.children) {
                    val report = item.getValue(Report::class.java)!!
                    reportList.add(report)
                }
                progress.visibility = View.GONE
                renderList(reportList)
            }

            override fun onCancelled(p0: DatabaseError) {
                progress.visibility = View.GONE
            }


        })
    }

    private fun renderList(reports: List<Report>) {
        reportAdapter?.addData(reports)
        reportAdapter?.notifyDataSetChanged()
    }

}