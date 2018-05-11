package com.lykke.mobile.ui.routedetails

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.lykke.mobile.Host
import com.lykke.mobile.R
import com.lykke.mobile.util.inflate

class AddBusinessToRouteFragment: Fragment() {

  companion object {
    fun newInstnce(): Fragment {
      return AddBusinessToRouteFragment()
    }
  }

  private lateinit var mBusinessListRV: RecyclerView
  private lateinit var mContinueBtn: Button

  private var mHost: Host? = null
  private lateinit var mViewModel: AddBusinessToRouteViewModel

  private val mItems = mutableListOf<AddBusinessToRouteViewModel.Item>()

  override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val view = container!!.inflate(R.layout.add_business_to_route_layout)
    mBusinessListRV = view.findViewById(R.id.add_business_to_route_business_list)
    mContinueBtn = view.findViewById(R.id.add_business_to_route_continue)
    mContinueBtn.setOnClickListener {
      mHost?.next()
    }
    setupViewPager()
    return view
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    if (context is Host) {
      mHost = context
    }
  }

  private fun setupViewPager() {
    mBusinessListRV.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    mBusinessListRV.adapter = Adapter()
  }

  override fun onStart() {
    super.onStart()
    mViewModel = ViewModelProviders.of(this).get(AddBusinessToRouteViewModel::class.java)
    mViewModel.fetchData()

    mViewModel.getUIViewModel().getItems().observe(this, Observer {
      mItems.clear()
      mItems.addAll(it!!)
      mBusinessListRV.adapter.notifyDataSetChanged()
    })
  }

  inner class Adapter : RecyclerView.Adapter<VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
      return VH(parent.inflate(R.layout.add_route_business_item))
    }

    override fun getItemCount(): Int {
      return mItems.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
      holder.businessName.text = mItems[position].businessKey

      holder.addBusinessBtn.isEnabled = mItems[position].isAddEnabled
      if (mItems[position].isAddEnabled) {
        holder.addBusinessBtn.setOnClickListener {
          mViewModel.handleAdd(mItems[position].businessKey)
        }
      }

      holder.removeBusinessBtn.isEnabled = mItems[position].isRemovedEnabled
      if (holder.removeBusinessBtn.isEnabled) {
        holder.removeBusinessBtn.setOnClickListener {
          mViewModel.handleRemove(mItems[position].businessKey)
        }
      }
    }
  }

  class VH(val view: View) : RecyclerView.ViewHolder(view) {
    val businessName = view.findViewById<TextView>(R.id.add_business_to_route_name)
    val addBusinessBtn = view.findViewById<Button>(R.id.add_business_to_route_add_btn)
    val removeBusinessBtn = view.findViewById<Button>(R.id.add_business_to_route_remove_btn)
  }
}
