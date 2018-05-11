package com.lykke.mobile.ui.routedetails

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.lykke.mobile.Host
import com.lykke.mobile.R
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.util.inflate

/**
 * Created by susnata on 4/12/18.
 */
class BusinessListFragment : Fragment() {
  companion object {
    private const val PAGE_TITLE = "PAGE_TITLE"
    private const val BUSINESS_LIST = "BUSINESS_LIST"

    fun newInstance(businessListViewModel: BusinessListViewModel): Fragment {
      val bundle = Bundle()
      bundle.putParcelable(BUSINESS_LIST, businessListViewModel)
      val fragment = BusinessListFragment()
      fragment.arguments = bundle
      return fragment
    }
  }

  private var pageTitle: String = ""
  private lateinit var mBusinessListRV: RecyclerView
  private lateinit var mBusinessListViewModel: BusinessListViewModel
  private lateinit var mBusinessListStatusTV: TextView
  private var mBusinessList = mutableListOf<Business>()
  private var mHost: Host? = null
  private lateinit var mViewModel: RouteDetailsViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      mBusinessListViewModel = it.getParcelable(BUSINESS_LIST)
      pageTitle = it.getString(PAGE_TITLE, "")
    }
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    if (context is Host) {
      mHost = context
    }
  }

  fun getHostActivity(): Host? {
    return mHost
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?): View? {

    val view = container!!.inflate(R.layout.business_list)
    mBusinessListRV = view.findViewById(R.id.businessListRV)
    mBusinessListRV.adapter = BusinessListAdapter()
    mBusinessListRV.layoutManager = LinearLayoutManager(
        activity, LinearLayout.VERTICAL, false)

    mBusinessListStatusTV = view.findViewById(R.id.businessListStatus)
    val addBusinessBtn = view.findViewById<Button>(R.id.add_business_to_route)
    if (mBusinessListViewModel.showAddBusinessBtn) {
      addBusinessBtn.visibility = View.VISIBLE
    }

    addBusinessBtn.setOnClickListener {
      mHost?.handleAddBusiness()
    }

    return view
  }

  override fun onStart() {
    super.onStart()
    mBusinessListViewModel.businesses.observe(this, Observer { businesses ->
      mBusinessList.clear()
      businesses?.let {
        if (it.isEmpty()) {
          showStatus(resources.getString(R.string.route_details_no_businesses))
        } else {
          mBusinessList.addAll(it)
          mBusinessListStatusTV.visibility = View.GONE
          mBusinessListRV.visibility = View.VISIBLE
        }
      }
      mBusinessListRV.adapter.notifyDataSetChanged()
    })

    mBusinessListViewModel.showAddBusinessBtn
  }

  private fun showStatus(message: String) {
    mBusinessListStatusTV.text = message
    mBusinessListStatusTV.visibility = View.VISIBLE
    mBusinessListRV.visibility = View.GONE
  }

  inner class BusinessListAdapter : RecyclerView.Adapter<VH>() {

    override fun onBindViewHolder(holder: VH, position: Int) {
      holder.businessNameTextView.text = mBusinessList[position].key
      holder.startCheckinBtn.isEnabled = mViewModel.getUIViewModel().isActionEnabled()
      holder.startCheckinBtn.setOnClickListener {
        mViewModel.getUIViewModel().handleStartCheckinClick(
            this@BusinessListFragment, mHost!!, mBusinessList[position])
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
      return VH(parent.inflate(R.layout.business_item))
    }

    override fun getItemCount(): Int {
      return mBusinessList.size
    }
  }

  fun setViewModel(vm: RouteDetailsViewModel) {
    mViewModel = vm
  }

  fun showAlreadyCheckedInAlert(business: Business) {
    val builder: AlertDialog.Builder
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      builder = AlertDialog.Builder(activity!!)
    } else {
      builder = AlertDialog.Builder(activity!!)
    }
    builder.setTitle(resources.getString(R.string.already_checked_in, business.key))
        .setMessage(resources.getString(R.string.recheckin_message))
        .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, which ->
          mViewModel.getUIViewModel().checkin(business, mHost!!)
        })
        .setNegativeButton(android.R.string.no, DialogInterface.OnClickListener { dialog, which ->
          dialog.dismiss()
        })
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show()
  }

  class VH(view: View) : RecyclerView.ViewHolder(view) {
    val businessNameTextView = view.findViewById<TextView>(R.id.businessNameTV)!!
    val startCheckinBtn = view.findViewById<Button>(R.id.startCheckinBtn)!!
  }
}
