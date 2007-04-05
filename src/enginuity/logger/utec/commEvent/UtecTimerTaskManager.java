package enginuity.logger.utec.commEvent;

import java.util.LinkedList;
import java.util.Queue;

public class UtecTimerTaskManager {
	private static Queue<String> theQueue = new LinkedList<String>();
	
	private static String currentTask = null;
	
	private static boolean isExecuting = false;
	
	/**
	 * String value, sent one char at a time.
	 * @param command
	 */
	public static void execute(String command){
		if(theQueue.peek() == null){
			if(isExecuting == true){
				theQueue.add(command);
			}else{
				isExecuting = true;
				new UtecTimerTaskExecute(command);
			}
		}else{
			theQueue.add(command);
		}
	}
	
	/**
	 * Char INT value.
	 * @param command
	 */
	public static void execute(int command){
		if(theQueue.peek() == null){
			if(isExecuting == true){
				theQueue.add((char)command+"");
			}else{
				isExecuting = true;
				new UtecTimerTaskExecute((char)command+"");
			}
		}else{
			theQueue.add((char)command+"");
		}
	}
	
	/**
	 * StringBuffer support
	 * @param command
	 */
	public static void execute(StringBuffer command){
		
		if(theQueue.peek() == null){
			if(isExecuting == true){
				theQueue.add(command.toString());
			}else{
				isExecuting = true;
				new UtecTimerTaskExecute(command.toString());
			}
		}else{
			theQueue.add(command.toString());
		}
	}
	
	/**
	 * Called when an executing task is finished
	 *
	 */
	public static void operationComplete(){
		System.out.println("--------");
		currentTask = theQueue.poll();
		
		// See if there is more to execute
		if(currentTask != null){
			isExecuting = true;
			new UtecTimerTaskExecute(currentTask);
		}else{
			isExecuting = false;
		}
	}
}
