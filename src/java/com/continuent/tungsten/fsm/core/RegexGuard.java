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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines a guard that accepts an event if its object is a string that matches
 * the regular expression supplied with the guard.
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class RegexGuard implements Guard
{
    Pattern pattern;

    /**
     * Creates a new <code>RegexGuard</code> object
     * 
     * @param regex A regex expression
     */
    public RegexGuard(String regex)
    {
        pattern = Pattern.compile(regex);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.continuent.tungsten.fsm.core.Guard#accept(com.continuent.tungsten.fsm.core.Event,
     *      com.continuent.tungsten.fsm.core.Entity,
     *      com.continuent.tungsten.fsm.core.State)
     */
    public <EventType> boolean accept(Event<EventType> message, Entity entity, State state)
    {
        EventType o = message.getData();
        if (o != null && o instanceof String)
        {
            Matcher m = pattern.matcher((String) o);
            return m.matches();
        }
        else
            return false;
    }
}
