package com.KSU.EventsParser.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import com.KSU.EventsParser.R
import com.KSU.EventsParser.core.config.AppConfig
import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.domain.reminder.ReminderSchedule
import com.KSU.EventsParser.ui.components.AppIcons
import com.KSU.EventsParser.ui.components.ReminderCustomDialog
import com.KSU.EventsParser.ui.components.ReminderPanel
import com.KSU.EventsParser.ui.components.SectionLabel
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.AppType
import com.KSU.EventsParser.ui.theme.Dimens
import com.KSU.EventsParser.ui.theme.EventPalette
import com.KSU.EventsParser.ui.util.dateTimeText
import com.KSU.EventsParser.ui.util.descriptionText
import com.KSU.EventsParser.ui.util.label
import com.KSU.EventsParser.ui.util.locationText
import com.KSU.EventsParser.ui.util.organizerText
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * Екран деталей події.
 *
 * НЕ РОБИТЬ ЖОДНОГО МЕРЕЖЕВОГО ЗАПИТУ. GET /events/{id} закритий API-ключем
 * і повертає raw_text — неопрацьований текст оголошення, у якому можуть бути
 * телефони й прізвища. Ключа в застосунку немає й бути не повинно, тому екран
 * будується з об'єкта, який уже є в списку (див. EventsViewModel.findById).
 *
 * Блок нагадування з макета («За 1 годину до початку») повернуто: одне
 * локальне нагадування на подію з обраним зсувом (див. ReminderPanel).
 * Окремо від нього сервер шле рівно один push у момент підтвердження події.
 * Далі йдуть дії з посиланням і кодом підключення.
 */
