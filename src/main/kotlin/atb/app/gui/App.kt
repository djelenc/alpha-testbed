package atb.app.gui

import atb.common.DefaultRandomGenerator
import atb.core.AlphaTestbed
import atb.gui.ParametersGUI
import atb.infrastructure.Evaluation
import atb.infrastructure.Runner
import atb.infrastructure.createRun
import atb.interfaces.*
import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import tornadofx.*
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import kotlin.collections.set


class ATBMainView : View() {

    private val controller: ATBController by inject()
    private var input: TextField by singleAssign()
    private var chart: LineChart<Number, Number> by singleAssign()

    override val root = vbox {
        prefHeight = 400.0
        prefWidth = 600.0

        hbox {
            input = textfield("1") {
                alignment = Pos.TOP_LEFT
                hgrow = Priority.ALWAYS
            }
            button("Start") {
                action {
                    controller.run(input.text.toInt(), chart)
                }
            }
            button("Stop") {
                action {
                    controller.stop()
                }
            }
            button("Print results") {
                action {
                    controller.showResults()
                }
            }
        }

        chart = linechart("Results",
                NumberAxis().apply {
                    tickUnit = 25.0
                    lowerBound = 0.0
                }, NumberAxis().apply {
            upperBound = 1.0
            lowerBound = 0.0
            tickUnit = 0.05
            isAutoRanging = false
        }) {
            vgrow = Priority.ALWAYS
        }
    }
}

class ATBController : Controller() {

    // (metric, service) -> [(time, value), ... ]
    private val metricData = HashMap<Pair<Metric, Int>, XYChart.Series<Number, Number>>()

    private val runner = Runner()

    private var task: Future<*> = CompletableFuture.completedFuture(Unit)

    fun run(seed: Int, chart: LineChart<Number, Number>) {
        chart.data.clear()
        metricData.clear()
        task = CompletableFuture.completedFuture(Unit)

        val gui = ParametersGUI(ATBController::class.java.classLoader)
        gui.setBatchRun(true)

        val answer = gui.showDialog()

        if (answer != 0) {
            return
        }

        val scenario = gui.setupParameters[0] as Scenario
        scenario.randomGenerator = DefaultRandomGenerator(seed)
        scenario.initialize(*gui.scenarioParameters)

        val trustModel = gui.setupParameters[1] as TrustModel<*>
        trustModel.setRandomGenerator(DefaultRandomGenerator(seed))
        trustModel.initialize(*gui.trustModelParameters)

        val metrics = HashMap<Metric, Array<Any>>()
        gui.setupParameters[2]?.let {
            metrics[it as Accuracy] = gui.accuracyParameters

            for (service in scenario.services) {
                metricData[Pair(it, service)] = XYChart.Series<Number, Number>().apply { name = it.toString() }
                chart.data.add(metricData[Pair(it, service)])
            }
        }
        gui.setupParameters[3]?.let {
            metrics[it as Utility] = gui.utilityParameters
            for (service in scenario.services) {
                metricData[Pair(it, service)] = XYChart.Series<Number, Number>().apply { name = it.toString() }
                chart.data.add(metricData[Pair(it, service)])
            }
        }
        gui.setupParameters[4]?.let {
            metrics[it as OpinionCost] = gui.opinionCostParameters
            for (service in scenario.services) {
                metricData[Pair(it, service)] = XYChart.Series<Number, Number>().apply { name = it.toString() }
                chart.data.add(metricData[Pair(it, service)])
            }
        }

        val duration = gui.setupParameters[5] as Int

        val protocol = AlphaTestbed.getProtocol(trustModel, scenario, metrics)
        protocol.subscribe({
            for ((key, data) in metricData) {
                val (metric, service) = key
                Platform.runLater {
                    data.data.add(XYChart.Data(it.time, it.getResult(service, metric)))
                }
            }
        })

        val run = createRun(protocol, duration, metrics.keys)
        task = runner.submit(run)
    }

    fun stop() {
        task.cancel(true)
        task = CompletableFuture.completedFuture(Unit)
    }

    fun showResults(): Unit = when (task.isDone) {
        true -> {
            try {
                val evaluation = task.get() as Evaluation
                println("Got ${evaluation.results.size} lines of data")
            } catch (e: CancellationException) {
                println("Evaluation has been stopped")
                task = CompletableFuture.completedFuture(Unit)
            } catch (e: ExecutionException) {
                println("Evaluation encountered an error: ${e.message}")
                task = CompletableFuture.completedFuture(Unit)
            }
        }
        false -> println("Evaluation in progress, cannot print data")
    }
}

class ATBApp : tornadofx.App(ATBMainView::class)

fun main(args: Array<String>) {
    Application.launch(ATBApp::class.java, *args)
}