package com.example.projekti

import com.google.firebase.database.*

class Database {

    private val database = FirebaseDatabase.getInstance().getReference("/users")

    // Tällä funktiolla haetaan kulutus käyttäjän UID:n perusteella.
    fun getKulutusByUid(uid: String, callback: (String?) -> Unit) {
        val query: Query = database.child(uid).child("kulutus")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val kulutus = dataSnapshot.value?.toString() ?: "0"
                callback(kulutus)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Käsittele virhe tässä.
                callback(null)
            }
        })
    }

    // Tällä funktiolla lisätään uusi käyttäjä tietokantaan käyttäjän UID:n perusteella, mutta tarkistetaan ensin, onko se jo olemassa.
    fun addUser(uid: String) {
        val query: Query = database.orderByValue().equalTo(uid)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.childrenCount == 0L) {
                    // Käyttäjää ei ole vielä tietokannassa, joten lisätään se.
                    database.child(uid).setValue(mapOf("kulutus" to 0))
                } else {
                    // Käyttäjä on jo tietokannassa, joten ei tarvitse lisätä sitä uudelleen.
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Käsittele virhe tässä.
            }
        })
    }

    // Asetetaan kulutus
    fun setKulutusByUid(uid: String, kulutus: Int) {
        database.child(uid).child("kulutus").setValue(kulutus)
    }

    // Tyhjennetään kulutus
    fun clearKulutusByUid(uid: String) {
        database.child(uid).child("kulutus").setValue(0)
    }
}