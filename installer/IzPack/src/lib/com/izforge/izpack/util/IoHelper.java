/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Elmar Klaus Bartz
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

package com.izforge.izpack.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * <p>
 * Class with some IO related helper.
 * </p>
 * 
 */
public class IoHelper
{

    // This class uses the same values for family and flavor as
    // TargetFactory. But this class should not depends on TargetFactory,
    // because it is possible that TargetFactory is not bound. Therefore
    // the definition here again.

    // ------------------------------------------------------------------------
    // Constant Definitions
    // ------------------------------------------------------------------------

    /** Placeholder during translatePath computing */
    private static final String MASKED_SLASH_PLACEHOLDER = "~&_&~";

    private static Properties envVars = null;

    /**
     * Default constructor
     */
    private IoHelper()
    {
    }

    /**
     * Copies the contents of inFile into outFile.
     * 
     * @param inFile path of file which should be copied
     * @param outFile path of file to create and copy the contents of inFile into
     */
    public static void copyFile(String inFile, String outFile) throws IOException
    {
        copyFile(new File(inFile), new File(outFile));
    }

    /**
     * Creates an in- and output stream for the given File objects and copies all the data from the
     * specified input to the specified output.
     * 
     * @param inFile File object for input
     * @param outFile File object for output
     * @exception IOException if an I/O error occurs
     */
    public static void copyFile(File inFile, File outFile) throws IOException
    {
        copyFile(inFile, outFile, null, null);
    }

    /**
     * Creates an in- and output stream for the given File objects and copies all the data from the
     * specified input to the specified output. If permissions is not null, a chmod will be done on
     * the output file.
     * 
     * @param inFile File object for input
     * @param outFile File object for output
     * @param permissions permissions for the output file
     * @exception IOException if an I/O error occurs
     */
    public static void copyFile(File inFile, File outFile, String permissions) throws IOException
    {
        copyFile(inFile, outFile, permissions, null);
    }

    /**
     * Creates an in- and output stream for the given File objects and copies all the data from the
     * specified input to the specified output. If the VariableSubstitutor is not null, a substition
     * will be done during copy.
     * 
     * @param inFile File object for input
     * @param outFile File object for output
     * @param vss substitutor which is used during copying
     * @exception IOException if an I/O error occurs
     */
    public static void copyFile(File inFile, File outFile, VariableSubstitutor vss)
            throws IOException
    {
        copyFile(inFile, outFile, null, vss);
    }

    /**
     * Creates an in- and output stream for the given File objects and copies all the data from the
     * specified input to the specified output. If the VariableSubstitutor is not null, a substition
     * will be done during copy. If permissions is not null, a chmod will be done on the output
     * file.
     * 
     * @param inFile File object for input
     * @param outFile File object for output
     * @param permissions permissions for the output file
     * @param vs substitutor which is used during copying
     * @exception IOException if an I/O error occurs
     */
    public static void copyFile(File inFile, File outFile, String permissions,
            VariableSubstitutor vs) throws IOException
    {
        copyFile(inFile, outFile, permissions, vs, null);
    }

    /**
     * Creates an in- and output stream for the given File objects and copies all the data from the
     * specified input to the specified output. If the VariableSubstitutor is not null, a substition
     * will be done during copy. If permissions is not null, a chmod will be done on the output
     * file. If type is not null, that type is used as file type at substitution.
     * 
     * @param inFile File object for input
     * @param outFile File object for output
     * @param permissions permissions for the output file
     * @param vs substitutor which is used during copying
     * @param type file type for the substitutor
     * @exception IOException if an I/O error occurs
     */
    public static void copyFile(File inFile, File outFile, String permissions,
            VariableSubstitutor vs, String type) throws IOException
    {
        FileOutputStream out = new FileOutputStream(outFile);
        FileInputStream in = new FileInputStream(inFile);
        if (vs == null)
        {
            byte[] buffer = new byte[5120];
            long bytesCopied = 0;
            int bytesInBuffer;
            while ((bytesInBuffer = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, bytesInBuffer);
                bytesCopied += bytesInBuffer;
            }
            in.close();
            out.close();
        }
        else
        {
            BufferedInputStream bin = new BufferedInputStream(in, 5120);
            BufferedOutputStream bout = new BufferedOutputStream(out, 5120);
            vs.substitute(bin, bout, type, null);
            bin.close();
            bout.close();
        }
        if (permissions != null && IoHelper.supported("chmod"))
        {
            chmod(outFile.getAbsolutePath(), permissions);
        }
    }

