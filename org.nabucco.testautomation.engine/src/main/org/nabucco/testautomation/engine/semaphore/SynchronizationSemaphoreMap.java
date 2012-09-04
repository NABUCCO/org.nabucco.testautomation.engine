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
package org.nabucco.testautomation.engine.semaphore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.testautomation.engine.exception.SynchronizationSemaphoreException;

/**
 * SynchronizationSemaphoreMap
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class SynchronizationSemaphoreMap {

    private final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(
            SynchronizationSemaphoreMap.class);

    private static SynchronizationSemaphoreMap instance;

    private Map<String, Semaphore> semaphoreMap;

    private SynchronizationSemaphoreMap() {
        this.semaphoreMap = Collections.synchronizedMap(new HashMap<String, Semaphore>());
    }

    /**
     * Gets an instance of SynchronizationSemaphoreMap.
     * 
     * @return the instance
     */
    public final static synchronized SynchronizationSemaphoreMap getInstance() {

        if (instance == null) {
            instance = new SynchronizationSemaphoreMap();
        }
        return instance;
    }

    /**
     * Acquires a lock for the given id. Only one single thread can acquire the lock for the id.
     * This operations blocks until the lock could be acquired. If the given timeout exceeds, an
     * SynchronizationSemaphoreException is thrown.
     * 
     * @param id
     *            the id for which the lock should be acquired
     * @param timeout
     *            the time to wait for acquiring
     * @throws SynchronizationSemaphoreException
     *             thrown, if timeout elapsed or the waiting thread is interrupted
     */
    public final void accuireLock(String id, long timeout) throws SynchronizationSemaphoreException {

        synchronized (semaphoreMap) {
            if (!this.semaphoreMap.containsKey(id)) {
                this.semaphoreMap.put(id, new Semaphore(1));
            }
        }

        try {
            Semaphore sem = this.semaphoreMap.get(id);
            logger.debug("Accuiring Semaphore for id '", id, "' at ", "" + System.currentTimeMillis(), ", timeout="
                    + timeout, " ms");

            // try to acquire lock for this id
            if (sem.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
                logger.debug("Accuired Semaphore for id '", id, "' at " + System.currentTimeMillis());
            } else {
                // false returned -> waiting time elapsed
                throw new SynchronizationSemaphoreException("Timeout of "
                        + timeout + " ms elapsed while acquiring semaphore for id '" + id + "'");
            }
        } catch (InterruptedException e) {
            String msg = "Caught an InterruptedException while waiting for lock with id '" + id + "'";
            logger.error(e, msg);
            throw new SynchronizationSemaphoreException(msg, e);
        }
    }

    /**
     * Releases the lock for the given id.
     * 
     * @param id
     *            the id for the lock
     */
    public final void releaseLock(String id) {
        Semaphore sem = this.semaphoreMap.get(id);
        sem.release();
    }
}
