package east.rlbot

import east.rlbot.data.DataPack
import east.rlbot.navigator.SimpleDriving
import east.rlbot.util.SmartRenderer
import rlbot.Bot
import rlbot.ControllerState
import rlbot.flat.GameTickPacket

abstract class BaseBot(private val index: Int, val team: Int, val name: String) : Bot {

    val data = DataPack(this, index)
    val renderer = SmartRenderer(index)
    val drive = SimpleDriving(this)

    override fun processInput(request: GameTickPacket): ControllerState {
        data.update(request)
        renderer.startPacket()
        val output = getOutput()
        renderer.finishAndSendIfDifferent()
        return output
    }

    abstract fun getOutput(): OutputController

    override fun getIndex(): Int = index
    override fun retire() {}
}