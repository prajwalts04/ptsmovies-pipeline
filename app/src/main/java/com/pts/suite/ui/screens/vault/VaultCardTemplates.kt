package com.pts.suite.ui.screens.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pts.suite.data.api.VaultDocument
import com.pts.suite.ui.theme.*

/**
 * 10 Canonical Vault Card Template Types supported by PTS Vault:
 * 1. Aadhaar Card (UIDAI)
 * 2. PAN Card (Income Tax Department)
 * 3. Passport (Republic of India)
 * 4. Driving Licence (State Transport Dept)
 * 5. Voter ID / EPIC Card (Election Commission)
 * 6. Bank Cards (Debit & Credit EMV)
 * 7. Vehicle Registration Certificate (RC)
 * 8. Transit Ticket / Boarding Pass
 * 9. Health & Life Insurance Policy
 * 10. Secret Key / Secure Credential
 */
enum class VaultCardTemplateType(val key: String, val displayName: String) {
    AADHAAR("aadhaar", "Aadhaar Card"),
    PAN("pan", "PAN Card"),
    PASSPORT("passport", "Passport"),
    DRIVING_LICENSE("driving_license", "Driving Licence"),
    VOTER_ID("voter_id", "Voter ID / EPIC"),
    BANK_CARD("bank_card", "Debit / Credit Card"),
    VEHICLE_RC("vehicle_rc", "Vehicle RC"),
    TRANSIT_TICKET("transit_ticket", "Transit / Ticket"),
    HEALTH_INSURANCE("health_insurance", "Health Insurance"),
    SECRET_KEY("secret_key", "Secret Key / API Key");

    companion object {
        fun fromDocType(docType: String?): VaultCardTemplateType {
            val normalized = (docType ?: "").lowercase().trim()
            return when {
                normalized.contains("aadhaar") || normalized.contains("uidai") -> AADHAAR
                normalized.contains("pan") || normalized.contains("tax") -> PAN
                normalized.contains("passport") -> PASSPORT
                normalized.contains("license") || normalized.contains("licence") || normalized.contains("driving") || normalized.contains("dl") -> DRIVING_LICENSE
                normalized.contains("voter") || normalized.contains("epic") || normalized.contains("election") -> VOTER_ID
                normalized.contains("bank") || normalized.contains("credit") || normalized.contains("debit") || normalized.contains("card") -> BANK_CARD
                normalized.contains("rc") || normalized.contains("vehicle") || normalized.contains("registration") -> VEHICLE_RC
                normalized.contains("ticket") || normalized.contains("transit") || normalized.contains("boarding") || normalized.contains("flight") || normalized.contains("train") -> TRANSIT_TICKET
                normalized.contains("insurance") || normalized.contains("medical") || normalized.contains("health") || normalized.contains("policy") -> HEALTH_INSURANCE
                normalized.contains("secret") || normalized.contains("key") || normalized.contains("token") || normalized.contains("crypto") || normalized.contains("seed") -> SECRET_KEY
                else -> BANK_CARD
            }
        }
    }
}

/**
 * Universal Vault Card Template View rendering 1 of the 10 rich card templates
 * with interactive touch states, masked toggle, and 1-tap copy.
 */
