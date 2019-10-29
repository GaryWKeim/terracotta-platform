/*
 * Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.
 */
package com.terracottatech.nomad.client;

import com.terracottatech.nomad.client.results.DiscoverResultsReceiver;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.InetSocketAddress;
import java.util.UUID;

import static com.terracottatech.nomad.client.NomadTestHelper.discovery;
import static com.terracottatech.nomad.client.NomadTestHelper.withItems;
import static com.terracottatech.nomad.server.ChangeRequestState.COMMITTED;
import static com.terracottatech.nomad.server.ChangeRequestState.ROLLED_BACK;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ClusterConsistencyCheckerTest {
  @Mock
  private DiscoverResultsReceiver<String> results;

  private ClusterConsistencyChecker<String> consistencyChecker = new ClusterConsistencyChecker<>();
  private InetSocketAddress address1 = InetSocketAddress.createUnresolved("localhost", 9410);
  private InetSocketAddress address2 = InetSocketAddress.createUnresolved("localhost", 9411);
  private InetSocketAddress address3 = InetSocketAddress.createUnresolved("localhost", 9412);
  private InetSocketAddress address4 = InetSocketAddress.createUnresolved("localhost", 9413);
  private InetSocketAddress address5 = InetSocketAddress.createUnresolved("localhost", 9414);

  @After
  public void after() {
    verifyNoMoreInteractions(results);
  }

  @Test
  public void allCommitForSameUuid() {
    UUID uuid = UUID.randomUUID();

    consistencyChecker.discovered(address1, discovery(COMMITTED, uuid));
    consistencyChecker.discovered(address2, discovery(COMMITTED, uuid));

    consistencyChecker.checkClusterConsistency(results);

    verifyNoMoreInteractions(results);
  }

  @Test
  public void allRollbackForSameUuid() {
    UUID uuid = UUID.randomUUID();

    consistencyChecker.discovered(address1, discovery(ROLLED_BACK, uuid));
    consistencyChecker.discovered(address2, discovery(ROLLED_BACK, uuid));

    consistencyChecker.checkClusterConsistency(results);
  }

  @Test
  public void inconsistentCluster() {
    UUID uuid = UUID.randomUUID();

    consistencyChecker.discovered(address1, discovery(COMMITTED, uuid));
    consistencyChecker.discovered(address2, discovery(ROLLED_BACK, uuid));

    consistencyChecker.checkClusterConsistency(results);

    verify(results).discoverClusterInconsistent(eq(uuid), withItems(address1), withItems(address2));
  }

  @Test
  public void differentUuids() {
    consistencyChecker.discovered(address1, discovery(COMMITTED));
    consistencyChecker.discovered(address2, discovery(ROLLED_BACK));

    consistencyChecker.checkClusterConsistency(results);
  }

  @Test
  public void multipleInconsistencies() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();

    consistencyChecker.discovered(address1, discovery(COMMITTED, uuid1));
    consistencyChecker.discovered(address2, discovery(ROLLED_BACK, uuid1));
    consistencyChecker.discovered(address3, discovery(COMMITTED, uuid2));
    consistencyChecker.discovered(address4, discovery(COMMITTED, uuid2));
    consistencyChecker.discovered(address5, discovery(ROLLED_BACK, uuid2));

    consistencyChecker.checkClusterConsistency(results);

    verify(results).discoverClusterInconsistent(eq(uuid1), withItems(address1), withItems(address2));
    verify(results).discoverClusterInconsistent(eq(uuid2), withItems(address3, address4), withItems(address5));
  }
}
