package com.hivemq.plugins.jvmmetrics.callbacks;

import com.codahale.metrics.MetricRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.SortedSet;

import static org.junit.Assert.assertTrue;

/**
 * @author Christoph Schäbel
 */
public class JvmMetricsStartTest {

    private MetricRegistry metricRegistry;
    private JvmMetricsStart callback;

    @Before
    public void before() {
        metricRegistry = new MetricRegistry();
        callback = new JvmMetricsStart(metricRegistry);
    }

    @Test
    public void testOnBrokerStart_metricsRegistered() throws Exception {

        callback.onBrokerStart();

        final SortedSet<String> names = metricRegistry.getNames();

        //check for at least one metric from each metric set
        assertTrue(names.contains("com.hivemq.jvm.buffer-pool.direct.count"));
        assertTrue(names.contains("com.hivemq.jvm.buffer-pool.mapped.count"));
        assertTrue(names.contains("com.hivemq.jvm.class-loader.loaded"));
        assertTrue(names.contains("com.hivemq.jvm.file-descriptor.ratio"));
        assertTrue(names.contains("com.hivemq.jvm.garbage-collector.PS-MarkSweep.count"));
        assertTrue(names.contains("com.hivemq.jvm.garbage-collector.PS-Scavenge.count"));
        assertTrue(names.contains("com.hivemq.jvm.memory.heap.max"));
        assertTrue(names.contains("com.hivemq.jvm.memory.non-heap.max"));
        assertTrue(names.contains("com.hivemq.jvm.memory.total.max"));
        assertTrue(names.contains("com.hivemq.jvm.threads.count"));
    }
}