/**
 * Tungsten Finite State Machine Library (FSM)
 * Copyright (C) 2007-2009 Continuent Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
 *
 * Initial developer(s): Robert Hodges
 * Contributor(s):
 */

package com.continuent.tungsten.fsm.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages the map of states and transitions composing a finite state machine.
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class StateTransitionMap
{
    private State                         startState;
    private State                         errorState;
    private Map<State, TransitionMatcher> transitionMap = new HashMap<State, TransitionMatcher>();
    private boolean                       initialized;

    /** Creates a new instance. */
    public StateTransitionMap()
    {
    }

    /**
     * Returns the starting state of this state machine.
     */
    public State getStartState()
    {
        return startState;
    }

    /**
     * Designates a default state to which control transfers if a transition
     * fails with a {@link TransitionFailureException}.
     * 
     * @param state An error state, which must already have been added to the
     *            map
     * @throws FiniteStateException Thrown if the state cannot be found in the
     *             map
     */
    public void setErrorState(State state) throws FiniteStateException
    {
        if (transitionMap.get(state) == null)
            throw new FiniteStateException(
                    "Unknown state--error states must be the state transition map: "
                            + state);

        errorState = state;
    }

    /**
     * Returns the error state or null if no error state has been designated.
     */
    public State getErrorState()
    {
        return errorState;
    }

    /**
     * Adds a new state to the state diagram.
     * 
     * @param state A uniquely named state
     * @return The state that was just added
     * @throws FiniteStateException
     */
    public State addState(State state) throws FiniteStateException
    {
        // Check for error conditions.
        if (transitionMap.get(state) != null)
            throw new FiniteStateException("State is already present in map: "
                    + state);

        if (state.isStart() && startState != null)
        {
            throw new FiniteStateException(
                    "Attempt to add initial state when initial state already exists: old="
                            + startState.getName() + " new=" + state.getName());
        }

        // Update the map.
        transitionMap.put(state, new TransitionMatcher());
        if (state.isStart())
        {
            this.startState = state;
        }

        return state;
    }

    /**
     * Adds a new state to the state diagram.
     * 
     * @param name A name that is unique within any state machine that contains
     *            this state
     * @param type A state type as defined by {@link StateType}
     * @param parent A parent state that contains this state if any
     * @param entryAction An action to perform on entering the state
     * @param exitAction An action to perform on leaving the state
     */
    public State addState(String name, StateType type, State parent,
            Action entryAction, Action exitAction) throws FiniteStateException
    {
        return addState(new State(name, type, parent, entryAction, exitAction));
    }

    /**
     * Adds a new state to the state diagram.
     * 
     * @param name A name that is unique within any state machine that contains
     *            this state
     * @param type A state type as defined by {@link StateType}
     * @param parent A parent state that contains this state if any
     */
    public State addState(String name, StateType type, State parent)
            throws FiniteStateException
    {
        return addState(new State(name, type, parent, null, null));
    }

    /**
     * Returns a state definition from the map or null if the state cannot be
     * found.
     * 
     * @param name The fully qualified state name
     */
    public State getStateByName(String name)
    {
        for (State state : this.transitionMap.keySet())
        {
            if (state.getName().equals(name))
                return state;
        }
        return null;
    }

    /**
     * Adds a transition to the state machine.
     * 
     * @param transition A transition between two states containin a guard
     * @throws FiniteStateException Thrown if the transition is invalid
     */
    public Transition addTransition(Transition transition)
            throws FiniteStateException
    {
        // Check for errors.
        TransitionMatcher matcher = transitionMap.get(transition.getInput());
        if (matcher == null)
            throw new FiniteStateException(
                    "Cannot find input state for transition: "
                            + transition.getName());
        if (transitionMap.get(transition.getOutput()) == null)
            throw new FiniteStateException(
                    "Cannot find output state for transition: "
                            + transition.getName());

        matcher.addTransition(transition);
        return transition;
    }

    /**
     * Adds a new transition to the map.
     * 
     * @param name Transition name
     * @param guard Condition guarding the transition
     * @param input Input state
     * @param action An action to take when the transition is triggered
     * @param output Output state
     */
    public Transition addTransition(String name, Guard guard, State input,
            Action action, State output) throws FiniteStateException
    {
        return addTransition(new Transition(name, guard, input, action, output));
    }

    /**
     * Adds a group of transitions to the map that share the same guard and
     * action and that have the same input and output state. The effect of
     * having such a group is to be able to specify an 'event' that fires, via
     * the guard, but that can only 'fire' when the state machine is in one of
     * the states specified. For transitions that are added via this means, the
     * output state is always the same as the input state.
     * 
     * @param name Transition name
     * @param guard Condition guarding the transition
     * @param states States in which the transition MAY be taken
     * @param action An action to take when the transition is triggered
     * @throws FiniteStateException
     */
    public void addTransitionGroup(String name, Guard guard, State[] states,
            Action action) throws FiniteStateException
    {
        for (State state : states)
        {
            String transitionName = String.format("%s:%s", name, state
                    .getName());
            addTransition(transitionName, guard, state, action, state);
        }
    }
    
    /**
     * Adds a group of transitions to the map that share the same guard and
     * action and that have the same input and output state. The effect of
     * having such a group is to be able to specify an 'event' that fires, via
     * the guard, but that can only 'fire' when the state machine is in one of
     * the states specified. For transitions that are added via this means, the
     * output state is always the same as the input state.
     * 
     * @param name Transition name
     * @param guard Condition guarding the transition
     * @param states States in which the transition MAY be taken
     * @param action An action to take when the transition is triggered
     * @throws FiniteStateException
     */
    public void addTransitionGroup(String name, Guard guard, Set<State> states,
            Action action) throws FiniteStateException
    {
        for (State state : states)
        {
            String transitionName = String.format("%s:%s", name, state
                    .getName());
            addTransition(transitionName, guard, state, action, state);
        }
    }

    /**
     * Adds a new transition to the map with a generated regex guard.
     * 
     * @param name Transition name
     * @param regex Regex expression to create RegexGuard
     * @param input Input state
     * @param action An action to take when the transition is triggered
     * @param output Output state
     */
    public Transition addTransition(String name, String regex, State input,
            Action action, State output) throws FiniteStateException
    {
        return addTransition(new Transition(name, new RegexGuard(regex), input,
                action, output));
    }

    /**
     * Adds a new transition to the map with a generated event type guard.
     * 
     * @param name Transition name
     * @param eventType Class of event type for EventTypeGuard
     * @param input Input state
     * @param action An action to take when the transition is triggered
     * @param output Output state
     */
    public Transition addTransition(String name, Class<?> eventType,
            State input, Action action, State output)
            throws FiniteStateException
    {
        return addTransition(new Transition(name,
                new EventTypeGuard(eventType), input, action, output));
    }

    /**
     * Validates the state machine for use. This must be called prior to any use
     * of the state machine.
     * 
     * @throws FiniteStateException
     */
    public void build() throws FiniteStateException
    {
        // Check to ensure state graph is not empty.
        if (this.transitionMap.size() == 0)
            throw new FiniteStateException("State machine contains no states");

        // Ensure we have a starting state.
        if (this.startState == null)
            throw new FiniteStateException("State machine has no start state");

        // Ensure that we have at least one ending state.
        boolean foundEnd = false;
        for (State state : transitionMap.keySet())
        {
            if (state.isEnd())
            {
                foundEnd = true;
                break;
            }
        }
        if (!foundEnd)
            throw new FiniteStateException("State machine has no end state(s)");

        // Ensure that every non-starting state other than the error state has
        // at least one transition either directly into it or to a substate.
        HashMap<State, Transition> inBoundTransitions = new HashMap<State, Transition>();
        for (TransitionMatcher matcher : transitionMap.values())
        {
            for (Transition transition : matcher.getTransitions())
            {
                // Assign transitions to original state and all parents.
                State out = transition.getOutput();
                while (out != null)
                {
                    inBoundTransitions.put(out, transition);
                    // Careful here--parents only get to count transitions from
                    // states that are not substates of themselves.
                    if (transition.getInput().isSubstateOf(out.getParent()))
                        out = null;
                    else
                        out = out.getParent();
                }
            }
        }

        for (State state : transitionMap.keySet())
        {
            if (!state.isStart() && inBoundTransitions.get(state) == null
                    && state != errorState)
            {
                throw new FiniteStateException(
                        "State has no inbound transitions, hence is unreachable: "
                                + state.getName());
            }
        }

        // Outbound #1: Ensure that every non-ending state has at least one
        // transition out of it or out of a substate.
        HashMap<State, Transition> outBoundTransitions = new HashMap<State, Transition>();
        for (TransitionMatcher matcher : transitionMap.values())
        {
            for (Transition transition : matcher.getTransitions())
            {
                // Assign transitions to original state and all parents.
                State in = transition.getInput();
                while (in != null)
                {
                    outBoundTransitions.put(in, transition);
                    // Careful here as well--parents only get to count
                    // transitions to states that are not substates of
                    // themselves.
                    if (transition.getOutput().isSubstateOf(in.getParent()))
                        in = null;
                    else
                        in = in.getParent();
                }
            }
        }

        // Outbound #2: Add non-ending states whose parents have outbound
        // states.
        for (State state : transitionMap.keySet())
        {
            if (!state.isEnd() && outBoundTransitions.get(state) == null)
            {
                State parent = state.getParent();
                while (parent != null)
                {
                    Transition parentTransition = outBoundTransitions
                            .get(parent);
                    if (parentTransition == null)
                        parent = parent.getParent();
                    else
                    {
                        outBoundTransitions.put(state, parentTransition);
                        break;
                    }
                }
            }
        }

        // Check for dead ends.
        for (State state : transitionMap.keySet())
        {
            if (!state.isEnd() && outBoundTransitions.get(state) == null)
            {
                throw new FiniteStateException(
                        "State has no outbound transitions, hence is dead-end: "
                                + state.getName());
            }
        }

        // Now ready for use!
        initialized = true;
    }

    /**
     * Computes the next transition given a particular input state and a message
     * delivered to that state.
     * 
     * @param inputState
     * @param event
     * @param entity
     * @return Next transition matching the event
     * @throws TransitionNotFoundException Thrown if no transition matches or if
     *             the map has not been properly initialized by a call to
     *             {@link #build()}
     */
    public <EventType> Transition nextTransition(State inputState, Event<EventType> event,
            Entity entity) throws FiniteStateException
    {
        if (!initialized)
            throw new FiniteStateException(
                    "State map not yet initialized through call to build() method");

        State matchingState = inputState;
        boolean noMatcher = true;
        Transition transition = null;

        // Walk the state hierarchy looking for a transition that accepts this
        // event.
        while (matchingState != null)
        {
            TransitionMatcher matcher = transitionMap.get(matchingState);
            if (matcher != null)
            {
                noMatcher = false;
                transition = matcher.matchTransition(event, entity);
                if (transition != null)
                    break;
            }
            matchingState = matchingState.getParent();
        }

        // Could not find any transitions from this state.
        if (noMatcher)
        {
            throw new TransitionNotFoundException(
                    "No exit transitions from state", inputState, event, entity);
        }

        if (transition == null)
        {
            throw new TransitionNotFoundException(
                    "No matching exit transition found", inputState, event,
                    entity);
        }
        return transition;
    }

    /**
     * Computes the next transition that may be chained to a previous transition
     * as the result of changes which cause a given guard's accept to fire. In
     * this case, the transition is not initiated by an event, but by the
     * condition itself. This is, in essence, a chaining operation.
     * 
     * @param inputState
     * @param entity
     * @return Next transition whose guard's accept fires.
     * @throws TransitionNotFoundException Thrown if no transition matches or if
     *             the map has not been properly initialized by a call to
     *             {@link #build()}
     */
    public <EventType> Transition nextChainedTransition(State inputState, Event<EventType> event,
            Entity entity) throws FiniteStateException
    {
        if (!initialized)
            throw new FiniteStateException(
                    "State map not yet initialized through call to build() method");

        State matchingState = inputState;
        boolean noMatcher = true;
        Transition transition = null;

        // Walk the state hierarchy looking for a transition that accepts this
        // event.
        while (matchingState != null)
        {
            TransitionMatcher matcher = transitionMap.get(matchingState);
            if (matcher != null)
            {
                noMatcher = false;
                transition = matcher.matchTransition(event, entity);
                if (transition != null)
                    break;
            }
            matchingState = matchingState.getParent();
        }

        // Could not find any transitions from this state.
        if (noMatcher)
        {
            throw new TransitionNotFoundException(
                    "No exit transitions from state", inputState, event, entity);
        }

        if (transition == null)
        {
            throw new TransitionNotFoundException(
                    "No matching exit transition found", inputState, event,
                    entity);
        }
        return transition;
    }

    /**
     * Gets the transition map
     * 
     * @return the current transition map
     */
    public Map<State, TransitionMatcher> getTransitionMap()
    {
        return new HashMap<State, TransitionMatcher>(transitionMap);
    }
}