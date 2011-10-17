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
     * @see com.continuent.tungsten.commons.patterns.fsm.Guard#accept(com.continuent.tungsten.commons.patterns.fsm.Event,
     *      com.continuent.tungsten.commons.patterns.fsm.Entity,
     *      com.continuent.tungsten.commons.patterns.fsm.State)
     */
    public boolean accept(Event message, Entity entity, State state)
    {
        Object o = message.getData();
        if (o != null && o instanceof String)
        {
            Matcher m = pattern.matcher((String) o);
            return m.matches();
        }
        else
            return false;
    }
}
