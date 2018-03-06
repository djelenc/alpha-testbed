package atb.app

import java.util.concurrent.*

internal class Runner : ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue(), ThreadFactory {
    Executors.defaultThreadFactory().newThread(it).apply { isDaemon = true }
}) {

    override fun afterExecute(task: Runnable?, thrown: Throwable?) {
        super.afterExecute(task, thrown)

        thrown?.printStackTrace()

        if (task is Future<*>) {
            try {
                (task as Future<*>).get()
            } catch (e: CancellationException) {
                // ignore
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }
    }
}
