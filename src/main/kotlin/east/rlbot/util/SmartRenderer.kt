package east.rlbot.util

import com.google.flatbuffers.FlatBufferBuilder
import east.rlbot.math.Vec3
import east.rlbot.math.clamp
import rlbot.cppinterop.RLBotDll
import rlbot.cppinterop.RLBotInterfaceException
import rlbot.render.RenderPacket
import rlbot.render.Renderer
import java.awt.Color


/**
 * A renderer with extended functionality. Remember to call `startPacket` and
 * `finishAndSendIfDifferent`. You cannot use this and the `BotLoopRenderer` at the same time.
 */
class SmartRenderer(index: Int) : Renderer(index) {

    private var previousPacket: RenderPacket? = null

    fun startPacket() {
        builder = FlatBufferBuilder(1000)
    }

    fun finishAndSendIfDifferent() {
        val packet: RenderPacket = doFinishPacket()
        if (packet != previousPacket) {
            RLBotDll.sendRenderPacket(packet)
            previousPacket = packet
        }
    }

    /**
     * Draw a cube with the given center and size
     */
    fun cube(color: Color, center: Vec3, size: Float) {
        val r = size / 2
        drawLine3d(color, center.plus(Vec3(-r, -r, -r)), center.plus(Vec3(-r, -r, r)))
        drawLine3d(color, center.plus(Vec3(r, -r, -r)), center.plus(Vec3(r, -r, r)))
        drawLine3d(color, center.plus(Vec3(-r, r, -r)), center.plus(Vec3(-r, r, r)))
        drawLine3d(color, center.plus(Vec3(r, r, -r)), center.plus(Vec3(r, r, r)))
        drawLine3d(color, center.plus(Vec3(-r, -r, -r)), center.plus(Vec3(-r, r, -r)))
        drawLine3d(color, center.plus(Vec3(r, -r, -r)), center.plus(Vec3(r, r, -r)))
        drawLine3d(color, center.plus(Vec3(-r, -r, r)), center.plus(Vec3(-r, r, r)))
        drawLine3d(color, center.plus(Vec3(r, -r, r)), center.plus(Vec3(r, r, r)))
        drawLine3d(color, center.plus(Vec3(-r, -r, -r)), center.plus(Vec3(r, -r, -r)))
        drawLine3d(color, center.plus(Vec3(-r, -r, r)), center.plus(Vec3(r, -r, r)))
        drawLine3d(color, center.plus(Vec3(-r, r, -r)), center.plus(Vec3(r, r, -r)))
        drawLine3d(color, center.plus(Vec3(-r, r, r)), center.plus(Vec3(r, r, r)))
    }

    /**
     * Draw a cross with the given center and size
     */
    fun cross(color: Color, center: Vec3, size: Float) {
        val r = size / 2
        drawLine3d(color, center.plus(Vec3(-r, 0, 0)), center.plus(Vec3(r, 0, 0)))
        drawLine3d(color, center.plus(Vec3(0, -r, 0)), center.plus(Vec3(0, r, 0)))
        drawLine3d(color, center.plus(Vec3(0, 0, -r)), center.plus(Vec3(0, 0, r)))
    }

    /**
     * Draw the next few seconds of ball prediction
     */
    fun ballTrajectory(color: Color, duration: Float) {
        try {
            val ballPrediction = RLBotDll.getBallPrediction()
            var previousLocation: Vec3? = null
            val stop = (60 * clamp(duration, 0f, 6f)).toInt()
            var i = 0
            while (i < ballPrediction.slicesLength()) {
                val slice = ballPrediction.slices(i)
                if (i >= stop) {
                    break
                }
                val location = Vec3(slice.physics().location())
                previousLocation?.let { drawLine3d(color, it, location) }
                previousLocation = location
                i += 4
            }
        } catch (ignored: RLBotInterfaceException) {
        }
    }
}