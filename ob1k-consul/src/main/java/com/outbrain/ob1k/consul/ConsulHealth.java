package com.outbrain.ob1k.consul;

import com.outbrain.ob1k.Service;
import com.outbrain.ob1k.concurrent.ComposableFuture;

import java.util.List;

/**
 * A programmatic API that maps to the /v1/health/* consul REST API
 *
 * @author Eran Harel
 */
public interface ConsulHealth extends Service {

  ComposableFuture<List<HealthInfoInstance>> filterDcLocalHealthyInstances(final String service, final String filterTag);

  ComposableFuture<List<HealthInfoInstance>> pollHealthyInstances(final String service, final String filterTag, final long index);

  ComposableFuture<List<HealthInfoInstance>> getInstancesHealth(final String service, final String dc);

  ComposableFuture<List<HealthInfoInstance.Check>> getInstancesChecks(final String service, final String dc);

  ComposableFuture<List<HealthInfoInstance.Check>> getInstancesAtState(final States state, final String dc);

  enum States {
    ANY,
    UNKNOWN,
    PASSING,
    WARNING,
    CRITICAL;

    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }
}