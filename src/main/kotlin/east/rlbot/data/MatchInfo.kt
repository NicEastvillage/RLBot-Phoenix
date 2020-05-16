package east.rlbot.data

import rlbot.flat.GameInfo

class MatchInfo {
    var time = 0f
    var timeRemaining = 99999f
    var overTime = false
    var roundActive = false
    var matchEnded = false
    var isKickoffPause = false

    fun update(info: GameInfo) {
        time = info.secondsElapsed()
        timeRemaining = info.gameTimeRemaining()
        overTime = info.isOvertime()
        roundActive = info.isRoundActive()
        matchEnded = info.isMatchEnded()
        isKickoffPause = info.isKickoffPause()
    }
}