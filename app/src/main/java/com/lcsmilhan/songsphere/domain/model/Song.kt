package com.lcsmilhan.songsphere.domain.model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

data class Song(
    val mediaId: String = "",
    val artist: String = "",
    val songName: String = "",
    val songUrl: String = "",
    val imageUrl: String = "",
    var isSelected: Boolean = false
) : Parcelable {

    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readBoolean(),
    )

    override fun describeContents(): Int {
        return 0
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mediaId)
        parcel.writeString(artist)
        parcel.writeString(songName)
        parcel.writeString(songUrl)
        parcel.writeString(imageUrl)
        parcel.writeBoolean(isSelected)
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }

    fun setIsSelected(isPlaying: Boolean) {
        isSelected = isPlaying
    }
}