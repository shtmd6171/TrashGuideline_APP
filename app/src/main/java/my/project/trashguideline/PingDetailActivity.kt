package my.project.trashguideline

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.ybq.android.spinkit.SpinKitView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import my.project.trashguideline.MYApplication.Companion.auth
import my.project.trashguideline.base.BaseActivity
import my.project.trashguideline.model.Board
import my.project.trashguideline.model.BoardLocation
import my.project.trashguideline.model.Report
import my.project.trashguideline.model.User
import my.project.trashguideline.utils.Const
import my.project.trashguideline.utils.L
import my.project.trashguideline.utils.bindView
import my.project.trashguideline.utils.getConvertDate
import my.project.trashguideline.utils.rx.RXCall
import my.project.trashguideline.utils.rx.RXHelper

class PingDetailActivity : BaseActivity() {
    private val ivPhoto: ImageView by bindView(R.id.uploaded_image)
    private val tvAddedUser: TextView by bindView(R.id.tv_addedbyuser)
    private val tvRecommend: TextView by bindView(R.id.tv_recommend)
    private val tvReport: TextView by bindView(R.id.tv_report)
    private val tvContent: TextView by bindView(R.id.tv_content)
    private val tvBackPoress: TextView by bindView(R.id.cancel_btn)
    private val tvDelete: TextView by bindView(R.id.cancel_delete)
    private val loadingProgress: SpinKitView by bindView(R.id.loading)

    private val viewRecommendContainer: RelativeLayout by bindView(R.id.view_recommend)
    private val viewReportContainer: RelativeLayout by bindView(R.id.view_report)

    private lateinit var mCurrentBoardLocation: BoardLocation
    private lateinit var databaseUserReference: DatabaseReference
    private lateinit var databaseBoardReference: DatabaseReference
    private lateinit var databaseReportReference: DatabaseReference
    private lateinit var imageStorageReference: StorageReference

    private var currentBoard: Board? = null

    private var reportDialog: AlertDialog? = null

    lateinit var mGlideRequestManager: RequestManager

    override fun getLayoutId(): Int = R.layout.activity_ping_detail

    override fun onInitView() {

        var intent = intent
        mCurrentBoardLocation = intent.getParcelableExtra("selected_board")
        auth = FirebaseAuth.getInstance()
        mGlideRequestManager = Glide.with(this)


        setVisibleDelete()
        setDataBase()
        setDataBaseListener()
    }

    private fun setVisibleDelete() =
        if (auth.currentUser?.uid == mCurrentBoardLocation.addedByUser) {
            tvDelete.visibility = View.VISIBLE
        } else {
            tvDelete.visibility = View.GONE
        }

    private fun setDataBase() {
        databaseBoardReference = FirebaseDatabase.getInstance().reference.child(Const.DB_BOARD)
            .child(mCurrentBoardLocation.key)
        databaseUserReference = FirebaseDatabase.getInstance().reference.child(Const.DB_USERS)

        databaseReportReference = FirebaseDatabase.getInstance().reference.child(Const.DB_REPORT)

        imageStorageReference =
            FirebaseStorage.getInstance().getReferenceFromUrl(Const.TRASHGUIDE_FIREBASE_STORAGE)
                .child(Const.IMAGE_BOARD)

    }

