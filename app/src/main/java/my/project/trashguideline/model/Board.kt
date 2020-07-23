package my.project.trashguideline.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Board(
    var addedByUser: String,
    var imageUrl: String,
    var content: String,
    var latitude: String,
    var longitude: String,
    var childKey: String,
    var reportUser: HashMap<String, String>,
    var recommentUser: HashMap<String, String>,
    var recommentCount: Int,
    var reportCount : Int
) : Parcelable {
    constructor() : this("", "", "", "", "", "", HashMap(), HashMap(), 0,0)
}