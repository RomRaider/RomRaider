Here are steps to setup Visual Studio Code to build and debug RomRaider.

1. Download Git for your operating system. You'll use git to clone the RomRaider repository.
	> https://git-scm.com/downloads
	
1. Download a Java OpenJDK 8 32bit version, several options are available: RedHat, Oracle, Adopt, etc. I use AdoptOpenJdk 8 just because I don't have to register to download it.

	When installing, if there's a option to set JAVA_HOME variable, have the installer do it. You may have to edit it manually later, Adopt lets you set this during OpenJDK install.
	
	> Links:
	>
	> [Red Hat OpenJdk](https://developers.redhat.com/products/openjdk/download?extIdCarryOver=true&sc_cid=701f2000000RWTnAAO)
	>
	> [Adopt OpenJdk](https://adoptopenjdk.net/releases.html?variant=openjdk8&jvmVariant=openj9)

1. Download ANT: https://ant.apache.org/bindownload.cgi
	> - 1.10.7 release - requires minimum of Java 8 at runtime
	> - Unzip **ANT** to a known location. 
	> 
	> For example, I use windows and decided on: 'C:\Users\Walter\ANT'
	
1. Add '**ANT_HOME**' as a System Environment variable excluding the quotes.
	> - For the value use the unzipped ANT path from the previous step. For Example: 'C:\Users\<USERNAME>\ANT'
	> - If you do not know how to add a environment variable, see: https://docs.oracle.com/javase/tutorial/essential/environment/paths.html

1. Add '**JRE_DIR**' as a System Environment variable excluding the quotes.
	> - For the value use the directory from the Java install. For Example: 'C:\Program Files (x86)\Java\jre-1.8'
	> - If you do not know how to add a environment variable, see: https://docs.oracle.com/javase/tutorial/essential/environment/paths.html

1. Edit the existing '**PATH**' System Environment, add the directory you unzipped ANT to with the \bin directory appended.
	> For Example: 'C:\Users\<USERNAME>\ANT\bin'
		
1. Download & Install '**Visual Studio Code**':
	> https://code.visualstudio.com/

1. Download & Install '**Java Extension Pack**' VsCode Extension:
	> https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack

1. Optional: Download & Install '**Ant Target Runner**' Extension: https://marketplace.visualstudio.com/items?itemName=nickheap.vscode-ant

1. Open the RomRaider folder in VS Code. Ensure the Explorer panel is open. 
	> 'View Menu > Explorer'

1. Open the terminal window '**View Menu > Terminal**'
	> - Type the following and press enter: ant all
	> - Alternately, you can use the 'Ant Target Runner' panel, it should be underneath the files list. Right click the 'all' node and select 'Run Ant Target'
	> - *You will need to do one of the above each time you make a code change.*

1. Open the Debug Panel. 
	> 'View Menu > Debug'

1. On the debug panel, click the '**create and launch.json file**' link. You may get a popup asking for Environment select Java. This will generate a launch.json file and open it.

1. When this file is generated, you'll need to delete all but two json entries. You want to keep ECUExec and EcuLoggerExec. The end result should look similar to below.
	```
	{
	// Use IntelliSense to learn about possible attributes.
	// Hover to view descriptions of existing attributes.
	// For more information, visit: https://go.microsoft.com/fwlink/?linkid=830387
		"version": "0.2.0",
		"configurations": [
			{
				"type": "java",
				"name": "Debug (Launch)-ECUExec<romraider>",
				"request": "launch",
				"mainClass": "com.romraider.ECUExec",
				"projectName": "romraider"
			},
			{
				"type": "java",
				"name": "Debug (Launch)-EcuLoggerExec<romraider>",
				"request": "launch",
				"mainClass": "com.romraider.logger.ecu.EcuLoggerExec",
				"projectName": "romraider"
			}
		]
	}

1. At the top of the debug panel, you'll see a label '**Run And Debug**' and a drop down box. 
	> - From the dropdown box select the appropriate option depending on whether you want to debug RomRaider Editor or RomRaider Logger.
	> - Press the play button next to the dropdown or press F5.
	> - You will likely get a popup: 'Build failed, do you want to continue?' Click Proceed.
	> - RomRaider should launch.

1. If you're still here, Congrats! You are now able to debug RomRaider. You can set breakpoints in the source code, and step through the code.

1. If the steps above didn't work, please review them to make sure you didn't miss anything. If it still doesn't work contact me via my github account below.

Thanks,

Walter Stypula

https://github.com/walterstypula