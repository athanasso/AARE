package net.prototux.aare.ui.car

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.prototux.aare.car.CarData
import kotlin.math.roundToInt

/**
 * Cockpit Telemetry Widget: live digital speedometer, gear state, RPM and fuel metrics.
 * Designed for immediate glanceability while driving.
 */
@Composable
internal fun CockpitCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val car by CarData.state.collectAsState()
    val speedKmh = car.speed?.value?.metersPerSecond?.let { (it * 3.6f).roundToInt() } ?: 0
    val activeGear = when (car.gearName) {
        "park" -> "P"
        "reverse" -> "R"
        "neutral" -> "N"
        "drive" -> "D"
        else -> car.gear?.value?.let { if (it > 0) "M$it" else "P" } ?: "P"
    }

    Card(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Header: Live Indicator & Gear selector strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (car.connected) MaterialTheme.colorScheme.primary else Color(0xFF6C7A89)),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (car.connected) "LIVE" else "STANDBY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                        ),
                        color = if (car.connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Gear indicators: P R N D
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf("P", "R", "N", "D").forEach { g ->
                        val isSelected = activeGear == g || (activeGear.startsWith("M") && g == "D")
                        val bg = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        val fg = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(bg)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (isSelected && activeGear.startsWith("M")) activeGear else g,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = fg,
                            )
                        }
                    }
                }
            }

            // Speedometer Centerpiece
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start,
            ) {
                Text(
                    text = "$speedKmh",
                    fontSize = 46.sp,
                    lineHeight = 48.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "KM/H",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                car.speed?.value?.cruiseSetSpeed?.let { cruise ->
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "CRUISE $cruise",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            // Bottom row: Secondary vehicle sensors (Fuel/RPM/Coolant/Voltage)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val rpm = car.rpm?.value
                val coolant = car.coolantTempC?.value
                val volts = car.batteryVoltage?.value
                val fuel = car.fuel?.value
                val temp = car.environment?.value?.temperatureC

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (rpm != null && rpm > 0f) {
                        Text(
                            text = "%.0f RPM".format(rpm),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else if (fuel?.levelPercent != null) {
                        Text(
                            text = "FUEL ${fuel.levelPercent}%",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = if (fuel.low == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = "TELEMETRY",
                            style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.8.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (coolant != null) {
                        Text(
                            text = "%.0f°C".format(coolant),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold),
                            color = if (coolant > 105f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        )
                    } else if (temp != null) {
                        Text(
                            text = "%.1f°C".format(temp),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (volts != null) {
                        Text(
                            text = "%.1fV".format(volts),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold),
                            color = if (volts < 11.8f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

