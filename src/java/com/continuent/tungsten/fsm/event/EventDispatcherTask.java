/**
 * Tungsten Finite State Machine Library (FSM)
 * Copyright (C) 2007-2011 Continuent Inc.
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
 * Initial developer(s): Teemu Ollakka
 * Contributor(s): Robert Hodges
 */

package com.continuent.tungsten.fsm.event;

import com.continuent.tungsten.fsm.core.Event;
import com.continuent.tungsten.fsm.core.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class defines an event dispatcher task, which is a separate thread that
 * dispatches events to a listener from a queue. It handles normal events,
 * out-of-band events, and event cancellation.
 * 
 * @author <a href="mailto:teemu.ollakka@continuent.com">Teemu Ollakka</a>
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class EventDispatcherTask implements Runnable, EventDispatcher
{
    private static Logger               logger           = LoggerFactory.getLogger(EventDispatcherTask.class);

    // Variables to define the state machine.
    private StateMachine                   stateMachine     = null;
    private Thread                         dispatcherThread = null;
    private boolean                        cancelled        = false;
    private EventRequest<?>                currentRequest   = null;
    private Future<?>                      submittedEvent   = null;
    private EventCompletionListener        listener;
    private final BlockingQueue<EventRequest<?>> notifications    = new LinkedBlockingQueue<EventRequest<?>>();

    /**
     * Instantiates a new dispatcher for events on a particular state machine.
     * 
     * @param stateMachine A stage machine on which to dispatch
     */
    public EventDispatcherTask(StateMachine stateMachine)
    {
        this.stateMachine = stateMachine;
    }

    /**
     * Set a listener for event processing completion.
     */
    public void setListener(EventCompletionListener listener)
    {
        this.listener = listener;
    }

    /**
     * Returns true if dispatcher thread is running.
     */
    public boolean isRunning()
    {
        return (dispatcherThread != null);
    }

    /**
     * Runs the thread, which continues until interrupted or cancelled is set to
     * true.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        // Allocate a thread pool to process each succeeding event.
        ExecutorService pool = Executors.newFixedThreadPool(1);
        try
        {
            while (!cancelled)
            {
                // Prepare to submit next event. We synchronize on the
                // request queue to avoid race conditions with cancellation.
                synchronized (notifications)
                {
                    // These operations must be serialized with cancellation.
                    while (notifications.isEmpty())
                        notifications.wait();
                    currentRequest = notifications.take();
                    EventProcessor eventProcessor = new EventProcessor(
                            stateMachine, currentRequest, listener);
                    submittedEvent = pool.submit(eventProcessor);
                }

                // Wait for the event to complete or be cancelled.
                try
                {
                    // Ensure the request is completed.
                    currentRequest.get();
                }
                catch (CancellationException e)
                {
                    if (logger.isDebugEnabled())
                        logger.debug(String.format(
                                "Event processing cancelled=%s",
                                currentRequest.getEvent()));
                    if (!currentRequest.isCancelled())
                    {
                        // Set status to cover possible race conditions if
                        // request thread is cancelled before it can set status.
                        currentRequest
                                .setStatus(new EventStatus(false, true, e));
                    }
                }

                // Show that we have completed processing.
                synchronized (notifications)
                {
                    // Synchronized to avoid race conditions with cancellation.
                    currentRequest = null;
                    submittedEvent = null;
                }
            }
        }
        catch (InterruptedException e)
        {
            logger.debug("Dispatcher loop terminated by InterruptedException");
        }
        catch (Throwable t)
        {
            logger.error("Dispatcher loop terminated by unexpected exception",
                    t);
        }
        logger.info("Dispatcher thread terminating");
    }

    /**
     * Puts an event in the queue for normal processing. This method returns a
     * Future that callers can call to obtain the event status. Events that
     * implement the {#link OutOfBandEvent} interface will be dispatched as if
     * the user had called {@link #putOutOfBand(Event)}.
     */
    public <EventType> EventRequest<EventType> put(Event<EventType> event) throws InterruptedException
    {
        if (event instanceof OutOfBandEvent)
            return putOutOfBand(event);
        else
            return putInternal(event);
    }

    /**
     * Cancel all pending events and put a new event in the queue for immediate
     * processing.
     */
    public <EventType> EventRequest<EventType> putOutOfBand(Event<EventType> event) throws InterruptedException
    {
        synchronized (notifications)
        {
            cancelAll();
            return putInternal(event);
        }
    }

    /**
     * Internal call to put an event into queue regardless of whether it arrived
     * as a normal or out-of-band event.
     */
    private <EventType> EventRequest<EventType> putInternal(Event<EventType> event) throws InterruptedException
    {
        synchronized (notifications)
        {
            EventRequest<EventType> request = new EventRequest<EventType>(this, event);
            notifications.put(request);
            notifications.notifyAll();
            return request;
        }
    }

    /**
     * Cancel all pending and executing events.
     */
    private void cancelAll() throws InterruptedException
    {
        synchronized (notifications)
        {
            // Cancel all pending requests.
            for (EventRequest request : notifications)
            {
                request.cancel(true);
            }

            // If there is an executing request, cancel that too.
            if (this.submittedEvent != null)
            {
                submittedEvent.cancel(true);
            }
        }
    }

    /**
     * Cancel a request that is already running.
     * 
     * @param request Request to cancel
     * @param mayInterruptIfRunning If true we can cancel running as opposed to
     *            enqueued request
     */
    public boolean cancelActive(EventRequest request,
            boolean mayInterruptIfRunning) throws InterruptedException
    {
        synchronized (notifications)
        {
            // If our request is executing and we are permitted, cancel it.
            if (currentRequest == request && mayInterruptIfRunning)
            {
                submittedEvent.cancel(true);
                return true;
            }
            else
                return false;
        }
    }

    /**
     * Start the event dispatcher, which spawns a separate thread.
     * 
     * @param name Name of the dispatcher thread
     */
    public synchronized void start(String name) throws Exception
    {
        logger.debug("Starting event dispatcher");
        if (dispatcherThread != null)
            throw new Exception("Dispatcher thread already started");
        if (name == null)
            name = this.getClass().getSimpleName();
        dispatcherThread = new Thread(this, name);
        dispatcherThread.start();
    }

    /**
     * Cancel the event dispatcher and wait for the thread to complete.
     */
    public synchronized void stop() throws InterruptedException
    {
        if (dispatcherThread == null)
            return;
        logger.info("Requesting dispatcher thread termination: name="
                + dispatcherThread.getName());
        cancelled = true;
        cancelAll();
        dispatcherThread.interrupt();
        dispatcherThread.join();
        dispatcherThread = null;
    }
}