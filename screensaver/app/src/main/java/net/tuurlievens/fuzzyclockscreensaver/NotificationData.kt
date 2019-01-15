package net.tuurlievens.fuzzyclockscreensaver

import android.graphics.drawable.Icon
import android.os.Build
import android.os.Parcel
import android.os.Parcelable

class NotificationData(
    var packageName: String,
    var count: Int = 1,
    var icon: Icon? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readParcelable(Icon::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeInt(count)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            parcel.writeParcelable(icon, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NotificationData> {
        override fun createFromParcel(parcel: Parcel): NotificationData {
            return NotificationData(parcel)
        }

        override fun newArray(size: Int): Array<NotificationData?> {
            return arrayOfNulls(size)
        }
    }

}