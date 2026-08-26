package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuthState
import com.example.ui.theme.TgBlue
import com.example.ui.theme.TgCyan
import com.example.ui.theme.TgDarkBackground
import com.example.ui.theme.TgDarkBorder
import com.example.ui.theme.TgDarkCard
import com.example.ui.theme.TgDarkSurface
import com.example.ui.theme.TgDarkSurfaceVariant
import com.example.ui.theme.TgTextMuted
import com.example.ui.theme.TgTextPrimary
import com.example.ui.theme.TgTextSecondary
import com.example.ui.theme.TgVoiceGreen
import com.example.ui.theme.TgVoiceMutedRed

@Composable
fun AuthScreen(
    authState: AuthState,
    onSendPhoneNumber: (String) -> Unit,
    onSendCode: (String) -> Unit,
    onSendPassword: (String) -> Unit,
    onQuickDemoLogin: () -> Unit,
    onOpenArchitecture: () -> Unit,
    onOpenDiagnostics: () -> Unit = {},
    onResetToPhone: () -> Unit = {},
    apiId: Int = 2040,
    apiHash: String = "b18441a1ff607e10a989891a5462e627",
    onUpdateApiCredentials: (Int, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var phoneNumber by remember { mutableStateOf("+91 98765 43210") }
    var authCode by remember { mutableStateOf("") }
    var cloudPassword by remember { mutableStateOf("") }
    var showApiSettings by remember { mutableStateOf(false) }
    var customApiIdText by remember { mutableStateOf(apiId.toString()) }
    var customApiHashText by remember { mutableStateOf(apiHash) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TgDarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Telegram Voice Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(TgCyan, TgBlue))
                    )
                    .border(2.dp, TgCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Telegram Voice Client",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TgTextPrimary
            )
            Text(
                text = "Ultra Low-Latency • Dedicated Voice Chat Gateway",
                fontSize = 12.sp,
                color = TgTextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Auth Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = TgDarkCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TgDarkBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    when (authState) {
                        is AuthState.WaitPhoneNumber, is AuthState.LoggedOut, is AuthState.Error -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Login with Telegram Account",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TgTextPrimary
                                    )
                                    Text(
                                        text = "Enter international number (+CountryCode)",
                                        fontSize = 11.sp,
                                        color = TgTextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Phone Number (+CountryCode Number)") },
                                placeholder = { Text("+91 98765 43210") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = TgCyan)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { if (phoneNumber.isNotBlank()) onSendPhoneNumber(phoneNumber) }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TgCyan,
                                    unfocusedBorderColor = TgDarkBorder,
                                    focusedTextColor = TgTextPrimary,
                                    unfocusedTextColor = TgTextPrimary,
                                    focusedContainerColor = TgDarkSurfaceVariant,
                                    unfocusedContainerColor = TgDarkSurfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_number_input")
                            )

                            if (authState is AuthState.Error) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = authState.message,
                                    color = TgVoiceMutedRed,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { if (phoneNumber.isNotBlank()) onSendPhoneNumber(phoneNumber) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TgCyan,
                                    contentColor = TgBlue
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("send_code_button")
                            ) {
                                Text("Send Telegram Code", color = TgBlue, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TgBlue, modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Optional Telegram Client Credentials Configuration Collapsible
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showApiSettings = !showApiSettings }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (showApiSettings) "▲ Hide Telegram API Credentials" else "▼ Custom Telegram API Credentials (my.telegram.org)",
                                    fontSize = 11.sp,
                                    color = TgCyan
                                )
                            }

                            AnimatedVisibility(visible = showApiSettings) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .background(TgDarkSurfaceVariant, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Custom MTProto API Keys (my.telegram.org):",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TgTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = customApiIdText,
                                        onValueChange = { customApiIdText = it },
                                        label = { Text("API ID", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = customApiHashText,
                                        onValueChange = { customApiHashText = it },
                                        label = { Text("API Hash", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            val id = customApiIdText.toIntOrNull() ?: 2040
                                            onUpdateApiCredentials(id, customApiHashText)
                                            showApiSettings = false
                                        },
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Text("Save Custom API Keys", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        is AuthState.WaitCode -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Enter Verification Code",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TgTextPrimary
                                    )
                                    Text(
                                        text = "Sent to Telegram on ${authState.phoneNumber}",
                                        fontSize = 11.sp,
                                        color = TgTextSecondary
                                    )
                                }

                                Text(
                                    text = "Edit #",
                                    fontSize = 12.sp,
                                    color = TgCyan,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { onResetToPhone() }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = authCode,
                                onValueChange = { authCode = it },
                                label = { Text("5-digit OTP code") },
                                placeholder = { Text("12345") },
                                leadingIcon = {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = TgVoiceGreen)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { if (authCode.isNotBlank()) onSendCode(authCode) }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TgVoiceGreen,
                                    unfocusedBorderColor = TgDarkBorder,
                                    focusedTextColor = TgTextPrimary,
                                    unfocusedTextColor = TgTextPrimary,
                                    focusedContainerColor = TgDarkSurfaceVariant,
                                    unfocusedContainerColor = TgDarkSurfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_code_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { if (authCode.isNotBlank()) onSendCode(authCode) },
                                colors = ButtonDefaults.buttonColors(containerColor = TgVoiceGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("verify_code_button")
                            ) {
                                Text("Verify & Connect", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        is AuthState.WaitPassword -> {
                            Text(
                                text = "Telegram 2FA Cloud Password",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TgTextPrimary
                            )
                            Text(
                                text = "Your account has 2-Step Verification enabled",
                                fontSize = 11.sp,
                                color = TgTextSecondary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = cloudPassword,
                                onValueChange = { cloudPassword = it },
                                label = { Text("Cloud Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = TgCyan)
                                },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { if (cloudPassword.isNotBlank()) onSendPassword(cloudPassword) }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TgCyan,
                                    unfocusedBorderColor = TgDarkBorder,
                                    focusedTextColor = TgTextPrimary,
                                    unfocusedTextColor = TgTextPrimary,
                                    focusedContainerColor = TgDarkSurfaceVariant,
                                    unfocusedContainerColor = TgDarkSurfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cloud_password_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { if (cloudPassword.isNotBlank()) onSendPassword(cloudPassword) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TgCyan,
                                    contentColor = TgBlue
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_password_button")
                            ) {
                                Text("Unlock & Sign In", color = TgBlue, fontWeight = FontWeight.Bold)
                            }
                        }

                        is AuthState.Ready -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TgVoiceGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Connected as ${authState.firstName}",
                                    color = TgVoiceGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick 1-Tap Demo / Test Account Switch
            OutlinedButton(
                onClick = onQuickDemoLogin,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("demo_profile_button")
            ) {
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = TgCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Explore with Verified Demo Profile", color = TgCyan, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // In-app Architecture & Telegram Limits button
            OutlinedButton(
                onClick = onOpenArchitecture,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("architecture_info_button")
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = TgTextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Telegram Protocol Architecture & Limits", color = TgTextSecondary, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real Subsystem Diagnostics Inspector button
            OutlinedButton(
                onClick = onOpenDiagnostics,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("diagnostics_screen_button")
            ) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = TgCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Voice Chat Diagnostics & Audio Subsystems", color = TgCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
