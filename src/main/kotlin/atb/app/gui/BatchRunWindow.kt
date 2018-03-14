package atb.app.gui

import atb.gui.ParametersGUI
import atb.infrastructure.*
import atb.interfaces.*
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import javafx.util.converter.NumberStringConverter
import tornadofx.*

class BatchRunView : View() {
    private val controller: BatchRunController by inject()

    override val root = form {
        title = "Batch runs"
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
            field("Output directory") {
                button {
                    text = System.getProperty("user.dir")
                    hgrow = Priority.ALWAYS
                    action {
                        val dir = chooseDirectory("Select folder for saving results")
                        dir?.let { text = it.toString() }
                    }
                }
            }
            enableWhen { controller.isRunning.not() }
        }
        fieldset("Manage run") {
            buttonbar {
                button("Start") {
                    action {
                        controller.logger.value = ""
                        controller.run()
                    }
                    enableWhen(controller.isRunning.not())
                }
                button("Stop") {
                    action { controller.stop() }
                    enableWhen(controller.isRunning)
                }
            }
            spacer { prefHeight = 10.0 }
        }
        fieldset("Run progress", labelPosition = Orientation.VERTICAL) {
            progressbar {
                fitToParentWidth()
                progressProperty().bindBidirectional(controller.rate)
            }
            field("Log output", Orientation.VERTICAL) {
                textarea {
                    //prefRowCount = 5
                    vgrow = Priority.ALWAYS
                    textProperty().bindBidirectional(controller.logger)
                    controller.logger.onChange { positionCaret(length) } // auto-scroll
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
            // To avoid threading issues, we have to copy instances of TMs and scenarios
            // in every run; the ones we get from ParametersGUI are not thread safe.
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
                rate.value = 100.0
                isRunning.value = false
                when {
                    it.any { it is Interrupted } -> logger.value += "Evaluation was interrupted.\n"
                    it.any { it is Faulted } -> logger.value += "Some runs failed.\n"
                    it.all { it is Completed } -> logger.value += "Evaluation completed.\n"
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