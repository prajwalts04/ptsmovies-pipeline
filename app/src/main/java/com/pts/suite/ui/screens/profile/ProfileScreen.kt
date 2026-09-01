package com.pts.suite.ui.screens.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pts.suite.data.api.*
import com.pts.suite.ui.theme.*
import com.pts.suite.updater.AppUpdater
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    user: UserInfo?,
    onNavigateToDownloads: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateManifest by remember { mutableStateOf<UpdateManifest?>(null) }

    // User Management states
    var allUsers by remember { mutableStateOf<List<UserInfo>>(emptyList()) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    // Change Password states
    var showChangePassDialog by remember { mutableStateOf(false) }
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }

    // Profile photo upload launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val tempFile = File(context.cacheDir, "avatar_upload.jpg")
                    val outputStream = FileOutputStream(tempFile)
                    inputStream?.copyTo(outputStream)
                    outputStream.close()
                    inputStream?.close()

                    val reqBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("avatar", tempFile.name, reqBody)
                    RetrofitClient.getService(context).uploadAvatar(part)
                    Toast.makeText(context, "Avatar updated successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (user?.role == "admin") {
            try {
                val res = RetrofitClient.getService(context).listUsers()
                if (res.isSuccessful && res.body() != null) {
                    allUsers = res.body()!!
                }
            } catch (e: Exception) {}
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Back Row
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Graphite100)
                }
                Text(text = "PROFILE & SETTINGS", color = Graphite100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Profile Avatar Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.2.dp, SketchBorder, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .border(2.dp, EmeraldGreen, CircleShape)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (!user?.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(model = user!!.avatarUrl, contentDescription = "Avatar", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Graphite200, modifier = Modifier.size(40.dp))
                    }
                }

                Text(text = "Tap photo to change avatar from phone", fontSize = 11.sp, color = Graphite400)

                Text(text = user?.username ?: "User", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Graphite100)

                Text(
                    text = (user?.role ?: "admin").uppercase(),
                    fontSize = 11.sp,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Quick Actions (Offline Downloads & Change Password)
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onNavigateToDownloads,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = Graphite100),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SketchBorder))
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Downloads", fontSize = 12.sp)
                }

                Button(
                    onClick = { showChangePassDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = Graphite100),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SketchBorder))
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Graphite300, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Password", fontSize = 12.sp)
                }
            }
        }

        // In-App Updater Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface)
                    .border(1.dp, SketchBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "APP VERSION & UPDATES", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Graphite400)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "PTS Suite v1.0.0 (Native)", fontSize = 13.sp, color = Graphite100, fontWeight = FontWeight.Bold)

                    Button(
                        onClick = {
                            isCheckingUpdate = true
                            scope.launch {
                                val manifest = AppUpdater.checkForUpdate(context)
                                updateManifest = manifest
                                isCheckingUpdate = false
                                if (manifest == null) {
                                    Toast.makeText(context, "You are on the latest version!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Graphite100, contentColor = PitchBlack)
                    ) {
                        Text(if (isCheckingUpdate) "Checking..." else "Check Update", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (updateManifest != null) {
                    Button(
                        onClick = { AppUpdater.startDownloadAndInstall(context, updateManifest!!.downloadUrl) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = PitchBlack)
                    ) {
                        Text("UPDATE TO ${updateManifest!!.versionName}", fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // User Management Section (Admin Only)
        if (user?.role == "admin") {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "USER MANAGEMENT", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Graphite400)

                    IconButton(onClick = { showAddUserDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add User", tint = EmeraldGreen)
                    }
                }
            }

            items(allUsers) { u ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurface)
                        .border(1.dp, SketchBorder, RoundedCornerShape(6.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = u.username, color = Graphite100, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = u.role.uppercase(), color = Graphite400, fontSize = 10.sp)
                    }

                    if (u.id != user.id) {
                        IconButton(onClick = {
                            scope.launch {
                                RetrofitClient.getService(context).deleteUser(u.id)
                                allUsers = allUsers.filter { it.id != u.id }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Logout Button
        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DangerRed))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = DangerRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", color = DangerRed, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add User Dialog
    if (showAddUserDialog) {
        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            title = { Text("Add New User", color = Graphite100) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newUsername, onValueChange = { newUsername = it }, label = { Text("Username") })
                    OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        RetrofitClient.getService(context).addUser(AddUserRequest(username = newUsername, password = newPassword))
                        showAddUserDialog = false
                        newUsername = ""
                        newPassword = ""
                        val res = RetrofitClient.getService(context).listUsers()
                        if (res.isSuccessful && res.body() != null) allUsers = res.body()!!
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) { Text("Cancel") }
            },
            containerColor = DarkSurface
        )
    }

    // Change Password Dialog
    if (showChangePassDialog) {
        AlertDialog(
            onDismissRequest = { showChangePassDialog = false },
            title = { Text("Change Password", color = Graphite100) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = currentPass, onValueChange = { currentPass = it }, label = { Text("Current Password") }, visualTransformation = PasswordVisualTransformation())
                    OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("New Password") }, visualTransformation = PasswordVisualTransformation())
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        RetrofitClient.getService(context).changePassword(ChangePasswordRequest(currentPassword = currentPass, newPassword = newPass))
                        showChangePassDialog = false
                        currentPass = ""
                        newPass = ""
                        Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showChangePassDialog = false }) { Text("Cancel") }
            },
            containerColor = DarkSurface
        )
    }
}
