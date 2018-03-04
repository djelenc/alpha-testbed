package testbed.app;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import testbed.common.DefaultRandomGenerator;
import testbed.core.AlphaTestbed;
import testbed.core.EvaluationProtocol;
import testbed.core.MetricSubscriber;
import testbed.interfaces.Metric;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;
import testbed.metric.CumulativeNormalizedUtility;
import testbed.metric.DefaultOpinionCost;
import testbed.metric.KendallsTauA;
import testbed.scenario.TransitiveOpinionProviderSelection;
import testbed.trustmodel.SimpleSelectingOpinionProviders;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppController implements MetricSubscriber {

    public TextField input;
    public Button start;
    public Button init;
    public Button stop;
    public VBox vbox;

    private final NumberAxis xAxis = new NumberAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private final LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);

    private Metric accuracy, utility, opinionCost;
    private XYChart.Series<Number, Number> accuracySeries, utilitySeries, opinionCostSeries;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @FXML
    protected void initialize() {
        vbox.getChildren().add(chart);
    }

    public void initButton(ActionEvent event) {
        // series.getData().clear();
        accuracy = new KendallsTauA();
        accuracySeries = new XYChart.Series<>();
        accuracySeries.setName(accuracy.toString());
        chart.getData().add(accuracySeries);

        utility = new CumulativeNormalizedUtility();
        utilitySeries = new XYChart.Series<>();
        utilitySeries.setName(utility.toString());
        chart.getData().add(utilitySeries);

        opinionCost = new DefaultOpinionCost();
        opinionCostSeries = new XYChart.Series<>();
        opinionCostSeries.setName(opinionCost.toString());
        chart.getData().add(opinionCostSeries);
    }


    public void startButton(ActionEvent event) {
        executor.execute(() -> {
            final TrustModel<?> model = new SimpleSelectingOpinionProviders();
            model.setRandomGenerator(new DefaultRandomGenerator(0));
            model.initialize();

            final Scenario scenario = new TransitiveOpinionProviderSelection();
            scenario.setRandomGenerator(new DefaultRandomGenerator(0));
            scenario.initialize(100, 0.05, 0.1, 1d, 1d);

            final Map<Metric, Object[]> metrics = new HashMap<>();
            metrics.put(accuracy, null);
            metrics.put(utility, null);
            metrics.put(opinionCost, null);

            final EvaluationProtocol ep = AlphaTestbed.getProtocol(model, scenario, metrics);
            ep.subscribe(this);

            for (int time = 1; time <= 500; time++) {
                ep.step(time);
            }
        });
    }

    public void stopButton(ActionEvent event) {
        executor.shutdownNow();
    }

    @Override
    public void update(final EvaluationProtocol instance) {
        Platform.runLater(() -> {
            accuracySeries.getData().add(new XYChart.Data<>(instance.getTime(), instance.getResult(0, accuracy)));
            utilitySeries.getData().add(new XYChart.Data<>(instance.getTime(), instance.getResult(0, utility)));
            opinionCostSeries.getData().add(new XYChart.Data<>(instance.getTime(), instance.getResult(0, opinionCost)));
        });
    }
}
