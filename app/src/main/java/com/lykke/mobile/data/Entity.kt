package com.lykke.mobile.data

import com.google.firebase.database.DataSnapshot

enum class Entity(val tableName: String, mapper: EntityMapper<Any>) {
  BUSINESSES("businesses", BusinessEntityMapper()),
  USER("users", UserEntityMapper()),
  ROUTE("businesses", RouteEntityMapper()),
}

class BusinessEntityMapper : EntityMapper<BusinessEntity> {
  override fun map(input: DataSnapshot): BusinessEntity {
    val business = input.getValue(BusinessEntity::class.java)!!
    business.key = input.key
    return business
  }
}

class UserEntityMapper : EntityMapper<UserEntity> {
  override fun map(input: DataSnapshot): UserEntity {
    val user = input.getValue(UserEntity::class.java)!!
    user.key = input.key
    return user
  }
}

class RouteEntityMapper : EntityMapper<RouteEntity> {
  override fun map(input: DataSnapshot): RouteEntity {
    val route = input.getValue(RouteEntity::class.java)!!
    route.key = input.key
    return route
  }
}

class CheckinEntityMapper : EntityMapper<CheckinEntity> {
  override fun map(input: DataSnapshot): CheckinEntity {
    val checkinEntity = input.getValue(CheckinEntity::class.java)!!
    checkinEntity.key = input.key
    return checkinEntity
  }
}