package com.ahmed.clientflow.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
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
import com.ahmed.clientflow.data.AppState
import com.ahmed.clientflow.data.AppLanguage
import com.ahmed.clientflow.data.AuthState
import com.ahmed.clientflow.data.Booking
import com.ahmed.clientflow.data.Client
import com.ahmed.clientflow.data.ClientStatus
import com.ahmed.clientflow.data.Invoice
import com.ahmed.clientflow.data.MessageTemplate
import com.ahmed.clientflow.data.Payment
import com.ahmed.clientflow.data.PaymentStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val pipeline = ClientStatus.entries

private const val lottieJson = """
{"v":"5.7.4","fr":30,"ip":0,"op":120,"w":200,"h":200,"nm":"pulse","ddd":0,"assets":[],"layers":[{"ddd":0,"ind":1,"ty":4,"nm":"Circle","sr":1,"ks":{"o":{"a":0,"k":100},"r":{"a":0,"k":0},"p":{"a":0,"k":[100,100,0]},"a":{"a":0,"k":[0,0,0]},"s":{"a":1,"k":[{"t":0,"s":[40,40,100]},{"t":60,"s":[100,100,100]},{"t":120,"s":[40,40,100]}]}},"shapes":[{"ty":"el","p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[120,120]},"nm":"Ellipse Path 1"},{"ty":"fl","c":{"a":0,"k":[0.145,0.388,0.922,1]},"o":{"a":0,"k":100},"nm":"Fill 1"}],"ip":0,"op":120,"st":0,"bm":0}]}
"""

sealed class BottomRoute(val route: String, val label: String) {
    data object Home : BottomRoute("home", "Home")
    data object Clients : BottomRoute("clients", "Clients")
    data object Bookings : BottomRoute("bookings", "Bookings")
}

@Composable
fun ClientFlowApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.exportPayload) {
        uiState.exportPayload?.let { payload ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, payload)
            }
            context.startActivity(Intent.createChooser(intent, "Export ClientFlow data"))
            viewModel.dismissExport()
        }
    }

    when (uiState.authState) {
        AuthState.Setup -> SetupPinScreen(viewModel)
        AuthState.Locked -> LockScreen(viewModel, uiState.pinError)
        AuthState.Unlocked -> MainScaffold(viewModel, uiState.appState, snackbarHostState)
    }
}

@Composable
private fun SetupPinScreen(viewModel: MainViewModel) {
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
            Text("Secure ClientFlow", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Set 4-digit PIN", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            PinField(pin) { if (it.length <= 4) pin = it.filter(Char::isDigit) }
            Spacer(Modifier.height(12.dp))
            PinField(confirm) { if (it.length <= 4) confirm = it.filter(Char::isDigit) }
            Spacer(Modifier.height(20.dp))
            Button(onClick = { viewModel.setupPin(pin) }, enabled = pin.length == 4 && pin == confirm) {
                Text("Save PIN")
            }
        }
    }
}

@Composable
private fun LockScreen(viewModel: MainViewModel, pinError: Boolean) {
    var pin by rememberSaveable { mutableStateOf("") }
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
            Text("ClientFlow Locked", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Enter PIN to continue", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            PinField(pin) { if (it.length <= 4) pin = it.filter(Char::isDigit) }
            AnimatedVisibility(pinError) {
                Text("Wrong PIN", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = { viewModel.unlock(pin); pin = "" }, enabled = pin.length == 4) {
                Text("Unlock")
            }
        }
    }
}

