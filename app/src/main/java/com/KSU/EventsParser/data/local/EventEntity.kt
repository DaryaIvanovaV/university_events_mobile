package com.KSU.EventsParser.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Рядок кешу подій.
 *
 * Свідомо зберігаємо поля СИРИМИ рядками, точно як їх надіслав сервер, а не
 * розібраними датами. Причини дві:
 *  1) значення на кшталт time = "11:00-16:00" не влазять у LocalTime, але їх
 *     треба показати користувачеві, а не втратити під час запису в кеш;
 *  2) кеш лишається дослівною копією відповіді сервера, тож розбір можна
 *     виправити в мапері без міграції бази.
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: Int,
    val eventTitle: String?,
    val date: String?,
    val time: String?,
    val location: String?,
    val organizer: String?,
    val targetAudience: String?,
    val eventType: String?,
    val description: String?,
    val language: String?,
    val link: String?,
    val meetingCode: String?,
    val status: String?,
    val createdAt: String?,
    val updatedAt: String?,
)
