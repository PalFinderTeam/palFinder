package com.github.palFinderTeam.palfinder.utils.time

/**
 * Define time units with their conversion to seconds factor.
 * Order important when using `.values()`
 */
enum class TimeUnit(val unitName: String, val toSecFactor: Int) {
    YEARS("year", 31536000),
    MONTHS("month", 2592000),
    DAYS("day", 86400),
    HOURS("hour", 3600),
    MINUTES("minute", 60),
    SECONDS("second", 1);
}