@Composable
fun VaultCardTemplateView(
    doc: VaultDocument,
    isExpanded: Boolean,
    onCopyField: (fieldName: String, value: String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val templateType = remember(doc.docType) { VaultCardTemplateType.fromDocType(doc.docType) }
    var isMasked by remember { mutableStateOf(true) }

    when (templateType) {
        VaultCardTemplateType.AADHAAR -> AadhaarCardView(doc, isExpanded, isMasked, onToggleMask = { isMasked = !isMasked }, onCopyField, onDelete, modifier)
        VaultCardTemplateType.PAN -> PanCardView(doc, isExpanded, isMasked, onToggleMask = { isMasked = !isMasked }, onCopyField, onDelete, modifier)
        VaultCardTemplateType.PASSPORT -> PassportCardView(doc, isExpanded, onCopyField, onDelete, modifier)
        VaultCardTemplateType.DRIVING_LICENSE -> DrivingLicenseCardView(doc, isExpanded, onCopyField, onDelete, modifier)
        VaultCardTemplateType.VOTER_ID -> VoterIdCardView(doc, isExpanded, onCopyField, onDelete, modifier)
        VaultCardTemplateType.BANK_CARD -> BankCardTemplateView(doc, isExpanded, isMasked, onToggleMask = { isMasked = !isMasked }, onCopyField, onDelete, modifier)
        VaultCardTemplateType.VEHICLE_RC -> VehicleRcCardView(doc, isExpanded, onCopyField, onDelete, modifier)
        VaultCardTemplateType.TRANSIT_TICKET -> TransitTicketCardView(doc, isExpanded, onCopyField, onDelete, modifier)
        VaultCardTemplateType.HEALTH_INSURANCE -> HealthInsuranceCardView(doc, isExpanded, onCopyField, onDelete, modifier)
        VaultCardTemplateType.SECRET_KEY -> SecretKeyCardView(doc, isExpanded, isMasked, onToggleMask = { isMasked = !isMasked }, onCopyField, onDelete, modifier)
    }
}

// ==========================================
// 1. AADHAAR CARD TEMPLATE
// ==========================================
@Composable
fun AadhaarCardView(
    doc: VaultDocument,
    isExpanded: Boolean,
    isMasked: Boolean,
    onToggleMask: () -> Unit,
    onCopyField: (String, String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedNumber = remember(doc.docNumber, isMasked) {
        val clean = doc.docNumber.replace(" ", "")
        if (isMasked && clean.length >= 8) {
            "•••• •••• " + clean.takeLast(4)
        } else if (clean.length == 12) {
            clean.chunked(4).joinToString(" ")
        } else {
            doc.docNumber
        }
    }

    CardContainer(
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF2A1B0A), Color(0xFF13110E), Color(0xFF0F1E13))
        ),
        borderColor = Color(0xFFD97706).copy(alpha = 0.4f),
        isExpanded = isExpanded,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with Tricolor Stripe Accent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF9933))
                    )
                    Text(
                        text = "UNIQUE IDENTIFICATION AUTHORITY OF INDIA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFE0B2),
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "AADHAAR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF9933)
                )
            }

            // Document Number Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onCopyField("Aadhaar Number", doc.docNumber) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedNumber.ifEmpty { "•••• •••• ••••" },
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onToggleMask, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Mask",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(onClick = { onCopyField("Aadhaar Number", doc.docNumber) }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Cardholder Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "NAME", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    Text(
                        text = doc.holderName.ifEmpty { doc.title }.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Graphite100
                    )
                }

                if (doc.extraInfo.isNotBlank()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "DETAILS", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                        Text(
                            text = doc.extraInfo,
                            fontSize = 11.sp,
                            color = Color(0xFF86EFAC),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. PAN CARD TEMPLATE
// ==========================================
@Composable
fun PanCardView(
    doc: VaultDocument,
    isExpanded: Boolean,
    isMasked: Boolean,
    onToggleMask: () -> Unit,
    onCopyField: (String, String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedPan = remember(doc.docNumber, isMasked) {
        val clean = doc.docNumber.trim().uppercase()
        if (isMasked && clean.length >= 5) {
            clean.take(2) + "•••" + clean.takeLast(5)
        } else {
            clean
        }
    }

    CardContainer(
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0B192C))
        ),
        borderColor = Color(0xFF38BDF8).copy(alpha = 0.4f),
        isExpanded = isExpanded,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "INCOME TAX DEPARTMENT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF7DD3FC),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "GOVT. OF INDIA / PERMANENT ACCOUNT CARD",
                        fontSize = 7.sp,
                        color = Graphite400
                    )
                }

                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
            }

            // PAN Number
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onCopyField("PAN Number", doc.docNumber) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedPan.ifEmpty { "••••••••••" },
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFDE047),
                    letterSpacing = 3.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onToggleMask, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Mask",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(onClick = { onCopyField("PAN Number", doc.docNumber) }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "CARDHOLDER NAME", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    Text(
                        text = doc.holderName.ifEmpty { doc.title }.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                if (doc.expiryDate.isNotBlank()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "DOB / INCORP", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                        Text(
                            text = doc.expiryDate,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. PASSPORT TEMPLATE
// ==========================================
@Composable
fun PassportCardView(
    doc: VaultDocument,
    isExpanded: Boolean,
    onCopyField: (String, String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    CardContainer(
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF031B33), Color(0xFF0A2E50), Color(0xFF021224))
        ),
        borderColor = Color(0xFFF59E0B).copy(alpha = 0.5f),
        isExpanded = isExpanded,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Flight,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = (doc.issuer.ifEmpty { "REPUBLIC OF INDIA" }).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFDE68A),
                            letterSpacing = 1.sp
                        )
                        Text(text = "PASSPORT / PASSEPORT", fontSize = 8.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }

                Text(
                    text = "IND",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFF59E0B)
                )
            }

            // Passport Number
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onCopyField("Passport Number", doc.docNumber) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "PASSPORT NO.", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    Text(
                        text = doc.docNumber.ifEmpty { "Z•••••••" },
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "GIVEN NAMES / SURNAME", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    Text(
                        text = doc.holderName.ifEmpty { doc.title }.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                if (doc.expiryDate.isNotBlank()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "EXPIRY DATE", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                        Text(
                            text = doc.expiryDate,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFDE68A)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. DRIVING LICENCE TEMPLATE
// ==========================================
@Composable
fun DrivingLicenseCardView(
    doc: VaultDocument,
    isExpanded: Boolean,
    onCopyField: (String, String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    CardContainer(
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF291804), Color(0xFF3B2006), Color(0xFF1E1003))
        ),
        borderColor = Color(0xFFD97706).copy(alpha = 0.5f),
        isExpanded = isExpanded,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = (doc.issuer.ifEmpty { "UNION OF INDIA DRIVING LICENCE" }).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFDE68A)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFB45309))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "DL", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }

            // DL Number
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onCopyField("Driving Licence Number", doc.docNumber) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = doc.docNumber.ifEmpty { "DL-••••••••••••" },
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "HOLDER", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    Text(
                        text = doc.holderName.ifEmpty { doc.title }.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                if (doc.expiryDate.isNotBlank()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "VALID TILL", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                        Text(
                            text = doc.expiryDate,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFBBF24)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. VOTER ID / EPIC TEMPLATE
// ==========================================
@Composable
fun VoterIdCardView(
    doc: VaultDocument,
    isExpanded: Boolean,
    onCopyField: (String, String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    CardContainer(
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
        ),
        borderColor = Color(0xFF4ADE80).copy(alpha = 0.4f),
        isExpanded = isExpanded,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.HowToVote,
                        contentDescription = null,
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ELECTION COMMISSION OF INDIA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF86EFAC),
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "EPIC",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4ADE80)
                )
            }

            // EPIC Number
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onCopyField("EPIC Number", doc.docNumber) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = doc.docNumber.ifEmpty { "ABC•••••••" },
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "ELECTOR'S NAME", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    Text(
                        text = doc.holderName.ifEmpty { doc.title }.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                if (doc.extraInfo.isNotBlank()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "CONSTITUENCY", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                        Text(
                            text = doc.extraInfo,
                            fontSize = 10.sp,
                            color = Color(0xFF86EFAC),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. BANK CARDS (DEBIT / CREDIT) TEMPLATE
// ==========================================
@Composable
fun BankCardTemplateView(
    doc: VaultDocument,
    isExpanded: Boolean,
    isMasked: Boolean,
    onToggleMask: () -> Unit,
    onCopyField: (String, String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedCardNumber = remember(doc.docNumber, isMasked) {
        val clean = doc.docNumber.replace(" ", "")
        if (isMasked && clean.length >= 12) {
            clean.take(4) + " •••• •••• " + clean.takeLast(4)
        } else if (clean.length == 16) {
            clean.chunked(4).joinToString(" ")
        } else {
            doc.docNumber
        }
    }

    CardContainer(
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A), Color(0xFF172554))
        ),
        borderColor = Color(0xFF60A5FA).copy(alpha = 0.4f),
        isExpanded = isExpanded,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (doc.issuer.ifEmpty { doc.categoryName ?: "PLATINUM DEBIT" }).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                // Gold EMV Microchip
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFD97706))
                        .border(0.5.dp, Color(0xFFFDE68A), RoundedCornerShape(4.dp))
                )
            }

            // 16-Digit Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onCopyField("Card Number", doc.docNumber) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedCardNumber.ifEmpty { "•••• •••• •••• ••••" },
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onToggleMask, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Mask",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(onClick = { onCopyField("Card Number", doc.docNumber) }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

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

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Bottom) {
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
                    if (doc.extraInfo.isNotBlank()) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "CVV", fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isMasked) "•••" else doc.extraInfo.replace("CVV:", "").trim(),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE047)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. VEHICLE REGISTRATION CERTIFICATE (RC)
