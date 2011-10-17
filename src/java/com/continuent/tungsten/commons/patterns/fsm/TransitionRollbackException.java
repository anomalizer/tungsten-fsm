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
 * Denotes a failed transition that can be rolled back to the previous state. 
 * Actions may throw this exception to indicate that there are no side effects
 * and the state change may safely be rolled back.  Any error handling is
 * fully encapsulated within the action and is complete at the time this 
 * exception is thrown. 
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public final class TransitionRollbackException extends FiniteStateException
{
    private static final long serialVersionUID = 1L;
    private final Event event;
    private final Entity entity; 
    private final Transition transition;
    private final int actionType;

    /**
     * Creates a rollback exception.  All fields must be provided. 
     */
    public TransitionRollbackException(String message, Event event,
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