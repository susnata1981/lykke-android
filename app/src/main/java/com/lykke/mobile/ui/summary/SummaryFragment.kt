package com.lykke.mobile.ui.summary

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.lykke.mobile.Host
import com.lykke.mobile.R
import com.lykke.mobile.util.format
import com.lykke.mobile.util.inflate


class SummaryFragment : Fragment() {

  companion object {
    fun newInstance(): SummaryFragment {
      return SummaryFragment()
    }
  }

  private lateinit var mGrossTV: TextView
  private lateinit var mTaxAmountTV: TextView
  private lateinit var mTotalTV: TextView
  private lateinit var mOrderItemsContainer: LinearLayout
  private lateinit var mPaymentTV: TextView

  private lateinit var mViewModel: SummaryViewModel
  private var mHost: Host? = null
  private var mFinishCheckinBtn: Button? = null


  override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = container!!.inflate(R.layout.summary_layout)
    mGrossTV = view.findViewById(R.id.grossAmount)
    mTaxAmountTV = view.findViewById(R.id.taxAmount)
    mTotalTV = view.findViewById(R.id.totalAmount)
    mPaymentTV = view.findViewById(R.id.payment_amount)
    mOrderItemsContainer = view.findViewById(R.id.orderItemsContainer)
    mFinishCheckinBtn = view.findViewById(R.id.finishCheckinBtn)

    mFinishCheckinBtn!!.setOnClickListener {
      mViewModel.handleFinish()
    }

    return view
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is Host) {
      mHost = context
      mViewModel = ViewModelProviders.of(this).get(SummaryViewModel::class.java)
      mHost?.let {
        mViewModel.fetchData(it.getCurrentBusiness()!!)
      }
    }

    mViewModel.getUIViewModel().getOrder().observe(this, Observer { order ->
      order?.let {
        mGrossTV!!.text = it.gross.format()
        mTaxAmountTV!!.text = (it.total - it.gross).format()
        mTotalTV!!.text = it.total.format()

        populateItems(it.items)
      }
    })

    mViewModel.getUIViewModel().getStatus().observe(this, Observer { status ->
      status?.let {
        showStatus(it)
      }
    })

    mViewModel.getUIViewModel().getPayment().observe(this, Observer {
      mPaymentTV.text = it
    })
  }

  private fun showStatus(message: String) {
    val builder: AlertDialog.Builder
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      builder = AlertDialog.Builder(activity, android.R.style.Theme_Material_Light_Dialog_Alert)
    } else {
      builder = AlertDialog.Builder(activity)
    }
    builder.setTitle("Completed checkin!")
        .setMessage("Great work!")
        .setPositiveButton(R.string.continue_button_title, DialogInterface.OnClickListener { dialog, which ->
          mHost?.next()
        })
        .setIcon(android.R.drawable.ic_dialog_info)
        .show()
  }

  private fun populateItems(items: Map<String, Int>) {
    if (items.isEmpty()) {
      mOrderItemsContainer!!.removeAllViews()
      val tv = TextView(activity)
      tv.text = activity!!.resources.getString(R.string.no_items)
      tv.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium)
      tv.setTextColor(activity!!.resources.getColor(R.color.textDarkPrimary))
      val lp = LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
      lp.marginStart = context!!.resources.getDimensionPixelOffset(R.dimen.default_margin)
      tv.layoutParams = lp
      tv.translationX = activity!!.window.decorView.width.toFloat()
      mOrderItemsContainer.addView(tv)

      tv.animate()
          .translationX(0f)
          .setDuration(500)
          .start()
      return
    }

    mOrderItemsContainer?.removeAllViews()
    if (items.isEmpty()) {
      val title = TextView(activity)
      title.setText(R.string.no_items)
      title.setTextAppearance(android.R.style.TextAppearance_Material_Subhead)
      mOrderItemsContainer.addView(title)
    } else {
      items.forEach { item, quantity ->
        val view = LayoutInflater.from(activity).inflate(
            R.layout.summary_order_item, mOrderItemsContainer, false)
        val itemNameTV = view.findViewById<TextView>(R.id.summary_order_item_name)
        val itemQuantityTV = view.findViewById<TextView>(R.id.summary_order_item_quantity)
        itemNameTV.text = item
        itemQuantityTV.text = quantity.toString()
        mOrderItemsContainer!!.addView(view)
      }
    }
  }
}
