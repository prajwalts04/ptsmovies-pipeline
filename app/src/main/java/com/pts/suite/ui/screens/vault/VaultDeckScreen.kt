package com.pts.suite.ui.screens.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.pts.suite.ui.components.DockTabItem
import com.pts.suite.ui.components.DynamicBottomDock
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
    var selectedTab by remember { mutableStateOf("wallet") } // "wallet", "notes", "categories"
    var expandedDocId by remember { mutableStateOf<Int?>(null) }
    var viewMode by remember { mutableStateOf("deck") } // "deck" (stacked) vs "grid"

    val vaultTabs = listOf(
        DockTabItem("wallet", "Cards & Wallet", Icons.Default.CreditCard),
        DockTabItem("notes", "Secure Notes", Icons.Default.Lock),
        DockTabItem("categories", "Categories", Icons.Default.Folder)
    )

    Scaffold(
        bottomBar = {
            DynamicBottomDock(
                tabs = vaultTabs,
                selectedTabId = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        containerColor = PitchBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Stacking Deck / Grid Switcher
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedTab) {
                        "wallet" -> "DIGITAL WALLET & PASSES"
                        "notes" -> "CONFIDENTIAL NOTES"
                        else -> "DOCUMENT CATEGORIES"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Graphite100,
                    letterSpacing = 1.sp
                )

                if (selectedTab == "wallet") {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface)
                            .border(1.dp, SketchBorder, RoundedCornerShape(6.dp))
                            .padding(2.dp)
                    ) {
                        IconButton(onClick = { viewMode = "deck" }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.ViewCarousel, contentDescription = "Deck", tint = if (viewMode == "deck") EmeraldGreen else Graphite400, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { viewMode = "grid" }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.GridView, contentDescription = "Grid", tint = if (viewMode == "grid") EmeraldGreen else Graphite400, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Tab 1: Cards & Wallet (Apple/Google Wallet Stacking Mode)
            if (selectedTab == "wallet") {
                if (documents.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No cards or passes in vault yet.", color = Graphite400, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = if (viewMode == "deck") Arrangement.spacedBy((-45).dp) else Arrangement.spacedBy(12.dp)
                    ) {
                        items(documents) { doc ->
                            val isExpanded = expandedDocId == doc.id

                            WalletPassCard(
                                doc = doc,
                                isExpanded = isExpanded,
                                onClick = { expandedDocId = if (isExpanded) null else doc.id },
                                onCopyNumber = { num ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Card Number", num))
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    scope.launch {
                                        RetrofitClient.getService(context).deleteVaultDocument(doc.id)
                                        onRefresh()
                                    }
                                }
                            )
                        }
                    }
                }
            } else if (selectedTab == "notes") {
                // Tab 2: Secure Notes
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notes) { note ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = note.title, color = Graphite100, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = note.content, color = Graphite300, fontSize = 12.sp)
                            Text(text = note.updatedAt ?: "", color = Graphite400, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            } else {
                // Tab 3: Categories
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurface)
                                .border(1.dp, SketchBorder, RoundedCornerShape(6.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = cat.name, color = Graphite100, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WalletPassCard(
    doc: VaultDocument,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onCopyNumber: (String) -> Unit,
    onDelete: () -> Unit
) {
    // Generate realistic card gradient
    val cardGradient = when (doc.docType) {
        "passport", "id_card" -> Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
        "license" -> Brush.linearGradient(listOf(Color(0xFF14532D), Color(0xFF052E16)))
        "finance", "bank_card" -> Brush.linearGradient(listOf(Color(0xFF312E81), Color(0xFF1E1B4B)))
        "medical" -> Brush.linearGradient(listOf(Color(0xFF881337), Color(0xFF4C0519)))
        else -> Brush.linearGradient(listOf(Color(0xFF181822), Color(0xFF0C0C12)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardGradient)
            .border(1.2.dp, SketchBorderActive, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = doc.categoryName ?: doc.docType.uppercase(),
                color = EmeraldGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Text(
                text = doc.issuer.ifEmpty { "PTS VAULT" },
                color = Graphite300,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = doc.title,
            color = Graphite100,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )

        if (doc.docNumber.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = doc.docNumber,
                    color = Graphite100,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                IconButton(onClick = { onCopyNumber(doc.docNumber) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Graphite300, modifier = Modifier.size(15.dp))
                }
            }
        }

        // Expanded Details (Holder name, Expiry, Delete)
        if (isExpanded) {
            Divider(color = Graphite800, thickness = 1.dp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "HOLDER NAME", fontSize = 9.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    Text(text = doc.holderName.ifEmpty { "N/A" }, fontSize = 12.sp, color = Graphite200, fontWeight = FontWeight.SemiBold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "EXPIRY DATE", fontSize = 9.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    Text(text = doc.expiryDate.ifEmpty { "Never" }, fontSize = 12.sp, color = GoldenYellow, fontWeight = FontWeight.SemiBold)
                }
            }

            if (doc.extraInfo.isNotBlank()) {
                Text(text = doc.extraInfo, color = Graphite300, fontSize = 11.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
