package my.project.trashguideline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.activity_account_delete_dialog.*

class AccountDeleteDialog : DialogFragment() {

    interface AccountDeleteDialogInterface {
        fun delete()
        fun cancelDelete()
    }

    private var accountDeleteDialogInterface : AccountDeleteDialogInterface? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_account_delete_dialog,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupListener()
    }

    fun addAccountDeleteDialogInterface(listener : AccountDeleteDialogInterface) {
        accountDeleteDialogInterface = listener
    }

    private fun setupListener(){
        delete_no.setOnClickListener {
            accountDeleteDialogInterface?.cancelDelete()
            dismiss()
        }
        delete_yes.setOnClickListener {
            accountDeleteDialogInterface?.delete()
            dismiss()
        }
    }
}
