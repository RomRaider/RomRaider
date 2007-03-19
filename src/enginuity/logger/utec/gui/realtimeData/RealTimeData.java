package enginuity.logger.utec.gui.realtimeData;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

import enginuity.logger.utec.gui.bottomControl.*;
import enginuity.logger.utec.gui.mapTabs.UtecDataManager;
import enginuity.logger.utec.properties.UtecProperties;
import enginuity.tts.SpeakString;
import enginuity.logger.utec.commEvent.*;
import enginuity.logger.utec.commInterface.UtecInterface;

/**
 * @author botman
 *
 * Class displays live data from the UTEC
 */
public class RealTimeData extends Component implements LoggerDataListener{

	//Recieved utec data, start values are zero
	public double[] doubleData = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	
	//Graph Constants
	private double maxHeight = 370;
	
	//Window Constants
	private int windowHeight = 400;
	
	// Needed data
	String[] indexes = UtecProperties.getProperties("utec.loggerIndexes");
	String[] titles = UtecProperties.getProperties("utec.loggerTitles");
	String[] dataRanges = UtecProperties.getProperties("utec.loggerDataRanges");
	
	//Constructor
	public RealTimeData() {
		UtecDataManager.addLoggerListener(this);
	}

	/**
	 * Sets the used ports
	 * @param portChoice
	 */
	public void setPort(String portChoice) {
		UtecInterface.setPortChoice(portChoice);
	}
	
