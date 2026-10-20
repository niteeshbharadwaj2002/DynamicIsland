/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.util

import android.app.Notification
import android.service.notification.StatusBarNotification

enum class TurnDirection {
    LEFT,
    RIGHT,
    SLIGHT_LEFT,
    SLIGHT_RIGHT,
    U_TURN,
    STRAIGHT,
    ROUNDABOUT,
    DESTINATION
}

data class NavigationInfo(
    val packageName: String,
    val distanceToTurnText: String,
    val maneuverTitle: String,
    val subText: String? = null,
    val turnDirection: TurnDirection = TurnDirection.STRAIGHT
)

/** Whether a ride is still coming to the rider or already taking them to the drop. */
enum class RidePhase { Pickup, Drop }

/** A live ride from Uber, Ola or Rapido (Rule_Book 4.14), read from the ride app's BigTextStyle text. */
data class RideInfo(
    val packageName: String,
    /** "Uber", "Ola", "Rapido". */
    val provider: String,
    val driverName: String?,
    /** Make and model, e.g. "White Swift Dzire", when the app names it. */
    val vehicle: String?,
    val licencePlate: String?,
    /** Minutes to the pickup ([RidePhase.Pickup]) or the drop ([RidePhase.Drop]); 0 once the driver is there. */
    val etaMinutes: Int?,
    val phase: RidePhase,
    /** The app's own headline, for the card. */
    val statusText: String
) {
    /** The Medium/expanded-small badge: "Pickup in 3m", "Drop in 12m", "Driver arrived". */
    val etaLabel: String
        get() = when {
            etaMinutes == 0 && phase == RidePhase.Pickup -> "Driver arrived"
            etaMinutes == null -> if (phase == RidePhase.Pickup) "Pickup" else "On trip"
            phase == RidePhase.Pickup -> "Pickup in ${etaMinutes}m"
            else -> "Drop in ${etaMinutes}m"
        }

    /** The compact pill badge: "3 min", "Here", or the phase. */
    val etaBadge: String
        get() = when {
            etaMinutes == 0 && phase == RidePhase.Pickup -> "Here"
            etaMinutes != null -> "$etaMinutes min"
            phase == RidePhase.Pickup -> "Pickup"
            else -> "On trip"
        }
}

/** What the expanded navigation card shows, split into its rows. */
data class NavigationCardText(
    /** "In 300 m" for the badge. */
    val distance: String,
    /** The maneuver itself ("Turn left onto MG Road"), without a leading distance. */
    val instruction: String,
    /** Extra instruction text such as a street or lane hint; empty when there is none. */
    val detail: String,
    /** Trip stats such as "24 min • 8.5 km" or "ETA 18:42"; empty when there are none. */
    val eta: String
)

object NavigationParser {

    private val RIDE_PROVIDERS = mapOf(
        "com.ubercab" to "Uber",
        "com.olacabs.customer" to "Ola",
        "com.rapido.passenger" to "Rapido"
    )

    // Indian plates as ride apps print them: "KA 01 AB 1234", "KA01AB1234", "DL 3C AB 1234", "MH-12-DE-1433".
    private val INDIAN_PLATE_REGEX = Regex("""\b[A-Z]{2}[ -]?\d{1,2}[A-Z]?[ -]?[A-Z]{1,3}[ -]?\d{3,4}\b""")
    // Elsewhere the app labels it: "Plate: 7ABC123", "licence plate 8XYZ901".
    private val LABELLED_PLATE_REGEX = Regex("""(?i:licen[cs]e plate|plate(?: no\.?| number)?)\s*[:#]?\s*([A-Z0-9]*\d[A-Z0-9]*(?:[ -][A-Z0-9]+)?)\b""")
    private val ETA_MINUTES_REGEX = Regex("""(?i)\b(\d{1,3})\s*(?:min|mins|minutes)\b""")
    private val NAME_BEFORE_VERB_REGEX = Regex(
        """\b([A-Z][a-z]+(?: [A-Z][a-z]+)?)(?: \(\d(?:\.\d)?\s*★?\))?,? (?:is|has) (?:arriving|on (?:the|his|her) way|here|waiting|nearby|en route|arrived|reached)"""
    )
    private val NAME_AFTER_ROLE_REGEX = Regex("""\b(?i:captain|driver|partner)[:,]?\s+([A-Z][a-z]+(?: [A-Z][a-z]+)?)""")
    private val VEHICLE_IN_REGEX = Regex("""\bin (?:a|an|the|your) ((?:[A-Z][\w-]*)(?: [A-Z0-9][\w-]*){0,3})""")
    private val VEHICLE_LABEL_REGEX = Regex("""\b(?i:bike|car|auto|cab|vehicle)\s*[:\-]\s*([A-Za-z][\w ]{2,24}?)\s*(?:,|•|·|\||$)""")
    private val VEHICLE_PARENS_REGEX = Regex("""\(([A-Z][A-Za-z0-9 ]{2,24})\)""")

