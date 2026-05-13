package com.ahmed.clientflow.ui.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ahmed.clientflow.data.AppLanguage
import com.ahmed.clientflow.data.Client
import com.ahmed.clientflow.data.Expense
import com.ahmed.clientflow.data.ExpenseCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

private fun formatDateHuman(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(millis))

private fun formatAmount(amount: Double): String =
    if (amount == amount.toLong().toDouble()) "%.0f".format(amount) else "%.2f".format(amount)

private fun categoryLabel(category: ExpenseCategory, language: AppLanguage): String = when (category) {
    ExpenseCategory.Travel -> when (language) {
        AppLanguage.English -> "Travel"
        AppLanguage.French -> "Voyage"
        AppLanguage.Arabic -> "سفر"
    }
    ExpenseCategory.Supplies -> when (language) {
        AppLanguage.English -> "Supplies"
        AppLanguage.French -> "Fournitures"
        AppLanguage.Arabic -> "مستلزمات"
    }
    ExpenseCategory.Software -> when (language) {
        AppLanguage.English -> "Software"
        AppLanguage.French -> "Logiciel"
        AppLanguage.Arabic -> "برمجيات"
    }
    ExpenseCategory.Marketing -> when (language) {
        AppLanguage.English -> "Marketing"
        AppLanguage.French -> "Marketing"
        AppLanguage.Arabic -> "تسويق"
    }
    ExpenseCategory.Other -> when (language) {
        AppLanguage.English -> "Other"
        AppLanguage.French -> "Autre"
        AppLanguage.Arabic -> "أخرى"
    }
}

private fun categoryColor(category: ExpenseCategory): androidx.compose.ui.graphics.Color = when (category) {
    ExpenseCategory.Travel -> androidx.compose.ui.graphics.Color(0xFF2196F3)
    ExpenseCategory.Supplies -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
    ExpenseCategory.Software -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
    ExpenseCategory.Marketing -> androidx.compose.ui.graphics.Color(0xFFFF9800)
    ExpenseCategory.Other -> androidx.compose.ui.graphics.Color(0xFF607D8B)
}

