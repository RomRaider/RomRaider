package enginuity.logger.utec.gui.realtimeData;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

import enginuity.logger.utec.gui.bottomControl.*;
import enginuity.tts.VoiceThread;
import enginuity.logger.utec.commEvent.*;
import enginuity.logger.utec.commInterface.CommInterface;

/**
 * @author botman
 *
 * Class displays live data from the UTEC
 */
public class RealTimeData extends JComponent implements CommListener{

	//Buttons to be used
	private JButton openButton;
	private JButton closeButton;
	private JButton startButton;
	private JButton stopButton;

	//Text areas to be used
	private JTextArea textFromUtec;

	//Recieved utec data, start values are zero
	public String[] stringData = {"0","0","0","0","0","0"};
	public double[] doubleData = {0,0,0,0,0,0};
	
	//Graph Constants
	private double maxHeight = 370;
	
	//Window Constants
	private int windowHeight = 400;
	
	//Constructor
	public RealTimeData() {
		CommInterface.addListener(this);
	}

	/**
	 * Sets the used ports
	 * @param portChoice
	 */
	public void setPort(String portChoice) {
		CommInterface.setPortChoice(portChoice);
	}
	
	public void paint(Graphics g) {
		
		Graphics2D g2 = (Graphics2D) g;
		GradientPaint gp = new GradientPaint((float)30, (float)-10, Color.RED, (float)30, (float)maxHeight, Color.GREEN);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Font font = new Font("Serif", Font.PLAIN, 20);
		g2.setFont(font);
		
		//RPM
		double max1 = 7500;
		double min1 = 0;
		double value1 = doubleData[0];
		double height1 = ((value1 - min1)/(max1 - min1))*maxHeight;
		double yValue1 = 5 + (maxHeight - height1);
		double xValueLeft1 = 30;
		double width1 = 90;
		RoundRectangle2D rect1 = new RoundRectangle2D.Double(xValueLeft1, yValue1, width1, height1, 10, 10);
		g2.setPaint(gp);
		g2.fill(rect1);
		g2.setPaint(Color.BLACK);
		g2.draw(rect1);
		g2.drawString("RPM:"+(int)value1, (int)xValueLeft1, windowHeight);
		
		//PSI
		double max2 = 20;
		double min2 = -14.7;
		double value2 = doubleData[1];
		if(value2 > 18.5){
			VoiceThread vc = new VoiceThread("Max P S I,"+value2);
			vc.start();
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
		g2.drawString("PSI:"+value2, (int)xValueLeft2, windowHeight);
		
		//KNOCK
		double max3 = 20;
		double min3 = 0;
		double value3 = doubleData[2];
		if(value3 > 0){
			VoiceThread vc = new VoiceThread("knock, count "+(int)value3);
			vc.start();
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
		g2.drawString("KNOCK:"+(int)value3, (int)xValueLeft3, windowHeight);
		
		//IGN
		double max4 = 80;
		double min4 = 0;
		double value4 = doubleData[3];
		double height4 = ((value4 - min4)/(max4 - min4))*maxHeight;
		double yValue4 = 5 + (maxHeight - height4);
		double xValueLeft4 = 400;
		double width4 = 90;
		RoundRectangle2D rect4 = new RoundRectangle2D.Double(xValueLeft4, yValue4, width4, height4, 10, 10);
		g2.setPaint(gp);
		g2.fill(rect4);
		g2.setPaint(Color.BLACK);
		g2.draw(rect4);
		g2.drawString("IGN:"+value4, (int)xValueLeft4, windowHeight);
		
		//DUTY
		double max5 = 105;
		double min5 = 0;
		double value5 = doubleData[4];
		if(value5 > 98){
			VoiceThread vc = new VoiceThread("Max duty "+value5);
			vc.start();
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
		g2.drawString("DUTY:"+value5, (int)xValueLeft5, windowHeight);
		
		//AFR
		double max6 = 26;
		double min6 = 0;
		double value6 = doubleData[5];
		double height6 = ((value6 - min6)/(max6 - min6))*maxHeight;
		double yValue6 = 5 + (maxHeight - height6);
		double xValueLeft6 = 660;
		double width6 = 90;
		RoundRectangle2D rect6 = new RoundRectangle2D.Double(xValueLeft6, yValue6, width6, height6, 10, 10);
		g2.setPaint(gp);
		g2.fill(rect6);
		g2.setPaint(Color.BLACK);
		g2.draw(rect6);
		g2.drawString("AFR:"+value6, (int)xValueLeft6, windowHeight);
		
	}
	
	public void getCommEvent(CommEvent e){
		if(e.isLoggerData()){
			stringData = e.getData();
			doubleData = e.getDoubleData();
			this.repaint();
		}
	}
}
