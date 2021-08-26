package east.rlbot

import east.rlbot.data.DataPack
import east.rlbot.data.Team
import east.rlbot.maneuver.Maneuver
import east.rlbot.math.Vec3
import east.rlbot.navigator.AerialMovement
import east.rlbot.navigator.ShotFinder
import east.rlbot.navigator.SimpleDriving
import east.rlbot.training.Training
import east.rlbot.util.DebugDraw
import rlbot.Bot
import rlbot.ControllerState
import rlbot.flat.GameTickPacket
import java.awt.Color

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

        if (index == 0) {
            for (car in data.allCars) {
                draw.orientedCube(car.hitboxCenter, car.hitbox, Color.WHITE)
                draw.orientedOrigin(car.pos, car.ori)
            }
        }

        // Get output
        training?.exec(this)
        val output = maneuver?.exec(data) ?: getOutput()
        draw.string2D(10, 10 + 20 * index, "$name: ${maneuver?.javaClass?.simpleName}")
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