@Composable
fun DetailScreen(
    event: Event,
    reminderOffsetMinutes: Int?,
    onSetReminder: (Int) -> Unit,
    onClearReminder: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = EventPalette.of(event.type)

    // «Зараз» оновлюємо при зміні події чи стану нагадування — цього достатньо,
    // щоб пресети, за які вже запізно, вимикалися.
    val now = remember(event, reminderOffsetMinutes) { Instant.now() }
    val availability = remember(event, now) {
        ReminderSchedule.availability(
            event = event,
            now = now,
            zone = AppConfig.eventZone,
            fallbackTime = AppConfig.reminderFallbackTime,
        )
    }
    val available = availability as? ReminderSchedule.Availability.Available

    var showCustomDialog by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    // Зсув, який чекає на рішення щодо дозволу на сповіщення.
    var pendingOffset by remember { mutableStateOf<Int?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionDenied = !granted
        val offset = pendingOffset
        pendingOffset = null
        // Без дозволу нічого не зберігаємо: нагадування, яке не можна показати, — обман.
        if (granted && offset != null) onSetReminder(offset)
    }

    // Дозвіл могли відкликати в системних налаштуваннях, поки нас не було.
    LaunchedEffect(reminderOffsetMinutes) {
        if (reminderOffsetMinutes != null && !hasNotificationPermission(context)) {
            permissionDenied = true
        }
    }

    /** Вмикає (або змінює) нагадування; на Android 13+ спершу питає дозвіл. */
    fun requestSetReminder(offsetMinutes: Int) {
        if (hasNotificationPermission(context)) {
            permissionDenied = false
            onSetReminder(offsetMinutes)
        } else {
            pendingOffset = offsetMinutes
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        // ─── Шапка ────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.HeaderBg)
                .statusBarsPadding()
                .padding(
                    start = Dimens.ScreenPadding,
                    end = Dimens.ScreenPadding,
                    top = Dimens.HeaderPaddingTop,
                    bottom = Dimens.HeaderDetailPaddingBottom,
                ),
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onBack),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.HeaderButtonGap),
            ) {
                Icon(
                    imageVector = AppIcons.ChevronLeft,
                    contentDescription = null,
                    tint = AppColors.OnPrimary,
                    modifier = Modifier.size(Dimens.IconHeader),
                )
                Text(
                    text = stringResource(R.string.action_back),
                    style = AppType.HeaderBack,
                    color = AppColors.OnPrimary,
                )
            }

            Box(Modifier.height(Dimens.SectionLabelGapWide))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.ChipRadius))
                    .background(colors.bg)
                    .padding(
                        horizontal = Dimens.ChipLargePaddingH,
                        vertical = Dimens.ChipLargePaddingV,
                    ),
            ) {
                Text(
                    text = event.type.label(),
                    style = AppType.ChipLarge,
                    color = colors.text,
                )
            }

            Box(Modifier.height(Dimens.SectionLabelGap))

            Text(
                text = event.title,
                style = AppType.DetailTitle,
                color = AppColors.OnPrimary,
            )
        }

        // ─── Вміст ────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.PanelGap),
        ) {
            Panel {
                InfoRow(
                    icon = AppIcons.Clock,
                    label = stringResource(R.string.info_datetime),
                    value = event.dateTimeText(),
                )
                PanelDivider()
                InfoRow(
                    icon = AppIcons.Pin,
                    label = stringResource(R.string.info_location),
                    value = event.locationText(),
                )
                PanelDivider()
                InfoRow(
                    icon = AppIcons.User,
                    label = stringResource(R.string.info_organizer),
                    value = event.organizerText(),
                )
                if (!event.targetAudience.isNullOrBlank()) {
                    PanelDivider()
                    InfoRow(
                        icon = AppIcons.Users,
                        label = stringResource(R.string.info_audience),
                        value = event.targetAudience,
                    )
                }
            }

            Panel {
                SectionLabel(text = stringResource(R.string.section_description))
                Box(Modifier.height(Dimens.SectionLabelGap))
                Text(
                    text = event.descriptionText(),
                    style = AppType.Description,
                    color = AppColors.TextBody,
                )
            }

            Panel {
                ReminderPanel(
                    availability = availability,
                    offsetMinutes = reminderOffsetMinutes,
                    now = now,
                    permissionDenied = permissionDenied,
                    onToggle = { wantOn ->
                        if (!wantOn) {
                            permissionDenied = false
                            onClearReminder()
                        } else if (available != null) {
                            val offset = ReminderSchedule.defaultOffset(available.start, Instant.now())
                            // Жоден пресет не вміщається — одразу просимо власне значення.
                            if (offset == null) showCustomDialog = true else requestSetReminder(offset)
                        }
                    },
                    onSelectOffset = ::requestSetReminder,
                    onCustomClick = { showCustomDialog = true },
                )
            }

            // Посилання та код підключення — різні сутності, різні дії.
            if (event.link != null || event.meetingCode != null) {
                Panel {
                    SectionLabel(text = stringResource(R.string.section_links))
                    Box(Modifier.height(Dimens.SectionLabelGapWide))

                    event.link?.let { url ->
                        ActionRow(
                            icon = AppIcons.Globe,
                            tint = AppColors.LinkTint,
                            tintBg = AppColors.LinkTintBg,
                            title = stringResource(R.string.action_join),
                            subtitle = url,
                            trailing = AppIcons.ExternalLink,
                            onClick = { openLink(context, url) },
                        )
                    }

                    if (event.link != null && event.meetingCode != null) {
                        PanelDivider()
                    }

                    // meeting_code — це НЕ URL. Відкривати його як посилання не можна,
                    // тому єдина дія — копіювання в буфер обміну.
                    event.meetingCode?.let { code ->
                        ActionRow(
                            icon = AppIcons.Copy,
                            tint = AppColors.CodeTint,
                            tintBg = AppColors.CodeTintBg,
                            title = stringResource(R.string.meeting_code_label),
                            subtitle = code,
                            trailing = AppIcons.Copy,
                            onClick = { copyToClipboard(context, code) },
                        )
                    }
                }
            }

            // «Додати в календар» має сенс лише за наявності дати.
            if (event.date != null) {
                Panel {
                    ActionRow(
                        icon = AppIcons.PlusCircle,
                        tint = AppColors.Primary,
                        tintBg = AppColors.ChipIdleBg,
                        title = stringResource(R.string.action_add_to_calendar),
                        subtitle = null,
                        trailing = null,
                        onClick = { addToCalendar(context, event) },
                    )
                }
            }
        }
    }

    if (showCustomDialog && available != null) {
        ReminderCustomDialog(
            start = available.start,
            initialMinutes = reminderOffsetMinutes,
            onConfirm = { minutes ->
                showCustomDialog = false
                requestSetReminder(minutes)
            },
            onDismiss = { showCustomDialog = false },
        )
    }
}