@Composable
private fun MainScaffold(viewModel: MainViewModel, state: AppState, snackbarHostState: SnackbarHostState) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: BottomRoute.Home.route

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                listOf(BottomRoute.Home, BottomRoute.Clients, BottomRoute.Bookings).forEach { route ->
                    val icon = when (route) {
                        BottomRoute.Home -> Icons.Default.Home
                        BottomRoute.Clients -> Icons.Default.People
                        BottomRoute.Bookings -> Icons.Default.CalendarMonth
                    }
                    NavigationBarItem(
                        selected = currentRoute == route.route,
                        onClick = { navController.navigate(route.route) { launchSingleTop = true } },
                        icon = { Icon(icon, null) },
                        label = { Text(route.label) }
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
                    onGenerateInvoice = viewModel::generateInvoice,
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
            composable("security") {
                SecurityScreen(onBack = { navController.popBackStack() }, onLock = {
                    viewModel.lockApp()
                    navController.popBackStack()
                }, onClearPin = viewModel::clearPin)
            }
            composable("settings") {
                SettingsScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onOpenSecurity = { navController.navigate("security") },
                    onOpenLicense = { navController.navigate("license") },
                    onSetLanguage = viewModel::setLanguage
                )
            }
            composable("license") {
                LicenseScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onActivate = { code, done -> viewModel.activatePro(code, done) }
                )
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
    val todayBookings = state.bookings.filter { it.date == today }
    val overdue = state.payments.filter { it.status != PaymentStatus.Paid && it.dueDate.isNotBlank() && it.dueDate < today }
    val revenue = state.payments.sumOf { it.paidAmount }
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
                IconButton(onClick = onExport) { Icon(Icons.Default.Send, null) }
                IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, null) }
            }
        }
        item {
            StatsRow(
                items = listOf(
                    "Clients" to state.clients.size.toString(),
                    "Bookings" to state.bookings.size.toString(),
                    "Revenue" to "$${revenue.toInt()}",
                    "Overdue" to overdue.size.toString()
                )
            )
        }
        item { SectionTitle("Today's bookings") }
        if (todayBookings.isEmpty()) item { EmptyCard("No bookings today") }
        items(todayBookings) { booking ->
            BookingCard(booking = booking, client = state.clients.find { it.id == booking.clientId }, onClick = { onOpenClient(booking.clientId) })
        }
        if (overdue.isNotEmpty()) {
            item { SectionTitle("Overdue payments") }
            items(overdue) { payment ->
                val client = state.clients.find { it.id == payment.clientId } ?: return@items
                PaymentCard(client = client, payment = payment, onClick = { onOpenClient(client.id) })
            }
        }
        item { SectionTitle("Active pipeline") }
        items(state.clients.filter { it.status in listOf(ClientStatus.Lead, ClientStatus.Quoted, ClientStatus.Booked) }.take(5)) { client ->
            ClientCard(client = client, payment = state.payments.find { it.clientId == client.id }, onClick = { onOpenClient(client.id) })
        }
    }
}

