/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.atomix.client.multimap;

import java.time.Duration;

import io.atomix.collections.DistributedMultiMap;
import io.atomix.resource.ReadConsistency;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Message;
import org.apache.camel.component.atomix.client.AbstractAtomixClientProducer;
import org.apache.camel.spi.InvokeOnHeader;
import org.apache.camel.util.ObjectHelper;

import static org.apache.camel.component.atomix.client.AtomixClientConstants.RESOURCE_KEY;
import static org.apache.camel.component.atomix.client.AtomixClientConstants.RESOURCE_NAME;
import static org.apache.camel.component.atomix.client.AtomixClientConstants.RESOURCE_READ_CONSISTENCY;
import static org.apache.camel.component.atomix.client.AtomixClientConstants.RESOURCE_TTL;
import static org.apache.camel.component.atomix.client.AtomixClientConstants.RESOURCE_VALUE;

public final class AtomixMultiMapProducer extends AbstractAtomixClientProducer<AtomixMultiMapEndpoint, DistributedMultiMap> {
    private final AtomixMultiMapConfiguration configuration;

    protected AtomixMultiMapProducer(AtomixMultiMapEndpoint endpoint) {
        super(endpoint, endpoint.getConfiguration().getDefaultAction().name());
        this.configuration = endpoint.getConfiguration();
    }

    // *********************************
    // Handlers
    // *********************************

    private long getResourceTtl(Message message) {
        Duration ttl = message.getHeader(RESOURCE_TTL, configuration::getTtl, Duration.class);
        return ttl != null ? ttl.toMillis() : 0;
    }

    @InvokeOnHeader("PUT")
    void onPut(Message message, AsyncCallback callback) throws Exception {
        final DistributedMultiMap<Object, Object> map = getResource(message);
        final Object key = message.getHeader(RESOURCE_KEY, configuration::getKey, Object.class);
        final Object val = message.getHeader(RESOURCE_VALUE, message::getBody, Object.class);
        final long ttl = getResourceTtl(message);

        ObjectHelper.notNull(key, RESOURCE_KEY);
        ObjectHelper.notNull(val, RESOURCE_VALUE);

        if (ttl > 0) {
            map.put(key, val, Duration.ofMillis(ttl)).thenAccept(
                    result -> processResult(message, callback, result));
        } else {
            map.put(key, val).thenAccept(
                    result -> processResult(message, callback, result));
        }
    }

    @InvokeOnHeader("GET")
    void onGet(Message message, AsyncCallback callback) throws Exception {
        final DistributedMultiMap<Object, Object> map = getResource(message);
        final Object key = message.getHeader(RESOURCE_KEY, configuration::getKey, Object.class);
        final ReadConsistency consistency
                = message.getHeader(RESOURCE_READ_CONSISTENCY, configuration::getReadConsistency, ReadConsistency.class);

        ObjectHelper.notNull(key, RESOURCE_KEY);

        if (consistency != null) {
            map.get(key, consistency).thenAccept(
                    result -> processResult(message, callback, result));
        } else {
            map.get(key).thenAccept(
                    result -> processResult(message, callback, result));
        }
    }

    @InvokeOnHeader("CLEAR")
    void onClear(Message message, AsyncCallback callback) throws Exception {
        final DistributedMultiMap<Object, Object> map = getResource(message);

        map.clear().thenAccept(
                result -> processResult(message, callback, result));
    }

    @InvokeOnHeader("SIZE")
    void onSize(Message message, AsyncCallback callback) throws Exception {
        final DistributedMultiMap<Object, Object> map = getResource(message);
        final ReadConsistency consistency
                = message.getHeader(RESOURCE_READ_CONSISTENCY, configuration::getReadConsistency, ReadConsistency.class);
        final Object key = message.getHeader(RESOURCE_KEY, message::getBody, Object.class);

        if (consistency != null) {
            if (key != null) {
                map.size(key, consistency).thenAccept(
                        result -> processResult(message, callback, result));
            } else {
                map.size(consistency).thenAccept(
                        result -> processResult(message, callback, result));
            }
        } else {
            if (key != null) {
                map.size(key).thenAccept(
                        result -> processResult(message, callback, result));
            } else {
                map.size().thenAccept(
                        result -> processResult(message, callback, result));
            }
        }
    }

