package main;

public class Paths {

	/**
	 * Readme:
	 * appDataDir and androidSDKPath are the two directories that you need change for 
	 * your local run.
	 * 
	 * androidSDKPath is the place where sdk locates on your computer
	 * appDataDir is the place where the generated files stored.
	 */
	
	public static String androidSDKPath = "/home/wenhaoc/bin/adt-bundle-linux-x86_64-20140702/";
	public static String appDataDir = "/home/wenhaoc/AppData/";

	
	public static String androidToolPath = androidSDKPath+"sdk/tools/";
	public static String androidPlatformToolPath = androidSDKPath+"/sdk/platform-tools/";
	public static String adbPath = androidPlatformToolPath+"adb";
	public static String androidJarPath = "libs/android.jar";
	public static String apktoolPath = "libs/apktool.jar";
}
