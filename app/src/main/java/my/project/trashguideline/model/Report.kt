package my.project.trashguideline.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Report(
    var addedByUser: String,
    var addedByUserName: String,
    var boardWritenUser: String,
    var boardChildKey: String,
    var reportChildKey: String,
    var updateTimeStamp: String,
    var content: String,
    var latitude: Double,
    var longitude: Double,
    var updateTimeMlis: Long,
    var voteUser: HashMap<String, Boolean>
) : Parcelable {
    constructor() : this("", "", "", "", "", "", "", 0.0, 0.0, 0, HashMap())
}