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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_guideline_page.*
import my.project.trashguideline.model.Board
import my.project.trashguideline.model.Report
import my.project.trashguideline.report.MyPingAdapter
import my.project.trashguideline.utils.ButterKt
import my.project.trashguideline.utils.Const
import my.project.trashguideline.utils.L
import my.project.trashguideline.utils.bindView

class MyPingFragment : Fragment() {


    private val progress: ProgressBar by bindView(R.id.progressBar)
    private val pingListView: RecyclerView by bindView(R.id.recyclerView)
    private var pingAdapter: MyPingAdapter? = null
    private lateinit var databaseBoardReference: DatabaseReference
    private var mainActivity: MainActivity? = null

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
    private fun filterQuery(req: DatabaseReference, key: String): Query? {
        return req.orderByChild("addedByUser").equalTo(key)
    }

    private fun setDataBase() {
        databaseBoardReference = FirebaseDatabase.getInstance().reference.child(Const.DB_BOARD)
    }

    private fun setupUI() {
        recyclerView.layoutManager = LinearLayoutManager(activity)

        //arrayListOf 나 mutableListOf를 통해 초기화를 한다.

        pingAdapter = object : MyPingAdapter(arrayListOf()) {
            override fun moveMaker(board: Board) {
                mainActivity?.pushNewFragment(board)
            }


        }

        pingListView.run {
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
            isNestedScrollingEnabled = false
            adapter = pingAdapter;
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        ButterKt.unbind(this)
    }

    public fun onMyPingLoad() {
        progress.visibility = View.VISIBLE
        val query = filterQuery(databaseBoardReference, FirebaseAuth.getInstance().currentUser!!.uid)
        query?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var boardList: ArrayList<Board> = ArrayList()

                for (item in dataSnapshot.children) {
                    val board = item.getValue(Board::class.java)!!
                    boardList.add(board)
                }
                progress.visibility = View.GONE
                renderList(boardList)
            }

            override fun onCancelled(p0: DatabaseError) {
                progress.visibility = View.GONE
            }


        })
    }

    private fun renderList(reports: List<Board>) {
        pingAdapter?.addData(reports)
        pingAdapter?.notifyDataSetChanged()
    }

}