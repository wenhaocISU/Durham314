package zhen.version1.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.Map.Entry;

import john.generateSequences.GenerateSequences;
import john.runtimeValidation.GenerateValidationScripts;
import zhen.version1.Support.Utility;
import zhen.version1.component.Event;
import zhen.version1.component.UIState;
import zhen.version1.framework.Common;
import zhen.version1.framework.Framework;
import zhen.version1.framework.RunTimeInformation;
import zhen.version1.framework.UIExplorer;
import zhen.version1.framework.UIExplorer.StepControlCallBack;

public class SampleTestClass {
	
	public static void main(String[] args) {
		//choose the name of apk file
		String[] name = {
				"signed_backupHelper.apk",
				"signed_Butane.apk",
				"signed_CalcA.apk",
				"signed_KitteyKittey.apk",
		};
		int index = 2;

		String path = "APK/"+name[index];
		
		//setup input parameters
		Map<String,Object> att = new HashMap<String,Object>();
		att.put(Common.apkPath	, path);
		
		Framework frame = new Framework(att);
		
		//once the step control is added, the program will wait for human instruction
		//before entering next operation loop
//		addExplorerStepControl(frame);
		//add a call back method which is called after the finish of traversal
		addOnTraverseFinishCallBack(frame);
		
		frame.enableStdinMonitor(false);

		//NOTE: right now it does require apk installed on the device manually
		//		and please close the app if previous opened
		frame.setup();//initialize
		frame.start();//start experiment
		
//		frame.terminate();
		
		johnsTest(frame);
		
		System.out.println("Finish!");
	}
	
	private static void johnsTest(Framework frame)
	{
		ArrayList<String> targets = new ArrayList<String>();
		targets.add("com.bae.drape.gui.calculator.CalculatorActivity: void handleOperation(com.bae.drape.gui.calculator.CalculatorActivity$Operation)");
		//targets.add("com.bae.drape.gui.calculator.CalculatorActivity: void handleNumber(int)");
		//targets.add("com.cs141.kittey.kittey.MainKitteyActivity: void nextButton(android.view.View)");
		
		System.out.println("START PRINTING MY STUFF");
		
		GenerateSequences gs = new GenerateSequences(frame, targets, false);
		ArrayList<String> enhancedSequences = gs.getEnhancedSequences();
		
		for (String string: enhancedSequences) {
			System.out.println(string);
		}
		
		GenerateValidationScripts gvs = new GenerateValidationScripts("handleOperation", "com.bae.drape.gui.calculator", "CalculatorActivity");
		gvs.setEventSequences(enhancedSequences);
		try {
			gvs.generateScripts();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("DONE PRINTING MY STUFF");
	}
	
	private static void addExplorerStepControl( Framework frame){
		UIExplorer explorer = frame.explorer;
		explorer.enableStepControl(true);
		explorer.setStepControlCallBack(UIExplorer.defaultCallBack);
	}
	
	private static void addOnTraverseFinishCallBack(Framework frame){
		frame.setOnProcedureEndsCallBack(new Framework.OnProcedureEndsCallBack(){
			@Override
			public void action(Framework frame) {
				Utility.info(Framework.TAG, "OnTraverseFinishCallBack ");
				// show the map of method -> event
				// and find a set possible events which leads to a method
				String targetMethod = "com.bae.drape.gui.calculaor.CalculatorActivity: void handleOperation(com.bae.drape.gui.calculator.CalculatorActivity$Operation)";
				Map<String, List<Event>> map = frame.rInfo.getMethodEventMap();
				List<Event> events = null;
				for(Entry<String, List<Event>> entry : map.entrySet()){
					String key = entry.getKey();
					Utility.info(RunTimeInformation.TAG,entry);	//show everything
					if(key.trim().equals(targetMethod)){
						Utility.info(Framework.TAG, "found targetMethod");
						events = entry.getValue(); break;
					}
				}
				
				//sample of entry.toString()
				//	com.example.backupHelper.BackupFilesListAdapter: void reset(boolean)
				//	=[launch com.example.backupHelper/com.example.backupHelper.BackupActivity]     
				if(events!= null && !events.isEmpty()){
					Utility.info(Framework.TAG, "Possible event set");
					for(Event event : events){
						Utility.info(Framework.TAG, event);
					}
					Event targetEvent = events.get(0);
					UIState targetUI = targetEvent.getSource();
					List<Event> path = frame.rInfo.getEventSequence(UIState.Launcher, targetUI);
					if(path == null){
						if(targetEvent.getEventType() == Event.iLAUNCH){
							path = new ArrayList<Event>();
							path.add(targetEvent);
						}
					}else{
						path.add(targetEvent);
					}
					
					Utility.info(Framework.TAG, "Path to UI with event which trigger the target");
					Utility.info(Framework.TAG, path);
					
					//reply the event sequence
					if(path!=null && path.isEmpty()){
						frame.executer.applyEventSequence(path.toArray(new Event[0]));
					}
				}else{
					Utility.info(Framework.TAG, "Event set empty");
				}
			}
		});
	}
}