    /**
     * Creates a temp file with delete on exit rule. The extension is extracted from the template if
     * possible, else the default extension is used. The contents of template will be copied into
     * the temporary file.
     * 
     * @param template file to copy from and define file extension
     * @param defaultExtension file extension if no is contained in template
     * @return newly created and filled temporary file
     * @throws IOException
     */
    public static File copyToTempFile(File template, String defaultExtension) throws IOException
    {
        return copyToTempFile(template, defaultExtension, null);
    }

    /**
     * Creates a temp file with delete on exit rule. The extension is extracted from the template if
     * possible, else the default extension is used. The contents of template will be copied into
     * the temporary file. If the variable substitutor is not null, variables will be replaced
     * during copying.
     * 
     * @param template file to copy from and define file extension
     * @param defaultExtension file extension if no is contained in template
     * @param vss substitutor which is used during copying
     * @return newly created and filled temporary file
     * @throws IOException
     */
    public static File copyToTempFile(File template, String defaultExtension,
            VariableSubstitutor vss) throws IOException
    {
        String path = template.getCanonicalPath();
        int pos = path.lastIndexOf('.');
        String ext = path.substring(pos);
        if (ext == null) ext = defaultExtension;
        File tmpFile = File.createTempFile("izpack_io", ext);
        tmpFile.deleteOnExit();
        IoHelper.copyFile(template, tmpFile, vss);
        return tmpFile;
    }

    /**
     * Creates a temp file with delete on exit rule. The extension is extracted from the template if
     * possible, else the default extension is used. The contents of template will be copied into
     * the temporary file.
     * 
     * @param template file to copy from and define file extension
     * @param defaultExtension file extension if no is contained in template
     * @return newly created and filled temporary file
     * @throws IOException
     */
    public static File copyToTempFile(String template, String defaultExtension) throws IOException
    {
        return copyToTempFile(new File(template), defaultExtension);
    }

    /**
     * Changes the permissions of the given file to the given POSIX permissions.
     * 
     * @param file the file for which the permissions should be changed
     * @param permissions POSIX permissions to be set
     * @throws IOException if an I/O error occurs
     */
    public static void chmod(File file, String permissions) throws IOException
    {
        chmod(file.getAbsolutePath(), permissions);
    }

    /**
     * Changes the permissions of the given file to the given POSIX permissions. This method will be
     * raised an exception, if the OS is not UNIX.
     * 
     * @param path the absolute path of the file for which the permissions should be changed
     * @param permissions POSIX permissions to be set
     * @throws IOException if an I/O error occurs
     */
    public static void chmod(String path, String permissions) throws IOException
    {
        // Perform UNIX
        if (OsVersion.IS_UNIX)
        {
            String[] params = { "chmod", permissions, path};
            String[] output = new String[2];
            FileExecutor fe = new FileExecutor();
            fe.executeCommand(params, output);
        }
        else
        {
            throw new IOException("Sorry, chmod not supported yet on " + OsVersion.OS_NAME + ".");
        }
    }

    /**
     * Returns the free (disk) space for the given path. If it is not ascertainable -1 returns.
     * 
     * @param path path for which the free space should be detected
     * @return the free space for the given path
     */
    public static long getFreeSpace(String path)
    {
        long retval = -1;
        if (OsVersion.IS_WINDOWS)
        {
            String command = "cmd.exe";
            if (System.getProperty("os.name").toLowerCase().indexOf("windows 9") > -1) return (-1);
            String[] params = { command, "/C", "\"dir /D /-C \"" + path + "\"\""};
            String[] output = new String[2];
            FileExecutor fe = new FileExecutor();
            fe.executeCommand(params, output);
            retval = extractLong(output[0], -3, 3, "%");
        }
        else if (OsVersion.IS_SUNOS)
        {
            String[] params = { "df", "-k", path};
            String[] output = new String[2];
            FileExecutor fe = new FileExecutor();
            fe.executeCommand(params, output);
            retval = extractLong(output[0], -3, 3, "%") * 1024;
        }
        else if (OsVersion.IS_HPUX)
        {
            String[] params = { "bdf", path };
            String[] output = new String[2];
            FileExecutor fe = new FileExecutor();
            fe.executeCommand(params, output);
            retval = extractLong(output[0], -3, 3, "%") * 1024;
        }
        else if (OsVersion.IS_UNIX)
        {
            String[] params = { "df", "-Pk", path};
            String[] output = new String[2];
            FileExecutor fe = new FileExecutor();
            fe.executeCommand(params, output);
            retval = extractLong(output[0], -3, 3, "%") * 1024;
        }
        return retval;
    }

