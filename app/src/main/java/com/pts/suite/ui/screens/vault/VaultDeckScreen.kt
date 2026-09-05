package com.pts.suite.ui.screens.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.pts.suite.data.api.RetrofitClient
import com.pts.suite.data.api.VaultCategory
import com.pts.suite.data.api.VaultDocument
import com.pts.suite.data.api.VaultNote
import com.pts.suite.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultDeckScreen(
    documents: List<VaultDocument>,
    notes: List<VaultNote>,
    categories: List<VaultCategory>,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isVaultUnlocked by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("deck") } // "deck", "grid", "notes"
    var selectedCategoryFilter by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedDocId by remember { mutableStateOf<Int?>(null) }
    var hoveredDocId by remember { mutableStateOf<Int?>(null) }
    var showAddDocDialog by remember { mutableStateOf(false) }

    VaultBiometricGate(
        isUnlocked = isVaultUnlocked,
        onUnlockSuccess = { isVaultUnlocked = true },
        onLockRequested = { isVaultUnlocked = false }
    ) {
        val filteredDocs = remember(documents, selectedCategoryFilter, searchQuery) {
            documents.filter { doc ->
                val matchesCat = selectedCategoryFilter == null || doc.categoryId == selectedCategoryFilter
                val matchesQuery = searchQuery.isBlank() ||
                        doc.title.contains(searchQuery, ignoreCase = true) ||
                        doc.holderName.contains(searchQuery, ignoreCase = true) ||
                        doc.docNumber.contains(searchQuery, ignoreCase = true) ||
                        doc.issuer.contains(searchQuery, ignoreCase = true) ||
                        doc.docType.contains(searchQuery, ignoreCase = true)
                matchesCat && matchesQuery
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PitchBlack)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Tab Switcher + Lock & Refresh Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val tabs = listOf(
                        "deck" to "Deck (${filteredDocs.size})",
                        "grid" to "Grid",
                        "notes" to "Notes (${notes.size})"
                    )
                    tabs.forEach { (tabId, label) ->
                        val isSelected = selectedTab == tabId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) EmeraldGreen else DarkSurface)
                                .border(1.dp, if (isSelected) EmeraldGreen else SketchBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedTab = tabId }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) PitchBlack else Graphite200,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (selectedTab != "notes") {
                        IconButton(onClick = { showAddDocDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Card", tint = EmeraldGreen)
                        }
                    }
                    IconButton(onClick = { isVaultUnlocked = false }) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock Vault", tint = Graphite400)
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = EmeraldGreen)
                    }
                }
            }

            // Search Filter Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search cards, numbers, notes...", fontSize = 12.sp, color = Graphite400) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Graphite400, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Graphite400, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldGreen,
                    unfocusedBorderColor = SketchBorder,
                    focusedTextColor = Graphite100,
                    unfocusedTextColor = Graphite100,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            // Category Chips Row (for Deck / Grid modes)
            if (selectedTab != "notes" && categories.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        val isAllSelected = selectedCategoryFilter == null
                        FilterChip(
                            selected = isAllSelected,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreen,
                                selectedLabelColor = PitchBlack,
                                containerColor = DarkSurface,
                                labelColor = Graphite300
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isAllSelected,
                                borderColor = if (isAllSelected) EmeraldGreen else SketchBorder
                            )
                        )
                    }

                    items(categories) { cat ->
                        val isSelected = selectedCategoryFilter == cat.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = if (isSelected) null else cat.id },
                            label = { Text(cat.name, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreen,
                                selectedLabelColor = PitchBlack,
                                containerColor = DarkSurface,
                                labelColor = Graphite300
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) EmeraldGreen else SketchBorder
                            )
                        )
                    }
                }
            }

            // Main Content Area
            when (selectedTab) {
                "deck" -> {
                    if (filteredDocs.isEmpty()) {
                        EmptyVaultDeckState(searchQuery = searchQuery, onAddDoc = { showAddDocDialog = true })
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy((-130).dp),
                            contentPadding = PaddingValues(top = 10.dp, bottom = 140.dp)
                        ) {
                            itemsIndexed(filteredDocs, key = { _, doc -> doc.id }) { index, doc ->
                                val isFocused = expandedDocId == doc.id
                                val isHovered = hoveredDocId == doc.id && !isFocused

                                // Hover Lift: translateY(-28.dp), scale(1.02f)
                                // Active Focus: translateY(-40.dp), scale(1.03f)
                                val targetOffsetY = when {
                                    isFocused -> (-40).dp
                                    isHovered -> (-28).dp
                                    else -> 0.dp
                                }
                                val targetScale = when {
                                    isFocused -> 1.03f
                                    isHovered -> 1.02f
                                    else -> 1.0f
                                }

                                val animatedOffsetY by animateDpAsState(
                                    targetValue = targetOffsetY,
                                    animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f)
                                )
                                val animatedScale by animateFloatAsState(
                                    targetValue = targetScale,
                                    animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f)
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .zIndex(if (isFocused) 100f else if (isHovered) 50f else index.toFloat())
                                        .offset(y = animatedOffsetY)
                                        .scale(animatedScale)
                                        .clickable {
                                            expandedDocId = if (isFocused) null else doc.id
                                        }
                                ) {
                                    VaultCardTemplateView(
                                        doc = doc,
                                        isExpanded = isFocused,
                                        onCopyField = { label, value ->
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
                                            Toast.makeText(context, "Copied $label: $value", Toast.LENGTH_SHORT).show()
                                        },
                                        onDelete = {
                                            scope.launch {
                                                try {
                                                    RetrofitClient.getService(context).deleteVaultDocument(doc.id)
                                                    onRefresh()
                                                    Toast.makeText(context, "Card deleted", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Error deleting card", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                "grid" -> {
                    if (filteredDocs.isEmpty()) {
                        EmptyVaultDeckState(searchQuery = searchQuery, onAddDoc = { showAddDocDialog = true })
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 120.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredDocs, key = { it.id }) { doc ->
                                VaultCardTemplateView(
                                    doc = doc,
                                    isExpanded = true,
                                    onCopyField = { label, value ->
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
                                        Toast.makeText(context, "Copied: $value", Toast.LENGTH_SHORT).show()
                                    },
                                    onDelete = {
                                        scope.launch {
                                            try {
                                                RetrofitClient.getService(context).deleteVaultDocument(doc.id)
                                                onRefresh()
                                                Toast.makeText(context, "Card deleted", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {}
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                "notes" -> {
                    SecureNoteEditorView(
                        notes = notes,
                        searchQuery = searchQuery,
                        onRefresh = onRefresh
                    )
                }
            }
        }

        // Add Document Form Modal
        if (showAddDocDialog) {
            AddVaultDocumentDialog(
                categories = categories,
                onDismiss = { showAddDocDialog = false },
                onDocumentAdded = {
                    showAddDocDialog = false
                    onRefresh()
                }
            )
        }
    }
}

@Composable
private fun EmptyVaultDeckState(
    searchQuery: String,
    onAddDoc: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, SketchBorder, RoundedCornerShape(16.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Graphite400, modifier = Modifier.size(44.dp))
            Text(
                text = if (searchQuery.isNotEmpty()) "No cards match "$searchQuery"" else "Your Digital Wallet is empty",
                color = Graphite200,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Store Aadhaar, PAN, Passports, Bank Cards, Driving Licences, and Secret Keys with hardware biometric protection.",
                color = Graphite400,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )
            Button(
                onClick = onAddDoc,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = PitchBlack, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ADD FIRST CARD", color = PitchBlack, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddVaultDocumentDialog(
    categories: List<VaultCategory>,
    onDismiss: () -> Unit,
    onDocumentAdded: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var holderName by remember { mutableStateOf("") }
    var docNumber by remember { mutableStateOf("") }
    var issuer by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var extraInfo by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(VaultCardTemplateType.BANK_CARD) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(categories.firstOrNull()?.id) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Document / Card", color = Graphite100, fontWeight = FontWeight.Black, fontSize = 16.sp)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card Template Selector Dropdown / Chips
                item {
                    Text("Card Template Type", fontSize = 11.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(VaultCardTemplateType.values()) { type ->
                            val isSel = selectedTemplate == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) EmeraldGreen else DarkSurface)
                                    .border(1.dp, if (isSel) EmeraldGreen else SketchBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedTemplate = type
                                        if (title.isBlank()) title = type.displayName
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = type.displayName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) PitchBlack else Graphite200
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; errorMsg = null },
                        label = { Text("Title / Card Label") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = holderName,
                        onValueChange = { holderName = it },
                        label = { Text("Holder Name / Insured") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = docNumber,
                        onValueChange = { docNumber = it; errorMsg = null },
                        label = { Text("Number (Card/PAN/Aadhaar/Key)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = issuer,
                        onValueChange = { issuer = it },
                        label = { Text("Issuer / Bank / Authority") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { expiryDate = it },
                            label = { Text("Expiry (MM/YY or Date)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = extraInfo,
                            onValueChange = { extraInfo = it },
                            label = { Text("Extra (CVV/Tag/PNR)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (errorMsg != null) {
                    item {
                        Text(text = errorMsg!!, color = DangerRed, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || docNumber.isBlank()) {
                        errorMsg = "Title and Number are required"
                        return@Button
                    }
                    isSubmitting = true
                    scope.launch {
                        try {
                            val fields = mutableMapOf<String, String>(
                                "title" to title.trim(),
                                "doc_type" to selectedTemplate.key,
                                "holder_name" to holderName.trim(),
                                "doc_number" to docNumber.trim(),
                                "issuer" to issuer.trim(),
                                "expiry_date" to expiryDate.trim(),
                                "extra_info" to extraInfo.trim()
                            )
                            if (selectedCategoryId != null) {
                                fields["category_id"] = selectedCategoryId.toString()
                            }
                            RetrofitClient.getService(context).createVaultDocument(fields)
                            onDocumentAdded()
                            Toast.makeText(context, "Card saved to vault", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            errorMsg = "Error saving card: ${e.message}"
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text(if (isSubmitting) "SAVING..." else "SAVE CARD", color = PitchBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Graphite300)
            }
        },
        containerColor = DarkSurfaceElevated
    )
}
