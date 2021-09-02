package east.rlbot.math

import east.rlbot.data.Orientation

data class OrientedCube(
    val ori: Orientation,
    val size: Vec3,
) {
    constructor(ori: Mat3, size: Vec3) : this(Orientation(ori), size)
}