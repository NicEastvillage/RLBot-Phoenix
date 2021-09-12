package east.rlbot

import rlbot.manager.BotManager


private const val DEFAULT_PORT = 18918

fun main() {
    val pythonInterface = PythonInterface(DEFAULT_PORT, BotManager().also { it.setRefreshRate(120) })
    Thread(pythonInterface::start).start()
}
