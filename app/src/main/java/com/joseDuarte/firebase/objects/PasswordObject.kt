package com.joseDuarte.firebase.objects

class PasswordObject {
    var id = ""
    var password = ""

    constructor() {}
    constructor(id: String, password: String) {
        this.id = id
        this.password = password
    }

    override fun toString(): String {
        return "id: $id, password: $password"
    }
}