package com.lykke.mobile.domain

interface Interactor<in T, out V> {
  fun execute(input: T): V
}