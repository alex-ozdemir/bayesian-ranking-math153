package aozdemir.bayesianFinal;

import java.awt.BasicStroke;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Plot {
	public static void plot(ArrayList<double[]> x,
			ArrayList<double[]> y,
			ArrayList<String> seriesNames,
			String fileName,
			String title,
			String xLabel,
			String yLabel,
			boolean legend) {
		
		
		//Verify numbers match up
		if ((x.size() != y.size()) || (y.size() != seriesNames.size())) {
			System.err.println("Must submit the same number of series names and value");
			return;
		}
		
		// Create a simple XY chart


		XYSeries[] series = new XYSeries[x.size()];
		for (int j = 0; j < series.length; j++) {
			if (x.get(j).length != y.get(j).length) {
				System.err.println("Must give the same number of X and Y Values in: " + seriesNames.get(j));
				return;
			}
			series[j] = new XYSeries(seriesNames.get(j));
			for (int i = 0; i < x.get(j).length; i++) {
				series[j].add(x.get(j)[i], y.get(j)[i]);
			}
		}
		
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (int j = 0; j < series.length; j++) {
			dataset.addSeries(series[j]);
		}
		// Generate the graph
		JFreeChart chart = ChartFactory.createXYLineChart(
				title, // Title
				xLabel, // x-axis Label
				yLabel, // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				legend, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
				);
		for (int i = 0; i < x.size(); i++) setSeriesStyle(chart, i, "line");
		try {
			ChartUtilities.saveChartAsJPEG(new File("data/"+fileName+".jpg"), chart, 700, 600);
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
	}
	
    /** Line style: line */
    public static final String STYLE_LINE = "line";
    /** Line style: dashed */
    public static final String STYLE_DASH = "dash";
    /** Line style: dotted */
    public static final String STYLE_DOT = "dot";

    /**
    * Convert style string to stroke object.
    * 
    * @param style One of STYLE_xxx.
    * @return Stroke for <i>style</i> or null if style not supported.
    */
   private static BasicStroke toStroke(String style) {
        BasicStroke result = null;
        
        if (style != null) {
            float lineWidth = 3.0f;
            float dash[] = {5.0f};
            float dot[] = {lineWidth};
    
            if (style.equalsIgnoreCase(STYLE_LINE)) {
                result = new BasicStroke(lineWidth);
            } else if (style.equalsIgnoreCase(STYLE_DASH)) {
                result = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
            } else if (style.equalsIgnoreCase(STYLE_DOT)) {
                result = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f, dot, 0.0f);
            }
        }//else: input unavailable
        
        return result;
    }//toStroke()

    /**
     * Set color of series.
     * 
     * @param chart JFreeChart.
     * @param seriesIndex Index of series to set color of (0 = first series)
     * @param style One of STYLE_xxx.
     */
    public static void setSeriesStyle(JFreeChart chart, int seriesIndex, String style) {
        if (chart != null && style != null) {
            BasicStroke stroke = toStroke(style);
            
            XYPlot plot = (XYPlot) chart.getPlot();
            if (plot instanceof XYPlot) {
                XYPlot xyPlot = chart.getXYPlot();
                XYItemRenderer xyir = xyPlot.getRenderer();
                try {
                    xyir.setSeriesStroke(seriesIndex, stroke); //series line style
                } catch (Exception e) {
                    System.err.println("Error setting style '"+style+"' for series '"+seriesIndex+"' of chart '"+chart+"': "+e);
                }
            } else {
                System.out.println("setSeriesColor() unsupported plot: "+plot);
            }
        }//else: input unavailable
    }//setSeriesStyle()
	
	public static void plot(double[] x,
			double[] y,
			String seriesNames,
			String fileName,
			String title,
			String xLabel,
			String yLabel,
			boolean legend) {
		
		
		//Verify numbers match up
		if (x.length != y.length) {
			System.err.println("Must give the same number of X and Y Values in: " + seriesNames);
			return;
		}

		// Create a simple XY chart


		XYSeries series = new XYSeries(seriesNames);
		for (int i = 0; i < x.length; i++) {
			series.add(x[i], y[i]);
		}
		
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series);
		// Generate the graph
		JFreeChart chart = ChartFactory.createXYLineChart(
				title, // Title
				xLabel, // x-axis Label
				yLabel, // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				legend, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
				);
		try {
			ChartUtilities.saveChartAsJPEG(new File("data/"+fileName+".jpg"), chart, 700, 600);
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
	}
}
