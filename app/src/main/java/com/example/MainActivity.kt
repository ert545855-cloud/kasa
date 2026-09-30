package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.example.data.repository.NexoRepository
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
    val activeBusiness = NexoRepository.getActiveBusiness()
    val activeRole by NexoRepository.currentStaffRole.collectAsState()

    LaunchedEffect(Unit) {
        authRepository.silentAutoLogin()
        // Local Room persistence for offline functionality
        authRepository.cacheMenuCategories(activeBusiness.id, NexoRepository.categories.value)
        authRepository.cacheMenuItems(activeBusiness.id, NexoRepository.products.value)
        authRepository.cacheRestaurantTables(activeBusiness.id, NexoRepository.tables.value)
    }

    var showLandingPage by remember { mutableStateOf(false) }
    var showLoginScreen by remember { mutableStateOf(false) }
    var showCustomerView by remember { mutableStateOf(false) }
    var customerTableNumber by remember { mutableIntStateOf(3) }
    var showSuperAdmin by remember { mutableStateOf(false) }
    var showOnboardingWizard by remember { mutableStateOf(false) }

    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }

    // Back handling
    if (showCustomerView) {
        BackHandler { showCustomerView = false }
        CustomerMenuScreen(
            initialTableNumber = customerTableNumber,
            onBackToDashboard = { showCustomerView = false }
        )
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

    if (showSuperAdmin) {
        BackHandler { showSuperAdmin = false }
        SuperAdminScreen(onBack = { showSuperAdmin = false })
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
                        Text("Hesap / Giriş", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showSuperAdmin = true
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
                NexoTopBar(
                    title = activeBusiness.name,
                    subtitle = "${activeBusiness.businessType.titleTr} • ${activeBusiness.branches.firstOrNull()?.name ?: "Merkez"}",
                    activeRole = activeRole,
                    onRoleChange = { NexoRepository.setStaffRole(it) },
                    onOpenCustomerView = {
                        customerTableNumber = 3
                        showCustomerView = true
                    },
                    onOpenAdmin = { showSuperAdmin = true },
                    onResetDemo = { NexoRepository.resetToDemoData() },
                    onOpenLogin = { showLoginScreen = true },
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
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
                            customerTableNumber = 3
                            showCustomerView = true
                        }
                    )
                    AppDestination.MENU -> MenuManagementScreen()
                    AppDestination.ROOM_MENU -> RoomMenuCatalogScreen(onNavigateBack = { currentDestination = AppDestination.DASHBOARD })
                    AppDestination.TABLES -> QrTablesScreen(
                        onPreviewCustomerMenu = { tblNum ->
                            customerTableNumber = tblNum
                            showCustomerView = true
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
