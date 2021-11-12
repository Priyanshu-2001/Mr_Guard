package com.geek.mrguard.data.policeAnalytics

data class Status(
    val __v: Int,
    val _id: String,
    val createdAt: String,
    val date: String,
    val time: String,
    val police: Police,
    val roomId: String,
    val status: String,
    val updatedAt: String,
    val victim: Victim
)