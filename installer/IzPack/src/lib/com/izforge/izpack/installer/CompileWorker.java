/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2003 Tino Schwarze
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLBuilder;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * This class does alle the work for compiling sources.
 * 
 * It responsible for
 * <ul>
 * <li>parsing the compilation spec XML file
 * <li>collecting and creating all jobs
 * <li>doing the actual compilation
 * </ul>
 * 
 * @author Tino Schwarze
 */
public class CompileWorker implements Runnable
{

    /** Compilation jobs */
    private ArrayList jobs;

    /** Name of resource for specifying compilation parameters. */
    private static final String SPEC_RESOURCE_NAME = "CompilePanel.Spec.xml";

    private VariableSubstitutor vs;

    private XMLElement spec;

    private AutomatedInstallData idata;

    private CompileHandler handler;

    private XMLElement compilerSpec;

    private ArrayList compilerList;

    private String compilerToUse;

    private XMLElement compilerArgumentsSpec;

    private ArrayList compilerArgumentsList;

    private String compilerArgumentsToUse;

    private CompileResult result = null;

    /**
     * The constructor.
     * 
     * @param idata The installation data.
     * @param handler The handler to notify of progress.
     */
    public CompileWorker(AutomatedInstallData idata, CompileHandler handler) throws IOException
    {
        this.idata = idata;
        this.handler = handler;
        this.vs = new VariableSubstitutor(idata.getVariables());

        Thread compilationThread = null;

        if (!readSpec()) throw new IOException("Error reading compilation specification");
    }

    /**
     * Return list of compilers to choose from.
     * 
     * @return ArrayList of String
     */
    public ArrayList getAvailableCompilers()
    {
        readChoices(this.compilerSpec, this.compilerList);
        return this.compilerList;
    }

    /**
     * Set the compiler to use.
     * 
     * The compiler is checked before compilation starts.
     * 
     * @param compiler compiler to use (not checked)
     */
    public void setCompiler(String compiler)
    {
        this.compilerToUse = compiler;
    }

    /** Get the compiler used. */
    public String getCompiler()
    {
        return this.compilerToUse;
    }

    /**
     * Return list of compiler arguments to choose from.
     * 
     * @return ArrayList of String
     */
    public ArrayList getAvailableArguments()
    {
        readChoices(this.compilerArgumentsSpec, this.compilerArgumentsList);
        return this.compilerArgumentsList;
    }

    /** Set the compiler arguments to use. */
    public void setCompilerArguments(String arguments)
    {
        this.compilerArgumentsToUse = arguments;
    }

    /** Get the compiler arguments used. */
    public String getCompilerArguments()
    {
        return this.compilerArgumentsToUse;
    }

    /** Get the result of the compilation. */
    public CompileResult getResult()
    {
        return this.result;
    }

    /** Start the compilation in a separate thread. */
    public void startThread()
    {
        Thread compilationThread = new Thread(this, "compilation thread");
        // will call this.run()
        compilationThread.start();
    }

    /**
     * This is called when the compilation thread is activated.
     * 
     * Can also be called directly if asynchronous processing is not desired.
     */
    public void run()
    {
        try
        {
            if (!collectJobs())
            {
                String[] dummy_command = { "no command"};

                this.result = new CompileResult(this.idata.langpack
                        .getString("CompilePanel.worker.nofiles"), dummy_command, "", "");
            }
            else
            {
                this.result = compileJobs();
            }
        }
        catch (Exception e)
        {
            this.result = new CompileResult();
            this.result.setStatus(CompileResult.FAILED);
            this.result.setAction(CompileResult.ACTION_ABORT);
        }

        this.handler.stopAction();
    }

    private boolean readSpec()
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

        try
        {
            parser.setReader(new StdXMLReader(input));

            this.spec = (XMLElement) parser.parse();
        }
        catch (Exception e)
        {
            System.out.println("Error parsing XML specification for compilation.");
            e.printStackTrace();
            return false;
        }

