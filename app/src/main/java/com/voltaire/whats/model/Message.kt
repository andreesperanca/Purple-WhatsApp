package com.voltaire.whats.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Timestamp

@Parcelize
data class Message(

    val text: String = "",
    val time: Long = 0,
    val toEmail: String = "",
    val fromEmail: String = ""
) : Parcelable
