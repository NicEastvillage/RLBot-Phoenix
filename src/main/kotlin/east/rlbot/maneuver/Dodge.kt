package east.rlbot.maneuver

import east.rlbot.OutputController

class Dodge(
        firstJumpDuration: Float = 0.08f,
        firstPauseDuration: Float = 0.12f,
        secondJumpDuration: Float = 0.08f,
        secondPauseDuration: Float = 0.4f
) : SteppedManeuver(
        TimedSingleOutputManeuver(firstJumpDuration, OutputController().withThrottle(1).withJump()),
        TimedSingleOutputManeuver(firstPauseDuration, OutputController().withThrottle(1)),
        TimedSingleOutputManeuver(secondJumpDuration, OutputController().withThrottle(1).withJump().withPitch(-1)),
        TimedSingleOutputManeuver(secondPauseDuration, OutputController().withThrottle(1)),
        TimedOutputManeuver(1f) { data -> null.also { data.bot.maneuver = Recovery() } }
)