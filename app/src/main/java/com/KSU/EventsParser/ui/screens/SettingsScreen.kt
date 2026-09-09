package com.KSU.EventsParser.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.KSU.EventsParser.R
import com.KSU.EventsParser.core.locale.AppLocale
import com.KSU.EventsParser.data.repository.DeviceRepository
import com.KSU.EventsParser.ui.components.AppHeader
import com.KSU.EventsParser.ui.components.AppIcons
import com.KSU.EventsParser.ui.components.AppToggle
import com.KSU.EventsParser.ui.components.SectionLabel
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.AppType
import com.KSU.EventsParser.ui.theme.Dimens
import kotlinx.coroutines.launch

/**
 * Вкладка «Налаштування».
 *
 * У макеті на цьому місці був екран «Alerts» зі списком нагадувань по кожній
 * події. Такої функції немає: планувальника нагадувань не існує, сервер шле
 * рівно один push у момент підтвердження події. Тому тут живе те, що справді
 * налаштовується: глобальний перемикач сповіщень і мова інтерфейсу.
 */
@Composable
fun SettingsScreen(
    deviceRepository: DeviceRepository,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val notificationsEnabled by deviceRepository.enabled.collectAsState(initial = false)
    var selectedLanguage by remember { mutableStateOf(AppLocale.selected(context)) }
    var permissionDenied by remember { mutableStateOf(false) }

    fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    // Дозвіл могли відкликати в системних налаштуваннях, поки нас не було.
    LaunchedEffect(notificationsEnabled) {
        if (notificationsEnabled && !hasNotificationPermission()) {
            permissionDenied = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionDenied = !granted
        if (granted) {
            scope.launch { deviceRepository.enableNotifications() }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(AppColors.Background)) {
        AppHeader(title = stringResource(R.string.title_settings))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.PanelGap),
        ) {
            // ─── Сповіщення ───────────────────────────────────────────────────
            SettingsPanel {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.LinkRowGap),
                ) {
                    Icon(
                        imageVector = AppIcons.Bell,
                        contentDescription = null,
                        tint = if (notificationsEnabled) {
                            AppColors.Accent
                        } else {
                            AppColors.TextMuted
                        },
                        modifier = Modifier.size(Dimens.IconBellDetail),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_notifications),
                            style = AppType.RowTitle,
                            color = AppColors.TextTitle,
                        )
                        Text(
                            text = stringResource(
                                if (notificationsEnabled) {
                                    R.string.settings_state_on
                                } else {
                                    R.string.settings_state_off
                                },
                            ),
                            style = AppType.RowSubtitle,
                            color = AppColors.TextMuted,
                        )
                    }
                    AppToggle(
                        checked = notificationsEnabled,
                        onCheckedChange = { wantEnabled ->
                            if (wantEnabled) {
                                // На Android 13+ без дозволу сповіщення просто
                                // не показуються — тому спершу питаємо дозвіл.
                                if (hasNotificationPermission()) {
                                    permissionDenied = false
                                    scope.launch { deviceRepository.enableNotifications() }
                                } else {
                                    permissionLauncher.launch(
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    )
                                }
                            } else {
                                permissionDenied = false
                                scope.launch { deviceRepository.disableNotifications() }
                            }
                        },
                    )
                }

                Text(
                    text = stringResource(R.string.settings_notifications_desc),
                    style = AppType.RowSubtitle,
                    color = AppColors.TextMuted,
                    modifier = Modifier.padding(top = Dimens.CardDateGap),
                )

                if (permissionDenied) {
                    Text(
                        text = stringResource(R.string.notifications_permission_denied),
                        style = AppType.RowSubtitle,
                        color = AppColors.CodeTint,
                        modifier = Modifier.padding(top = Dimens.CardPlaceGap),
                    )
                }
            }

            // ─── Мова ─────────────────────────────────────────────────────────
            SettingsPanel {
                SectionLabel(text = stringResource(R.string.settings_language))
                Box(Modifier.height(Dimens.SectionLabelGapWide))

                AppLocale.options.forEachIndexed { index, tag ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.CardDateGap)
                                .height(Dimens.Hairline)
                                .background(AppColors.DividerLight),
                        )
                    }
                    LanguageRow(
                        label = stringResource(languageLabelRes(tag)),
                        selected = tag == selectedLanguage,
                        onClick = {
                            if (tag != selectedLanguage) {
                                selectedLanguage = tag
                                // На API < 33 локаль застосовується лише після
                                // перестворення Activity — система цього не робить.
                                val needsRecreate = AppLocale.apply(context, tag)
                                if (needsRecreate) {
                                    context.findActivity()?.recreate()
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

/** Біла картка зі скругленням 14 і м'якою тінню. */
@Composable
private fun SettingsPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
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

/**
 * Знаходить Activity в ланцюжку контекстів.
 *
 * Пряме приведення `context as? Activity` тут ненадійне: MainActivity підмінює
 * базовий контекст у attachBaseContext (див. AppLocale.wrap), тому LocalContext
 * може виявитися ContextWrapper. Мовчазний null означав би, що мова
 * не перемкнеться взагалі, і причину було б не видно.
 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun languageLabelRes(tag: String): Int = when (tag) {
    AppLocale.ENGLISH -> R.string.language_en
    else -> R.string.language_uk
}

@Composable
private fun LanguageRow(
    label: String,
    selected: Boolean,
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
        Text(
            text = label,
            style = AppType.RowTitle,
            color = AppColors.TextTitle,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = AppIcons.Check,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(Dimens.IconSmall),
            )
        }
    }
}
