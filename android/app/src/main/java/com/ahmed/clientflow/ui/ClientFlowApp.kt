package com.ahmed.clientflow.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ahmed.clientflow.R
import com.ahmed.clientflow.MainViewModel
import com.ahmed.clientflow.ui.screen.BackupScreen
import com.ahmed.clientflow.ui.screen.ExpenseFormScreen
import com.ahmed.clientflow.ui.screen.ExpensesScreen
import com.ahmed.clientflow.ui.component.PipelineRow
import com.ahmed.clientflow.data.AppState
import com.ahmed.clientflow.data.AppLanguage
import com.ahmed.clientflow.data.AppTheme
import com.ahmed.clientflow.data.AuthState
import com.ahmed.clientflow.data.Booking
import com.ahmed.clientflow.data.Client
import com.ahmed.clientflow.data.ClientStatus
import com.ahmed.clientflow.data.Expense
import com.ahmed.clientflow.data.ExpenseCategory
import com.ahmed.clientflow.data.Invoice
import com.ahmed.clientflow.data.MessageTemplate
import com.ahmed.clientflow.data.Payment
import com.ahmed.clientflow.data.PaymentStatus
import com.ahmed.clientflow.data.RecurrenceType
import com.ahmed.clientflow.data.FreelancerInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val pipeline = ClientStatus.entries

private data class BookingOccurrence(
    val booking: Booking,
    val occurrenceDate: String
)

private const val lottieJson = """
{"v":"5.7.4","fr":30,"ip":0,"op":120,"w":200,"h":200,"nm":"pulse","ddd":0,"assets":[],"layers":[{"ddd":0,"ind":1,"ty":4,"nm":"Circle","sr":1,"ks":{"o":{"a":0,"k":100},"r":{"a":0,"k":0},"p":{"a":0,"k":[100,100,0]},"a":{"a":0,"k":[0,0,0]},"s":{"a":1,"k":[{"t":0,"s":[40,40,100]},{"t":60,"s":[100,100,100]},{"t":120,"s":[40,40,100]}]}},"shapes":[{"ty":"el","p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[120,120]},"nm":"Ellipse Path 1"},{"ty":"fl","c":{"a":0,"k":[0.145,0.388,0.922,1]},"o":{"a":0,"k":100},"nm":"Fill 1"}],"ip":0,"op":120,"st":0,"bm":0}]}
"""

sealed class BottomRoute(val route: String, val label: String) {
    data object Home : BottomRoute("home", "Home")
    data object Clients : BottomRoute("clients", "Clients")
    data object Bookings : BottomRoute("bookings", "Bookings")
    data object Revenue : BottomRoute("revenue", "Revenue")
    data object Analytics : BottomRoute("analytics", "Analytics")
}

@Composable
fun ClientFlowApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val language = uiState.appState.language

    LaunchedEffect(uiState.exportPayload) {
        uiState.exportPayload?.let { payload ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, payload)
            }
            context.startActivity(Intent.createChooser(intent, tx("export_data", language)))
            viewModel.dismissExport()
        }
    }

    when (uiState.authState) {
        AuthState.Setup -> SetupPinScreen(viewModel, language)
        AuthState.Locked -> LockScreen(viewModel, uiState.pinError, uiState.appState.biometricEnabled, language)
        AuthState.Unlocked -> MainScaffold(viewModel, uiState.appState, uiState.deviceId, snackbarHostState)
    }
}

