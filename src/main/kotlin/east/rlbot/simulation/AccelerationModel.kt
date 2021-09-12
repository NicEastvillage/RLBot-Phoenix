package east.rlbot.simulation

object AccelerationModel {
    val throttle = StraightAccelerationLUT(ClassLoader.getSystemResourceAsStream("AccelerationData/StraightThrottle.csv"))
    val boost = StraightAccelerationLUT(ClassLoader.getSystemResourceAsStream("AccelerationData/StraightBoost.csv"))
    val turnThrottle = TurningAccelerationLUT(ClassLoader.getSystemResourceAsStream("AccelerationData/TurningThrottle.csv"))
    val turnBoost = TurningAccelerationLUT(ClassLoader.getSystemResourceAsStream("AccelerationData/TurningBoost.csv"))
}