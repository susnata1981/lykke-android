package com.lykke.mobile.domain

import com.lykke.mobile.data.Repository
import com.lykke.mobile.domain.model.Inventory
import com.lykke.mobile.domain.model.Item
import io.reactivex.Observable
import io.reactivex.Single

class InventoryInteractor(val repository: Repository) : Interactor<String?, Observable<Inventory>> {

  override fun execute(input: String?): Observable<Inventory> {
    return repository.getInventory().map { items ->
      val r = mutableListOf<Item>()
      items.forEach { item ->
        r.add(Item(item.name, item.price, item.quantity, item.timeCreated))
      }
      val sortedList = r.sortedBy { it.key }
      Inventory(sortedList)
    }.toObservable()
  }
}

class UpdateInventoryQuantityInteractor(val repository: Repository)
  : Interactor<Map<String, Int>, Single<Boolean>> {

  override fun execute(req: Map<String, Int>): Single<Boolean> {
    return repository.updateInventory(req)
  }
}
