package net.tuurlievens.fuzzyclockscreensaver

import android.annotation.SuppressLint
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.core.graphics.drawable.IconCompat

class NotificationData(
    var packageName: String,
    var count: Int = 1,
    var icon: IconCompat? = null
) : Parcelable {

    @SuppressLint("RestrictedApi")
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val temp = parcel.readParcelable<Parcelable>(IconCompat::class.java.classLoader)
            if (temp != null)
                icon = IconCompat.createFromIcon(temp as Icon)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeInt(count)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            parcel.writeParcelable(if (icon==null) { null } else { icon!!.toIcon() }, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
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