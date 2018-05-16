package com.lykke.mobile.ui.payment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.lykke.mobile.Host
import com.lykke.mobile.R
import com.lykke.mobile.domain.model.Payment
import com.lykke.mobile.util.format
import com.lykke.mobile.util.inflate
import java.util.*

class EnterPaymentFragment : Fragment() {

  companion object {
    fun newInstance(): EnterPaymentFragment {
      return EnterPaymentFragment()
    }
  }

  private var mBusinessName: TextView? = null
  private var mOutstandingBalance: TextView? = null
  private var mLastPaymentAmount: TextView? = null
  private var mLastPaymentDate: TextView? = null
  private var mCurrentPayment: TextView? = null
  private var mNextBtn: Button? = null

  private var mHost: Host? = null
  private var mViewModel: EnterPaymentViewModel? = null

  private lateinit var mCurrentPaymentAmount: EditText

  override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val view = container!!.inflate(R.layout.enter_payment)
    mBusinessName = view.findViewById(R.id.enter_payment_business_name)
    mOutstandingBalance = view.findViewById(R.id.enter_payment_outstanding_balance)
    mCurrentPaymentAmount = view.findViewById(R.id.enter_payment_current_payment)
    mLastPaymentAmount = view.findViewById(R.id.enter_payment_last_order)
    mLastPaymentDate = view.findViewById(R.id.enter_payment_last_payment_date)
    mCurrentPayment = view.findViewById(R.id.enter_payment_current_payment)
    mCurrentPayment!!.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) {
        try {
          val amount = s.toString().toDouble()
          if (amount < 0) {
            Snackbar.make(
                getView()!!,
                resources.getString(R.string.invalid_payment),
                Snackbar.LENGTH_LONG).show()
            mCurrentPayment!!.text = "0"
          }
        } catch(ex: NumberFormatException) {
          Snackbar.make(
              getView()!!, resources.getString(R.string.invalid_payment), Snackbar.LENGTH_SHORT)
              .show()
          mCurrentPayment!!.text = "0"
        }
      }

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val input = s.toString()
        if (input.isNullOrEmpty()) {
          return
        }

        val indexOfDecimal = input.indexOf(".")
        if (indexOfDecimal == -1) {
          return
        }

        if (input.length - indexOfDecimal - 1 > 2) {
          mCurrentPayment!!.text = input.substring(0, Math.min(indexOfDecimal + 3, input.length))
        }
      }
    })

    mNextBtn = view.findViewById(R.id.enter_payment_next_btn)
    mNextBtn!!.setOnClickListener {
      var amount = 0.0
      if (mCurrentPayment!!.text.isNotEmpty()) {
        amount = mCurrentPayment!!.text.toString().toDouble()
      }

      mViewModel!!.getUIViewModel().updatePayment(Payment(amount, Date().time))
      val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      imm.hideSoftInputFromWindow(view.windowToken, 0)
      mHost?.next()
    }
    return view
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    if (context is Host) {
      mHost = context
      mViewModel = ViewModelProviders.of(this).get(EnterPaymentViewModel::class.java)
      mHost?.let {
        mViewModel!!.fetchData(it.getCurrentBusiness()!!)
      }
    }
  }

  override fun onStart() {
    super.onStart()
    mBusinessName!!.text = mHost!!.getCurrentBusiness()?.key
    mOutstandingBalance!!.text = mHost!!.getCurrentBusiness()?.outstandingBalance?.format()

    mViewModel!!.getUIViewModel().getLastPayment().observe(this, Observer { payment ->
      payment?.let {
        mLastPaymentAmount!!.text = it
      }
    })

    mViewModel!!.getUIViewModel().getLastPaymentDate().observe(this, Observer { payment ->
      payment?.let {
        mLastPaymentDate!!.text = it
      }
    })

    mViewModel!!.getUIViewModel().getStatus().observe(this, Observer { status ->
      status?.let {
        if (!it.isNullOrEmpty()) {
          showStatus(it)
        }
      }
    })

    mViewModel!!.getUIViewModel().getCurrentPayment().observe(this, Observer {
      mCurrentPaymentAmount.setText(it)
    })

    mHost?.setToolbarTitle(resources.getString(R.string.enter_payment_fragment_title))
  }

  private fun showStatus(message: String) {
    Snackbar.make(view!!, message, Snackbar.LENGTH_SHORT).show()
  }
}