// ==========================================
@Composable
fun VehicleRcCardView(
    doc: VaultDocument,
    isExpanded: Boolean,
    onCopyField: (String, String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    CardContainer(
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF064E3B), Color(0xFF022C22), Color(0xFF0F172A))
        ),
        borderColor = Color(0xFF34D399).copy(alpha = 0.4f),
        isExpanded = isExpanded,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "CERTIFICATE OF REGISTRATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFA7F3D0),
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = (doc.issuer.ifEmpty { "RTO" }).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399)
                )
            }

            // Reg Number
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onCopyField("Registration Number", doc.docNumber) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = doc.docNumber.ifEmpty { "KA-01-AB-1234" },
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF6EE7B7),
                    letterSpacing = 2.sp
                )
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "REGISTERED OWNER", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    Text(
                        text = doc.holderName.ifEmpty { doc.title }.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                if (doc.extraInfo.isNotBlank()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "VEHICLE MODEL", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                        Text(
                            text = doc.extraInfo,
                            fontSize = 10.sp,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. TRANSIT TICKET / BOARDING PASS TEMPLATE
// ==========================================
@Composable
fun TransitTicketCardView(
    doc: VaultDocument,
    isExpanded: Boolean,
    onCopyField: (String, String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    CardContainer(
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF431407), Color(0xFF7C2D12), Color(0xFF290E05))
        ),
        borderColor = Color(0xFFFB923C).copy(alpha = 0.5f),
        isExpanded = isExpanded,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = Color(0xFFFB923C),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = (doc.issuer.ifEmpty { "TRANSIT BOARDING PASS" }).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFED7AA)
                    )
                }

                Text(
                    text = "CONFIRMED",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4ADE80)
                )
            }

            // PNR / Seat / Ticket No
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onCopyField("Ticket PNR / No", doc.docNumber) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "PNR / BOOKING REF", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    Text(
                        text = doc.docNumber.ifEmpty { "PNR: ••••••••" },
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFDBA74),
                        letterSpacing = 2.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "PASSENGER", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    Text(
                        text = doc.holderName.ifEmpty { doc.title }.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                if (doc.expiryDate.isNotBlank()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "TRAVEL DATE", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                        Text(
                            text = doc.expiryDate,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFED7AA)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. HEALTH & LIFE INSURANCE TEMPLATE
// ==========================================
@Composable
fun HealthInsuranceCardView(
    doc: VaultDocument,
    isExpanded: Boolean,
    onCopyField: (String, String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    CardContainer(
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF831843), Color(0xFF500724), Color(0xFF1F040E))
        ),
        borderColor = Color(0xFFF472B6).copy(alpha = 0.4f),
        isExpanded = isExpanded,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = Color(0xFFF472B6),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = (doc.issuer.ifEmpty { "HEALTH INSURANCE POLICY" }).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFCE7F3)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFBE185D))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "CASHLESS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Policy Number
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onCopyField("Policy Number", doc.docNumber) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "POLICY NUMBER", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    Text(
                        text = doc.docNumber.ifEmpty { "POL-••••••••" },
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "INSURED PERSON", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    Text(
                        text = doc.holderName.ifEmpty { doc.title }.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                if (doc.expiryDate.isNotBlank()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "VALID TILL", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                        Text(
                            text = doc.expiryDate,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFF9A8D4)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 10. SECRET KEY / SECURE CREDENTIAL TEMPLATE
// ==========================================
@Composable
fun SecretKeyCardView(
    doc: VaultDocument,
    isExpanded: Boolean,
    isMasked: Boolean,
    onToggleMask: () -> Unit,
    onCopyField: (String, String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedSecret = remember(doc.docNumber, isMasked) {
        if (isMasked) "••••••••••••••••••••" else doc.docNumber
    }

    CardContainer(
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF18181B), Color(0xFF09090B), Color(0xFF052E16))
        ),
        borderColor = EmeraldGreen.copy(alpha = 0.5f),
        isExpanded = isExpanded,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = (doc.issuer.ifEmpty { "ENCRYPTED SECRET KEY" }).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = EmeraldGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "AES-256",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Graphite400
                )
            }

            // Secret Value Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { onCopyField("Secret Key", doc.docNumber) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedSecret.ifEmpty { "••••••••••••••••" },
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onToggleMask, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Mask",
                            tint = Graphite300,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(onClick = { onCopyField("Secret Key", doc.docNumber) }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Graphite300,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "LABEL / SERVICE", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                    Text(
                        text = doc.holderName.ifEmpty { doc.title },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Graphite100
                    )
                }
                if (doc.extraInfo.isNotBlank()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "TAG", fontSize = 8.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                        Text(
                            text = doc.extraInfo,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Graphite300
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// REUSABLE CARD CONTAINER
// ==========================================
@Composable
private fun CardContainer(
    gradient: Brush,
    borderColor: Color,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isExpanded) 190.dp else 160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        content = content
    )
}