@Composable
private fun SetupPinScreen(viewModel: MainViewModel, language: AppLanguage) {
    var pin by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    val composition by rememberLottieComposition(LottieCompositionSpec.JsonString(lottieJson))
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(composition = composition, iterations = Int.MAX_VALUE, modifier = Modifier.size(180.dp))
            Text(tx("secure_clientflow", language), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(tx("set_pin", language), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            PinField(pin) { if (it.length <= 4) pin = it.filter(Char::isDigit) }
            Spacer(Modifier.height(12.dp))
            PinField(confirm) { if (it.length <= 4) confirm = it.filter(Char::isDigit) }
            Spacer(Modifier.height(20.dp))
            Button(onClick = { viewModel.setupPin(pin) }, enabled = pin.length == 4 && pin == confirm) {
                Text(tx("save_pin", language))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LockScreen(viewModel: MainViewModel, pinError: Boolean, biometricEnabled: Boolean, language: AppLanguage) {
    var pin by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Lock, null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text(tx("app_locked", language), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(tx("enter_pin_continue", language), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            PinField(pin) { if (it.length <= 4) pin = it.filter(Char::isDigit) }
            AnimatedVisibility(pinError) {
                Text(tx("wrong_pin", language), color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = { viewModel.unlock(pin); pin = "" }, enabled = pin.length == 4) {
                Text(tx("unlock", language))
            }

            if (biometricEnabled && activity != null) {
                val manager = remember { com.ahmed.clientflow.security.BiometricAuthManager(activity) }
                if (manager.isAvailable()) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = {
                        manager.authenticate(
                            title = tx("app_locked", language),
                            subtitle = tx("enter_pin_continue", language),
                            onSuccess = viewModel::unlockByBiometric,
                            onError = {},
                            onFailed = {}
                        )
                    }) {
                        Icon(Icons.Default.Fingerprint, null)
                        Spacer(Modifier.width(8.dp))
                        Text(tx("use_fingerprint", language))
                    }
                }
            }
        }
    }
}

@Composable
private fun MainScaffold(viewModel: MainViewModel, state: AppState, deviceId: String, snackbarHostState: SnackbarHostState) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: BottomRoute.Home.route

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val navGold = Color(0xFFF5C518)
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                tonalElevation = 0.dp
            ) {
                listOf(BottomRoute.Home, BottomRoute.Clients, BottomRoute.Bookings, BottomRoute.Revenue, BottomRoute.Analytics).forEach { route ->
                    val icon = when (route) {
                        BottomRoute.Home -> Icons.Default.Home
                        BottomRoute.Clients -> Icons.Default.People
                        BottomRoute.Bookings -> Icons.Default.CalendarMonth
                        BottomRoute.Revenue -> Icons.Default.Shield
                        BottomRoute.Analytics -> Icons.Default.Star
                    }
                    val sel = currentRoute == route.route
                    NavigationBarItem(
                        selected = sel,
                        onClick = { navController.navigate(route.route) { launchSingleTop = true } },
                        icon = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icon, null)
                                if (sel) {
                                    Spacer(Modifier.height(4.dp))
                                    Box(Modifier.size(5.dp).background(navGold, CircleShape))
                                }
                            }
                        },
                        label = { Text(route.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = navGold,
                            selectedTextColor = navGold,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = navGold.copy(alpha = 0.10f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == BottomRoute.Clients.route) {
                FloatingActionButton(onClick = {
                    if (viewModel.canAddClient(state.clients.size)) {
                        navController.navigate("clientForm")
                    } else {
                        navController.navigate("license")
                    }
                }) { Icon(Icons.Default.Add, null) }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = BottomRoute.Home.route, modifier = Modifier.padding(padding)) {
            composable(BottomRoute.Home.route) {
                DashboardScreen(
                    state = state,
                    onOpenClient = { navController.navigate("client/$it") },
                    onOpenSettings = { navController.navigate("settings") },
                    onExport = viewModel::exportData
                )
            }
            composable(BottomRoute.Clients.route) {
                ClientsScreen(
                    state = state,
                    canAddMore = viewModel.canAddClient(state.clients.size),
                    onOpenClient = { navController.navigate("client/$it") },
                    onAddClient = { navController.navigate("clientForm") },
                    onUpgrade = { navController.navigate("license") }
                )
            }
            composable(BottomRoute.Bookings.route) {
                BookingsScreen(
                    state = state,
                    onOpenClient = { navController.navigate("client/$it") },
                    onEditBooking = { clientId, bookingId -> navController.navigate("bookingForm/$clientId?bookingId=$bookingId") },
                    onDeleteBooking = viewModel::deleteBooking
                )
            }
            composable(BottomRoute.Revenue.route) {
                RevenueScreen(
                    state = state,
                    onOpenExpenses = { navController.navigate("expenses") }
                )
            }
            composable(BottomRoute.Analytics.route) {
                AnalyticsScreen(
                    state = state,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("expenses") {
                ExpensesScreen(
                    expenses = state.expenses,
                    clients = state.clients,
                    language = state.language,
                    onBack = { navController.popBackStack() },
                    onAddExpense = { navController.navigate("expenseForm") },
                    onEditExpense = { navController.navigate("expenseForm?expenseId=$it") },
                    onDeleteExpense = viewModel::deleteExpense
                )
            }
            composable("expenseForm?expenseId={expenseId}", arguments = listOf(navArgument("expenseId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })) { entry ->
                val expenseId = entry.arguments?.getString("expenseId")
                val expense = state.expenses.find { it.id == expenseId }
                ExpenseFormScreen(
                    expense = expense,
                    clients = state.clients,
                    language = state.language,
                    onBack = { navController.popBackStack() },
                    onSave = viewModel::saveExpense
                )
            }
            composable("clientForm?clientId={clientId}", arguments = listOf(navArgument("clientId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })) { entry ->
                ClientFormScreen(
                    state = state,
                    clientId = entry.arguments?.getString("clientId"),
                    onBack = { navController.popBackStack() },
                    onSave = viewModel::saveClient
                )
            }
            composable("client/{clientId}", arguments = listOf(navArgument("clientId") { type = NavType.StringType })) { entry ->
                val clientId = entry.arguments?.getString("clientId").orEmpty()
                ClientDetailScreen(
                    state = state,
                    clientId = clientId,
                    onBack = { navController.popBackStack() },
                    onEditClient = { navController.navigate("clientForm?clientId=$clientId") },
                    onOpenPayment = { navController.navigate("payment/$clientId") },
                    onOpenBooking = { bookingId ->
                        navController.navigate(
                            if (bookingId == null) "bookingForm/$clientId" else "bookingForm/$clientId?bookingId=$bookingId"
                        )
                    },
                    onStatusChange = { status -> viewModel.updateClientStatus(clientId, status) },
                    onDeleteClient = {
                        viewModel.deleteClient(clientId)
                        navController.popBackStack()
                    },
                    onDeleteBooking = viewModel::deleteBooking,
                    onEditInvoice = { invoiceId -> navController.navigate("invoiceForm/$clientId?invoiceId=$invoiceId") },
                    onDeleteInvoice = viewModel::deleteInvoice,
                    onGenerateInvoice = { id, amount, desc -> viewModel.saveInvoice(null, id, amount, desc) },
                    onSaveTemplate = viewModel::saveTemplate,
                    onDeleteTemplate = viewModel::deleteTemplate
                )
            }
            composable("payment/{clientId}", arguments = listOf(navArgument("clientId") { type = NavType.StringType })) { entry ->
                PaymentScreen(
                    state = state,
                    clientId = entry.arguments?.getString("clientId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onSave = viewModel::savePayment
                )
            }
            composable("bookingForm/{clientId}?bookingId={bookingId}", arguments = listOf(
                navArgument("clientId") { type = NavType.StringType },
                navArgument("bookingId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )) { entry ->
                BookingFormScreen(
                    state = state,
                    clientId = entry.arguments?.getString("clientId").orEmpty(),
                    bookingId = entry.arguments?.getString("bookingId"),
                    onBack = { navController.popBackStack() },
                    onSave = viewModel::saveBooking
                )
            }
            composable("invoiceForm/{clientId}?invoiceId={invoiceId}", arguments = listOf(
                navArgument("clientId") { type = NavType.StringType },
                navArgument("invoiceId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )) { entry ->
                InvoiceFormScreen(
                    state = state,
                    clientId = entry.arguments?.getString("clientId").orEmpty(),
                    invoiceId = entry.arguments?.getString("invoiceId"),
                    onBack = { navController.popBackStack() },
                    onSave = viewModel::saveInvoice
                )
            }
            composable("security") {
                SecurityScreen(
                    language = state.language,
                    biometricEnabled = state.biometricEnabled,
                    onBack = { navController.popBackStack() },
                    onLock = {
                        viewModel.lockApp()
                        navController.popBackStack()
                    },
                    onClearPin = viewModel::clearPin,
                    onEnableBiometric = viewModel::enableBiometric,
                    onDisableBiometric = viewModel::disableBiometric
                )
            }
            composable("settings") {
                SettingsScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onOpenSecurity = { navController.navigate("security") },
                    onOpenLicense = { navController.navigate("license") },
                    onOpenBackup = { navController.navigate("backup") },
                    onOpenFreelancerInfo = { navController.navigate("freelancerInfo") },
                    onSetLanguage = viewModel::setLanguage,
                    onSetTheme = viewModel::setTheme
                )
            }
            composable("backup") {
                BackupScreen(
                    language = state.language,
                    lastBackupTime = state.lastBackupTime,
                    onBack = { navController.popBackStack() },
                    onBackupComplete = viewModel::updateLastBackupTime
                )
            }
            composable("license") {
                LicenseScreen(
                    state = state,
                    deviceId = deviceId,
                    onBack = { navController.popBackStack() },
                    onActivate = { code, done -> viewModel.activatePro(code) { success, _ -> done(success) } }
                )
            }
            composable("freelancerInfo") {
                FreelancerInfoScreen(
                    freelancerInfo = state.freelancerInfo,
                    language = state.language,
                    onBack = { navController.popBackStack() },
                    onSave = viewModel::saveFreelancerInfo
                )
            }
        }
    }
}

@Composable
private fun RevenueScreen(state: AppState, onOpenExpenses: () -> Unit) {
    val language = state.language
    val paid = state.payments.filter { it.status == PaymentStatus.Paid }
    val partial = state.payments.filter { it.status == PaymentStatus.Partial }
    val unpaid = state.payments.filter { it.status == PaymentStatus.Unpaid }
    val totalRevenue = state.payments.sumOf { it.paidAmount }
    val pendingAmount = state.payments.sumOf { (it.totalAmount - it.paidAmount).coerceAtLeast(0.0) }
    val totalInvoiced = state.payments.sumOf { it.totalAmount }
    val totalExpenses = state.expenses.sumOf { it.amount }
    val netProfit = totalRevenue - totalExpenses

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Revenue", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
                    Text("$${formatAmount(totalRevenue)}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("Pending: $${formatAmount(pendingAmount)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f))
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.12f))) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$${formatAmount(netProfit)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (netProfit >= 0) Color(0xFF4CAF50) else Color(0xFFE53935))
                        Text("Net Profit", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFE53935).copy(alpha = 0.12f))) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("-$${formatAmount(totalExpenses)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                        Text("Expenses", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        item {
            OutlinedButton(onClick = onOpenExpenses, modifier = Modifier.fillMaxWidth()) {
                Text("View All Expenses")
            }
        }
    }
}

private enum class AnalyticsPeriod { ALL_TIME, THIS_MONTH, THIS_YEAR }

@Composable
private fun AnalyticsScreen(state: AppState, onBack: () -> Unit) {
    val language = state.language
    var selectedPeriod by rememberSaveable { mutableStateOf(AnalyticsPeriod.ALL_TIME) }

    val calendar = java.util.Calendar.getInstance()
    val currentMonth = calendar.get(java.util.Calendar.MONTH)
    val currentYear = calendar.get(java.util.Calendar.YEAR)

    val filteredClients = state.clients.filter { client ->
        when (selectedPeriod) {
            AnalyticsPeriod.ALL_TIME -> true
            AnalyticsPeriod.THIS_MONTH -> {
                val createdCal = java.util.Calendar.getInstance().apply { timeInMillis = client.createdAt }
                createdCal.get(java.util.Calendar.MONTH) == currentMonth && createdCal.get(java.util.Calendar.YEAR) == currentYear
            }
            AnalyticsPeriod.THIS_YEAR -> {
                val createdCal = java.util.Calendar.getInstance().apply { timeInMillis = client.createdAt }
                createdCal.get(java.util.Calendar.YEAR) == currentYear
            }
        }
    }

    val filteredPayments = state.payments.filter { payment ->
        val client = state.clients.find { it.id == payment.clientId } ?: return@filter false
        when (selectedPeriod) {
            AnalyticsPeriod.ALL_TIME -> true
            AnalyticsPeriod.THIS_MONTH -> {
                val createdCal = java.util.Calendar.getInstance().apply { timeInMillis = client.createdAt }
                createdCal.get(java.util.Calendar.MONTH) == currentMonth && createdCal.get(java.util.Calendar.YEAR) == currentYear
            }
            AnalyticsPeriod.THIS_YEAR -> {
                val createdCal = java.util.Calendar.getInstance().apply { timeInMillis = client.createdAt }
                createdCal.get(java.util.Calendar.YEAR) == currentYear
            }
        }
    }

    val filteredBookings = state.bookings.filter { booking ->
        val client = state.clients.find { it.id == booking.clientId } ?: return@filter false
        when (selectedPeriod) {
            AnalyticsPeriod.ALL_TIME -> true
            AnalyticsPeriod.THIS_MONTH -> {
                val createdCal = java.util.Calendar.getInstance().apply { timeInMillis = client.createdAt }
                createdCal.get(java.util.Calendar.MONTH) == currentMonth && createdCal.get(java.util.Calendar.YEAR) == currentYear
            }
            AnalyticsPeriod.THIS_YEAR -> {
                val createdCal = java.util.Calendar.getInstance().apply { timeInMillis = client.createdAt }
                createdCal.get(java.util.Calendar.YEAR) == currentYear
            }
        }
    }

    val totalRevenue = filteredPayments.sumOf { it.paidAmount }
    val clientsWithPayments = filteredPayments.map { it.clientId }.distinct().size
    val arpc = if (clientsWithPayments > 0) totalRevenue / clientsWithPayments else 0.0

    val statusCounts = filteredClients.groupBy { it.status }.mapValues { it.value.size }
    val totalClients = filteredClients.size.coerceAtLeast(1)
    val convertedCount = statusCounts.filterKeys { it != ClientStatus.Lead }.values.sum()
    val conversionRate = if (totalClients > 0) (convertedCount * 100 / totalClients) else 0

    val completedClients = filteredClients.filter { it.status == ClientStatus.Completed || it.status == ClientStatus.Paid }
    val clientBookingCounts = filteredBookings.groupBy { it.clientId }.mapValues { it.value.size }
    val retainedClients = completedClients.count { clientBookingCounts[it.id] ?: 0 >= 2 }
    val retentionRate = if (completedClients.isNotEmpty()) (retainedClients * 100 / completedClients.size) else 0

    val pipelineValue = filteredPayments.sumOf { it.totalAmount }
    val wonValue = filteredPayments.filter { it.status == PaymentStatus.Paid }.sumOf { it.totalAmount }
    val wonPercent = if (pipelineValue > 0) (wonValue * 100 / pipelineValue).toInt() else 0

    val statusColors = listOf(
        Color(0xFF2196F3),
        Color(0xFF00BCD4),
        Color(0xFFFF9800),
        Color(0xFF4CAF50),
        Color(0xFF9C27B0)
    )
    val statusLabels = ClientStatus.entries.map { statusLabel(it, language) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(t("analytics", language), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = selectedPeriod == AnalyticsPeriod.ALL_TIME,
                        onClick = { selectedPeriod = AnalyticsPeriod.ALL_TIME },
                        label = { Text(t("all_time", language), style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = selectedPeriod == AnalyticsPeriod.THIS_MONTH,
                        onClick = { selectedPeriod = AnalyticsPeriod.THIS_MONTH },
                        label = { Text(t("this_month", language), style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = selectedPeriod == AnalyticsPeriod.THIS_YEAR,
                        onClick = { selectedPeriod = AnalyticsPeriod.THIS_YEAR },
                        label = { Text(t("this_year", language), style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        if (filteredClients.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(t("no_analytics_data", language), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("💰", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("$${formatAmount(arpc)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(t("avg_revenue_per_client", language), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("📈", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("$conversionRate%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(t("lead_to_booked", language), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("🔄", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("$retentionRate%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(t("clients_returned", language), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("💼", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("$${formatAmount(pipelineValue)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("$wonPercent% won", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF17171D))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(t("status_distribution", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Normal)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(0.4f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                statusLabels.forEachIndexed { index, label ->
                                    val count = statusCounts[ClientStatus.entries[index]] ?: 0
                                    val percent = if (totalClients > 0) (count * 100 / totalClients) else 0
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).background(statusColors[index], CircleShape))
                                        Spacer(Modifier.width(6.dp))
                                        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6E6E80))
                                        Spacer(Modifier.width(4.dp))
                                        Text("$percent%", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF0F0F4))
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier.weight(0.6f),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(100.dp)) {
                                    var startAngle = -90f
                                    val total = statusCounts.values.sum().toFloat().coerceAtLeast(1f)
                                    statusCounts.entries.sortedBy { it.key.ordinal }.forEachIndexed { index, (_, count) ->
                                        val sweep = (count / total) * 360f
                                        drawArc(
                                            color = statusColors[index],
                                            startAngle = startAngle,
                                            sweepAngle = sweep,
                                            useCenter = true
                                        )
                                        startAngle += sweep
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text(t("pipeline_value", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        val paymentColors = listOf(Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFFE53935))
                        val paymentLabels = listOf("Paid", "Partial", "Unpaid")
                        val paymentValues = listOf(
                            filteredPayments.filter { it.status == PaymentStatus.Paid }.sumOf { it.totalAmount },
                            filteredPayments.filter { it.status == PaymentStatus.Partial }.sumOf { it.totalAmount },
                            filteredPayments.filter { it.status == PaymentStatus.Unpaid }.sumOf { it.totalAmount }
                        )
                        val maxVal = paymentValues.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
                        paymentValues.forEachIndexed { index, value ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(paymentLabels[index], modifier = Modifier.width(60.dp), style = MaterialTheme.typography.labelMedium)
                                Box(modifier = Modifier.weight(1f).height(20.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(fraction = (value / maxVal).toFloat())
                                            .height(20.dp)
                                            .background(paymentColors[index])
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("$${formatAmount(value)}", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    state: AppState,
    onOpenClient: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onExport: () -> Unit
) {
    val today = todayKey()
    val todayBookings = state.bookings
        .flatMap { expandBookingOccurrences(it) }
        .filter { it.occurrenceDate == today }
        .sortedWith(compareBy({ it.occurrenceDate }, { it.booking.time }))
    val overdue = state.payments.filter { it.status != PaymentStatus.Paid && it.dueDate.isNotBlank() && it.dueDate < today }
    val revenue = state.payments.sumOf { it.paidAmount }
    val bookingCount = state.bookings.flatMap { expandBookingOccurrences(it) }.size
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.clientflow_logo),
                        contentDescription = "ClientFlow logo",
                        modifier = Modifier.size(52.dp),
                        contentScale = ContentScale.Fit
                    )
                    Column {
                        Text("ClientFlow", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(formatDateHuman(System.currentTimeMillis()), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onExport) { Icon(Icons.AutoMirrored.Filled.Send, null) }
                IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, null) }
            }
        }
        item {
            StatsRow(
                items = listOf(
                    t("clients", state.language) to state.clients.size.toString(),
                    t("bookings", state.language) to bookingCount.toString(),
                    t("revenue", state.language) to "$${revenue.toInt()}",
                    t("overdue", state.language) to overdue.size.toString()
                )
            )
        }
        item { SectionTitle(t("todays_bookings", state.language)) }
        if (todayBookings.isEmpty()) item { EmptyCard(t("no_bookings_today", state.language)) }
        items(todayBookings) { occurrence ->
            BookingCard(
                booking = occurrence.booking.copy(date = occurrence.occurrenceDate),
                client = state.clients.find { it.id == occurrence.booking.clientId },
                language = state.language,
                onClick = { onOpenClient(occurrence.booking.clientId) }
            )
        }
        if (overdue.isNotEmpty()) {
            item { SectionTitle(t("overdue_payments", state.language)) }
            items(overdue) { payment ->
                val client = state.clients.find { it.id == payment.clientId } ?: return@items
                PaymentCard(client = client, payment = payment, language = state.language, onClick = { onOpenClient(client.id) })
            }
        }
        item { SectionTitle(t("active_pipeline", state.language)) }
        items(state.clients.filter { it.status in listOf(ClientStatus.Lead, ClientStatus.Quoted, ClientStatus.Booked) }.take(5)) { client ->
            ClientCard(client = client, payment = state.payments.find { it.clientId == client.id }, language = state.language, onClick = { onOpenClient(client.id) })
        }
    }
}

@Composable
private fun SettingsScreen(
    state: AppState,
    onBack: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenLicense: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenFreelancerInfo: () -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetTheme: (AppTheme) -> Unit
) {
    val context = LocalContext.current
    val language = state.language
    val accentGold = Color(0xFFF5C518)
    FormScaffold(title = tr("settings", language), onBack = onBack) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ── PREFERENCES ──
            SettingsSectionLabel(tr("preferences", language))
            SettingsAnimatedCard(0) {
                Column(Modifier.padding(16.dp)) {
                    Text(tr("language", language), style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    SettingsLanguagePills(selected = state.language, onSelect = onSetLanguage)
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    Spacer(Modifier.height(14.dp))
                    Text(tr("theme", language), style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    SettingsThemeSwatches(selected = state.theme, language = language, onSelect = onSetTheme)
                }
            }
            // ── ACCOUNT ──
            SettingsSectionLabel(tr("account_section", language))
            SettingsAnimatedCard(1) {
                Column {
                    SettingsGroupRow(emoji = "\uD83D\uDEE1\uFE0F", iconTint = Color(0xFF4CAF50),
                        title = tr("security", language), subtitle = tr("security_desc", language), onClick = onOpenSecurity)
                    HorizontalDivider(modifier = Modifier.padding(start = 66.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingsGroupRow(emoji = "\uD83E\uDDFE", iconTint = Color(0xFFFF9800),
                        title = tr("invoice_profile", language), subtitle = tr("invoice_profile_desc", language), onClick = onOpenFreelancerInfo)
                }
            }
            // ── DATA ──
            SettingsSectionLabel(tr("data_section", language))
            SettingsAnimatedCard(2) {
                SettingsGroupRow(emoji = "\uD83D\uDCBE", iconTint = Color(0xFF2196F3),
                    title = tr("backup_restore", language), subtitle = tr("backup_restore_desc", language), onClick = onOpenBackup)
            }
            // ── SUBSCRIPTION ──
            SettingsSectionLabel(tr("subscription_section", language))
            SettingsAnimatedCard(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color(0xFF1a1a08), Color(0xFF1f1f0a))),
                            RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Star, null, tint = accentGold, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (state.isPro) "Pro Plan" else tr("free_plan", language),
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accentGold)
                            Spacer(Modifier.height(4.dp))
                            if (state.isPro) {
                                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF22C55E).copy(alpha = 0.18f)) {
                                    Text("Active", modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                                        color = Color(0xFF22C55E))
                                }
                            } else {
                                Text(tr("manage_plan", language), style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f))
                            }
                        }
                        if (!state.isPro) {
                            Surface(onClick = onOpenLicense, shape = RoundedCornerShape(14.dp), color = accentGold) {
                                Text(tx("upgrade", language), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold, color = Color.Black, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
            // ── ABOUT ──
            SettingsSectionLabel(tr("about_section", language))
            SettingsAnimatedCard(4) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                        Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center) {
                            Text("AL", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(tx("developer_name", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(tx("developer_role", language), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingsContactRow(emoji = "\uD83D\uDCE7", iconTint = Color(0xFFE53935),
                        label = tx("contact_email", language),
                        onClick = { context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:k.ahmed.lara@gmail.com") }) })
                    HorizontalDivider(modifier = Modifier.padding(start = 54.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingsContactRow(emoji = "\uD83D\uDCAC", iconTint = Color(0xFF25D366),
                        label = tx("contact_whatsapp", language),
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("https://wa.me/212666289222") }) })
                    HorizontalDivider(modifier = Modifier.padding(start = 54.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingsContactRow(emoji = "\uD83C\uDF10", iconTint = Color(0xFF1565C0),
                        label = tx("contact_website", language),
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("https://laaraichi.com") }) })
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FreelancerInfoScreen(
    freelancerInfo: FreelancerInfo,
    language: AppLanguage,
    onBack: () -> Unit,
    onSave: (String, String, String, String, String, String, String, String) -> Unit
) {
    var companyName by rememberSaveable { mutableStateOf(freelancerInfo.companyName) }
    var address by rememberSaveable { mutableStateOf(freelancerInfo.address) }
    var phone by rememberSaveable { mutableStateOf(freelancerInfo.phone) }
    var email by rememberSaveable { mutableStateOf(freelancerInfo.email) }
    var ice by rememberSaveable { mutableStateOf(freelancerInfo.ice) }
    var logoUri by rememberSaveable { mutableStateOf(freelancerInfo.logoUri) }
    var accentColor by rememberSaveable { mutableStateOf(freelancerInfo.accentColor) }
    var invoiceNote by rememberSaveable { mutableStateOf(freelancerInfo.invoiceNote) }

    FormScaffold(title = tx("invoice_profile", language), onBack = onBack) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(companyName, { companyName = it }, label = { Text(tx("company_name", language)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(address, { address = it }, label = { Text(tx("address", language)) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            OutlinedTextField(phone, { phone = it }, label = { Text(tx("phone", language)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(email, { email = it }, label = { Text(tx("email", language)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(ice, { ice = it }, label = { Text(tx("ice", language)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(logoUri, { logoUri = it }, label = { Text(tx("logo_url", language)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
            OutlinedTextField(accentColor, { accentColor = it }, label = { Text(tx("accent_color", language)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(invoiceNote, { invoiceNote = it }, label = { Text(tx("invoice_note", language)) }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Button(onClick = {
                onSave(companyName, address, phone, email, ice, logoUri, accentColor, invoiceNote)
                onBack()
            }, modifier = Modifier.fillMaxWidth()) { Text(tx("save", language)) }
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsAnimatedCard(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(index * 60L); visible = true }
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(320)) + slideInVertically(tween(320)) { it / 3 }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) { content() }
    }
}

@Composable
private fun SettingsGroupRow(emoji: String, iconTint: Color, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center) {
            Text(emoji, style = MaterialTheme.typography.bodyLarge)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsContactRow(emoji: String, iconTint: Color, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(iconTint.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center) {
            Text(emoji, style = MaterialTheme.typography.bodyMedium)
        }
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsLanguagePills(selected: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    val gold = Color(0xFFF5C518)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppLanguage.entries.forEach { lang ->
            val sel = selected == lang
            Surface(onClick = { onSelect(lang) }, shape = RoundedCornerShape(50),
                color = if (sel) gold else Color.Transparent,
                border = BorderStroke(1.dp, if (sel) gold else MaterialTheme.colorScheme.outlineVariant)) {
                Text(languageLabel(lang), modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                    color = if (sel) Color.Black else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun SettingsThemeSwatches(selected: AppTheme, language: AppLanguage, onSelect: (AppTheme) -> Unit) {
    val colors = mapOf(
        AppTheme.Default to Color(0xFF2563EB), AppTheme.Green to Color(0xFF1B5E20),
        AppTheme.Orange to Color(0xFFE65100), AppTheme.Blue to Color(0xFF1A237E),
        AppTheme.Midnight to Color(0xFFFFD700), AppTheme.Teal to Color(0xFF00695C)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AppTheme.entries.forEach { theme ->
            val sel = selected == theme
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(colors[theme] ?: Color.Gray)
                    .then(if (sel) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                    .clickable { onSelect(theme) },
                contentAlignment = Alignment.Center
            ) {
                if (sel) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ClientsScreen(
    state: AppState,
    canAddMore: Boolean,
    onOpenClient: (String) -> Unit,
    onAddClient: () -> Unit,
    onUpgrade: () -> Unit
) {
    val language = state.language
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("All") }
    val filtered = state.clients.filter {
        val matchSearch = search.isBlank() || it.name.contains(search, true) || it.phone.contains(search, true) || it.serviceType.contains(search, true)
        val matchFilter = filter == "All" || it.status.name == filter
        matchSearch && matchFilter
    }.sortedByDescending { it.createdAt }

    Column(Modifier.fillMaxSize()) {
        SearchBar(search = search, onSearch = { search = it }, placeholder = tx("search_clients", language))
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                StatusFilters(selected = filter, language = language, onSelect = { filter = it })
            }
            if (filtered.isEmpty()) item { EmptyCard(tx("no_clients_yet", language)) }
            items(filtered) { client ->
                ClientCard(client = client, payment = state.payments.find { it.clientId == client.id }, language = language, onClick = { onOpenClient(client.id) })
            }
            item {
                if (!canAddMore) {
                    OutlinedButton(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) {
                        Text(tx("upgrade_add_clients", language))
                    }
                } else {
                    OutlinedButton(onClick = onAddClient, modifier = Modifier.fillMaxWidth()) {
                        Text(tx("add_client", language))
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingsScreen(
    state: AppState,
    onOpenClient: (String) -> Unit,
    onEditBooking: (String, String) -> Unit,
    onDeleteBooking: (String) -> Unit
) {
    val language = state.language
    var search by rememberSaveable { mutableStateOf("") }
    var monthOffset by rememberSaveable { mutableStateOf(0) }
    var selectedDate by rememberSaveable { mutableStateOf(todayKey()) }
    var previewBookingId by rememberSaveable { mutableStateOf<String?>(null) }
    var lastMonthOffset by rememberSaveable { mutableStateOf(0) }
    val occurrences = state.bookings.flatMap { expandBookingOccurrences(it) }
    val bookings = occurrences.filter {
        val client = state.clients.find { c -> c.id == it.booking.clientId }
        search.isBlank() || it.occurrenceDate.contains(search, true) || it.booking.location.contains(search, true) || (client?.name?.contains(search, true) == true)
    }.sortedBy { it.occurrenceDate }
    val monthCalendar = remember(monthOffset) {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, monthOffset)
        }
    }
    val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.US).format(monthCalendar.time)
    val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val leadingBlanks = ((monthCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7)
    val dateBookingCount = bookings.groupingBy { it.occurrenceDate }.eachCount()
    val selectedDayBookings = bookings.filter { it.occurrenceDate == selectedDate }
    val monthPrefix = SimpleDateFormat("yyyy-MM", Locale.US).format(monthCalendar.time)
    val previewBooking = state.bookings.find { it.id == previewBookingId }

    LaunchedEffect(monthOffset, dateBookingCount) {
        val monthDates = dateBookingCount.keys.filter { it.startsWith(monthPrefix) }.sorted()
        selectedDate = when {
            selectedDate.startsWith(monthPrefix) -> selectedDate
            monthDates.isNotEmpty() -> monthDates.first()
            else -> "$monthPrefix-01"
        }
        lastMonthOffset = monthOffset
    }

    Column(Modifier.fillMaxSize()) {
        SearchBar(search = search, onSearch = { search = it }, placeholder = tx("search_bookings", language))
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(
                    modifier = Modifier.pointerInput(monthOffset) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount > 24f) monthOffset -= 1
                            if (dragAmount < -24f) monthOffset += 1
                        }
                    }
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { monthOffset -= 1 }) { Icon(Icons.Default.ChevronLeft, null) }
                            Text(
                                text = monthLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() },
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            IconButton(onClick = { monthOffset += 1 }) { Icon(Icons.Default.ChevronRight, null) }
                        }
                        CalendarLegend()
                        CalendarWeekHeader()
                        AnimatedContent(
                            targetState = monthOffset,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    slideInHorizontally(animationSpec = tween(220)) { it } + fadeIn() togetherWith
                                        slideOutHorizontally(animationSpec = tween(220)) { -it } + fadeOut()
                                } else {
                                    slideInHorizontally(animationSpec = tween(220)) { -it } + fadeIn() togetherWith
                                        slideOutHorizontally(animationSpec = tween(220)) { it } + fadeOut()
                                }
                            },
                            label = "month-calendar"
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(7),
                                userScrollEnabled = false,
                                modifier = Modifier.height(280.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(leadingBlanks) {
                                    Box(modifier = Modifier.size(40.dp))
                                }
                                items((1..daysInMonth).toList()) { day ->
                                    val cellDate = monthCalendar.clone().let { cloned ->
                                        (cloned as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                                        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cloned.time)
                                    }
                                    val weekday = monthCalendar.clone().let { cloned ->
                                        (cloned as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                                        cloned.get(Calendar.DAY_OF_WEEK)
                                    }
                                    CalendarDayCell(
                                        day = day,
                                        bookingCount = dateBookingCount[cellDate] ?: 0,
                                        selected = cellDate == selectedDate,
                                        isToday = cellDate == todayKey(),
                                        isWeekend = weekday == Calendar.SATURDAY || weekday == Calendar.SUNDAY,
                                        onClick = { selectedDate = cellDate }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                SectionTitle("${tx("appointments_on", language)} $selectedDate (${selectedDayBookings.size})")
            }
            if (selectedDayBookings.isEmpty()) {
                item { EmptyCard(tx("no_appointments_day", language)) }
            } else {
                items(selectedDayBookings) { booking ->
                    DayBookingCard(
                        booking = booking.booking.copy(date = booking.occurrenceDate),
                        client = state.clients.find { it.id == booking.booking.clientId },
                        language = language,
                        onClick = { previewBookingId = booking.booking.id }
                    )
                }
            }
        }
    }

    if (previewBooking != null) {
        BookingDetailSheet(
            booking = previewBooking,
            client = state.clients.find { it.id == previewBooking.clientId },
            language = language,
            onDismiss = { previewBookingId = null },
            onOpenClient = {
                previewBookingId = null
                onOpenClient(previewBooking.clientId)
            },
            onEditBooking = {
                previewBookingId = null
                onEditBooking(previewBooking.clientId, previewBooking.id)
            },
            onDeleteBooking = {
                onDeleteBooking(previewBooking.id)
                previewBookingId = null
            }
        )
    }
}

@Composable
private fun CalendarLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        LegendItem(color = Color(0xFF16A34A), label = "1 booking")
        LegendItem(color = Color(0xFFD97706), label = "2 bookings")
        LegendItem(color = Color(0xFFDC2626), label = "3+ bookings")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CalendarWeekHeader() {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(day, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    bookingCount: Int,
    selected: Boolean,
    isToday: Boolean,
    isWeekend: Boolean,
    onClick: () -> Unit
) {
    val hasBooking = bookingCount > 0
    Card(
        modifier = Modifier
            .size(40.dp)
            .border(
                width = if (isToday) 1.5.dp else 0.dp,
                color = if (isToday) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                selected -> MaterialTheme.colorScheme.primary
                hasBooking -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                color = when {
                    selected -> MaterialTheme.colorScheme.onPrimary
                    isWeekend -> MaterialTheme.colorScheme.tertiary
                    hasBooking -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (selected || hasBooking || isToday) FontWeight.Bold else FontWeight.Normal
            )
            if (hasBooking) {
                if (bookingCount <= 3) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(bookingCount) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(
                                        color = when {
                                            selected -> MaterialTheme.colorScheme.onPrimary
                                            bookingCount >= 3 -> Color(0xFFDC2626)
                                            bookingCount == 2 -> Color(0xFFD97706)
                                            else -> Color(0xFF16A34A)
                                        },
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            bookingCount.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayBookingCard(booking: Booking, client: Client?, language: AppLanguage, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(client?.name ?: tx("unknown_client", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (!client?.phone.isNullOrBlank()) {
                        Text(client?.phone.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (booking.time.isNotBlank()) {
                        Text(booking.time, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    if (!client?.serviceType.isNullOrBlank()) {
                        Text(client?.serviceType.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (booking.location.isNotBlank()) {
                Text("${tx("location", language)}: ${booking.location}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (booking.notes.isNotBlank()) {
                Text(booking.notes)
            }
            RecurrenceBadge(booking = booking, language = language)
            Text(tx("tap_quick_details", language), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingDetailSheet(
    booking: Booking,
    client: Client?,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onOpenClient: () -> Unit,
    onEditBooking: () -> Unit,
    onDeleteBooking: () -> Unit
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(20.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(client?.name ?: tx("appointment", language), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (!client?.phone.isNullOrBlank()) Text("${tx("phone", language)}: ${client?.phone}")
            if (!client?.serviceType.isNullOrBlank()) Text("${tx("service_type", language)}: ${client?.serviceType}")
            if (booking.date.isNotBlank()) Text("${tx("date", language)}: ${booking.date}")
            if (booking.time.isNotBlank()) Text("${tx("time", language)}: ${booking.time}")
            if (booking.location.isNotBlank()) Text("${tx("location", language)}: ${booking.location}")
            if (booking.notes.isNotBlank()) Text("${tx("notes", language)}: ${booking.notes}")
            RecurrenceBadge(booking = booking, language = language)
            Button(onClick = onOpenClient, modifier = Modifier.fillMaxWidth()) {
                Text(tx("open_client", language))
            }
            OutlinedButton(onClick = onEditBooking, modifier = Modifier.fillMaxWidth()) {
                Text(tx("edit_booking", language))
            }
            OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                Text(tx("delete_booking", language))
            }
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(tx("close", language))
            }
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDeleteBooking()
                }) { Text(tx("delete", language)) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(tx("cancel", language)) } },
            title = { Text(tx("delete_booking_confirm_title", language)) },
            text = { Text(tx("delete_booking_confirm_text", language)) }
        )
    }
}

@Composable
private fun ClientFormScreen(
    state: AppState,
    clientId: String?,
    onBack: () -> Unit,
    onSave: (String?, String, String, String, String, ClientStatus) -> Unit
) {
    val existing = state.clients.find { it.id == clientId }
    val language = state.language
    var name by rememberSaveable { mutableStateOf(existing?.name.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(existing?.phone.orEmpty()) }
    var serviceType by rememberSaveable { mutableStateOf(existing?.serviceType.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(existing?.notes.orEmpty()) }
    var status by rememberSaveable { mutableStateOf(existing?.status ?: ClientStatus.Lead) }
    FormScaffold(title = if (existing == null) tx("add_client_title", language) else tx("edit_client_title", language), onBack = onBack) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text(tx("name", language)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(phone, { phone = it }, label = { Text(tx("phone", language)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(serviceType, { serviceType = it }, label = { Text(tx("service_type", language)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(notes, { notes = it }, label = { Text(tx("notes", language)) }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            StatusDropdown(status = status, onSelect = { status = it })
            Button(
                onClick = {
                    onSave(clientId, name, phone, serviceType, notes, status)
                    onBack()
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (existing == null) tx("add_client", language) else tx("save_changes", language)) }
        }
    }
}

@Composable
private fun InvoiceFormScreen(
    state: AppState,
    clientId: String,
    invoiceId: String?,
    onBack: () -> Unit,
    onSave: (String?, String, Double, String) -> Unit
) {
    val invoice = state.invoices.find { it.id == invoiceId }
    val language = state.language
    var amount by rememberSaveable { mutableStateOf(invoice?.amount?.toString() ?: "") }
    var description by rememberSaveable { mutableStateOf(invoice?.description ?: "") }

    FormScaffold(title = t("invoice", language), onBack = onBack) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(tx("total_amount", language)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(tx("service_type", language)) }, // Using service_type as a label for description
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Button(
                onClick = {
                    onSave(invoiceId, clientId, amount.toDoubleOrNull() ?: 0.0, description)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(tx("save", language))
            }
        }
    }
}

@Composable
private fun BookingFormScreen(
    state: AppState,
    clientId: String,
    bookingId: String?,
    onBack: () -> Unit,
    onSave: (String?, String, String, String, String, String, RecurrenceType, String) -> Unit
) {
    val booking = state.bookings.find { it.id == bookingId }
    val language = state.language
    var date by rememberSaveable { mutableStateOf(booking?.date.orEmpty()) }
    var time by rememberSaveable { mutableStateOf(booking?.time.orEmpty()) }
    var location by rememberSaveable { mutableStateOf(booking?.location.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(booking?.notes.orEmpty()) }
    var recurrence by rememberSaveable { mutableStateOf(booking?.recurrence ?: RecurrenceType.None) }
    var recurrenceUntil by rememberSaveable { mutableStateOf(booking?.recurrenceUntil.orEmpty()) }
    FormScaffold(title = tx("booking", language), onBack = onBack) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DatePickerField(value = date, label = tx("date", language), onDateSelected = { date = it })
            TimePickerField(value = time, label = tx("time", language), onTimeSelected = { time = it })
            OutlinedTextField(location, { location = it }, label = { Text(tx("location", language)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(notes, { notes = it }, label = { Text(tx("notes", language)) }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            RecurrenceDropdown(recurrence = recurrence, language = language, onSelect = { recurrence = it })
            if (recurrence != RecurrenceType.None) {
                DatePickerField(value = recurrenceUntil, label = t("repeat_until", language), onDateSelected = { recurrenceUntil = it })
            }
            Button(onClick = {
                onSave(bookingId, clientId, date, time, location, notes, recurrence, recurrenceUntil)
                onBack()
            }, modifier = Modifier.fillMaxWidth()) { Text(tx("save_booking", language)) }
        }
    }
}

@Composable
private fun PaymentScreen(
    state: AppState,
    clientId: String,
    onBack: () -> Unit,
    onSave: (String, Double, Double, String) -> Unit
) {
    val client = state.clients.find { it.id == clientId }
    val payment = state.payments.find { it.clientId == clientId }
    val language = state.language
    var total by rememberSaveable { mutableStateOf(payment?.totalAmount?.toString().orEmpty()) }
    var paid by rememberSaveable { mutableStateOf(payment?.paidAmount?.toString().orEmpty()) }
    var dueDate by rememberSaveable { mutableStateOf(payment?.dueDate.orEmpty()) }
    FormScaffold(title = tx("payment", language), onBack = onBack) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("${tx("payment_for", language)} ${client?.name.orEmpty()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(total, { total = it }, label = { Text(tx("total_amount", language)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OutlinedTextField(paid, { paid = it }, label = { Text(tx("paid_amount", language)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            DatePickerField(value = dueDate, label = tx("due_date", language), onDateSelected = { dueDate = it })
            SummaryCard(total.toDoubleOrNull() ?: 0.0, paid.toDoubleOrNull() ?: 0.0, dueDate, language)
            Button(onClick = {
                onSave(clientId, total.toDoubleOrNull() ?: 0.0, paid.toDoubleOrNull() ?: 0.0, dueDate)
                onBack()
            }, modifier = Modifier.fillMaxWidth()) { Text(tx("save_payment", language)) }
        }
    }
}

@Composable
private fun ClientDetailScreen(
    state: AppState,
    clientId: String,
    onBack: () -> Unit,
    onEditClient: () -> Unit,
    onOpenPayment: () -> Unit,
    onOpenBooking: (String?) -> Unit,
    onStatusChange: (ClientStatus) -> Unit,
    onDeleteClient: () -> Unit,
    onDeleteBooking: (String) -> Unit,
    onEditInvoice: (String) -> Unit,
    onDeleteInvoice: (String) -> Unit,
    onGenerateInvoice: (String, Double, String) -> Unit,
    onSaveTemplate: (String?, String, String, String) -> Unit,
    onDeleteTemplate: (String) -> Unit
) {
    val context = LocalContext.current
    val client = state.clients.find { it.id == clientId } ?: return
    val language = state.language
    val bookings = state.bookings
        .filter { it.clientId == clientId }
        .flatMap { expandBookingOccurrences(it) }
        .sortedWith(compareBy({ it.occurrenceDate }, { it.booking.time }))
    val payment = state.payments.find { it.clientId == clientId }
    val invoices = state.invoices.filter { it.clientId == clientId }
    var showTemplates by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    FormScaffold(
        title = client.name,
        onBack = onBack,
        actions = {
            IconButton(onClick = onEditClient) { Icon(Icons.Default.Edit, null) }
        }
    ) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(client.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(client.phone, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (client.serviceType.isNotBlank()) Text(client.serviceType, color = MaterialTheme.colorScheme.primary)
                        if (client.notes.isNotBlank()) Text(client.notes)
                        Button(onClick = { showTemplates = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.AutoMirrored.Filled.Message, null)
                            Spacer(Modifier.size(8.dp))
                            Text(tx("send_whatsapp", language))
                        }
                    }
                }
            }
            item { SectionTitle(tx("pipeline", language)) }
            item {
                PipelineRow(
                    current = client.status.name,
                    onSelect = { onStatusChange(ClientStatus.valueOf(it)) },
                    language = language
                )
            }
            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tx("payment", language), modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            IconButton(onClick = onOpenPayment) { Icon(Icons.Default.Edit, null) }
                        }
                        if (payment == null) {
                            OutlinedButton(onClick = onOpenPayment, modifier = Modifier.fillMaxWidth()) { Text(tx("add_payment_details", language)) }
                        } else {
                            Text("${tx("total", language)}: $${payment.totalAmount}")
                            Text("${tx("paid", language)}: $${payment.paidAmount}")
                            Text("${tx("balance", language)}: $${payment.totalAmount - payment.paidAmount}")
                            Text("${tx("due", language)}: ${payment.dueDate.ifBlank { "-" }}")
                            AssistChip(onClick = {}, label = { Text(payment.status.name) })
                        }
                    }
                }
            }
            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tx("bookings", language), modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            IconButton(onClick = { onOpenBooking(null) }) { Icon(Icons.Default.Add, null) }
                        }
                        if (bookings.isEmpty()) {
                            OutlinedButton(onClick = { onOpenBooking(null) }, modifier = Modifier.fillMaxWidth()) { Text(tx("add_booking", language)) }
                        } else {
                            bookings.forEach { occurrence ->
                                BookingRowCompact(
                                    booking = occurrence.booking.copy(date = occurrence.occurrenceDate),
                                    language = language,
                                    onEdit = { onOpenBooking(occurrence.booking.id) },
                                    onDelete = { onDeleteBooking(occurrence.booking.id) }
                                )
                            }
                        }
                    }
                }
            }
            if (invoices.isNotEmpty()) {
                item {
                    Card {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(tx("invoices", language), fontWeight = FontWeight.SemiBold)
                            invoices.forEach { invoice ->
                                InvoiceRow(
                                    invoice = invoice,
                                    onEdit = { onEditInvoice(invoice.id) },
                                    onDelete = { onDeleteInvoice(invoice.id) },
                                    onExportPdf = {
                                        val generator = com.ahmed.clientflow.pdf.InvoicePdfGenerator(context)
                                        val uri = generator.generatePdf(invoice, client, payment, state.freelancerInfo ?: com.ahmed.clientflow.data.FreelancerInfo())
                                        if (uri != null) {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/pdf"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Invoice PDF"))
                                        } else {
                                            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        if (payment != null) {
                            onGenerateInvoice(client.id, payment.totalAmount, client.serviceType.ifBlank { tx("service", language) })
                            Toast.makeText(context, tx("invoice_created", language), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, tx("add_payment_first", language), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(tx("generate_invoice", language)) }
            }
            item {
                OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.size(8.dp))
                    Text(tx("delete_client", language))
                }
            }
        }
    }

    if (showTemplates) {
        MessageTemplateSheet(
            templates = state.templates,
            client = client,
            nextBooking = bookings.firstOrNull()?.let { it.booking.copy(date = it.occurrenceDate) },
            payment = payment,
            onDismiss = { showTemplates = false },
            onSaveTemplate = onSaveTemplate,
            onDeleteTemplate = onDeleteTemplate
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            confirmButton = { TextButton(onClick = onDeleteClient) { Text(tx("delete", language)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(tx("cancel", language)) } },
            title = { Text(tx("delete_client_confirm_title", language)) },
            text = { Text(client.name) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecurityScreen(
    language: AppLanguage,
    biometricEnabled: Boolean,
    onBack: () -> Unit,
    onLock: () -> Unit,
    onClearPin: () -> Unit,
    onEnableBiometric: () -> Unit,
    onDisableBiometric: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity
    val bioAvailable = try {
        activity?.let { com.ahmed.clientflow.security.BiometricAuthManager.canAuthenticate(it) } ?: false
    } catch (e: Exception) {
        false
    }
    var showBioConfirm by remember { mutableStateOf(false) }

    FormScaffold(title = tx("security", language), onBack = onBack) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(tx("app_security", language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(tx("security_desc", language), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onLock, modifier = Modifier.fillMaxWidth()) { Text(tx("lock_now", language)) }
                    OutlinedButton(onClick = onClearPin, modifier = Modifier.fillMaxWidth()) { Text(tx("reset_pin", language)) }
                }
            }
            if (bioAvailable) {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Fingerprint, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Fingerprint Unlock", fontWeight = FontWeight.Medium)
                                Text(
                                    "Use fingerprint to unlock app",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        if (activity != null) {
                                            showBioConfirm = true
                                        } else {
                                            android.widget.Toast.makeText(context, "Biometric not available", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        onDisableBiometric()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBioConfirm && activity != null) {
        var authCompleted by remember { mutableStateOf(false) }
        if (!authCompleted) {
            val manager = remember(activity) { com.ahmed.clientflow.security.BiometricAuthManager(activity) }
            LaunchedEffect(showBioConfirm) {
                try {
                    manager.authenticate(
                        title = "Fingerprint Unlock",
                        subtitle = "Confirm to enable fingerprint unlock",
                        onSuccess = {
                            onEnableBiometric()
                            authCompleted = true
                        },
                        onError = {
                            authCompleted = true
                        },
                        onFailed = {
                            authCompleted = true
                        }
                    )
                } catch (e: Exception) {
                    authCompleted = true
                }
            }
        }
    }
}

@Composable
private fun LicenseScreen(state: AppState, deviceId: String, onBack: () -> Unit, onActivate: (String, (Boolean) -> Unit) -> Unit) {
    val context = LocalContext.current
    val language = state.language
    var code by rememberSaveable { mutableStateOf("") }
    FormScaffold(title = tx("upgrade", language), onBack = onBack) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (state.isPro) tr("pro_active", language) else tr("free_plan", language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${tx("free_limit", language)}: ${state.freeClientLimit}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    Text(
                        "${tx("device_id", language)}: $deviceId",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(deviceId))
                            Toast.makeText(context, tx("copied", language), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        tr("contact_for_code", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    ContactRow(
                        emoji = "\uD83D\uDCE7",
                        text = tx("contact_email", language),
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:k.ahmed.lara@gmail.com")
                            }
                            context.startActivity(intent)
                        },
                        language = language
                    )
                    ContactRow(
                        emoji = "\uD83D\uDCAC",
                        text = tx("contact_whatsapp", language),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://wa.me/212666289222")
                            }
                            context.startActivity(intent)
                        },
                        language = language
                    )
                    ContactRow(
                        emoji = "\uD83C\uDF10",
                        text = tx("contact_website", language),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://laaraichi.com")
                            }
                            context.startActivity(intent)
                        },
                        language = language
                    )
                }
            }
            OutlinedTextField(code, { code = it }, label = { Text(tx("activation_code", language)) }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                onActivate(code) { ok ->
                    Toast.makeText(context, if (ok) tx("pro_activated", language) else tx("invalid_code", language), Toast.LENGTH_SHORT).show()
                    if (ok) onBack()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text(tx("activate", language)) }
        }
    }
}

@Composable
private fun ContactRow(emoji: String, text: String, onClick: () -> Unit, language: AppLanguage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormScaffold(title: String, onBack: () -> Unit, actions: @Composable () -> Unit = {}, content: @Composable () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(title) }, navigationIcon = {
            IconButton(onClick = onBack) { Text("<") }
        }, actions = { actions() })
    }) { padding ->
        Box(Modifier.padding(padding)) { content() }
    }
}

@Composable
private fun SearchBar(search: String, onSearch: (String) -> Unit, placeholder: String = "Search clients") {
    OutlinedTextField(
        value = search,
        onValueChange = onSearch,
        label = { Text(placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
private fun DatePickerField(
    value: String,
    label: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember(value) {
        Calendar.getInstance().apply {
            if (value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                val parts = value.split("-")
                set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onDateSelected(
                                String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                            )
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Select date")
            }
        }
    )
}

@Composable
private fun TimePickerField(
    value: String,
    label: String,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val parts = value.split(":")
    val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 9
    val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            onTimeSelected(String.format(Locale.US, "%02d:%02d", hourOfDay, minute))
                        },
                        initialHour,
                        initialMinute,
                        true
                    ).show()
                }
            ) {
                Icon(Icons.Default.Schedule, contentDescription = "Select time")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurrenceDropdown(recurrence: RecurrenceType, language: AppLanguage, onSelect: (RecurrenceType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = recurrenceLabel(recurrence, language),
            onValueChange = {},
            readOnly = true,
            label = { Text(t("recurrence", language)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            RecurrenceType.entries.forEach {
                DropdownMenuItem(
                    text = { Text(recurrenceLabel(it, language)) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusFilters(selected: String, language: AppLanguage, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = { onSelect("All") },
            label = { Text(t("status_all", language)) },
            border = if (selected == "All") BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
        )
        ClientStatus.entries.forEach { status ->
            AssistChip(
                onClick = { onSelect(status.name) },
                label = { Text(statusLabel(status, language)) },
                border = if (selected == status.name) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
            )
        }
    }
}



private fun themeLabel(theme: AppTheme, language: AppLanguage): String = when (theme) {
    AppTheme.Default -> when (language) {
        AppLanguage.English -> "Default"
        AppLanguage.French -> "Par défaut"
        AppLanguage.Arabic -> "الافتراضي"
    }
    AppTheme.Green -> when (language) {
        AppLanguage.English -> "Nature Green"
        AppLanguage.French -> "Vert Nature"
        AppLanguage.Arabic -> "الأخضر"
    }
    AppTheme.Orange -> when (language) {
        AppLanguage.English -> "Sunset Orange"
        AppLanguage.French -> "Orange Sunset"
        AppLanguage.Arabic -> "البرتقالي"
    }
    AppTheme.Blue -> when (language) {
        AppLanguage.English -> "Classic Blue"
        AppLanguage.French -> "Bleu Classique"
        AppLanguage.Arabic -> "الأزرق"
    }
    AppTheme.Midnight -> when (language) {
        AppLanguage.English -> "Midnight Gold"
        AppLanguage.French -> "Minuit Or"
        AppLanguage.Arabic -> "ذهبي"
    }
    AppTheme.Teal -> when (language) {
        AppLanguage.English -> "Ocean Teal"
        AppLanguage.French -> "Teal Océan"
        AppLanguage.Arabic -> "تيال"
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun EmptyCard(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}

@Composable
private fun StatsRow(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { chunk ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chunk.forEach { (label, value) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

private fun clientStatusColor(status: ClientStatus): Color = when (status) {
    ClientStatus.Lead      -> Color(0xFF8B5CF6)
    ClientStatus.Quoted    -> Color(0xFFF59E0B)
    ClientStatus.Booked    -> Color(0xFF3B82F6)
    ClientStatus.Completed -> Color(0xFF22C55E)
    else                   -> Color(0xFF6B7280)
}

private fun avatarColorFromName(name: String): Color {
    val palette = listOf(
        Color(0xFF7C3AED), Color(0xFF2563EB), Color(0xFF059669),
        Color(0xFFD97706), Color(0xFFDC2626), Color(0xFF0891B2),
        Color(0xFF65A30D), Color(0xFFDB2777)
    )
    val idx = Math.abs(name.hashCode()) % palette.size
    return palette[idx]
}

private fun nameInitials(name: String): String {
    val parts = name.trim().split(" ")
    return if (parts.size >= 2) "${parts[0].firstOrNull() ?: ""}${parts[1].firstOrNull() ?: ""}".uppercase()
    else name.take(2).uppercase()
}

@Composable
private fun ClientCard(client: Client, payment: Payment?, language: AppLanguage, onClick: () -> Unit) {
    val statusColor = clientStatusColor(client.status)
    val avatarColor = avatarColorFromName(client.name)
    val payColor = when (payment?.status) {
        PaymentStatus.Paid    -> Color(0xFF22C55E)
        PaymentStatus.Partial -> Color(0xFFF59E0B)
        PaymentStatus.Unpaid  -> Color(0xFFEF4444)
        else                  -> Color.Transparent
    }
    val cardColor = MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        // Left accent line
        Box(
            modifier = Modifier
                .width(3.dp)
                .matchParentSize()
                .background(statusColor.copy(alpha = 0.7f))
                .align(Alignment.CenterStart)
        )
        Row(
            modifier = Modifier.padding(start = 15.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(avatarColor.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(nameInitials(client.name), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
            }
            // Info column
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(client.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary, fontSize = androidx.compose.ui.unit.TextUnit(15f, androidx.compose.ui.unit.TextUnitType.Sp))
                    if (client.serviceType.isNotBlank()) {
                        Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Text(client.serviceType, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = androidx.compose.ui.unit.TextUnit(13f, androidx.compose.ui.unit.TextUnitType.Sp),
                            maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false))
                    }
                }
                Text(
                    "\uD83D\uDCDE ${client.phone}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp),
                    maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (payment != null) {
                    Surface(shape = RoundedCornerShape(50), color = payColor.copy(alpha = 0.15f)) {
                        Text(
                            paymentStatusLabel(payment.status, language),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = payColor,
                            fontSize = androidx.compose.ui.unit.TextUnit(11f, androidx.compose.ui.unit.TextUnitType.Sp)
                        )
                    }
                }
            }
            // Status badge top-right
            Surface(shape = RoundedCornerShape(50), color = statusColor.copy(alpha = 0.15f)) {
                Text(
                    statusLabel(client.status, language),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor,
                    fontSize = androidx.compose.ui.unit.TextUnit(11f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
            }
        }
    }
}

@Composable
private fun BookingCard(booking: Booking, client: Client?, language: AppLanguage, onClick: () -> Unit) {
    val clientStatus = client?.status ?: ClientStatus.Lead
    val statusColor = clientStatusColor(clientStatus)
    val avatarColor = avatarColorFromName(client?.name ?: "?")
    val displayName = client?.name ?: tx("unknown_client", language)
    val cardColor = MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .matchParentSize()
                .background(statusColor.copy(alpha = 0.7f))
                .align(Alignment.CenterStart)
        )
        Row(
            modifier = Modifier.padding(start = 15.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(avatarColor.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(nameInitials(displayName), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary, fontSize = androidx.compose.ui.unit.TextUnit(15f, androidx.compose.ui.unit.TextUnitType.Sp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (booking.location.isNotBlank()) {
                        Text("\uD83D\uDCCD ${booking.location}", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp),
                            maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
                if (booking.notes.isNotBlank()) {
                    Text(booking.notes, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp))
                }
            }
            // Time badge
            if (booking.time.isNotBlank()) {
                Text(booking.time, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp))
            }
        }
    }
}

@Composable
private fun PaymentCard(client: Client, payment: Payment, language: AppLanguage, onClick: () -> Unit) {
    val cardColor = MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.width(3.dp).matchParentSize().background(MaterialTheme.colorScheme.error.copy(alpha = 0.7f)).align(Alignment.CenterStart))
        Row(
            modifier = Modifier.padding(start = 15.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(avatarColorFromName(client.name).copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center) {
                Text(nameInitials(client.name), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(client.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                    fontSize = androidx.compose.ui.unit.TextUnit(15f, androidx.compose.ui.unit.TextUnitType.Sp))
                Text("${tx("balance", language)}: $${"%.2f".format(payment.totalAmount - payment.paidAmount)}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Text("${tx("due", language)} ${payment.dueDate}", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun SummaryCard(total: Double, paid: Double, dueDate: String, language: AppLanguage) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(tx("summary", language), fontWeight = FontWeight.Bold)
            Text("${tx("total", language)}: $${"%.2f".format(total)}")
            Text("${tx("paid", language)}: $${"%.2f".format(paid)}")
            Text("${tx("balance", language)}: $${"%.2f".format(total - paid)}")
            if (dueDate.isNotBlank()) Text("${tx("due", language)}: $dueDate")
            AssistChip(onClick = {}, label = { Text(paymentStatusText(total, paid)) })
        }
    }
}

@Composable
private fun BookingRowCompact(booking: Booking, language: AppLanguage, onEdit: () -> Unit, onDelete: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${booking.date} ${booking.time}".trim(), modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null) }
        }
        RecurrenceBadge(booking = booking, language = language)
        if (booking.location.isNotBlank()) Text(booking.location)
        if (booking.notes.isNotBlank()) Text(booking.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider()
    }
}

@Composable
private fun InvoiceRow(
    invoice: Invoice,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExportPdf: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.weight(1f)) {
            Text(invoice.description, fontWeight = FontWeight.Medium)
            Text(formatDateHuman(invoice.createdAt), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("$${invoice.amount}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp)) }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, modifier = Modifier.size(20.dp)) }
        IconButton(onClick = onExportPdf) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Export PDF", modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(status: ClientStatus, onSelect: (ClientStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = status.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ClientStatus.entries.forEach {
                DropdownMenuItem(text = { Text(it.name) }, onClick = {
                    onSelect(it)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun PinField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        visualTransformation = PasswordVisualTransformation(),
        label = { Text(tx("pin", AppLanguage.English)) },
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageTemplateSheet(
    templates: List<MessageTemplate>,
    client: Client,
    nextBooking: Booking?,
    payment: Payment?,
    onDismiss: () -> Unit,
    onSaveTemplate: (String?, String, String, String) -> Unit,
    onDeleteTemplate: (String) -> Unit
) {
    val context = LocalContext.current
    var editTarget by remember { mutableStateOf<MessageTemplate?>(null) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var emoji by rememberSaveable { mutableStateOf("\uD83D\uDCAC") }
    val amount = payment?.let { "$${"%.2f".format(it.totalAmount - it.paidAmount)}" }.orEmpty()
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(tx("whatsapp_templates", AppLanguage.English), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            templates.forEach { template ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${template.emoji} ${template.name}", fontWeight = FontWeight.Bold)
                        Text(formatMessage(template.content, client, amount, nextBooking?.date.orEmpty()), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                sendWhatsApp(
                                    context,
                                    client.phone,
                                    formatMessage(template.content, client, amount, nextBooking?.date.orEmpty())
                                )
                            }, modifier = Modifier.weight(1f)) { Text(tx("send", AppLanguage.English)) }
                            OutlinedButton(onClick = {
                                editTarget = template
                                editingId = template.id
                                name = template.name
                                content = template.content
                                emoji = template.emoji
                            }, modifier = Modifier.weight(1f)) { Text(tx("edit", AppLanguage.English)) }
                            if (!template.isDefault) {
                                IconButton(onClick = { onDeleteTemplate(template.id) }) { Icon(Icons.Default.Delete, null) }
                            }
                        }
                    }
                }
            }
            OutlinedButton(onClick = {
                editTarget = MessageTemplate(name = "", content = "", emoji = "\uD83D\uDCAC")
                editingId = null
                name = ""
                content = ""
                emoji = "\uD83D\uDCAC"
            }, modifier = Modifier.fillMaxWidth()) { Text(tx("new_template", AppLanguage.English)) }
        }
    }

    if (editTarget != null) {
        AlertDialog(
            onDismissRequest = { editTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    onSaveTemplate(editingId, name, content, emoji)
                    editTarget = null
                    editingId = null
                }) { Text(tx("save", AppLanguage.English)) }
            },
            dismissButton = { TextButton(onClick = { editTarget = null; editingId = null }) { Text(tx("cancel", AppLanguage.English)) } },
            title = { Text(tx("template", AppLanguage.English)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text(tx("name", AppLanguage.English)) })
                    OutlinedTextField(emoji, { emoji = it }, label = { Text(tx("emoji", AppLanguage.English)) })
                    OutlinedTextField(content, { content = it }, label = { Text(tx("message", AppLanguage.English)) }, minLines = 4)
                }
            }
        )
    }
}

private fun sendWhatsApp(context: android.content.Context, phone: String, message: String) {
    val cleaned = phone.filter(Char::isDigit)
    val uri = Uri.parse("https://wa.me/$cleaned?text=${Uri.encode(message)}")
    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
}

private fun formatMessage(template: String, client: Client, amount: String, date: String): String {
    return template
        .replace("{name}", client.name)
        .replace("{amount}", amount)
        .replace("{date}", date)
        .replace("{service}", client.serviceType)
}

private fun paymentStatusText(total: Double, paid: Double): String = when {
    paid <= 0 -> "Unpaid"
    paid >= total -> "Paid"
    else -> "Partial"
}

private fun statusLabel(status: ClientStatus, language: AppLanguage): String = when (status) {
    ClientStatus.Lead -> t("status_lead", language)
    ClientStatus.Quoted -> t("status_quoted", language)
    ClientStatus.Booked -> t("status_booked", language)
    ClientStatus.Completed -> t("status_completed", language)
    ClientStatus.Paid -> t("status_paid", language)
}

private fun paymentStatusLabel(status: PaymentStatus, language: AppLanguage): String = when (status) {
    PaymentStatus.Unpaid -> t("unpaid", language)
    PaymentStatus.Partial -> t("partial", language)
    PaymentStatus.Paid -> t("paid_status", language)
}

private fun recurrenceLabel(recurrence: RecurrenceType, language: AppLanguage): String = when (recurrence) {
    RecurrenceType.None -> t("recurrence_none", language)
    RecurrenceType.Daily -> t("recurrence_daily", language)
    RecurrenceType.Weekly -> t("recurrence_weekly", language)
    RecurrenceType.Monthly -> t("recurrence_monthly", language)
}

private fun recurrenceSummary(booking: Booking, language: AppLanguage): String? {
    if (booking.recurrence == RecurrenceType.None) return null
    val base = when (booking.recurrence) {
        RecurrenceType.None -> return null
        RecurrenceType.Daily -> t("repeats_daily", language)
        RecurrenceType.Weekly -> t("repeats_weekly", language)
        RecurrenceType.Monthly -> t("repeats_monthly", language)
    }
    return if (booking.recurrenceUntil.isNotBlank()) {
        "$base ${t("until", language)} ${booking.recurrenceUntil}"
    } else {
        base
    }
}

@Composable
private fun RecurrenceBadge(booking: Booking, language: AppLanguage) {
    val summary = recurrenceSummary(booking, language) ?: return
    AssistChip(
        onClick = {},
        label = { Text(summary) }
    )
}

private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

private fun formatDateHuman(millis: Long): String = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(millis))

private fun expandBookingOccurrences(
    booking: Booking,
    horizonMonths: Int = 12,
    maxOccurrences: Int = 120
): List<BookingOccurrence> {
    if (booking.date.isBlank()) return emptyList()
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val startDate = runCatching { fmt.parse(booking.date) }.getOrNull() ?: return emptyList()
    val startCal = Calendar.getInstance().apply { time = startDate }
    val endCal = Calendar.getInstance().apply {
        time = startDate
        add(Calendar.MONTH, horizonMonths)
    }
    val untilCal = booking.recurrenceUntil.takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }?.let {
        Calendar.getInstance().apply { time = fmt.parse(it)!! }
    }

    val occurrences = mutableListOf<BookingOccurrence>()
    val cursor = Calendar.getInstance().apply { time = startDate }

    while (occurrences.size < maxOccurrences && !cursor.after(endCal)) {
        val currentDate = fmt.format(cursor.time)
        occurrences += BookingOccurrence(booking, currentDate)
        if (booking.recurrence == RecurrenceType.None) break
        when (booking.recurrence) {
            RecurrenceType.None -> break
            RecurrenceType.Daily -> cursor.add(Calendar.DAY_OF_MONTH, 1)
            RecurrenceType.Weekly -> cursor.add(Calendar.DAY_OF_MONTH, 7)
            RecurrenceType.Monthly -> cursor.add(Calendar.MONTH, 1)
        }
        if (untilCal != null && cursor.after(untilCal)) break
    }

    return occurrences
}

private fun languageLabel(language: AppLanguage): String = when (language) {
    AppLanguage.English -> "English"
    AppLanguage.French -> "Francais"
    AppLanguage.Arabic -> "العربية"
}

private fun tr(key: String, language: AppLanguage): String = when (key) {
    "settings" -> when (language) {
        AppLanguage.English -> "Settings"
        AppLanguage.French -> "Parametres"
        AppLanguage.Arabic -> "الإعدادات"
    }
    "language" -> when (language) {
        AppLanguage.English -> "Language"
        AppLanguage.French -> "Langue"
        AppLanguage.Arabic -> "اللغة"
    }
    "language_desc" -> when (language) {
        AppLanguage.English -> "Choose app language."
        AppLanguage.French -> "Choisir langue application."
        AppLanguage.Arabic -> "اختر لغة التطبيق."
    }
    "security" -> when (language) {
        AppLanguage.English -> "Security"
        AppLanguage.French -> "Securite"
        AppLanguage.Arabic -> "الأمان"
    }
    "open_security" -> when (language) {
        AppLanguage.English -> "Open security"
        AppLanguage.French -> "Ouvrir securite"
        AppLanguage.Arabic -> "فتح الأمان"
    }
    "plan" -> when (language) {
        AppLanguage.English -> "Plan"
        AppLanguage.French -> "Forfait"
        AppLanguage.Arabic -> "الخطة"
    }
    "pro_active" -> when (language) {
        AppLanguage.English -> "Pro active"
        AppLanguage.French -> "Pro actif"
        AppLanguage.Arabic -> "برو مفعل"
    }
    "free_plan" -> when (language) {
        AppLanguage.English -> "Free plan"
        AppLanguage.French -> "Plan gratuit"
        AppLanguage.Arabic -> "خطة مجانية"
    }
    "manage_plan" -> when (language) {
        AppLanguage.English -> "Manage plan"
        AppLanguage.French -> "Gerer forfait"
        AppLanguage.Arabic -> "إدارة الخطة"
    }
    "contact_for_code" -> when (language) {
        AppLanguage.English -> "Contact for activation code"
        AppLanguage.French -> "Contacter pour code activation"
        AppLanguage.Arabic -> "تواصل للحصول على الرمز"
    }
    "theme" -> when (language) {
        AppLanguage.English -> "App Theme"
        AppLanguage.French -> "Theme"
        AppLanguage.Arabic -> "سمة التطبيق"
    }
    "theme_desc" -> when (language) {
        AppLanguage.English -> "Choose app color theme."
        AppLanguage.French -> "Choisir le theme de couleur."
        AppLanguage.Arabic -> "اختر سمة ألوان التطبيق."
    }
    else -> key
}

private fun t(key: String, language: AppLanguage): String = when (key) {
    "clients" -> when (language) {
        AppLanguage.English -> "Clients"
        AppLanguage.French -> "Clients"
        AppLanguage.Arabic -> "العملاء"
    }
    "revenue" -> when (language) {
        AppLanguage.English -> "Revenue"
        AppLanguage.French -> "Revenus"
        AppLanguage.Arabic -> "الايرادات"
    }
    "analytics" -> when (language) {
        AppLanguage.English -> "Analytics"
        AppLanguage.French -> "Analytique"
        AppLanguage.Arabic -> "التحليلات"
    }
    "avg_revenue_per_client" -> when (language) {
        AppLanguage.English -> "Avg Revenue/Client"
        AppLanguage.French -> "Revenu moy/client"
        AppLanguage.Arabic -> "متوسط الإيرادات/عميل"
    }
    "conversion_rate" -> when (language) {
        AppLanguage.English -> "Conversion"
        AppLanguage.French -> "Conversion"
        AppLanguage.Arabic -> "معدل التحويل"
    }
    "retention" -> when (language) {
        AppLanguage.English -> "Retention"
        AppLanguage.French -> "Retention"
        AppLanguage.Arabic -> "الاحتفاظ"
    }
    "pipeline_value" -> when (language) {
        AppLanguage.English -> "Pipeline Value"
        AppLanguage.French -> "Valeur pipeline"
        AppLanguage.Arabic -> "قيمة المسار"
    }
    "all_time" -> when (language) {
        AppLanguage.English -> "All Time"
        AppLanguage.French -> "Tout"
        AppLanguage.Arabic -> "كل الأوقات"
    }
    "this_month" -> when (language) {
        AppLanguage.English -> "This Month"
        AppLanguage.French -> "Ce mois"
        AppLanguage.Arabic -> "هذا الشهر"
    }
    "this_year" -> when (language) {
        AppLanguage.English -> "This Year"
        AppLanguage.French -> "Cette annee"
        AppLanguage.Arabic -> "هذه السنة"
    }
    "status_distribution" -> when (language) {
        AppLanguage.English -> "Status Distribution"
        AppLanguage.French -> "Repartition statut"
        AppLanguage.Arabic -> "توزيع الحالة"
    }
    "no_analytics_data" -> when (language) {
        AppLanguage.English -> "No clients yet - Add clients to see analytics"
        AppLanguage.French -> "Pas encore de clients - Ajoutez des clients pour voir les analyses"
        AppLanguage.Arabic -> "لا يوجد عملاء بعد - أضف عملاء لرؤية التحليلات"
    }
    "returned" -> when (language) {
        AppLanguage.English -> "returned"
        AppLanguage.French -> "revenu"
        AppLanguage.Arabic -> "عاد"
    }
    "per_client" -> when (language) {
        AppLanguage.English -> "per client"
        AppLanguage.French -> "par client"
        AppLanguage.Arabic -> "لكل عميل"
    }
    "lead_to_booked" -> when (language) {
        AppLanguage.English -> "Lead to Booked"
        AppLanguage.French -> "Prospect -> Reserve"
        AppLanguage.Arabic -> "محتمل -> محجوز"
    }
    "clients_returned" -> when (language) {
        AppLanguage.English -> "clients returned"
        AppLanguage.French -> "clients revenus"
        AppLanguage.Arabic -> "عملاء عادوا"
    }
    "overdue" -> when (language) {
        AppLanguage.English -> "Overdue"
        AppLanguage.French -> "En retard"
        AppLanguage.Arabic -> "متأخر"
    }
    "todays_bookings" -> when (language) {
        AppLanguage.English -> "Today's bookings"
        AppLanguage.French -> "Reservations du jour"
        AppLanguage.Arabic -> "مواعيد اليوم"
    }
    "no_bookings_today" -> when (language) {
        AppLanguage.English -> "No bookings today"
        AppLanguage.French -> "Aucune reservation aujourd'hui"
        AppLanguage.Arabic -> "لا مواعيد اليوم"
    }
    "overdue_payments" -> when (language) {
        AppLanguage.English -> "Overdue payments"
        AppLanguage.French -> "Paiements en retard"
        AppLanguage.Arabic -> "الدفعات المتأخرة"
    }
    "active_pipeline" -> when (language) {
        AppLanguage.English -> "Active pipeline"
        AppLanguage.French -> "Pipeline actif"
        AppLanguage.Arabic -> "المسار النشط"
    }
    "recurrence" -> when (language) {
        AppLanguage.English -> "Recurrence"
        AppLanguage.French -> "Recurrence"
        AppLanguage.Arabic -> "التكرار"
    }
    "repeat_until" -> when (language) {
        AppLanguage.English -> "Repeat until"
        AppLanguage.French -> "Repeter jusqu'au"
        AppLanguage.Arabic -> "كرر حتى"
    }
    "recurrence_none" -> when (language) {
        AppLanguage.English -> "None"
        AppLanguage.French -> "Aucune"
        AppLanguage.Arabic -> "بدون"
    }
    "recurrence_daily" -> when (language) {
        AppLanguage.English -> "Daily"
        AppLanguage.French -> "Quotidienne"
        AppLanguage.Arabic -> "يومي"
    }
    "recurrence_weekly" -> when (language) {
        AppLanguage.English -> "Weekly"
        AppLanguage.French -> "Hebdomadaire"
        AppLanguage.Arabic -> "اسبوعي"
    }
    "recurrence_monthly" -> when (language) {
        AppLanguage.English -> "Monthly"
        AppLanguage.French -> "Mensuelle"
        AppLanguage.Arabic -> "شهري"
    }
    "repeats_daily" -> when (language) {
        AppLanguage.English -> "Repeats daily"
        AppLanguage.French -> "Repete chaque jour"
        AppLanguage.Arabic -> "يتكرر يوميا"
    }
    "repeats_weekly" -> when (language) {
        AppLanguage.English -> "Repeats weekly"
        AppLanguage.French -> "Repete chaque semaine"
        AppLanguage.Arabic -> "يتكرر اسبوعيا"
    }
    "repeats_monthly" -> when (language) {
        AppLanguage.English -> "Repeats monthly"
        AppLanguage.French -> "Repete chaque mois"
        AppLanguage.Arabic -> "يتكرر شهريا"
    }
    "until" -> when (language) {
        AppLanguage.English -> "until"
        AppLanguage.French -> "jusqu'au"
        AppLanguage.Arabic -> "حتى"
    }
    "unpaid" -> when (language) {
        AppLanguage.English -> "Unpaid"
        AppLanguage.French -> "Impayee"
        AppLanguage.Arabic -> "غير مدفوع"
    }
    "partial" -> when (language) {
        AppLanguage.English -> "Partial"
        AppLanguage.French -> "Partiel"
        AppLanguage.Arabic -> "جزئي"
    }
    "paid_status" -> when (language) {
        AppLanguage.English -> "Paid"
        AppLanguage.French -> "Paye"
        AppLanguage.Arabic -> "مدفوع"
    }
    else -> tx(key, language)
}

private fun formatAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) "%.0f".format(amount) else "%.2f".format(amount)
}

private fun tx(key: String, language: AppLanguage): String = when (key) {
    "export_data" -> when (language) {
        AppLanguage.English -> "Export ClientFlow data"
        AppLanguage.French -> "Exporter donnees ClientFlow"
        AppLanguage.Arabic -> "تصدير بيانات ClientFlow"
    }
    "secure_clientflow" -> when (language) {
        AppLanguage.English -> "Secure ClientFlow"
        AppLanguage.French -> "Securiser ClientFlow"
        AppLanguage.Arabic -> "تأمين ClientFlow"
    }
    "set_pin" -> when (language) {
        AppLanguage.English -> "Set 4-digit PIN"
        AppLanguage.French -> "Definir code PIN 4 chiffres"
        AppLanguage.Arabic -> "ضع رمز PIN من 4 أرقام"
    }
    "save_pin" -> when (language) {
        AppLanguage.English -> "Save PIN"
        AppLanguage.French -> "Enregistrer PIN"
        AppLanguage.Arabic -> "حفظ PIN"
    }
    "app_locked" -> when (language) {
        AppLanguage.English -> "ClientFlow Locked"
        AppLanguage.French -> "ClientFlow verrouille"
        AppLanguage.Arabic -> "ClientFlow مقفل"
    }
    "enter_pin_continue" -> when (language) {
        AppLanguage.English -> "Enter PIN to continue"
        AppLanguage.French -> "Entrer PIN pour continuer"
        AppLanguage.Arabic -> "أدخل PIN للمتابعة"
    }
    "wrong_pin" -> when (language) {
        AppLanguage.English -> "Wrong PIN"
        AppLanguage.French -> "PIN incorrect"
        AppLanguage.Arabic -> "PIN غير صحيح"
    }
    "unlock" -> when (language) {
        AppLanguage.English -> "Unlock"
        AppLanguage.French -> "Deverrouiller"
        AppLanguage.Arabic -> "فتح"
    }
    "search_clients" -> when (language) {
        AppLanguage.English -> "Search clients"
        AppLanguage.French -> "Rechercher clients"
        AppLanguage.Arabic -> "البحث عن العملاء"
    }
    "no_clients_yet" -> when (language) {
        AppLanguage.English -> "No clients yet"
        AppLanguage.French -> "Aucun client pour le moment"
        AppLanguage.Arabic -> "لا يوجد عملاء بعد"
    }
    "upgrade_add_clients" -> when (language) {
        AppLanguage.English -> "Upgrade to add more clients"
        AppLanguage.French -> "Mettre a niveau pour ajouter plus de clients"
        AppLanguage.Arabic -> "قم بالترقية لإضافة المزيد من العملاء"
    }
    "add_client" -> when (language) {
        AppLanguage.English -> "Add client"
        AppLanguage.French -> "Ajouter client"
        AppLanguage.Arabic -> "إضافة عميل"
    }
    "add_client_title" -> when (language) {
        AppLanguage.English -> "Add Client"
        AppLanguage.French -> "Ajouter Client"
        AppLanguage.Arabic -> "إضافة عميل"
    }
    "edit_client_title" -> when (language) {
        AppLanguage.English -> "Edit Client"
        AppLanguage.French -> "Modifier Client"
        AppLanguage.Arabic -> "تعديل عميل"
    }
    "name" -> when (language) {
        AppLanguage.English -> "Name"
        AppLanguage.French -> "Nom"
        AppLanguage.Arabic -> "الاسم"
    }
    "save_changes" -> when (language) {
        AppLanguage.English -> "Save changes"
        AppLanguage.French -> "Enregistrer modifications"
        AppLanguage.Arabic -> "حفظ التعديلات"
    }
    "search_bookings" -> when (language) {
        AppLanguage.English -> "Search bookings"
        AppLanguage.French -> "Rechercher reservations"
        AppLanguage.Arabic -> "البحث في المواعيد"
    }
    "appointments_on" -> when (language) {
        AppLanguage.English -> "Appointments on"
        AppLanguage.French -> "Rendez-vous du"
        AppLanguage.Arabic -> "مواعيد يوم"
    }
    "no_appointments_day" -> when (language) {
        AppLanguage.English -> "No appointments on this day"
        AppLanguage.French -> "Aucun rendez-vous ce jour"
        AppLanguage.Arabic -> "لا توجد مواعيد في هذا اليوم"
    }
    "appointment" -> when (language) {
        AppLanguage.English -> "Appointment"
        AppLanguage.French -> "Rendez-vous"
        AppLanguage.Arabic -> "موعد"
    }
    "open_client" -> when (language) {
        AppLanguage.English -> "Open client"
        AppLanguage.French -> "Ouvrir client"
        AppLanguage.Arabic -> "فتح العميل"
    }
    "edit_booking" -> when (language) {
        AppLanguage.English -> "Edit booking"
        AppLanguage.French -> "Modifier reservation"
        AppLanguage.Arabic -> "تعديل الموعد"
    }
    "delete_booking" -> when (language) {
        AppLanguage.English -> "Delete booking"
        AppLanguage.French -> "Supprimer reservation"
        AppLanguage.Arabic -> "حذف الموعد"
    }
    "delete_booking_confirm_title" -> when (language) {
        AppLanguage.English -> "Delete booking?"
        AppLanguage.French -> "Supprimer reservation ?"
        AppLanguage.Arabic -> "حذف الموعد؟"
    }
    "delete_booking_confirm_text" -> when (language) {
        AppLanguage.English -> "This action cannot be undone."
        AppLanguage.French -> "Action irreversible."
        AppLanguage.Arabic -> "لا يمكن التراجع عن هذا الإجراء."
    }
    "close" -> when (language) {
        AppLanguage.English -> "Close"
        AppLanguage.French -> "Fermer"
        AppLanguage.Arabic -> "إغلاق"
    }
    "delete" -> when (language) {
        AppLanguage.English -> "Delete"
        AppLanguage.French -> "Supprimer"
        AppLanguage.Arabic -> "حذف"
    }
    "cancel" -> when (language) {
        AppLanguage.English -> "Cancel"
        AppLanguage.French -> "Annuler"
        AppLanguage.Arabic -> "إلغاء"
    }
    "booking" -> when (language) {
        AppLanguage.English -> "Booking"
        AppLanguage.French -> "Reservation"
        AppLanguage.Arabic -> "موعد"
    }
    "date" -> when (language) {
        AppLanguage.English -> "Date"
        AppLanguage.French -> "Date"
        AppLanguage.Arabic -> "التاريخ"
    }
    "time" -> when (language) {
        AppLanguage.English -> "Time"
        AppLanguage.French -> "Heure"
        AppLanguage.Arabic -> "الوقت"
    }
    "location" -> when (language) {
        AppLanguage.English -> "Location"
        AppLanguage.French -> "Lieu"
        AppLanguage.Arabic -> "المكان"
    }
    "notes" -> when (language) {
        AppLanguage.English -> "Notes"
        AppLanguage.French -> "Notes"
        AppLanguage.Arabic -> "ملاحظات"
    }
    "save_booking" -> when (language) {
        AppLanguage.English -> "Save booking"
        AppLanguage.French -> "Enregistrer reservation"
        AppLanguage.Arabic -> "حفظ الموعد"
    }
    "phone" -> when (language) {
        AppLanguage.English -> "Phone"
        AppLanguage.French -> "Telephone"
        AppLanguage.Arabic -> "الهاتف"
    }
    "service_type" -> when (language) {
        AppLanguage.English -> "Service type"
        AppLanguage.French -> "Type service"
        AppLanguage.Arabic -> "نوع الخدمة"
    }
    "payment" -> when (language) {
        AppLanguage.English -> "Payment"
        AppLanguage.French -> "Paiement"
        AppLanguage.Arabic -> "الدفع"
    }
    "payment_for" -> when (language) {
        AppLanguage.English -> "Payment for"
        AppLanguage.French -> "Paiement pour"
        AppLanguage.Arabic -> "الدفع لـ"
    }
    "total_amount" -> when (language) {
        AppLanguage.English -> "Total amount"
        AppLanguage.French -> "Montant total"
        AppLanguage.Arabic -> "المبلغ الإجمالي"
    }
    "paid_amount" -> when (language) {
        AppLanguage.English -> "Paid amount"
        AppLanguage.French -> "Montant paye"
        AppLanguage.Arabic -> "المبلغ المدفوع"
    }
    "due_date" -> when (language) {
        AppLanguage.English -> "Due date"
        AppLanguage.French -> "Date echeance"
        AppLanguage.Arabic -> "تاريخ الاستحقاق"
    }
    "save_payment" -> when (language) {
        AppLanguage.English -> "Save payment"
        AppLanguage.French -> "Enregistrer paiement"
        AppLanguage.Arabic -> "حفظ الدفع"
    }
    "send_whatsapp" -> when (language) {
        AppLanguage.English -> "Send WhatsApp"
        AppLanguage.French -> "Envoyer WhatsApp"
        AppLanguage.Arabic -> "إرسال واتساب"
    }
    "pipeline" -> when (language) {
        AppLanguage.English -> "Pipeline"
        AppLanguage.French -> "Pipeline"
        AppLanguage.Arabic -> "المسار"
    }
    "add_payment_details" -> when (language) {
        AppLanguage.English -> "Add payment details"
        AppLanguage.French -> "Ajouter details paiement"
        AppLanguage.Arabic -> "إضافة تفاصيل الدفع"
    }
    "total" -> when (language) {
        AppLanguage.English -> "Total"
        AppLanguage.French -> "Total"
        AppLanguage.Arabic -> "الإجمالي"
    }
    "paid" -> when (language) {
        AppLanguage.English -> "Paid"
        AppLanguage.French -> "Paye"
        AppLanguage.Arabic -> "المدفوع"
    }
    "balance" -> when (language) {
        AppLanguage.English -> "Balance"
        AppLanguage.French -> "Reste"
        AppLanguage.Arabic -> "المتبقي"
    }
    "due" -> when (language) {
        AppLanguage.English -> "Due"
        AppLanguage.French -> "Echeance"
        AppLanguage.Arabic -> "الاستحقاق"
    }
    "bookings" -> when (language) {
        AppLanguage.English -> "Bookings"
        AppLanguage.French -> "Reservations"
        AppLanguage.Arabic -> "المواعيد"
    }
    "invoice" -> when (language) {
        AppLanguage.English -> "Invoice"
        AppLanguage.French -> "Facture"
        AppLanguage.Arabic -> "فاتورة"
    }
    "add_booking" -> when (language) {
        AppLanguage.English -> "Add booking"
        AppLanguage.French -> "Ajouter reservation"
        AppLanguage.Arabic -> "إضافة موعد"
    }
    "invoices" -> when (language) {
        AppLanguage.English -> "Invoices"
        AppLanguage.French -> "Factures"
        AppLanguage.Arabic -> "الفواتير"
    }
    "status_all" -> when (language) {
        AppLanguage.English -> "All"
        AppLanguage.French -> "Tout"
        AppLanguage.Arabic -> "الكل"
    }
    "status_lead" -> when (language) {
        AppLanguage.English -> "Lead"
        AppLanguage.French -> "Prospect"
        AppLanguage.Arabic -> "عميل محتمل"
    }
    "status_quoted" -> when (language) {
        AppLanguage.English -> "Quoted"
        AppLanguage.French -> "Devis envoyé"
        AppLanguage.Arabic -> "تم تقديم عرض"
    }
    "status_booked" -> when (language) {
        AppLanguage.English -> "Booked"
        AppLanguage.French -> "Réservé"
        AppLanguage.Arabic -> "محجوز"
    }
    "status_completed" -> when (language) {
        AppLanguage.English -> "Completed"
        AppLanguage.French -> "Terminé"
        AppLanguage.Arabic -> "مكتمل"
    }
    "status_paid" -> when (language) {
        AppLanguage.English -> "Paid"
        AppLanguage.French -> "Payé"
        AppLanguage.Arabic -> "مدفوع"
    }
    "service" -> when (language) {
        AppLanguage.English -> "Service"
        AppLanguage.French -> "Service"
        AppLanguage.Arabic -> "الخدمة"
    }
    "invoice_created" -> when (language) {
        AppLanguage.English -> "Invoice created"
        AppLanguage.French -> "Facture creee"
        AppLanguage.Arabic -> "تم إنشاء الفاتورة"
    }
    "add_payment_first" -> when (language) {
        AppLanguage.English -> "Add payment first"
        AppLanguage.French -> "Ajouter paiement d'abord"
        AppLanguage.Arabic -> "أضف الدفع أولاً"
    }
    "generate_invoice" -> when (language) {
        AppLanguage.English -> "Generate invoice"
        AppLanguage.French -> "Generer facture"
        AppLanguage.Arabic -> "إنشاء فاتورة"
    }
    "delete_client" -> when (language) {
        AppLanguage.English -> "Delete client"
        AppLanguage.French -> "Supprimer client"
        AppLanguage.Arabic -> "حذف العميل"
    }
    "delete_client_confirm_title" -> when (language) {
        AppLanguage.English -> "Delete client?"
        AppLanguage.French -> "Supprimer client ?"
        AppLanguage.Arabic -> "حذف العميل؟"
    }
    "app_security" -> when (language) {
        AppLanguage.English -> "App security"
        AppLanguage.French -> "Securite application"
        AppLanguage.Arabic -> "أمان التطبيق"
    }
    "security_desc" -> when (language) {
        AppLanguage.English -> "PIN lock enabled. Data stays local on device."
        AppLanguage.French -> "Verrou PIN actif. Donnees restent locales."
        AppLanguage.Arabic -> "قفل PIN مفعل. البيانات تبقى محلية على الجهاز."
    }
    "lock_now" -> when (language) {
        AppLanguage.English -> "Lock app now"
        AppLanguage.French -> "Verrouiller maintenant"
        AppLanguage.Arabic -> "اقفل التطبيق الآن"
    }
    "reset_pin" -> when (language) {
        AppLanguage.English -> "Reset PIN"
        AppLanguage.French -> "Reinitialiser PIN"
        AppLanguage.Arabic -> "إعادة تعيين PIN"
    }
    "upgrade" -> when (language) {
        AppLanguage.English -> "Upgrade"
        AppLanguage.French -> "Mise a niveau"
        AppLanguage.Arabic -> "الترقية"
    }
    "free_limit" -> when (language) {
        AppLanguage.English -> "Free limit"
        AppLanguage.French -> "Limite gratuite"
        AppLanguage.Arabic -> "الحد المجاني"
    }
    "activation_code" -> when (language) {
        AppLanguage.English -> "Activation code"
        AppLanguage.French -> "Code activation"
        AppLanguage.Arabic -> "رمز التفعيل"
    }
    "pro_activated" -> when (language) {
        AppLanguage.English -> "Pro activated"
        AppLanguage.French -> "Pro active"
        AppLanguage.Arabic -> "تم تفعيل البرو"
    }
    "invalid_code" -> when (language) {
        AppLanguage.English -> "Invalid code"
        AppLanguage.French -> "Code invalide"
        AppLanguage.Arabic -> "رمز غير صالح"
    }
    "activate" -> when (language) {
        AppLanguage.English -> "Activate"
        AppLanguage.French -> "Activer"
        AppLanguage.Arabic -> "تفعيل"
    }
    "device_id" -> when (language) {
        AppLanguage.English -> "Device ID"
        AppLanguage.French -> "ID Appareil"
        AppLanguage.Arabic -> "معرف الجهاز"
    }
    "copied" -> when (language) {
        AppLanguage.English -> "Copied to clipboard"
        AppLanguage.French -> "Copie dans le presse-papiers"
        AppLanguage.Arabic -> "تم النسخ"
    }
    "contact_email" -> when (language) {
        AppLanguage.English -> "k.ahmed.lara@gmail.com"
        AppLanguage.French -> "k.ahmed.lara@gmail.com"
        AppLanguage.Arabic -> "k.ahmed.lara@gmail.com"
    }
    "contact_whatsapp" -> when (language) {
        AppLanguage.English -> "+212666289222"
        AppLanguage.French -> "+212666289222"
        AppLanguage.Arabic -> "+212666289222"
    }
    "contact_website" -> when (language) {
        AppLanguage.English -> "laaraichi.com"
        AppLanguage.French -> "laaraichi.com"
        AppLanguage.Arabic -> "laaraichi.com"
    }
    "summary" -> when (language) {
        AppLanguage.English -> "Summary"
        AppLanguage.French -> "Resume"
        AppLanguage.Arabic -> "الملخص"
    }
    "pin" -> when (language) {
        AppLanguage.English -> "PIN"
        AppLanguage.French -> "PIN"
        AppLanguage.Arabic -> "PIN"
    }
    "whatsapp_templates" -> when (language) {
        AppLanguage.English -> "WhatsApp templates"
        AppLanguage.French -> "Modeles WhatsApp"
        AppLanguage.Arabic -> "قوالب واتساب"
    }
    "send" -> when (language) {
        AppLanguage.English -> "Send"
        AppLanguage.French -> "Envoyer"
        AppLanguage.Arabic -> "إرسال"
    }
    "edit" -> when (language) {
        AppLanguage.English -> "Edit"
        AppLanguage.French -> "Modifier"
        AppLanguage.Arabic -> "تعديل"
    }
    "new_template" -> when (language) {
        AppLanguage.English -> "New template"
        AppLanguage.French -> "Nouveau modele"
        AppLanguage.Arabic -> "قالب جديد"
    }
    "save" -> when (language) {
        AppLanguage.English -> "Save"
        AppLanguage.French -> "Enregistrer"
        AppLanguage.Arabic -> "حفظ"
    }
    "company_name" -> when (language) {
        AppLanguage.English -> "Company Name"
        AppLanguage.French -> "Nom de l'entreprise"
        AppLanguage.Arabic -> "اسم الشركة"
    }
    "address" -> when (language) {
        AppLanguage.English -> "Address"
        AppLanguage.French -> "Adresse"
        AppLanguage.Arabic -> "العنوان"
    }
    "phone" -> when (language) {
        AppLanguage.English -> "Phone"
        AppLanguage.French -> "Telephone"
        AppLanguage.Arabic -> "الهاتف"
    }
    "email" -> when (language) {
        AppLanguage.English -> "Email"
        AppLanguage.French -> "Email"
        AppLanguage.Arabic -> "البريد الإلكتروني"
    }
    "ice" -> when (language) {
        AppLanguage.English -> "ICE"
        AppLanguage.French -> "ICE"
        AppLanguage.Arabic -> "ICE"
    }
    "logo_url" -> when (language) {
        AppLanguage.English -> "Logo URL"
        AppLanguage.French -> "URL du logo"
        AppLanguage.Arabic -> "رابط الشعار"
    }
    "accent_color" -> when (language) {
        AppLanguage.English -> "Accent Color"
        AppLanguage.French -> "Couleur d'accent"
        AppLanguage.Arabic -> "لون التمييز"
    }
    "invoice_note" -> when (language) {
        AppLanguage.English -> "Invoice Note"
        AppLanguage.French -> "Note de facture"
        AppLanguage.Arabic -> "ملاحظة الفاتورة"
    }
    "template" -> when (language) {
        AppLanguage.English -> "Template"
        AppLanguage.French -> "Modele"
        AppLanguage.Arabic -> "قالب"
    }
    "emoji" -> when (language) {
        AppLanguage.English -> "Emoji"
        AppLanguage.French -> "Emoji"
        AppLanguage.Arabic -> "إيموجي"
    }
    "message" -> when (language) {
        AppLanguage.English -> "Message"
        AppLanguage.French -> "Message"
        AppLanguage.Arabic -> "رسالة"
    }
    "unknown_client" -> when (language) {
        AppLanguage.English -> "Unknown client"
        AppLanguage.French -> "Client inconnu"
        AppLanguage.Arabic -> "عميل غير معروف"
    }
    "tap_quick_details" -> when (language) {
        AppLanguage.English -> "Tap for quick details"
        AppLanguage.French -> "Touchez pour details rapides"
        AppLanguage.Arabic -> "اضغط للتفاصيل السريعة"
    }
    else -> key
}
