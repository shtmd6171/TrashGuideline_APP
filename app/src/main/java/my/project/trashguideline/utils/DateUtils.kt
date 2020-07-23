package my.project.trashguideline.utils

import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

fun getConvertDate(): String = SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(Date())
fun isMaterUser(): Boolean = FirebaseAuth.getInstance().currentUser!!.uid == "shJNprGZhJhRcyRW7FdTCX38N863"
fun getConvertDate(mils : Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(mils)