    /**
     * Returns whether the given method will be supported with the given environment. Some methods
     * of this class are not supported on all operation systems.
     * 
     * @param method name of the method
     * @return true if the method will be supported with the current enivronment else false
     * @throws RuntimeException if the given method name does not exist
     */
    public static boolean supported(String method)
    {
        if ("getFreeSpace".equals(method))
        {
            if (OsVersion.IS_UNIX) return true;
            if (OsVersion.IS_WINDOWS)
            { // getFreeSpace do not work on Windows 98.
                if (System.getProperty("os.name").toLowerCase().indexOf("windows 9") > -1)
                    return (false);
                return (true);
            }
        }
        else if ("chmod".equals(method))
        {
            if (OsVersion.IS_UNIX) return true;
        }
        else if ("copyFile".equals(method))
        {
            return true;
        }
        else if ("getPrimaryGroup".equals(method))
        {
            if (OsVersion.IS_UNIX) return true;
        }
        else if ("getenv".equals(method))
        {
            return true;
        }
        else
        {
            throw new RuntimeException("method name " + method + "not supported by this method");
        }
        return false;

    }

    /**
     * Returns the first existing parent directory in a path
     * 
     * @param path path which should be scanned
     * @return the first existing parent directory in a path
     */
    public static File existingParent(File path)
    {
        File result = path;
        while (!result.exists())
        {
            if (result.getParent() == null) return result;
            result = result.getParentFile();
        }
        return result;
    }

    /**
     * Extracts a long value from a string in a special manner. The string will be broken into
     * tokens with a standard StringTokenizer. Arround the assumed place (with the given half range)
     * the tokens are scaned reverse for a token which represents a long. if useNotIdentifier is not
     * null, tokens which are contains this string will be ignored. The first founded long returns.
     * 
     * @param in the string which should be parsed
     * @param assumedPlace token number which should contain the value
     * @param halfRange half range for detection range
     * @param useNotIdentifier string which determines tokens which should be ignored
     * @return founded long
     */
    private static long extractLong(String in, int assumedPlace, int halfRange,
            String useNotIdentifier)
    {
        long retval = -1;
        StringTokenizer st = new StringTokenizer(in);
        int length = st.countTokens();
        int i;
        int currentRange = 0;
        String[] interestedEntries = new String[halfRange + halfRange];
        for (i = 0; i < length - halfRange + assumedPlace; ++i)
            st.nextToken(); // Forget this entries.

        for (i = 0; i < halfRange + halfRange; ++i)
        { // Put the interesting Strings into an intermediaer array.
            if (st.hasMoreTokens())
            {
                interestedEntries[i] = st.nextToken();
                currentRange++;
            }
        }

        for (i = currentRange - 1; i >= 0; --i)
        {
            if (useNotIdentifier != null && interestedEntries[i].indexOf(useNotIdentifier) > -1)
                continue;
            try
            {
                retval = Long.parseLong(interestedEntries[i]);
            }
            catch (NumberFormatException nfe)
            {
                continue;
            }
            break;
        }
        return retval;
    }

    /**
     * Returns the primary group of the current user. This feature will be supported only on Unix.
     * On other systems null returns.
     * 
     * @return the primary group of the current user
     */
    public static String getPrimaryGroup()
    {
        if (supported("getPrimaryGroup"))
        {
            if (OsVersion.IS_SUNOS)
            { // Standard id of SOLARIS do not support -gn.
                String[] params = { "id"};
                String[] output = new String[2];
                FileExecutor fe = new FileExecutor();
                fe.executeCommand(params, output);
                // No we have "uid=%u(%s) gid=%u(%s)"
                if (output[0] != null)
                {
                    StringTokenizer st = new StringTokenizer(output[0], "()");
                    int length = st.countTokens();
                    if (length >= 4)
                    {
                        for (int i = 0; i < 3; ++i)
                            st.nextToken();
                        return (st.nextToken());
                    }
                }
                return (null);
            }
            else
            {
                String[] params = { "id", "-gn"};
                String[] output = new String[2];
                FileExecutor fe = new FileExecutor();
                fe.executeCommand(params, output);
                return output[0];
            }
        }
        else
            return null;
    }

