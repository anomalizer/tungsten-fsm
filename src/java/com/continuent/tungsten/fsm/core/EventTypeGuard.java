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

/**
 * Defines a guard that accepts an event if it is an instance of the type
 * supplied when this guard is created.  
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class EventTypeGuard implements Guard
{
    private final Class<?> type;

    public EventTypeGuard(Class<?> type)
    {
        this.type = type;
    }
    
    /**
     * Returns true if the event is an instance of type.  Note we check the 
     * type of the Event, *not* its data.  
     * 
     * {@inheritDoc}
     * @see com.continuent.tungsten.fsm.core.Guard#accept(com.continuent.tungsten.fsm.core.Event, com.continuent.tungsten.fsm.core.Entity, com.continuent.tungsten.fsm.core.State)
     */
    public boolean accept(Event<?> message, Entity entity, State state)
    {
        return (type.isInstance(message));
    }
}
