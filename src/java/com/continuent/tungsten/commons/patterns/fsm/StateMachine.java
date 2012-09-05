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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a finite state machine. Finite state machines are a simple but
 * powerful formalism for describing state of applications, particularly those
 * with real-time or concurrent behavior. Finite state machines consist of the
 * following elements:
 * <ul>
 * <li>States - The set of legal states of the system</li>
 * <li>Transitions - The legal changes between states</li>
 * <li>Events - A set of messages that may induce state changes</li>
 * <li>Guards - A set of conditions that determine the conditions under which an
 * event causes a particular transition to be taken</li>
 * </ul>
 * Usage of state machines is quite simple. Programs create a state machine from
 * a StateMachineMap, which is a static definition. The state machine then
 * accepts events and takes appropriate transitions depending on the state
 * machine definition.
 * <p>
 * The following example shows how to build a state machine and deliver events.
 * <p>
 * 
 * <pre><code>
 *  // Build the state transition map. 
 *  StateTransitionMap map = new StateTransitionMap();
 *  State started = new State("STARTED", null, StateType.START);
 *  State aborted = new State("ABORTED", null, StateType.END);
 *  State committed = new State("COMMITTED", null, StateType.END);
 *  Transition do_abort = new Transition(started, new PositiveGuard(), abort);
 *  Transition do_commit = new Transition(started, new NegativeGuard(), commit);
 *  
 *  map.addState(started);
 *  map.addState(aborted);
 *  map.addState(committed);
 *  map.addTransition(do_abort);
 *  map.addTransition(do_commit);
 *  map.build();
 *   
 *  Create a state machine and deliver an event.  
 *  StateMachine sm = new StateMachine(map);
 *  sm.deliverEvent(new Event("abort"));
 * </code></pre>
 * State machine maps can include Action classes, which allow clients to define
 * procedures that execute when an event triggers a transition, an state is
 * entered, or a state is exited.
 * <p>
 * State machines enforce basic synchronization between threads by synchronizing
 * the applyEvent() call. Additional synchronization, if required, must be
 * supplied by the application.
 * <p>
 * Finally, state machines have an error handling model that includes a family
 * of exceptions to signal error conditions both large and small. There is also
 * a default error state that will
 * <p>
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class StateMachine<ET extends Entity>
{
    private static Logger             logger              = LoggerFactory.getLogger(StateMachine.class);
    private State<ET>                     state;
    private final ET              entity;
    private final StateTransitionMap<ET>  map;
    private int                       transitions         = 0;
    private int                       maxTransitions      = 0;
    private List<StateChangeListener<ET>> listeners           = new ArrayList<StateChangeListener<ET>>();
    private boolean                   forwardChainEnabled = false;

    /**
     * Creates a new state machine in the default initialization state.
     */
    public StateMachine(StateTransitionMap<ET> map, ET entity)
    {
        this.map = map;
        this.entity = entity;
        this.state = map.getStartState();
    }

    /**
     * Sets the maximum number of state transitions allowed in this state
     * machine. This setting detects infinite loops.
     * 
     * @param max Maximum number of transitions or 0 to ignore
     */
    public synchronized void setMaxTransitions(int max)
    {
        this.maxTransitions = max;
    }

    /**
     * Add a state change listener. 
     */
    public synchronized void addListener(StateChangeListener<ET> listener)
    {
        listeners.add(listener);
    }

    /**
     * Remove a state change listener if it exists.
     * 
     * @return True if the listener was removed; false if it could not be found
     */
    public synchronized boolean removeListener(StateChangeListener listener)
    {
        return listeners.remove(listener);
    }

    /**
     * Applies a message to the state transition diagram, thereby triggering the
     * next state.
     * 
     * @param event A event
     * @throws TransitionNotFoundException Thrown if an appropriate transition
     *             cannot be found
     * @throws TransitionRollbackException Thrown if the transition is rolled
     *             back cleanly by action code
     * @throws FiniteStateException Thrown if a generic error occurs
     */
    public synchronized <T> void applyEvent(Event<T> event)
            throws FiniteStateException
    {
        TransitionFailureException deferredException = null;

        if (maxTransitions > 0)
        {
            transitions++;
            if (transitions > maxTransitions)
                throw new FiniteStateException(
                        "Max transition count exceeded: state="
                                + state.getName() + " transition count="
                                + transitions);
        }

        // Find the next transition. This is guaranteed to be non-null.
        Transition<ET, ?> transition = map.nextTransition(state, event, entity);
        State<ET> nextState = transition.getOutput();
        logger.debug("Executing state transition: input state={} transition={} output state={}",
                new Object[] {state.getName(), transition.getName(), nextState.getName()});

        int actionType = -1;
        try
        {
            // Compute the least common parent between the current and next
            // state. Entry and exit actions fire below this state only in
            // the state hierarchy.
            State<ET> leastCommonParent = state.getLeastCommonParent(nextState);

            // If we are transitioning to a new state look for exit actions.
            if (state != nextState)
            {
                // Fire exit actions up to the state below the least common
                // parent
                // if it exists.
                State<ET> exitState = state;
                logger.debug("Searching for exit actions for current state: {}",
                                state.getName());

                while (exitState != null && exitState != leastCommonParent)
                {
                    if (exitState.getExitAction() != null)
                    {
                        Action<ET> exitAction = exitState.getExitAction();
                        actionType = Action.EXIT_ACTION;
                        logger.debug("Executing exit action for state: {}",
                                exitState.getName());
                        exitAction.doAction(event, entity, transition,
                                actionType);
                    }

                    exitState = exitState.getParent();
                }
            }

            // Fire transition action if it exists.
            if (transition.getAction() != null)
            {
                Action<ET> transitionAction = transition.getAction();
                actionType = Action.TRANSITION_ACTION;
                logger.debug("Executing action for transition: {}",
                        transition.getName());
                transitionAction
                        .doAction(event, entity, transition, actionType);
            }

            // If we are transitioning to a new state look for entry actions.
            if (state != nextState)
            {
                logger.debug("Searching for entry actions for next state: {}",
                        nextState.getName());

                // Fire entry actions from the state below the least common
                // parent (if there is one) to the next state itself.
                State<ET>[] entryStates = nextState.getHierarchy();
                int startIndex = -1;
                if (leastCommonParent == null)
                    startIndex = 0;
                else
                {
                    for (int i = 0; i < entryStates.length; i++)
                    {
                        if (entryStates[i] == leastCommonParent)
                        {
                            startIndex = i + 1;
                            break;
                        }
                    }
                }

                for (int i = startIndex; i < entryStates.length; i++)
                {
                    State<ET> entryState = entryStates[i];
                    if (entryState.getEntryAction() != null)
                    {
                        Action<ET> entryAction = entryState.getEntryAction();
                        actionType = Action.ENTER_ACTION;
                        logger.debug("Executing entry action for state: {}",
                                entryState.getName());
                        entryAction.doAction(event, entity, transition,
                                actionType);
                    }
                }
            }
        }
        catch (TransitionRollbackException e)
        {
            // Log and rethrow a rollback exception.
            logger.debug("Transition rolled back: state={} transition={} actionType={}",
                new Object[] {state.getName(), transition.getName(), actionType});
            throw e;
        }
        catch (TransitionFailureException e)
        {
            // Transition to the error state and rethrow the exception.
            logger.debug("Transition failed: state={} transition={}  actionType={}",
                    new Object[] {state.getName(),  transition.getName(), actionType});

            State<ET> errorState = map.getErrorState();

            // Make sure we have an error state!
            if (errorState == null)
            {
                String msg = "Attempt to throw TransitionFailureException when no error state exists";
                logger.error(msg, e);
                throw new FiniteStateException(msg, e);
            }

            // Now transition to it or try to at least.
            try
            {
                Action<ET> errorStateEntryAction = errorState.getEntryAction();
                if (errorStateEntryAction != null)
                {
                    logger.debug("Executing entry action for error state: {}",
                                     errorState.getName());
                    errorStateEntryAction.doAction(event, entity, transition,
                            Action.ENTER_ACTION);
                }
                nextState = errorState;
            }
            catch (Throwable t)
            {
                // This bad. Nothing to do but throw an generic exception.
                throw new FiniteStateException(
                        "Transition to error state failed", t);
            }
            // Store so that the application sees there has been an error.
            deferredException = e;
        }

        // If we changed state, move to the new state and notify listeners.
        if (state != nextState)
        {
            logger.debug("Entering new state: {}", nextState.getName());

            State<ET> prevState = state;
            state = nextState;

            for (StateChangeListener<ET> listener : listeners)
            {
                listener.stateChanged(entity, prevState, nextState);
            }

            if (isForwardChainEnabled())
            {
                // Now see if we have a chained transition to handle. We can
                // expect to get
                // a FiniteStateException since we may not have a chained
                // transition.
                try
                {
                    if ((transition = map.nextTransition(state, event, entity)) != null)
                    {
                        applyEvent(event);
                    }
                }
                catch (FiniteStateException f)
                {
                    // Just ignore it.
                    return;
                }
            }
        }

        // If we have a deferred exception, throw it now.
        if (deferredException != null)
            throw deferredException;
    }

    /**
     * Returns the current state.
     */
    public State<ET> getState()
    {
        return state;
    }

    /**
     * Returns the entity that this state machine manages.
     */
    public ET getEntity()
    {
        return entity;
    }

    /**
     * Returns true if the finite state machine is in an end state.
     */
    public boolean isEndState()
    {
        return state.isEnd();
    }

    /**
     * @return the forwardChainEnabled
     */
    public boolean isForwardChainEnabled()
    {
        return forwardChainEnabled;
    }

    /**
     * @param forwardChainEnabled the forwardChainEnabled to set
     */
    public void setForwardChainEnabled(boolean forwardChainEnabled)
    {
        this.forwardChainEnabled = forwardChainEnabled;
    }

    /**
     * Returns the error state of this state machine, if defined, or null.
     */
    public State<ET> getErrorState()
    {
        return this.map.getErrorState();
    }

    /**
     * Creates a latch on a state in the state machine.
     */
    public StateTransitionLatch<ET> createStateTransitionLatch(State<ET> expected,
            boolean exitOnError)
    {
        return new StateTransitionLatch<ET>(this, expected, exitOnError);
    }
}
