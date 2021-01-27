package east.rlbot

import east.rlbot.data.DataPack
import east.rlbot.maneuver.Maneuver
import east.rlbot.navigator.AerialMovement
import east.rlbot.navigator.SimpleDriving
import east.rlbot.training.Training
import east.rlbot.util.SmartRenderer
import rlbot.Bot
import rlbot.ControllerState
import rlbot.flat.GameTickPacket

abstract class BaseBot(private val index: Int, val team: Int, val name: String) : Bot {

    val data = DataPack(this, index)
    val draw = SmartRenderer(index)
    val drive = SimpleDriving(this)
    val fly = AerialMovement(this)
    var maneuver: Maneuver? = null

    var lastOutput: OutputController = OutputController()

    var training: Training? = null

    override fun processInput(request: GameTickPacket): ControllerState {
        data.update(request)

        // Get output
        draw.startPacket()
        training?.exec(this)
        val output = getOutput()
        draw.finishAndSendIfDifferent()

        // Check if maneuver is done and can be discarded
        if (maneuver?.done == true) {
            maneuver = null
        }

        // Feedback for next tick
        lastOutput = output

        return output
    }

    abstract fun getOutput(): OutputController

    override fun getIndex(): Int = index
    override fun retire() {}
}