package com.example.harmonychat.Models


class UserStatus {
    var name: String? = null
    var profileImage: String? = null
    var lastUpdated: Long = 0
    var statuses: ArrayList<Status>? = null

    constructor() {}
    constructor(
        name: String?,
        profileImage: String?,
        lastupdated: Long,
        statuses: ArrayList<Status>?
    ) {
        this.name = name
        this.profileImage = profileImage
        lastUpdated = lastupdated
        this.statuses = statuses
    }
}
