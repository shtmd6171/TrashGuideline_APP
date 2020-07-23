package my.project.trashguideline

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.Scheduler
import kotlinx.android.synthetic.main.activity_guideline_page.*
import my.project.trashguideline.MYApplication.Companion.auth
import my.project.trashguideline.model.Report
import my.project.trashguideline.model.Vote
import my.project.trashguideline.report.VoteAdapter
import my.project.trashguideline.utils.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class VoteFragment : Fragment() {


    private val progress: ProgressBar by bindView(R.id.progressBar)
    private val voteRecyclerView: RecyclerView by bindView(R.id.recyclerView)

    private lateinit var databaseReportReference: DatabaseReference
    private var mainActivity: MainActivity? = null
    private var voteAdapter: VoteAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = activity as MainActivity?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vote, container, false)
        ButterKt.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDataBase()
        setupUI()
    }

    private fun setDataBase() {
        auth = FirebaseAuth.getInstance()
        databaseReportReference = FirebaseDatabase.getInstance().reference.child(Const.DB_REPORT)
    }

    private fun setupUI() {
        recyclerView.layoutManager = LinearLayoutManager(activity)

        voteAdapter = object : VoteAdapter(arrayListOf()) {
            override fun moveMaker(vote: Vote) {
                mainActivity?.pushNewFragment(vote.report)
            }

            override fun selectedDeny(report: Report, position: Int) {
                if (!report.voteUser.containsKey(auth.currentUser!!.uid)) {
                    report.voteUser[auth.currentUser!!.uid] = false
                    databaseReportReference.child(report.reportChildKey).setValue(report).addOnCompleteListener {
                        onVoteItemsLoad()
                    }
                }else{
                   MessageUtils.showLongToastMsg(mainActivity,"이미 투표를 하였습니다.")
                }
            }

            override fun selectedSuccess(report: Report, position: Int) {
                if (!report.voteUser.containsKey(auth.currentUser!!.uid)) {
                    report.voteUser[auth.currentUser!!.uid] = true
                    databaseReportReference.child(report.reportChildKey).setValue(report).addOnCompleteListener {
                        onVoteItemsLoad()
                    }
                }else{
                    MessageUtils.showLongToastMsg(mainActivity,"이미 투표를 하였습니다.")
                }
            }

            override fun selectedReport(vote: Vote) {
                startActivity(
                    Intent(
                        activity,
                        ReportSeparateActivity::class.java
                    ).putExtra("EXTRA_REPORT_SEQ", vote.report.boardChildKey)
                )
            }


        }

        voteRecyclerView.run {
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
            isNestedScrollingEnabled = false
            adapter = voteAdapter;
        }
    }


    var reportMap: HashMap<String, MutableList<Report>> = HashMap()

    public fun onVoteItemsLoad() {
        progress.visibility = View.VISIBLE
        databaseReportReference.orderByChild("updateTimeMlis")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    progress.visibility = View.GONE
                    var reportList: MutableList<Report>? = null
                    var items: ArrayList<Vote> = ArrayList()
                    reportMap.clear()

                    for (item in dataSnapshot.children) {
                        val report = item.getValue(Report::class.java) ?: return

                        reportList = reportMap[report.boardChildKey]

                        if (reportList == null) {
                            reportList = arrayListOf()
                        }

                        reportList.add(report)
                        reportMap[report.boardChildKey] = reportList
                        //HashMap key 중복시 value 가 중복 되어 저장된다.
                    }


                    reportMap.entries.toHashSet()

                    for ((key) in reportMap.entries) {
                        val list: MutableList<Report>? = reportMap.get(key)
                        list?.let {
                            var timeInterval = getConvertDate(it[0].updateTimeMlis) + "~" + getConvertDate(it[it.size - 1].updateTimeMlis)
                            items.add(Vote(it[0], timeInterval))
                        }
                    }
                    renderList(items)
                }

                override fun onCancelled(p0: DatabaseError) {
                    progress.visibility = View.GONE
                }


            })
    }

    private fun renderList(votes: List<Vote>) {
        voteAdapter?.addData(votes)
        voteAdapter?.notifyDataSetChanged()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        ButterKt.unbind(this)
    }
}
