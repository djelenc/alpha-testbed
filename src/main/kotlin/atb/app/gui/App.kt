package atb.app.gui

import atb.gui.ParametersGUI
import atb.infrastructure.*
import atb.interfaces.*
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Pos
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.layout.Priority
import javafx.stage.StageStyle
import javafx.util.converter.NumberStringConverter
import tornadofx.*
import kotlin.collections.set
import kotlin.properties.Delegates


class BatchRunView : View() {
    // maybe use form builder instead
    // https://edvin.gitbooks.io/tornadofx-guide/content/part1/7.%20Layouts%20and%20Menus.html

    private val start = SimpleIntegerProperty(1)
    private val stop = SimpleIntegerProperty(30)

    override val root = vbox {
        prefHeight = 300.0
        prefWidth = 450.0

        hbox {
            alignment = Pos.BASELINE_CENTER
            label("Seed range: ")
            textfield {
                prefWidth = 45.0
                textProperty().bindBidirectional(start, NumberStringConverter())
            }
            label(" - ")
            textfield {
                prefWidth = 45.0
                textProperty().bindBidirectional(stop, NumberStringConverter())
            }
            button("Start") { action { println("$start - $stop") } }
            button("Stop") { action { println("$start - $stop") } }
        }
        textarea {
            vgrow = Priority.ALWAYS
        }
    }
}

class ATBMainView : View() {

    private val controller: ATBController by inject()
    private var chart: LineChart<Number, Number> by singleAssign()

    private val seed = SimpleIntegerProperty(1)

    override val root = vbox {
        prefHeight = 400.0
        prefWidth = 600.0

        hbox {
            textfield {
                alignment = Pos.TOP_LEFT
                hgrow = Priority.ALWAYS
                textProperty().bindBidirectional(seed, NumberStringConverter())
            }
            button("Start") {
                action {
                    controller.run(seed.value, chart)
                }
            }
            button("Stop") {
                action {
                    controller.stop()
                }
            }
            button("Batch run") {
                action {
                    find(BatchRunView::class).openModal(stageStyle = StageStyle.UTILITY)
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
    private var state: EvaluationState by Delegates.observable<EvaluationState>(Idle) { _, _, new ->
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
}

class JFXApp : tornadofx.App(ATBMainView::class)

fun main(args: Array<String>) {
    Application.launch(JFXApp::class.java, *args)
}