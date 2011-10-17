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
 * Denotes a completely failed transition. Actions may throw this exception to
 * indicate that the state machine should move to the default error state, if
 * designated. It is an error to throw this exception in a state machine that
 * does not have a default error state. Any error handling is fully encapsulated
 * within the action and is complete at the time this exception is thrown.
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public final class TransitionFailureException extends FiniteStateException
{
    private static final long serialVersionUID = 1L;
    private final Event event;
    private final Entity entity; 
    private final Transition transition;
    private final int actionType;

    /**
     * Creates a transition failure exception.  All fields must be filled out. 
     */
   public TransitionFailureException(String message, Event event,
            Entity entity, Transition transition, int actionType, Throwable t)
    {
        super(message, t);
        this.event = event;
        this.entity = entity;
        this.transition = transition;
        this.actionType = actionType;
    }

    public Event getEvent()
    {
        return event;
    }

    public Entity getEntity()
    {
        return entity;
    }

    public Transition getTransition()
    {
        return transition;
    }

    public int getActionType()
    {
        return actionType;
    }
}