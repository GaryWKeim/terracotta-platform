/*
 * Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.
 */
package org.terracotta.dynamic_config.system_tests;

import org.junit.Before;
import org.junit.Test;
import org.terracotta.dynamic_config.cli.config_tool.ConfigTool;

import java.io.File;

import static org.hamcrest.Matchers.containsString;

@ClusterDefinition(nodesPerStripe = 2)
public class SetCommandIT extends DynamicConfigIT {

  @Before
  @Override
  public void before() throws Exception {
    super.before();
    ConfigTool.start("attach", "-d", "localhost:" + getNodePort(), "-s", "localhost:" + getNodePort(1, 2));
    assertCommandSuccessful();
  }

  /*<--Stripe-wide Tests-->*/
  @Test
  public void testStripe_level_setDataDirectory() {
    ConfigTool.start("set", "-s", "localhost:" + getNodePort(), "-c", "stripe.1.data-dirs.main=stripe1-node1-data-dir");
    assertCommandSuccessful();

    ConfigTool.start("get", "-s", "localhost:" + getNodePort(), "-c", "data-dirs");
    waitUntil(out::getLog, containsString("stripe.1.node.1.data-dirs=main:stripe1-node1-data-dir"));
    waitUntil(out::getLog, containsString("stripe.1.node.2.data-dirs=main:stripe1-node1-data-dir"));
  }

  @Test
  public void testStripe_level_setBackupDirectory() {
    ConfigTool.start("set", "-s", "localhost:" + getNodePort(), "-c", "stripe.1.node-backup-dir=backup" + File.separator + "stripe-1");
    assertCommandSuccessful();

    ConfigTool.start("get", "-s", "localhost:" + getNodePort(), "-c", "node-backup-dir");
    waitUntil(out::getLog, containsString("stripe.1.node.1.node-backup-dir=backup" + File.separator + "stripe-1"));
    waitUntil(out::getLog, containsString("stripe.1.node.2.node-backup-dir=backup" + File.separator + "stripe-1"));
  }


  /*<--Cluster-wide Tests-->*/
  @Test
  public void testCluster_setOffheap() {
    ConfigTool.start("set", "-s", "localhost:" + getNodePort(), "-c", "offheap-resources.main=1GB");
    assertCommandSuccessful();

    ConfigTool.start("get", "-s", "localhost:" + getNodePort(), "-c", "offheap-resources");
    waitUntil(out::getLog, containsString("offheap-resources=main:1GB"));
  }

  @Test
  public void testCluster_setBackupDirectory() {
    ConfigTool.start("set", "-s", "localhost:" + getNodePort(), "-c", "node-backup-dir=backup" + File.separator + "data");
    assertCommandSuccessful();

    ConfigTool.start("get", "-s", "localhost:" + getNodePort(), "-c", "node-backup-dir");
    waitUntil(out::getLog, containsString("node-backup-dir=backup" + File.separator + "data"));
  }

  @Test
  public void testCluster_setClientLeaseTime() {
    ConfigTool.start("set", "-s", "localhost:" + getNodePort(), "-c", "client-lease-duration=10s");
    assertCommandSuccessful();

    ConfigTool.start("get", "-s", "localhost:" + getNodePort(), "-c", "client-lease-duration");
    waitUntil(out::getLog, containsString("client-lease-duration=10s"));
  }

  @Test
  public void testCluster_setFailoverPriorityAvailability() {
    ConfigTool.start("set", "-s", "localhost:" + getNodePort(), "-c", "failover-priority=availability");
    assertCommandSuccessful();

    ConfigTool.start("get", "-s", "localhost:" + getNodePort(), "-c", "failover-priority");
    waitUntil(out::getLog, containsString("failover-priority=availability"));
  }

  @Test
  public void testCluster_setFailoverPriorityConsistency() {
    ConfigTool.start("set", "-s", "localhost:" + getNodePort(), "-c", "failover-priority=consistency:2");
    assertCommandSuccessful();

    ConfigTool.start("get", "-s", "localhost:" + getNodePort(), "-c", "failover-priority");
    waitUntil(out::getLog, containsString("failover-priority=consistency:2"));
  }

  @Test
  public void testCluster_setClientReconnectWindow() {
    ConfigTool.start("set", "-s", "localhost:" + getNodePort(), "-c", "client-reconnect-window=10s");
    assertCommandSuccessful();

    ConfigTool.start("get", "-s", "localhost:" + getNodePort(), "-c", "client-reconnect-window");
    waitUntil(out::getLog, containsString("client-reconnect-window=10s"));
  }

  @Test
  public void testCluster_setClientReconnectWindow_postActivation() throws Exception {
    activateCluster();

    ConfigTool.start("set", "-s", "localhost:" + getNodePort(), "-c", "client-reconnect-window=10s");
    assertCommandSuccessful();

    ConfigTool.start("get", "-s", "localhost:" + getNodePort(), "-c", "client-reconnect-window");
    waitUntil(out::getLog, containsString("client-reconnect-window=10s"));
  }
}
