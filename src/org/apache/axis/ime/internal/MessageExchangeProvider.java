/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.axis.ime.internal;

import org.apache.axis.i18n.Messages;
import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.MessageContext;
import org.apache.axis.ime.MessageExchange;
import org.apache.axis.ime.MessageContextListener;
import org.apache.axis.ime.MessageExchangeCorrelator;
import org.apache.axis.ime.MessageExchangeFactory;
import org.apache.axis.ime.MessageExchangeFaultListener;
import org.apache.axis.ime.internal.util.WorkerPool;
import org.apache.axis.ime.internal.util.KeyedBuffer;
import org.apache.axis.ime.internal.util.NonPersistentKeyedBuffer;
import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;

import java.util.Map;

/**
 * @author James M Snell (jasnell@us.ibm.com)
 */
public abstract class MessageExchangeProvider
        implements MessageExchangeFactory {

    protected static Log log =
        LogFactory.getLog(MessageExchangeProvider.class.getName());

    public static final long SELECT_TIMEOUT = 1000 * 30;
    public static final long DEFAULT_THREAD_COUNT = 5;

    protected final WorkerPool WORKERS = new WorkerPool();
    protected final KeyedBuffer SEND = new NonPersistentKeyedBuffer(WORKERS);
    protected final KeyedBuffer RECEIVE = new NonPersistentKeyedBuffer(WORKERS);
    protected final KeyedBuffer RECEIVE_REQUESTS = new NonPersistentKeyedBuffer(WORKERS);

    protected boolean initialized = false;

    protected Handler getSendHandler() {
      return null;
    }
    
    protected Handler getReceiveHandler() {
      return null;
    }

    protected abstract MessageExchangeSendListener getMessageExchangeSendListener();

    protected abstract ReceivedMessageDispatchPolicy getReceivedMessageDispatchPolicy();

    public MessageExchange createMessageExchange()
            throws AxisFault {
        return new MessageExchangeImpl(this);
    }

    /**
     * Unsupported for now
     */
    public MessageExchange createMessageExchange(
            Map properties,
            String[] enabledFeatures)
            throws AxisFault {
        throw AxisFault.makeFault(
            new UnsupportedOperationException(
                Messages.getMessage("unsupportedOperationException00")));
    }
            
    public void cleanup()
            throws InterruptedException {
        if (log.isDebugEnabled()) {
            log.debug("Enter: MessageExchangeProvider::cleanup");
        }
        WORKERS.cleanup();
        if (log.isDebugEnabled()) {
            log.debug("Exit: MessageExchangeProvider::cleanup");
        }
    }  

    public void init() {
        init(DEFAULT_THREAD_COUNT);
    }

    public void init(long THREAD_COUNT) {
        if (log.isDebugEnabled()) {
            log.debug("Enter: MessageExchangeProvider::init");
        }
        if (initialized)
            throw new IllegalStateException(Messages.getMessage("illegalStateException00"));
        for (int n = 0; n < THREAD_COUNT; n++) {
            WORKERS.addWorker(new MessageSender(WORKERS, SEND, getMessageExchangeSendListener(), getSendHandler()));
            WORKERS.addWorker(new MessageReceiver(WORKERS, RECEIVE, getReceivedMessageDispatchPolicy(), getReceiveHandler()));
        }
        initialized = true;
        if (log.isDebugEnabled()) {
            log.debug("Exit: MessageExchangeProvider::init");
        }
    }
    
    public void processReceive(
            MessageExchangeReceiveContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Enter: MessageExchangeProvider::processReceive");
        }
        RECEIVE_REQUESTS.put(
            context.getMessageExchangeCorrelator(),
            context);
        if (log.isDebugEnabled()) {
            log.debug("Exit: MessageExchangeProvider::processReceive");
        }
    }
    
    public void processSend(
            MessageExchangeSendContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Enter: MessageExchangeProvider::processSend");
        }
        SEND.put(
            context.getMessageExchangeCorrelator(),
            context);
        if (log.isDebugEnabled()) {
            log.debug("Exit: MessageExchangeProvider::processSend");
        }
    }

    public void shutdown() {
        shutdown(false);
    }

    public void shutdown(boolean force) {
        if (log.isDebugEnabled()) {
            log.debug("Enter: MessageExchangeProvider::shutdown");
        }
        if (!force) {
            WORKERS.safeShutdown();
        } else {
            WORKERS.shutdown();
        }
        if (log.isDebugEnabled()) {
            log.debug("Exit: MessageExchangeProvider::shutdown");
        }
    }

    public void awaitShutdown()
            throws InterruptedException {
        if (log.isDebugEnabled()) {
            log.debug("Enter: MessageExchangeProvider::awaitShutdown");
        }
        WORKERS.awaitShutdown();
        if (log.isDebugEnabled()) {
            log.debug("Exit: MessageExchangeProvider::awaitShutdown");
        }
    }

    public void awaitShutdown(long shutdown)
            throws InterruptedException {
        if (log.isDebugEnabled()) {
            log.debug("Enter: MessageExchangeProvider::awaitShutdown");
        }
        WORKERS.awaitShutdown(shutdown);
        if (log.isDebugEnabled()) {
            log.debug("Exit: MessageExchangeProvider::awaitShutdown");
        }
    }



  // -- Worker Classes --- //
    public static class MessageReceiver 
            implements Runnable {
        
        protected static Log log =
            LogFactory.getLog(MessageReceiver.class.getName());
        
        protected WorkerPool pool;
        protected KeyedBuffer channel;
        protected ReceivedMessageDispatchPolicy policy;
        protected Handler handler;
    
        protected MessageReceiver(
                WorkerPool pool,
                KeyedBuffer channel,
                ReceivedMessageDispatchPolicy policy,
                Handler handler) {
            this.pool = pool;
            this.channel = channel;
            this.policy = policy;
            this.handler = handler;
        }
    
        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Enter: MessageExchangeProvider.MessageReceiver::run");
            }
            try {
                while (!pool.isShuttingDown()) {
                    MessageExchangeSendContext context = (MessageExchangeSendContext)channel.select(SELECT_TIMEOUT);
                    if (context != null) {
                      if (handler != null)
                        handler.invoke(context.getMessageContext());
                      policy.dispatch(context);
                    }
                }
            } catch (Throwable t) {
                log.error(Messages.getMessage("fault00"), t);
            } finally {
                pool.workerDone(this);
                if (log.isDebugEnabled()) {
                    log.debug("Exit: MessageExchangeProvider.MesageReceiver::run");
                }
            }
        }
    
    }



    public static class MessageSender 
            implements Runnable {

        protected static Log log =
            LogFactory.getLog(MessageReceiver.class.getName());
    
        protected WorkerPool pool;
        protected KeyedBuffer channel;
        protected MessageExchangeSendListener listener;
        protected Handler handler;
    
        protected MessageSender(
                WorkerPool pool,
                KeyedBuffer channel,
                MessageExchangeSendListener listener,
                Handler handler) {
            this.pool = pool;
            this.channel = channel;
            this.listener = listener;
            this.handler = handler;
        }
        
        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Enter: MessageExchangeProvider.MessageSender::run");
            }
            try {
                while (!pool.isShuttingDown()) {
                    MessageExchangeSendContext context = (MessageExchangeSendContext)channel.select(SELECT_TIMEOUT);
                    if (context != null) {
                      if (handler != null)
                        handler.invoke(context.getMessageContext());
                      listener.onSend(context);
                    }
                }
            } catch (Throwable t) {
                log.error(Messages.getMessage("fault00"), t);
            } finally {
                pool.workerDone(this);
                if (log.isDebugEnabled()) {
                    log.debug("Exit: MessageExchangeProvider.MessageSender::run");
                }
            }
        }
    
    }

}
