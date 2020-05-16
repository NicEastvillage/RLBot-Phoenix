package east.rlbot

import rlbot.manager.BotManager


private const val DEFAULT_PORT = 18918

fun main(args: Array<String>) {
    val pythonInterface = PythonInterface(DEFAULT_PORT, BotManager())
    Thread(pythonInterface::start).start()
}
