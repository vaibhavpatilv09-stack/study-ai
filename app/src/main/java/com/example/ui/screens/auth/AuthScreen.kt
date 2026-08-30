package com.example.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthState
import com.example.data.remote.supabase.SupabaseConfig
import com.example.ui.components.*
import com.example.ui.theme.*

enum class AuthTab {
    SIGN_IN,
    SIGN_UP,
    SUPABASE_CONFIG
}

@Composable
fun AuthScreen(
    authState: AuthState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit,
    onSignOut: () -> Unit,
    onUpdateSupabaseConfig: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(AuthTab.SIGN_IN) }

    // Sign In Fields
    var signInEmail by remember { mutableStateOf("scholar@studymate.ai") }
    var signInPassword by remember { mutableStateOf("mastery2026") }
    var signInError by remember { mutableStateOf<String?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }

    // Sign Up Fields
    var signUpName by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var signUpConfirmPassword by remember { mutableStateOf("") }
    var signUpError by remember { mutableStateOf<String?>(null) }
    var isSigningUp by remember { mutableStateOf(false) }

    // Supabase Config Fields
    var customUrl by remember { mutableStateOf(SupabaseConfig.currentSupabaseUrl) }
    var customAnonKey by remember { mutableStateOf(SupabaseConfig.currentAnonKey) }
    var configSavedMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Banner
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(IndigoPrimary, CyanSecondary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = "Supabase Auth",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Supabase Cloud Authentication",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Secure user registration, session management & PostgreSQL synchronization",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Authenticated Banner if already logged in
        if (authState is AuthState.Authenticated) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("authenticated_user_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldSuccess.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = authState.displayName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = authState.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ACTIVE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Supabase User ID: ${authState.uid.take(16)}...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    AppPrimaryButton(
                        text = "Sign Out",
                        onClick = onSignOut,
                        variant = ButtonVariant.DANGER,
                        leadingIcon = Icons.Default.Logout,
                        height = 46.dp,
                        testTag = "logout_button"
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Tab Selector (Sign In | Sign Up | Supabase Config)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabButton(
                    title = "Sign In",
                    icon = Icons.Default.Login,
                    isSelected = currentTab == AuthTab.SIGN_IN,
                    onClick = { currentTab = AuthTab.SIGN_IN },
                    modifier = Modifier.weight(1f),
                    testTag = "tab_sign_in"
                )
                TabButton(
                    title = "Register",
                    icon = Icons.Default.PersonAdd,
                    isSelected = currentTab == AuthTab.SIGN_UP,
                    onClick = { currentTab = AuthTab.SIGN_UP },
                    modifier = Modifier.weight(1f),
                    testTag = "tab_sign_up"
                )
                TabButton(
                    title = "Supabase",
                    icon = Icons.Default.Settings,
                    isSelected = currentTab == AuthTab.SUPABASE_CONFIG,
                    onClick = { currentTab = AuthTab.SUPABASE_CONFIG },
                    modifier = Modifier.weight(1f),
                    testTag = "tab_supabase_config"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tab Contents
        when (currentTab) {
            AuthTab.SIGN_IN -> {
                // SIGN IN FORM
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Sign in with Supabase",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        AppTextField(
                            value = signInEmail,
                            onValueChange = {
                                signInEmail = it
                                signInError = null
                            },
                            label = "Email Address",
                            placeholder = "name@example.com",
                            leadingIcon = Icons.Default.Email,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            isError = signInError != null,
                            testTag = "signin_email_input"
                        )

                        AppPasswordField(
                            value = signInPassword,
                            onValueChange = {
                                signInPassword = it
                                signInError = null
                            },
                            label = "Password",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            testTag = "signin_password_input"
                        )

                        if (signInError != null) {
                            Text(
                                text = signInError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        AppPrimaryButton(
                            text = "Sign In",
                            onClick = {
                                if (signInEmail.isBlank() || !signInEmail.contains("@")) {
                                    signInError = "Please enter a valid email address"
                                } else if (signInPassword.length < 6) {
                                    signInError = "Password must be at least 6 characters"
                                } else {
                                    isSigningIn = true
                                    onSignIn(signInEmail, signInPassword)
                                    isSigningIn = false
                                }
                            },
                            isLoading = isSigningIn,
                            leadingIcon = Icons.Default.Login,
                            testTag = "signin_submit_button"
                        )

                        // Quick Demo Fill button
                        AppSecondaryButton(
                            text = "Fill Demo Credentials",
                            onClick = {
                                signInEmail = "scholar@studymate.ai"
                                signInPassword = "mastery2026"
                                signInError = null
                            },
                            leadingIcon = Icons.Default.AutoFixHigh,
                            testTag = "fill_demo_button"
                        )
                    }
                }
            }

            AuthTab.SIGN_UP -> {
                // SIGN UP FORM
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Create Supabase Account",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        AppTextField(
                            value = signUpName,
                            onValueChange = {
                                signUpName = it
                                signUpError = null
                            },
                            label = "Full Name",
                            placeholder = "Elena Vance",
                            leadingIcon = Icons.Default.Person,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            testTag = "signup_name_input"
                        )

                        AppTextField(
                            value = signUpEmail,
                            onValueChange = {
                                signUpEmail = it
                                signUpError = null
                            },
                            label = "Email Address",
                            placeholder = "elena.vance@stanford.edu",
                            leadingIcon = Icons.Default.Email,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            testTag = "signup_email_input"
                        )

                        AppPasswordField(
                            value = signUpPassword,
                            onValueChange = {
                                signUpPassword = it
                                signUpError = null
                            },
                            label = "Password (min 6 chars)",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            testTag = "signup_password_input"
                        )

                        AppPasswordField(
                            value = signUpConfirmPassword,
                            onValueChange = {
                                signUpConfirmPassword = it
                                signUpError = null
                            },
                            label = "Confirm Password",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            testTag = "signup_confirm_password_input"
                        )

                        if (signUpError != null) {
                            Text(
                                text = signUpError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        AppPrimaryButton(
                            text = "Register Account",
                            onClick = {
                                if (signUpName.isBlank()) {
                                    signUpError = "Please enter your name"
                                } else if (signUpEmail.isBlank() || !signUpEmail.contains("@")) {
                                    signUpError = "Please enter a valid email address"
                                } else if (signUpPassword.length < 6) {
                                    signUpError = "Password must be at least 6 characters"
                                } else if (signUpPassword != signUpConfirmPassword) {
                                    signUpError = "Passwords do not match"
                                } else {
                                    isSigningUp = true
                                    onSignUp(signUpEmail, signUpPassword, signUpName)
                                    isSigningUp = false
                                }
                            },
                            isLoading = isSigningUp,
                            variant = ButtonVariant.SUCCESS,
                            leadingIcon = Icons.Default.PersonAdd,
                            testTag = "signup_submit_button"
                        )
                    }
                }
            }

            AuthTab.SUPABASE_CONFIG -> {
                // SUPABASE CONFIG FORM
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Supabase Project Settings",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Connect to your custom Supabase cloud project or inspect the active GoTrue auth configuration.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        AppTextField(
                            value = customUrl,
                            onValueChange = {
                                customUrl = it
                                configSavedMessage = null
                            },
                            label = "Supabase Project URL",
                            placeholder = "https://your-project.supabase.co",
                            leadingIcon = Icons.Default.Language,
                            testTag = "supabase_url_input"
                        )

                        AppTextField(
                            value = customAnonKey,
                            onValueChange = {
                                customAnonKey = it
                                configSavedMessage = null
                            },
                            label = "Supabase Anon / Public Key",
                            placeholder = "eyJhbGciOi...",
                            leadingIcon = Icons.Default.Key,
                            testTag = "supabase_key_input"
                        )

                        if (configSavedMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = configSavedMessage!!,
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldSuccess
                                )
                            }
                        }

                        AppPrimaryButton(
                            text = "Save & Reconnect Supabase",
                            onClick = {
                                onUpdateSupabaseConfig(customUrl, customAnonKey)
                                configSavedMessage = "✅ Supabase endpoints updated and reconnected successfully!"
                            },
                            leadingIcon = Icons.Default.Sync,
                            testTag = "save_supabase_config_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .testTag(testTag),
        color = if (isSelected) IndigoPrimary else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
