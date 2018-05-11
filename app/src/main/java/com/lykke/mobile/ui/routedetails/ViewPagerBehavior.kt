package com.lykke.mobile.ui.routedetails

import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View

class ViewPagerBehavior: CoordinatorLayout.Behavior<ViewPager>() {

  override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: ViewPager, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
    Log.d("ZZZ", "onStartNestedScroll axes = $axes")
    return axes == ViewCompat.SCROLL_AXIS_VERTICAL
  }

  override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: ViewPager, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
    super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    Log.d("ZZZ", "onNestedPreScroll $dy")
  }

  override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: ViewPager, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
    super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
    Log.d("ZZZ", "onNestedScroll $dyConsumed $dyUnconsumed")
  }
}