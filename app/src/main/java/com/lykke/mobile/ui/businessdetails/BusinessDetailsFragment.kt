package com.lykke.mobile.ui.businessdetails

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.lykke.mobile.Host
import com.lykke.mobile.R
import com.lykke.mobile.ViewModelFactory
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.util.inflate

class BusinessDetailsFragment: Fragment() {
  private lateinit var mBusiness: Business

  companion object {
    private const val BUSINESS = "BUSINESS"

    fun newInstance(business: Business): BusinessDetailsFragment {
      val bundle = Bundle()
      bundle.putParcelable(BUSINESS, business)
      val fragment = BusinessDetailsFragment()
      fragment.arguments = bundle
      return fragment
    }
  }

  private lateinit var mViewModel: BusinessDetailsViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    mBusiness = arguments?.get(BUSINESS) as Business
    setTitle()
    mViewModel = ViewModelFactory.getInstance(activity!!.application)
        .create(BusinessDetailsViewModel::class.java)
    mViewModel.fetchData(mBusiness)
  }

  private var mBusinessNameTV: TextView? = null
  private var mBusinessAddressTV: TextView? = null
  private var mOutstandingBalanceTV: TextView? = null

  private var mLastOrderAmountTV: TextView? = null
  private var mLastPaymentAmountTV: TextView? = null
  private var mLastVisitDateTV: TextView? = null

  private var mHost: Host? = null

  override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = container!!.inflate(R.layout.business_details)
    mBusinessNameTV = view.findViewById(R.id.businessNameTV)
    mBusinessAddressTV = view.findViewById(R.id.businessAddressTV)
    mOutstandingBalanceTV = view.findViewById(R.id.outstandingBalanceTV)

    mLastOrderAmountTV = view.findViewById(R.id.lastOrderAmountTV)
    mLastPaymentAmountTV = view.findViewById(R.id.lastPaymentAmountTV)
    mLastVisitDateTV = view.findViewById(R.id.lastVisitDateTV)

    view.findViewById<Button>(R.id.businesss_details_continue).setOnClickListener {
      mHost?.next()
    }

    return view
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is Host) {
      mHost = context
    }
  }

  private fun setTitle() {
    mHost?.let {
      if (mBusiness != null) {
        it.setToolbarTitle(mBusiness.key)
      }
    }
  }

  override fun onStart() {
    super.onStart()
    mBusinessNameTV!!.text = mViewModel.getUIViewModel().getBusinessName()
    mBusinessAddressTV!!.text = mViewModel.getUIViewModel().getBusinessAddress()
    mOutstandingBalanceTV!!.text = mViewModel.getUIViewModel().getOutstandingBalance().format()

    val uiViewModel = mViewModel.getUIViewModel()
    uiViewModel.getLastOrderAmount().observe(this, Observer {
      mLastOrderAmountTV!!.text = it
    })

    uiViewModel.getLastPaymentAmount().observe(this, Observer {
      mLastPaymentAmountTV!!.text = it
    })

    uiViewModel.getLastVisitDate().observe(this, Observer {
      mLastVisitDateTV!!.text = it
    })
  }
}