    private val NOT_A_NAME = setOf(
        "Your", "The", "Driver", "Captain", "Ola", "Uber", "Rapido", "Cab", "Auto", "Bike", "Ride", "Trip", "Your Ola",
        "Your Uber", "Your Rapido", "Your Captain", "Your Driver", "Your Ride", "Your Cab", "Your Auto", "Your Bike"
    )
    private val RIDE_PHRASES = listOf(
        "driver", "captain", "arriving", "on the way", "on his way", "on her way", "pickup", "pick-up", "pick up",
        "drop", "trip", "ride", "otp", "arrived", "en route", "heading to", "is here", "reach", "waiting for you"
    )
    private val DROP_PHRASES = listOf(
        "drop", "destination", "heading to", "on trip", "trip to", "en route to", "reach your", "reaching", "enjoy your ride"
    )
    private val PROMO_PHRASES = listOf("% off", "offer", "discount", "coupon", "cashback", "promo", "save up to", "flat ")

    fun isRideApp(packageName: String): Boolean = packageName in RIDE_PROVIDERS

    /** Reads a live ride from a ride app's notification, preferring its BigTextStyle text. */
    fun parseRide(sbn: StatusBarNotification): RideInfo? {
        if (!isRideApp(sbn.packageName)) return null
        val extras = sbn.notification?.extras ?: return null
        return parseRide(
            packageName = sbn.packageName,
            title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        )
    }

    /**
     * The pure half of [parseRide]: a ride needs a licence plate, or a ride phrase ("arriving", "captain",
     * "drop", ...) together with an ETA or a driver's name. Promotions ("20% off your next ride") are not rides
     * unless a plate is in them.
     */
    fun parseRide(packageName: String, title: String?, text: String?, bigText: String?): RideInfo? {
        val provider = RIDE_PROVIDERS[packageName] ?: return null
        val headline = title.orEmpty().trim()
        // BigTextStyle carries the full ride card; the plain text is often its first line.
        val body = listOfNotNull(bigText, text).map { it.trim() }.filter { it.isNotEmpty() }.distinct()
            .joinToString(" • ")
        val content = "$headline • $body"
        if (headline.isEmpty() && body.isEmpty()) return null
        val lower = content.lowercase()

        val plate = INDIAN_PLATE_REGEX.find(content)?.value?.replace('-', ' ')
            ?: LABELLED_PLATE_REGEX.find(content)?.groupValues?.get(1)?.trim()
        val hasRidePhrase = RIDE_PHRASES.any { lower.contains(it) }
        if (plate == null && PROMO_PHRASES.any { lower.contains(it) }) return null

        val arrived = listOf("has arrived", "is here", "arrived at", "waiting for you", "reached your pickup", "reached pickup")
            .any { lower.contains(it) }
        val eta = if (arrived) 0 else ETA_MINUTES_REGEX.find(content)?.groupValues?.get(1)?.toIntOrNull()
        val driver = driverName(content)
        if (plate == null && !(hasRidePhrase && (eta != null || driver != null))) return null

        val phase = if (!arrived && DROP_PHRASES.any { lower.contains(it) }) RidePhase.Drop else RidePhase.Pickup
        return RideInfo(
            packageName = packageName,
            provider = provider,
            driverName = driver,
            vehicle = vehicle(content, plate),
            licencePlate = plate,
            etaMinutes = eta,
            phase = phase,
            statusText = headline.ifEmpty { body.substringBefore(" • ") }
        )
    }

    private fun driverName(content: String): String? {
        val candidates = NAME_BEFORE_VERB_REGEX.findAll(content).map { it.groupValues[1] } +
            NAME_AFTER_ROLE_REGEX.findAll(content).map { it.groupValues[1] }
        return candidates.firstOrNull { it !in NOT_A_NAME && it.split(' ').none { word -> word in NOT_A_NAME } }
    }

