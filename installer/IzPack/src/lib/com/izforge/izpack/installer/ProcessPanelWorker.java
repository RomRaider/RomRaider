/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Tino Schwarze
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

package com.izforge.izpack.installer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLBuilder;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.Pack;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * This class does alle the work for the process panel.
 * 
 * It responsible for
 * <ul>
 * <li>parsing the process spec XML file
 * <li>performing the actions described therein
 * </ul>
 * 
 * @author Tino Schwarze
 */
public class ProcessPanelWorker implements Runnable
{

    /** Name of resource for specifying processing parameters. */
    private static final String SPEC_RESOURCE_NAME = "ProcessPanel.Spec.xml";

    private VariableSubstitutor vs;

    protected AbstractUIProcessHandler handler;

    private ArrayList jobs = new ArrayList();

    private boolean result = true;
    
    private static PrintWriter logfile = null;

    private String logfiledir = null;

    protected AutomatedInstallData idata;

    /**
     * The constructor.
     * 
     * @param idata The installation data.
     * @param handler The handler to notify of progress.
     */
    public ProcessPanelWorker(AutomatedInstallData idata, AbstractUIProcessHandler handler)
            throws IOException
    {
        this.handler = handler;
        this.idata = idata;
        this.vs = new VariableSubstitutor(idata.getVariables());

        // Removed this test in order to move out of the CTOR (ExecuteForPack
        // Patch)
        // if (!readSpec())
        // throw new IOException("Error reading processing specification");
    }

    private boolean readSpec() throws IOException
    {
        InputStream input;
        try
        {
            input = ResourceManager.getInstance().getInputStream(SPEC_RESOURCE_NAME);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        StdXMLParser parser = new StdXMLParser();
        parser.setBuilder(new StdXMLBuilder());
        parser.setValidator(new NonValidator());

        XMLElement spec;
        try
        {
            parser.setReader(new StdXMLReader(input));

            spec = (XMLElement) parser.parse();
        }
        catch (Exception e)
        {
            System.err.println("Error parsing XML specification for processing.");
            System.err.println(e.toString());
            return false;
        }

        if (!spec.hasChildren()) return false;

        // Handle logfile
        XMLElement lfd = spec.getFirstChildNamed("logfiledir");
        if (lfd != null)
        {
            logfiledir = lfd.getContent();
        }

        for (Iterator job_it = spec.getChildrenNamed("job").iterator(); job_it.hasNext();)
        {
            XMLElement job_el = (XMLElement) job_it.next();

            // ExecuteForPack Patch
            // Check if processing required for pack
            Vector forPacks = job_el.getChildrenNamed("executeForPack");
            if (!jobRequiredFor(forPacks))
            {
                continue;
            }

            // first check OS constraints - skip jobs not suited for this OS
            List constraints = OsConstraint.getOsList(job_el);

            if (OsConstraint.oneMatchesCurrentSystem(constraints))
            {
                List ef_list = new ArrayList();

                String job_name = job_el.getAttribute("name", "");

                for (Iterator ef_it = job_el.getChildrenNamed("executefile").iterator(); ef_it
                        .hasNext();)
                {
                    XMLElement ef = (XMLElement) ef_it.next();

                    String ef_name = ef.getAttribute("name");

                    if ((ef_name == null) || (ef_name.length() == 0))
                    {
                        System.err.println("missing \"name\" attribute for <executefile>");
                        return false;
                    }

                    List args = new ArrayList();

                    for (Iterator arg_it = ef.getChildrenNamed("arg").iterator(); arg_it.hasNext();)
                    {
                        XMLElement arg_el = (XMLElement) arg_it.next();

                        String arg_val = arg_el.getContent();

                        args.add(arg_val);
                    }

                    ef_list.add(new ExecutableFile(ef_name, args));
                }

                for (Iterator ef_it = job_el.getChildrenNamed("executeclass").iterator(); ef_it
                        .hasNext();)
                {
                    XMLElement ef = (XMLElement) ef_it.next();
                    String ef_name = ef.getAttribute("name");
                    if ((ef_name == null) || (ef_name.length() == 0))
                    {
                        System.err.println("missing \"name\" attribute for <executeclass>");
                        return false;
                    }

                    List args = new ArrayList();
                    for (Iterator arg_it = ef.getChildrenNamed("arg").iterator(); arg_it.hasNext();)
                    {
                        XMLElement arg_el = (XMLElement) arg_it.next();
                        String arg_val = arg_el.getContent();
                        args.add(arg_val);
                    }

                    ef_list.add(new ExecutableClass(ef_name, args));
                }
                this.jobs.add(new ProcessingJob(job_name, ef_list));
            }

        }

        return true;
    }

    /**
     * This is called when the processing thread is activated.
     * 
     * Can also be called directly if asynchronous processing is not desired.
     */
    public void run()
    {
        // ExecuteForPack patch
        // Read spec only here... not before, cause packs are otherwise
        // all selected or de-selected
        try
        {
            if (!readSpec())
            {
                System.err.println("Error parsing XML specification for processing.");
                return;
            }
        }
        catch (java.io.IOException ioe)
        {
            System.err.println(ioe.toString());
            return;
        }

        // Create logfile if needed. Do it at this point because
        // variable substitution needs selected install path.
        if (logfiledir != null)
        {
            logfiledir = IoHelper.translatePath(logfiledir, new VariableSubstitutor(idata
                    .getVariables()));

            File lf;

            String appVersion = idata.getVariable("APP_VER");

            if (appVersion != null)
                appVersion = "V" + appVersion;
            else
                appVersion = "undef";

            String identifier = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());

            identifier = appVersion.replace(' ', '_') + "_" + identifier;

            try
            {
                lf = File.createTempFile("Install_" + identifier + "_", ".log",
                        new File(logfiledir));
                logfile = new PrintWriter(new FileOutputStream(lf), true);
            }
            catch (IOException e)
            {
                Debug.error(e);
                // TODO throw or throw not, that's the question...
            }
        }

        this.handler.startProcessing(this.jobs.size());

        for (Iterator job_it = this.jobs.iterator(); job_it.hasNext();)
        {
            ProcessingJob pj = (ProcessingJob) job_it.next();

            this.handler.startProcess(pj.name);

            this.result = pj.run(this.handler, this.vs);

            this.handler.finishProcess();

            if (!this.result) break;
        }

        this.handler.finishProcessing();
        if (logfile != null) logfile.close();
    }

