package east.rlbot

class PhoenixBot(index: Int, team: Int, name: String) : BaseBot(index, team, name) {
    override fun getOutput(): OutputController {
        return drive.towards(
                data.ball.pos,
                targetSpeed = 2300f,
                boostMinimum = 0
        )
    }
}