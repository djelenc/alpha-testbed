package atb.app.gui

import atb.gui.ParametersGUI
import atb.infrastructure.*
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
import kotlin.collections.set
import kotlin.properties.Delegates


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
            button("Export") {
                action {
                    controller.export()
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

    // plotting data (metric, service) -> [(tick, value), ... ]
    private val metricData = HashMap<Pair<Metric, Int>, XYChart.Series<Number, Number>>()

    // used for interrupting executing runs
    private var interrupter: () -> Unit = {}

    // current evaluation state
    private var state: EvaluationState by Delegates.observable<EvaluationState>(Idle) { _, old, new ->
        when (new) {
            is Idle -> println("Evaluation is idling")
            is Running -> println("Run is in progress!")
            is Faulted -> println("Run terminated abruptly: ${new.thrown}")
            is Completed -> println("Run completed: ${new.data.readings.size} data points")
            is Interrupted -> println("Run was interrupted at tick ${new.tick}")
        }
    }

    fun stop() = interrupter()

    fun run(seed: Int, chart: LineChart<Number, Number>) {
        chart.data.clear()
        metricData.clear()

        val gui = ParametersGUI(ATBController::class.java.classLoader)
        gui.setBatchRun(true)
        val answer = gui.showDialog()
        if (answer != 0) {
            return
        }

        val scenario = gui.setupParameters[0] as Scenario
        val trustModel = gui.setupParameters[1] as TrustModel<*>
        val duration = gui.setupParameters[5] as Int
        val metrics = HashMap<Metric, Array<Any>>()
        gui.setupParameters[2]?.let { metrics[it as Accuracy] = gui.accuracyParameters }
        gui.setupParameters[3]?.let { metrics[it as Utility] = gui.utilityParameters }
        gui.setupParameters[4]?.let { metrics[it as OpinionCost] = gui.opinionCostParameters }
        metrics.forEach { metric, _ ->
            for (service in scenario.services) {
                metricData[Pair(metric, service)] = XYChart.Series<Number, Number>().apply { name = metric.toString() }
                chart.data.add(metricData[Pair(metric, service)])
            }
        }

        val protocol = createProtocol(trustModel, gui.trustModelParameters, scenario, gui.scenarioParameters, metrics, seed)
        protocol.subscribe({
            for ((key, data) in metricData) {
                val (metric, service) = key
                Platform.runLater {
                    data.data.add(XYChart.Data(it.time, it.getResult(service, metric)))
                }
            }
        })

        val evaluationTask = setupEvaluation(protocol, duration, metrics.keys)
        run(evaluationTask, { Platform.runLater { state = it } })
        state = Running
        interrupter = evaluationTask.interrupter
    }

    fun export() {
        val copied = state
        if (copied is Completed) {
            copied.data.toJSON()
        }
    }
}

class JFXApp : tornadofx.App(ATBMainView::class)

fun main(args: Array<String>) {
    Application.launch(JFXApp::class.java, *args)
}