package com.example.harmonychat.Models

class Message {
    var message: String? = null
    var senderId: String? = null
    var imageUrl: String? = null
    var videoUrl: String? = null
    var pdfUrl: String? = null
    var docUrl: String? = null
    var timestamp: Long = 0

    constructor() {}

    constructor(message: String?, senderId: String?, timestamp: Long) {
        this.message = message
        this.senderId = senderId
        this.timestamp = timestamp
    }

    var feeling = 0
}
