package mobile.lykke.com.lykke.mobile.data

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.lykke.mobile.data.CheckinEntity
import com.lykke.mobile.data.CheckinStatus
import com.lykke.mobile.data.FirebaseRepository
import com.lykke.mobile.data.OrderEntity
import com.lykke.mobile.data.PaymentEntity
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.hasEntry
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.concurrent.CountDownLatch
import org.hamcrest.CoreMatchers.`is` as Is

class FirebaseRepositoryTest {
  private lateinit var repository: FirebaseRepository

  private var countDownLatch: CountDownLatch? = null

  companion object {
    const val USER_KEY = "userKey"
    const val BUSINESS_KEY = "business_key"
    const val GROSS = 100.0
    const val TOTAL = 110.5
    const val ITEM1 = "ITEM1"
    const val ITEM1_QUANTITY = 10
    const val PAYMENT_AMOUNT = 25.0
  }

  private lateinit var ITEM_MAP: MutableMap<String, Int>

  @Before
  fun setup() {
    repository = FirebaseRepository(FirebaseDatabase.getInstance())
    ITEM_MAP = mutableMapOf()
    ITEM_MAP[ITEM1] = ITEM1_QUANTITY
  }

  @Test
  fun createCheckinTest() {
    var checkinRef: String? = null
    countDownLatch = CountDownLatch(1)
    repository.getCheckin(USER_KEY, BUSINESS_KEY, Date().time)
        .subscribe {
          if (it.key.isNotEmpty()) {
            checkinRef = it.key
            verifyCheckin(it)
            countDownLatch!!.countDown()
          }
        }

    repository.createCheckin(USER_KEY, BUSINESS_KEY)
        .subscribe {
          if (it.key.isNotEmpty()) {
            Log.d("XXX", "New checking -> $it")
            countDownLatch!!.countDown()
          }
        }
    countDownLatch!!.await()
    repository.deleteCheckin(checkinRef!!)
  }

  @Test
  fun updateCheckinTest() {
    var checkinRef: String? = null
    var checkin: CheckinEntity? = null
    var updated = false
    countDownLatch = CountDownLatch(2)
    repository.getCheckin(USER_KEY, BUSINESS_KEY, Date().time)
        .subscribe {
          if (it.key.isNotEmpty()) {
            checkinRef = it.key
            checkin = it
            verifyCheckin(it)
            if (!updated) {
              updateCheckin(it)
              updated = true
            }
            countDownLatch!!.countDown()
          }
        }

    repository.createCheckin(USER_KEY, BUSINESS_KEY)
        .subscribe {
        }
    countDownLatch!!.await()

    assertThat(checkin!!.order!!.total, Is(TOTAL))
    assertThat(checkin!!.order!!.gross, Is(GROSS))
    assertThat(checkin!!.order!!.items, hasEntry(ITEM1, ITEM1_QUANTITY))
    assertThat(checkin!!.payment!!.amount, Is(PAYMENT_AMOUNT))

    repository.deleteCheckin(checkinRef!!)
  }

  @Test
  fun deleteCheckinTest() {
    countDownLatch = CountDownLatch(1)
    var checkinRef: String? = null
    repository.createCheckin(USER_KEY, BUSINESS_KEY)
        .subscribe {
          if (it.key.isNotEmpty()) {
            checkinRef = it.key

            repository.deleteCheckin(checkinRef!!).subscribe { success ->
              assertThat(success, Is(true))
              countDownLatch!!.countDown()
            }
          }
        }

    countDownLatch!!.await()
  }

  private fun updateCheckin(checkin: CheckinEntity) {
    val order = OrderEntity(
        GROSS,
        TOTAL,
        ITEM_MAP)
    val payment = PaymentEntity(
        amount = PAYMENT_AMOUNT)


    val newCheckin = CheckinEntity(
        checkin.key,
        checkin.userKey,
        checkin.businessKey,
        checkin.status,
        checkin.timeCompleted,
        order,
        payment)

    repository.updateCheckin(newCheckin).subscribe {
    }
  }

  fun verifyCheckin(checkin: CheckinEntity) {
    assertThat(checkin.userKey, CoreMatchers.`is`(USER_KEY))
    assertThat(checkin.businessKey, CoreMatchers.`is`(BUSINESS_KEY))
    assertThat(checkin.status, CoreMatchers.`is`(CheckinStatus.IN_PROGRESS))
  }
}

