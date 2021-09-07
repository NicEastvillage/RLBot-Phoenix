package east.rlbot.simulation

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import east.rlbot.data.Orientation
import east.rlbot.math.Mat3
import east.rlbot.math.Vec3
import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.sign

class TurningAccelerationLUT(file: File) {

    private val entries: List<Entry> = csvReader().readAllWithHeader(file).map { row ->
        Entry(
            row["time"]!!.toFloat(),
            row["angle"]!!.toFloat(),
            row["speed"]!!.toFloat(),
            Vec3(row["x"]!!.toFloat(), row["y"]!!.toFloat()),
        )
    }

    fun simUntilLimit(
        initialSpeed: Float,
        timeLimit: Float? = null,
        angleLimit: Float? = null,
        speedLimit: Float? = null,
    ): LookupResult {
        // At least one limit expected
        assert(timeLimit != null || angleLimit != null || speedLimit != null) { "No limit set" }

        // limits must be positive
        if (timeLimit != null) assert(timeLimit > 0)
        if (speedLimit != null) assert(speedLimit > 0)
        if (angleLimit != null) assert(angleLimit > 0)

        // Start entry from initial speed
        val startIndex = findIndex(initialSpeed) { it.speed }
        val startEntry = entries[startIndex]

        var timeLimitIndex = entries.size - 1
        var angleLimitIndex = entries.size - 1
        var speedLimitIndex = entries.size - 1

        if (timeLimit != null)
            timeLimitIndex = findIndex(startEntry.time + timeLimit) { it.time }
        if (angleLimit != null)
            angleLimitIndex = findIndex(startEntry.angle + angleLimit) { it.angle }
        if (speedLimit != null)
            speedLimitIndex = findIndex(startEntry.speed + speedLimit) { it.speed }

        // Find soonest reached limit
        val resultIndex = listOf(timeLimitIndex, angleLimitIndex, speedLimitIndex).minOrNull()!!.coerceAtLeast(startIndex)
        val resultEntry = entries[resultIndex]

        val displacement = startEntry.localDisplacement - resultEntry.localDisplacement
        val startOri = Orientation(Mat3.eulerToRotation(0f, startEntry.angle, 0f))
        val localDisplacement = startOri.toLocal(displacement)

        return LookupResult(
            duration = resultEntry.time - startEntry.time,
            angle = resultEntry.angle - startEntry.angle,
            endSpeed = resultEntry.speed,
            localDisplacement = localDisplacement,
            durationLimitReached = timeLimit != null && resultIndex == timeLimitIndex,
            angleLimitReached = angleLimit != null && resultIndex == angleLimitIndex,
            speedLimitReached = speedLimit != null && resultIndex == speedLimitIndex,
        )
    }

    /**
     * Returns the index of the entry where the given property reaches the given value, or the entry right before it,
     * if the exact value is not found
     */
    private fun findIndex(value: Float, property: (Entry) -> Float): Int {
        return entries.binarySearch { (property(it) - value).sign.toInt() }.absoluteValue.coerceAtMost(entries.size - 1)
    }

    data class Entry(
        val time: Float,
        val angle: Float,
        val speed: Float,
        val localDisplacement: Vec3,
    )

    data class LookupResult(
        val duration: Float,
        val angle: Float,
        val endSpeed: Float,
        val localDisplacement: Vec3,
        val durationLimitReached: Boolean,
        val angleLimitReached: Boolean,
        val speedLimitReached: Boolean,
    )
}