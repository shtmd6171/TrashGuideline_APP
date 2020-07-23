package my.project.trashguideline

import android.app.Activity
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import my.project.trashguideline.MYApplication.Companion.auth
import my.project.trashguideline.base.BaseActivity
import my.project.trashguideline.utils.L
import my.project.trashguideline.utils.bindView

class AccountDeleteActivity : BaseActivity(){

    private val tvCancel: TextView by bindView(R.id.cancel_btn)
    private val tvDeleteAccount: TextView by bindView(R.id.cancel_delete)

    override fun getLayoutId(): Int = R.layout.activity_delete_account

    override fun onInitView() {
        auth = FirebaseAuth.getInstance()
    }

    override fun setListener() {
        tvCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        tvDeleteAccount.setOnClickListener {
            L.i(":::auth " + auth.currentUser);
            auth.currentUser?.delete()?.addOnCompleteListener(this){
                if(it.isSuccessful){
                    simpleToast("계정 탈퇴에 성공하였습니다")
                    setResult(Activity.RESULT_OK)
                    finish()
                }else{
                    simpleToast("계정 탈퇴에 실패하였습니다.")
                }
            }
        }
    }

}