package com.pts.suite.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pts.suite.ui.theme.*

@Composable
fun LoginScreen(
    initialServerUrl: String,
    onLogin: (serverUrl: String, username: String, pass: String) -> Unit,
    onBiometricLogin: () -> Unit,
    canUseBiometric: Boolean,
    isLoading: Boolean,
    errorMessage: String?
) {
    var serverUrl by remember { mutableStateOf(initialServerUrl) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.5.dp, SketchBorder, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Brand Logo & Title
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = EmeraldGreen,
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = "PTS SUITE",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Graphite100,
                letterSpacing = 1.sp
            )

            Text(
                text = "Sign in with your Raspberry Pi account",
                fontSize = 13.sp,
                color = Graphite400
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = DangerRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Server URL Input
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("Server URL / Domain") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Graphite100,
                    unfocusedBorderColor = SketchBorder,
                    focusedTextColor = Graphite100,
                    unfocusedTextColor = Graphite200
                )
            )

            // Username Input
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Graphite100,
                    unfocusedBorderColor = SketchBorder,
                    focusedTextColor = Graphite100,
                    unfocusedTextColor = Graphite200
                )
            )

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Graphite100,
                    unfocusedBorderColor = SketchBorder,
                    focusedTextColor = Graphite100,
                    unfocusedTextColor = Graphite200
                )
            )

            // Login Button
            Button(
                onClick = { onLogin(serverUrl, username, password) },
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Graphite100, contentColor = PitchBlack)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PitchBlack, strokeWidth = 2.dp)
                } else {
                    Text("SIGN IN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Biometric Option if previously logged in
            if (canUseBiometric) {
                OutlinedButton(
                    onClick = onBiometricLogin,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SketchBorder))
                ) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock with Biometrics", color = Graphite100, fontSize = 13.sp)
                }
            }
        }
    }
}
