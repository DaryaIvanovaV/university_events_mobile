package com.KSU.EventsParser.domain.model

/**
 * Тип події з поля `event_type`.
 *
 * Перелік на сервері поповнюється з часом (`workshop` і `hackathon` додали пізніше
 * за решту), тому невідоме значення НІКОЛИ не кидає виняток, а перетворюється
 * на [OTHER]. Падіння клієнта через новий тип події на сервері неприпустиме.
 */
enum class EventType(val apiValue: String) {
    CONFERENCE("conference"),
    LECTURE("lecture"),
    WEBINAR("webinar"),
    SEMINAR("seminar"),
    WORKSHOP("workshop"),
    HACKATHON("hackathon"),
    DEADLINE("deadline"),
    EXAM("exam"),
    CONSULTATION("consultation"),
    OTHER("other");

    companion object {
        private val byApiValue: Map<String, EventType> = entries.associateBy { it.apiValue }

        /**
         * Відображає сире значення з API на [EventType].
         * Невідомий рядок, порожній рядок або `null` → [OTHER].
         */
        fun fromApi(raw: String?): EventType =
            raw?.trim()?.lowercase()?.let { byApiValue[it] } ?: OTHER
    }
}
