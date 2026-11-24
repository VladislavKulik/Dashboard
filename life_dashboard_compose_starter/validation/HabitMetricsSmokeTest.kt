package com.example.lifedashboard.validation

import com.example.lifedashboard.data.DateKeys
import com.example.lifedashboard.data.HabitMetrics
import com.example.lifedashboard.data.model.DashboardHabit

fun main() {
    val firstHabitDates = (1..14).map(DateKeys::keyForDaysAgo).toSet()
    val secondHabitDates = (1..5).map(DateKeys::keyForDaysAgo).toSet()
    val habit = DashboardHabit("h1", "Demo", firstHabitDates)
    val secondHabit = DashboardHabit("h2", "Demo 2", secondHabitDates)

    check(DateKeys.lastDays(7).last() == DateKeys.today())
    check(DateKeys.lastDays(7).size == 7)
    check(DateKeys.currentMonthGrid().size % 7 == 0)
    check(HabitMetrics.currentStreak(habit.completionDates) == 14)
    check(HabitMetrics.currentStreak(secondHabit.completionDates) == 5)

    val checkedToday = habit.completionDates + DateKeys.today()
    check(HabitMetrics.currentStreak(checkedToday) == 15)
    val uncheckedToday = checkedToday - DateKeys.today()
    check(HabitMetrics.currentStreak(uncheckedToday) == 14)

    val rate = HabitMetrics.monthlyActivityPercent(listOf(habit, secondHabit))
    check(rate in 0..100)

    val screenSeedDates = (1..14).toList() + (16..20).toList()
    val screenSeed = DashboardHabit(
        "seed",
        "Утренняя зарядка",
        screenSeedDates.map(DateKeys::keyForDaysAgo).toSet()
    )
    val screenSeedRate = HabitMetrics.monthlyActivityPercent(listOf(screenSeed, secondHabit))
    check(screenSeedRate in 80..90)
    check(HabitMetrics.completedHabitCount(listOf(habit, secondHabit), DateKeys.keyForDaysAgo(1)) == 2)
    check(HabitMetrics.completedHabitCount(listOf(habit, secondHabit), DateKeys.today()) == 0)

    println("Habit metrics smoke tests passed; sample test rate=$rate%, screen seed rate=$screenSeedRate%")
}
