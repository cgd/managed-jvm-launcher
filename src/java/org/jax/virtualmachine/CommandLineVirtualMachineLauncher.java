/*
 * Copyright (c) 2009 The Jackson Laboratory
 * 
 * This software was developed by Gary Churchill's Lab at The Jackson
 * Laboratory (see http://research.jax.org/faculty/churchill).
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jax.virtualmachine;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.util.TypeSafeSystemProperties;
import org.jax.util.TypeSafeSystemProperties.OsFamily;

/**
 * Class used to launch a new JVM instance.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class CommandLineVirtualMachineLauncher implements VirtualMachineLauncher
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            CommandLineVirtualMachineLauncher.class.getName());
    
    /**
     * {@inheritDoc}
     */
    public void launchVirtualMachine(VirtualMachineSettings settings)
        throws VirtualMachineException
    {
        try
        {
            String javaExecutable =
                CommandLineVirtualMachineLauncher.getJavaExecutablePath();
            if(javaExecutable == null)
            {
                throw new VirtualMachineException(
                        "failed to locate the java executable");
            }
            else
            {
                List<String> jvmArgs =
                    CommandLineVirtualMachineLauncher.getVirtualMachineArguments(
                            settings);
                
                List<String> command = new ArrayList<String>();
                command.add(javaExecutable);
                command.addAll(jvmArgs);
                command.add(settings.getMainClassName());
                command.addAll(settings.getApplicationArguments());
                
                if(LOG.isLoggable(Level.FINE))
                {
                    StringBuffer logMessageBuffer = new StringBuffer(
                            "Launching JVM as:");
                    for(String commandToken: command)
                    {
                        logMessageBuffer.append(" ");
                        logMessageBuffer.append(commandToken);
                    }
                    
                    LOG.fine(logMessageBuffer.toString());
                }
                
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.environment().clear();
                processBuilder.environment().putAll(
                        settings.getEnvironment());
                
                CommandLineVirtualMachineLauncher.readAndPrintStreams(
                        processBuilder.start());
            }
        }
        catch(VirtualMachineException ex)
        {
            // this type of exception can be thrown as is
            throw ex;
        }
        catch(Exception ex)
        {
            // pass the exception on as a VM exception
            throw new VirtualMachineException(
                    "failed to initialize virtual machine",
                    ex);
        }
    }
    
    /**
     * Extract the JVM options from the given settings
     * @param settings
     *          the settings that we're extracting the JVM options from
     * @return
     *          the options list
     */
    private static List<String> getVirtualMachineArguments(
            VirtualMachineSettings settings)
    {
        List<String> argList = new ArrayList<String>();
        if(!settings.getUseDefaultMaxMemory())
        {
            argList.add("-Xmx" + settings.getMaxMemoryMegabytes() + "M");
        }
        
        for(Map.Entry<Object, Object> currProperty:
            settings.getSystemProperties().entrySet())
        {
            argList.add(CommandLineVirtualMachineLauncher.propertyToVirtualMachineArgument(
                    (String)currProperty.getKey(),
                    (String)currProperty.getValue()));
        }
        
        if(settings.getClasspath() != null)
        {
            argList.add("-classpath");
            argList.add(settings.getClasspath());
        }
        
        return argList;
    }
    
    /**
     * Convert a property key/value pair into a command line argument for the
     * JVM
     * @param propertyKey
     *          the property key
     * @param propertyValue
     *          the property value
     * @return
     *          the command line arg for the pair
     */
    private static String propertyToVirtualMachineArgument(
            String propertyKey,
            String propertyValue)
    {
        return "-D" + propertyKey + "=" + propertyValue;
    }

    /**
     * Get the path to the java executable
     * @return
     *          the path to the executable
     */
    private static String getJavaExecutablePath()
    {
        // first get the java home
        String javaHome = TypeSafeSystemProperties.getJavaHome();
        String osName = TypeSafeSystemProperties.getOsName();
        if(javaHome == null || osName == null)
        {
            if(javaHome == null)
            {
                LOG.warning("could not determine java home");
            }
            
            if(osName == null)
            {
                LOG.warning("could not determine OS name");
            }
            
            return null;
        }
        else
        {
            // we have what we need to build the executable path
            String javaExePath =
                javaHome + File.separator + "bin" +
                File.separator + "java";
            OsFamily osFamily = TypeSafeSystemProperties.getOsFamily();
            if(osFamily == OsFamily.WINDOWS_OS_FAMILY)
            {
                // give windows special treatment since its executables
                // use the ".exe" extension
                javaExePath += ".exe";
            }
            
            // validate that the executable exists before returning the path
            File javaExeFile = new File(javaExePath);
            if(!javaExeFile.exists())
            {
                LOG.warning(
                        "could not find the java executable at the " +
                        "expected location: " + javaExePath);
                javaExePath = null;
            }
            
            return javaExePath;
        }
    }

    /**
     * Read the stdout and stderr streams from the given process,
     * redirecting them to our own stdout and stderr. Only return after
     * both streams have completed.
     * @param process
     *          the process that we're reading from
     * @throws InterruptedException
     *          if our thread is interrupted
     */
    private static void readAndPrintStreams(Process process)
        throws InterruptedException
    {
        // buffer the streams
        final BufferedReader bufferedStdErr = new BufferedReader(
                new InputStreamReader(process.getErrorStream()));
        final BufferedReader bufferedStdOut = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        
        // spawn two threads to drain the output to a queue
        BlockingQueue<ReaderAndLinePair> drainQueue =
            new SynchronousQueue<ReaderAndLinePair>();
        Thread stdErrThread = new Thread(new ReaderToQueueDrain(
                bufferedStdErr,
                drainQueue));
        Thread stdOutThread = new Thread(new ReaderToQueueDrain(
                bufferedStdOut,
                drainQueue));
        stdErrThread.start();
        stdOutThread.start();
        
        // print output until we get the 2 null signals from our drains
        int nullSignalCount = 0;
        while(nullSignalCount < 2)
        {
            ReaderAndLinePair currPair = drainQueue.take();
            if(currPair.getLineRead() == null)
            {
                nullSignalCount++;
            }
            else
            {
                // direct the output to the correct stream
                if(currPair.getReader() == bufferedStdErr)
                {
                    System.err.println(currPair.getLineRead());
                }
                else
                {
                    System.out.println(currPair.getLineRead());
                }
            }
        }
    }
    
    /**
     * Drains the given reader to the given queue.
     * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
     */
    private static class ReaderToQueueDrain implements Runnable
    {
        private final BlockingQueue<ReaderAndLinePair> queue;
        
        private final BufferedReader reader;
        
        /**
         * Constructor
         * @param reader the reader to drain
         * @param queue the queue to drain to
         */
        public ReaderToQueueDrain(
                BufferedReader reader,
                BlockingQueue<ReaderAndLinePair> queue)
        {
            this.reader = reader;
            this.queue = queue;
        }

        /**
         * {@inheritDoc}
         */
        public void run()
        {
            try
            {
                for(String currLine = this.reader.readLine();
                    currLine != null;
                    currLine = this.reader.readLine())
                {
                    this.queue.put(new ReaderAndLinePair(this.reader, currLine));
                }
                
                // null signals that we're done
                this.queue.put(new ReaderAndLinePair(this.reader, null));
            }
            catch(Exception ex)
            {
                LOG.log(Level.SEVERE,
                        "received error while reading process output",
                        ex);
                
                // null signals that we're done
                try
                {
                    this.queue.put(new ReaderAndLinePair(this.reader, null));
                }
                catch(InterruptedException ex2)
                {
                    LOG.log(Level.SEVERE,
                            "i'm having a bad day. can't even send null signal",
                            ex);
                }
            }
        }
    }
    
    /**
     * A private class for correlating a line with the reader that it was read
     * from.
     * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
     */
    private static class ReaderAndLinePair
    {
        private final String lineRead;
        
        private final BufferedReader reader;
        
        /**
         * Constructor
         * @param reader
         *          see {@link #getReader()}
         * @param lineRead
         *          see {@link #getLineRead()}
         */
        public ReaderAndLinePair(BufferedReader reader, String lineRead)
        {
            this.lineRead = lineRead;
            this.reader = reader;
        }

        /**
         * @return the lineRead
         */
        public String getLineRead()
        {
            return this.lineRead;
        }

        /**
         * @return the reader
         */
        public BufferedReader getReader()
        {
            return this.reader;
        }
    }
}
