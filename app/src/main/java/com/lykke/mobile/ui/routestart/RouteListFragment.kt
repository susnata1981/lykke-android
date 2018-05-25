package com.lykke.mobile.ui.routestart

import android.arch.lifecycle.Observer
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.lykke.mobile.Host
import com.lykke.mobile.R
import com.lykke.mobile.ViewModelFactory
import com.lykke.mobile.util.inflate
import kotlinx.android.synthetic.main.route_list_item.view.dayTextView
import kotlinx.android.synthetic.main.route_list_item.view.routeNameTextView

class RouteListFragment : Fragment() {

  private var mHost: Host? = null
  private var mViewModel: RouteListViewModel? = null

  private lateinit var mAdapter: RouteListAdaper
  private lateinit var routeListRV: RecyclerView

  companion object {
    private const val TAG = "RouteListFragment"

    private const val HEADER = 0
    private const val ROUTE = 1
    private const val CURRENT_ROUTE = 2

    fun newInstance(): RouteListFragment {
      return RouteListFragment()
    }
  }

  private var mRoutes = mutableListOf<RouteListViewModel.RouteItem>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d(TAG, "onCreate called");
  }

  private fun updateView() {
    mAdapter.notifyDataSetChanged()
  }

  override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    container.let {
      val view = container!!.inflate(layoutId = R.layout.start_fragment_layout)
      routeListRV = view.findViewById(R.id.routeListView)
      routeListRV.layoutManager = LinearLayoutManager(
          activity, LinearLayout.VERTICAL, false)
      mAdapter = RouteListAdaper()
      routeListRV.adapter = mAdapter

      return view
    }
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    mViewModel = ViewModelFactory.getInstance(activity!!.application)
        .create(RouteListViewModel::class.java)

    mViewModel!!.getUIViewModel().getRouteItemList().observe(this, Observer {
      mRoutes.clear()
      mRoutes.addAll(it!!)
      updateView()
    })

    if (context is Host) {
      mHost = context
    }
  }

  override fun onStart() {
    super.onStart()
    mHost?.let {
      it.setToolbarTitle(resources.getString(R.string.route_list_fragment_title))
    }
  }

  fun next() {
    mHost.let { mHost!!.next() }
  }

  inner class RouteListAdaper : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
      return when (viewType) {
        CURRENT_ROUTE ->
          VH(parent.inflate(R.layout.route_list_item_today))
        else -> VH(parent.inflate(R.layout.route_list_item))
      }
    }

    override fun getItemCount(): Int = mRoutes.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = mRoutes[position]
        holder.itemView.dayTextView.text = data.dayOfWeek.name
        holder.itemView.routeNameTextView.text = data.key

        if (data.isCurrentRoute) {
          val drawable = (holder.itemView.background.mutate() as GradientDrawable)
          var color = 0
          if (Build.VERSION.SDK_INT >= 23) {
            color = resources.getColor(R.color.colorAccent, context!!.theme)
          } else {
            color = resources.getColor(R.color.colorAccent)
          }
          drawable.setColor(color)
          drawable.invalidateSelf()
        }

        holder.itemView.setOnClickListener {
          if (data.isHoliday) {
            Toast.makeText(
                activity, resources.getString(R.string.holiday_message), Toast.LENGTH_SHORT).show()
          } else {
            mViewModel!!.getUIViewModel().handleClick(mHost!!, data)
          }
        }
    }

    override fun getItemViewType(position: Int): Int {
      if (mRoutes[position].isCurrentRoute) {
        return CURRENT_ROUTE
      }
      return ROUTE
    }
  }

  class VH(val view: View) : RecyclerView.ViewHolder(view) {
    init {
      val dayTextView = view.findViewById<TextView>(R.id.dayTextView)
      val routeNameTextView = view.findViewById<TextView>(R.id.routeNameTextView)
    }
  }
}