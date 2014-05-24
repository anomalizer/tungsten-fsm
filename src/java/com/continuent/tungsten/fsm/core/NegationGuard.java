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
 * Defines a guard that wraps another guard and negates the result of the
 * accept() method call.
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class NegationGuard implements Guard
{
    private final Guard guard;

    /**
     * Creates a new instance.
     * 
     * @param guard Guard whose value will be negated
     */
    public NegationGuard(Guard guard)
    {
        this.guard = guard;
    }

    /**
     * Accepts the event and reverses decision of underlying guard.
     * {@inheritDoc}
     * 
     * @see com.continuent.tungsten.fsm.core.Guard#accept(com.continuent.tungsten.fsm.core.Event,
     *      com.continuent.tungsten.fsm.core.Entity,
     *      com.continuent.tungsten.fsm.core.State)
     */
    @Override
    public boolean accept(Event<?> message, Entity entity, State state)
    {
        return (!guard.accept(message, entity, state));
    }

}
