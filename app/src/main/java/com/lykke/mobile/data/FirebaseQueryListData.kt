package com.lykke.mobile.data

import android.arch.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class FirebaseQueryListData<V>(private val ref: DatabaseReference, mapper: EntityMapper<V>): LiveData<List<V>>() {

  val valueListener = object: ValueEventListener {
    override fun onCancelled(p0: DatabaseError?) {
    }

    override fun onDataChange(snapshot: DataSnapshot?) {
      var result = mutableListOf<V>()
      snapshot?.let {
        for (item: DataSnapshot in snapshot.children) {
          val entity = mapper.map(item)
          result.add(entity)
        }
      }
      value = result
    }
  }

  override fun onActive() {
    super.onActive()
    ref.addValueEventListener(valueListener)
  }

  override fun onInactive() {
    super.onInactive()
    ref.removeEventListener(valueListener)
  }
}