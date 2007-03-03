package enginuity.logger.utec.commEvent;

import java.util.Timer;

import enginuity.logger.utec.properties.UtecProperties;

public class UtecTimerTaskExecute implements UtecTimerTaskListener{
	private final Timer timer = new Timer();
	private int delay = Integer.parseInt(UtecProperties.getProperties("utec.commandTransmissionPauseMS")[0]);
	private int period = Integer.parseInt(UtecProperties.getProperties("utec.dataTransmissionPauseMS")[0]);
	private UtecTimerTask utecTimerTask = new UtecTimerTask( this);
	
	public UtecTimerTaskExecute(String data){
		utecTimerTask.setData(data);
		timer.schedule(utecTimerTask, delay, period);
	}
	
	public void utecCommTimerCompleted() {
		// Ensure that the manager knows we are done
		UtecTimerTaskManager.operationComplete();
		
		timer.cancel();
	}
}