    /** Start the compilation in a separate thread. */
    public void startThread()
    {
        Thread processingThread = new Thread(this, "processing thread");
        // will call this.run()
        processingThread.start();
    }

    /**
     * Return the result of the process execution.
     * 
     * @return true if all processes succeeded, false otherwise.
     */
    public boolean getResult()
    {
        return this.result;
    }
    
    interface Processable
    {

        /**
         * @param handler The UI handler for user interaction and to send output to.
         * @return true on success, false if processing should stop
         */
        public boolean run(AbstractUIProcessHandler handler, VariableSubstitutor vs);
    }

    private static class ProcessingJob implements Processable
    {

        public String name;

        private List processables;

        public ProcessingJob(String name, List processables)
        {
            this.name = name;
            this.processables = processables;
        }

        public boolean run(AbstractUIProcessHandler handler, VariableSubstitutor vs)
        {
            for (Iterator pr_it = this.processables.iterator(); pr_it.hasNext();)
            {
                Processable pr = (Processable) pr_it.next();

                if (!pr.run(handler, vs)) return false;
            }

            return true;
        }

    }

    private static class ExecutableFile implements Processable
    {

        private String filename;

        private List arguments;

        protected AbstractUIProcessHandler handler;

        public ExecutableFile(String fn, List args)
        {
            this.filename = fn;
            this.arguments = args;
        }

        public boolean run(AbstractUIProcessHandler handler, VariableSubstitutor vs)
        {
            this.handler = handler;

            String params[] = new String[this.arguments.size() + 1];

            params[0] = vs.substitute(this.filename, "plain");

            int i = 1;
            for (Iterator arg_it = this.arguments.iterator(); arg_it.hasNext();)
            {
                params[i++] = vs.substitute((String) arg_it.next(), "plain");
            }

            try
            {
                Process p = Runtime.getRuntime().exec(params);

                OutputMonitor stdoutMon = new OutputMonitor(this.handler, p.getInputStream(), false);
                OutputMonitor stderrMon = new OutputMonitor(this.handler, p.getErrorStream(), true);
                Thread stdoutThread = new Thread(stdoutMon);
                Thread stderrThread = new Thread(stderrMon);
                stdoutThread.setDaemon(true);
                stderrThread.setDaemon(true);
                stdoutThread.start();
                stderrThread.start();

                try
                {
                    int exitStatus = p.waitFor();

                    stopMonitor(stdoutMon, stdoutThread);
                    stopMonitor(stderrMon, stderrThread);

                    if (exitStatus != 0)
                    {
                        if (this.handler.askQuestion("process execution failed",
                                "Continue anyway?", AbstractUIHandler.CHOICES_YES_NO,
                                AbstractUIHandler.ANSWER_YES) == AbstractUIHandler.ANSWER_NO) { return false; }
                    }
                }
                catch (InterruptedException ie)
                {
                    p.destroy();
                    this.handler.emitError("process interrupted", ie.toString());
                    return false;
                }
            }
            catch (IOException ioe)
            {
                this.handler.emitError("I/O error", ioe.toString());
                return false;
            }

            return true;
        }

        private void stopMonitor(OutputMonitor m, Thread t)
        {
            // taken from com.izforge.izpack.util.FileExecutor
            m.doStop();
            long softTimeout = 500;
            try
            {
                t.join(softTimeout);
            }
            catch (InterruptedException e)
            {}

            if (!t.isAlive()) return;

            t.interrupt();
            long hardTimeout = 500;
            try
            {
                t.join(hardTimeout);
            }
            catch (InterruptedException e)
            {}
        }

        static public class OutputMonitor implements Runnable
        {

            private boolean stderr = false;

            private AbstractUIProcessHandler handler;

            private BufferedReader reader;

            private Boolean stop = Boolean.valueOf(false);

