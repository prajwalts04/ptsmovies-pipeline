package com.pts.suite.ui.screens.files

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pts.suite.data.api.FileItem
import com.pts.suite.data.api.RetrofitClient
import com.pts.suite.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FileEditorDialog(
    fileItem: FileItem,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var fileContent by remember { mutableStateOf("") }
    var initialContent by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    val hasUnsavedChanges = fileContent != initialContent

    // Load initial file content from server
    LaunchedEffect(fileItem.path) {
        isLoading = true
        scope.launch {
            try {
                val service = RetrofitClient.getService(context)
                val token = RetrofitClient.getAuthToken(context) ?: ""
                val serverUrl = RetrofitClient.getServerUrl(context).trimEnd('/')
                val streamUrl = "$serverUrl/api/fs/read?path=${java.net.URLEncoder.encode(fileItem.path, "UTF-8")}"

                val res = service.downloadFileStream(streamUrl)
                if (res.isSuccessful && res.body() != null) {
                    val text = res.body()!!.string()
                    fileContent = text
                    initialContent = text
                } else {
                    fileContent = "# Empty or new file: ${fileItem.name}\n"
                    initialContent = fileContent
                }
            } catch (e: Exception) {
                fileContent = "# Error loading file: ${e.message}\n"
                initialContent = fileContent
            } finally {
                isLoading = false
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (hasUnsavedChanges) showDiscardConfirm = true else onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PitchBlack.copy(alpha = 0.95f))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.5.dp, SketchBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Bar: Filename, Dirty indicator, Save button, Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                        Text(
                            text = fileItem.name,
                            color = Graphite100,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        if (hasUnsavedChanges) {
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = GoldenYellow
                            ) {
                                Text(
                                    text = "MODIFIED",
                                    color = PitchBlack,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Save Button
                        Button(
                            onClick = {
                                isSaving = true
                                scope.launch {
                                    try {
                                        val service = RetrofitClient.getService(context)
                                        val dir = fileItem.path.substringBeforeLast('/', "/Data")
                                        val payload = mapOf(
                                            "dirPath" to dir,
                                            "filePath" to fileItem.path,
                                            "name" to fileItem.name,
                                            "content" to fileContent
                                        )
                                        service.createDirectory(payload) // writes or updates file on server
                                        initialContent = fileContent
                                        Toast.makeText(context, "Saved ${fileItem.name}", Toast.LENGTH_SHORT).show()
                                        onSaved()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            enabled = !isSaving && hasUnsavedChanges,
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = PitchBlack, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SAVE", color = PitchBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Close Button
                        IconButton(
                            onClick = {
                                if (hasUnsavedChanges) showDiscardConfirm = true else onDismiss()
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Graphite300)
                        }
                    }
                }

                // Monospace Editor Area
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EmeraldGreen)
                    }
                } else {
                    OutlinedTextField(
                        value = fileContent,
                        onValueChange = { fileContent = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Graphite100
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = SketchBorder,
                            focusedContainerColor = PitchBlack,
                            unfocusedContainerColor = PitchBlack
                        )
                    )
                }

                // Bottom Status Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Lines: ${fileContent.lines().size} | Chars: ${fileContent.length}",
                        color = Graphite400,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = fileItem.path,
                        color = Graphite400,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }
        }
    }

    // Discard Unsaved Changes Confirmation Dialog
    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text("Discard Unsaved Changes?", color = Graphite100, fontWeight = FontWeight.Bold) },
            text = { Text("You have unsaved edits in ${fileItem.name}. Discard them and close?", color = Graphite300) },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("DISCARD", color = Graphite100, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) {
                    Text("KEEP EDITING", color = Graphite200)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}
