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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pts.suite.data.api.RetrofitClient
import com.pts.suite.data.api.VaultNote
import com.pts.suite.ui.theme.*
import kotlinx.coroutines.launch

/**
 * SecureNoteEditorView provides complete CRUD operations for confidential encrypted text notes,
 * seed phrases, SSH recovery codes, and passwords with 1-tap clipboard copy.
 */
@Composable
fun SecureNoteEditorView(
    notes: List<VaultNote>,
    searchQuery: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var editingNote by remember { mutableStateOf<VaultNote?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<VaultNote?>(null) }

    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header with Add Button and Count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CONFIDENTIAL NOTES (${filteredNotes.size})",
                color = Graphite300,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Note", tint = PitchBlack, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("NEW NOTE", color = PitchBlack, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }

        if (filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, SketchBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Graphite500,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No notes matching \"$searchQuery\"" else "No confidential notes yet",
                        color = Graphite400,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(filteredNotes, key = { it.id }) { note ->
                    NoteCardItem(
                        note = note,
                        onEdit = { editingNote = note },
                        onDelete = { noteToDelete = note },
                        onCopyContent = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Note Content", note.content))
                            Toast.makeText(context, "Copied note content to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        onCopyTitle = {
                            val clipboard = context.getSystemService(Context.CLIOBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Note Title", note.title))
                            Toast.makeText(context, "Copied title", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Create Note Dialog
    if (showCreateDialog) {
        NoteEditDialog(
            initialTitle = "",
            initialContent = "",
            dialogTitle = "Create Secure Note",
            confirmButtonLabel = "SAVE NOTE",
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, content ->
                scope.launch {
                    try {
                        RetrofitClient.getService(context).createVaultNote(
                            mapOf("title" to title, "content" to content)
                        )
                        showCreateDialog = false
                        onRefresh()
                        Toast.makeText(context, "Note created successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error creating note: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Edit Note Dialog
    editingNote?.let { note ->
        NoteEditDialog(
            initialTitle = note.title,
            initialContent = note.content,
            dialogTitle = "Edit Secure Note",
            confirmButtonLabel = "UPDATE",
            onDismiss = { editingNote = null },
            onConfirm = { title, content ->
                scope.launch {
                    try {
                        RetrofitClient.getService(context).updateVaultNote(
                            note.id,
                            mapOf("title" to title, "content" to content)
                        )
                        editingNote = null
                        onRefresh()
                        Toast.makeText(context, "Note updated", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error updating note: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }


    // Delete Confirmation Dialog
    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Note?", color = Graphite100, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${note.title}\"? This action cannot be undone.",
                    color = Graphite300,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                RetrofitClient.getService(context).deleteVaultNote(note.id)
                                noteToDelete = null
                                onRefresh()
                                Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error deleting note: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("DELETE", color = Graphite100, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("CANCEL", color = Graphite300)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}

@Composable
private fun NoteCardItem(
    note: VaultNote,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopyContent: () -> Unit,
    onCopyTitle: () -> Unit
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, SketchBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Note Top Bar: Title + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onCopyTitle),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = note.title,
                        color = Graphite100,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }


                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onCopyContent, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Content",
                            tint = Graphite300,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Graphite300,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = DangerRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }


            // Note Content Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(onClick = onCopyContent)
                    .padding(10.dp)
            ) {
                Text(
                    text = note.content,
                    color = Graphite200,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 17.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Timestamps / Stats Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val wordsCount = note.content.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                Text(
                    text = "${note.content.length} chars • #wordsCount words",
                    color = Graphite400,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                note.updatedAt?.let {
                    Text(
                        text = it.take(10),
                        color = Graphite500,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteEditDialog(
    initialTitle: String,
    initialContent: String,
    dialogTitle: String,
    confirmButtonLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = dialogTitle, color = Graphite100, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        error = null
                    },
                    label = { Text("Note Title") },
                    placeholder = { Text("e.g., PiSSH Private Key / Recovery Seed") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = SketchBorder,
                        focusedTextColor = Graphite100,
                        unfocusedTextColor = Graphite100
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        error = null
                    },
                    label = { Text("Secret Content") },
                    placeholder = { Text("Enter confidential note text, secret tokens, or recovery seeds...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = SketchBorder,
                        focusedTextColor = Graphite100,
                        unfocusedTextColor = Graphite100
                    )
                )

                if (error != null) {
                    Text(text = error!!, color = DangerRed, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        error = "Title cannot be blank"
                    } else {
                        onConfirm(title.trim(), content.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text(text = confirmButtonLabel, color = PitchBlack, fontWeight = FontWeight.Bold)
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
