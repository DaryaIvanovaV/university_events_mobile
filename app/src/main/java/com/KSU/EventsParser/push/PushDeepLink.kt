package com.KSU.EventsParser.push

import android.content.Intent

/**
 * Розбір переходу на подію з push-сповіщення.
 *
 * **Усі значення в блоці `data` — рядки**, включно з `event_id`: FCM не вміє
 * передавати числа. Тому парсимо рядок, а не читаємо getIntExtra.
 * Поле `date` приходить порожнім рядком, якщо дати в події немає, — саме тому
 * порожні значення тут відкидаються.
 *
 * Оскільки в payload є блок `notification`, у згорнутому застосунку
 * `onMessageReceived` НЕ викликається: сповіщення показує система, а `data`
 * потрапляє в extras Intent'а запуску. Тому шляхів два, і обробити треба обидва.
 */
object PushDeepLink {

    const val EXTRA_EVENT_ID = "event_id"
    const val EXTRA_EVENT_TYPE = "event_type"
    const val EXTRA_DATE = "date"

    /** `null`, якщо Intent не з push або ідентифікатор непридатний. */
    fun eventIdFrom(intent: Intent?): Int? {
        val raw = intent?.extras?.getString(EXTRA_EVENT_ID)?.trim()
        if (raw.isNullOrEmpty()) return null
        return raw.toIntOrNull()
    }
}
