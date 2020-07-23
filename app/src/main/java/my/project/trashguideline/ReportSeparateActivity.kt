package my.project.trashguideline

import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ybq.android.spinkit.SpinKitView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_guideline_page.*
import my.project.trashguideline.base.BaseActivity
import my.project.trashguideline.model.Report
import my.project.trashguideline.report.ReportSepareateAdapter
import my.project.trashguideline.utils.Const
import my.project.trashguideline.utils.bindView

class ReportSeparateActivity : BaseActivity() {
    private val progress: ProgressBar by bindView(R.id.progressBar)
    private val voteRecyclerView: RecyclerView by bindView(R.id.recyclerView)


    private lateinit var databaseReportReference: DatabaseReference
    private lateinit var queryReport: Query
    private var seq: String? = null


    private var reportAdapter: ReportSepareateAdapter? = null

    override fun getLayoutId(): Int = R.layout.activity_report_separeate

    override fun onInitView() {

        var intent = intent
        seq = intent.getStringExtra("EXTRA_REPORT_SEQ")

        setupUI();
        setDataBase()
        setDataBaseListener()
    }

    override fun setListener() {

    }


    private fun setDataBase() {
        databaseReportReference = FirebaseDatabase.getInstance().reference.child(Const.DB_REPORT)
        queryReport = databaseReportReference.orderByChild("boardChildKey").equalTo(seq)
    }

    private fun setDataBaseListener() {
        progress.visibility = View.VISIBLE
        //현재 올려진 게시글을 먼저 데이터를 내려받고. 글쓴이정보를 내려받는다.
        queryReport.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progress.visibility = View.GONE
                var items: ArrayList<Report> = ArrayList()

                for (item in dataSnapshot.children) {
                    val report = item.getValue(Report::class.java) ?: return
                    items.add(report)
                }
                renderList(items)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progress.visibility = View.GONE
            }

        })
    }


    private fun setupUI() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        reportAdapter = object : ReportSepareateAdapter(arrayListOf()) {
            override fun moveMaker(report: Report) {

            }

        }

        voteRecyclerView.run {
            layoutManager = LinearLayoutManager(this@ReportSeparateActivity)
            addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
            isNestedScrollingEnabled = false
            adapter = reportAdapter;
        }
    }

    private fun renderList(votes: List<Report>) {
        reportAdapter?.addData(votes)
        reportAdapter?.notifyDataSetChanged()

    }

}