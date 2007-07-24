/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2001,2002 Marcus Stursberg
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

package com.izforge.izpack.compiler;

/**
 * Indicates a Failure to compile.
 * 
 * @author Marcus Stursberg
 */
public class CompilerException extends java.io.IOException
{

    static final long serialVersionUID = 6247426753392546734L;

    /**
     * The throwable that caused this throwable to get thrown, or null if this throwable was not
     * caused by another throwable, or if the causative throwable is unknown. If this field is equal
     * to this throwable itself, it indicates that the cause of this throwable has not yet been
     * initialized.
     */
    private Throwable _cause = this;

    /**
     * Construct a new exception with the specified message.
     * 
     * @param message Description of the error
     */
    public CompilerException(String message)
    {
        super(message);
    }

    /**
     * Construct a new exception with the specified message and wraps another cause.
     * 
     * @param message Description of the error
     * @param cause Throwable
     */
    public CompilerException(String message, Throwable cause)
    {
        super(message);
        this._cause = cause;
    }

    /**
     * Initializes the <i>cause</i> of this throwable to the specified value. (The cause is the
     * throwable that caused this throwable to get thrown.)
     * 
     * <p>
     * This method can be called at most once. It is generally called from within the constructor,
     * or immediately after creating the throwable. If this throwable was created with {@link
     * #CompilerException(String,Throwable)}, this method cannot be called even once.
     * 
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()}
     * method). (A <code>null</code> value is permitted, and indicates that the cause is
     * nonexistent or unknown.)
     * @return a reference to this <code>Throwable</code> instance.
     * @throws IllegalArgumentException if <code>cause</code> is this throwable. (A throwable
     * cannot be its own cause.)
     * @throws IllegalStateException if this throwable was created with {@link
     * #CompilerException(String,Throwable)}, or this method has already been called on this
     * throwable.
     */
    public synchronized Throwable initCause(Throwable cause)
    {
        if (this._cause != this) throw new IllegalStateException("Can't overwrite cause");
        if (cause == this) throw new IllegalArgumentException("Self-causation not permitted");
        this._cause = cause;
        return this;
    }

    /**
     * Returns the cause of this throwable or <code>null</code> if the cause is nonexistent or
     * unknown. (The cause is the throwable that caused this throwable to get thrown.)
     * 
     * <p>
     * This implementation returns the cause that was supplied via one of the constructors requiring
     * a <code>Throwable</code>, or that was set after creation with the
     * {@link #initCause(Throwable)} method. While it is typically unnecessary to override this
     * method, a subclass can override it to return a cause set by some other means. This is
     * appropriate for a "legacy chained throwable" that predates the addition of chained exceptions
     * to <code>Throwable</code>. Note that it is <i>not</i> necessary to override any of the
     * <code>PrintStackTrace</code> methods, all of which invoke the <code>getCause</code>
     * method to determine the cause of a throwable.
     * 
     * @return the cause of this throwable or <code>null</code> if the cause is nonexistent or
     * unknown.
     */
    public Throwable getCause()
    {
        return (_cause == this ? null : _cause);
    }
}
