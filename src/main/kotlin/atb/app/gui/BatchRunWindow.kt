package atb.app.gui

import atb.gui.ParametersGUI
import atb.infrastructure.*
import atb.interfaces.*
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Priority
import javafx.util.converter.NumberStringConverter
import tornadofx.*

class BatchRunView : View() {
    private val controller: BatchRunController by inject()

    override val root = form {
        fieldset("Seed range") {
            field("Start") {
                textfield {
                    textProperty().bindBidirectional(controller.start, NumberStringConverter())
                }
            }
            field("Stop") {
                textfield {
                    textProperty().bindBidirectional(controller.stop, NumberStringConverter())
                }
            }

            disableWhen { controller.isRunning }
            enableWhen { controller.isRunning.not() }
        }
        fieldset {
            buttonbar {
                button("Start") {
                    action { controller.run() }
                    disableWhen { controller.isRunning }
                    enableWhen { controller.isRunning.not() }
                }
                button("Stop") {
                    action { controller.stop() }
                    disableWhen { controller.isRunning.not() }
                    enableWhen { controller.isRunning }
                }
            }
            progressbar {
                fitToParentWidth()
                progressProperty().bindBidirectional(controller.rate)
            }
        }

        textarea {
            prefRowCount = 5
            vgrow = Priority.ALWAYS
            textProperty().bindBidirectional(controller.logger)
        }

    }
}

class BatchRunController : Controller() {
    val start = SimpleIntegerProperty(1)
    val stop = SimpleIntegerProperty(30)
    val logger = SimpleStringProperty("")
    val rate = SimpleDoubleProperty(0.0)

    val isRunning = SimpleBooleanProperty(false)

    private var interrupter: () -> Unit = {}

    fun stop() = interrupter()

    fun run() {
        val gui = ParametersGUI(ATBMainController::class.java.classLoader)
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
        rate.value = 0.0

        interrupter = runBatch(tasks, {
            Platform.runLater {
                logger.value += "All done!\n"
                rate.value = 100.0
                isRunning.value = false
                when {
                    it.any { it is Interrupted } -> println("Batch run was interrupted")
                    it.any { it is Faulted } -> println("Batch run failed")
                    it.all { it is Completed } -> println("All good!")
                    else -> throw IllegalStateException("All states have to be complete")
                }
            }
        }, {
            Platform.runLater {
                when (it) {
                    is Completed -> logger.value += "Completed run ${it.data.seed}\n"
                    is Interrupted -> logger.value += "Interrupted run ${it.data.seed} at ${it.tick}\n"
                    is Faulted -> logger.value += "An exception (${it.thrown}) occurred at ${it.tick}\n"
                    else -> logger.value += "Something else went wrong ...\n"
                }
                rate.value += progressRate
            }
        })
        isRunning.value = true
    }
}