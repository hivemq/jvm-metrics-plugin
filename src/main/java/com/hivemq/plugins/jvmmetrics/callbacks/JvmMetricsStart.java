/*
 * Copyright 2015 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.plugins.jvmmetrics.callbacks;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.*;
import com.hivemq.spi.callback.CallbackPriority;
import com.hivemq.spi.callback.events.broker.OnBrokerStart;
import com.hivemq.spi.callback.exception.BrokerUnableToStartException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * Callback which registers new JVM-based metrics with HiveMQ's global metric registry when the broker starts.
 *
 * @author Christoph Schäbel
 */
public class JvmMetricsStart implements OnBrokerStart {

    private static final Logger log = LoggerFactory.getLogger(JvmMetricsStart.class);

    public static final String JVM_METRIC_PREFIX = "com.hivemq.jvm.";

    private final MetricRegistry metricRegistry;

    /**
     * Constructor
     *
     * @param metricRegistry HiveMQ's global metric registry
     */
    @Inject
    public JvmMetricsStart(final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    /**
     * Callback which registers new JVM-based metrics with HiveMQ's global metric registry when the broker starts.
     *
     * @throws BrokerUnableToStartException
     */
    @Override
    public void onBrokerStart() throws BrokerUnableToStartException {

        registerBufferPoolMetrics();
        registerThreadStateMetrics();
        registerClassLoaderMetrics();
        registerGarbageCollectorMetrics();
        registerMemoryMetrics();
        registerFileDescriptorMetrics();

        log.debug("Registered JMV metrics with prefix {}", JVM_METRIC_PREFIX);
    }

    private void registerFileDescriptorMetrics() {
        try {

            register("file-descriptor.ratio", new FileDescriptorRatioGauge(ManagementFactory.getOperatingSystemMXBean()));

        } catch (Exception ex) {
            log.warn("Not able to register JVM metrics for File descriptor usage, this is probably not supported by this JVM");
            if (log.isDebugEnabled()) {
                log.debug("original Exception", ex);
            }
        }
    }

    private void registerMemoryMetrics() {
        try {

            registerAll("memory", new MemoryUsageGaugeSet(ManagementFactory.getMemoryMXBean(), ManagementFactory.getMemoryPoolMXBeans()));

        } catch (Exception ex) {
            log.warn("Not able to register JVM metrics for Memory usage, this is probably not supported by this JVM");
            if (log.isDebugEnabled()) {
                log.debug("original Exception", ex);
            }
        }
    }

    private void registerGarbageCollectorMetrics() {
        try {

            registerAll("garbage-collector", new GarbageCollectorMetricSet(ManagementFactory.getGarbageCollectorMXBeans()));

        } catch (Exception ex) {
            log.warn("Not able to register JVM metrics for GarbageCollection, this is probably not supported by this JVM");
            if (log.isDebugEnabled()) {
                log.debug("original Exception", ex);
            }
        }
    }

    private void registerClassLoaderMetrics() {
        try {

            registerAll("class-loader", new ClassLoadingGaugeSet(ManagementFactory.getClassLoadingMXBean()));

        } catch (Exception ex) {
            log.warn("Not able to register JVM metrics for ClassLoaders, this is probably not supported by this JVM");
            if (log.isDebugEnabled()) {
                log.debug("original Exception", ex);
            }
        }
    }

    private void registerThreadStateMetrics() {
        try {

            registerAll("threads", new CachedThreadStatesGaugeSet(ManagementFactory.getThreadMXBean(),
                    new ThreadDeadlockDetector(ManagementFactory.getThreadMXBean()), 1, TimeUnit.SECONDS));

        } catch (Exception ex) {
            log.warn("Not able to register JVM metrics for Thread states, this is probably not supported by this JVM");
            if (log.isDebugEnabled()) {
                log.debug("original Exception", ex);
            }
        }
    }

    private void registerBufferPoolMetrics() {
        try {

            registerAll("buffer-pool", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));

        } catch (Exception ex) {
            log.warn("Not able to register JVM metrics for BufferPools, this is probably not supported by this JVM");
            if (log.isDebugEnabled()) {
                log.debug("original Exception", ex);
            }
        }
    }

    private void registerAll(String prefix, MetricSet metricSet) {
        for (Map.Entry<String, Metric> entry : metricSet.getMetrics().entrySet()) {
            if (entry.getValue() instanceof MetricSet) {
                registerAll(prefix + "." + entry.getKey(), (MetricSet) entry.getValue());
            } else {
                register(prefix + "." + entry.getKey(), entry.getValue());
            }
        }
    }

    private void register(final String prefix, Metric metric) {
        metricRegistry.register(JVM_METRIC_PREFIX + prefix, metric);
    }

    @Override
    public int priority() {
        return CallbackPriority.MEDIUM;
    }
}
