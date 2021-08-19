package east.rlbot.data

import java.awt.Color

enum class Team(
    val index: Int,
    val ysign: Int,
    val color: Color,
    val altColor: Color,
    val altAltColor: Color,
) {
    BLUE(0, -1, Color.BLUE, Color.CYAN, Color.GREEN),
    ORANGE(1, 1, Color.ORANGE, Color.RED, Color.YELLOW);

    companion object {
        fun fromIndex(index: Int) = if (index == 0) BLUE else ORANGE
    }
}
