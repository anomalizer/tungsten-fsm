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
     * @see com.continuent.tungsten.commons.patterns.fsm.Guard#accept(com.continuent.tungsten.commons.patterns.fsm.Event,
     *      com.continuent.tungsten.commons.patterns.fsm.Entity,
     *      com.continuent.tungsten.commons.patterns.fsm.State)
     */
    public boolean accept(Event message, Entity entity, State state)
    {
        return (!guard.accept(message, entity, state));
    }

}
