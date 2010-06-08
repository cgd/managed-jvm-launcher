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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jax.util.TypeSafeSystemProperties;


/**
 * The parameters that are used by {@link VirtualMachineLauncher} to launch
 * a new virtual machine.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
// TODO this should become a JavaProcessBuilder
// TODO finish comments
// TODO add things like -jar vs -cp, start dir etc...
public class VirtualMachineSettings
{
    private String mainClassName;
    
    private long maxMemoryMegabytes;
    
    private boolean useDefaultMaxMemory;
    
    private Map<String, String> environment;
    
    private Properties systemProperties;
    
    private List<String> applicationArguments;
    
    private String classpath;
    
    private static final int BYTES_PER_MEGABYTE = (1 << 20);

    /**
     * Constructor
     */
    public VirtualMachineSettings()
    {
        this.mainClassName = null;
        this.maxMemoryMegabytes =
            Runtime.getRuntime().maxMemory() / BYTES_PER_MEGABYTE;
        this.useDefaultMaxMemory = true;
        this.environment = new HashMap<String, String>(System.getenv());
        this.systemProperties = new Properties();
        this.applicationArguments = new ArrayList<String>();
        this.classpath = TypeSafeSystemProperties.getSystemClassPath();
        this.setJavaLibraryPath(
                TypeSafeSystemProperties.getJavaLibraryPath());
    }
    
    /**
     * Getter for the main class that the VM is launching
     * @return
     *          the fully qualified name of the main class
     */
    public String getMainClassName()
    {
        return this.mainClassName;
    }

    /**
     * Setter for the main class that the VM is launching
     * @param mainClassName
     *          the fully qualified name of the main class
     */
    public void setMainClassName(String mainClassName)
    {
        this.mainClassName = mainClassName;
    }

    /**
     * Getter for the maximum memory allocation
     * @return
     *          the maximum VM memory in MB
     */
    public long getMaxMemoryMegabytes()
    {
        return this.maxMemoryMegabytes;
    }

    /**
     * Set the max memory for the JVM. This implicitly calls
     * {@link #setUseDefaultMaxMemory(boolean)} with <code>false</code>
     * @param maxMemoryMegabytes the maxMemoryBytes to set
     */
    public void setMaxMemoryMegabytes(long maxMemoryMegabytes)
    {
        this.setUseDefaultMaxMemory(false);
        this.maxMemoryMegabytes = maxMemoryMegabytes;
    }
    
    /**
     * Prepend the given path to the java library path.
     * {@link File#pathSeparator} is inserted between the given path
     * and the current java.library.path
     * @param pathToPrepend
     *          the path that we're prepending to the java.library.path
     */
    public void prependToJavaLibraryPath(String pathToPrepend)
    {
        if(this.getJavaLibraryPath() == null)
        {
            this.setJavaLibraryPath(pathToPrepend);
        }
        else
        {
            this.setJavaLibraryPath(
                    pathToPrepend +
                    File.pathSeparator +
                    this.getJavaLibraryPath());
        }
    }
    
    /**
     * Get the matching environment key using a case insensitive comparison.
     * If it doesn't exist, return null.
     * @param key
     *          the key
     * @return
     *          the match or null if we cant find it
     */
    public String getEnvironmentKeyCaseInsensitive(String key)
    {
        for(String currKey: this.getEnvironment().keySet())
        {
            if(key.equalsIgnoreCase(currKey))
            {
                return currKey;
            }
        }
        
        return null;
    }
    
    /**
     * Like {@link #prependToEnvironmentVariable(String, String)} except we
     * prepend even if the key's case doesn't match up
     * @param caseInsensitiveEnvironmentVariableKey
     *          the key (name)
     * @param pathToPrepend
     *          the path to prepend
     */
    public void prependToEnvironmentVariableCaseInsensitive(
            final String caseInsensitiveEnvironmentVariableKey,
            final String pathToPrepend)
    {
        String matchingKey = this.getEnvironmentKeyCaseInsensitive(
                caseInsensitiveEnvironmentVariableKey);
        if(matchingKey == null)
        {
            this.prependToEnvironmentVariable(
                    caseInsensitiveEnvironmentVariableKey,
                    pathToPrepend);
        }
        else
        {
            this.prependToEnvironmentVariable(
                    matchingKey,
                    pathToPrepend);
        }
    }
    
    /**
     * prepend the given path to the given environment variable. if the
     * environment variable doesn't exist, create it with the given path
     * @param environmentVariableKey
     *          the key (name) of the environment variable
     * @param pathToPrepend
     *          the path that we're going to prepend
     */
    public void prependToEnvironmentVariable(
            final String environmentVariableKey,
            final String pathToPrepend)
    {
        String envVarValue = this.getEnvironment().get(
                environmentVariableKey);
        if(envVarValue == null)
        {
            // just set the value
            envVarValue = pathToPrepend;
        }
        else
        {
            // prepend the value
            envVarValue =
                pathToPrepend +
                File.pathSeparator +
                envVarValue;
        }
        this.getEnvironment().put(
                environmentVariableKey,
                envVarValue);
    }
    
    /**
     * The java.library.path to use for the new JVM. A null indicates that
     * the default path should be used. The setter and getter for this
     * are both just pass throughs to the
     * {@link #getSystemProperties() system properties} so if the backing
     * property is modified, that will affect this property and vice
     * versa. The default value for this property is inherited from
     * the current runtime.
     * @return the javaLibraryPath
     */
    public String getJavaLibraryPath()
    {
        return this.systemProperties.getProperty(
                TypeSafeSystemProperties.JAVA_LIB_PATH_PROP_NAME);
    }

    /**
     * Setter for the java.library.path
     * @param javaLibraryPath
     *          the java.library.path
     */
    public void setJavaLibraryPath(String javaLibraryPath)
    {
        if(javaLibraryPath == null)
        {
            this.systemProperties.remove(
                    TypeSafeSystemProperties.JAVA_LIB_PATH_PROP_NAME);
        }
        else
        {
            this.systemProperties.setProperty(
                    TypeSafeSystemProperties.JAVA_LIB_PATH_PROP_NAME,
                    javaLibraryPath);
        }
    }

    /**
     * Getter that determines if we should use the JVM's default max
     * memory value
     * @return
     *          true if the default should be used
     */
    public boolean getUseDefaultMaxMemory()
    {
        return this.useDefaultMaxMemory;
    }

    /**
     * Setter for determining whether or not the JVM's default max memory
     * value should be used
     * @param useDefaultMaxMemory
     *          setter that determines if we should use the default JVM
     *          value for the maximum memory constraint
     */
    public void setUseDefaultMaxMemory(boolean useDefaultMaxMemory)
    {
        this.useDefaultMaxMemory = useDefaultMaxMemory;
    }

    /**
     * The environment variables to use. null means use the default
     * @return the environment
     */
    public Map<String, String> getEnvironment()
    {
        return this.environment;
    }

    /**
     * Setter for the environment
     * @param environment the environment to set
     */
    public void setEnvironment(Map<String, String> environment)
    {
        this.environment = environment;
    }

    /**
     * Getter for the system properties. These turn into '-D' arguments
     * for the JVM
     * @return the system properties
     */
    public Properties getSystemProperties()
    {
        return this.systemProperties;
    }

    /**
     * Setter for the system properties
     * @param systemProperties the systemProperties to set
     */
    public void setSystemProperties(Properties systemProperties)
    {
        this.systemProperties = systemProperties;
    }

    /**
     * Getter for the application arguments
     * @return the applicationArguments
     */
    public List<String> getApplicationArguments()
    {
        return this.applicationArguments;
    }

    /**
     * Setter for the application arguments
     * @param applicationArguments the applicationArguments to set
     */
    public void setApplicationArguments(List<String> applicationArguments)
    {
        this.applicationArguments = applicationArguments;
    }

    /**
     * Getter for the classpath
     * @return the classpath
     */
    public String getClasspath()
    {
        return this.classpath;
    }

    /**
     * Setter for the classpath
     * @param classpath the classpath to set
     */
    public void setClasspath(String classpath)
    {
        this.classpath = classpath;
    }
}
