/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Chadwick McHenry
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.uninstaller;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.izforge.izpack.util.OsVersion;

/**
 * Allows an application to modify the jar file from which it came, including outright deletion. The
 * jar file of an app is usually locked when java is run so this is normally not possible.
 * <p>
 * 
 * Create a SelfModifier with a target method, then invoke the SelfModifier with arguments to be
 * passed to the target method. The jar file containing the target method's class (obtained by
 * reflection) will be extracted to a temporary directory, and a new java process will be spawned to
 * invoke the target method. The original jar file may now be modified.
 * <p>
 * 
 * If the constructor or invoke() methods fail, it is generally because secondary java processes
 * could not be started.
 * 
 * <b>Requirements</b>
 * <ul>
 * <li>The target method, and all it's required classes must be in a jar file.
 * <li>The Self Modifier, and its inner classes must also be in the jar file.
 * </ul>
 * 
 * There are three system processes (or "phases") involved, the first invoked by the user, the
 * second and third by the SelfModifier.
 * <p>
 * 
 * <b>Phase 1:</b>
 * <ol>
 * <li>Program is launched, SelfModifier is created, invoke(String[]) is called
 * <li>A temporary directory (or "sandbox") is created in the default temp directory, and the jar
 * file contents ar extracted into it
 * <li>Phase 2 is spawned using the sandbox as it's classpath, SelfModifier as the main class, the
 * arguments to "invoke(String[])" as the main arguments, and the <a
 * href="#selfmodsysprops">SelfModifier system properties</a> set.
 * <li>Immidiately exit so the system unlocks the jar file
 * </ol>
 * 
 * <b>Phase 2:</b>
 * <ol>
 * <li>Initializes from system properties.
 * <li>Spawn phase 3 exactly as phase 2 except the self.modifier.phase system properties set to 3.
 * <li>Wait for phase 3 to die
 * <li>Delete the temporary sandbox
 * </ol>
 * 
 * <b>Phase 3:</b>
 * <ol>
 * <li>Initializes from system properties.
 * <li>Redirect std err stream to the log
 * <li>Invoke the target method with arguments we were given
 * <li>The target method is expected to call exit(), or to not start any looping threads (e.g. AWT
 * thread). In other words, the target is the new "main" method.
 * </ol>
 * 
 * <a name="selfmodsysprops"><b>SelfModifier system properties</b></a> used to pass information
 * between processes. <table border="1">
 * <tr>
 * <th>Constant
 * <th>System property
 * <th>description</tr>
 * <tr>
 * <td><a href="#BASE_KEY">BASE_KEY</a>
 * <td>self.mod.jar
 * <td>base path to log file and sandbox dir</tr>
 * <tr>
 * <td><a href="#JAR_KEY">JAR_KEY</a>
 * <td>self.mod.class
 * <td>path to original jar file</tr>
 * <tr>
 * <td><a href="#CLASS_KEY">CLASS_KEY</a>
 * <td>self.mod.method
 * <td>class of target method</tr>
 * <tr>
 * <td><a href="#METHOD_KEY">METHOD_KEY</a>
 * <td>self.mod.phase
 * <td>name of method to be invoked in sandbox</tr>
 * <tr>
 * <td><a href="#PHASE_KEY">PHASE_KEY</a>
 * <td>self.mod.base
 * <td>phase of operation to run</tr>
 * </table>
 * 
 * @author Chadwick McHenry
 * @version 1.0
 */
public class SelfModifier
{

    /** System property name of base for log and sandbox of secondary processes. */
    public static final String BASE_KEY = "self.mod.base";

    /** System property name of original jar file containing application. */
    public static final String JAR_KEY = "self.mod.jar";

    /** System property name of class declaring target method. */
    public static final String CLASS_KEY = "self.mod.class";

    /** System property name of target method to invoke in secondary process. */
    public static final String METHOD_KEY = "self.mod.method";

