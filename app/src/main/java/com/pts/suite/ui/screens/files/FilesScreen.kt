package com.pts.suite.ui.screens.files

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pts.suite.data.api.FileItem
import com.pts.suite.data.api.RetrofitClient
import com.pts.suite.ui.theme.*
import kotlinx.coroutines.launch

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

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

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
                    filesList = body.items
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

    // Back gesture inside folder structure
    BackHandler(enabled = currentPath != "/Data" && currentPath != "/") {
        if (!parentPath.isNullOrEmpty()) {
            loadPath(parentPath!!)
        } else {
            loadPath("/Data")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Current Path Breadcrumb Bar & Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (!parentPath.isNullOrEmpty() && currentPath != "/Data" && currentPath != "/") {
                    IconButton(
                        onClick = { loadPath(parentPath!!) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Parent Directory", tint = EmeraldGreen)
                    }
                }

                Text(
                    text = currentPath,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Graphite100,
                    maxLines = 1
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { showNewFolderDialog = true }) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = EmeraldGreen)
                }
                IconButton(onClick = { loadPath(currentPath) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Graphite300)
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldGreen)
            }
        } else if (filesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface)
                    .border(1.dp, SketchBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Empty folder", color = Graphite400, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filesList) { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                if (file.isDir) {
                                    loadPath(file.path)
                                } else {
                                    Toast.makeText(context, file.name + " (" + file.formattedSize + ")", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (file.isDir) Icons.Default.Folder else if (file.isVideo) Icons.Default.Movie else Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = if (file.isDir) EmeraldGreen else Graphite300,
                                modifier = Modifier.size(24.dp)
                            )

                            Column {
                                Text(
                                    text = file.name,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (file.isDir) FontWeight.Bold else FontWeight.Normal,
                                    color = Graphite100,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (file.isDir) "Folder" else file.formattedSize,
                                    fontSize = 11.sp,
                                    color = Graphite400,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        RetrofitClient.getService(context).deleteFiles(mapOf("paths" to listOf(file.path)))
                                        loadPath(currentPath)
                                        Toast.makeText(context, "Deleted: ${file.name}", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }

    // New Folder Dialog
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("Create New Folder", color = Graphite100) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = SketchBorder,
                        focusedTextColor = Graphite100
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            scope.launch {
                                try {
                                    RetrofitClient.getService(context).createDirectory(
                                        mapOf("path" to currentPath, "name" to newFolderName.trim())
                                    )
                                    showNewFolderDialog = false
                                    newFolderName = ""
                                    loadPath(currentPath)
                                    Toast.makeText(context, "Folder created", Toast.LENGTH_SHORT).show()
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
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("CANCEL", color = Graphite300)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}
