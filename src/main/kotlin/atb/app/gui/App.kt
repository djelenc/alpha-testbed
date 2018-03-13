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
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.stage.StageStyle
import javafx.util.converter.NumberStringConverter
import tornadofx.*
import kotlin.collections.set
import kotlin.properties.Delegates


class BatchRunView : View() {
    private val start = SimpleIntegerProperty(1)
    private val stop = SimpleIntegerProperty(30)
    private var logger by singleAssign<TextArea>()

    private val controller: ATBController by inject()

    override val root = form {
        fieldset("Seed range") {
            field("Start") {
                textfield {
                    prefWidth = 45.0
                    textProperty().bindBidirectional(start, NumberStringConverter())
                }
            }
            field("Stop") {
                textfield {
                    prefWidth = 45.0
                    textProperty().bindBidirectional(stop, NumberStringConverter())
                }
            }

        }
        buttonbar {
            button("Start") {
                action { controller.runBatch(start.value, stop.value, logger) }
            }
            button("Stop") {
                action { controller.stop() }
            }
        }
        logger = textarea {
            vgrow = Priority.ALWAYS
        }
    }
}

class ATBMainView : View() {
    private val controller: ATBController by inject()
    private var chart by singleAssign<LineChart<Number, Number>>()

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

        val job = setupEvaluation(protocol, duration, metrics.keys)
        interrupter = run(job, { Platform.runLater { state = it } })
        state = Running
    }

    fun runBatch(start: Int, stop: Int, logger: TextArea) {
        val gui = ParametersGUI(ATBController::class.java.classLoader)
        gui.setBatchRun(true)
        val answer = gui.showDialog()
        if (answer != 0) {
            return
        }

        val duration = gui.setupParameters[5] as Int

        val tasks = (start..stop).map { seed ->
            // GUI returns instances of TMs and scenarios
            // due to thread issues, we have to make copies for every run
            val model = (gui.setupParameters[1] as TrustModel<*>).javaClass.newInstance()
            val scenario = (gui.setupParameters[0] as Scenario).javaClass.newInstance()
            val metrics = HashMap<Metric, Array<Any>>()
            gui.setupParameters[2]?.let { metrics[it as Accuracy] = gui.accuracyParameters }
            gui.setupParameters[3]?.let { metrics[it as Utility] = gui.utilityParameters }
            gui.setupParameters[4]?.let { metrics[it as OpinionCost] = gui.opinionCostParameters }

            val protocol = createProtocol(model, gui.trustModelParameters,
                    scenario, gui.scenarioParameters, metrics, seed)
            setupEvaluation(protocol, duration, metrics.keys)
        }

        interrupter = runBatch(tasks, {
            Platform.runLater {
                logger.appendText("All done!\n")
            }
        }, {
            when (it) {
                is Completed -> Platform.runLater {
                    logger.appendText("Completed run ${it.data.seed}\n")
                }
                is Interrupted -> Platform.runLater {
                    logger.appendText("Interrupted run ${it.data.seed} at ${it.tick}\n")
                }
                is Faulted -> Platform.runLater {
                    logger.appendText("An exception (${it.thrown}) occurred at ${it.tick}\n")
                }
                else -> Platform.runLater {
                    logger.appendText("Something else went wrong ...\n")
                }
            }
        })
        state = Running
    }
}

class JFXApp : tornadofx.App(ATBMainView::class)

fun main(args: Array<String>) {
    Application.launch(JFXApp::class.java, *args)
}