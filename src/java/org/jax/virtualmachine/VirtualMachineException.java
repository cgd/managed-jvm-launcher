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

/**
 * For problems that occur while attempting to launch a JVM
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class VirtualMachineException extends Exception
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 7737139468267328610L;

    /**
     * Constructor
     */
    public VirtualMachineException()
    {
    }

    /**
     * Constructor
     * @param message
     *          the message
     */
    public VirtualMachineException(String message)
    {
        super(message);
    }

    /**
     * Constructor
     * @param cause
     *          the underlying cause of this exception
     */
    public VirtualMachineException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor
     * @param message
     *          the message
     * @param cause
     *          the underlying cause of this exception
     */
    public VirtualMachineException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
