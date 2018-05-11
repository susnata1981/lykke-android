package com.lykke.mobile.ui.summary

import android.support.v4.app.Fragment
import com.lykke.mobile.ui.StateContext
import com.lykke.mobile.ui.State

class SummaryState(context: StateContext): State(context) {

  override fun getFragment(): Fragment {
    return SummaryFragment.newInstance()
  }
}