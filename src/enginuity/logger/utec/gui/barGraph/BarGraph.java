/*
 * Created on May 26, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package enginuity.logger.utec.gui.barGraph;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;

/**
 * @author emorgan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BarGraph extends JComponent{
	
	public BarGraph(){
		
	}

	private void Init(){
		
	}
	
	public void paint(Graphics g) {
		
		//Dimensions of bounding entity
		Dimension theSize = this.getSize();
		int width = theSize.width;
		int height = theSize.height;
		
		
		Graphics2D g2 = (Graphics2D) g;
		GradientPaint gp = new GradientPaint((float)0, (float)0, Color.RED, (float)0, (float)height-10, Color.GREEN);
		
		RoundRectangle2D rect1 = new RoundRectangle2D.Double(30, 5, 90, 390, 10, 10);
		g2.setPaint(gp);
		g2.fill(rect1);
		g2.setPaint(Color.BLACK);
		g2.draw(rect1);
	}
}
