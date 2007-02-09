/*
 * Created on May 28, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package enginuity.logger.utec.commEvent;



import java.util.*;
/**
 * @author emorgan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CommEvent {
	public String UtecBuffer = null;
	public String[] data = new String[6];
	public double[] doubleData = new double[6];
	
	public CommEvent(String buffer){
		UtecBuffer = buffer;
		StringTokenizer st = new StringTokenizer(UtecBuffer, ",");
		int counter = 0;
		while(st.hasMoreTokens()){
			String theData = st.nextToken();
			
			//RPM
			if(counter == 0){
				data[0] = theData;
			}
			
			//PSI
			if(counter == 1){
				data[1] = theData;
			}
			
			//KNOCK
			if(counter == 5){
				data[2] = theData;
			}
			
			//IGN
			if(counter == 6){
				data[3] = theData;
			}
			
			//DUTY
			if(counter == 7){
				data[4] = theData;
			}
			
			//AFR
			if(counter == 13){
				data[5] = theData;
			}
			
			counter++;
		}
		
		for(int i = 0; i < 6; i++){
			String theData = data[i];
			theData = theData.trim();
			if(theData.startsWith(">")){
				theData = "25.5";
			}
			if(theData.startsWith("--")){
				theData = "0.0";
			}
			doubleData[i] = Double.parseDouble(theData);
		}
		
	}
}
