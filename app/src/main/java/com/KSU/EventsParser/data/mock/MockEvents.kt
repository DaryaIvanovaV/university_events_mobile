package com.KSU.EventsParser.data.mock

import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.domain.model.EventType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Тестові дані для фази 1 (до підключення справжнього API).
 *
 * Набір навмисно складається з «незручних» випадків, а не з гарних записів:
 * якщо інтерфейс переживає їх, він перевіряє прод. Тут є подія без дати,
 * без часу, з нерозбірливим часом, з кодом підключення без посилання,
 * з невідомим типом і з дуже довгою назвою.
 *
 * Дати прив'язані до сьогоднішнього дня, щоб у календарі завжди було що показати.
 */
object MockEvents {

    private val today: LocalDate = LocalDate.now()
    private val createdAt: Instant = Instant.parse("2026-09-01T19:10:13.108393Z")
    private val updatedAt: Instant = Instant.parse("2026-09-01T19:45:51.913159Z")

    val events: List<Event> = listOf(
        // Звичайна повна подія — базовий випадок.
        Event(
            id = 1,
            title = "Міжнародна конференція з машинного навчання",
            date = today.plusDays(2),
            dateRaw = today.plusDays(2).toString(),
            time = LocalTime.of(9, 0),
            timeRaw = "09:00",
            location = "Велика конференц-зала, корпус А",
            organizer = "професор кафедри інформатики",
            targetAudience = "3 та 4 курси, аспіранти",
            type = EventType.CONFERENCE,
            description = "Щорічна конференція факультету, присвячена останнім " +
                "досягненням у галузі штучного інтелекту. Доповіді студентів, " +
                "аспірантів і запрошених спікерів.",
            language = "uk",
            link = "https://ml-conf.ksu.edu.ua",
            meetingCode = null,
            createdAt = createdAt,
            updatedAt = updatedAt,
        ),

        // ПАСТКА: код підключення Є, а посилання НЕМАЄ. Нормальна ситуація за контрактом.
        // Код не можна відкривати як URL — лише копіювати.
        Event(
            id = 2,
            title = "Вступ до машинного навчання: від лінійної регресії до перших нейромереж",
            date = today.plusDays(5),
            dateRaw = today.plusDays(5).toString(),
            time = LocalTime.of(12, 0),
            timeRaw = "12:00",
            location = "Онлайн (Zoom)",
            organizer = "керівник напряму аналітики даних у великій продуктовій компанії",
            targetAudience = "2 та 3 курси, усі охочі",
            type = EventType.WEBINAR,
            description = "Підключення через Zoom, ідентифікатор конференції та код " +
                "доступу вказано нижче. Окремого посилання організатор не надав.",
            language = "uk",
            link = null,
            meetingCode = "845 2371 9004, код 316742",
            createdAt = createdAt,
            updatedAt = updatedAt,
        ),

        // ПАСТКА: часу немає зовсім.
        Event(
            id = 3,
            title = "День відкритих дверей факультету",
            date = today.plusDays(7),
            dateRaw = today.plusDays(7).toString(),
            time = null,
            timeRaw = null,
            location = "Головний хол університету",
            organizer = "деканат",
            targetAudience = "абітурієнти та батьки",
            type = EventType.CONSULTATION,
            description = "Презентація факультету для абітурієнтів. Екскурсія лабораторіями, " +
                "зустріч із викладачами та студентами.",
            language = "uk",
            link = "https://ksu.edu.ua/admissions",
            meetingCode = null,
            createdAt = createdAt,
            updatedAt = updatedAt,
        ),

        // ПАСТКА: час не у форматі HH:MM. Сервер не має валідатора цього поля,
        // тому такі значення просочуються. Показуємо сирий рядок як є.
        Event(
            id = 4,
            title = "Хакатон DevFest: 48 годин розробки",
            date = today.plusDays(9),
            dateRaw = today.plusDays(9).toString(),
            time = null,
            timeRaw = "11:00-16:00",
            location = "Лабораторія розробки, корпус Б",
            organizer = "оргкомітет",
            targetAudience = "усі курси",
            type = EventType.HACKATHON,
            description = "48-годинний хакатон для студентів усіх курсів. " +
                "Теми: освітні технології, медичні дані, відкриті дані міста.",
            language = "uk",
            link = "https://devfest.ksu.edu.ua",
            meetingCode = "код команди: 4471",
            createdAt = createdAt,
            updatedAt = updatedAt,
        ),

        // ПАСТКА: дуже довга назва — перевірка переносів і обрізання.
        Event(
            id = 5,
            title = "Науково-практичний семінар з теоретичної інформатики, обчислювальної " +
                "складності та застосування формальних методів верифікації у розподілених " +
                "системах реального часу для студентів старших курсів",
            date = today.plusDays(12),
            dateRaw = today.plusDays(12).toString(),
            time = LocalTime.of(14, 30),
            timeRaw = "14:30",
            location = "Ауд. 312, корпус В",
            organizer = "докторка технічних наук, завідувачка кафедри",
            targetAudience = "4 курс, магістри",
            type = EventType.SEMINAR,
            description = "Практичний семінар з основ формальної верифікації.",
            language = "uk",
            link = null,
            meetingCode = null,
            createdAt = createdAt,
            updatedAt = updatedAt,
        ),

        // ПАСТКА: невідомий серверу тип. У домені він уже став OTHER
        // (EventType.fromApi("flashmob") -> OTHER) і має показатися нейтрально-сірим.
        Event(
            id = 6,
            title = "Флешмоб до Дня студента",
            date = today.plusDays(14),
            dateRaw = today.plusDays(14).toString(),
            time = LocalTime.of(13, 0),
            timeRaw = "13:00",
            location = "Подвір'я університету",
            organizer = "студентське самоврядування",
            targetAudience = null,
            type = EventType.fromApi("flashmob"),
            description = null,
            language = "uk",
            link = null,
            meetingCode = null,
            createdAt = createdAt,
            updatedAt = updatedAt,
        ),

        Event(
            id = 7,
            title = "Захист курсових робіт — 2 курс",
            date = today.plusDays(20),
            dateRaw = today.plusDays(20).toString(),
            time = LocalTime.of(9, 0),
            timeRaw = "09:00",
            location = "Ауд. 101–104, корпус А",
            organizer = "комісія кафедри",
            targetAudience = "2 курс",
            type = EventType.EXAM,
            description = "Публічний захист курсових робіт з дисципліни " +
                "«Алгоритми та структури даних».",
            language = "uk",
            link = null,
            meetingCode = null,
            createdAt = createdAt,
            updatedAt = updatedAt,
        ),

        Event(
            id = 8,
            title = "Дедлайн подання тез до збірника",
            date = today.plusDays(20),
            dateRaw = today.plusDays(20).toString(),
            time = LocalTime.of(23, 59),
            timeRaw = "23:59",
            location = null,
            organizer = "редколегія збірника",
            targetAudience = "аспіранти",
            type = EventType.DEADLINE,
            description = "Останній день подання тез.",
            language = "uk",
            link = "https://ksu.edu.ua/abstracts",
            meetingCode = null,
            createdAt = createdAt,
            updatedAt = updatedAt,
        ),

        Event(
            id = 9,
            title = "Майстер-клас: Docker та контейнеризація",
            date = today.plusMonths(1).withDayOfMonth(8),
            dateRaw = today.plusMonths(1).withDayOfMonth(8).toString(),
            time = LocalTime.of(16, 0),
            timeRaw = "16:00",
            location = "Комп'ютерний клас 108 першого корпусу",
            organizer = "інженер з інфраструктури",
            targetAudience = "3 курс, усі охочі",
            type = EventType.WORKSHOP,
            description = "Практичний майстер-клас для початківців.",
            language = "uk",
            link = "https://ksu.edu.ua/docker",
            meetingCode = null,
            createdAt = createdAt,
            updatedAt = updatedAt,
        ),

        Event(
            id = 10,
            title = "Відкрита лекція: історія обчислювальної техніки",
            date = today.plusMonths(1).withDayOfMonth(15),
            dateRaw = today.plusMonths(1).withDayOfMonth(15).toString(),
            time = LocalTime.of(16, 0),
            timeRaw = "16:00",
            location = "Ауд. 201, корпус А",
            organizer = "професор кафедри",
            targetAudience = "1 курс, абітурієнти",
            type = EventType.LECTURE,
            description = "Відкрита лекція для першокурсників та абітурієнтів.",
            language = "uk",
            link = null,
            meetingCode = null,
            createdAt = createdAt,
            updatedAt = updatedAt,
        ),

        // ПАСТКА: дати НЕМАЄ. Такі події ніколи не потраплять у сітку календаря
        // і не приходять у push. Вони мають лишатися доступними — секція «Без дати».
        Event(
            id = 11,
            title = "Набір до наукового гуртка з комп'ютерного зору",
            date = null,
            dateRaw = null,
            time = null,
            timeRaw = null,
            location = "Лабораторія 305",
            organizer = "керівник гуртка",
            targetAudience = "2–4 курси",
            type = EventType.SEMINAR,
            description = "Дату першої зустрічі буде оголошено окремо.",
            language = "uk",
            link = null,
            meetingCode = null,
            createdAt = createdAt,
            updatedAt = updatedAt,
        ),

        // ПАСТКА: заповнені лише обов'язкові поля. Усе інше — null.
        Event(
            id = 12,
            title = "Консультація перед екзаменом",
            date = null,
            dateRaw = null,
            time = null,
            timeRaw = null,
            location = null,
            organizer = null,
            targetAudience = null,
            type = EventType.CONSULTATION,
            description = null,
            language = null,
            link = null,
            meetingCode = null,
            createdAt = createdAt,
            updatedAt = updatedAt,
        ),
    )
}
