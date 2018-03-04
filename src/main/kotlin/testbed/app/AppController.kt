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
    lateinit var input: TextField
    @FXML
    lateinit var init: Button
    @FXML
    lateinit var stop: Button
    @FXML
    lateinit var verticalBox: VBox

    private val chart = LineChart(
            NumberAxis().apply {
                tickUnit = 25.0
                lowerBound = 0.0
            },
            NumberAxis().apply {
                upperBound = 1.0
                lowerBound = 0.0
                tickUnit = 0.05
                isAutoRanging = false
            })

    private val metricData = HashMap<Metric, XYChart.Series<Number, Number>>()

    private val executor = Executors.newSingleThreadExecutor()

    @FXML
    fun initialize() {
        verticalBox.apply {
            children += chart
        }
        input.text = "1"
    }

    fun initButton(event: ActionEvent) {
        val gui = ParametersGUI(AppController::class.java.classLoader)
        gui.setBatchRun(true)

        val answer = gui.showDialog()

        if (answer != 0) {
            return
        }

        val seed = Integer.parseInt(input.text)

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

        executor.execute {
            for (time in 1..duration) {
                protocol.step(time)
            }
        }
    }

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