/** На Android 13+ без дозволу POST_NOTIFICATIONS сповіщення не показуються. */
private fun hasNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

/** Біла картка зі скругленням 14 і м'якою тінню — Panel із макета. */
@Composable
private fun Panel(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = Dimens.PanelElevation,
                shape = RoundedCornerShape(Dimens.PanelRadius),
                ambientColor = AppColors.CardShadow,
                spotColor = AppColors.CardShadow,
            )
            .clip(RoundedCornerShape(Dimens.PanelRadius))
            .background(AppColors.Surface)
            .padding(
                horizontal = Dimens.PanelPaddingH,
                vertical = Dimens.PanelPaddingV,
            ),
        content = content,
    )
}

@Composable
private fun PanelDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.PanelDividerGap)
            .height(Dimens.Hairline)
            .background(AppColors.Divider),
    )
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.InfoRowGap),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.TextLabelIcon,
            modifier = Modifier
                .padding(top = Dimens.InfoRowIconTopOffset)
                .size(Dimens.IconInfoRow),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                style = AppType.InfoLabel,
                color = AppColors.TextMuted,
            )
            Box(Modifier.height(Dimens.InfoRowLabelGap))
            Text(
                text = value,
                style = AppType.InfoValue,
                color = AppColors.TextTitle,
            )
        }
    }
}

/** Рядок дії: кольорова плитка з іконкою, назва, підпис і значок праворуч. */
@Composable
private fun ActionRow(
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    tintBg: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String?,
    trailing: ImageVector?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimens.LinkRowPaddingV),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.LinkRowGap),
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.LinkIconBox)
                .clip(RoundedCornerShape(Dimens.LinkIconBoxRadius))
                .background(tintBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(Dimens.IconLinkBox),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppType.RowTitle,
                color = AppColors.TextTitle,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = AppType.RowSubtitle,
                    color = tint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Icon(
                imageVector = trailing,
                contentDescription = null,
                tint = AppColors.TextMuted,
                modifier = Modifier.size(Dimens.IconLinkExternal),
            )
        }
    }
}

/** Відкриває посилання в Custom Tab. Схему вже перевірив мапер. */
private fun openLink(context: Context, url: String) {
    try {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, Uri.parse(url))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.error_cannot_open_link, Toast.LENGTH_SHORT).show()
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(
        ClipData.newPlainText(context.getString(R.string.meeting_code_label), text),
    )
    // Починаючи з Android 13 система показує власне повідомлення про копіювання,
    // тож дублювати його своїм Toast не треба.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(context, R.string.toast_copied, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Додає подію в календар пристрою через ACTION_INSERT.
 *
 * Час беремо в часовому поясі пристрою: `date` за контрактом — локальна
 * календарна дата події, а користувач і подія перебувають в одному поясі.
 * Якщо часу немає, створюємо подію на весь день, а не о 00:00.
 */
private fun addToCalendar(context: Context, event: Event) {
    val date = event.date ?: return
    val zone = ZoneId.systemDefault()
    val isAllDay = event.time == null
    val startMillis = date.atTime(event.time ?: LocalTime.MIDNIGHT)
        .atZone(zone)
        .toInstant()
        .toEpochMilli()

    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, event.title)
        event.location?.let { putExtra(CalendarContract.Events.EVENT_LOCATION, it) }
        event.description?.let { putExtra(CalendarContract.Events.DESCRIPTION, it) }
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, isAllDay)
        if (!isAllDay) {
            putExtra(
                CalendarContract.EXTRA_EVENT_END_TIME,
                startMillis + DEFAULT_DURATION_MILLIS,
            )
        }
    }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.error_no_calendar_app, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Тривалість за замовчуванням: в API немає поля часу завершення,
 * тому беремо одну годину, а не вигадуємо кінець події.
 */
private const val DEFAULT_DURATION_MILLIS = 60L * 60L * 1000L
