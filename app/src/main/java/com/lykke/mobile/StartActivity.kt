package com.lykke.mobile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.Transition
import android.transition.TransitionInflater
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.domain.model.Route
import com.lykke.mobile.ui.State
import com.lykke.mobile.ui.StateContext
import com.lykke.mobile.ui.businessdetails.BusinessDetailsState
import com.lykke.mobile.ui.login.LoginActivity
import com.lykke.mobile.ui.order.EnterOrderState
import com.lykke.mobile.ui.payment.EnterPaymentState
import com.lykke.mobile.ui.routedetails.AddBusinessToRouteState
import com.lykke.mobile.ui.routedetails.RouteDetailsFragment
import com.lykke.mobile.ui.routedetails.RouteDetailsState
import com.lykke.mobile.ui.routestart.RouteListState
import com.lykke.mobile.ui.summary.SummaryState
import kotlinx.android.synthetic.main.start_layout.drawer_layout
import kotlinx.android.synthetic.main.start_layout.nav_view
import kotlinx.android.synthetic.main.start_layout.toolbar

class StartActivity : AppCompatActivity(), Host, StateContext {
  enum class TransitionTypes {
    ENTER, EXIT, RETURN, REENTER
  }

  override fun getContext(): Context {
    return this
  }

  override fun setToolbarTitle(title: String) {
    mToolbarTitleView.text = title
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

  private lateinit var mToolbarTitleView: TextView
  private lateinit var mDrawerTitle: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_layout)

    setSupportActionBar(toolbar)
    mToolbarTitleView = toolbar.findViewById(R.id.toolbar_title)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_white)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar!!.setDisplayShowTitleEnabled(false)

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
      Log.d("LLL", "Moving back -> $mCurrentState ${mCurrentState!!.prevState}")
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

    mDrawerTitle = nav_view.getHeaderView(0).findViewById(R.id.drawer_title)
    mDrawerTitle.text = resources.getString(R.string.drawer_welcome_message,
        FirebaseAuth.getInstance().currentUser!!.displayName)

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

  private var mPrevState: State? = null

  override fun next(view: View?) {
    if (mCurrentState == mSummaryState) {
      supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    mPrevState = mCurrentState
    mCurrentState = mCurrentState?.nextState
    updateFragment(view)
  }

  private fun updateFragment(view: View? = null) {
    mCurrentState?.let {
      val nextFragment = it.getFragment()

      nextFragment.enterTransition = getTransition(TransitionTypes.ENTER, it)
      nextFragment.exitTransition = getTransition(TransitionTypes.EXIT, it)
      nextFragment.reenterTransition = getTransition(TransitionTypes.REENTER, it)
      nextFragment.returnTransition = getTransition(TransitionTypes.RETURN, it)
      val transaction = supportFragmentManager.beginTransaction()
      transaction.replace(R.id.content_frame, nextFragment)
          .addToBackStack(null)

      if (mPrevState == mDetailsState && mCurrentState == mBusinessDetailsState) {
        nextFragment.sharedElementEnterTransition = TransitionInflater.from(this)
            .inflateTransition(R.transition.move)
        transaction.addSharedElement(view, resources.getString(R.string.business_name_transition_name))
      }

      transaction.commit()
    }
  }

  private fun getTransition(type: TransitionTypes, state: State): Transition {
    when (type) {
      TransitionTypes.ENTER ->
        return if (state.getEnterTransition() == null) {
          TransitionInflater.from(this).inflateTransition(R.transition.enter)
        } else {
          state.getEnterTransition()!!
        }
      TransitionTypes.EXIT ->
        return if (state.getExitTransition() == null) {
          TransitionInflater.from(this).inflateTransition(R.transition.exit)
        } else {
          state.getExitTransition()!!
        }
      TransitionTypes.RETURN ->
        return if (state.getReturnTransition() == null) {
          TransitionInflater.from(this).inflateTransition(R.transition.return_transition)
        } else {
          state.getReturnTransition()!!
        }
      TransitionTypes.REENTER ->
        return if (state.getReenterTransition() == null) {
          TransitionInflater.from(this).inflateTransition(R.transition.reenter)
        } else {
          state.getEnterTransition()!!
        }
    }
  }

  override fun setCurrentRoute(route: Route) {
    mCurrentRoute = route
  }

  override fun getCurrentRoute(): Route {
    return mCurrentRoute!!
  }
}