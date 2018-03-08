package atb.infrastructure

import atb.core.EvaluationProtocol
import atb.interfaces.Metric
import java.util.*
import java.util.concurrent.*

internal class Runner : ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue(), ThreadFactory {
    Executors.defaultThreadFactory().newThread(it).apply { isDaemon = true }
}) {

    override fun afterExecute(task: Runnable?, thrown: Throwable?) {
        if (task is Future<*>) {
            try {
                task.get()
            } catch (ignored: CancellationException) {
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }
    }
}


data class Evaluation(val protocol: EvaluationProtocol, val metrics: Set<Metric>,
                      val results: MutableList<Result>, val seed: Int)

data class Result(val tick: Int, val metric: Metric, val service: Int, val value: Double)

fun createRun(protocol: EvaluationProtocol, duration: Int, metrics: Set<Metric>):
        Callable<Evaluation> {
    val results = ArrayList<Result>()
    val evaluation = Evaluation(protocol, metrics, results, protocol.scenario.randomGenerator.seed)

    protocol.subscribe({
        for (metric in metrics) {
            for (service in it.scenario.services) {
                val value = it.getResult(service, metric)
                evaluation.results.add(Result(it.time, metric, service, value))
            }
        }
    })

    return Callable {
        for (time in 1..duration) {
            protocol.step(time)

            if (Thread.interrupted()) {
                break
            }
        }
        evaluation
    }
}