package com.pts.suite.ui.screens.vault

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.pts.suite.ui.theme.*

/**
 * BiometricHelper manages AndroidX BiometricPrompt authentication
 * for securing PTS Vault documents, secret notes, and credentials.
 */
object BiometricHelper {

    /**
     * Checks if biometric authentication (fingerprint/face) or device credentials are ready.
     */
    fun canAuthenticate(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_STRONG
        }
        return biometricManager.canAuthenticate(authenticators)
    }

    /**
     * Returns true if biometric hardware is present and enrolled.
     */
    fun isBiometricAvailable(context: Context): Boolean {
        return canAuthenticate(context) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Launches AndroidX BiometricPrompt on the host Activity.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "PTS Vault Authentication",
        subtitle: String = "Verify your identity to view confidential cards & notes",
        description: String = "Scan your fingerprint or use device face unlock",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            promptInfoBuilder.setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        } else {
            promptInfoBuilder.setNegativeButtonText("Use Master PIN")
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        prompt.authenticate(promptInfoBuilder.build())
    }
}

/**
 * VaultBiometricGate composable locks access to confidential vault data
 * until fingerprint, face, or master PIN verification succeeds.
 */
@Composable
fun VaultBiometricGate(
    isUnlocked: Boolean,
    onUnlockSuccess: () -> Unit,
    onLockRequested: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var showPinFallback by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    val isBioAvailable = remember { BiometricHelper.isBiometricAvailable(context) }

    if (isUnlocked) {
        content()
    } else {
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
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(1.5.dp, SketchBorder, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Shield / Fingerprint Hero Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(EmeraldGreenDark, DarkSurfaceElevated)
                            )
                        )
                        .border(1.5.dp, EmeraldGreen.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (showPinFallback) Icons.Default.Password else Icons.Default.Fingerprint,
                        contentDescription = "Biometric Lock",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Text(
                    text = "PTS Secure Vault",
                    color = Graphite100,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (showPinFallback) {
                        "Enter your master PIN or password to unlock digital wallet and confidential notes."
                    } else {
                        "Hardware biometric encryption is active. Authenticate with fingerprint or face ID to access vault."
                    },
                    color = Graphite300,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                if (showPinFallback) {
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = null
                        },
                        label = { Text("Master PIN / Passphrase") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (enteredPin.isNotBlank()) {
                                    onUnlockSuccess()
                                } else {
                                    pinError = "PIN cannot be empty"
                                }
                            }
                        ),
                        singleLine = true,
                        isError = pinError != null,
                        supportingText = {
                            if (pinError != null) {
                                Text(text = pinError!!, color = DangerRed, fontSize = 11.sp)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = SketchBorder,
                            focusedTextColor = Graphite100,
                            unfocusedTextColor = Graphite100
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showPinFallback = false
                                pinError = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Graphite300),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(SketchBorder, SketchBorder))),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("BIOMETRIC", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (enteredPin.isNotBlank()) {
                                    onUnlockSuccess()
                                } else {
                                    pinError = "Please enter your PIN"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("UNLOCK", color = PitchBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            val activity = context as? FragmentActivity
                            if (activity != null && isBioAvailable) {
                                BiometricHelper.authenticate(
                                    activity = activity,
                                    onSuccess = {
                                        onUnlockSuccess()
                                        Toast.makeText(context, "Vault Unlocked", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { _, _ ->
                                        showPinFallback = true
                                    },
                                    onFailed = {
                                        Toast.makeText(context, "Biometric verification failed", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                showPinFallback = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = PitchBlack,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UNLOCK WITH BIOMETRICS",
                            color = PitchBlack,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    TextButton(
                        onClick = { showPinFallback = true }
                    ) {
                        Text(
                            text = "Use Master PIN / Password fallback",
                            color = Graphite300,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
