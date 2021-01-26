package com.joseDuarte.firebase.firebase

import com.google.firebase.database.*
import com.joseDuarte.firebase.MainActivity
import com.joseDuarte.firebase.objects.PasswordObject

class FirebaseDB(val activity: MainActivity) {

    companion object {
        private val VALID_CHARACTERS_REGEX: Regex = "[^a-zA-Z0-9]*".toRegex()
        val DB_CONNECTION = FirebaseDatabase.getInstance()

        lateinit var EVENT_LISTENER : ValueEventListener
        lateinit var USER_REF: DatabaseReference
        lateinit var USER_FIREBASE_DB_PATH : String

        fun makeFirebaseURLPath(data: String): String {
            val mailData: List<String> = data.split("@")
            val domains: List<String> = mailData[1].split(".")

            USER_FIREBASE_DB_PATH = ""
            for (i in (domains.size - 1) downTo 0) {
                USER_FIREBASE_DB_PATH += domains[i] + "/"
            }

            var needle: String
            USER_FIREBASE_DB_PATH += mailData[0].replace(VALID_CHARACTERS_REGEX) { needle = it.value; "" }
            return USER_FIREBASE_DB_PATH
        }

        fun addPasswordObjectToFirebase(passwordObject: PasswordObject) {
            USER_REF.child("data").child(passwordObject.id).setValue(passwordObject)
        }

        fun editPasswordObjectFromFirebase(passwordObject: PasswordObject) {
            addPasswordObjectToFirebase(passwordObject)
        }

        fun removePasswordObjectFromFirebase(id: String) {
            USER_REF.child("data").child(id).removeValue()
        }
    }

    init {
        makeFirebaseURLPath(activity.USER_ID)
        USER_REF = DB_CONNECTION.getReference(USER_FIREBASE_DB_PATH)
        initListeners()
    }

    private fun initListeners() {

        EVENT_LISTENER = USER_REF.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data: Iterable<DataSnapshot> = dataSnapshot.child("data").children
                MainActivity.USER_DATA_LIST.clear()
                for (it in data) {
                    MainActivity.USER_DATA_LIST.add(it.getValue(PasswordObject::class.java)!!)
                }
                activity.update()
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })
        /*USER_REF.child("data")
            .addChildEventListener(object : ChildEventListener {
                override fun onCancelled(node: DatabaseError) {}
                override fun onChildMoved(node: DataSnapshot, p1: String?) {}
                override fun onChildChanged(node: DataSnapshot, p1: String?) {
                    val id = node.child("id").value
                    val passwordObject: PasswordObject? =
                        MainActivity.USER_DATA_LIST.find {
                            it.id == id.toString()
                        }

                    if (passwordObject != null) {
                        passwordObject.password = node.child("password")
                                                        .value
                                                        .toString()
                    }
                    println("2")
                    activity.update()
                }
                override fun onChildAdded(node: DataSnapshot, p1: String?) {
                    val id = node.child("id").value
                    val passwordObject: PasswordObject? =
                        MainActivity.USER_DATA_LIST.find {
                            it.id == id.toString()
                        }

                    if(passwordObject==null) {
                        MainActivity.USER_DATA_LIST.add(
                            node.getValue(PasswordObject::class.java)!!
                        )
                        println("3")
                        activity.update()
                    }
                }
                override fun onChildRemoved(node: DataSnapshot) {
                    val id = node.child("id").value
                    val passwordObject: PasswordObject? =
                        MainActivity.USER_DATA_LIST.find {
                            it.id == id.toString()
                        }

                    MainActivity.USER_DATA_LIST.remove(passwordObject)
                    println("4")
                    activity.update()
                }
            })*/
    }

    fun restartListeners() {
        USER_REF.addValueEventListener(EVENT_LISTENER)
    }

    fun stopListeners() {
        USER_REF.removeEventListener(EVENT_LISTENER)
    }


}