    private fun vehicle(content: String, plate: String?): String? {
        val raw = VEHICLE_LABEL_REGEX.find(content)?.groupValues?.get(1)
            ?: VEHICLE_IN_REGEX.find(content)?.groupValues?.get(1)
            ?: VEHICLE_PARENS_REGEX.find(content)?.groupValues?.get(1)
            ?: return null
        // The plate sometimes follows the model in the same run of words ("Swift Dzire KA01AB1234").
        val cleaned = raw.trim().let { v -> if (plate != null) v.replace(plate, "").trim() else v }
            .trimEnd(',', '.', '-').trim()
        return cleaned.takeIf { it.length >= 3 && INDIAN_PLATE_REGEX.find(it) == null }
    }


    fun isNavigationApp(packageName: String): Boolean = AppPackageCatalog.isNavigationApp(packageName)

    fun parse(sbn: StatusBarNotification): NavigationInfo? {
        val packageName = sbn.packageName

        // Delivery and ride apps track an order or a trip; they never give turn-by-turn guidance, even when one of
        // them tags its notification with the navigation category and a distance ("Your rider is 500 m away").
        if (AppPackageCatalog.isLiveActivityApp(packageName)) return null

        val notification = sbn.notification ?: return null
        val isCategoryNav = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            notification.category == Notification.CATEGORY_NAVIGATION
        } else {
            notification.category == "navigation"
        }
        val knownApp = isNavigationApp(packageName)

        if (!isCategoryNav && !knownApp) {
            return null
        }

        // A map app also posts ordinary notifications (Maps' "Your September Timeline", a saved-place tip, a traffic
        // alert).
        if (!isCategoryNav) {
            val running = (notification.flags and (Notification.FLAG_ONGOING_EVENT or Notification.FLAG_FOREGROUND_SERVICE)) != 0
            if (!running) return null
        }

        val extras = notification.extras ?: return null
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (extras.getCharSequence(Notification.EXTRA_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT))?.toString().orEmpty()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        if (title.isBlank() && text.isBlank()) return null

        val combinedContent = "$title $text".lowercase()

        // Exclude OEM screen recording, voice recording, calls, and hotspot notifications from being hijacked by Navigation
        if (OemDeviceRules.isNonNavigationContent(packageName, combinedContent)) {
            return null
        }

        val leadingDist = NavigationLocale.LEADING_DISTANCE.find(title.trim())
        val anyDist = if (leadingDist == null) NavigationLocale.ANY_DISTANCE.find("$title $text") else null
        val distanceMatch = leadingDist ?: anyDist
        val hasTurnDirection = parseTurnDirection(combinedContent) != TurnDirection.STRAIGHT
        val hasManeuverWording = hasTurnDirection || NavigationLocale.MANEUVER_WORDING.containsMatchIn(combinedContent)
        val hasTripStat = NavigationLocale.TRIP_STAT.containsMatchIn(combinedContent)

        if (knownApp) {
            // A known map app that categorises the notification as navigation is trusted.
            if (!isCategoryNav && !hasManeuverWording && distanceMatch == null && !hasTripStat) return null
        } else {
            // Any other app only gets here through the navigation category, which delivery, ride and tracking apps
            // use too.
            if (!hasManeuverWording) return null
            if (NavigationLocale.DELIVERY_OR_RIDE_WORDING.containsMatchIn(combinedContent)) return null
        }

        val distanceText = distanceMatch?.value ?: "In 200 m"

        val turnDirection = parseTurnDirection(combinedContent)

        val maneuverTitle = when {
            title.isNotBlank() && !title.startsWith(distanceText, ignoreCase = true) -> title
            text.isNotBlank() -> text
            else -> "Head straight"
        }

        val formattedSubText = when {
            subText != null && subText.isNotBlank() -> subText
            text.isNotBlank() && text != maneuverTitle -> text
            else -> "Turn-by-turn navigation"
        }