@Composable
private fun SettingsScreen(
    state: AppState,
    onBack: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenLicense: () -> Unit,
    onSetLanguage: (AppLanguage) -> Unit
) {
    FormScaffold(title = tr("settings", state.language), onBack = onBack) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(tr("language", state.language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(tr("language_desc", state.language), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    StatusLanguageChips(selected = state.language, onSelect = onSetLanguage)
                }
            }
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(tr("security", state.language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = onOpenSecurity, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Shield, null)
                        Spacer(Modifier.size(8.dp))
                        Text(tr("open_security", state.language))
                    }
                }
            }
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(tr("plan", state.language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        if (state.isPro) tr("pro_active", state.language) else tr("free_plan", state.language),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = onOpenLicense, modifier = Modifier.fillMaxWidth()) {
                        Text(tr("manage_plan", state.language))
                    }
                }
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
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("All") }
    val filtered = state.clients.filter {
        val matchSearch = search.isBlank() || it.name.contains(search, true) || it.phone.contains(search, true) || it.serviceType.contains(search, true)
        val matchFilter = filter == "All" || it.status.name == filter
        matchSearch && matchFilter
    }.sortedByDescending { it.createdAt }

    Column(Modifier.fillMaxSize()) {
        SearchBar(search = search, onSearch = { search = it })
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                StatusFilters(selected = filter, onSelect = { filter = it })
            }
            if (filtered.isEmpty()) item { EmptyCard("No clients yet") }
            items(filtered) { client ->
                ClientCard(client = client, payment = state.payments.find { it.clientId == client.id }, onClick = { onOpenClient(client.id) })
            }
            item {
                if (!canAddMore) {
                    OutlinedButton(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) {
                        Text("Upgrade to add more clients")
                    }
                } else {
                    OutlinedButton(onClick = onAddClient, modifier = Modifier.fillMaxWidth()) {
                        Text("Add client")
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
    var search by rememberSaveable { mutableStateOf("") }
    var monthOffset by rememberSaveable { mutableStateOf(0) }
    var selectedDate by rememberSaveable { mutableStateOf(todayKey()) }
    var previewBookingId by rememberSaveable { mutableStateOf<String?>(null) }
    var lastMonthOffset by rememberSaveable { mutableStateOf(0) }
    val bookings = state.bookings.filter {
        val client = state.clients.find { c -> c.id == it.clientId }
        search.isBlank() || it.date.contains(search, true) || it.location.contains(search, true) || (client?.name?.contains(search, true) == true)
    }.sortedBy { it.date }
    val monthCalendar = remember(monthOffset) {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, monthOffset)
        }
    }
    val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.US).format(monthCalendar.time)
    val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val leadingBlanks = ((monthCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7)
    val dateBookingCount = bookings.groupingBy { it.date }.eachCount()
    val selectedDayBookings = bookings.filter { it.date == selectedDate }
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
        SearchBar(search = search, onSearch = { search = it }, placeholder = "Search bookings")
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
                SectionTitle("Appointments on $selectedDate (${selectedDayBookings.size})")
            }
            if (selectedDayBookings.isEmpty()) {
                item { EmptyCard("No appointments on this day") }
            } else {
                items(selectedDayBookings) { booking ->
                    DayBookingCard(booking = booking, client = state.clients.find { it.id == booking.clientId }, onClick = { previewBookingId = booking.id })
                }
            }
        }
    }

    if (previewBooking != null) {
        BookingDetailSheet(
            booking = previewBooking,
            client = state.clients.find { it.id == previewBooking.clientId },
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
private fun DayBookingCard(booking: Booking, client: Client?, onClick: () -> Unit) {
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
                    Text(client?.name ?: "Unknown client", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                Text("Location: ${booking.location}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (booking.notes.isNotBlank()) {
                Text(booking.notes)
            }
            Text("Tap for quick details", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingDetailSheet(
    booking: Booking,
    client: Client?,
    onDismiss: () -> Unit,
    onOpenClient: () -> Unit,
    onEditBooking: () -> Unit,
    onDeleteBooking: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(20.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(client?.name ?: "Appointment", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (!client?.phone.isNullOrBlank()) Text("Phone: ${client?.phone}")
            if (!client?.serviceType.isNullOrBlank()) Text("Service: ${client?.serviceType}")
            if (booking.date.isNotBlank()) Text("Date: ${booking.date}")
            if (booking.time.isNotBlank()) Text("Time: ${booking.time}")
            if (booking.location.isNotBlank()) Text("Location: ${booking.location}")
            if (booking.notes.isNotBlank()) Text("Notes: ${booking.notes}")
            Button(onClick = onOpenClient, modifier = Modifier.fillMaxWidth()) {
                Text("Open client")
            }
            OutlinedButton(onClick = onEditBooking, modifier = Modifier.fillMaxWidth()) {
                Text("Edit booking")
            }
            OutlinedButton(onClick = onDeleteBooking, modifier = Modifier.fillMaxWidth()) {
                Text("Delete booking")
            }
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Close")
            }
        }
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
    var name by rememberSaveable { mutableStateOf(existing?.name.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(existing?.phone.orEmpty()) }
    var serviceType by rememberSaveable { mutableStateOf(existing?.serviceType.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(existing?.notes.orEmpty()) }
    var status by rememberSaveable { mutableStateOf(existing?.status ?: ClientStatus.Lead) }
    FormScaffold(title = if (existing == null) "Add Client" else "Edit Client", onBack = onBack) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(serviceType, { serviceType = it }, label = { Text("Service type") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            StatusDropdown(status = status, onSelect = { status = it })
            Button(
                onClick = {
                    onSave(clientId, name, phone, serviceType, notes, status)
                    onBack()
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (existing == null) "Add client" else "Save changes") }
        }
    }
}

@Composable
private fun BookingFormScreen(
    state: AppState,
    clientId: String,
    bookingId: String?,
    onBack: () -> Unit,
    onSave: (String?, String, String, String, String, String) -> Unit
) {
    val booking = state.bookings.find { it.id == bookingId }
    var date by rememberSaveable { mutableStateOf(booking?.date.orEmpty()) }
    var time by rememberSaveable { mutableStateOf(booking?.time.orEmpty()) }
    var location by rememberSaveable { mutableStateOf(booking?.location.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(booking?.notes.orEmpty()) }
    FormScaffold(title = "Booking", onBack = onBack) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DatePickerField(value = date, label = "Date", onDateSelected = { date = it })
            TimePickerField(value = time, label = "Time", onTimeSelected = { time = it })
            OutlinedTextField(location, { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Button(onClick = {
                onSave(bookingId, clientId, date, time, location, notes)
                onBack()
            }, modifier = Modifier.fillMaxWidth()) { Text("Save booking") }
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
    var total by rememberSaveable { mutableStateOf(payment?.totalAmount?.toString().orEmpty()) }
    var paid by rememberSaveable { mutableStateOf(payment?.paidAmount?.toString().orEmpty()) }
    var dueDate by rememberSaveable { mutableStateOf(payment?.dueDate.orEmpty()) }
    FormScaffold(title = "Payment", onBack = onBack) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Payment for ${client?.name.orEmpty()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(total, { total = it }, label = { Text("Total amount") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OutlinedTextField(paid, { paid = it }, label = { Text("Paid amount") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            DatePickerField(value = dueDate, label = "Due date", onDateSelected = { dueDate = it })
            SummaryCard(total.toDoubleOrNull() ?: 0.0, paid.toDoubleOrNull() ?: 0.0, dueDate)
            Button(onClick = {
                onSave(clientId, total.toDoubleOrNull() ?: 0.0, paid.toDoubleOrNull() ?: 0.0, dueDate)
                onBack()
            }, modifier = Modifier.fillMaxWidth()) { Text("Save payment") }
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
    onGenerateInvoice: (String, Double, String) -> Unit,
    onSaveTemplate: (String?, String, String, String) -> Unit,
    onDeleteTemplate: (String) -> Unit
) {
    val context = LocalContext.current
    val client = state.clients.find { it.id == clientId } ?: return
    val bookings = state.bookings.filter { it.clientId == clientId }.sortedBy { it.date }
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
                            Icon(Icons.Default.Message, null)
                            Spacer(Modifier.size(8.dp))
                            Text("Send WhatsApp")
                        }
                    }
                }
            }
            item { SectionTitle("Pipeline") }
            item {
                PipelineRow(current = client.status, onSelect = onStatusChange)
            }
            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Payment", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            IconButton(onClick = onOpenPayment) { Icon(Icons.Default.Edit, null) }
                        }
                        if (payment == null) {
                            OutlinedButton(onClick = onOpenPayment, modifier = Modifier.fillMaxWidth()) { Text("Add payment details") }
                        } else {
                            Text("Total: $${payment.totalAmount}")
                            Text("Paid: $${payment.paidAmount}")
                            Text("Balance: $${payment.totalAmount - payment.paidAmount}")
                            Text("Due: ${payment.dueDate.ifBlank { "-" }}")
                            AssistChip(onClick = {}, label = { Text(payment.status.name) })
                        }
                    }
                }
            }
            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Bookings", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            IconButton(onClick = { onOpenBooking(null) }) { Icon(Icons.Default.Add, null) }
                        }
                        if (bookings.isEmpty()) {
                            OutlinedButton(onClick = { onOpenBooking(null) }, modifier = Modifier.fillMaxWidth()) { Text("Add booking") }
                        } else {
                            bookings.forEach { booking ->
                                BookingRowCompact(
                                    booking = booking,
                                    onEdit = { onOpenBooking(booking.id) },
                                    onDelete = { onDeleteBooking(booking.id) }
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
                            Text("Invoices", fontWeight = FontWeight.SemiBold)
                            invoices.forEach { invoice ->
                                InvoiceRow(invoice)
                            }
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        if (payment != null) {
                            onGenerateInvoice(client.id, payment.totalAmount, client.serviceType.ifBlank { "Service" })
                            Toast.makeText(context, "Invoice created", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Add payment first", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Generate invoice") }
            }
            item {
                OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.size(8.dp))
                    Text("Delete client")
                }
            }
        }
    }

    if (showTemplates) {
        MessageTemplateSheet(
            templates = state.templates,
            client = client,
            nextBooking = bookings.firstOrNull(),
            payment = payment,
            onDismiss = { showTemplates = false },
            onSaveTemplate = onSaveTemplate,
            onDeleteTemplate = onDeleteTemplate
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            confirmButton = { TextButton(onClick = onDeleteClient) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
            title = { Text("Delete client?") },
            text = { Text(client.name) }
        )
    }
}

@Composable
private fun SecurityScreen(onBack: () -> Unit, onLock: () -> Unit, onClearPin: () -> Unit) {
    FormScaffold(title = "Security", onBack = onBack) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("App security", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("PIN lock enabled. Data stays local on device.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onLock, modifier = Modifier.fillMaxWidth()) { Text("Lock app now") }
                    OutlinedButton(onClick = onClearPin, modifier = Modifier.fillMaxWidth()) { Text("Reset PIN") }
                }
            }
        }
    }
}

@Composable
private fun LicenseScreen(state: AppState, onBack: () -> Unit, onActivate: (String, (Boolean) -> Unit) -> Unit) {
    val context = LocalContext.current
    var code by rememberSaveable { mutableStateOf("") }
    FormScaffold(title = "Upgrade", onBack = onBack) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (state.isPro) "Pro active" else "Free plan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Free limit: ${state.freeClientLimit} client", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            OutlinedTextField(code, { code = it }, label = { Text("Activation code") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                onActivate(code) { ok ->
                    Toast.makeText(context, if (ok) "Pro activated" else "Invalid code", Toast.LENGTH_SHORT).show()
                    if (ok) onBack()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Activate") }
        }
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
    val focusManager = LocalFocusManager.current
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                focusManager.clearFocus()
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
    )
}

@Composable
private fun TimePickerField(
    value: String,
    label: String,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val parts = value.split(":")
    val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 9
    val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                focusManager.clearFocus()
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
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusFilters(selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        (listOf("All") + ClientStatus.entries.map { it.name }).forEach {
            AssistChip(onClick = { onSelect(it) }, label = { Text(it) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusLanguageChips(selected: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppLanguage.entries.forEach { language ->
            AssistChip(onClick = { onSelect(language) }, label = { Text(languageLabel(language)) })
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun EmptyCard(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatsRow(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { chunk ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chunk.forEach { (label, value) ->
                    Card(modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientCard(client: Client, payment: Payment?, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(client.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                AssistChip(onClick = {}, label = { Text(client.status.name) })
            }
            Text(client.phone, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (client.serviceType.isNotBlank()) Text(client.serviceType, color = MaterialTheme.colorScheme.primary)
            payment?.let { Text("Payment: ${it.status.name}", color = when (it.status) {
                PaymentStatus.Paid -> Color(0xFF15803D)
                PaymentStatus.Partial -> Color(0xFFD97706)
                PaymentStatus.Unpaid -> MaterialTheme.colorScheme.error
            }) }
        }
    }
}

@Composable
private fun BookingCard(booking: Booking, client: Client?, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(client?.name ?: "Unknown client", fontWeight = FontWeight.Bold)
            Text("${booking.date} ${booking.time}".trim(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (booking.location.isNotBlank()) Text(booking.location)
            if (booking.notes.isNotBlank()) Text(booking.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PaymentCard(client: Client, payment: Payment, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2))) {
        Column(Modifier.padding(16.dp)) {
            Text(client.name, fontWeight = FontWeight.Bold)
            Text("Balance: $${payment.totalAmount - payment.paidAmount}", color = MaterialTheme.colorScheme.error)
            Text("Due ${payment.dueDate}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SummaryCard(total: Double, paid: Double, dueDate: String) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Summary", fontWeight = FontWeight.Bold)
            Text("Total: $${"%.2f".format(total)}")
            Text("Paid: $${"%.2f".format(paid)}")
            Text("Balance: $${"%.2f".format(total - paid)}")
            if (dueDate.isNotBlank()) Text("Due: $dueDate")
            AssistChip(onClick = {}, label = { Text(paymentStatusText(total, paid)) })
        }
    }
}

@Composable
private fun PipelineRow(current: ClientStatus, onSelect: (ClientStatus) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        pipeline.forEach { status ->
            AssistChip(onClick = { onSelect(status) }, label = { Text(status.name) })
        }
    }
}

@Composable
private fun BookingRowCompact(booking: Booking, onEdit: () -> Unit, onDelete: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${booking.date} ${booking.time}".trim(), modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null) }
        }
        if (booking.location.isNotBlank()) Text(booking.location)
        if (booking.notes.isNotBlank()) Text(booking.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Divider()
    }
}

@Composable
private fun InvoiceRow(invoice: Invoice) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(invoice.description, fontWeight = FontWeight.Medium)
            Text(formatDateHuman(invoice.createdAt), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("$${invoice.amount}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
        label = { Text("PIN") },
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
            Text("WhatsApp templates", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                            }, modifier = Modifier.weight(1f)) { Text("Send") }
                            OutlinedButton(onClick = {
                                editTarget = template
                                editingId = template.id
                                name = template.name
                                content = template.content
                                emoji = template.emoji
                            }, modifier = Modifier.weight(1f)) { Text("Edit") }
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
            }, modifier = Modifier.fillMaxWidth()) { Text("New template") }
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
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editTarget = null; editingId = null }) { Text("Cancel") } },
            title = { Text("Template") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Name") })
                    OutlinedTextField(emoji, { emoji = it }, label = { Text("Emoji") })
                    OutlinedTextField(content, { content = it }, label = { Text("Message") }, minLines = 4)
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

private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

private fun formatDateHuman(millis: Long): String = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(millis))

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
    else -> key
}
