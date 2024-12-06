package com.example.aplikasistory.data.response

import android.os.Parcel
import android.os.Parcelable

data class Story(
	val id: String,
	val name: String,
	val description: String,
	val photoUrl: String,
	val createdAt: String
) : Parcelable {
	constructor(parcel: Parcel) : this(
		id = parcel.readString() ?: "",
		name = parcel.readString() ?: "",
		description = parcel.readString() ?: "",
		photoUrl = parcel.readString() ?: "",
		createdAt = parcel.readString() ?: ""
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(id)
		parcel.writeString(name)
		parcel.writeString(description)
		parcel.writeString(photoUrl)
		parcel.writeString(createdAt)
	}

	override fun describeContents(): Int = 0

	companion object CREATOR : Parcelable.Creator<Story> {
		override fun createFromParcel(parcel: Parcel): Story {
			return Story(parcel)
		}

		override fun newArray(size: Int): Array<Story?> {
			return arrayOfNulls(size)
		}
	}
}


