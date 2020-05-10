package east.rlbot

import rlbot.Bot
import rlbot.ControllerState
import rlbot.flat.GameTickPacket

abstract class BaseBot(private val index: Int) : Bot {

    override fun processInput(request: GameTickPacket): ControllerState {
        return getOutput(request)
    }

    abstract fun getOutput(request: GameTickPacket): OutputController

    override fun getIndex(): Int = index
    override fun retire() {}
}