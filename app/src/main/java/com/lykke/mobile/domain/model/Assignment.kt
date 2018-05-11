package com.lykke.mobile.domain.model

import android.os.Parcelable
import com.lykke.mobile.data.AssignmentEntity
import kotlinx.android.parcel.Parcelize

/**
 * Created by susnata on 4/12/18.
 */
@Parcelize
data class Assignment(
    val assignee: String?,
    val dayOfWeek: String?) : Parcelable {

  companion object {
    fun convert(assignment: AssignmentEntity?): Assignment {
      assignment?.let {
        return Assignment(assignment.assignee, assignment.dayOfWeek)
      }
      return Assignment(null, null)
    }
  }
}