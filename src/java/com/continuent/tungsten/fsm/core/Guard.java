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
 * Denotes a class used to determine whether the conditions for a workflow
 * transition have been met.
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public interface Guard
{
    /**
     * Returns true if the message is accepted and we should take the transition
     * associated with this guard.
     * 
     * @param message A message that should be processed by this guard.
     * @param entity The entity whose state is being managed
     * @param state The current entity state
     * @return true if the message is accepted
     */
    public <EventType> boolean accept(Event<EventType> message, Entity entity, State state);
}
