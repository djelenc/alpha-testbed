package testbed.app

import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import testbed.common.DefaultRandomGenerator
import testbed.core.AlphaTestbed
import testbed.core.EvaluationProtocol
import testbed.core.MetricSubscriber
import testbed.gui.ParametersGUI
import testbed.interfaces.*
import tornadofx.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future

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
                    controller.stop(chart)
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

class ATBController : Controller(), MetricSubscriber {

    private val metricData = HashMap<Metric, XYChart.Series<Number, Number>>()

    private val executor = Executors.newSingleThreadExecutor()

    fun run(seed: Int, chart: LineChart<Number, Number>): Future<*> {
        val gui = ParametersGUI(ATBController::class.java.classLoader)
        gui.setBatchRun(true)

        val answer = gui.showDialog()

        if (answer != 0) {
            return CompletableFuture.completedFuture(Unit)
        }

        val scenario = gui.setupParameters[0] as Scenario
        scenario.setRandomGenerator(DefaultRandomGenerator(seed))
        scenario.initialize(*gui.scenarioParameters)

        val trustModel = gui.setupParameters[1] as TrustModel<*>
        trustModel.setRandomGenerator(DefaultRandomGenerator(seed))
        trustModel.initialize(*gui.trustModelParameters)

        val metrics = HashMap<Metric, Array<Any>>()
        gui.setupParameters[2]?.let {
            metrics[it as Accuracy] = gui.accuracyParameters
            metricData[it] = XYChart.Series<Number, Number>().apply { name = it.toString() }
            chart.data.add(metricData[it])
        }
        gui.setupParameters[3]?.let {
            metrics[it as Utility] = gui.utilityParameters
            metricData[it] = XYChart.Series<Number, Number>().apply { name = it.toString() }
            chart.data.add(metricData[it])
        }
        gui.setupParameters[4]?.let {
            metrics[it as OpinionCost] = gui.opinionCostParameters
            metricData[it] = XYChart.Series<Number, Number>().apply { name = it.toString() }
            chart.data.add(metricData[it])
        }

        val duration = gui.setupParameters[5] as Int

        val protocol = AlphaTestbed.getProtocol(trustModel, scenario, metrics)
        protocol.subscribe(this)

        return executor.submit {
            for (time in 1..duration) {
                protocol.step(time)
            }
        }
    }

    fun stop(chart: LineChart<Number, Number>) {
        chart.data.clear()
        metricData.clear()
    }

    override fun update(instance: EvaluationProtocol) {
        Platform.runLater {
            for ((metric, series) in metricData) {
                series.data.add(XYChart.Data(instance.time, instance.getResult(0, metric)))
            }
        }
    }
}

class ATBApp : tornadofx.App(ATBMainView::class)

fun main(args: Array<String>) {
    Application.launch(ATBApp::class.java, *args)
}