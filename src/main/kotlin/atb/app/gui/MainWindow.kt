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
import javafx.util.StringConverter
import javafx.util.converter.NumberStringConverter
import tornadofx.*
import tornadofx.controlsfx.statusbar

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
                enableWhen(controller.state.isNotEqualTo(Running))
            }
            button("Start") {
                action { controller.run() }
                enableWhen(controller.state.isNotEqualTo(Running))
            }
            button("Stop") {
                action { controller.stop() }
                enableWhen(controller.state.isEqualTo(Running))
            }
            button("Batch run") {
                action { find(BatchRunView::class).openModal(stageStyle = StageStyle.UTILITY) }
                enableWhen(controller.state.isNotEqualTo(Running))
            }
        }

        linechart("Results",
                NumberAxis().apply {
                    tickUnit = 25.0
                    lowerBound = 0.0
                }, NumberAxis().apply {
            upperBound = 1.01
            lowerBound = 0.0
            tickUnit = 0.05
            isAutoRanging = false
        }) {
            vgrow = Priority.ALWAYS
            dataProperty().bindBidirectional(controller.series)
        }
        statusbar {
            progressProperty().bind(controller.progress)
            textProperty().bindBidirectional(controller.state, EvaluationStateToString())
        }
    }

    /** Converts between EvaluationState instances and their string representation */
    internal class EvaluationStateToString : StringConverter<EvaluationState>() {
        override fun fromString(value: String): EvaluationState =
                throw Error("This property cannot be created from String!")

        override fun toString(value: EvaluationState): String = when (value) {
            is Interrupted -> "Interrupted at ${value.tick}"
            is Faulted -> "Error occurred at ${value.tick}: ${value.thrown.message}"
            else -> value.javaClass.simpleName
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
    val state = SimpleObjectProperty<EvaluationState>(Idle)

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

        protocol.subscribe {
            for ((key, data) in metric2series) {
                val (metric, service) = key
                Platform.runLater {
                    data.data.add(XYChart.Data(it.time, it.getResult(service, metric)))
                    progress.value += rate
                }
            }
        }

        val job = setupEvaluation(protocol, duration, metrics.keys)
        interrupter = run(job) {
            Platform.runLater {
                state.value = it
                (it as? Faulted)?.thrown?.printStackTrace()
            }
        }
        state.value = Running
    }
}