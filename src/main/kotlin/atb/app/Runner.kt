package atb.app

import atb.core.EvaluationProtocol
import atb.interfaces.Metric
import atb.interfaces.Scenario
import atb.interfaces.TrustModel
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

data class Evaluation(val trustModel: TrustModel<*>,
                      val scenario: Scenario,
                      val metrics: Set<Metric>,
                      val results: List<EvaluationData>)

data class EvaluationData(val tick: Int, val metric: Metric, val service: Int, val value: Double)

class MyExecutor {
    private val runner = Runner()

    fun enqueue(protocol: EvaluationProtocol, duration: Int, metrics: Set<Metric>): Future<List<Int>> {
        // TODO: init Evaluation

        // TODO: add to EvaluationData
        protocol.subscribe({

        })

        for (time in 1..duration) {
            protocol.step(time)
        }

        // TODO: return data

        TODO()
    }
}