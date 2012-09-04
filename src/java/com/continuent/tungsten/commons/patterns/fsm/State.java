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
public class State<ET extends Entity>
{
    private final String    name;
    private final StateType type;
    private final State<ET>     parent;
    private final Action<ET>    entryAction;
    private final Action<ET>    exitAction;

    private final String    qualifiedName;
    private final State<ET>[]   hierarchy;
    private List<State<ET>>     children = new ArrayList<State<ET>>();

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
    public State(String name, StateType type, State<ET> parent, Action<ET> entryAction,
            Action<ET> exitAction)
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
            @SuppressWarnings("unchecked") final State<ET>[] tmp = new State[]{this};
            this.hierarchy = tmp;
        }
        else
        {
            this.qualifiedName = parent.getName() + ":" + name;
            State<ET>[] parentArray = parent.getHierarchy();
            @SuppressWarnings("unchecked") State<ET>[] selfArray = new State[parentArray.length + 1];
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
     * Utility method for creating a new state without a parent.
     */
    public State(String name, StateType type, Action<ET> entryAction,
            Action<ET> exitAction)
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
     * Utility method for creating a new state without actions.
     */
    public State(String name, StateType type, State<ET> parent)
    {
        this(name, type, parent, null, null);
    }

    /**
     * Adds a child to this state. This is package-protected as it should only
     * be done by the state transition map.
     * 
     * @param state Child state
     */
    void addChild(State<ET> state)
    {
        children.add(state);
    }

    /**
     * Returns an immutable list of the children of this state. The list is
     * empty if there are no children.
     */
    public List<State<ET>> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    /**
     * Returns the entry action or null if there is none.
     */
    public Action<ET> getEntryAction()
    {
        return entryAction;
    }

    /**
     * Returns the exit action or null if there is none.
     */
    public Action<ET> getExitAction()
    {
        return exitAction;
    }

    /**
     * Returns fully qualified name of the state including any parent states.
     */
    public final String getName()
    {
        return qualifiedName;
    }

    /**
     * Returns the name of this state without any parent names.
     */
    public final String getBaseName()
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
    public State<ET> getParent()
    {
        return parent;
    }

    /**
     * s Returns the state hierarchy from the highest enclosing state, if any,
     * down to this state.
     */
    public State<ET>[] getHierarchy()
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
    public State<ET> getLeastCommonParent(State<ET> other)
    {
        State<ET> least = null;
        State<ET>[] otherHierarchy = other.getHierarchy();
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
        return "State: " + getName();
    }
    
    /**
     * Returns true if state names match. 
     */
    public boolean equals(Object o)
    {
        if (o != null && o instanceof State)
        {
            String otherName = ((State) o).getName();
            if (qualifiedName == null)
                return qualifiedName == otherName;
            else
                return qualifiedName.equals(otherName);
        }
        else
            return false;
    }
}