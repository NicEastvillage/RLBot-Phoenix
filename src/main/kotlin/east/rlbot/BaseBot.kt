package east.rlbot

import east.rlbot.data.Arena
import east.rlbot.data.DataPack
import east.rlbot.data.Team
import east.rlbot.maneuver.Maneuver
import east.rlbot.math.Vec3
import east.rlbot.navigator.AerialMovement
import east.rlbot.navigator.ShotFinder
import east.rlbot.navigator.SimpleDriving
import east.rlbot.simulation.turnRadius
import east.rlbot.training.AerialOrientateTraining
import east.rlbot.training.BallNudgerTraining
import east.rlbot.training.Training
import east.rlbot.util.DebugDraw
import rlbot.Bot
import rlbot.ControllerState
import rlbot.flat.GameTickPacket
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sqrt
import kotlin.random.Random

abstract class BaseBot(private val index: Int, teamIndex: Int, val name: String) : Bot {

    val team = Team.get(teamIndex)

    val data = DataPack(this, index)
    val draw = DebugDraw(index)
    val drive = SimpleDriving(this)
    val shotFinder = ShotFinder(this)
    val fly = AerialMovement(this)
    var maneuver: Maneuver? = null

    var lastOutput: OutputController = OutputController()

    var training: Training? = null

    override fun processInput(request: GameTickPacket): ControllerState {
        draw.start()
        data.update(request)
        if (data.me.isFirstFrameOfBeingDemolished) onDemolished()
        if (data.match.isFirstFrameOfKickOff) onKickoffBegin()

        // Get output
        training?.exec(this)?.let { return it }
        val output = maneuver?.exec(data) ?: getOutput()
        draw.string2D(10, 560 + 20 * index, "$name: ${maneuver?.javaClass?.simpleName}", color = Color.WHITE)
        draw.send()

        // Check if maneuver is done and can be discarded
        if (maneuver?.done == true) {
            maneuver = null
        }

        // Feedback for next tick
        lastOutput = output

        return output
    }

    abstract fun onKickoffBegin()

    open fun onDemolished() {
        maneuver = null
    }

    abstract fun getOutput(): OutputController

    override fun getIndex(): Int = index
    override fun retire() {}

    fun print(text: String) {
        println("$name: $text")
    }
}