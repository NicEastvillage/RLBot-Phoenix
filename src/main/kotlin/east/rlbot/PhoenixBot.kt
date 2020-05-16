package east.rlbot

import east.rlbot.data.DataPack

class PhoenixBot(index: Int, team: Int, name: String) : BaseBot(index, team, name) {
    override fun getOutput(data: DataPack): OutputController {
        return drive.towards(
                data.ball.pos,
                targetSpeed = 2300f,
                boostMinimum = 0
        )
    }
}