    /** System property name of phase (1, 2, or 3) indicator. */
    public static final String PHASE_KEY = "self.mod.phase";

    /** Base prefix name for sandbox and log, used only in phase 1. */
    private String prefix = "izpack";

    /** Target method to be invoked in sandbox. */
    private Method method = null;

    /** Log for phase 2 and 3, because we can't capture the stdio from them. */
    private File logFile = null;

    /** Directory which we extract too, invoke from, and finally delete. */
    private File sandbox = null;

    /** Original jar file program was launched from. */
    private File jarFile = null;

    /** Current phase of execution: 1, 2, or 3. */
    private int phase = 0;

    /** For logging time. */
    private SimpleDateFormat isoPoint = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private Date date = new Date();

    public static void test(String[] args)
    {
        // open a File for random access in the sandbox, which will cause
        // deletion
        // of the file and its parent directories to fail until it is closed (by
        // virtue of this java process halting)
        try
        {
            File sandbox = new File(System.getProperty(BASE_KEY) + ".d");
            File randFile = new File(sandbox, "RandomAccess.tmp");
            RandomAccessFile rand = new RandomAccessFile(randFile, "rw");
            rand.writeChars("Just a test: The jvm has to close 'cuz I won't!\n");

            System.err.print("Deleting sandbox: ");
            deleteTree(sandbox);
            System.err.println(sandbox.exists() ? "FAILED" : "SUCCEEDED");
        }
        catch (Exception x)
        {
            System.err.println(x.getMessage());
            x.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        // phase 1 already set up the sandbox and spawned phase 2.
        // phase 2 creates the log, spawns phase 3 and waits
        // phase 3 invokes method and returns. method must kill all it's threads

        try
        {
            // all it's attributes are retrieved from system properties
            SelfModifier selfModifier = new SelfModifier();

            // phase 2: invoke a process for phase 3, wait, and clean up
            if (selfModifier.phase == 2)
                selfModifier.invoke2(args);

            // phase 3: invoke method and die
            else if (selfModifier.phase == 3) selfModifier.invoke3(args);
        }
        catch (IOException ioe)
        {
            System.err.println("Error invoking a secondary phase");
            System.err.println("Note that this program is only intended as a secondary process");
            ioe.printStackTrace();
        }
    }

    /**
     * Internal constructor where target class and method are obtained from system properties.
     * 
     * @throws IOException for errors getting to the sandbox.
     * @throws SecurityException if access to the target method is denied
     */
    private SelfModifier() throws IOException
    {
        phase = Integer.parseInt(System.getProperty(PHASE_KEY));

        String cName = System.getProperty(CLASS_KEY);
        String tName = System.getProperty(METHOD_KEY);

        jarFile = new File(System.getProperty(JAR_KEY));
        logFile = new File(System.getProperty(BASE_KEY) + ".log");
        sandbox = new File(System.getProperty(BASE_KEY) + ".d");

        // retrieve refrence to target method
        try
        {
            Class clazz = Class.forName(cName);
            Method method = clazz.getMethod(tName, new Class[] { String[].class});

            initMethod(method);
        }
        catch (ClassNotFoundException x1)
        {
            log("No class found for " + cName);
        }
        catch (NoSuchMethodException x2)
        {
            log("No method " + tName + " found in " + cName);
        }
    }

    /**
     * Creates a SelfModifier which will invoke the target method in a separate process from which
     * it may modify it's own jar file.
     * 
     * The target method must be public, static, and take a single array of strings as its only
     * parameter. The class which declares the method must also be public. Reflection is used to
     * ensure this.
     * 
     * @param method a public, static method that accepts a String array as it's only parameter. Any
     * return value is ignored.
     * 
     * @throws NullPointerException if <code>method</code> is null
     * @throws IllegalArgumentException if <code>method</code> is not public, static, and take a
     * String array as it's only argument, or of it's declaring class is not public.
     * @throws IllegalStateException if process was not invoked from a jar file, or an IOExceptioin
     * occured while accessing it
     * @throws IOException if java is unable to be executed as a separte process
     * @throws SecurityException if access to the method, or creation of a subprocess is denied
     */
    public SelfModifier(Method method) throws IOException
    {
        phase = 1;
        initJavaExec();
        initMethod(method);
    }

    /**
     * Check the method for the required properties (public, static, params:(String[])).
     * 
     * @throws NullPointerException if <code>method</code> is null
     * @throws IllegalArgumentException if <code>method</code> is not public, static, and take a
     * String array as it's only argument, or of it's declaring class is not public.
     * @throws SecurityException if access to the method is denied
     */
    private void initMethod(Method method)
    {
        int mod = method.getModifiers();
        if ((mod & Modifier.PUBLIC) == 0 || (mod & Modifier.STATIC) == 0)
            throw new IllegalArgumentException("Method not public and static");

        Class[] params = method.getParameterTypes();
        if (params.length != 1 || !params[0].isArray()
                || !"java.lang.String".equals(params[0].getComponentType().getName()))
            throw new IllegalArgumentException("Method must accept String array");

        Class clazz = method.getDeclaringClass();
        mod = clazz.getModifiers();
        if ((mod & Modifier.PUBLIC) == 0 || (mod & Modifier.INTERFACE) != 0)
            throw new IllegalArgumentException("Method must be in a public class");

        this.method = method;
    }

    /**
     * This call ensures that java can be exec'd in a separate process.
     * 
     * @throws IOException if an I/O error occurs, indicating java is unable to be exec'd
     * @throws SecurityException if a security manager exists and doesn't allow creation of a
     * subprocess
     */
    private void initJavaExec() throws IOException
    {
        try
        {
            Process p = Runtime.getRuntime().exec(javaCommand());

            new StreamProxy(p.getErrorStream(), "err").start();
            new StreamProxy(p.getInputStream(), "out").start();
            p.getOutputStream().close();

            // even if it returns an error code, it was at least found
            p.waitFor();
        }
        catch (InterruptedException ie)
        {
            throw new IOException("Unable to create a java subprocess");
        }
    }

    /***********************************************************************************************
     * --------------------------------------------------------------------- Phase 1 (call from
     * external spawn phase 2) ---------------------------------------------------------------------
     */

    /**
     * Invoke the target method in a separate process from which it may modify it's own jar file.
     * This method does not normally return. After spawning the secondary process, the current
     * process must die before the jar file is unlocked, therefore calling this method is akin to
     * calling {@link System#exit(int)}.
     * <p>
     * 
     * The contents of the current jar file are extracted copied to a 'sandbox' directory from which
     * the method is invoked. The path to the original jar file is placed in the system property
     * {@link #JAR_KEY}.
     * <p>
     * 
     * @param args arguments to pass to the target method. May be empty or null to indicate no
     * arguments.
     * 
     * @throws IOException for lots of things
     * @throws IllegalStateException if method's class was not loaded from a jar
     */
    public void invoke(String[] args) throws IOException
    {
        // Initialize sandbox and log file to be unique, but similarly named
        while (true)
        {
            logFile = File.createTempFile(prefix, ".log");
            String f = logFile.toString();
            sandbox = new File(f.substring(0, f.length() - 4) + ".d");

            // check if the similarly named directory is free
            if (!sandbox.exists()) break;

            logFile.delete();
        }
        if (!sandbox.mkdir()) throw new RuntimeException("Failed to create temp dir: " + sandbox);

        sandbox = sandbox.getCanonicalFile();
        logFile = logFile.getCanonicalFile();

        jarFile = findJarFile(method.getDeclaringClass()).getCanonicalFile();
        if (jarFile == null) throw new IllegalStateException("SelfModifier must be in a jar file");
        log("JarFile: " + jarFile);

        extractJarFile();

        if (args == null) args = new String[0];
        spawn(args, 2);

        // finally, if all went well, the invoking process must exit
        log("Exit");
        System.exit(0);
    }

    /**
     * Run a new jvm with all the system parameters needed for phases 2 and 3.
     * 
     * @throws IOException if there is an error getting the cononical name of a path
     */
    private Process spawn(String[] args, int nextPhase) throws IOException
    {
        String base = logFile.getAbsolutePath();
        base = base.substring(0, base.length() - 4);

        // invoke from tmpdir, passing target method arguments as args, and
        // SelfModifier parameters as sustem properties
        String[] javaCmd = new String[] { javaCommand(), "-classpath", sandbox.getAbsolutePath(),
                "-D" + BASE_KEY + "=" + base, "-D" + JAR_KEY + "=" + jarFile.getPath(),
                "-D" + CLASS_KEY + "=" + method.getDeclaringClass().getName(),
                "-D" + METHOD_KEY + "=" + method.getName(), "-D" + PHASE_KEY + "=" + nextPhase,
                getClass().getName()};

        String[] entireCmd = new String[javaCmd.length + args.length];
        System.arraycopy(javaCmd, 0, entireCmd, 0, javaCmd.length);
        System.arraycopy(args, 0, entireCmd, javaCmd.length, args.length);

        StringBuffer sb = new StringBuffer("Spawning phase ");
        sb.append(nextPhase).append(": ");
        for (int i = 0; i < entireCmd.length; i++)
            sb.append("\n\t").append(entireCmd[i]);
        log(sb.toString());

        // Just invoke it and let it go, the exception will be caught above
        // Won't compile on < jdk1.3, but will run on jre1.2
        if (JAVA_SPECIFICATION_VERSION < 1.3)
            return Runtime.getRuntime().exec(entireCmd, null);
        else
            return Runtime.getRuntime().exec(entireCmd, null, null); // workDir);
    }

    /**
     * Retrieve the jar file the specified class was loaded from.
     * 
     * @return null if file was not loaded from a jar file
     * @throws SecurityException if access to is denied by SecurityManager
     */
    public static File findJarFile(Class clazz)
    {
        String resource = clazz.getName().replace('.', '/') + ".class";

        URL url = ClassLoader.getSystemResource(resource);
        if (!"jar".equals(url.getProtocol())) return null;

        String path = url.getFile();
        // starts at "file:..." (use getPath() as of 1.3)
        path = path.substring(0, path.lastIndexOf('!'));

        File file;

        // getSystemResource() returns a valid URL (eg. spaces are %20), but a
        // file
        // Constructed w/ it will expect "%20" in path. URI and File(URI)
        // properly
        // deal with escaping back and forth, but didn't exist until 1.4
        if (JAVA_SPECIFICATION_VERSION < 1.4)
            file = new File(fromURI(path));
        else
            file = new File(URI.create(path));

        return file;
    }

    /**
     * @throws IOException
     */
    private void extractJarFile() throws IOException
    {
        byte[] buf = new byte[5120];
        int extracted = 0;
        InputStream in = null;
        OutputStream out = null;
        String MANIFEST = "META-INF/MANIFEST.MF";

        JarFile jar = new JarFile(jarFile, true);

        try
        {
            Enumeration entries = jar.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.isDirectory()) continue;

                String pathname = entry.getName();
                if (MANIFEST.equals(pathname.toUpperCase())) continue;

                in = jar.getInputStream(entry);

                File outFile = new File(sandbox, pathname);
                File parent = outFile.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();

                out = new BufferedOutputStream(new FileOutputStream(outFile));

                int n;
                while ((n = in.read(buf, 0, buf.length)) > 0)
                    out.write(buf, 0, n);

                out.close();
                extracted++;
            }
            jar.close();

            log("Extracted " + extracted + " file" + (extracted > 1 ? "s" : "") + " into "
                    + sandbox.getPath());
        }
        finally
        {
            try
            {
                jar.close();
            }
            catch (IOException ioe)
            {}
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException ioe)
                {}
            }
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException ioe)
                {}
            }
        }
    }

    /***********************************************************************************************
     * --------------------------------------------------------------------- Phase 2 (spawn the
     * phase 3 and clean up) ---------------------------------------------------------------------
     */

    /**
     * Invoke phase 2, which starts phase 3, then cleans up the sandbox. This is needed because
     * GUI's often call the exit() method to kill the AWT thread, and early versions of java did not
     * have exit hooks. In order to delete the sandbox on exit we invoke method in separate process
     * and wait for that process to complete. Even worse, resources in the jar may be locked by the
     * target process, which would prevent the sandbox from being deleted as well.
     */
    private void invoke2(String[] args)
    {

        int retVal = -1;
        try
        {
            // TODO: in jre 1.2, Phs1 consistently needs more time to unlock the
            // original jar. Phs2 should wait to invoke Phs3 until it knows its
            // parent (Phs1) has died, but Process.waitFor() only works on
            // children. Can we see when a parent dies, or /this/ Process
            // becomes
            // orphaned?
            try
            {
                Thread.sleep(1000);
            }
            catch (Exception x)
            {}

            // spawn phase 3, capture its stdio and wait for it to exit
            Process p = spawn(args, 3);

            new StreamProxy(p.getErrorStream(), "err", log).start();
            new StreamProxy(p.getInputStream(), "out", log).start();
            p.getOutputStream().close();

            try
            {
                retVal = p.waitFor();
            }
            catch (InterruptedException e)
            {
                log(e);
            }

            // clean up and go
            log("deleteing sandbox");
            deleteTree(sandbox);
        }
        catch (Exception e)
        {
            log(e);
        }
        log("Phase 3 return value = " + retVal);
    }

    /** Recursively delete a file structure. */
    public static boolean deleteTree(File file)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++)
                deleteTree(files[i]);
        }
        return file.delete();
    }

    /***********************************************************************************************
     * --------------------------------------------------------------------- Phase 3 (invoke method,
     * let it go as long as it likes)
     * ---------------------------------------------------------------------
     */

    /**
     * Invoke the target method and let it run free!
     */
    private void invoke3(String[] args)
    {
        // std io is being redirected to the log
        try
        {
            errlog("Invoking method: " + method.getDeclaringClass().getName() + "."
                    + method.getName() + "(String[] args)");

            method.invoke(null, new Object[] { args});
        }
        catch (Throwable t)
        {
            errlog(t.getMessage());
            t.printStackTrace();
            errlog("exiting");
            System.err.flush();
            System.exit(31);
        }

        errlog("Method returned, waiting for other threads");
        System.err.flush();
        // now let the method call exit...
    }

    /***********************************************************************************************
     * --------------------------------------------------------------------- Logging
     * ---------------------------------------------------------------------
     */

    PrintStream log = null;

    private void errlog(String msg)
    {
        date.setTime(System.currentTimeMillis());
        System.err.println(isoPoint.format(date) + " Phase " + phase + ": " + msg);
    }

    private PrintStream checkLog()
    {
        try
        {
            if (log == null) log = new PrintStream(new FileOutputStream(logFile.toString(), true));
        }
        catch (IOException x)
        {
            System.err.println("Phase " + phase + " log err: " + x.getMessage());
            x.printStackTrace();
        }
        date.setTime(System.currentTimeMillis());
        return log;
    }

    private void log(Throwable t)
    {
        if (checkLog() != null)
        {
            log.println(isoPoint.format(date) + " Phase " + phase + ": " + t.getMessage());
            t.printStackTrace(log);
        }
    }

    private void log(String msg)
    {
        if (checkLog() != null)
            log.println(isoPoint.format(date) + " Phase " + phase + ": " + msg);
    }

    public static class StreamProxy extends Thread
    {

        InputStream in;

        String name;

        OutputStream out;

        public StreamProxy(InputStream in, String name)
        {
            this(in, name, null);
        }

        public StreamProxy(InputStream in, String name, OutputStream out)
        {
            this.in = in;
            this.name = name;
            this.out = out;
        }

        public void run()
        {
            try
            {
                PrintWriter pw = null;
                if (out != null) pw = new PrintWriter(out);

                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = br.readLine()) != null)
                {
                    if (pw != null) pw.println(line);
                    // System.out.println(name + ">" + line);
                }
                if (pw != null) pw.flush();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }

    /***********************************************************************************************
     * --------------------------------------------------------------------- Apache ant code
     * ---------------------------------------------------------------------
     */
    // This was stolen (and specialized from much more modular code) from the
    // jakarta ant class org.apache.tools.ant.taskdefs.condition.Os
    // See the javaCommand() method.
    private static final float JAVA_SPECIFICATION_VERSION = Float.parseFloat(System
            .getProperty("java.specification.version"));

    private static final String JAVA_HOME = System.getProperty("java.home");

    /**
     * Constructs a file path from a <code>file:</code> URI.
     * 
     * <p>
     * Will be an absolute path if the given URI is absolute.
     * </p>
     * 
     * <p>
     * Swallows '%' that are not followed by two characters, doesn't deal with non-ASCII characters.
     * </p>
     * 
     * @param uri the URI designating a file in the local filesystem.
     * @return the local file system path for the file.
     */
    public static String fromURI(String uri)
    {
        if (!uri.startsWith("file:"))
            throw new IllegalArgumentException("Can only handle file: URIs");

        if (uri.startsWith("file://"))
            uri = uri.substring(7);
        else
            uri = uri.substring(5);

        uri = uri.replace('/', File.separatorChar);
        if (File.pathSeparatorChar == ';' && uri.startsWith("\\") && uri.length() > 2
                && Character.isLetter(uri.charAt(1)) && uri.lastIndexOf(':') > -1)
        {
            uri = uri.substring(1);
        }

        StringBuffer sb = new StringBuffer();
        CharacterIterator iter = new StringCharacterIterator(uri);
        for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next())
        {
            if (c == '%')
            {
                char c1 = iter.next();
                if (c1 != CharacterIterator.DONE)
                {
                    int i1 = Character.digit(c1, 16);
                    char c2 = iter.next();
                    if (c2 != CharacterIterator.DONE)
                    {
                        int i2 = Character.digit(c2, 16);
                        sb.append((char) ((i1 << 4) + i2));
                    }
                }
            }
            else
            {
                sb.append(c);
            }
        }

        String path = sb.toString();
        return path;
    }

    private static String addExtension(String command)
    {
        // This is the most common extension case - exe for windows and OS/2,
        // nothing for *nix.
        return command + (OsVersion.IS_WINDOWS || OsVersion.IS_OS2 ? ".exe" : "");
    }

    private static String javaCommand()
    {
        // This was stolen (and specialized from much more modular code) from
        // the
        // jakarta ant classes Os & JavaEnvUtils. Also see the following
        // org.apache.tools.ant.taskdefs.Java
        // org.apache.tools.ant.taskdefs.Execute
        // org.apache.tools.ant.taskdefs.condition.Os
        // org.apache.tools.ant.util.CommandlineJava
        // org.apache.tools.ant.util.JavaEnvUtils
        // org.apache.tools.ant.util.FileUtils
        // TODO: I didn't copy nearly all of their conditions
        String executable = addExtension("java");
        String dir = new File(JAVA_HOME + "/bin").getAbsolutePath();
        File jExecutable = new File(dir, executable);

        // Unfortunately on Windows java.home doesn't always refer
        // to the correct location, so we need to fall back to
        // assuming java is somewhere on the PATH.
        if (!jExecutable.exists()) return executable;
        return jExecutable.getAbsolutePath();
    }
}
