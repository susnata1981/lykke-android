package com.lykke.mobile.ui.routedetails

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import biz.laenger.android.vpbs.BottomSheetUtils
import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.lykke.mobile.Host
import com.lykke.mobile.R
import com.lykke.mobile.ViewModelFactory
import com.lykke.mobile.data.CheckinStatus
import com.lykke.mobile.domain.model.Route
import com.lykke.mobile.util.inflate

class RouteDetailsFragment : android.support.v4.app.Fragment(), OnMapReadyCallback {
  private lateinit var mMap: GoogleMap
  private val mapFragment = SupportMapFragment.newInstance()

  private var mCheckinsViewPager: ViewPager? = null
  private var mCheckinFilterTabLayout: TabLayout? = null

  private lateinit var mViewModel: RouteDetailsViewModel
  private lateinit var mHost: Host
  private lateinit var mCurrentRoute: Route

  companion object {
    private const val TAG = "RouteDetailsFragment"
    private const val CURRENT_ROUTE = "CURRENT_ROUTE"
    private const val KOLKATA_LAT = 22.5233063
    private const val KOLKATA_LNG = 88.3548363
    private val mCheckinStatusList = CheckinStatus.values()

    fun newInstance(route: Route): RouteDetailsFragment {
      val bundle = Bundle()
      bundle.putParcelable(CURRENT_ROUTE, route)
      val fragment = RouteDetailsFragment()
      fragment.arguments = bundle
      return fragment
    }
  }

  override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap

    mViewModel.getUIViewModel().getRoute().observe(this, Observer { route ->
      route?.let {
        it.businesses.forEach { b ->
          mMap.addMarker(MarkerOptions()
              .position(LatLng(b.lat, b.lng))
              .title(b.key)
              .snippet(b.address))
        }
      }
    })

    // Add a marker in Kolkata, India, and move the camera.
    mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(22.5577449, 88.2671297)))
    Handler().postDelayed({
      mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
          LatLng(KOLKATA_LAT, KOLKATA_LNG), 13.0f))
    }, 900)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    mCurrentRoute = arguments?.get(CURRENT_ROUTE) as Route

    mViewModel = ViewModelFactory.getInstance(activity!!.application).create(
        RouteDetailsViewModel::class.java)
    mViewModel.getRouteDetails(mCurrentRoute.key!!)
  }

  override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

    val view = container!!.inflate(layoutId = R.layout.route_details_layout)
    view.findViewById<TextView>(R.id.routeDetailsTitle).text =
        resources.getString(R.string.route_details_bottom_sheet_title, mCurrentRoute.key)
    view.findViewById<TextView>(R.id.routeDetailsDay).text = mCurrentRoute.assignment.dayOfWeek

    val existingMapFragment = childFragmentManager.findFragmentById(R.id.mapContainer)
    if (existingMapFragment == null) {
      childFragmentManager.beginTransaction()
          .add(R.id.mapContainer, mapFragment)
          .commit()
      mapFragment.getMapAsync(this)
    }

    setupBottomSheet(view)
    return view
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    if (context is Host) {
      mHost = context
    }
  }

  override fun onStart() {
    super.onStart()
    if (mViewModel.getUIViewModel().hasStartedCheckin()) {
      val bottomSheet = view!!.findViewById<LinearLayout>(R.id.bottom_sheet)
      ViewPagerBottomSheetBehavior.from(bottomSheet).state = ViewPagerBottomSheetBehavior.STATE_EXPANDED
    }

    if (mHost != null) {
      mHost.setToolbarTitle(resources.getString(R.string.route_details_title, mCurrentRoute.key!!))
    }

    mViewModel.getRouteDetails(mCurrentRoute.key!!)
  }

  private fun setupBottomSheet(view: View) {
    mCheckinsViewPager = view.findViewById(R.id.checkinsViewPager)
    mCheckinFilterTabLayout = view.findViewById(R.id.checkinFilterTabLayout)
    mCheckinsViewPager?.adapter = CheckinsListAdpater(childFragmentManager)
    mCheckinFilterTabLayout?.setupWithViewPager(mCheckinsViewPager)
    BottomSheetUtils.setupViewPager(mCheckinsViewPager)
  }

  inner class CheckinsListAdpater(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
      val fragment = BusinessListFragment.newInstance()
          as BusinessListFragment
      fragment.setViewModel(mViewModel, mCheckinStatusList[position])
      return fragment
    }

    override fun getCount(): Int {
      return mCheckinStatusList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
      val vm = mViewModel.getUIViewModel().getBusinessListViewModel(mCheckinStatusList[position])
      vm.mPageTitle.observe(activity!!, Observer {
        mCheckinFilterTabLayout!!.getTabAt(position)?.text = it!!
      })

      return mCheckinStatusList[position].name
    }
  }

  class VH(view: View) : RecyclerView.ViewHolder(view) {
    val dayTv = view.findViewById<TextView>(R.id.dayTextView)
    val routeNameTv = view.findViewById<TextView>(R.id.routeNameTextView)
  }
}

