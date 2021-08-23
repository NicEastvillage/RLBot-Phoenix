package east.rlbot.data

import east.rlbot.math.Vec3

class Goal private constructor(val team: Team) {

    val pos = Vec3(y=team.ysign * Arena.LENGTH2)
    val left = pos.withX(team.ysign * WIDTH2)
    val right = pos.withX(-team.ysign * WIDTH2)

    companion object {
        const val WIDTH = 1786f
        const val WIDTH2 = WIDTH / 2f
        const val HEIGHT = 642f
        const val DEPTH = 880

        val goals = listOf(Goal(Team.BLUE), Goal(Team.ORANGE))

        fun get(team: Team) = goals[team.index]
        fun get(team: Int) = goals[team]
    }
}