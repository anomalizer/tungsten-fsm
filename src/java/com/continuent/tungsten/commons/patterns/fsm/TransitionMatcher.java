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

import java.util.List;
import java.util.Vector;

/**
 * Matches transitions against a particular event.
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class TransitionMatcher
{
    Vector<Transition> transitions = new Vector<Transition>();

    public TransitionMatcher()
    {
    }

    public void addTransition(Transition transition)
    {
        transitions.add(transition);
    }

    public List<Transition> getTransitions()
    {
        return transitions;
    }

    public Transition matchTransition(Event event, Entity entity)
    {
        for (Transition transition : transitions)
        {
            if (transition.accept(event, entity))
            {
                return transition;
            }
        }
        return null;
    }
}