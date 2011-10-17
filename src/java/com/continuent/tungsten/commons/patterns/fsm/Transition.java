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

/**
 * Defines a transition between two states with an accompanying guard that
 * determines when the transition can be applied.
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class Transition
{
	private final String name;
    private final Guard  guard;
    private final State  input;
    private final Action action;
    private final State  output;

    /**
     * Creates a new transition instance.
     * 
     * @param name Transition name
     * @param input Input state
     * @param guard Condition guarding the transition
     * @param action An action to take when the transition is triggered
     * @param output Output state
     */
    public Transition(String name, Guard guard, State input, Action action,
			State output) 
    {
		this.name = name;
		this.guard = guard;
		this.input = input;
		this.action = action;
		this.output = output;
	}
    
    /**
     * Creates a new transition instance with a default name "none". 
     */
    public Transition(Guard guard, State input, Action action,
			State output) 
    {
    	this("none", guard, input, action, output);
    }

    public String getName()
    {
    	return name;
    }

    public Guard getGuard()
    {
        return guard;
    }

    public State getInput()
    {
        return input;
    }

    public Action getAction()
    {
        return action;
    }

    public State getOutput()
    {
        return output;
    }

    /**
     * Returns true if the guard class accepts this event.
     */
    public boolean accept(Event event, Entity entity)
    {
        return guard.accept(event, entity, input);
    }
}