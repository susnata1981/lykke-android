package mobile.lykke.com.lykke

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.lykke.mobile.data.FirebaseRepository

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.util.concurrent.CountDownLatch

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
  val lock = CountDownLatch(1)
  @Test
  fun useAppContext() {
    // Context of the app under test.
    val appContext = InstrumentationRegistry.getTargetContext()
    assertEquals("com.lykke.mobile.test", appContext.packageName)
  }

  private lateinit var repository: FirebaseRepository

  @Before
  fun setup() {
    FirebaseApp.initializeApp(InstrumentationRegistry.getTargetContext())
    repository = FirebaseRepository(FirebaseDatabase.getInstance())
  }

  @Test
  fun test() {
    Log.d("XXX", "creating checkin...")
    repository.createCheckin("userKey", "businessKey").subscribe {
      Log.d("XXX", "Created checking $it")
      assertNotNull(it)
      lock.countDown()
    }
    lock.await()
  }
}