    /**
     * Returns a string resulting from replacing all occurrences of what in this string with with.
     * In opposite to the String.replaceAll method this method do not use regular expression or
     * other methods which are only available in JRE 1.4 and later. This method was special made to
     * mask masked slashes to avert a conversion during path translation.
     * 
     * @param destination string for which the replacing should be performed
     * @param what what string should be replaced
     * @param with with what string what should be replaced
     * @return a new String object if what was found in the given string, else the given string self
     */
    public static String replaceString(String destination, String what, String with)
    {
        if (destination.indexOf(what) >= 0)
        { // what found, with (placeholder) not included in destination ->
            // perform changing.
            StringBuffer buf = new StringBuffer();
            int last = 0;
            int current = destination.indexOf(what);
            int whatLength = what.length();
            while (current >= 0)
            { // Do not use Methods from JRE 1.4 and higher ...
                if (current > 0) buf.append(destination.substring(last, current));
                buf.append(with);
                last = current + whatLength;
                current = destination.indexOf(what, last);
            }
            if (destination.length() > last) buf.append(destination.substring(last));
            return buf.toString();
        }
        return destination;
    }

    /**
     * Translates a relative path to a local system path.
     * 
     * @param destination The path to translate.
     * @return The translated path.
     */
    public static String translatePath(String destination, VariableSubstitutor vs)
    {
        // Parse for variables
        destination = vs.substitute(destination, null);

        // Convert the file separator characters

        // destination = destination.replace('/', File.separatorChar);
        // Undo the conversion if the slashes was masked with
        // a backslash

        // Not all occurencies of slashes are path separators. To differ
        // between it we allow to mask a slash with a backslash infront.
        // Unfortunately we cannot use String.replaceAll because it
        // handles backslashes in the replacement string in a special way
        // and the method exist only beginning with JRE 1.4.
        // Therefore the little bit crude way following ...
        if (destination.indexOf("\\/") >= 0 && destination.indexOf(MASKED_SLASH_PLACEHOLDER) < 0)
        { // Masked slash found, placeholder not included in destination ->
            // perform masking.
            destination = replaceString(destination, "\\/", MASKED_SLASH_PLACEHOLDER);
            // Masked slashes changed to MASKED_SLASH_PLACEHOLDER.
            // Replace unmasked slashes.
            destination = destination.replace('/', File.separatorChar);
            // Replace the MASKED_SLASH_PLACEHOLDER to slashes; masking
            // backslashes will
            // be removed.
            destination = replaceString(destination, MASKED_SLASH_PLACEHOLDER, "/");
        }
        else
            destination = destination.replace('/', File.separatorChar);
        return destination;
    }

    /**
     * Returns the value of the environment variable given by key. This method is a work around for
     * VM versions which do not support getenv in an other way. At the first call all environment
     * variables will be loaded via an exec. On Windows keys are not case sensitive.
     * 
     * @param key variable name for which the value should be resolved
     * @return the value of the environment variable given by key
     */
    public static String getenv(String key)
    {
        if (envVars == null) loadEnv();
        if (envVars == null) return (null);
        if (OsVersion.IS_WINDOWS) key = key.toUpperCase();
        return (String) (envVars.get(key));
    }

    /**
     * Loads all environment variables via an exec.
     */
    private static void loadEnv()
    {
        String[] output = new String[2];
        String[] params;
        if (OsVersion.IS_WINDOWS)
        {
            String command = "cmd.exe";
            if (System.getProperty("os.name").toLowerCase().indexOf("windows 9") > -1)
                command = "command.com";
            String[] paramst = { command, "/C", "set"};
            params = paramst;
        }
        else
        {
            String[] paramst = { "env"};
            params = paramst;
        }
        FileExecutor fe = new FileExecutor();
        fe.executeCommand(params, output);
        if (output[0].length() <= 0) return;
        String lineSep = System.getProperty("line.separator");
        StringTokenizer st = new StringTokenizer(output[0], lineSep);
        envVars = new Properties();
        String var = null;
        while (st.hasMoreTokens())
        {
            String line = st.nextToken();
            if (line.indexOf('=') == -1)
            { // May be a env var with a new line in it.
                if (var == null)
                {
                    var = lineSep + line;
                }
                else
                {
                    var += lineSep + line;
                }
            }
            else
            { // New var, perform the previous one.
                setEnvVar(var);
                var = line;
            }
        }
        setEnvVar(var);
    }

    /**
     * Extracts key and value from the given string var. The key should be separated from the value
     * by a sign. On Windows all chars of the key are translated to upper case.
     * 
     * @param var
     */
    private static void setEnvVar(String var)
    {
        if (var == null) return;
        int index = var.indexOf('=');
        if (index < 0) return;
        String key = var.substring(0, index);
        // On windows change all key chars to upper.
        if (OsVersion.IS_WINDOWS) key = key.toUpperCase();
        envVars.setProperty(key, var.substring(index + 1));

    }
}
