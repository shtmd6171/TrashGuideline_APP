package my.project.trashguideline

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.tedpark.tedonactivityresult.rx2.TedRxOnActivityResult
import kotlinx.android.synthetic.main.activity_account_setting.*
import my.project.trashguideline.utils.L

class AccountSettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_setting)

        setupListener()
    }

    private fun setupListener() {
        // onBackPressed() 뒤로가기
        account_setting_back.setOnClickListener { onBackPressed() }
        // 로그아웃
        account_setting_logout.setOnClickListener {
            signOutAccount()
        }
        // 계정탈퇴
        account_setting_delete.setOnClickListener { deleteAccount() }
    }


    private fun signOutAccount() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                setResult(Activity.RESULT_OK)
                finish()
            }
    }

    private fun deleteAccount() {
        TedRxOnActivityResult.with(this)
            .startActivityForResult(Intent(this,AccountDeleteActivity::class.java))
            .subscribe({ activityResult ->
                if (activityResult.resultCode == Activity.RESULT_OK) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }

            }, { error -> L.e("error " + error.message) })

    }

    private fun showDeleteDialog() {
        AccountDeleteDialog().apply {
            addAccountDeleteDialogInterface(object :
                AccountDeleteDialog.AccountDeleteDialogInterface {
                override fun delete() {
                    deleteAccount()
                }

                override fun cancelDelete() {
                }
            })
        }.show(supportFragmentManager, "")
    }

    private fun moveToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
