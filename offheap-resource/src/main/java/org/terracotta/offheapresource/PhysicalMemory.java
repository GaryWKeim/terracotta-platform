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
package org.terracotta.offheapresource;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cdennis
 */
class PhysicalMemory {

  private static final Logger LOGGER = LoggerFactory.getLogger(PhysicalMemory.class);
  private static final OperatingSystemMXBean OS_BEAN = ManagementFactory.getOperatingSystemMXBean();

  public static Long totalPhysicalMemory() {
    return getAttribute("getTotalPhysicalMemorySize");
  }

  public static Long freePhysicalMemory() {
    return getAttribute("getFreePhysicalMemorySize");
  }

  public static Long totalSwapSpace() {
    return getAttribute("getTotalSwapSpaceSize");
  }

  public static Long freeSwapSpace() {
    return getAttribute("getFreeSwapSpaceSize");
  }

  public static Long ourCommittedVirtualMemory() {
    return getAttribute("getCommittedVirtualMemorySize");
  }

  @SuppressWarnings("unchecked")
  private static <T> T getAttribute(String name) {
    LOGGER.trace("Bean lookup for {}", name);
    for (Class<?> s = OS_BEAN.getClass(); s != null; s = s.getSuperclass()) {
      try {
        T result = (T) s.getMethod(name).invoke(OS_BEAN);
        LOGGER.trace("Bean lookup successful using {}, got {}", s, result);
        return result;
      } catch (SecurityException e) {
        LOGGER.trace("Bean lookup failed on {}", s, e);
      } catch (NoSuchMethodException e) {
        LOGGER.trace("Bean lookup failed on {}", s, e);
      } catch (IllegalAccessException e) {
        LOGGER.trace("Bean lookup failed on {}", s, e);
      } catch (IllegalArgumentException e) {
        LOGGER.trace("Bean lookup failed on {}", s, e);
      } catch (InvocationTargetException e) {
        LOGGER.trace("Bean lookup failed on {}", s, e);
      }
    }
    for (Class<?> i : OS_BEAN.getClass().getInterfaces()) {
      try {
        T result = (T) i.getMethod(name).invoke(OS_BEAN);
        LOGGER.trace("Bean lookup successful using {}, got {}", i, result);
        return result;
      } catch (SecurityException e) {
        LOGGER.trace("Bean lookup failed on {}", i, e);
      } catch (NoSuchMethodException e) {
        LOGGER.trace("Bean lookup failed on {}", i, e);
      } catch (IllegalAccessException e) {
        LOGGER.trace("Bean lookup failed on {}", i, e);
      } catch (IllegalArgumentException e) {
        LOGGER.trace("Bean lookup failed on {}", i, e);
      } catch (InvocationTargetException e) {
        LOGGER.trace("Bean lookup failed on {}", i, e);
      }
    }
    LOGGER.trace("Returning null for {}", name);
    return null;
  }
}
