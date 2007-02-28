package enginuity.logger.utec.commEvent;

import java.util.Timer;

import enginuity.logger.utec.properties.UtecProperties;

public class UtecTimerTaskExecute implements UtecTimerTaskListener{
	private final Timer timer = new Timer();
	
	private int delay = Integer.parseInt(UtecProperties.getProperties("utec.commandTransmissionPauseMS")[0]);
	private int period = Integer.parseInt(UtecProperties.getProperties("utec.dataTransmissionPauseMS")[0]);
	
	public UtecTimerTaskExecute(String data){
		UtecTimerTask utecTimerTask = new UtecTimerTask(data, this);
		
		timer.schedule(utecTimerTask, delay, period);
	}
	
	public void utecCommTimerCompleted() {
		
		// Ensure that the manager knows we are done
		UtecTimerTaskManager.operationComplete();
		
		timer.cancel();
	}
}
