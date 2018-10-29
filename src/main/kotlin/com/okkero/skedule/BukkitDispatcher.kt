package com.okkero.skedule

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

private val bukkitScheduler
    get() = Bukkit.getScheduler()

private fun TimeUnit.toBukkitTicks(time: Long): Long {
    return toMillis(time) / 50
}

@InternalCoroutinesApi
class BukkitDispatcher(val plugin: JavaPlugin, val async: Boolean = false) : CoroutineDispatcher(), Delay {

    private val runTaskLater: (Plugin, Runnable, Long) -> BukkitTask =
            if (async)
                bukkitScheduler::runTaskLaterAsynchronously
            else
                bukkitScheduler::runTaskLater
    private val runTask: (Plugin, Runnable) -> BukkitTask =
            if (async)
                bukkitScheduler::runTaskAsynchronously
            else
                bukkitScheduler::runTask

    override fun scheduleResumeAfterDelay(time: Long, continuation: CancellableContinuation<Unit>) {
        runTaskLater(plugin, Runnable { continuation.apply { resumeUndispatched(Unit) } }, time / 50)
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!async && Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            runTask(plugin, block)
        }
    }

}

@InternalCoroutinesApi
fun JavaPlugin.dispatcher(async: Boolean = false) = BukkitDispatcher(this, async)
