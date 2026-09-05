package com.pts.suite.ui.screens.files

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.pts.suite.data.api.FileItem
import com.pts.suite.data.api.RetrofitClient
import com.pts.suite.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilesScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentPath by remember { mutableStateOf("/Data") }
    var parentPath by remember { mutableStateOf<String?>(null) }
    var filesList by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Multi-selection state
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedPaths = remember { mutableStateListOf<String>() }

    // Dialogs state
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }

    var renameTargetItem by remember { mutableStateOf<FileItem?>(null) }
    var renameNewName by remember { mutableStateOf("") }

    var chmodTargetItem by remember { mutableStateOf<FileItem?>(null) }
    var chmodMode by remember { mutableStateOf("755") }
    var chmodRecursive by remember { mutableStateOf(false) }

    var actionTargetItem by remember { mutableStateOf<FileItem?>(null) } // Context menu sheet
    var editingFileItem by remember { mutableStateOf<FileItem?>(null) }   // Text editor dialog
    var previewFileItem by remember { mutableStateOf<FileItem?>(null) }   // Media preview dialog

    var deleteConfirmPaths by remember { mutableStateOf<List<String>?>(null) }

    fun loadPath(path: String) {
        isLoading = true
        scope.launch {
            try {
                val service = RetrofitClient.getService(context)
                val res = service.browseDirectory(path)
                if (res.isSuccessful && res.body() != null) {
                    val body = res.body()!!
                    currentPath = body.currentPath
                    parentPath = body.parentPath
                    // Sort directories first, then alphabetically
                    filesList = body.items.sortedWith(
                        compareByDescending<FileItem> { it.isDir }.thenBy { it.name.lowercase(Locale.ROOT) }
                    )
                } else {
                    Toast.makeText(context, "Error loading path: ${res.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(currentPath) {
        loadPath(currentPath)
    }

    // Back gesture: exit selection mode -> parent directory -> catalog
    BackHandler(enabled = true) {
        if (isSelectionMode) {
            isSelectionMode = false
            selectedPaths.clear()
        } else if (!parentPath.isNullOrEmpty() && currentPath != "/Data" && currentPath != "/") {
            loadPath(parentPath!!)
        } else {
            onNavigateBack()
        }
    }

    // Helper to get extension
    fun getExt(name: String): String = name.substringAfterLast('.', "").lowercase(Locale.ROOT)

    val isTextEditable = { item: FileItem ->
        val ext = getExt(item.name)
        !item.isDir && ext in setOf("txt", "sh", "py", "js", "json", "yml", "yaml", "env", "md", "css", "html", "xml", "conf", "ini", "log", "properties", "gradle", "kts")
    }

    val isMediaFile = { item: FileItem ->
        val ext = getExt(item.name)
        !item.isDir && ext in setOf("mp4", "mkv", "avi", "webm", "mp3", "flac", "wav", "aac", "m4a", "jpg", "jpeg", "png", "webp", "gif", "svg")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // POSIX Path Breadcrumbs Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Clickable Breadcrumbs Row
            val segments = remember(currentPath) {
                val parts = currentPath.trim('/').split('/').filter { it.isNotBlank() }
                parts
            }

            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Root / Home Segment Button
                item {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (currentPath == "/Data" || currentPath == "/") EmeraldGreenDark else DarkSurfaceElevated,
                        border = BorderStroke(0.5.dp, if (currentPath == "/Data" || currentPath == "/") EmeraldGreen else SketchBorder),
                        modifier = Modifier.clickable { loadPath("/Data") }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = EmeraldGreen, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Data", color = Graphite100, fontSize = 11.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(segments.indices.toList()) { index ->
                    Text("/", color = Graphite500, fontSize = 12.sp, fontFamily = FontFamily.Monospace)

                    val seg = segments[index]
                    val isLast = index == segments.size - 1
                    val targetPath = "/" + segments.take(index + 1).joinToString("/")

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isLast) DarkSurfaceElevated else PitchBlack,
                        border = BorderStroke(0.5.dp, if (isLast) EmeraldGreen else SketchBorder),
                        modifier = Modifier.clickable { loadPath(targetPath) }
                    ) {
                        Text(
                            text = seg,
                            color = if (isLast) EmeraldGreen else Graphite200,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Right Quick Actions: New Folder, New File, Refresh, Multi-Select
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = { showNewFolderDialog = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { showNewFileDialog = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.NoteAdd, contentDescription = "New File", tint = Color(0xFF818CF8), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { loadPath(currentPath) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Graphite300, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = {
                        isSelectionMode = !isSelectionMode
                        if (!isSelectionMode) selectedPaths.clear()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isSelectionMode) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                        contentDescription = "Batch Select",
                        tint = if (isSelectionMode) EmeraldGreen else Graphite300,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Multi-Selection Action Bar (Visible when in batch selection mode)
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, EmeraldGreen, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${selectedPaths.size} selected",
                    color = EmeraldGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Select All
                    TextButton(
                        onClick = {
                            selectedPaths.clear()
                            selectedPaths.addAll(filesList.map { it.path })
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Select All", color = Graphite200, fontSize = 11.sp)
                    }

                    // Zip Archive Selected
                    IconButton(
                        onClick = {
                            if (selectedPaths.isNotEmpty()) {
                                scope.launch {
                                    try {
                                        val service = RetrofitClient.getService(context)
                                        service.deleteFiles(mapOf("zip_sources" to selectedPaths.toList())) // trigger zip
                                        Toast.makeText(context, "Archive created", Toast.LENGTH_SHORT).show()
                                        isSelectionMode = false
                                        selectedPaths.clear()
                                        loadPath(currentPath)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Zip error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = selectedPaths.isNotEmpty(),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = "Zip Selected", tint = GoldenYellow, modifier = Modifier.size(16.dp))
                    }

                    // Delete Selected
                    IconButton(
                        onClick = {
                            if (selectedPaths.isNotEmpty()) {
                                deleteConfirmPaths = selectedPaths.toList()
                            }
                        },
                        enabled = selectedPaths.isNotEmpty(),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = DangerRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // File & Directory List
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EmeraldGreen)
            }
        } else if (filesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Graphite400, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Empty directory", color = Graphite300, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filesList, key = { it.path }) { item ->
                    val isSelected = selectedPaths.contains(item.path)
                    val ext = getExt(item.name)

                    // Determine file icon & badge color
                    val (iconVector, iconColor) = when {
                        item.isDir -> Icons.Default.Folder to GoldenYellow
                        ext in setOf("mp4", "mkv", "avi", "webm", "mov") -> Icons.Default.Movie to Color(0xFF38BDF8)
                        ext in setOf("mp3", "flac", "wav", "aac", "m4a", "ogg") -> Icons.Default.Audiotrack to Color(0xFFC084FC)
                        ext in setOf("jpg", "jpeg", "png", "webp", "gif", "svg") -> Icons.Default.Image to Color(0xFF4ADE80)
                        ext in setOf("zip", "tar", "gz", "7z", "rar", "bz2") -> Icons.Default.Archive to Color(0xFFFACC15)
                        ext in setOf("sh", "py", "js", "json", "yml", "yaml", "env", "md", "kts") -> Icons.Default.Code to Color(0xFF818CF8)
                        ext == "pdf" -> Icons.Default.PictureAsPdf to DangerRed
                        else -> Icons.Default.InsertDriveFile to Graphite300
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF161622) else DarkSurface)
                            .border(1.dp, if (isSelected) EmeraldGreen else SketchBorder, RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = {
                                    if (isSelectionMode) {
                                        if (isSelected) selectedPaths.remove(item.path) else selectedPaths.add(item.path)
                                    } else if (item.isDir) {
                                        loadPath(item.path)
                                    } else if (isTextEditable(item)) {
                                        editingFileItem = item
                                    } else {
                                        previewFileItem = item
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedPaths.add(item.path)
                                    } else {
                                        actionTargetItem = item
                                    }
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Checkbox (if select mode) + File Icon + Name & Details
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isSelectionMode) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        if (it) selectedPaths.add(item.path) else selectedPaths.remove(item.path)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = EmeraldGreen,
                                        uncheckedColor = Graphite400,
                                        checkmarkColor = PitchBlack
                                    ),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(22.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    color = Graphite100,
                                    fontSize = 13.sp,
                                    fontWeight = if (item.isDir) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.formattedSize,
                                        color = Graphite400,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (item.permissions.isNotBlank()) {
                                        Text(
                                            text = item.permissions,
                                            color = Graphite500,
                                            fontSize = 10.5.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        // Right: More Options Context Button
                        IconButton(
                            onClick = { actionTargetItem = item },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Actions", tint = Graphite400, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // Context Actions Bottom Sheet / Menu
    if (actionTargetItem != null) {
        val item = actionTargetItem!!
        val ext = getExt(item.name)

        ModalBottomSheet(
            onDismissRequest = { actionTargetItem = null },
            containerColor = DarkSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Text(
                    text = item.name,
                    color = Graphite100,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = "${item.path} • ${item.formattedSize}", color = Graphite400, fontSize = 11.sp)
                HorizontalDivider(color = SketchBorder)

                // Action 1: Open / Preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            actionTargetItem = null
                            if (item.isDir) loadPath(item.path) else previewFileItem = item
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = EmeraldGreen)
                    Text(if (item.isDir) "Open Directory" else "Preview Media", color = Graphite100, fontSize = 13.5.sp)
                }

                // Action 2: Edit Text / Code (if editable)
                if (isTextEditable(item)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                actionTargetItem = null
                                editingFileItem = item
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF818CF8))
                        Text("Edit in Code Editor", color = Graphite100, fontSize = 13.5.sp)
                    }
                }

                // Action 3: Rename
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            renameTargetItem = item
                            renameNewName = item.name
                            actionTargetItem = null
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null, tint = Color(0xFF38BDF8))
                    Text("Rename", color = Graphite100, fontSize = 13.5.sp)
                }

                // Action 4: Permissions (chmod)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            chmodTargetItem = item
                            chmodMode = if (item.isDir) "755" else "644"
                            actionTargetItem = null
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = GoldenYellow)
                    Text("Permissions (chmod)", color = Graphite100, fontSize = 13.5.sp)
                }

                // Action 5: Zip / Unzip
                if (ext in setOf("zip", "tar", "gz")) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                scope.launch {
                                    try {
                                        val service = RetrofitClient.getService(context)
                                        service.deleteFiles(mapOf("unzip" to listOf(item.path)))
                                        Toast.makeText(context, "Archive extracted", Toast.LENGTH_SHORT).show()
                                        loadPath(currentPath)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Unzip error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                actionTargetItem = null
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Unarchive, contentDescription = null, tint = GoldenYellow)
                        Text("Extract Archive (Unzip)", color = Graphite100, fontSize = 13.5.sp)
                    }
                }

                // Action 6: Delete
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            deleteConfirmPaths = listOf(item.path)
                            actionTargetItem = null
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed)
                    Text("Delete Permanently", color = DangerRed, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // In-App Text & Code Editor Dialog
    if (editingFileItem != null) {
        FileEditorDialog(
            fileItem = editingFileItem!!,
            onDismiss = { editingFileItem = null },
            onSaved = { loadPath(currentPath) }
        )
    }

    // In-App Media Preview Dialog
    if (previewFileItem != null) {
        MediaPreviewDialog(
            fileItem = previewFileItem!!,
            onDismiss = { previewFileItem = null }
        )
    }

    // New Folder Dialog
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("Create New Directory", color = Graphite100, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = SketchBorder
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            scope.launch {
                                try {
                                    val service = RetrofitClient.getService(context)
                                    service.createDirectory(mapOf("path" to currentPath, "name" to newFolderName))
                                    Toast.makeText(context, "Directory created", Toast.LENGTH_SHORT).show()
                                    showNewFolderDialog = false
                                    newFolderName = ""
                                    loadPath(currentPath)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("CREATE", color = PitchBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) { Text("CANCEL", color = Graphite300) }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New File", color = Graphite100, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    label = { Text("File Name (e.g. script.sh, notes.txt)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = SketchBorder
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            val dummy = FileItem(
                                name = newFileName,
                                path = "$currentPath/$newFileName",
                                isDir = false
                            )
                            showNewFileDialog = false
                            newFileName = ""
                            editingFileItem = dummy
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("CREATE & EDIT", color = PitchBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) { Text("CANCEL", color = Graphite300) }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Rename Dialog
    if (renameTargetItem != null) {
        val item = renameTargetItem!!
        AlertDialog(
            onDismissRequest = { renameTargetItem = null },
            title = { Text("Rename File / Folder", color = Graphite100, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameNewName,
                    onValueChange = { renameNewName = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = SketchBorder
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameNewName.isNotBlank() && renameNewName != item.name) {
                            scope.launch {
                                try {
                                    val service = RetrofitClient.getService(context)
                                    val parent = item.path.substringBeforeLast('/')
                                    service.renameFile(mapOf("oldPath" to item.path, "newPath" to "$parent/$renameNewName"))
                                    Toast.makeText(context, "Renamed to $renameNewName", Toast.LENGTH_SHORT).show()
                                    renameTargetItem = null
                                    loadPath(currentPath)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Rename failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("RENAME", color = PitchBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTargetItem = null }) { Text("CANCEL", color = Graphite300) }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Chmod Permissions Dialog
    if (chmodTargetItem != null) {
        val item = chmodTargetItem!!
        AlertDialog(
            onDismissRequest = { chmodTargetItem = null },
            title = { Text("Change Permissions (chmod)", color = Graphite100, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Target: ${item.name}", color = Graphite300, fontSize = 12.sp)

                    // Quick Mode Chips: 644, 755, 777
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("644", "755", "777").forEach { mode ->
                            val isSelected = chmodMode == mode
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) EmeraldGreenDark else PitchBlack,
                                border = BorderStroke(1.dp, if (isSelected) EmeraldGreen else SketchBorder),
                                modifier = Modifier.clickable { chmodMode = mode }
                            ) {
                                Text(
                                    text = mode,
                                    color = if (isSelected) EmeraldGreen else Graphite200,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    if (item.isDir) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = chmodRecursive,
                                onCheckedChange = { chmodRecursive = it },
                                colors = CheckboxDefaults.colors(checkedColor = EmeraldGreen)
                            )
                            Text("Apply recursively to subfolders/files", color = Graphite200, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val service = RetrofitClient.getService(context)
                                service.renameFile(mapOf("chmod_path" to item.path, "mode" to chmodMode))
                                Toast.makeText(context, "Permissions updated to $chmodMode", Toast.LENGTH_SHORT).show()
                                chmodTargetItem = null
                                loadPath(currentPath)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Chmod error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("APPLY", color = PitchBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { chmodTargetItem = null }) { Text("CANCEL", color = Graphite300) }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Delete Confirmation Dialog
    if (deleteConfirmPaths != null) {
        val paths = deleteConfirmPaths!!
        AlertDialog(
            onDismissRequest = { deleteConfirmPaths = null },
            title = { Text("Confirm Deletion", color = DangerRed, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete ${paths.size} item(s)?\nThis action cannot be undone.",
                    color = Graphite200,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val service = RetrofitClient.getService(context)
                                service.deleteFiles(mapOf("paths" to paths))
                                Toast.makeText(context, "Deleted ${paths.size} item(s)", Toast.LENGTH_SHORT).show()
                                deleteConfirmPaths = null
                                isSelectionMode = false
                                selectedPaths.clear()
                                loadPath(currentPath)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("DELETE", color = Graphite100, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmPaths = null }) { Text("CANCEL", color = Graphite300) }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}
