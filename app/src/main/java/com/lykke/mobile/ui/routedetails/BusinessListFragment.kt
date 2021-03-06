package com.lykke.mobile.ui.routedetails

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
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
import com.lykke.mobile.data.CheckinStatus
import com.lykke.mobile.domain.model.Business
import com.lykke.mobile.util.inflate

/**
 * Created by susnata on 4/12/18.
 */
class BusinessListFragment : Fragment() {
  companion object {
    private const val PAGE_TITLE = "PAGE_TITLE"
    private const val BUSINESS_LIST = "BUSINESS_LIST"
    private const val CHECKIN_STATUS = "CHECKIN_STATUS"

    fun newInstance(): Fragment {
      return BusinessListFragment()
    }
  }

  private lateinit var mBusinessListRV: RecyclerView
  private lateinit var mBusinessListViewModel: BusinessListViewModel
  private lateinit var mBusinessListStatusTV: TextView
  private var mBusinessList = mutableListOf<Business>()
  private var mHost: Host? = null
  private lateinit var mViewModel: RouteDetailsViewModel
  private lateinit var mCheckinStatus: CheckinStatus

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    if (context is Host) {
      mHost = context
    }
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
      mBusinessListViewModel.isActionEnabled.observe(this, Observer { enabled ->
        addBusinessBtn.isEnabled = enabled!!
      })
    }

    addBusinessBtn.setOnClickListener {
      mHost?.handleAddBusiness()
    }

    return view
  }

  override fun onStart() {
    super.onStart()

    mBusinessListViewModel.businesses.observe(activity!!, Observer {
      mBusinessList.clear()
      if (it!!.isEmpty()) {
        showStatus(resources.getString(R.string.route_details_no_businesses))
      } else {
        mBusinessList.addAll(it!!)
        mBusinessListStatusTV.visibility = View.GONE
        mBusinessListRV.visibility = View.VISIBLE
      }
      mBusinessListRV.adapter.notifyDataSetChanged()
    })
  }

  private fun showStatus(message: String) {
    mBusinessListStatusTV.text = message
    mBusinessListStatusTV.visibility = View.VISIBLE
    mBusinessListRV.visibility = View.GONE
  }

  inner class BusinessListAdapter : RecyclerView.Adapter<VH>() {

    override fun onBindViewHolder(holder: VH, position: Int) {
      holder.businessNameTextView.text = mBusinessList[position].key
      mViewModel.getUIViewModel().isCheckinEnabled().observe(this@BusinessListFragment, Observer { enabled ->
        holder.startCheckinBtn.isEnabled = enabled!!
      })
      holder.startCheckinBtn.setOnClickListener {
        ViewCompat.setTransitionName(holder.businessNameTextView, resources.getString(R.string.business_name_transition_name))

        mViewModel.getUIViewModel().handleStartCheckinClick(
            this@BusinessListFragment, mHost!!, mBusinessList[position],
            holder.businessNameTextView)
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
      return VH(parent.inflate(R.layout.business_item))
    }

    override fun getItemCount(): Int {
      return mBusinessList.size
    }
  }

  fun setViewModel(vm: RouteDetailsViewModel, status: CheckinStatus) {
    mCheckinStatus = status
    mViewModel = vm
    mBusinessListViewModel = mViewModel.getUIViewModel().getBusinessListViewModel(mCheckinStatus)
  }

  fun showAlreadyCheckedInAlert(business: Business, view: View?) {
    val builder = AlertDialog.Builder(activity!!)
    builder.setTitle(resources.getString(R.string.already_checked_in, business.key))
        .setMessage(resources.getString(R.string.recheckin_message))
        .setPositiveButton(android.R.string.yes, { _, _ ->
          mViewModel.getUIViewModel().checkin(business, mHost!!, view)
        })
        .setNegativeButton(android.R.string.no, DialogInterface.OnClickListener { dialog, which ->
          dialog.dismiss()
        })
        .setIcon(R.drawable.ic_priority_high_24dp)
        .show()
  }

  class VH(view: View) : RecyclerView.ViewHolder(view) {
    val businessNameTextView = view.findViewById<TextView>(R.id.businessNameTV)!!
    val startCheckinBtn = view.findViewById<Button>(R.id.startCheckinBtn)!!
  }
}
