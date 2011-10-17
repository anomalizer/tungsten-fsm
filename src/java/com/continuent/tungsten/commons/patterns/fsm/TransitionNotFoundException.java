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
 * Denotes an exception due to a missing transition to handle a particular
 * event in a particular state.  Clients can use this error to invoke 
 * optional processing, such as printing a warning or ignoring missing 
 * transitions. 
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class TransitionNotFoundException extends FiniteStateException
{
    private static final long serialVersionUID = 1L;
    
    private final State state;
    private final Event event;
    private final Entity entity;

    /**
     * Creates a new <code>TransitionNotFoundException</code> object
     * 
     * @param message A message describing the failure. 
     * @param state The state for which no transition was found 
     * @param event The event for which no transition was found 
     * @param entity The entity whose state is being managed
     */
    public TransitionNotFoundException(String message, State state, Event event, 
            Entity entity)
    {
        super(message);
        this.state = state;
        this.event = event; 
        this.entity = entity;
    }

    public State getState()
    {
        return state;
    }

    public Event getEvent()
    {
        return event;
    }

    public Entity getEntity()
    {
        return entity;
    }
}