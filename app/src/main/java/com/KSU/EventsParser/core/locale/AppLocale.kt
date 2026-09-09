package com.KSU.EventsParser.core.locale

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Перемикач мови інтерфейсу.
 *
 * Мов у застосунку рівно дві: українська (типові ресурси `res/values/`)
 * і англійська (`res/values-en/`). Варіанта «системна» в перемикачі НЕМАЄ
 * навмисно: він створював би хибне враження, ніби мов більше. Якщо система
 * стоїть, скажімо, польською, підтримати її ми все одно не можемо — Android
 * візьме типові ресурси, тобто українську. Тому користувач завжди обирає
 * конкретну мову, а не абстрактну «системну».
 *
 * Реалізовано двома шляхами, бо системний API з'явився лише в Android 13:
 *  • API 33+ — штатний [LocaleManager]. Система сама зберігає вибір, сама
 *    перезапускає Activity і показує застосунок у системних налаштуваннях мов
 *    (для цього в маніфесті вказано android:localeConfig);
 *  • API 26…32 — зберігаємо тег мови самі й підмінюємо Configuration
 *    в attachBaseContext.
 *
 * Навмисно використано SharedPreferences, а не DataStore: attachBaseContext
 * викликається синхронно до створення будь-якої корутини, і блокуватися там
 * на асинхронному читанні DataStore не можна.
 */
object AppLocale {

    const val UKRAINIAN = "uk"
    const val ENGLISH = "en"

    /** Порядок для перемикача в налаштуваннях. Українська — основна мова. */
    val options: List<String> = listOf(UKRAINIAN, ENGLISH)

    private const val PREFS_NAME = "app_locale"
    private const val KEY_LANGUAGE_TAG = "language_tag"

    /**
     * Чи є в системі штатний LocaleManager (Android 13+).
     * Винесено в одне місце: гілок дві, і вони мають перемикатися разом.
     */
    private val hasSystemLocaleManager: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /**
     * Мова, якою інтерфейс показується просто зараз.
     *
     * Якщо користувач ще нічого не обирав, повертаємо не порожній рядок,
     * а ту мову, якою екран фактично намальовано, — щоб у перемикачі завжди
     * стояла позначка навпроти реального стану.
     */
    fun selected(context: Context): String {
        val explicit = explicitTag(context)
        return if (!explicit.isNullOrEmpty()) explicit else effective(context)
    }

    /**
     * Зберігає вибір мови.
     *
     * @return true, якщо Activity треба перезапустити вручну. На API 33+
     *         система робить це сама, тому повертається false.
     */
    fun apply(context: Context, languageTag: String): Boolean =
        if (hasSystemLocaleManager) {
            context.getSystemService(LocaleManager::class.java)?.applicationLocales =
                LocaleList.forLanguageTags(languageTag)
            false
        } else {
            prefs(context).edit().putString(KEY_LANGUAGE_TAG, languageTag).apply()
            true
        }

    /**
     * Загортає базовий контекст Activity у потрібну локаль.
     * Викликати з MainActivity.attachBaseContext.
     */
    fun wrap(base: Context): Context {
        // На API 33+ локаль застосовує сама система — втручатися не треба.
        if (hasSystemLocaleManager) return base

        val tag = explicitTag(base)
        if (tag.isNullOrEmpty()) return base

        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return base.createConfigurationContext(configuration)
    }

    /** Явний вибір користувача або `null`, якщо він ще нічого не обирав. */
    private fun explicitTag(context: Context): String? =
        if (hasSystemLocaleManager) {
            context.getSystemService(LocaleManager::class.java)
                ?.applicationLocales
                ?.takeIf { !it.isEmpty }
                ?.get(0)
                ?.language
        } else {
            prefs(context).getString(KEY_LANGUAGE_TAG, null)
        }

    /**
     * Мова, якою Android фактично малює ресурси.
     * Англійська — лише для `values-en`; будь-що інше означає типові ресурси,
     * тобто українську.
     */
    private fun effective(context: Context): String =
        if (context.resources.configuration.locales[0].language == ENGLISH) ENGLISH else UKRAINIAN

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