	public void paint(Graphics g) {
		
		Graphics2D g2 = (Graphics2D) g;
		GradientPaint gp = new GradientPaint((float)30, (float)-10, Color.RED, (float)30, (float)maxHeight, Color.GREEN);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Font font = new Font("Serif", Font.PLAIN, 20);
		g2.setFont(font);
		
		// 0
		double max1 = (minAndMax(dataRanges[0]))[1];
		double min1 = (minAndMax(dataRanges[0]))[0];
		double value1 = doubleData[Integer.parseInt(indexes[0])];
		double height1 = ((value1 - min1)/(max1 - min1))*maxHeight;
		double yValue1 = 5 + (maxHeight - height1);
		double xValueLeft1 = 30;
		double width1 = 90;
		RoundRectangle2D rect1 = new RoundRectangle2D.Double(xValueLeft1, yValue1, width1, height1, 10, 10);
		g2.setPaint(gp);
		g2.fill(rect1);
		g2.setPaint(Color.BLACK);
		g2.draw(rect1);
		g2.drawString(titles[0]+":"+(int)value1, (int)xValueLeft1, windowHeight);
		
		// 1
		double max2 = (minAndMax(dataRanges[1]))[1];
		double min2 = (minAndMax(dataRanges[1]))[0];
		double value2 = doubleData[Integer.parseInt(indexes[1])];
		double maxAllowed2 = Double.parseDouble(UtecProperties.getProperties("utec.boostLimitWarningMaxAcceptable")[0]);
		if(value2 > maxAllowed2){
			SpeakString vc = new SpeakString("Max P S I,"+value2);
		}
		double height2 = ((value2 - min2)/(max2 - min2))*maxHeight;
		double yValue2 = 5 + (maxHeight - height2);
		double xValueLeft2 = 150;
		double width2 = 90;
		RoundRectangle2D rect2 = new RoundRectangle2D.Double(xValueLeft2, yValue2, width2, height2, 10, 10);
		g2.setPaint(gp);
		g2.fill(rect2);
		g2.setPaint(Color.BLACK);
		g2.draw(rect2);
		g2.drawString(titles[1]+":"+value2, (int)xValueLeft2, windowHeight);
		
		// 2
		double max3 = (minAndMax(dataRanges[2]))[1];
		double min3 = (minAndMax(dataRanges[2]))[0];
		double value3 = doubleData[Integer.parseInt(indexes[2])];
		double maxAllowed3 = Double.parseDouble(UtecProperties.getProperties("utec.knockCountWarningMaxAcceptable")[0]);
		if(value3 > maxAllowed3){
			SpeakString vc = new SpeakString("knock, count "+(int)value3);

			Toolkit.getDefaultToolkit().beep();
			Toolkit.getDefaultToolkit().beep();
			Toolkit.getDefaultToolkit().beep();
		}
		double height3 = ((value3 - min3)/(max3 - min3))*maxHeight;
		double yValue3 = 5 + (maxHeight - height3);
		double xValueLeft3 = 280;
		double width3 = 90;
		RoundRectangle2D rect3 = new RoundRectangle2D.Double(xValueLeft3, yValue3, width3, height3, 10, 10);
		g2.setPaint(gp);
		g2.fill(rect3);
		g2.setPaint(Color.BLACK);
		g2.draw(rect3);
		g2.drawString(titles[2]+":"+(int)value3, (int)xValueLeft3, windowHeight);
		
		// 3
		double max4 = (minAndMax(dataRanges[3]))[1];
		double min4 = (minAndMax(dataRanges[3]))[0];
		double value4 = doubleData[Integer.parseInt(indexes[3])];
		double height4 = ((value4 - min4)/(max4 - min4))*maxHeight;
		double yValue4 = 5 + (maxHeight - height4);
		double xValueLeft4 = 400;
		double width4 = 90;
		RoundRectangle2D rect4 = new RoundRectangle2D.Double(xValueLeft4, yValue4, width4, height4, 10, 10);
		g2.setPaint(gp);
		g2.fill(rect4);
		g2.setPaint(Color.BLACK);
		g2.draw(rect4);
		g2.drawString(titles[3]+":"+value4, (int)xValueLeft4, windowHeight);
		
		// 4
		double max5 = (minAndMax(dataRanges[4]))[1];
		double min5 = (minAndMax(dataRanges[4]))[0];
		double value5 = doubleData[Integer.parseInt(indexes[4])];
		double maxAllowed5 = Double.parseDouble(UtecProperties.getProperties("utec.injectorCycleWarningMaxAcceptable")[0]);
		if(value5 > maxAllowed5){
			SpeakString vc = new SpeakString("Max injector duty "+value5);
		}
		double height5 = ((value5 - min5)/(max5 - min5))*maxHeight;
		double yValue5 = 5 + (maxHeight - height5);
		double xValueLeft5 = 530;
		double width5 = 90;
		RoundRectangle2D rect5 = new RoundRectangle2D.Double(xValueLeft5, yValue5, width5, height5, 10, 10);
		g2.setPaint(gp);
		g2.fill(rect5);
		g2.setPaint(Color.BLACK);
		g2.draw(rect5);
		g2.drawString(titles[4]+":"+value5, (int)xValueLeft5, windowHeight);
		
		// 5
		double max6 = (minAndMax(dataRanges[5]))[1];
		double min6 = (minAndMax(dataRanges[5]))[0];
		double value6 = doubleData[Integer.parseInt(indexes[5])];
		double height6 = ((value6 - min6)/(max6 - min6))*maxHeight;
		double yValue6 = 5 + (maxHeight - height6);
		double xValueLeft6 = 660;
		double width6 = 90;
		RoundRectangle2D rect6 = new RoundRectangle2D.Double(xValueLeft6, yValue6, width6, height6, 10, 10);
		g2.setPaint(gp);
		g2.fill(rect6);
		g2.setPaint(Color.BLACK);
		g2.draw(rect6);
		g2.drawString(titles[5]+":"+value6, (int)xValueLeft6, windowHeight);
		
	}
	
	public void getCommEvent(double[] doubleData){
		this.doubleData = doubleData;
		//System.out.println("Got new data:"+doubleData[0]+"," + doubleData[1]);
		this.repaint();
	}
	
	private double[] minAndMax(String value){
		String[] temp = value.split("<>");
		
		double[] values = new double[2];
		values[0] = Double.parseDouble(temp[0]);
		values[1] = Double.parseDouble(temp[1]);
		
		return values;
	}
}
