package my.project.trashguideline.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.firebase.ui.auth.AuthUI
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tedpark.tedonactivityresult.rx2.TedRxOnActivityResult
import my.project.trashguideline.MYApplication.Companion.auth
import my.project.trashguideline.MainActivity
import my.project.trashguideline.R
import my.project.trashguideline.base.BaseActivity
import my.project.trashguideline.model.User
import my.project.trashguideline.utils.Const
import my.project.trashguideline.utils.L
import my.project.trashguideline.utils.bindView


class LoginActivity : BaseActivity() {

    companion object {
        fun getGoogleSingInOption(context: Context): GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestProfile()
                .requestEmail()
                .build()
    }


    private lateinit var googleSignInClient: GoogleSignInClient

    private val tvLoginError: TextView by bindView(R.id.tv_login_error)
    private val tvSignUp: TextView by bindView(R.id.tv_signup)
    private val tvFindPw: TextView by bindView(R.id.tv_find_fw)
    private val etEamil: TextView by bindView(R.id.et_email)
    private val etPassword: TextView by bindView(R.id.et_password)
    private val loadingProgress: SpinKitView by bindView(R.id.loading)
    private val btnLogin: Button by bindView(R.id.btn_server_login)
    private val btnGoogle: Button by bindView(R.id.btn_google_login)
    private val RC_SIGN_IN = 3000

    override fun getLayoutId(): Int = R.layout.activity_login

    override fun onInitView() {
        L.i(":::LoginActivity $tvLoginError");
        checkPreviewLogin()
        //구글 로그인을 위한 구글 Sign객체를 초기화.
        googleSignInClient = GoogleSignIn.getClient(this, getGoogleSingInOption(this))
        auth = FirebaseAuth.getInstance()
    }

    override fun setListener() {

        tvFindPw.setOnClickListener {
            //비밀번호 찾기 클릭시시
            if(isEmpty(etEamil.text.toString())){
                simpleToast("이메일 설정칸에 이메일을 입력해주세요")
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(etEamil.text.toString())?.addOnCompleteListener(this){
                if(it.isSuccessful){
                    simpleToast("비밀번호 재설정 메일을 보냈습니다.재설정 해주세요.")
                }else{
                    simpleToast("재설정 메일을 보내는데 실패하였습니다.")
                }
            }
        }
        btnLogin.setOnClickListener {
            //로그인 버튼 클릭시 이벤트 처리..
            onClickLogin()
        }

        btnGoogle.setOnClickListener {
            onClickGoogle()
        }

        tvSignUp.setOnClickListener {
            //회원 가입 버튼 클릭시 이벤트 처리..
            TedRxOnActivityResult.with(this)
                .startActivityForResult(Intent(this@LoginActivity, SignUpActivity::class.java))
                .subscribe({ activityResult ->
                    if (activityResult.resultCode == Activity.RESULT_OK) {
                        //Firebase는 회원가입직후 자동 로그인 처리가 되기때문에 따로 로그아웃 처리를 하지않으면, 로그인을 2번할 필요가없음.
                        finish()
                        moveToMainActivity()
                    }

                }, { error -> L.e("error " + error.message) })

        }

    }

    private fun onClickGoogle() {
        //구글 로그인을 시도한다.
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun onClickLogin() {

        loadingProgress.visibility = View.VISIBLE
        var id = etEamil.text.toString()
        var password = etPassword.text.toString()


        FirebaseAuth.getInstance().signInWithEmailAndPassword(id, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    FirebaseDatabase.getInstance()
                        .reference
                        .child("Users")
                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    return
                                }
                                val item = dataSnapshot.getValue(User::class.java)

                                if (item != null) {
                                    loadingProgress.visibility = View.GONE
                                    simpleToast("로그인에 성공하였습니다.")
                                    moveToMainActivity()
                                }


                            }

                        })
                }
            }.addOnFailureListener { error ->
                L.e("error: $error")
                when (error) {
                    is FirebaseAuthInvalidCredentialsException -> simpleToast("비밀번호가 무효하거나 혹은 잘못 입력하였습니다.")
                    else -> simpleToast("알수없는 오류로 실패 하였습니다. 다시 시도해주세요.")
                }

                loadingProgress.visibility = View.GONE
                simpleToast("알수없는 오류로 실패 하였습니다. 다시 시도해주세요.")

            }
    }

    // 이미 로그인한 적이 있으면 (user값이 존재 하면) startActivity를 수행하고
    // user값이 없다면 showLoginWindow()를 통해 로그인 창을 띄움
    private fun checkPreviewLogin() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user?.uid != null) {
            moveToMainActivity()
        }
    }

    private fun moveToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showLoginWindow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.GreenTheme)
                .build(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                simpleToast("로그인 실패, 로그인을 다시 시도해주세요")
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        L.d("firebaseAuthWithGoogle:" + acct.id!!)
        loadingProgress.visibility = View.VISIBLE


        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    L.d("signInWithCredential:success")
                    val user = auth.currentUser
                    val fbUser = User(user?.displayName ?: "", user?.email!!, "000000")
                    FirebaseDatabase.getInstance().reference.child(Const.DB_USERS).child(user.uid)
                        .setValue(fbUser)
                        .addOnCompleteListener {
                            loadingProgress.visibility = View.GONE
                            simpleToast("회원 가입에 성공 하였습니다.")
                            moveToMainActivity()
                        }
                } else {
                    // If sign in fails, display a message to the user.
                    L.w("signInWithCredential:failure " + task.exception)
                    simpleToast("로그인 실패, 로그인을 다시 시도해주세요")
                }
                loadingProgress.visibility = View.GONE
            }
    }
}