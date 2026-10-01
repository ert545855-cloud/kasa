package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.NexoModule
import com.example.data.model.NexoPushNotification
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.example.data.repository.NexoRepository
import com.example.data.repository.OrderRepository
import com.example.ui.components.NexoTopBar
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class AppDestination(val titleTr: String, val icon: ImageVector) {
    DASHBOARD("Özet", Icons.Default.Dashboard),
    MENU("Menü", Icons.Default.RestaurantMenu),
    ROOM_MENU("Room Menü (Çevrimdışı)", Icons.Default.MenuBook),
    TABLES("Masalar & QR", Icons.Default.QrCode2),
    ORDERS("Sipariş & Mutfak", Icons.Default.SoupKitchen),
    CRM("Müşteri", Icons.Default.People),
    STOCK("Stok", Icons.Default.Inventory2),
    BOOKING("Randevu", Icons.Default.CalendarToday),
    LOYALTY("Sadakat", Icons.Default.CardGiftcard),
    SALES("Kasa POS", Icons.Default.PointOfSale),
    QUOTES("Teklifler", Icons.Default.Description),
    CAMPAIGNS("Kampanya", Icons.Default.Campaign),
    AI("Yapay Zeka", Icons.Default.AutoAwesome),
    ANALYTICS("Raporlar", Icons.Default.Assessment),
    STAFF("Personel", Icons.Default.Badge),
    SETTINGS("Ayarlar", Icons.Default.Settings)
}

enum class SystemEnvironment(val title: String, val subtitle: String, val icon: ImageVector) {
    RESTAURANT_APP("NEXO BUSINESS", "Restoran Paneli & Mobil App", Icons.Default.Storefront),
    CUSTOMER_WEBSITE("MÜŞTERİ SİTESİ", "casa-cafe.nexo.business", Icons.Default.Language),
    SUPER_ADMIN("SUPER ADMIN", "NEXO SaaS Platformu", Icons.Default.AdminPanelSettings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexoTheme {
                NexoAppRoot()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexoAppRoot() {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val orderRepository = remember { OrderRepository.getInstance(context) }
    val currentUser by authRepository.currentUser.collectAsState()
    val activeBusiness = NexoRepository.getActiveBusiness()
    val activeRole by NexoRepository.currentStaffRole.collectAsState()
    val latestNotification by NexoRepository.latestPushNotification.collectAsState()

    LaunchedEffect(activeBusiness.id) {
        authRepository.silentAutoLogin()
        // Local Room persistence for offline functionality - cache business first
        authRepository.cacheBusiness(activeBusiness)
        authRepository.cacheMenuCategories(activeBusiness.id, NexoRepository.categories.value)
        authRepository.cacheMenuItems(activeBusiness.id, NexoRepository.products.value)
        authRepository.cacheRestaurantTables(activeBusiness.id, NexoRepository.tables.value)

        // Real-time Firestore <-> Room synchronization & reactive flows
        NexoRepository.bindOrderRepository(orderRepository)
        orderRepository.startRealtimeSync(activeBusiness.id, this)

        launch {
            orderRepository.newOrdersFlow.collect { orderEntity ->
                NexoRepository.syncFromOrderEntity(orderEntity)
            }
        }
        launch {
            orderRepository.statusUpdatesFlow.collect { orderEntity ->
                NexoRepository.syncFromOrderEntity(orderEntity)
            }
        }
    }

    var currentEnvironment by remember { mutableStateOf(SystemEnvironment.RESTAURANT_APP) }
    var customerTableNumber by remember { mutableIntStateOf(12) }

    var showLandingPage by remember { mutableStateOf(false) }
    // Start with Login Screen if not authenticated (Firebase Auth flow)
    var showLoginScreen by remember { mutableStateOf(currentUser == null) }
    var showOnboardingWizard by remember { mutableStateOf(false) }

    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }

    // ========================================================
    // ENVIRONMENT SEPARATION ROUTING
    // ========================================================
    if (currentEnvironment == SystemEnvironment.CUSTOMER_WEBSITE) {
        BackHandler { currentEnvironment = SystemEnvironment.RESTAURANT_APP }
        CustomerMenuScreen(
            initialTableNumber = customerTableNumber,
            onBackToDashboard = { currentEnvironment = SystemEnvironment.RESTAURANT_APP }
        )
        return
    }

    if (currentEnvironment == SystemEnvironment.SUPER_ADMIN) {
        BackHandler { currentEnvironment = SystemEnvironment.RESTAURANT_APP }
        SuperAdminScreen(onBack = { currentEnvironment = SystemEnvironment.RESTAURANT_APP })
        return
    }

    if (showLoginScreen) {
        BackHandler { showLoginScreen = false }
        LoginScreen(
            authRepository = authRepository,
            onLoginSuccess = {
                showLoginScreen = false
                currentDestination = AppDestination.DASHBOARD
            },
            onOpenLiveDemo = {
                showLoginScreen = false
                currentDestination = AppDestination.DASHBOARD
            }
        )
        return
    }

    if (showLandingPage) {
        BackHandler { showLandingPage = false }
        LandingScreen(
            authRepository = authRepository,
            onStartFree = {
                showLandingPage = false
                showOnboardingWizard = true
            },
            onOpenLiveDemo = {
                NexoRepository.resetToDemoData()
                showLandingPage = false
                currentDestination = AppDestination.DASHBOARD
            },
            onLoginClick = {
                showLandingPage = false
                showLoginScreen = true
            }
        )
        return
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    if (currentDestination != AppDestination.DASHBOARD) {
        BackHandler { currentDestination = AppDestination.DASHBOARD }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                // Header in Drawer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("N", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = activeBusiness.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${activeBusiness.businessType.titleTr} • ${activeRole.titleTr}",
                                fontSize = 11.sp,
                                color = NexoTextSecondary
                            )
                        }
                    }
                }