        return NavigationInfo(
            packageName = packageName,
            distanceToTurnText = distanceText,
            maneuverTitle = maneuverTitle,
            subText = formattedSubText,
            turnDirection = turnDirection
        )
    }

    private val ENGLISH_LEFT_WORD = Regex("\\b(turn|go|head|take)\\s+left\\b")
    private val ENGLISH_RIGHT_WORD = Regex("\\b(turn|go|head|take)\\s+right\\b")
    private val LEFT_WORD = Regex("\\bleft\\b")
    private val RIGHT_WORD = Regex("\\bright\\b")

    private val U_TURN = NavigationLocale.anyOf(NavigationLocale.U_TURN)
    private val SLIGHT_LEFT = NavigationLocale.anyOf(NavigationLocale.SLIGHT_LEFT)
    private val SLIGHT_RIGHT = NavigationLocale.anyOf(NavigationLocale.SLIGHT_RIGHT)
    private val LEFT = NavigationLocale.anyOf(NavigationLocale.LEFT)
    private val RIGHT = NavigationLocale.anyOf(NavigationLocale.RIGHT)
    private val ROUNDABOUT = NavigationLocale.anyOf(NavigationLocale.ROUNDABOUT)
    private val DESTINATION = NavigationLocale.anyOf(NavigationLocale.DESTINATION)

    fun parseTurnDirection(content: String): TurnDirection {
        val lower = content.lowercase()
        return when {
            U_TURN.containsMatchIn(lower) -> TurnDirection.U_TURN
            SLIGHT_LEFT.containsMatchIn(lower) -> TurnDirection.SLIGHT_LEFT
            SLIGHT_RIGHT.containsMatchIn(lower) -> TurnDirection.SLIGHT_RIGHT
            LEFT.containsMatchIn(lower) -> TurnDirection.LEFT
            RIGHT.containsMatchIn(lower) -> TurnDirection.RIGHT
            ROUNDABOUT.containsMatchIn(lower) -> TurnDirection.ROUNDABOUT
            DESTINATION.containsMatchIn(lower) -> TurnDirection.DESTINATION
            // Word-boundary direction matching only when not preceded by numbers/time words
            ENGLISH_LEFT_WORD.containsMatchIn(lower) -> TurnDirection.LEFT
            ENGLISH_RIGHT_WORD.containsMatchIn(lower) -> TurnDirection.RIGHT
            !lower.contains("left to") && !lower.contains("min left") && !lower.contains("mins left") && !lower.contains("mb left") && !lower.contains("gb left") && !lower.contains("sec left") && LEFT_WORD.containsMatchIn(lower) && (lower.contains("onto") || lower.contains("street") || lower.contains("road") || lower.contains("ave")) -> TurnDirection.LEFT
            !lower.contains("right now") && !lower.contains("all right") && RIGHT_WORD.containsMatchIn(lower) && (lower.contains("onto") || lower.contains("street") || lower.contains("road") || lower.contains("ave")) -> TurnDirection.RIGHT
            else -> TurnDirection.STRAIGHT
        }
    }

    private val SEGMENT_SPLIT = Regex("\\s*[\u2022\u00B7|]\\s*")

    /** Splits a navigation notification into the rows of the expanded card. */
    fun cardText(title: String, text: String): NavigationCardText {
        val segments = text.split(SEGMENT_SPLIT).map { it.trim() }.filter { it.isNotEmpty() }
        val (statSegments, detailSegments) = segments.partition { NavigationLocale.TRIP_STAT.containsMatchIn(it) || NavigationLocale.TRIP_DISTANCE.matches(it) }
        val eta = statSegments.joinToString(" \u2022 ")
        val detail = detailSegments.joinToString(" \u2022 ")

        val distanceSource = "$title $detail"
        val distMatch = NavigationLocale.ANY_DISTANCE.find(distanceSource)
        // "In 300 m" for Latin-script text; other scripts show the distance as the app wrote it ("через 300 м" stays unprefixed).
        val distance = when {
            distMatch == null -> "In 200 m"
            distMatch.value.all { it.code < 128 } -> "In ${distMatch.value}"
            else -> distMatch.value
        }

        val trimmedTitle = title.trim()
        val stripped = trimmedTitle.replace(NavigationLocale.LEADING_DISTANCE_PREFIX, "").trim()
        val instruction = when {
            stripped.isNotEmpty() -> stripped
            trimmedTitle.isNotEmpty() -> trimmedTitle
            else -> "Navigation"
        }
        return NavigationCardText(distance, instruction, detail, eta)
    }
}
