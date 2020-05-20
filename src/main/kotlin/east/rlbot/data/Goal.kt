package east.rlbot.data

import east.rlbot.math.Vec3

class Goal private constructor(val ts: Float) {

    val pos = Vec3(y=ts * Arena.LENGTH2)
    val left = pos.withX(ts * WIDTH2)
    val right = pos.withX(-ts * WIDTH2)

    companion object {
        const val WIDTH = 1786f
        const val WIDTH2 = WIDTH / 2f
        const val HEIGHT = 642f
        const val DEPTH = 880

        val goals = listOf(Goal(-1f), Goal(1f))

        fun get(team: Int) = goals[team]
    }
}