    @InvokeOnHeader("IS_EMPTY")
    void onIsEmpty(Message message, AsyncCallback callback) throws Exception {
        final DistributedMultiMap<Object, Object> map = getResource(message);
        final ReadConsistency consistency
                = message.getHeader(RESOURCE_READ_CONSISTENCY, configuration::getReadConsistency, ReadConsistency.class);

        if (consistency != null) {
            map.isEmpty(consistency).thenAccept(
                    result -> processResult(message, callback, result));
        } else {
            map.isEmpty().thenAccept(
                    result -> processResult(message, callback, result));
        }
    }

    @InvokeOnHeader("CONTAINS_KEY")
    void onContainsKey(Message message, AsyncCallback callback) throws Exception {
        final DistributedMultiMap<Object, Object> map = getResource(message);
        final ReadConsistency consistency
                = message.getHeader(RESOURCE_READ_CONSISTENCY, configuration::getReadConsistency, ReadConsistency.class);
        final Object key = message.getHeader(RESOURCE_KEY, message::getBody, Object.class);

        ObjectHelper.notNull(key, RESOURCE_KEY);

        if (consistency != null) {
            map.containsKey(key, consistency).thenAccept(
                    result -> processResult(message, callback, result));
        } else {
            map.containsKey(key).thenAccept(
                    result -> processResult(message, callback, result));
        }
    }

    @InvokeOnHeader("CONTAINS_VALUE")
    void onContainsValue(Message message, AsyncCallback callback) throws Exception {
        final DistributedMultiMap<Object, Object> map = getResource(message);
        final ReadConsistency consistency
                = message.getHeader(RESOURCE_READ_CONSISTENCY, configuration::getReadConsistency, ReadConsistency.class);
        final Object value = message.getHeader(RESOURCE_VALUE, message::getBody, Object.class);

        ObjectHelper.notNull(value, RESOURCE_VALUE);

        if (consistency != null) {
            map.containsValue(value, consistency).thenAccept(
                    result -> processResult(message, callback, result));
        } else {
            map.containsValue(value).thenAccept(
                    result -> processResult(message, callback, result));
        }
    }

    @InvokeOnHeader("CONTAINS_ENTRY")
    void onContainsEntry(Message message, AsyncCallback callback) throws Exception {
        final DistributedMultiMap<Object, Object> map = getResource(message);
        final ReadConsistency consistency
                = message.getHeader(RESOURCE_READ_CONSISTENCY, configuration::getReadConsistency, ReadConsistency.class);
        final Object key = message.getHeader(RESOURCE_KEY, message::getBody, Object.class);
        final Object value = message.getHeader(RESOURCE_VALUE, message::getBody, Object.class);

        ObjectHelper.notNull(key, RESOURCE_VALUE);
        ObjectHelper.notNull(value, RESOURCE_KEY);

        if (consistency != null) {
            map.containsEntry(key, value, consistency).thenAccept(
                    result -> processResult(message, callback, result));
        } else {
            map.containsEntry(key, value).thenAccept(
                    result -> processResult(message, callback, result));
        }
    }

    @InvokeOnHeader("REMOVE")
    void onRemove(Message message, AsyncCallback callback) throws Exception {
        final DistributedMultiMap<Object, Object> map = getResource(message);
        final Object key = message.getHeader(RESOURCE_KEY, message::getBody, Object.class);
        final Object value = message.getHeader(RESOURCE_VALUE, message::getBody, Object.class);

        ObjectHelper.notNull(key, RESOURCE_KEY);

        if (value != null) {
            map.remove(key, value).thenAccept(
                    result -> processResult(message, callback, result));
        } else {
            map.remove(key).thenAccept(
                    result -> processResult(message, callback, result));
        }
    }

    @InvokeOnHeader("REMOVE_VALUE")
    void onRemoveValue(Message message, AsyncCallback callback) throws Exception {
        final DistributedMultiMap<Object, Object> map = getResource(message);
        final Object value = message.getHeader(RESOURCE_VALUE, message::getBody, Object.class);

        ObjectHelper.notNull(value, RESOURCE_VALUE);

        map.removeValue(value).thenAccept(
                result -> processResult(message, callback, result));
    }

    // *********************************
    // Implementation
    // *********************************

    @Override
    protected String getResourceName(Message message) {
        return message.getHeader(RESOURCE_NAME, getAtomixEndpoint()::getResourceName, String.class);
    }

    @Override
    protected DistributedMultiMap<Object, Object> createResource(String resourceName) {
        return getAtomixEndpoint()
                .getAtomix()
                .getMultiMap(
                        resourceName,
                        new DistributedMultiMap.Config(getAtomixEndpoint().getConfiguration().getResourceOptions(resourceName)),
                        new DistributedMultiMap.Options(getAtomixEndpoint().getConfiguration().getResourceConfig(resourceName)))
                .join();
    }
}
