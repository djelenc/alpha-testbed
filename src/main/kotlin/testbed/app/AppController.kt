package testbed.app

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import testbed.common.DefaultRandomGenerator
import testbed.core.AlphaTestbed
import testbed.core.EvaluationProtocol
import testbed.core.MetricSubscriber
import testbed.gui.ParametersGUI
import testbed.interfaces.*
import java.util.concurrent.Executors

class AppController : MetricSubscriber {

    @FXML
    var input: TextField? = null
    @FXML
    var start: Button? = null
    @FXML
    var init: Button? = null
    @FXML
    var stop: Button? = null
    @FXML
    var vbox: VBox? = null

    private val chart = LineChart(NumberAxis(), NumberAxis())
    private val metricData = HashMap<Metric, XYChart.Series<Number, Number>>()

    private val executor = Executors.newSingleThreadExecutor()

    @FXML
    protected fun initialize() {
        vbox!!.children.add(chart)
        input?.text = "1"
    }

    fun initButton(event: ActionEvent) {
        val gui = ParametersGUI(AppController::class.java.classLoader)
        gui.setBatchRun(false)

        val answer = gui.showDialog()

        if (answer != 0) return

        // seed
        val seed = input?.text?.let { Integer.parseInt(it) } ?: 1
        // set scenario
        val scenario = gui.setupParameters[0] as Scenario
        scenario.setRandomGenerator(DefaultRandomGenerator(seed))
        scenario.initialize(*gui.scenarioParameters)
        // set trust model
        val trustModel = gui.setupParameters[1] as TrustModel<*>
        trustModel.setRandomGenerator(DefaultRandomGenerator(seed))
        trustModel.initialize(*gui.trustModelParameters)
        // metrics
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

        val protocol = AlphaTestbed.getProtocol(trustModel, scenario, metrics)
        executor.execute {
            protocol.subscribe(this)
            for (time in 1..500) {
                protocol.step(time)
                Thread.yield()
            }
        }
    }
    
    fun startButton(event: ActionEvent) {}

    fun stopButton(event: ActionEvent) {
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