private fun tx(key: String, language: AppLanguage): String = when (key) {
    "expenses" -> when (language) {
        AppLanguage.English -> "Expenses"
        AppLanguage.French -> "Depenses"
        AppLanguage.Arabic -> "المصروفات"
    }
    "add_expense" -> when (language) {
        AppLanguage.English -> "Add expense"
        AppLanguage.French -> "Ajouter depense"
        AppLanguage.Arabic -> "إضافة مصروف"
    }
    "edit_expense" -> when (language) {
        AppLanguage.English -> "Edit expense"
        AppLanguage.French -> "Modifier depense"
        AppLanguage.Arabic -> "تعديل مصروف"
    }
    "no_expenses" -> when (language) {
        AppLanguage.English -> "No expenses yet"
        AppLanguage.French -> "Aucune depense"
        AppLanguage.Arabic -> "لا توجد مصروفات"
    }
    "total_expenses" -> when (language) {
        AppLanguage.English -> "Total expenses"
        AppLanguage.French -> "Total depenses"
        AppLanguage.Arabic -> "إجمالي المصروفات"
    }
    "category" -> when (language) {
        AppLanguage.English -> "Category"
        AppLanguage.French -> "Categorie"
        AppLanguage.Arabic -> "الفئة"
    }
    "amount" -> when (language) {
        AppLanguage.English -> "Amount"
        AppLanguage.French -> "Montant"
        AppLanguage.Arabic -> "المبلغ"
    }
    "description" -> when (language) {
        AppLanguage.English -> "Description"
        AppLanguage.French -> "Description"
        AppLanguage.Arabic -> "الوصف"
    }
    "date" -> when (language) {
        AppLanguage.English -> "Date"
        AppLanguage.French -> "Date"
        AppLanguage.Arabic -> "التاريخ"
    }
    "client_optional" -> when (language) {
        AppLanguage.English -> "Client (optional)"
        AppLanguage.French -> "Client (optionnel)"
        AppLanguage.Arabic -> "العميل (اختياري)"
    }
    "none" -> when (language) {
        AppLanguage.English -> "None"
        AppLanguage.French -> "Aucun"
        AppLanguage.Arabic -> "لا شيء"
    }
    "save" -> when (language) {
        AppLanguage.English -> "Save"
        AppLanguage.French -> "Enregistrer"
        AppLanguage.Arabic -> "حفظ"
    }
    "cancel" -> when (language) {
        AppLanguage.English -> "Cancel"
        AppLanguage.French -> "Annuler"
        AppLanguage.Arabic -> "إلغاء"
    }
    "delete" -> when (language) {
        AppLanguage.English -> "Delete"
        AppLanguage.French -> "Supprimer"
        AppLanguage.Arabic -> "حذف"
    }
    "delete_expense_title" -> when (language) {
        AppLanguage.English -> "Delete expense?"
        AppLanguage.French -> "Supprimer depense ?"
        AppLanguage.Arabic -> "حذف المصروف؟"
    }
    "delete_expense_text" -> when (language) {
        AppLanguage.English -> "This action cannot be undone."
        AppLanguage.French -> "Action irreversible."
        AppLanguage.Arabic -> "لا يمكن التراجع عن هذا الإجراء."
    }
    else -> key
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    expenses: List<Expense>,
    clients: List<Client>,
    language: AppLanguage,
    currencySymbol: String = "$",
    onBack: () -> Unit,
    onAddExpense: () -> Unit,
    onEditExpense: (String) -> Unit,
    onDeleteExpense: (String) -> Unit
) {
    val sorted = expenses.sortedByDescending { it.date }
    val totalExpenses = expenses.sumOf { it.amount }
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tx("expenses", language), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        if (expenses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(tx("no_expenses", language), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp + padding.calculateTopPadding()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                tx("total_expenses", language),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "${currencySymbol}${formatAmount(totalExpenses)}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                items(sorted) { expense ->
                    val client = expense.clientId?.let { cid -> clients.find { it.id == cid } }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditExpense(expense.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor(expense.category).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    categoryLabel(expense.category, language).first().uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = categoryColor(expense.category)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    expense.description.ifBlank { categoryLabel(expense.category, language) },
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    buildString {
                                        append(formatDateHuman(expense.date))
                                        if (client != null) append("  \u2022  ${client.name}")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${currencySymbol}${formatAmount(expense.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    categoryLabel(expense.category, language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = categoryColor(expense.category)
                                )
                            }
                            IconButton(onClick = { confirmDeleteId = expense.id }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = tx("delete", language),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    if (confirmDeleteId != null) {
        AlertDialog(
            onDismissRequest = { confirmDeleteId = null },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteExpense(confirmDeleteId!!)
                    confirmDeleteId = null
                }) { Text(tx("delete", language)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteId = null }) {
                    Text(tx("cancel", language))
                }
            },
            title = { Text(tx("delete_expense_title", language)) },
            text = { Text(tx("delete_expense_text", language)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFormScreen(
    expense: Expense?,
    clients: List<Client>,
    language: AppLanguage,
    onBack: () -> Unit,
    onSave: (String?, String?, ExpenseCategory, Double, String, Long) -> Unit
) {
    var category by rememberSaveable { mutableStateOf(expense?.category ?: ExpenseCategory.Other) }
    var amountText by rememberSaveable { mutableStateOf(expense?.amount?.let { if (it == 0.0) "" else formatAmount(it) }.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(expense?.description.orEmpty()) }
    var dateText by rememberSaveable { mutableStateOf(
        expense?.date?.let { formatDateHuman(it).let { d ->
            val parts = d.split("/")
            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else todayKey()
        } } ?: todayKey()
    )}
    var selectedClientId by rememberSaveable { mutableStateOf(expense?.clientId) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var clientExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val calendar = remember(dateText) {
        Calendar.getInstance().apply {
            if (dateText.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                val parts = dateText.split("-")
                set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (expense == null) tx("add_expense", language) else tx("edit_expense", language),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = categoryLabel(category, language),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(tx("category", language)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    ExpenseCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(categoryLabel(cat, language)) },
                            onClick = {
                                category = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { v -> if (v.isEmpty() || v.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amountText = v },
                label = { Text(tx("amount", language)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(tx("description", language)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = dateText,
                onValueChange = {},
                readOnly = true,
                label = { Text(tx("date", language)) },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    dateText = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (clients.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = clientExpanded,
                    onExpandedChange = { clientExpanded = it }
                ) {
                    val selectedClient = clients.find { it.id == selectedClientId }
                    OutlinedTextField(
                        value = selectedClient?.name ?: tx("none", language),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(tx("client_optional", language)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = clientExpanded,
                        onDismissRequest = { clientExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(tx("none", language)) },
                            onClick = {
                                selectedClientId = null
                                clientExpanded = false
                            }
                        )
                        clients.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    selectedClientId = c.id
                                    clientExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val dateMillis = runCatching {
                        val parts = dateText.split("-")
                        Calendar.getInstance().apply {
                            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 12, 0, 0)
                        }.timeInMillis
                    }.getOrDefault(System.currentTimeMillis())
                    onSave(expense?.id, selectedClientId, category, amount, description, dateMillis)
                    onBack()
                },
                enabled = amountText.toDoubleOrNull()?.let { it > 0 } == true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(tx("save", language))
            }
        }
    }
}
