package my.project.trashguideline.listener

import android.location.Location

interface LocationCallback {
    fun callback(location : Location)
}