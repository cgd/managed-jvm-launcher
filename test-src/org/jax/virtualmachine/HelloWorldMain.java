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

import java.util.Map;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class HelloWorldMain
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
//        System.load("/Library/Frameworks/R.framework/Versions/2.4/Resources/lib/" + "libR.dylib");
        System.out.println("Hello world!");
        System.out.println(System.mapLibraryName("R"));
        System.err.println("my error output");
        
        for(Map.Entry<String, String> currEnv: System.getenv().entrySet())
        {
            System.out.println("ENV: " + currEnv.getKey() + "=" + currEnv.getValue());
        }
        
        System.out.println(
                "java.library.path=" + System.getProperty("java.library.path"));
        System.out.println(
                "java.class.path=" + System.getProperty("java.class.path"));
    }
}
