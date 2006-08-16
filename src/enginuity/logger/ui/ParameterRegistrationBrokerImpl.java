package enginuity.logger.ui;

import enginuity.Settings;
import enginuity.logger.LoggerController;
import enginuity.logger.LoggerControllerImpl;
import enginuity.logger.definition.EcuParameter;
import enginuity.logger.query.LoggerCallback;
import static enginuity.logger.ui.SpringUtilities.makeCompactGrid;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import static org.jfree.chart.plot.PlotOrientation.VERTICAL;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.synchronizedMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ParameterRegistrationBrokerImpl implements ParameterRegistrationBroker {
    private final LoggerController controller;
    private final LoggerDataTableModel dataTableModel;
    private final JPanel graphPanel;
    private final Map<EcuParameter, ChartPanel> chartMap = synchronizedMap(new HashMap<EcuParameter, ChartPanel>());
    private final List<EcuParameter> registeredEcuParameters = Collections.synchronizedList(new ArrayList<EcuParameter>());
    private int loggerCount = 0;
    private long loggerStartTime = 0;

    public ParameterRegistrationBrokerImpl(Settings settings, LoggerDataTableModel dataTableModel, JPanel graphPanel) {
        this.controller = new LoggerControllerImpl(settings);
        this.dataTableModel = dataTableModel;
        this.graphPanel = graphPanel;
    }

    public synchronized void registerEcuParameterForLogging(final EcuParameter ecuParam) {
        if (!registeredEcuParameters.contains(ecuParam)) {
            // add to datatable
            dataTableModel.addParam(ecuParam);

            // add to charts
            final XYSeries series = new XYSeries(ecuParam.getName());
            //TODO: Make chart max item count configurable via settings
            series.setMaximumItemCount(2000);
            final XYDataset xyDataset = new XYSeriesCollection(series);
            final JFreeChart chart = ChartFactory.createXYLineChart(ecuParam.getName(), "Time (sec)", ecuParam.getName() + " (" + ecuParam.getConvertor().getUnits() + ")",
                    xyDataset, VERTICAL, false, true, false);
            ChartPanel chartPanel = new ChartPanel(chart, false, true, true, true, true);
            graphPanel.add(chartPanel);
            //graphPanel.add(new ChartScrollBar(Adjustable.HORIZONTAL, chart));
            chartMap.put(ecuParam, chartPanel);
            makeCompactGrid(graphPanel, ++loggerCount, 1, 10, 10, 20, 20);
            repaintGraphPanel(2);

            // add to dashboard

            // add logger and setup callback
            controller.addLogger(ecuParam, new LoggerCallback() {
                public void callback(byte[] value) {
                    // update data table
                    dataTableModel.updateParam(ecuParam, value);

                    // update graph
                    series.add((System.currentTimeMillis() - loggerStartTime) / 1000, ecuParam.getConvertor().convert(value));

                    // update dashboard

                }
            });

            // add to registered parameters list
            registeredEcuParameters.add(ecuParam);
        }
    }

    public synchronized void deregisterEcuParameterFromLogging(EcuParameter ecuParam) {
        if (registeredEcuParameters.contains(ecuParam)) {
            // remove logger
            controller.removeLogger(ecuParam);

            // remove from datatable
            dataTableModel.removeParam(ecuParam);

            // remove from charts
            graphPanel.remove(chartMap.get(ecuParam));
            chartMap.remove(ecuParam);
            makeCompactGrid(graphPanel, --loggerCount, 1, 10, 10, 20, 20);
            repaintGraphPanel(1);

            // remove from dashboard

            // remove from registered list
            registeredEcuParameters.remove(ecuParam);
        }

    }

    public List<String> listSerialPorts() {
        return controller.listSerialPorts();
    }


    public synchronized void start() {
        loggerStartTime = System.currentTimeMillis();
        controller.start();
    }

    public synchronized void stop() {
        controller.stop();
    }

    private void repaintGraphPanel(int parentRepaintLevel) {
        if (loggerCount < parentRepaintLevel) {
            graphPanel.doLayout();
            graphPanel.repaint();
        } else {
            if (loggerCount == 1) {
                graphPanel.doLayout();
            }
            graphPanel.getParent().doLayout();
            graphPanel.getParent().repaint();
        }
    }

}
