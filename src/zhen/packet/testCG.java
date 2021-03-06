package zhen.packet;

import inputGeneration.StaticInfo;
import inputGeneration.StaticLayout;
import inputGeneration.StaticViewNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class testCG {

	public static void main(String[] args){
		String path = "APK/SimpleCallGraphTestApp.apk";
		testSubCompoenent(path);
	}
	
	private static void testSubCompoenent(String path){
		long time1 = System.currentTimeMillis();
		
		File f =new File(path);
		inputGeneration.StaticInfo.initAnalysis(f, false);
//		analysisTools.Soot.generateAPKData(f);
		System.out.println("Act names:");
		ArrayList<String> list = StaticInfo.getActivityNames(f);
		printLineByLine(list);
		
//		System.out.println("Class names:");
		ArrayList<String> clist = StaticInfo.getClassNames(f);
//		printLineByLine(clist); -- correct
		
		String targetClass = "";
//		String targetClass = "com.example.simplecallgraphtestapp.SimpleTarget";
		for(String className : clist){
			if(className.toLowerCase().contains("simpletarget")){
				targetClass = className;
				break;
			}
		}
		System.out.println("target class:" +targetClass );
		System.out.println("Method names:");
		ArrayList<String> mList = StaticInfo.getAllMethodSignatures(f, targetClass);
		printLineByLine(mList);
		
		
		
		String targetMethod = "";
//		String targetMethod = "void Function_1(android.view.View)";
		for(String methodname: mList){
			if(methodname.toLowerCase().contains("function_2")){
				targetMethod = methodname;
				break;
			}
		}
		System.out.println("targetMethod:"+targetMethod);
		
		ArrayList<String> outCallTargetsList = StaticInfo.getOutCallTargets(targetClass, targetMethod);
		ArrayList<String> inCallTargetsList = StaticInfo.getInCallSources(targetClass, targetMethod);
		ArrayList<String> possibleCallSequencesList = StaticInfo.getPossibleCallSequences(targetClass, targetMethod);
		ArrayList<String> allPossibleIncomingCallersList = StaticInfo.getAllPossibleIncomingCallers(targetClass, targetMethod);
		
		System.out.println("getOutCallTargets");printLineByLine(outCallTargetsList);
		System.out.println("getInCallSources");printLineByLine(inCallTargetsList);
		System.out.println("getPossibleCallSequences");printLineByLine(possibleCallSequencesList);
		System.out.println("getAllPossibleIncomingCallers");printLineByLine(allPossibleIncomingCallersList);
		
		
		System.out.println("Until now: "+String.format("%.3f", (System.currentTimeMillis()-time1)/1000.0));
		System.out.println("Using possibleCallSequencesList");
		
		//ignore onCreate
		Map<String,ArrayList<String>> methodToHandler = new HashMap<String,ArrayList<String>>();
		for(String sequence: possibleCallSequencesList){
			ArrayList<String> handlerList = new ArrayList<String>();
			String[] sub_methodList = sequence.split(",");
			for(int i =1;i<sub_methodList.length;i++){
				String methodSig = sub_methodList[i];
				System.out.println("methodSig:"+methodSig);
				String parts[] = methodSig.split(":");
				String className = parts[0];
				String methodName = parts[1];
				ArrayList<String> tmp = StaticInfo.findEventHandlersThatMightDirectlyCallThisMethod(f, className, methodName);
				handlerList.addAll(tmp);
			}
			System.out.println("Handler:");
			System.out.println(handlerList);
			methodToHandler.put(sub_methodList[0], handlerList);
		}
		
		//com.example.simplecallgraphtestapp.MainActivity,activity_main,trigger2,android:onClick
		long total = System.currentTimeMillis();
		System.out.println("Total time: "+String.format("%.3f", (total-time1)/1000.0));
	}
	
//	private boolean isHanlerClass(String className){
//		
//		
//		return false;
//	}
	static void printLineByLine(List li){
		if(li == null || li.isEmpty()){
			System.out.println("Empty");
			return;
		}
		
		for(Object o:li){
			System.out.println(o);
		}
	}
}
