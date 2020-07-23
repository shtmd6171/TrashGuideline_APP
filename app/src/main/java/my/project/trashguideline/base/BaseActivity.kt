package my.project.trashguideline.base

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import my.project.trashguideline.utils.ButterKt

abstract class BaseActivity : AppCompatActivity() {
    @LayoutRes
    protected abstract fun getLayoutId(): Int

    protected abstract fun onInitView()
    protected abstract fun setListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getLayoutId() != 0) {
            setContentView(getLayoutId())
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        onInitContentView()
    }

    private fun onInitContentView() {
        ButterKt.bind(this)
        onInitView()
        setListener()
    }

    protected fun simpleToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    protected fun isEmpty(text: String): Boolean = text.equals("", ignoreCase = true)

}