package com.lykke.mobile.data

import com.google.firebase.database.DataSnapshot

interface EntityMapper<out V> {
  fun map(input: DataSnapshot): V
}