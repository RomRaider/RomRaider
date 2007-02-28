package enginuity.logger.utec.commEvent;

public class TestUtecTimer{
	
	public static void main(String[] args){
		UtecTimerTaskManager.execute(33);
		
		UtecTimerTaskManager.execute(new StringBuffer("Hi there bababababab"));
		
		UtecTimerTaskManager.execute("This is a test.");
		
		UtecTimerTaskManager.execute("I love cheese.");
		
	}
	
}
