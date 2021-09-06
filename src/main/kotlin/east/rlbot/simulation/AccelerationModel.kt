package east.rlbot.simulation

import java.io.File

object AccelerationModel {
    val throttle = AccelerationLUT(File(javaClass.getResource("/AccelerationData/StraightThrottle.csv").toURI()))
    val boost = AccelerationLUT(File(javaClass.getResource("/AccelerationData/StraightBoost.csv").toURI()))
}