                HorizontalDivider(color = NexoBorderSubtle)

                // Destinations Scrollable List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(AppDestination.entries) { dest ->
                        val isSelected = currentDestination == dest
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = dest.titleTr,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            icon = {
                                Icon(
                                    dest.icon,
                                    contentDescription = dest.titleTr,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else NexoTextSecondary
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                currentDestination = dest
                                coroutineScope.launch { drawerState.close() }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("drawer_item_${dest.name.lowercase()}")
                        )
                    }
                }

                HorizontalDivider(color = NexoBorderSubtle)

                // Bottom Drawer Account & Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showLoginScreen = true
                        }
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Giriş / Hesap", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            currentEnvironment = SystemEnvironment.SUPER_ADMIN
                        }
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Yönetim", tint = NexoTextSecondary)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    // System Environment Mode Selector Pill Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SystemEnvironment.entries.forEach { env ->
                                val isSelected = currentEnvironment == env
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            if (env == SystemEnvironment.CUSTOMER_WEBSITE) {
                                                customerTableNumber = 12
                                            }
                                            currentEnvironment = env
                                        }
                                        .testTag("env_tab_${env.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            env.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (isSelected) Color.White else NexoTextSecondary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = when (env) {
                                                SystemEnvironment.RESTAURANT_APP -> "Nexo İşletme"
                                                SystemEnvironment.CUSTOMER_WEBSITE -> "Müşteri Menü"
                                                SystemEnvironment.SUPER_ADMIN -> "SaaS Admin"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = if (isSelected) Color.White else NexoTextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    NexoTopBar(
                        title = activeBusiness.name,
                        subtitle = "${activeBusiness.businessType.titleTr} • ${activeBusiness.branches.firstOrNull()?.name ?: "Merkez"}",
                        activeRole = activeRole,
                        onRoleChange = { NexoRepository.setStaffRole(it) },
                        onOpenCustomerView = {
                            customerTableNumber = 12
                            currentEnvironment = SystemEnvironment.CUSTOMER_WEBSITE
                        },
                        onOpenAdmin = { currentEnvironment = SystemEnvironment.SUPER_ADMIN },
                        onResetDemo = { NexoRepository.resetToDemoData() },
                        onOpenLogin = { showLoginScreen = true },
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Active Screen in NEXO BUSINESS
                when (currentDestination) {
                    AppDestination.DASHBOARD -> DashboardScreen(
                        onNavigateToModule = { mod ->
                            currentDestination = when (mod) {
                                NexoModule.MENU -> AppDestination.MENU
                                NexoModule.ORDERS -> AppDestination.ORDERS
                                NexoModule.CRM -> AppDestination.CRM
                                NexoModule.STOCK -> AppDestination.STOCK
                                NexoModule.BOOKING -> AppDestination.BOOKING
                                NexoModule.LOYALTY -> AppDestination.LOYALTY
                                NexoModule.SALES -> AppDestination.SALES
                                NexoModule.STAFF -> AppDestination.STAFF
                                NexoModule.AI -> AppDestination.AI
                                NexoModule.ANALYTICS -> AppDestination.ANALYTICS
                                NexoModule.CAMPAIGNS -> AppDestination.CAMPAIGNS
                                NexoModule.QUOTES -> AppDestination.QUOTES
                                NexoModule.SETTINGS -> AppDestination.SETTINGS
                            }
                        },
                        onOpenCustomerView = {
                            customerTableNumber = 12
                            currentEnvironment = SystemEnvironment.CUSTOMER_WEBSITE
                        }
                    )
                    AppDestination.MENU -> MenuManagementScreen()
                    AppDestination.ROOM_MENU -> RoomMenuCatalogScreen(onNavigateBack = { currentDestination = AppDestination.DASHBOARD })
                    AppDestination.TABLES -> QrTablesScreen(
                        onPreviewCustomerMenu = { tblNum ->
                            customerTableNumber = tblNum
                            currentEnvironment = SystemEnvironment.CUSTOMER_WEBSITE
                        }
                    )
                    AppDestination.ORDERS -> OrdersAndKdsScreen()
                    AppDestination.CRM -> CrmScreen()
                    AppDestination.STOCK -> StockScreen()
                    AppDestination.BOOKING -> BookingScreen()
                    AppDestination.LOYALTY -> LoyaltyScreen()
                    AppDestination.SALES -> SalesPosScreen()
                    AppDestination.QUOTES -> QuotesScreen()
                    AppDestination.CAMPAIGNS -> CampaignsScreen()
                    AppDestination.AI -> AiStudioScreen()
                    AppDestination.ANALYTICS -> AnalyticsScreen()
                    AppDestination.STAFF -> StaffScreen()
                    AppDestination.SETTINGS -> SettingsAndPlansScreen()
                }

                // ========================================================
                // NEW ORDER PUSH NOTIFICATION FLOATING BANNER
                // ========================================================
                if (latestNotification != null) {
                    val notif = latestNotification!!
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(12.dp)
                            .fillMaxWidth()
                            .testTag("new_order_push_notification_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NexoDarkSurface),
                        border = BorderStroke(2.dp, NexoAmber),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = CircleShape, color = NexoAmber.copy(alpha = 0.2f)) {
                                        Icon(
                                            Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = NexoAmber,
                                            modifier = Modifier.padding(6.dp).size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("🔔 NEW ORDER", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = NexoAmber)
                                        Text(notif.tableInfo, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    }
                                }

                                IconButton(
                                    onClick = { NexoRepository.dismissPushNotification() },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = NexoDarkTextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = notif.itemsSummary,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.95f),
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Total: ${notif.totalAmountFormatted}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NexoEmeraldLight
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        currentDestination = AppDestination.ORDERS
                                        NexoRepository.dismissPushNotification()
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("view_order_notification_btn"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text("VIEW ORDER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        NexoRepository.acceptLatestNotificationOrder()
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("accept_order_notification_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = NexoEmeraldDark)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ACCEPT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Onboarding Wizard Dialog
    if (showOnboardingWizard) {
        OnboardingWizardDialog(
            onDismiss = { showOnboardingWizard = false },
            onCompleted = { name, type, phone, address, currency, modules ->
                NexoRepository.createNewBusiness(name, type, phone, address, currency, modules)
                showOnboardingWizard = false
                currentDestination = AppDestination.DASHBOARD
            }
        )
    }
}