            public OutputMonitor(AbstractUIProcessHandler handler, InputStream is, boolean stderr)
            {
                this.stderr = stderr;
                this.reader = new BufferedReader(new InputStreamReader(is));
                this.handler = handler;
            }

            public void run()
            {
                try
                {
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        this.handler.logOutput(line, stderr);

                        // log output also to file given in ProcessPanelSpec

                        if (logfile != null) logfile.println(line);

                        synchronized (this.stop)
                        {
                            if (stop.booleanValue()) return;
                        }
                    }
                }
                catch (IOException ioe)
                {
                    this.handler.logOutput(ioe.toString(), true);

                    // log errors also to file given in ProcessPanelSpec

                    if (logfile != null) logfile.println(ioe.toString());

                }

            }

            public void doStop()
            {
                synchronized (this.stop)
                {
                    this.stop = Boolean.valueOf(true);
                }
            }

        }

    }

    /**
     * Tries to create a class that has an empty contstructor and a method
     * run(AbstractUIProcessHandler, String[]) If found, it calls the method and processes all
     * returned exceptions
     */
    private static class ExecutableClass implements Processable
    {

        final private String myClassName;

        final private List myArguments;

        protected AbstractUIProcessHandler myHandler;

        public ExecutableClass(String className, List args)
        {
            myClassName = className;
            myArguments = args;
        }

        public boolean run(AbstractUIProcessHandler aHandler, VariableSubstitutor varSubstitutor)
        {
            boolean result = false;
            myHandler = aHandler;

            String params[] = new String[myArguments.size()];

            int i = 0;
            for (Iterator arg_it = myArguments.iterator(); arg_it.hasNext();)
                params[i++] = varSubstitutor.substitute((String) arg_it.next(), "plain");

            try
            {
                ClassLoader loader = this.getClass().getClassLoader();
                Class procClass = loader.loadClass(myClassName);

                Object o = procClass.newInstance();
                Method m = procClass.getMethod("run", new Class[] { AbstractUIProcessHandler.class,
                        String[].class});

                m.invoke(o, new Object[] { myHandler, params});
                result = true;
            }
            catch (SecurityException e)
            {
                myHandler.emitError("Post Processing Error",
                        "Security exception thrown when processing class: " + myClassName);
            }
            catch (ClassNotFoundException e)
            {
                myHandler.emitError("Post Processing Error", "Cannot find processing class: "
                        + myClassName);
            }
            catch (NoSuchMethodException e)
            {
                myHandler.emitError("Post Processing Error",
                        "Processing class does not have 'run' method: " + myClassName);
            }
            catch (IllegalAccessException e)
            {
                myHandler.emitError("Post Processing Error", "Error accessing processing class: "
                        + myClassName);
            }
            catch (InvocationTargetException e)
            {
                myHandler.emitError("Post Processing Error", "Invocation Problem calling : "
                        + myClassName + ", " + e.getCause().getMessage());
            }
            catch (Exception e)
            {
                myHandler.emitError("Post Processing Error",
                        "Exception when running processing class: " + myClassName + ", "
                                + e.getMessage());
            }
            catch (Error e)
            {
                myHandler.emitError("Post Processing Error",
                        "Error when running processing class: " + myClassName + ", "
                                + e.getMessage());
            }
            catch (Throwable e)
            {
                myHandler.emitError("Post Processing Error",
                        "Error when running processing class: " + myClassName + ", "
                                + e.getMessage());
            }
            return result;
        }
    }

    /*------------------------ ExecuteForPack PATCH -------------------------*/
    /*
     * Verifies if the job is required for any of the packs listed. The job is required for a pack
     * in the list if that pack is actually selected for installation. <br><br> <b>Note:</b><br>
     * If the list of selected packs is empty then <code>true</code> is always returned. The same
     * is true if the <code>packs</code> list is empty.
     * 
     * @param packs a <code>Vector</code> of <code>String</code>s. Each of the strings denotes
     * a pack for which the schortcut should be created if the pack is actually installed.
     * 
     * @return <code>true</code> if the shortcut is required for at least on pack in the list,
     * otherwise returns <code>false</code>.
     */
    /*--------------------------------------------------------------------------*/
    /*
     * @design
     * 
     * The information about the installed packs comes from InstallData.selectedPacks. This assumes
     * that this panel is presented to the user AFTER the PacksPanel.
     * 
     * /*--------------------------------------------------------------------------
     */

    private boolean jobRequiredFor(Vector packs)
    {
        String selected;
        String required;

        if (packs.size() == 0) { return (true); }

        // System.out.println ("Number of selected packs is "
        // +idata.selectedPacks.size () );

        for (int i = 0; i < idata.selectedPacks.size(); i++)
        {
            selected = ((Pack) idata.selectedPacks.get(i)).name;

            // System.out.println ("Selected pack is " + selected);

            for (int k = 0; k < packs.size(); k++)
            {
                required = (String) ((XMLElement) packs.elementAt(k)).getAttribute("name", "");
                // System.out.println ("Attribute name is " + required);
                if (selected.equals(required))
                {
                    // System.out.println ("Return true");
                    return (true);
                }
            }
        }
        return (false);
    }

}
