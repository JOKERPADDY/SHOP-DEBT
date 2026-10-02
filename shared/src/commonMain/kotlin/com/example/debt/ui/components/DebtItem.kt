package com.example.debt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.debt.logic.DebtCalculator
import com.example.debt.logic.DebtStatus
import com.example.debt.models.Debt
import com.example.debt.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun DebtItem(
    debt: Debt,
    onClick: () -> Unit,
    onSendInvoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val remaining = DebtCalculator.getDebtRemaining(debt)
    val status = DebtCalculator.getDebtStatus(debt, today)
    
    val statusColor = when (status) {
        DebtStatus.PENDING -> OrangeColor
        DebtStatus.PARTIAL -> BlueColor
        DebtStatus.PAID -> GreenColor
        DebtStatus.OVERDUE -> RedColor
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        backgroundColor = SurfaceColor,
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = debt.customerName,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = debt.product,
                    style = MaterialTheme.typography.body2,
                    color = TextSecondary
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSendInvoice) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send Invoice",
                            tint = AccentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "KES ${remaining.toInt()}",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        color = if (status == DebtStatus.OVERDUE) RedColor else TextPrimary
                    )
                }
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.caption,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
