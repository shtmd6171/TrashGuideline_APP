package my.project.trashguideline.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BoardLocation(
    var addedByUser : String,
    var key: String,
    var latitude: Double,
    var longitude: Double
) : Parcelable {
    constructor() : this ("","",0.0,0.0)
}