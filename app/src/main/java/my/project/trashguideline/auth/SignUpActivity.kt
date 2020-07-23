package my.project.trashguideline.auth

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase
import my.project.trashguideline.MYApplication.Companion.auth
import my.project.trashguideline.R
import my.project.trashguideline.base.BaseActivity
import my.project.trashguideline.model.User
import my.project.trashguideline.utils.Const
import my.project.trashguideline.utils.bindView

class SignUpActivity : BaseActivity() {
    private val etName: TextView by bindView(R.id.et_name)
    private val etEamil: TextView by bindView(R.id.et_email)
    private val etPassword: TextView by bindView(R.id.et_password)
    private val btnSignUp: Button by bindView(R.id.btn_user_signup)

    private val loadingProgress: SpinKitView by bindView(R.id.loading)

    override fun getLayoutId(): Int = R.layout.activity_sign_up
    override fun onInitView() {
        auth = FirebaseAuth.getInstance()
    }

    override fun setListener() {
        btnSignUp.setOnClickListener {
            //회원가입 버튼 클릭시

            if (isEmpty(etName.text.toString()) || isEmpty(etEamil.text.toString()) || isEmpty(
                    etPassword.text.toString()
                )
            ) {
                simpleToast("빈칸 없이 입력해 주세요.")
                return@setOnClickListener
            }

            //빈칸 체크후 User 객체에 회원정보를 넣은후 계정을 생성시도를 한다.
            val user = User(etName.text.toString(),etEamil.text.toString(), etPassword.text.toString())
            createAccount(user)

        }
    }

    private fun createAccount(user: User) {
        //계정 생성
        loadingProgress.visibility = View.VISIBLE
        auth.fetchSignInMethodsForEmail(user.email).addOnCompleteListener { task ->
            //회원가입할 계정의 중복검사를 한다.
            if (task.isSuccessful) {
                task.result?.let {
                    var result = it.signInMethods!!.isEmpty()
                    //result 값이 false면 이미 있는 계정 true면 없는계정

                    if (!result) {
                        loadingProgress.visibility = View.GONE
                        simpleToast("이미 가입된 계정입니다.")
                    } else {
                        //서버에 계정이없다면? 정상적으로 회원가입 로직을 수행한다.
                        auth.createUserWithEmailAndPassword(
                            user.email,
                            user.password
                        )
                            .addOnCompleteListener { response ->
                                if (response.isSuccessful) {
                                    registerAccount(response,user)
                                }
                            }.addOnFailureListener { error ->
                                loadingProgress.visibility = View.GONE
                                when (error) {
                                    is FirebaseAuthWeakPasswordException -> simpleToast("비밀번호를 최소 6자 이상 입력해주세요")
                                    else -> simpleToast("알수없는 오류로 실패 하였습니다. 다시 시도해주세요.")
                                }
                            }
                    }
                }
            }
        }
    }

    private fun registerAccount(auth: Task<AuthResult>?, user: User) {
        auth?.let {
            var uid = auth.result?.user?.uid
            if (uid != null) {
                FirebaseDatabase.getInstance().reference.child(Const.DB_USERS).child(uid).setValue(user)
                    .addOnCompleteListener {
                        loadingProgress.visibility = View.GONE
                        simpleToast("회원 가입에 성공 하였습니다.")
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
            }
        }


    }

}