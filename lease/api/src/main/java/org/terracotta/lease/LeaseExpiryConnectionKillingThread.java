/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terracotta.lease;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.connection.Connection;

import java.io.IOException;

public class LeaseExpiryConnectionKillingThread extends Thread {
  private static Logger LOGGER = LoggerFactory.getLogger(LeaseExpiryConnectionKillingThread.class);

  private final LeaseMaintainer leaseMaintainer;
  private final Connection connection;
  private final TimeSource timeSource;

  LeaseExpiryConnectionKillingThread(LeaseMaintainer leaseMaintainer, Connection connection) {
    this.leaseMaintainer = leaseMaintainer;
    this.connection = connection;
    this.timeSource = TimeSourceProvider.getTimeSource();
    setName("LeaseExpiryConnectionKillingThread");
    setDaemon(true);
  }

  public void run() {
    while (true) {
      try {
        Lease lease = leaseMaintainer.getCurrentLease();

        if (!(lease instanceof NullLease)) {
          boolean validLease = lease.isValidAndContiguous(lease);
          if (!validLease) {
            connection.close();
          }
        }

        timeSource.sleep(200L);
      } catch (IOException e) {
        LOGGER.error("Closing connection, due to lease expiry, caused an error", e);
      } catch (InterruptedException e) {
        Thread.interrupted();
        break;
      }
    }
  }
}
