package com.agupta07505.smartisland.ui.expanded

import android.app.ActivityOptions
import android.app.KeyguardManager
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.agupta07505.smartisland.R
import com.agupta07505.smartisland.util.ShadeGroup
import com.agupta07505.smartisland.util.ShadeItem
import com.agupta07505.smartisland.util.ShadeNotifications
import com.agupta07505.smartisland.util.runCatchingLogged

private val PanelSurface = Color(0xFF222222)
private val RowSurface = Color(0xFF2C2C2E)
private val MutedText = Color(0xFF9CA3AF)
private val AccentBlue = Color(0xFF3B82F6)

/** Which panel the expanded island shows while it has no card: the control center or the notification center. */
enum class IdlePanel { ControlCenter, Notifications }

/** The panel the expanded island draws for an empty list, provided once by the overlay. */
val LocalIdlePanel = androidx.compose.runtime.compositionLocalOf { IdlePanel.ControlCenter }

/**
 * The swipe-down notification center (Rule_Book §4.17): the shade's live notifications, grouped by app, newest
 * first. A tap opens the app, the cross dismisses, "Clear all" clears every clearable one and "Reply" answers a
 * message in place. While the device is locked it shows apps and counts only, with no actions that leave the lock.
 */
@Composable
internal fun NotificationCenter(
    onCollapse: () -> Unit,
    onInputStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locked = remember { (context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardLocked }
    val all by ShadeNotifications.items.collectAsState()
    val groups = remember(all, locked) { ShadeNotifications.group(ShadeNotifications.forLockState(all, locked)) }
    var replyingKey by remember { mutableStateOf<String?>(null) }
    DisposableEffect(Unit) { onDispose { onInputStateChanged(false) } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PanelSurface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.notification_center_title),
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (!locked && all.any { it.clearable }) {
                TextButton(onClick = { ShadeNotifications.dismissAll?.invoke() }) {
                    Text(stringResource(R.string.notification_center_clear_all), color = AccentBlue, fontSize = 13.sp)
                }
            }
        }
        if (groups.isEmpty()) {
            Text(
                text = stringResource(R.string.notification_center_empty),
                color = MutedText,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 18.dp)
            )
        } else {
            Column(
                modifier = Modifier.heightIn(max = 170.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groups.forEach { group ->
                    GroupCard(
                        group = group,
                        locked = locked,
                        replyingKey = replyingKey,
                        onReplying = { key ->
                            replyingKey = key
                            onInputStateChanged(key != null)
                        },
                        onOpened = onCollapse
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupCard(
    group: ShadeGroup,
    locked: Boolean,
    replyingKey: String?,
    onReplying: (String?) -> Unit,
    onOpened: () -> Unit
) {
    val context = LocalContext.current
    val icon = remember(group.packageName) {
        runCatching { context.packageManager.getApplicationIcon(group.packageName).toBitmap(64, 64).asImageBitmap() }.getOrNull()
    }
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(RowSurface).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icon != null) {
                Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(20.dp).clip(CircleShape))
            } else {
                Box(Modifier.size(20.dp).clip(CircleShape).background(AccentBlue))
            }
            Text(group.appLabel, color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            if (group.items.size > 1) {
                Text(group.items.size.toString(), color = MutedText, fontSize = 12.sp)
            }
        }
        if (locked) {
            Text(
                text = stringResource(R.string.notification_center_locked_count, group.items.size),
                color = Color.White,
                fontSize = 14.sp
            )
        } else {
            group.items.forEach { item ->
                ItemRow(item, replying = replyingKey == item.key, onReplying = onReplying, onOpened = onOpened)
            }
        }
    }
}

@Composable
private fun ItemRow(item: ShadeItem, replying: Boolean, onReplying: (String?) -> Unit, onOpened: () -> Unit) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        ShadeNotifications.open?.invoke(item.key)
                        onOpened()
                    }
            ) {
                if (item.title.isNotBlank()) {
                    Text(item.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (item.text.isNotBlank()) {
                    Text(item.text, color = MutedText, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            if (item.reply != null && !replying) {
                TextButton(onClick = { onReplying(item.key) }) {
                    Text(stringResource(R.string.notification_center_reply), color = AccentBlue, fontSize = 13.sp)
                }
            }
            if (item.clearable) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.notification_center_dismiss),
                    tint = MutedText,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { ShadeNotifications.dismiss?.invoke(item.key) }
                        .padding(7.dp)
                )
            }
        }
        if (replying && item.reply != null) {
            var text by remember { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1C1C1E))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    if (text.isEmpty()) {
                        Text(stringResource(R.string.notification_center_reply_hint), color = MutedText, fontSize = 14.sp)
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        cursorBrush = SolidColor(Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TextButton(
                    enabled = text.isNotBlank(),
                    onClick = {
                        sendShadeReply(context, item, text.trim())
                        onReplying(null)
                    }
                ) { Text(stringResource(R.string.notification_center_send), color = AccentBlue, fontSize = 13.sp) }
            }
        }
    }
}

/** Fires the notification's own reply action with [text], the same way the expanded notification card does. */
private fun sendShadeReply(context: Context, item: ShadeItem, text: String) {
    val reply = item.reply ?: return
    runCatchingLogged("NotificationCenter", "Failed to send the reply") {
        val intent = Intent()
        val bundle = Bundle().apply { putCharSequence(reply.remoteInputKey, text) }
        RemoteInput.addResultsToIntent(arrayOf(RemoteInput.Builder(reply.remoteInputKey).build()), intent, bundle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val options = ActivityOptions.makeBasic()
                .setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                .toBundle()
            reply.pendingIntent.send(context, 0, intent, null, null, null, options)
        } else {
            reply.pendingIntent.send(context, 0, intent)
        }
    }
}