    private fun setDataBaseListener() {
        loadingProgress.visibility = View.VISIBLE

        //현재 올려진 게시글을 먼저 데이터를 내려받고. 글쓴이정보를 내려받는다.
        databaseBoardReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val board = dataSnapshot.getValue(Board::class.java)
                board?.let {

                    databaseUserReference.child(board.addedByUser)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(databaseError: DatabaseError) {

                            }

                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val user = dataSnapshot.getValue(User::class.java)

                                user?.let {
                                    currentBoard = board;
                                    loadingProgress.visibility = View.GONE
                                    tvAddedUser.text = String.format("작성자 : %s", user.name)
                                    tvContent.text = board.content
                                    tvReport.text = String.format("%s", board.reportCount)
                                    tvRecommend.text = String.format("%s", board.recommentCount)

                                    imageStorageReference.child(board.addedByUser)
                                        .child(board.imageUrl)
                                        .downloadUrl.addOnSuccessListener { uri ->
                                        mGlideRequestManager.load(uri).centerCrop()
                                            .diskCacheStrategy(DiskCacheStrategy.ALL).into(ivPhoto)
                                    }

                                } ?: run {

                                }
                            }

                        })
                } ?: run {
                    loadingProgress.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }

        })
    }

    override fun setListener() {
        tvBackPoress.setOnClickListener {
            onBackPressed()
        }

        tvDelete.setOnClickListener {
            FirebaseDatabase.getInstance().reference.child(Const.DB_BOARD)
                .child(mCurrentBoardLocation.key).removeValue()
            setResult(Activity.RESULT_OK)
            finish()
        }

        viewReportContainer.setOnClickListener {

            currentBoard?.let {
                //리포트 버튼 누를떄 리포트 다이얼로그창을 활성화 시킨다.
                val builder = AlertDialog.Builder(this)

                val inflater = LayoutInflater.from(this)
                val view: View = inflater.inflate(R.layout.view_report_dialog, null)
                val etContent = view.findViewById<View>(R.id.et_text) as EditText
                val sendButton = view.findViewById<View>(R.id.back_to_loginpage_setting) as Button
                val reportKey =
                    FirebaseDatabase.getInstance().reference.child(Const.DB_REPORT).push().key
                var voteMap = HashMap<String, Boolean>()

                sendButton.setOnClickListener {
                    databaseUserReference.child(auth.currentUser!!.uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(databaseError: DatabaseError) {

                            }

                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val user = dataSnapshot.getValue(User::class.java)

                                user?.let {
                                    reportDialog?.dismiss()

                                    var report = Report(
                                        auth.currentUser!!.uid,
                                        user.name,
                                        currentBoard!!.addedByUser,
                                        currentBoard!!.childKey,
                                        reportKey!!,
                                        getConvertDate(),
                                        etContent.text.toString(),
                                        mCurrentBoardLocation.latitude,
                                        mCurrentBoardLocation.longitude,
                                        System.currentTimeMillis(),
                                        voteMap
                                    )

                                    L.i("report : " + report.toString());



                                    databaseBoardReference.runTransaction(object :
                                        Transaction.Handler {
                                        override fun doTransaction(mutableData: MutableData): Transaction.Result {
                                            val board = mutableData.getValue(Board::class.java)
                                                ?: return Transaction.success(
                                                    mutableData
                                                )

                                            if (board.reportUser.containsKey(auth.currentUser!!.uid)) {

                                                if (board.reportUser[auth.currentUser!!.uid].equals(
                                                        getConvertDate(),
                                                        ignoreCase = true
                                                    )
                                                ) {
                                                    //이미 오늘 투표를 완료 하였다면?
                                                    RXHelper.runOnUiThread(object : RXCall<String> {
                                                        override fun onCall(data: String?) {
                                                            simpleToast("오늘 더이상 리포트를 제출 할수 없습니다.")
                                                        }

                                                        override fun onError(error: Throwable) {
                                                            L.e("::::error : $error")
                                                        }
                                                    })
                                                    return Transaction.success(mutableData)
                                                }

                                                board.reportCount = board.reportCount + 1
                                                board.reportUser[auth.currentUser!!.uid] =
                                                    getConvertDate()
                                            } else {
                                                board.reportCount = board.reportCount + 1
                                                board.reportUser[auth.currentUser!!.uid] =
                                                    getConvertDate()
                                            }
                                            mutableData.value = board

                                            databaseReportReference.child(reportKey)
                                                .setValue(report)

                                            RXHelper.runOnUiThread(object : RXCall<String> {
                                                override fun onCall(data: String?) {
                                                    tvReport.text =
                                                        String.format("%s", board.reportCount)
                                                }

                                                override fun onError(error: Throwable) {
                                                    L.e("::::error : $error")
                                                }
                                            })

                                            return Transaction.success(mutableData)
                                        }

                                        override fun onComplete(
                                            databaseError: DatabaseError?,
                                            p1: Boolean,
                                            dataSnapshot: DataSnapshot?
                                        ) {

                                        }

                                    })

                                } ?: run {

                                }
                            }

                        })


                }

                builder.run {
                    setView(view)
                }
                reportDialog = builder.create()
                reportDialog?.show()
            }

        }

        viewRecommendContainer.setOnClickListener {
            //추천버튼을 누를때마다 활성화 시킨다.
            databaseBoardReference.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val board =
                        mutableData.getValue(Board::class.java) ?: return Transaction.success(
                            mutableData
                        )


                    if (board.recommentUser.containsKey(auth.currentUser!!.uid)) {

                        if (board.recommentUser[auth.currentUser!!.uid].equals(
                                getConvertDate(),
                                ignoreCase = true
                            )
                        ) {
                            //이미 오늘 투표를 완료 하였다면?
                            RXHelper.runOnUiThread(object : RXCall<String> {
                                override fun onCall(data: String?) {
                                    simpleToast("오늘 더이상 투표를 할수 없습니다.")
                                }

                                override fun onError(error: Throwable) {
                                    L.e("::::error : $error")
                                }
                            })
                            return Transaction.success(mutableData)
                        }

                        board.recommentCount = board.recommentCount + 1
                        board.recommentUser[auth.currentUser!!.uid] = getConvertDate()
                    } else {
                        board.recommentCount = board.recommentCount + 1
                        board.recommentUser[auth.currentUser!!.uid] = getConvertDate()
                    }
                    mutableData.value = board

                    return Transaction.success(mutableData)
                }

                override fun onComplete(
                    databaseError: DatabaseError?,
                    p1: Boolean,
                    dataSnapshot: DataSnapshot?
                ) {
                    if (dataSnapshot != null) {
                        val board = dataSnapshot.getValue(Board::class.java) ?: return
                        tvRecommend.text = String.format("%s", board.recommentCount)

                    }

                }

            })

        }
    }

}