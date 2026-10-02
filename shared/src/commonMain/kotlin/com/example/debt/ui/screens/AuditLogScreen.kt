package com.example.debt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.debt.models.LogEntry
import com.example.debt.ui.theme.*
import com.example.debt.ui.viewmodels.AuditLogViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AuditLogScreen(
    onBack: () -> Unit,
    viewModel: AuditLogViewModel = viewModel { AuditLogViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        backgroundColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Audit Log", color = TextPrimary, fontWeight = FontWeight.Bold) },
                backgroundColor = SurfaceColor,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.logs) { log ->
                    AuditLogItem(log)
                }
            }
        }
    }
}

@Composable
fun AuditLogItem(log: LogEntry) {
    val dateTime = log.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    val dateStr = "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year}"
    val timeStr = "${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"

    Card(
        backgroundColor = SurfaceColor,
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.actionType.replace("_", " "),
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    color = when (log.actionType) {
                        "ADD_DEBT" -> GreenColor
                        "DELETE_DEBT" -> RedColor
                        "ADD_PAYMENT" -> BlueColor
                        else -> AccentColor
                    }
                )
                Text(
                    text = "$dateStr $timeStr",
                    style = MaterialTheme.typography.caption,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = log.description,
                style = MaterialTheme.typography.body2,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "By: ${log.actorEmail}",
                style = MaterialTheme.typography.caption,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
