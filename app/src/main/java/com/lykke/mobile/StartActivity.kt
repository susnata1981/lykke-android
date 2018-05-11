package com.lykke.mobile

import android.app.FragmentManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionInflater
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Route
import com.lykke.mobile.ui.State
import com.lykke.mobile.ui.StateContext
import com.lykke.mobile.ui.businessdetails.BusinessDetailsState
import com.lykke.mobile.ui.login.LoginActivity
import com.lykke.mobile.ui.order.EnterOrderState
import com.lykke.mobile.ui.payment.EnterPaymentState
import com.lykke.mobile.ui.routedetails.AddBusinessToRouteState
import com.lykke.mobile.ui.routedetails.RouteDetailsState
import com.lykke.mobile.ui.routestart.RouteListState
import com.lykke.mobile.ui.summary.SummaryState
import kotlinx.android.synthetic.main.start_layout.drawer_layout
import kotlinx.android.synthetic.main.start_layout.nav_view
import kotlinx.android.synthetic.main.start_layout.toolbar

class StartActivity : AppCompatActivity(), Host, StateContext {
  override fun setToolbarTitle(title: String) {
    supportActionBar!!.title = title
  }

  override fun handleAddBusiness() {
    mAddBusinessToRouteState.prevState = mCurrentState
    mAddBusinessToRouteState.nextState = mCurrentState
    mCurrentState = mAddBusinessToRouteState
    updateFragment()
  }

  override fun getBusiness(): Business {
    return mCurrentBusiness!!
  }

  override fun setCurrentBusiness(business: Business) {
    mCurrentBusiness = business
  }

  override fun getCurrentBusiness(): Business? {
    return mCurrentBusiness
  }

  companion object {
    private const val TAG = "StartActivity"
  }

  private var mCurrentRoute: Route? = null
  private var mCurrentBusiness: Business? = null

  private val mStartState = RouteListState(this)
  private val mDetailsState = RouteDetailsState(this)
  private val mBusinessDetailsState = BusinessDetailsState(this)
  private val mEnterPaymentState = EnterPaymentState(this)
  private val mEnterOrderState = EnterOrderState(this)
  private val mSummaryState = SummaryState(this)
  private var mCurrentState: State? = null
  private var mAddBusinessToRouteState = AddBusinessToRouteState(this)

  private lateinit var mViewModel: StartActivityViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_layout)

    setSupportActionBar(toolbar)
    toolbar.title = "Start"
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_white)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    initState()
    val fragment = mCurrentState!!.getFragment()
    fragment.exitTransition = TransitionInflater.from(this)
        .inflateTransition(R.transition.exit)
    supportFragmentManager.beginTransaction()
        .replace(R.id.content_frame, fragment)
        .commit()

    setupDrawerView()
    mViewModel = ViewModelProviders.of(this).get(StartActivityViewModel::class.java)
  }

  override fun onBackPressed() {
    if (mCurrentState?.prevState != null) {
      mCurrentState = mCurrentState!!.prevState
      supportFragmentManager.popBackStack()

      if (mCurrentState == mAddBusinessToRouteState) {
        supportFragmentManager.popBackStack()
        mCurrentState = mCurrentState!!.prevState
      }
    }
  }

  private fun initState() {
    mCurrentState = mStartState
    mStartState.nextState = mDetailsState
    mDetailsState.prevState = mStartState
    mDetailsState.nextState = mBusinessDetailsState
    mBusinessDetailsState.prevState = mDetailsState
    mBusinessDetailsState.nextState = mEnterOrderState
    mEnterOrderState.prevState = mBusinessDetailsState
    mEnterOrderState.nextState = mEnterPaymentState
    mEnterPaymentState.nextState = mSummaryState
    mEnterPaymentState.prevState = mEnterOrderState
    mSummaryState.prevState = mEnterPaymentState
    mSummaryState.nextState = mDetailsState
  }

  private fun setupDrawerView() {
    drawer_layout.setStatusBarBackground(R.color.colorPrimaryDark)

    nav_view.setNavigationItemSelectedListener {
      when (it.itemId) {
        R.id.all_routes_menu -> {
          mCurrentState = mStartState
          updateFragment()
        }
        R.id.logout_menu -> {
          mViewModel.logout().observe(this, Observer {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
          })
        }
        else -> {
          throw IllegalArgumentException("Invalid selection")
        }
      }
      it.isChecked = true
      drawer_layout.closeDrawers()
      true
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        drawer_layout.openDrawer(GravityCompat.START)
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun next() {
    if (mCurrentState == mSummaryState) {
      supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    mCurrentState = mCurrentState?.nextState
    updateFragment()
  }

  private fun updateFragment() {
    mCurrentState?.let {
      val nextFragment = it.getFragment()
      nextFragment.exitTransition = TransitionInflater.from(this).inflateTransition(R.transition.exit)
      nextFragment.enterTransition = TransitionInflater.from(this).inflateTransition(R.transition.enter)
      nextFragment.returnTransition = TransitionInflater.from(this).inflateTransition(R.transition.return_transition)
      nextFragment.reenterTransition = TransitionInflater.from(this).inflateTransition(R.transition.reenter)

      supportFragmentManager.beginTransaction()
          .replace(R.id.content_frame, nextFragment)
//          .setReorderingAllowed(true)
          .addToBackStack(null)
          .commit()
    }
  }

  override fun setCurrentRoute(route: Route) {
    mCurrentRoute = route
  }

  override fun getCurrentRoute(): Route {
    return mCurrentRoute!!
  }
}