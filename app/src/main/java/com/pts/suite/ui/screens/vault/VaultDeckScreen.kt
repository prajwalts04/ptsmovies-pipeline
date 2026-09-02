package com.pts.suite.ui.screens.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pts.suite.data.api.RetrofitClient
import com.pts.suite.data.api.VaultCategory
import com.pts.suite.data.api.VaultDocument
import com.pts.suite.data.api.VaultNote
import com.pts.suite.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun VaultDeckScreen(
    documents: List<VaultDocument>,
    notes: List<VaultNote>,
    categories: List<VaultCategory>,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf("deck") } // "deck", "grid", "notes"
    var expandedDocId by remember { mutableStateOf<Int?>(null) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Sub-Navigation Tabs: Wallet Deck, Grid View, Secure Notes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val tabs = listOf(
                    "deck" to "Deck View",
                    "grid" to "Grid (${documents.size})",
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

            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = EmeraldGreen)
            }
        }

        when (selectedTab) {
            "deck" -> {
                // Apple / Google Wallet Interactive Stacked Cards Deck
                if (documents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .border(1.dp, SketchBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Graphite400, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No digital wallet cards added yet", color = Graphite300, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy((-140).dp),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
                    ) {
                        itemsIndexed(documents, key = { _, doc -> doc.id }) { index, doc ->
                            val isExpanded = expandedDocId == doc.id
                            val elevationOffset by animateDpAsState(
                                targetValue = if (isExpanded) 12.dp else 0.dp,
                                animationSpec = spring()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = if (isExpanded) (-20).dp else 0.dp)
                                    .padding(vertical = elevationOffset)
                                    .clickable {
                                        expandedDocId = if (isExpanded) null else doc.id
                                    }
                            ) {
                                DigitalWalletCardView(
                                    doc = doc,
                                    isExpanded = isExpanded,
                                    onCopyNumber = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Card Number", doc.docNumber))
                                        Toast.makeText(context, "Copied: ${doc.docNumber}", Toast.LENGTH_SHORT).show()
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
            }

            "grid" -> {
                // Responsive Grid View of all passes
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(documents) { doc ->
                        DigitalWalletCardView(
                            doc = doc,
                            isExpanded = true,
                            onCopyNumber = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Card Number", doc.docNumber))
                                Toast.makeText(context, "Copied: ${doc.docNumber}", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                scope.launch {
                                    try {
                                        RetrofitClient.getService(context).deleteVaultDocument(doc.id)
                                        onRefresh()
                                    } catch (e: Exception) {}
                                }
                            }
                        )
                    }
                }
            }

            "notes" -> {
                // Secure Notes Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "CONFIDENTIAL NOTES", color = Graphite300, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Button(
                        onClick = { showAddNoteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("+ ADD NOTE", color = PitchBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(1.dp, SketchBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No confidential notes stored", color = Graphite400, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(notes) { note ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = note.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Graphite100)
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    RetrofitClient.getService(context).deleteVaultNote(note.id)
                                                    onRefresh()
                                                } catch (e: Exception) {}
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(text = note.content, fontSize = 12.sp, color = Graphite300)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Note Modal Dialog
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("New Confidential Note", color = Graphite100) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = SketchBorder,
                            focusedTextColor = Graphite100
                        )
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Content") },
                        modifier = Modifier.height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = SketchBorder,
                            focusedTextColor = Graphite100
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank()) {
                            scope.launch {
                                try {
                                    RetrofitClient.getService(context).createVaultNote(
                                        mapOf("title" to noteTitle, "content" to noteContent)
                                    )
                                    showAddNoteDialog = false
                                    noteTitle = ""
                                    noteContent = ""
                                    onRefresh()
                                    Toast.makeText(context, "Note saved", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {}
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("SAVE", color = PitchBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("CANCEL", color = Graphite300)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}

@Composable
fun DigitalWalletCardView(
    doc: VaultDocument,
    isExpanded: Boolean,
    onCopyNumber: () -> Unit,
    onDelete: () -> Unit
) {
    val gradientColors = when (doc.docType.lowercase()) {
        "bank_card", "credit" -> listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
        "id_card", "passport" -> listOf(Color(0xFF14532D), Color(0xFF022C22))
        "driving_license" -> listOf(Color(0xFF78350F), Color(0xFF451A03))
        "medical" -> listOf(Color(0xFF831843), Color(0xFF500724))
        else -> listOf(Color(0xFF27272A), Color(0xFF09090B))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isExpanded) 200.dp else 160.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(gradientColors))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Card Issuer & Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (doc.issuer.ifEmpty { doc.categoryName ?: "DIGITAL PASS" }).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )

                // Gold microchip badge
                Box(
                    modifier = Modifier
                        .size(width = 30.dp, height = 22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFD97706))
                        .border(0.5.dp, Color(0xFFFDE68A), RoundedCornerShape(4.dp))
                )
            }

            // Card Document Number with 1-Tap Copy
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onCopyNumber)
            ) {
                Text(
                    text = doc.docNumber.ifEmpty { "•••• •••• •••• ••••" },
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Number",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Cardholder Name & Expiry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "CARDHOLDER", fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    Text(
                        text = doc.holderName.ifEmpty { doc.title }.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (doc.expiryDate.isNotBlank()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "EXPIRES", fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Text(
                            text = doc.expiryDate,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
