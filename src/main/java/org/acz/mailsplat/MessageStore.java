/*
 * Copyright 2010 Proofpoint, Inc.
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
package org.acz.mailsplat;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.proofpoint.event.client.EventClient;
import org.weakref.jmx.Flatten;
import org.weakref.jmx.Managed;

public class MessageStore
{
    private final ConcurrentMap<String, Message> messages;
    private final MessageStoreStats stats;

    @Inject
    public MessageStore(StoreConfig config, EventClient eventClient)
    {
        Preconditions.checkNotNull(config, "config must not be null");
        Preconditions.checkNotNull(eventClient, "eventClient is null");

        messages = new MapMaker()
                .expireAfterWrite((long) config.getTtl().toMillis(), TimeUnit.MILLISECONDS)
                .makeMap();
        stats = new MessageStoreStats(eventClient);
    }

    @Managed
    @Flatten
    public MessageStoreStats getStats()
    {
        return stats;
    }

    public Message get(String id)
    {
        Preconditions.checkNotNull(id, "id must not be null");

        Message message = messages.get(id);
        if (message != null) {
            stats.messageFetched();
        }
        return message;
    }

    public void put(Message message)
    {
        Preconditions.checkNotNull(message, "message must not be null");

        if (messages.containsKey(message.getId())) {
            throw new IllegalArgumentException("message already exists: " + message.getId());
        }

        messages.put(message.getId(), message);
        stats.messageAdded(message.getId(), message);
    }

    /**
     * @return true if the entry was removed
     */
    public boolean delete(String id)
    {
        Preconditions.checkNotNull(id, "id must not be null");

        Message removedMessage = messages.remove(id);
        if (removedMessage != null) {
            stats.messageRemoved(id, removedMessage);
        }

        return removedMessage != null;
    }

    public Collection<Message> getAll()
    {
        return ImmutableList.copyOf(messages.values());
    }
}
