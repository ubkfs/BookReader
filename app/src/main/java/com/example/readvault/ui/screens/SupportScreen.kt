package com.example.readvault.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readvault.data.model.SupportTicket
import com.example.readvault.ui.viewmodel.ReadVaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    viewModel: ReadVaultViewModel,
    modifier: Modifier = Modifier
) {
    val tickets by viewModel.supportTickets.collectAsState(initial = emptyList())
    var showNewTicketDialog by remember { mutableStateOf(false) }
    var expandedTicketId by remember { mutableStateOf<Long?>(null) }
    var replyText by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewTicketDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("create_ticket_fab")
            ) {
                Icon(Icons.Default.AddComment, contentDescription = "Create Ticket")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Help Desk & Support",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Need help with offline reading, formats or sync? Talk to us.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (tickets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No support tickets yet. Tap '+' to submit a query.")
                    }
                }
            } else {
                items(tickets, key = { it.id }) { ticket ->
                    val isExpanded = expandedTicketId == ticket.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedTicketId = if (isExpanded) null else ticket.id
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(ticket.ticketNumber, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(ticket.category, fontSize = 10.sp) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }

                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(ticket.status, fontSize = 10.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (ticket.status == "Resolved") Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                        labelColor = if (ticket.status == "Resolved") Color(0xFF065F46) else Color(0xFF92400E)
                                    ),
                                    modifier = Modifier.height(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = ticket.subject,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Created: ${ticket.date}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(12.dp))

                                // Messages Thread
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ticket.messages.forEach { msg ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (msg.isAdmin) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                )
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(text = msg.senderName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text(text = msg.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = msg.message, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Reply box
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = replyText,
                                        onValueChange = { replyText = it },
                                        placeholder = { Text("Write a reply…", fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            if (replyText.isNotBlank()) {
                                                viewModel.replyToTicket(ticket.id, replyText)
                                                replyText = ""
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "Send Reply", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewTicketDialog) {
        var subject by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Reader & Formats") }
        var message by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewTicketDialog = false },
            title = { Text("New Support Ticket", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("How can we help? *") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subject.isNotBlank() && message.isNotBlank()) {
                            viewModel.createSupportTicket(subject, category, message)
                            showNewTicketDialog = false
                        }
                    },
                    enabled = subject.isNotBlank() && message.isNotBlank()
                ) {
                    Text("Submit Ticket")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTicketDialog = false }) { Text("Cancel") }
            }
        )
    }
}
