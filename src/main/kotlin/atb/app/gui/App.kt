package atb.app.gui

import atb.gui.ParametersGUI
import atb.infrastructure.*
import atb.interfaces.*
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
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
    private val controller: BatchRunController by inject()

    override val root = form {
        fieldset("Seed range") {
            field("Start") {
                textfield {
                    //prefWidth = 45.0
                    textProperty().bindBidirectional(controller.start, NumberStringConverter())
                }
            }
            field("Stop") {
                textfield {
                    //prefWidth = 45.0
                    textProperty().bindBidirectional(controller.stop, NumberStringConverter())
                }
            }
            buttonbar {
                button("Start") {
                    action { controller.run() }
                }
                button("Stop") {
                    action { controller.stop() }
                }
            }
            progressbar {
                fitToParentWidth()
                progressProperty().bindBidirectional(controller.rate)
            }
        }
        fieldset(labelPosition = Orientation.VERTICAL) {
            field("", Orientation.VERTICAL) {
                textarea {
                    prefRowCount = 5
                    vgrow = Priority.ALWAYS
                    textProperty().bindBidirectional(controller.logger)
                }
            }
        }
    }
}

class BatchRunController : Controller() {
    val start = SimpleIntegerProperty(1)
    val stop = SimpleIntegerProperty(30)
    val logger = SimpleStringProperty("")
    val rate = SimpleDoubleProperty(0.0)

    private var interrupter: () -> Unit = {}

    private var state: EvaluationState by Delegates.observable<EvaluationState>(Idle) { _, _, new ->
        Platform.runLater {
            when (new) {
                is Idle -> logger.value += "Evaluation is idle.\n"
                is Running -> logger.value += "Run is in progress!\n"
                is Faulted -> logger.value += "Run terminated abruptly: ${new.thrown}\n"
                is Completed -> logger.value += "Run completed: ${new.data.readings.size} data points\n"
                is Interrupted -> logger.value += "Run was interrupted at tick ${new.tick}\n"
            }
        }
    }

    fun stop() = interrupter()

    fun run() {
        rate.value = 0.0
        val gui = ParametersGUI(ATBController::class.java.classLoader)
        val answer = gui.showDialog()
        if (answer != 0) {
            return
        }

        val duration = gui.setupParameters[5] as Int

        val tasks = (start.value..stop.value).map { seed ->
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

        val progressRate = 1.0 / (stop.value - start.value)

        interrupter = runBatch(tasks, {
            Platform.runLater {
                logger.value += "All done!\n"
                rate.value = 100.0
            }
        }, {
            Platform.runLater {
                when (it) {
                    is Completed -> logger.value += ("Completed run ${it.data.seed}\n")
                    is Interrupted -> logger.value += ("Interrupted run ${it.data.seed} at ${it.tick}\n")
                    is Faulted -> logger.value += ("An exception (${it.thrown}) occurred at ${it.tick}\n")
                    else -> logger.value += ("Something else went wrong ...\n")
                }
                rate.value += progressRate
            }
        })
        state = Running
    }
}

class ATBMainView : View() {
    private val controller: ATBController by inject()
    private var chart by singleAssign<LineChart<Number, Number>>()

    override val root = vbox {
        prefHeight = 400.0
        prefWidth = 600.0

        hbox {
            textfield {
                alignment = Pos.TOP_LEFT
                hgrow = Priority.ALWAYS
                textProperty().bindBidirectional(controller.seed, NumberStringConverter())
            }
            button("Start") {
                action {
                    controller.run(chart)
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
    val seed = SimpleIntegerProperty(1)

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

    fun run(chart: LineChart<Number, Number>) {
        chart.data.clear()

        val gui = ParametersGUI(ATBController::class.java.classLoader)
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

        val protocol = createProtocol(trustModel, gui.trustModelParameters, scenario, gui.scenarioParameters, metrics, seed.value)

        val metric2series = HashMap<Pair<Metric, Int>, XYChart.Series<Number, Number>>()

        metrics.forEach { metric, _ ->
            for (service in scenario.services) {
                metric2series[Pair(metric, service)] = XYChart.Series<Number, Number>().apply { name = metric.toString() }
                chart.data.add(metric2series[Pair(metric, service)])
            }
        }

        protocol.subscribe({
            for ((key, data) in metric2series) {
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
}

class JFXApp : tornadofx.App(ATBMainView::class)

fun main(args: Array<String>) {
    Application.launch(JFXApp::class.java, *args)
}