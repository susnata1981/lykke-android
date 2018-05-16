package com.lykke.mobile.ui.order

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.lykke.mobile.Host
import com.lykke.mobile.R
import com.lykke.mobile.ViewModelFactory
import com.lykke.mobile.domain.model.Inventory
import com.lykke.mobile.domain.model.Order
import com.lykke.mobile.util.format
import com.lykke.mobile.util.inflate
import java.util.*

class EnterOrderFragment : Fragment() {
  companion object {
    private const val SALES_TAX = .08f

    fun newInstance(): EnterOrderFragment {
      return EnterOrderFragment()
    }
  }

  private var mInventoryListRV: RecyclerView? = null
  private lateinit var mViewModel: EnterOrderViewModel
  private var mInventory: Inventory? = null
  private val mItemQuantityMap = mutableMapOf<String, Int>()
  private var mHost: Host? = null
  private var mLastOrder: Order? = null

  private val ORDER_TITLE = 1000
  private val ORDER_ITEM = 1001
  private val ORDER_DETAILS = 1002

  private lateinit var mProgressBarContainer: View

  override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = container!!.inflate(R.layout.enter_order_layout)
    mInventoryListRV = view.findViewById(R.id.enter_order_inventory_list)

    mInventoryListRV!!.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
    mInventoryListRV!!.adapter = InventoryListAdapter()

    mProgressBarContainer = view.findViewById(R.id.enter_order_progress_bar_container)
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
    mViewModel = ViewModelFactory.getInstance(activity!!.application)
        .create(EnterOrderViewModel::class.java)

    mViewModel.fetchData(mHost!!.getCurrentBusiness()!!)

    mViewModel.getUIViewModel().getInventory().observe(this,
        Observer {
          mInventory = it!!
          mInventoryListRV!!.adapter.notifyDataSetChanged()
        })

    mViewModel.getUIViewModel().getStatus().observe(this, Observer { message ->
      val snackbar = Snackbar.make(view!!, message!!, Snackbar.LENGTH_SHORT)
      snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
          super.onDismissed(transientBottomBar, event)
          mHost!!.next()
        }
      })
      snackbar.show()
    })

    mViewModel.getUIViewModel().getLastOrder().observe(this, Observer {
      if (it != null) {
        mLastOrder = it
        mItemQuantityMap.clear()
        mItemQuantityMap.putAll(mLastOrder!!.items)
        mInventoryListRV!!.adapter.notifyDataSetChanged()
      }
    })

    mViewModel.getUIViewModel().shouldShowProgressBar().observe(this, Observer { show ->
      if (show!!) {
        mProgressBarContainer.visibility = View.VISIBLE
      } else {
        mProgressBarContainer.visibility = View.GONE
      }
    })

    mViewModel.getUIViewModel().next().observe(this, Observer {
      mHost?.next()
    })

    mHost?.setToolbarTitle(resources.getString(R.string.enter_order_title))
  }

  private var mOrderTotalVH: OrderDetailsVH? = null

  inner class InventoryListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      return if (viewType == ORDER_TITLE) {
        val titleView = TextView(parent.context)
        titleView.setText(R.string.enter_order_title)
        titleView.setTextAppearance(R.style.Headline)
        TitleVH(titleView)
      } else if (viewType == ORDER_DETAILS) {
        OrderDetailsVH(parent.inflate(R.layout.order_details_view))
      } else {
        VH(parent.inflate(R.layout.inventory_item))
      }
    }

    override fun getItemCount(): Int {
      if (mInventory != null && mInventory!!.items != null) {
        return mInventory!!.items.size + 2
      }
      return 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
      val viewType = getItemViewType(position)
      if (viewType == ORDER_ITEM) {
        val item = mInventory!!.items[position - 1]
        if (holder is VH) {
          holder.itemNameTV.text = item.key
          holder.remainingQuantityTV.text = item.quantity.toString()

          if (mItemQuantityMap[item.key] != null) {
            holder.orderQuantity.setText(mItemQuantityMap[item.key].toString())
          } else {
            mLastOrder?.let {
              if (it.items[item.key] != null) {
                holder.orderQuantity.setText(it.items[item.key].toString())
              }
            }
          }

          holder.orderQuantity.addTextChangedListener(holder.quantityWatcher)
        }
      } else if (viewType == ORDER_DETAILS) {
        updateTotal(holder as OrderDetailsVH)
        mOrderTotalVH = holder
        Log.d("KKK", "EOF::mItemQuantityMap -> $mItemQuantityMap")
        holder.continueBtn!!.setOnClickListener {
          val order = Order(mGross, mTotal, mItemQuantityMap, Date().time)
          mViewModel.getUIViewModel().updateOrder(order)
        }

      }
    }

    override fun getItemViewType(position: Int): Int {
      return when (position) {
        0 -> ORDER_TITLE
        mInventory!!.items.size + 1 -> ORDER_DETAILS
        else -> ORDER_ITEM
      }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
      super.onViewRecycled(holder)
      if (holder is VH) {
        holder.orderQuantity.setText("")
        holder.orderQuantity.removeTextChangedListener(holder.quantityWatcher)
      }
    }
  }

  private var mGross: Double = 0.0
  private var mTax: Double = 0.0
  private var mTotal: Double = 0.0

  fun updateTotal(vh: OrderDetailsVH?) {
    vh ?: return

    mInventory ?: return

    mGross = 0.0
    mItemQuantityMap.forEach { key, quantity ->
      val item = mInventory!!.items.firstOrNull { it.key == key }
      if (item != null) {
        mGross += item.price * quantity
      }
    }

    mTax = SALES_TAX * mGross
    mTotal = mTax + mGross

    vh.orderGrossTV.text = mGross.format()
    vh.orderTaxTV.text = mTax.format()
    vh.orderTotalTV.text = mTotal.format()
  }

  inner class TitleVH(view: View) : RecyclerView.ViewHolder(view) {
  }

  inner class OrderDetailsVH(view: View) : RecyclerView.ViewHolder(view) {
    val orderGrossTV = view.findViewById<TextView>(R.id.order_gross)
    val orderTaxTV = view.findViewById<TextView>(R.id.order_tax)
    val orderTotalTV = view.findViewById<TextView>(R.id.order_total)
    val continueBtn = view.findViewById<Button>(R.id.order_continue_btn)
  }

  inner class VH(view: View) : RecyclerView.ViewHolder(view) {
    var itemNameTV: TextView = view.findViewById(R.id.item_name)
    var remainingQuantityTV: TextView = view.findViewById(R.id.item_quantity)
    var orderQuantity: EditText = view.findViewById(R.id.order_quantity)

    val quantityWatcher = object : TextWatcher {
      override fun afterTextChanged(s: Editable?) {
        val input = s?.toString()

        if (input.isNullOrEmpty()) {
          return
        }

        try {
          val quantity = Integer.parseInt(input)
          if (quantity < 0) {
            Snackbar.make(
                view!!,
                "Must provide valid positive quantity ($input)",
                Snackbar.LENGTH_LONG).show()
            return
          }

          mItemQuantityMap[mInventory!!.items[adapterPosition - 1].key!!] = quantity
          updateTotal(mOrderTotalVH)

        } catch (e: NumberFormatException) {
          Toast.makeText(activity, "Must provide valid quantity ($input) ${orderQuantity.text}", Toast.LENGTH_LONG)
              .show()
          orderQuantity.setText("0".toCharArray(), 0, 1)
        }
      }

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(input: CharSequence?, start: Int, before: Int, count: Int) {
      }
    }
  }
}