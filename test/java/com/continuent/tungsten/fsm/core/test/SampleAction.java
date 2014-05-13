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

package com.continuent.tungsten.fsm.core.test;

import com.continuent.tungsten.fsm.core.Action;
import com.continuent.tungsten.fsm.core.Entity;
import com.continuent.tungsten.fsm.core.Event;
import com.continuent.tungsten.fsm.core.Transition;
import com.continuent.tungsten.fsm.core.TransitionFailureException;
import com.continuent.tungsten.fsm.core.TransitionRollbackException;

/**
 * Sample class to test different kinds of action outcomes.
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class SampleAction implements Action
{
    public enum Outcome
    {
        SUCCEED, ROLLBACK, FAILURE, BUG, ILLEGAL
    };

    private Outcome outcome = Outcome.SUCCEED;
    private int     wait    = 0;
    private int     count   = 0;

    // Set the number of seconds to wait before proceeding to outcome.
    public void setWaitSeconds(int wait)
    {
        this.wait = wait;
    }

    // Set the outcome.
    public void setSucceed()
    {
        outcome = Outcome.SUCCEED;
    }

    public void setRollback()
    {
        outcome = Outcome.ROLLBACK;
    }

    public void setFailure()
    {
        outcome = Outcome.FAILURE;
    }

    public void setBug()
    {
        outcome = Outcome.BUG;
    }

    public void setIllegal()
    {
        outcome = Outcome.ILLEGAL;
    }

    public void setOutcome(Outcome outcome)
    {
        this.outcome = outcome;
    }

    public int getCount()
    {
        return count;
    }

    public void clearCount()
    {
        count = 0;
    }

    public void doAction(Event event, Entity entity, Transition transition,
            int actionType) throws TransitionRollbackException,
            TransitionFailureException, InterruptedException
    {
        // If we have a wait of more than 0, delay for that many seconds.
        if (wait > 0)
        {
            Thread.sleep(wait * 1000L);
        }

        // Process the outcome.
        if (outcome == Outcome.SUCCEED)
        {
            count++;
            return;
        }
        else if (outcome == Outcome.ROLLBACK)
            throw new TransitionRollbackException("rollback", event, entity,
                    transition, actionType, null);
        else if (outcome == Outcome.FAILURE)
            throw new TransitionFailureException("rollback", event, entity,
                    transition, actionType, null);
        else if (outcome == Outcome.BUG)
            throw new RuntimeException("fail");
        else if (outcome == Outcome.ILLEGAL)
            throw new RuntimeException("Illegal action call! Entity=" + entity
                    + " Transition=" + transition);
    }
}
