package east.rlbot.prediction

import java.io.File

object AccelerationModel {
    val throttle = AccelerationLUT(File(javaClass.getResource("/AccelerationData/throttle.csv").toURI()))
    val boost = AccelerationLUT(File(javaClass.getResource("/AccelerationData/boost.csv").toURI()))
}