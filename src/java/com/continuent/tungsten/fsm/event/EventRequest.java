/**
 * Tungsten Finite State Machine Library (FSM)
 * Copyright (C) 2007-2008 Continuent Inc.
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
 */

package com.continuent.tungsten.fsm.event;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.continuent.tungsten.fsm.core.Event;

/**
 * Defines an event request, which contains the event to be processed as well as
 * the status of it. This class implements the Future interface so that it can
 * be returned to clients that track status of events.
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class EventRequest implements Future<EventStatus>
{
    private final EventDispatcher dispatcher;
    private final Event           event;
    private boolean               cancelRequested = false;
    private boolean               started         = false;
    private EventStatus           status;
    private Object                annotation;

    /**
     * Instantiates request for this event.
     * 
     * @param dispatcher Event dispatcher handling the event
     * @param request Pending request
     */
    EventRequest(EventDispatcher dispatcher, Event event)
    {
        this.dispatcher = dispatcher;
        this.event = event;
    }

    public synchronized Event getEvent()
    {
        return event;
    }

    /**
     * Adds a client annotation to this event request.
     */
    public synchronized void setAnnotation(Object annotation)
    {
        this.annotation = annotation;
    }

    /**
     * Returns the client annotation or null if no annotation has been added.
     */
    public synchronized Object getAnnotation()
    {
        return annotation;
    }

    /**
     * Marks the event as having started processing.
     */
    public synchronized void started()
    {
        started = true;
    }

    /**
     * Sets the status on a processed event and notifies anyone waiting for
     * status to arrive.
     */
    public synchronized void setStatus(EventStatus status)
    {
        this.status = status;
        this.notifyAll();
    }

    /**
     * Cancels the event if still pending, returning true if cancellation is
     * successful.
     * 
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    public synchronized boolean cancel(boolean mayInterruptIfRunning)
    {
        // Perform cancellation based on where we are.
        if (!started)
        {
            // If we have not started, just mark ourselves for cancellation.
            this.cancelRequested = true;
            return true;
        }
        else if (isDone())
        {
            // Cannot cancel after we are finished.
            return false;
        }
        else
        {
            // We are not done and not running, so try to cancel.
            try
            {
                return dispatcher.cancelActive(this, mayInterruptIfRunning);
            }
            catch (InterruptedException e)
            {
                // Show that we were interrupted. This seems kind of unlikely.
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    /**
     * Returns the event status, waiting indefinitely if necessary until it
     * completes.
     * 
     * @see java.util.concurrent.Future#get()
     */
    public synchronized EventStatus get() throws InterruptedException,
            ExecutionException
    {
        while (!isDone())
        {
            wait();
        }
        return status;
    }

    /**
     * Returns the event status, waiting up to a timeout for completion.
     * 
     * @throws TimeoutException Thrown if the wait times out
     * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
     */
    public synchronized EventStatus get(long timeout, TimeUnit unit)
            throws InterruptedException, TimeoutException
    {
        // Try to wait on status.
        if (!isDone())
        {
            // Normalize time units to millis. To prevent nanoseconds and
            // microseconds from being cleared to 0, make sure that any
            // non-zero value results in 1ms. Java changed its mind
            // about time units in a kind of messy way, hence this code.
            long timeoutMillis = convertTimeToMillis(timeout, unit);
            if (timeout > 0 && timeoutMillis == 0)
                timeoutMillis = 1;
            this.wait(timeoutMillis);
        }

        // If we finished, return status; otherwise signal a timeout.
        if (isDone())
            return status;
        else
            throw new TimeoutException();
    }

    /**
     * Returns true if cancel was requested. It may not have been processed yet.
     */
    public synchronized boolean isCancelRequested()
    {
        return cancelRequested;
    }

    /**
     * Returns true if the event was processed and was cancelled.
     * 
     * @see java.util.concurrent.Future#isCancelled()
     */
    public synchronized boolean isCancelled()
    {
        return (status != null && status.isCancelled());
    }

    /**
     * Returns true if event is complete.
     * 
     * @see java.util.concurrent.Future#isDone()
     */
    public synchronized boolean isDone()
    {
        return (status != null);
    }

    // Converts time to milliseconds.
    public long convertTimeToMillis(long time, TimeUnit unit)
    {
        switch (unit)
        {
            case NANOSECONDS :
                return time / (1000 * 1000);
            case MICROSECONDS :
                return time / 1000;
            case MILLISECONDS :
                return time;
            case SECONDS :
                return time * 1000;
            case MINUTES :
                return time * 1000 * 60;
            case HOURS :
                return time * 1000 * 60 * 60;
            case DAYS :
                return time * 1000 * 60 * 60 * 24;
            default :
                return time;
        }
    }
}