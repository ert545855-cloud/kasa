package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.NexoRoomDatabase
import com.example.data.local.entity.BranchEntity
import com.example.data.local.entity.BusinessEntity
import com.example.data.local.entity.MemberEntity
import com.example.data.repository.CartItemInput
import com.example.data.repository.NexoRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: NexoRoomDatabase

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, NexoRoomDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NEXO OS", appName)
  }

  @Test
  fun `verify room persistence for business branch and member entities`() = runBlocking {
    val businessId = "biz-test-01"
    val business = BusinessEntity(
      id = businessId,
      name = "NEXO Test Cafe",
      slug = "nexo-test-cafe",
      businessType = "CAFE",
      ownerId = "user-123",
      phone = "+90 555 111 2233",
      address = "Kadıköy, İstanbul",
      currency = "₺"
    )
    db.businessDao().insertBusiness(business)

    val fetchedBiz = db.businessDao().getBusinessById(businessId)
    assertNotNull(fetchedBiz)
    assertEquals("NEXO Test Cafe", fetchedBiz?.name)

    val branch = BranchEntity(
      id = "branch-01",
      businessId = businessId,
      name = "Merkez Şube",
      address = "Kadıköy, İstanbul",
      isMain = true
    )
    db.branchDao().insertBranch(branch)

    val fetchedBranch = db.branchDao().getBranchById("branch-01")
    assertNotNull(fetchedBranch)
    assertEquals("Merkez Şube", fetchedBranch?.name)
    assertEquals(businessId, fetchedBranch?.businessId)

    val member = MemberEntity(
      id = "member-01",
      businessId = businessId,
      userId = "user-123",
      role = "OWNER",
      name = "Test Kurucu",
      email = "kurucu@nexo.com"
    )
    db.memberDao().insertMember(member)

    val fetchedMember = db.memberDao().getMembersForBusiness(businessId)
    assertNotNull(fetchedMember)
  }

  @Test
  fun `verify server side order price calculation`() {
    val biz = NexoRepository.getActiveBusiness()
    val products = NexoRepository.products.value
    assertTrue("Products should not be empty", products.isNotEmpty())

    val firstProduct = products.first()
    val cartInput = listOf(
      CartItemInput(
        productId = firstProduct.id,
        quantity = 2
      )
    )

    val result = NexoRepository.createCustomerOrder(
      businessId = biz.id,
      tableNumber = 5,
      cartItems = cartInput,
      customerName = "Test Misafir",
      customerPhone = "+905551234567",
      notes = "Test notu"
    )

    assertTrue("Order should be created successfully", result.isSuccess)
    val order = result.getOrNull()!!
    assertEquals(firstProduct.price * 2, order.total, 0.01)
    assertEquals(5, order.tableNumber)
  }

  @Test
  fun `verify prompt CUJ table 12 latte and cheesecake order with real time sync`() {
    NexoRepository.resetToDemoData()
    val cafe = NexoRepository.casaCafeBusiness
    assertEquals("casa-cafe", cafe.slug)
    assertEquals("casa-cafe.nexo.business", cafe.websiteDomain)

    val products = NexoRepository.products.value.filter { it.businessId == cafe.id }
    val latte = products.first { it.name == "Latte" }
    val cheesecake = products.first { it.name == "Cheesecake" }

    assertEquals(140.0, latte.price, 0.01)
    assertEquals(140.0, cheesecake.price, 0.01)

    // Customer scans Table 12 QR and submits order for 2x Latte + 1x Cheesecake
    val cartInputs = listOf(
      CartItemInput(productId = latte.id, quantity = 2),
      CartItemInput(productId = cheesecake.id, quantity = 1)
    )

    val orderResult = NexoRepository.createCustomerOrder(
      businessId = cafe.id,
      tableNumber = 12,
      cartItems = cartInputs,
      customerName = "Masa 12 Misafiri",
      customerPhone = "+905551234567",
      notes = "Sıcak servis edilsin"
    )

    assertTrue("Customer order should be created", orderResult.isSuccess)
    val order = orderResult.getOrNull()!!

    // Total must be 420 TL
    assertEquals(420.0, order.total, 0.01)
    assertEquals(12, order.tableNumber)
    assertEquals(com.example.data.model.OrderStatus.PENDING, order.status)
    assertTrue("Order number starts with NX-", order.orderNumber.startsWith("NX-"))

    // Real-time Push Notification was dispatched to restaurant mobile app
    val notif = NexoRepository.latestPushNotification.value
    assertNotNull("Push notification must be triggered", notif)
    assertEquals("Table 12", notif?.tableInfo)
    assertTrue(notif!!.totalAmountFormatted.contains("420"))

    // Restaurant employee accepts the order in mobile app
    NexoRepository.acceptLatestNotificationOrder()
    val updatedOrderAccepted = NexoRepository.orders.value.first { it.id == order.id }
    assertEquals(com.example.data.model.OrderStatus.ACCEPTED, updatedOrderAccepted.status)

    // Status sync: PREPARING
    NexoRepository.updateOrderStatus(order.id, com.example.data.model.OrderStatus.PREPARING)
    assertEquals(com.example.data.model.OrderStatus.PREPARING, NexoRepository.orders.value.first { it.id == order.id }.status)

    // Status sync: READY
    NexoRepository.updateOrderStatus(order.id, com.example.data.model.OrderStatus.READY)
    assertEquals(com.example.data.model.OrderStatus.READY, NexoRepository.orders.value.first { it.id == order.id }.status)

    // Status sync: COMPLETED
    NexoRepository.updateOrderStatus(order.id, com.example.data.model.OrderStatus.COMPLETED)
    assertEquals(com.example.data.model.OrderStatus.COMPLETED, NexoRepository.orders.value.first { it.id == order.id }.status)
  }
}
