package com.ahmed.clientflow.ui.screen

import android.net.Uri
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahmed.clientflow.data.AppLanguage
import com.ahmed.clientflow.data.Client
import java.io.BufferedReader
import java.io.InputStreamReader

data class ImportPreview(val name: String, val phone: String, val serviceType: String = "", val notes: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    language: AppLanguage,
    isPro: Boolean,
    freeLimit: Int,
    currentClientCount: Int,
    onBack: () -> Unit,
    onImport: (List<Client>) -> Unit
) {
    val context = LocalContext.current
    var parsed by remember { mutableStateOf<List<ImportPreview>>(emptyList()) }
    var selected by remember { mutableStateOf(setOf<Int>()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = parseFile(context, uri)
            when (result) {
                is ParseResult.Success -> {
                    parsed = result.clients
                    selected = parsed.indices.toSet()
                    errorMsg = null
                }
                is ParseResult.Error -> {
                    errorMsg = result.message
                    parsed = emptyList()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tx("import_data", language), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (parsed.isEmpty()) {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(tx("import_title", language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(tx("import_desc", language), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(tx("import_formats", language), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { filePicker.launch("text/*") }, modifier = Modifier.weight(1f)) {
                                Text(tx("pick_csv", language))
                            }
                            OutlinedButton(onClick = { filePicker.launch("text/*") }, modifier = Modifier.weight(1f)) {
                                Text(tx("pick_vcard", language))
                            }
                        }

                        if (errorMsg != null) {
                            Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            } else {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            tx("found_clients", language).replace("{count}", parsed.size.toString()),
                            fontWeight = FontWeight.SemiBold
                        )
                        val canAddMore = isPro || (currentClientCount + selected.size) <= freeLimit
                        if (!canAddMore) {
                            Text(
                                tx("import_limit_reached", language),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { selected = if (selected.size == parsed.size) emptySet() else parsed.indices.toSet() }) {
                                Text(tx("select_all", language))
                            }
                            Spacer(Modifier.weight(1f))
                            if (canAddMore) {
                                Button(onClick = {
                                    val toImport = parsed.filterIndexed { i, _ -> i in selected }.map {
                                        Client(name = it.name, phone = it.phone, serviceType = it.serviceType, notes = it.notes)
                                    }
                                    onImport(toImport)
                                    onBack()
                                }) {
                                    Text(tx("import_selected", language).replace("{count}", selected.size.toString()))
                                }
                            }
                        }
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(parsed) { index, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                selected = if (index in selected) selected - index else selected + index
                            }
                        ) {
                            Row(
                                Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (index in selected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = if (index in selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.SemiBold)
                                    Text(item.phone, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (item.serviceType.isNotBlank()) {
                                        Text(item.serviceType, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class ParseResult {
    data class Success(val clients: List<ImportPreview>) : ParseResult()
    data class Error(val message: String) : ParseResult()
}

private fun parseFile(context: android.content.Context, uri: Uri): ParseResult {
    return try {
        val reader = BufferedReader(InputStreamReader(context.contentResolver.openInputStream(uri) ?: return ParseResult.Error("Cannot open file")))
        val content = reader.readText()
        reader.close()

        val clients = if (content.trimStart().startsWith("BEGIN:VCARD")) {
            parseVCard(content)
        } else {
            parseCSV(content)
        }

        if (clients.isEmpty()) ParseResult.Error("No clients found in file")
        else ParseResult.Success(clients)
    } catch (e: Exception) {
        ParseResult.Error("Parse error: ${e.message}")
    }
}

private fun parseCSV(content: String): List<ImportPreview> {
    val lines = content.lines().filter { it.isNotBlank() }
    if (lines.size < 2) return emptyList()

    val header = lines[0].lowercase().split(",").map { it.trim().replace("\"", "") }
    val nameIdx = header.indexOfFirst { it == "name" || it == "client" }.coerceAtLeast(0)
    val phoneIdx = header.indexOfFirst { it == "phone" || it == "mobile" || it == "number" || it == "tel" || it == "telephone" }
    val serviceIdx = header.indexOfFirst { it == "service" || it == "servicetype" || it == "service_type" || it == "type" }
    val notesIdx = header.indexOfFirst { it == "notes" || it == "note" }

    return lines.drop(1).mapNotNull { line ->
        val parts = splitCSVLine(line)
        val name = parts.getOrElse(nameIdx) { "" }.trim()
        val phone = if (phoneIdx >= 0) parts.getOrElse(phoneIdx) { "" }.trim() else ""
        if (name.isBlank() && phone.isBlank()) return@mapNotNull null
        ImportPreview(
            name = name.ifBlank { "Unknown" },
            phone = phone,
            serviceType = if (serviceIdx >= 0) parts.getOrElse(serviceIdx) { "" }.trim() else "",
            notes = if (notesIdx >= 0) parts.getOrElse(notesIdx) { "" }.trim() else ""
        )
    }
}

private fun splitCSVLine(line: String): List<String> {
    val result = mutableListOf<String>()
    var current = StringBuilder()
    var inQuotes = false
    for (ch in line) {
        when {
            ch == '"' -> inQuotes = !inQuotes
            ch == ',' && !inQuotes -> {
                result.add(current.toString().trim().removeSurrounding("\""))
                current = StringBuilder()
            }
            else -> current.append(ch)
        }
    }
    result.add(current.toString().trim().removeSurrounding("\""))
    return result
}

private fun parseVCard(content: String): List<ImportPreview> {
    val vcards = content.split("END:VCARD").filter { it.contains("BEGIN:VCARD") }
    return vcards.mapNotNull { block ->
        val lines = block.lines()
        var name = ""
        var phone = ""
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("FN:") || trimmed.startsWith("fn:") -> name = trimmed.removePrefix("FN:").removePrefix("fn:").trim()
                trimmed.startsWith("TEL;") || trimmed.startsWith("tel;") -> {
                    val colon = trimmed.lastIndexOf(":")
                    if (colon >= 0) phone = trimmed.substring(colon + 1).trim()
                }
                trimmed.startsWith("TEL:") || trimmed.startsWith("tel:") -> phone = trimmed.removePrefix("TEL:").removePrefix("tel:").trim()
            }
        }
        if (name.isBlank() && phone.isBlank()) return@mapNotNull null
        ImportPreview(name = name.ifBlank { "Unknown" }, phone = phone)
    }
}

private fun tx(key: String, language: AppLanguage): String = when (key) {
    "import_data" -> when (language) {
        AppLanguage.English -> "Import Data"
        AppLanguage.French -> "Importer donnees"
        AppLanguage.Arabic -> "استيراد البيانات"
    }
    "import_title" -> when (language) {
        AppLanguage.English -> "Import Clients"
        AppLanguage.French -> "Importer clients"
        AppLanguage.Arabic -> "استيراد العملاء"
    }
    "import_desc" -> when (language) {
        AppLanguage.English -> "Pick a CSV or VCard file to import clients in bulk."
        AppLanguage.French -> "Choisir fichier CSV ou VCard pour import groupé."
        AppLanguage.Arabic -> "اختر ملف CSV أو VCard لاستيراد العملاء بكميات."
    }
    "import_formats" -> when (language) {
        AppLanguage.English -> "Supported: CSV (Name, Phone, Service, Notes) or VCard (.vcf)"
        AppLanguage.French -> "Supporte: CSV (Nom, Telephone, Service, Notes) ou VCard (.vcf)"
        AppLanguage.Arabic -> "مدعوم: CSV (الاسم، الهاتف، الخدمة، الملاحظات) أو VCard"
    }
    "pick_csv" -> when (language) {
        AppLanguage.English -> "Pick CSV"
        AppLanguage.French -> "Choisir CSV"
        AppLanguage.Arabic -> "اختيار CSV"
    }
    "pick_vcard" -> when (language) {
        AppLanguage.English -> "Pick VCard"
        AppLanguage.French -> "Choisir VCard"
        AppLanguage.Arabic -> "اختيار VCard"
    }
    "found_clients" -> when (language) {
        AppLanguage.English -> "Found {count} clients"
        AppLanguage.French -> "Trouve {count} clients"
        AppLanguage.Arabic -> "تم العثور على {count} عميل"
    }
    "select_all" -> when (language) {
        AppLanguage.English -> "Select all"
        AppLanguage.French -> "Tout selectionner"
        AppLanguage.Arabic -> "تحديد الكل"
    }
    "import_selected" -> when (language) {
        AppLanguage.English -> "Import {count}"
        AppLanguage.French -> "Importer {count}"
        AppLanguage.Arabic -> "استيراد {count}"
    }
    "import_limit_reached" -> when (language) {
        AppLanguage.English -> "Free limit reached. Upgrade to Pro to import more."
        AppLanguage.French -> "Limite gratuite atteinte. Passez Pro pour importer plus."
        AppLanguage.Arabic -> "تم الوصول للحد المجاني. قم بالترقية لاستيراد المزيد."
    }
    else -> key
}
