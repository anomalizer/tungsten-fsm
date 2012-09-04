/**
 * Tungsten Scale-Out Stack
 * Copyright (C) 2007-2009 Continuent Inc.
 * Contact: tungsten@continuent.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of version 2 of the GNU General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
 *
 * Initial developer(s): Robert Hodges
 * Contributor(s):
 */

package com.continuent.tungsten.commons.patterns.fsm;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implements a latch that waits for a state machine reach a specified state or
 * optionally the error state. StateTransitionLatch instances are designed to be
 * used with the JDK 1.5+ concurrency libraries as shown in the following sample
 * of typical use.
 * 
 * <pre><code>
 *   StateTransitionLatch latch = myStateMachine.createStateTransitionLatch(mystate, true);
 *   Future<State> result = exec.submit(latch);
 *   ...
 *   State finalState = result.get(10, TimeUnit.SECONDS);
 *   if (latch.isExpected())
 *   {
 *      // we found the expected state
 *   }
 * </code></pre>
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class StateTransitionLatch<ET extends Entity>
        implements
            Callable<State>,
            StateChangeListener<ET>
{
    private final StateMachine<ET>   stateMachine;
    private final State<ET>          expected;
    private final boolean        endOnError;

    private State<ET>                errorState;
    private State<ET>                current;
    private boolean              done;
    private boolean              reachedExpected;
    private boolean              reachedError;
    private BlockingQueue<State<ET>> stateQueue = new LinkedBlockingQueue<State<ET>>();

    /**
     * Defines a new latch that waits for the the state machine to reach a
     * specified state or the error state.
     * 
     * @param sm State machine on whose state we are waiting
     * @param expected State we are awaiting
     * @param endOnError If true, end if we reach the error state
     */
    public StateTransitionLatch(StateMachine<ET> sm, State<ET> expected,
            boolean endOnError)
    {
        this.stateMachine = sm;
        this.expected = expected;
        this.endOnError = endOnError;
        errorState = sm.getErrorState();
    }

    /**
     * Returns true if we reached either the error state or some other state.
     */
    public boolean isDone()
    {
        return done;
    }

    /**
     * Returns true if we successfully reached the expected state.
     */
    public boolean isExpected()
    {
        return reachedExpected;
    }

    /**
     * Returns true if we dropped into the error state.
     */
    public boolean isError()
    {
        return reachedError;
    }

    /**
     * Returns the current state. Synchronization ensures that this value is
     * correctly visible across threads.
     */
    protected synchronized State<ET> getCurrent()
    {
        return current;
    }

    /**
     * Sets the current state with synchronization to ensure visibility across
     * threads.
     */
    protected synchronized void setCurrent(State<ET> state)
    {
        this.current = state;
    }

    /**
     * Enqueues a new state for examination by the latch.
     */
    @Override
    public synchronized void stateChanged(ET entity, State<ET> oldState,
            State<ET> newState)
    {
        stateQueue.add(newState);
    }

    /**
     * Implements the latching logic, namely waiting until one of the following
     * conditions is fulfilled:
     * <ul>
     * <li>We reach the desired state</li>
     * <li>We reach the error state (optional)</li>
     * <li>We are cancelled</li>
     * </ul>
     * We return the current state at the end of the run if and only if we find
     * a desired state. Predicates like isExpected() allow clients to determine
     * what we found.
     * 
     * @return Current state if successful or null
     */
    public State<ET> call()
    {
        try
        {
            // WARNING: Must synchronize adding listener and setting
            // initial state or you can miss a notification from the state
            // machine.
            synchronized (stateMachine)
            {
                stateMachine.addListener(this);
                this.stateQueue.add(stateMachine.getState());
            }

            // Run until we finish or somebody interrupts us.
            while (!done && !Thread.interrupted())
            {
                try
                {
                    setCurrent(stateQueue.take());
                    // Use startsWith to handle parent states. 
                    if (current.getName().startsWith(expected.getName()))
                    {
                        done = true;
                        reachedExpected = true;
                    }
                    else if (endOnError && errorState != null
                            && current.equals(errorState))
                    {
                        done = true;
                        reachedError = true;
                    }
                }
                catch (InterruptedException e)
                {
                    // Interruption means somebody is trying to cancel us.
                    break;
                }
            }
        }
        finally
        {
            stateMachine.removeListener(this);
        }

        // If we are done, return the current state. Otherwise, return null.
        if (done)
            return current;
        else
            return null;
    }
}