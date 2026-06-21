package com.hheelo.countdown

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun CustomEditorHeader(onAdd: () -> Unit) {
    val extraColors = LocalCountdownColors.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.custom_countdown_title), fontWeight = FontWeight.SemiBold, color = extraColors.textPrimary, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.add_button))
            }
        }
        Text(
            stringResource(R.string.editor_description),
            color = extraColors.textSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
internal fun CustomEventEditor(
    event: CountdownEvent,
    highlighted: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onChange: (CountdownEvent) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val context = LocalContext.current
    val extraColors = LocalCountdownColors.current
    val targetDate = LocalDate.parse(event.targetDate)
    val borderColor = if (highlighted) countdownColor(event.colorHex) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = extraColors.cardSurface)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = event.title,
                        onValueChange = { onChange(event.copy(title = it)) },
                        label = { Text(stringResource(R.string.event_name_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = event.isPinned,
                            onCheckedChange = { onChange(event.copy(isPinned = it)) }
                        )
                        Icon(Icons.Filled.PushPin, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.pin_to_top), fontSize = 14.sp)
                    }
                }
                Column {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = stringResource(R.string.move_up))
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                        Icon(Icons.Filled.ArrowDownward, contentDescription = stringResource(R.string.move_down))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = countdownColor(CountdownColorHex.Danger)
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onChange(event.copy(targetDate = LocalDate.of(year, month + 1, dayOfMonth).toString()))
                        },
                        targetDate.year,
                        targetDate.monthValue - 1,
                        targetDate.dayOfMonth
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.target_date_button, targetDate.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            }

            ColorSwatchPicker(selectedHex = event.colorHex) { onChange(event.copy(colorHex = it)) }
        }
    }
}

@Composable
private fun ColorSwatchPicker(selectedHex: String, onSelect: (String) -> Unit) {
    val extraColors = LocalCountdownColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CountdownColorHex.eventSwatches.forEach { hex ->
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(countdownColor(hex))
                    .border(if (selectedHex == hex) 3.dp else 0.dp, extraColors.swatchBorder, CircleShape)
                    .clickable { onSelect(hex) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedHex == hex) {
                    Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun EmptyState() {
    val extraColors = LocalCountdownColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = extraColors.cardSurface
    ) {
        Text(
            stringResource(R.string.empty_state_hint),
            modifier = Modifier.padding(18.dp),
            color = extraColors.textSecondary
        )
    }
}
