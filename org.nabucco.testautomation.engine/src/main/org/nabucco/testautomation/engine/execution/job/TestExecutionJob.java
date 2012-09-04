/*
 * Copyright 2012 PRODYNA AG
 *
 * Licensed under the Eclipse Public License (EPL), Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/eclipse-1.0.php or
 * http://www.nabucco.org/License.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nabucco.testautomation.engine.execution.job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.testautomation.engine.base.engine.ExecutionController;
import org.nabucco.testautomation.engine.base.exception.InterruptionException;
import org.nabucco.testautomation.engine.execution.TestExecutionEvent;
import org.nabucco.testautomation.engine.execution.TestExecutionListener;
import org.nabucco.testautomation.engine.execution.info.TestExecutionInfoFactory;
import org.nabucco.testautomation.settings.facade.datatype.engine.ExecutionStatusType;
import org.nabucco.testautomation.settings.facade.datatype.engine.TestExecutionInfo;

/**
 * TestExecutionJob
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public abstract class TestExecutionJob implements Runnable, ExecutionController {

    private final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(TestExecutionJob.class);

    protected final Lock lock = new ReentrantLock();

    private final Condition pause = lock.newCondition();

    private final List<TestExecutionListener> listener = new ArrayList<TestExecutionListener>();

    private ExecutionStatusType status = ExecutionStatusType.INITIALIZED;

    private boolean pauseRequested = false;

    private boolean interruptionRequested = false;

    private Long id;

    private Long startTime;

    private Long stopTime;

    public final long getId() {
        return id;
    }

    public final void setId(long id) {
        this.id = id;
    }

    public final ExecutionStatusType getStatus() {
        return status;
    }

    public final void addTestExecutionListener(TestExecutionListener listener) {
        lock.lock();
        try {
            this.listener.add(listener);
        } finally {
            lock.unlock();
        }
    }

    public final boolean removeTestExecutionListener(TestExecutionListener listener) {
        lock.lock();
        try {
            return this.listener.remove(listener);
        } finally {
            lock.unlock();
        }
    }

    public final void pause() {
        lock.lock();
        try {
            pauseRequested = true;
        } finally {
            lock.unlock();
        }
    }

    public final void resume() {
        lock.lock();
        try {
            pauseRequested = false;
            pause.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public final void interrupt() {
        lock.lock();
        try {
            interruptionRequested = true;
            pause.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public final boolean isPaused() {
        return pauseRequested;
    }

    public final boolean isInterrupted() {
        return interruptionRequested;
    }

    public final void tryPause() {
        lock.lock();
        try {
            status = ExecutionStatusType.PAUSED;
            pause.await();
        } catch (InterruptedException e) {
            status = ExecutionStatusType.RUNNING;
            logger.error("Unexpected interruption while Job " + id + " was sleeping");
        } finally {
            lock.unlock();
        }
    }

    public final void tryInterruption() {
        lock.lock();
        try {
            finalizeExecution();
            throw new InterruptionException();
        } finally {
            lock.unlock();
        }
    }

    public final void rejected() {
        lock.lock();
        try {
            status = ExecutionStatusType.REJECTED;
            finalizeExecution();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void sleep(Long duration) {
        lock.lock();
        try {
            pause.await(duration, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("Unexpected interruption while Job " + id + " was sleeping");
        } finally {
            lock.unlock();
        }
    }

    public TestExecutionInfo getTestExecutionInfo() {
        lock.lock();
        try {
            TestExecutionInfo info = TestExecutionInfoFactory.getInstance().createTestExecutionInfo(id, status,
                    startTime, stopTime);
            return info;
        } finally {
            lock.unlock();
        }
    }

    public final void run() {
        try {
            startTime = System.currentTimeMillis();
            status = ExecutionStatusType.RUNNING;
            prepareExecution();
            execute();
            finalizeExecution();
            executionFinished();
        } catch (InterruptionException ex) {
            logger.error("Job " + id + " interrupted");
            finalizeExecution();
            executionInterrupted();
        } catch (Exception ex) {
            logger.fatal(ex, "Unexpected error during execution of job " + id);
            executionInterrupted();
        } finally {
            stopTime = System.currentTimeMillis();
        }
    }

    protected void executionFinished() {
        TestExecutionEvent e = new TestExecutionEvent(this.id);
        status = ExecutionStatusType.FINISHED;

        for (TestExecutionListener tel : this.listener) {
            tel.testExecutionFinished(e);
        }
    }

    protected void executionInterrupted() {
        TestExecutionEvent e = new TestExecutionEvent(this.id);
        status = ExecutionStatusType.INTERRUPTED;

        for (TestExecutionListener tel : this.listener) {
            tel.testExecutionFinished(e);
        }
    }

    /**
	 * Called before execution
	 */
    protected abstract void prepareExecution();

    /**
	 * Called for execution
	 */
    protected abstract void execute() throws InterruptionException;

    /**
     * Called after execution or when job was rejected.
     */
    protected abstract void finalizeExecution();

}
