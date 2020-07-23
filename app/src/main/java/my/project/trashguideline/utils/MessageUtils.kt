package my.project.trashguideline.utils

import android.R
import android.content.Context
import android.graphics.Paint
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast

//토스트 메세지 Util 클래스
object MessageUtils {
    private var mToast: Toast? = null
    fun showShortToastMsg(context: Context?, msg: String?) {
        showToastMsg(
            context,
            msg,
            Toast.LENGTH_SHORT,
            Gravity.CENTER
        )
    }

    fun showShortToastMsg(context: Context, stringResId: Int) {
        showShortToastMsg(
            context,
            context.getString(stringResId)
        )
    }

    fun showLongToast(context: Context, stringResId: Int) {
        if (mToast != null) {
            mToast!!.cancel()
        }
        mToast =
            Toast.makeText(context, context.getString(stringResId), Toast.LENGTH_LONG)
        val v = mToast?.getView()
            ?.findViewById<View>(R.id.message)
        if (v != null && v is TextView) {
            v.gravity = Gravity.CENTER
        }
        mToast?.setText(context.getString(stringResId))
        mToast?.show()
    }

    fun showLongToastStringFormat(
        context: Context?,
        text: String?
    ) {
        if (mToast != null) {
            mToast!!.cancel()
        }
        mToast =
            Toast.makeText(context, text, Toast.LENGTH_LONG)
        mToast?.show()
    }

    fun showLongToastMsg(context: Context?, msg: String?) {
        showToastMsg(
            context,
            msg,
            Toast.LENGTH_LONG,
            Gravity.CENTER
        )
    }

    fun showLongToastMsg(context: Context, stringResId: Int) {
        showLongToastMsg(
            context,
            context.getString(stringResId)
        )
    }

    fun showToastMsg(
        context: Context?,
        stringResId: Int,
        duration: Int,
        gravity: Int
    ): Toast {
        val toast = Toast.makeText(context, stringResId, duration)
        val root = toast.view
        val v = root.findViewById<View>(R.id.message)
        if (v != null && v is TextView) {
            v.gravity = gravity
        }
        toast.show()
        return toast
    }

    fun showToastMsg(
        context: Context?,
        msg: String?,
        duration: Int,
        gravity: Int
    ) {
        if (mToast != null) {
            mToast!!.cancel()
        }
        if (context == null) return
        mToast = Toast.makeText(context, msg, duration)
        val v = mToast?.getView()
            ?.findViewById<View>(R.id.message)
        if (v != null && v is TextView) {
            v.gravity = gravity
        }
        mToast?.show()
        releaseToast(if (duration == Toast.LENGTH_SHORT) 1000 else 2000.toLong())
    }

    private fun releaseToast(duration: Long) {
        Handler().postDelayed({
            if (mToast != null) {
                mToast!!.cancel()
                mToast = null
            }
        }, duration)
    }

    fun showToastMsg(
        context: Context,
        stringResId: Int,
        duration: Int,
        gravity: Int,
        xOffset: Int,
        yOffset: Int
    ) {
        val toast = Toast.makeText(context, stringResId, duration)
        val v =
            toast.view.findViewById<View>(R.id.message)
        if (v != null && v is TextView) {
            val textView = v
            textView.gravity = Gravity.CENTER
            val paint =
                Paint(Paint.ANTI_ALIAS_FLAG)
            paint.textSize = textView.textSize
            val textSize =
                Math.ceil(paint.measureText(context.getString(stringResId)).toDouble()).toInt()
            textView.layoutParams.width = textSize
        }
        toast.setGravity(gravity, xOffset, yOffset)
        toast.show()
    }

    fun showToastMsg(
        context: Context?,
        message: String?,
        duration: Int,
        gravity: Int,
        xOffset: Int,
        yOffset: Int
    ) {
        val toast = Toast.makeText(context, message, duration)
        val v =
            toast.view.findViewById<View>(R.id.message)
        if (v != null && v is TextView) {
            val textView = v
            textView.gravity = Gravity.CENTER
            val paint =
                Paint(Paint.ANTI_ALIAS_FLAG)
            paint.textSize = textView.textSize
            val textSize = Math.ceil(paint.measureText(message).toDouble()).toInt()
            textView.layoutParams.width = textSize
        }
        toast.setGravity(gravity, xOffset, yOffset)
        toast.show()
    }
}