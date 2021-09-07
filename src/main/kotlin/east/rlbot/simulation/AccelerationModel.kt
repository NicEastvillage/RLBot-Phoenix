package east.rlbot.simulation

import java.io.File

object AccelerationModel {
    val throttle = StraightAccelerationLUT(File(javaClass.getResource("/AccelerationData/StraightThrottle.csv").toURI()))
    val boost = StraightAccelerationLUT(File(javaClass.getResource("/AccelerationData/StraightBoost.csv").toURI()))
}