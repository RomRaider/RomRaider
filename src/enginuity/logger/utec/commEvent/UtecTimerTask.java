package enginuity.logger.utec.commEvent;

import java.util.TimerTask;

import enginuity.logger.utec.comm.UtecSerialConnectionManager;
import enginuity.logger.utec.gui.JutecGUI;

public class UtecTimerTask extends TimerTask{
	private UtecTimerTaskListener listener = null;
	private String data = "";
	private int counter = 0;
	private StringBuffer stringBuffer = null;
	
	public UtecTimerTask(UtecTimerTaskListener listener){
		this.listener = listener;
	}
	
	public void setData(String data){
		this.data = data;
		this.stringBuffer = new StringBuffer(data);
		//JutecGUI.getInstance().getJProgressBar().setMinimum(0);
		//JutecGUI.getInstance().getJProgressBar().setMaximum(data.length());
	}
	
	public void run(){
		char theChar = stringBuffer.charAt(counter);
		System.out.println("->"+theChar+"<-  :"+(int)theChar+"");
		
		//Send the data to the Utec
		UtecSerialConnectionManager.sendCommandToUtec((int)theChar);
		
		counter++;
		//JutecGUI.getInstance().getJProgressBar().setValue(counter);
		
		// Kill the timer after a at the end of the string
		if(counter == data.length()){
			this.cancel();
			listener.utecCommTimerCompleted();
		}
	}
}
