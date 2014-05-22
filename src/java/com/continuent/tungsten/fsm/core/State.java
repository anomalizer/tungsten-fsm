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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Denotes a state in a finite state machine. States are characterized by a
 * state name, a state type, an optional parent state, and entry and exit
 * actions. Sub-states are those states that have a parent state that encloses
 * them.
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
@lombok.EqualsAndHashCode(of = "qualifiedName")
public class State
{
    private final String    name;
    private final StateType type;
    private final State     parent;
    private final Action    entryAction;
    private final Action    exitAction;

    private final String    qualifiedName;
    private final State[]   hierarchy;
    private List<State>     children = new ArrayList<State>();

    /**
     * Creates a new state.
     * 
     * @param name A name that is unique within any state machine that contains
     *            this state
     * @param type A state type as defined by {@link StateType}
     * @param parent A parent state that contains this state if any
     * @param entryAction An action to perform on entering the state
     * @param exitAction An action to perform on leaving the state
     */
    public State(String name, StateType type, State parent, Action entryAction,
            Action exitAction)
    {
        this.name = name;
        this.type = type;
        this.parent = parent;
        this.entryAction = entryAction;
        this.exitAction = exitAction;

        // Compute name and enclosing state hierarchy.
        if (parent == null)
        {
            this.qualifiedName = name;
            this.hierarchy = new State[]{this};
        }
        else
        {
            this.qualifiedName = parent.getName() + ":" + name;
            State[] parentArray = parent.getHierarchy();
            State[] selfArray = new State[parentArray.length + 1];
            for (int i = 0; i < parentArray.length; i++)
                selfArray[i] = parentArray[i];
            selfArray[selfArray.length - 1] = this;
            this.hierarchy = selfArray;
        }

        if (parent == null)
        {

        }
    }

    /**
     * Utility method that allows for an enumerated type to be passed in to
     * define the name. This further allows for using enumerated types within
     * classes that use the state machine.
     * 
     * @param stateEnum
     * @param type
     * @param parent
     * @param entryAction
     * @param exitAction
     */
    public State(Enum<?> stateEnum, StateType type, State parent,
            Action entryAction, Action exitAction)
    {
        this(stateEnum.toString(), type, parent, entryAction, exitAction);
    }

    /**
     * Utility method for creating a new state without a parent.
     */
    public State(String name, StateType type, Action entryAction,
            Action exitAction)
    {
        this(name, type, null, entryAction, exitAction);
    }

    /**
     * Utility method for creating a new state without a parent or actions.
     */
    public State(String name, StateType type)
    {
        this(name, type, null, null, null);
    }

    /**
     * Utility method for creating a new state without a parent or actions.
     */
    public State(Enum<?> stateEnum, StateType type)
    {
        this(stateEnum, type, null, null, null);
    }

    /**
     * Utility method for creating a new state without actions.
     */
    public State(String name, StateType type, State parent)
    {
        this(name, type, parent, null, null);
    }

    /**
     * Adds a child to this state. This is package-protected as it should only
     * be done by the state transition map.
     * 
     * @param state Child state
     */
    void addChild(State state)
    {
        children.add(state);
    }

    /**
     * Returns an immutable list of the children of this state. The list is
     * empty if there are no children.
     */
    public List<State> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    /**
     * Returns the entry action or null if there is none.
     */
    public Action getEntryAction()
    {
        return entryAction;
    }

    /**
     * Returns the exit action or null if there is none.
     */
    public Action getExitAction()
    {
        return exitAction;
    }

    /**
     * Returns fully qualified name of the state including any parent states.
     */
    public String getName()
    {
        return qualifiedName;
    }

    /**
     * Returns the name of this state without any parent names.
     */
    public String getBaseName()
    {
        return name;
    }

    public StateType getType()
    {
        return type;
    }

    public boolean isStart()
    {
        return type == StateType.START;
    }

    public boolean isEnd()
    {
        return type == StateType.END;
    }

    /**
     * Returns the immediately enclosing state or null if this is not a
     * sub-state.
     */
    public State getParent()
    {
        return parent;
    }

    /**
     * s Returns the state hierarchy from the highest enclosing state, if any,
     * down to this state.
     */
    public State[] getHierarchy()
    {
        return hierarchy;
    }

    /**
     * Returns true if this state is enclosed by another state.
     */
    public boolean isSubstate()
    {
        return parent != null;
    }

    /**
     * Returns true if the other state encloses this one.
     */
    public boolean isSubstateOf(State other)
    {
        if (parent == null)
            return false;
        else if (parent == other)
            return true;
        else
            return parent.isSubstateOf(other);
    }

    /**
     * Returns the lowest shared parent of the other state or null if these
     * states have no common parent.
     */
    public State getLeastCommonParent(State other)
    {
        State least = null;
        State[] otherHierarchy = other.getHierarchy();
        for (int i = 0; i < hierarchy.length; i++)
        {

            if (otherHierarchy.length <= i)
                break;
            else if (hierarchy[i] == other.getHierarchy()[i])
                least = hierarchy[i];
            else
                break;
        }
        return least;
    }

    /**
     * Prints a string representation of the state including its fully qualified
     * name.
     */
    public String toString()
    {
        return getName();
    }

    /**
     * Returns true if state names match.
     */
}