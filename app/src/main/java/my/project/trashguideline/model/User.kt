package my.project.trashguideline.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var name: String,
    var email: String,
    var password: String
) : Parcelable {
    constructor() : this ("","","")
}
