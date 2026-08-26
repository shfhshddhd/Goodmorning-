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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuthState
import com.example.data.model.GroupVoiceChat
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
import com.example.ui.theme.TgVoiceGreenGlow

@Composable
fun ActiveChatsScreen(
    userAuthState: AuthState.Ready,
    activeChats: List<GroupVoiceChat>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onJoinChat: (GroupVoiceChat) -> Unit,
    onRefresh: () -> Unit,
    onOpenArchitecture: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredChats = activeChats.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                (it.username?.contains(searchQuery, ignoreCase = true) == true) ||
                (it.pinnedTopic?.contains(searchQuery, ignoreCase = true) == true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TgDarkBackground)
            .padding(16.dp)
    ) {
        // Top User Profile & Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TgDarkCard)
                .border(1.dp, TgDarkBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(TgDarkBorder),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userAuthState.firstName.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = TgTextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "TG Voice Client",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TgTextPrimary
                    )
                    Text(
                        text = "LOW LATENCY PIPELINE • ACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TgCyan,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Row {
                IconButton(
                    onClick = onOpenArchitecture,
                    modifier = Modifier.testTag("open_architecture_icon")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Architecture Specs",
                        tint = TgCyan
                    )
                }
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.testTag("logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = TgTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar & Count Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ACTIVE VOICE CHATS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TgTextSecondary,
                letterSpacing = 1.2.sp
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TgDarkSurfaceVariant)
                    .border(1.dp, TgCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${filteredChats.size} GROUPS FOUND",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TgCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Filter active rooms...", fontSize = 13.sp, color = TgTextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TgTextMuted, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Active Chats",
                        tint = TgCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TgCyan,
                unfocusedBorderColor = TgDarkBorder,
                focusedTextColor = TgTextPrimary,
                unfocusedTextColor = TgTextPrimary,
                focusedContainerColor = TgDarkCard,
                unfocusedContainerColor = TgDarkCard
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_voice_chats_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // List of Active Group Voice Chats
        if (filteredChats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Headset,
                        contentDescription = null,
                        tint = TgTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No active voice chats found",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TgTextSecondary
                    )
                    Text(
                        text = "Voice chats appear here in real-time when started in your Telegram groups",
                        fontSize = 12.sp,
                        color = TgTextMuted,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredChats, key = { it.id }) { chat ->
                    ActiveChatCard(
                        chat = chat,
                        onJoin = { onJoinChat(chat) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveChatCard(
    chat: GroupVoiceChat,
    onJoin: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TgDarkCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TgDarkBorder, RoundedCornerShape(16.dp))
            .clickable { onJoin() }
            .testTag("active_chat_card_${chat.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Title & Live Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Group Color Rounded Box
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TgBlue)
                            .border(1.dp, TgCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.title.take(1).uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TgCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = chat.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TgTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "${chat.activeParticipantsCount} members listening",
                            fontSize = 11.sp,
                            color = TgTextMuted
                        )
                    }
                }

                // Join Button Pill
                Button(
                    onClick = onJoin,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TgCyan,
                        contentColor = TgBlue
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("join_voice_chat_${chat.id}")
                ) {
                    Text(
                        text = "JOIN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TgBlue,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            if (!chat.pinnedTopic.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Topic: ${chat.pinnedTopic}",
                    fontSize = 11.sp,
                    color = TgTextSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
