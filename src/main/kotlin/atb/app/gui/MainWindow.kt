package atb.app.gui

import atb.gui.ParametersGUI
import atb.infrastructure.*
import atb.interfaces.*
import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.layout.Priority
import javafx.stage.StageStyle
import javafx.util.converter.NumberStringConverter
import tornadofx.*
import tornadofx.controlsfx.statusbar
import kotlin.properties.Delegates

class ATBMainView : View() {
    private val controller: ATBMainController by inject()

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
                    controller.run()
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

        linechart("Results",
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
            dataProperty().bindBidirectional(controller.series)
        }
        statusbar {
            progressProperty().bind(controller.progress)
        }
    }
}

class ATBMainController : Controller() {
    val seed = SimpleIntegerProperty(1)
    val series = SimpleObjectProperty<ObservableList<XYChart.Series<Number, Number>>>(
            FXCollections.observableArrayList())
    val progress = SimpleDoubleProperty(0.0)

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

    fun run() {
        val gui = ParametersGUI(ATBMainController::class.java.classLoader)
        val answer = gui.showDialog()
        if (answer != 0) {
            return
        }

        series.value.clear()
        progress.value = 0.0

        val scenario = gui.setupParameters[0] as Scenario
        val trustModel = gui.setupParameters[1] as TrustModel<*>
        val duration = gui.setupParameters[5] as Int
        val metrics = HashMap<Metric, Array<Any>>()
        gui.setupParameters[2]?.let { metrics[it as Accuracy] = gui.accuracyParameters }
        gui.setupParameters[3]?.let { metrics[it as Utility] = gui.utilityParameters }
        gui.setupParameters[4]?.let { metrics[it as OpinionCost] = gui.opinionCostParameters }

        val protocol = createProtocol(trustModel, gui.trustModelParameters, scenario,
                gui.scenarioParameters, metrics, seed.value)

        val metric2series = HashMap<Pair<Metric, Int>, XYChart.Series<Number, Number>>()

        metrics.forEach { metric, _ ->
            for (service in scenario.services) {
                metric2series[Pair(metric, service)] = XYChart.Series<Number, Number>().apply {
                    name = metric.toString()
                }
                series.value.add(metric2series[Pair(metric, service)])
            }
        }

        val rate = 1.0 / duration

        protocol.subscribe({
            for ((key, data) in metric2series) {
                val (metric, service) = key
                Platform.runLater {
                    data.data.add(XYChart.Data(it.time, it.getResult(service, metric)))
                    progress.value += rate
                }
            }
        })

        val job = setupEvaluation(protocol, duration, metrics.keys)
        interrupter = run(job, { Platform.runLater { state = it } })
        state = Running
    }
}