        if (!this.spec.hasChildren()) return false;

        this.compilerArgumentsList = new ArrayList();
        this.compilerList = new ArrayList();

        // read <global> information
        XMLElement global = this.spec.getFirstChildNamed("global");

        // use some default values if no <global> section found
        if (global != null)
        {

            // get list of compilers
            this.compilerSpec = global.getFirstChildNamed("compiler");

            if (this.compilerSpec != null)
            {
                readChoices(this.compilerSpec, this.compilerList);
            }

            this.compilerArgumentsSpec = global.getFirstChildNamed("arguments");

            if (this.compilerArgumentsSpec != null)
            {
                // basicly perform sanity check
                readChoices(this.compilerArgumentsSpec, this.compilerArgumentsList);
            }

        }

        // supply default values if no useful ones where found
        if (this.compilerList.size() == 0)
        {
            this.compilerList.add("javac");
            this.compilerList.add("jikes");
        }

        if (this.compilerArgumentsList.size() == 0)
        {
            this.compilerArgumentsList.add("-O -g:none");
            this.compilerArgumentsList.add("-O");
            this.compilerArgumentsList.add("-g");
            this.compilerArgumentsList.add("");
        }

        return true;
    }

    // helper function
    private void readChoices(XMLElement element, ArrayList result)
    {
        Vector choices = element.getChildrenNamed("choice");

        if (choices == null) return;

        result.clear();

        Iterator choice_it = choices.iterator();

        while (choice_it.hasNext())
        {
            XMLElement choice = (XMLElement) choice_it.next();

            String value = choice.getAttribute("value");

            if (value != null)
            {
                List osconstraints = OsConstraint.getOsList(choice);

                if (OsConstraint.oneMatchesCurrentSystem(osconstraints))
                {
                    result.add(this.vs.substitute(value, "plain"));
                }
            }

        }

    }

    /**
     * Parse the compilation specification file and create jobs.
     */
    private boolean collectJobs() throws Exception
    {
        XMLElement data = this.spec.getFirstChildNamed("jobs");

        if (data == null) return false;

        // list of classpath entries
        ArrayList classpath = new ArrayList();

        this.jobs = new ArrayList();

        // we throw away the toplevel compilation job
        // (all jobs are collected in this.jobs)
        collectJobsRecursive(data, classpath);

        return true;
    }

    /** perform the actual compilation */
    private CompileResult compileJobs()
    {
        ArrayList args = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(this.compilerArgumentsToUse);

        while (tokenizer.hasMoreTokens())
        {
            args.add(tokenizer.nextToken());
        }

        Iterator job_it = this.jobs.iterator();

        this.handler.startAction("Compilation", this.jobs.size());

        // check whether compiler is valid (but only if there are jobs)
        if (job_it.hasNext())
        {
            CompilationJob first_job = (CompilationJob) this.jobs.get(0);

            CompileResult check_result = first_job.checkCompiler(this.compilerToUse, args);
            if (!check_result.isContinue()) { return check_result; }

        }

        int job_no = 0;

        while (job_it.hasNext())
        {
            CompilationJob job = (CompilationJob) job_it.next();

            this.handler.nextStep(job.getName(), job.getSize(), job_no++);

            CompileResult result = job.perform(this.compilerToUse, args);

            if (!result.isContinue()) return result;
        }

        Debug.trace("compilation finished.");
        return new CompileResult();
    }

    private CompilationJob collectJobsRecursive(XMLElement node, ArrayList classpath)
            throws Exception
    {
        Enumeration toplevel_tags = node.enumerateChildren();
        ArrayList ourclasspath = (ArrayList) classpath.clone();
        ArrayList files = new ArrayList();

        while (toplevel_tags.hasMoreElements())
        {
            XMLElement child = (XMLElement) toplevel_tags.nextElement();

            if ("classpath".equals(child.getName()))
            {
                changeClassPath(ourclasspath, child);
            }
            else if ("job".equals(child.getName()))
            {
                CompilationJob subjob = collectJobsRecursive(child, ourclasspath);
                if (subjob != null) this.jobs.add(subjob);
            }
            else if ("directory".equals(child.getName()))
            {
                String name = child.getAttribute("name");

                if (name != null)
                {
                    // substitute variables
                    String finalname = this.vs.substitute(name, "plain");

                    files.addAll(scanDirectory(new File(finalname)));
                }

            }
            else if ("file".equals(child.getName()))
            {
                String name = child.getAttribute("name");

                if (name != null)
                {
                    // substitute variables
                    String finalname = this.vs.substitute(name, "plain");

                    files.add(new File(finalname));
                }

            }
            else if ("packdepency".equals(child.getName()))
            {
                String name = child.getAttribute("name");

                if (name == null)
                {
                    System.out
                            .println("invalid compilation spec: <packdepency> without name attribute");
                    return null;
                }

                // check whether the wanted pack was selected for installation
                Iterator pack_it = this.idata.selectedPacks.iterator();
                boolean found = false;

                while (pack_it.hasNext())
                {
                    com.izforge.izpack.Pack pack = (com.izforge.izpack.Pack) pack_it.next();

                    if (pack.name.equals(name))
                    {
                        found = true;
                        break;
                    }
                }

                if (!found)
                {
                    Debug.trace("skipping job because pack " + name + " was not selected.");
                    return null;
                }

            }

        }

        if (files.size() > 0)
            return new CompilationJob(this.handler, this.idata.langpack, (String) node
                    .getAttribute("name"), files, ourclasspath);

        return null;
    }

    /** helper: process a <code>&lt;classpath&gt;</code> tag. */
    private void changeClassPath(ArrayList classpath, XMLElement child) throws Exception
    {
        String add = child.getAttribute("add");
        if (add != null)
        {
            add = this.vs.substitute(add, "plain");
            if (!new File(add).exists())
            {
                if (!this.handler.emitWarning("Invalid classpath", "The path " + add
                        + " could not be found.\nCompilation may fail."))
                    throw new Exception("Classpath " + add + " does not exist.");
            }
            else
            {
                classpath.add(this.vs.substitute(add, "plain"));
            }

        }

        String sub = child.getAttribute("sub");
        if (sub != null)
        {
            int cpidx = -1;
            sub = this.vs.substitute(sub, "plain");

            do
            {
                cpidx = classpath.indexOf(sub);
                classpath.remove(cpidx);
            }
            while (cpidx >= 0);

        }

    }

    /**
     * helper: recursively scan given directory.
     * 
     * @return list of files found (might be empty)
     */
    private ArrayList scanDirectory(File path)
    {
        Debug.trace("scanning directory " + path.getAbsolutePath());

        ArrayList result = new ArrayList();

        if (!path.isDirectory()) return result;

        File[] entries = path.listFiles();

        for (int i = 0; i < entries.length; i++)
        {
            File f = entries[i];

            if (f == null) continue;

            if (f.isDirectory())
            {
                result.addAll(scanDirectory(f));
            }
            else if ((f.isFile()) && (f.getName().toLowerCase().endsWith(".java")))
            {
                result.add(f);
            }

        }

        return result;
    }

    /** a compilation job */
    private static class CompilationJob
    {

        private CompileHandler listener;

        private String name;

        private ArrayList files;

        private ArrayList classpath;

        private LocaleDatabase langpack;

        // XXX: figure that out (on runtime?)
        private static final int MAX_CMDLINE_SIZE = 4096;

        public CompilationJob(CompileHandler listener, LocaleDatabase langpack, ArrayList files,
                ArrayList classpath)
        {
            this.listener = listener;
            this.langpack = langpack;
            this.name = null;
            this.files = files;
            this.classpath = classpath;
        }

        public CompilationJob(CompileHandler listener, LocaleDatabase langpack, String name,
                ArrayList files, ArrayList classpath)
        {
            this.listener = listener;
            this.langpack = langpack;
            this.name = name;
            this.files = files;
            this.classpath = classpath;
        }

        public String getName()
        {
            if (this.name != null) return this.name;

            return "";
        }

        public int getSize()
        {
            return this.files.size();
        }

        public CompileResult perform(String compiler, ArrayList arguments)
        {
            Debug.trace("starting job " + this.name);
            // we have some maximum command line length - need to count
            int cmdline_len = 0;

            // used to collect the arguments for executing the compiler
            LinkedList args = new LinkedList(arguments);

            {
                Iterator arg_it = args.iterator();
                while (arg_it.hasNext())
                    cmdline_len += ((String) arg_it.next()).length() + 1;
            }

            // add compiler in front of arguments
            args.add(0, compiler);
            cmdline_len += compiler.length() + 1;

            // construct classpath argument for compiler
            // - collect all classpaths
            StringBuffer classpath_sb = new StringBuffer();
            Iterator cp_it = this.classpath.iterator();
            while (cp_it.hasNext())
            {
                String cp = (String) cp_it.next();
                if (classpath_sb.length() > 0) classpath_sb.append(File.pathSeparatorChar);
                classpath_sb.append(new File(cp).getAbsolutePath());
            }

            String classpath_str = classpath_sb.toString();

            // - add classpath argument to command line
            if (classpath_str.length() > 0)
            {
                args.add("-classpath");
                cmdline_len += 11;
                args.add(classpath_str);
                cmdline_len += classpath_str.length() + 1;
            }

            // remember how many arguments we have which don't change for the
            // job
            int common_args_no = args.size();
            // remember how long the common command line is
            int common_args_len = cmdline_len;

            // used for execution
            FileExecutor executor = new FileExecutor();
            String output[] = new String[2];

            // used for displaying the progress bar
            String jobfiles = "";
            int fileno = 0;
            int last_fileno = 0;

            // now iterate over all files of this job
            Iterator file_it = this.files.iterator();

            while (file_it.hasNext())
            {
                File f = (File) file_it.next();

                String fpath = f.getAbsolutePath();

                Debug.trace("processing " + fpath);

                // we add the file _first_ to the arguments to have a better
                // chance to get something done if the command line is almost
                // MAX_CMDLINE_SIZE or even above
                fileno++;
                jobfiles += f.getName() + " ";
                args.add(fpath);
                cmdline_len += fpath.length();

                // start compilation if maximum command line length reached
                if (cmdline_len >= MAX_CMDLINE_SIZE)
                {
                    Debug.trace("compiling " + jobfiles);

                    // display useful progress bar (avoid showing 100% while
                    // still
                    // compiling a lot)
                    this.listener.progress(last_fileno, jobfiles);
                    last_fileno = fileno;

                    String[] full_cmdline = (String[]) args.toArray(output);

                    int retval = executor.executeCommand(full_cmdline, output);

                    // update progress bar: compilation of fileno files done
                    this.listener.progress(fileno, jobfiles);

                    if (retval != 0)
                    {
                        CompileResult result = new CompileResult(this.langpack
                                .getString("CompilePanel.error"), full_cmdline, output[0],
                                output[1]);
                        this.listener.handleCompileError(result);
                        if (!result.isContinue()) return result;
                    }
                    else
                    {
                        // verify that all files have been compiled successfully
                        // I found that sometimes, no error code is returned
                        // although
                        // compilation failed.
                        Iterator arg_it = args.listIterator(common_args_no);
                        while (arg_it.hasNext())
                        {
                            File java_file = new File((String) arg_it.next());

                            String basename = java_file.getName();
                            int dotpos = basename.lastIndexOf('.');
                            basename = basename.substring(0, dotpos) + ".class";
                            File class_file = new File(java_file.getParentFile(), basename);

                            if (!class_file.exists())
                            {
                                CompileResult result = new CompileResult(this.langpack
                                        .getString("CompilePanel.error.noclassfile")
                                        + java_file.getAbsolutePath(), full_cmdline, output[0],
                                        output[1]);
                                this.listener.handleCompileError(result);
                                if (!result.isContinue()) return result;
                                // don't continue any further
                                break;
                            }

                        }

                    }

                    // clean command line: remove files we just compiled
                    for (int i = args.size() - 1; i >= common_args_no; i--)
                    {
                        args.removeLast();
                    }

                    cmdline_len = common_args_len;
                    jobfiles = "";
                }

            }

            if (cmdline_len > common_args_len)
            {
                this.listener.progress(last_fileno, jobfiles);

                String[] full_cmdline = (String[]) args.toArray(output);

                int retval = executor.executeCommand(full_cmdline, output);

                this.listener.progress(fileno, jobfiles);

                if (retval != 0)
                {
                    CompileResult result = new CompileResult(this.langpack
                            .getString("CompilePanel.error"), full_cmdline, output[0], output[1]);
                    this.listener.handleCompileError(result);
                    if (!result.isContinue()) return result;
                }

            }

            Debug.trace("job " + this.name + " done (" + fileno + " files compiled)");

            return new CompileResult();
        }

        /**
         * Check whether the given compiler works.
         * 
         * This performs two steps:
         * <ol>
         * <li>check whether we can successfully call "compiler -help"</li>
         * <li>check whether we can successfully call "compiler -help arguments" (not all compilers
         * return an error here)</li>
         * </ol>
         * 
         * On failure, the method CompileHandler#errorCompile is called with a descriptive error
         * message.
         * 
         * @param compiler the compiler to use
         * @param arguments additional arguments to pass to the compiler
         * @return false on error
         */
        public CompileResult checkCompiler(String compiler, ArrayList arguments)
        {
            int retval = 0;
            FileExecutor executor = new FileExecutor();
            String[] output = new String[2];

            Debug.trace("checking whether \"" + compiler + " -help\" works");

            {
                String[] args = { compiler, "-help"};

                retval = executor.executeCommand(args, output);

                if (retval != 0)
                {
                    CompileResult result = new CompileResult(this.langpack
                            .getString("CompilePanel.error.compilernotfound"), args, output[0],
                            output[1]);
                    this.listener.handleCompileError(result);
                    if (!result.isContinue()) return result;
                }
            }

            Debug.trace("checking whether \"" + compiler + " -help +arguments\" works");

            // used to collect the arguments for executing the compiler
            LinkedList args = new LinkedList(arguments);

            // add -help argument to prevent the compiler from doing anything
            args.add(0, "-help");

            // add compiler in front of arguments
            args.add(0, compiler);

            // construct classpath argument for compiler
            // - collect all classpaths
            StringBuffer classpath_sb = new StringBuffer();
            Iterator cp_it = this.classpath.iterator();
            while (cp_it.hasNext())
            {
                String cp = (String) cp_it.next();
                if (classpath_sb.length() > 0) classpath_sb.append(File.pathSeparatorChar);
                classpath_sb.append(new File(cp).getAbsolutePath());
            }

            String classpath_str = classpath_sb.toString();

            // - add classpath argument to command line
            if (classpath_str.length() > 0)
            {
                args.add("-classpath");
                args.add(classpath_str);
            }

            String[] args_arr = (String[]) args.toArray(output);

            retval = executor.executeCommand(args_arr, output);

            if (retval != 0)
            {
                CompileResult result = new CompileResult(this.langpack
                        .getString("CompilePanel.error.invalidarguments"), args_arr, output[0],
                        output[1]);
                this.listener.handleCompileError(result);
                if (!result.isContinue()) return result;
            }

            return new CompileResult();
        }

    }

}
