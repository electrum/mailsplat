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

import com.google.common.collect.ImmutableList;
import com.proofpoint.event.client.InMemoryEventClient;
import com.proofpoint.units.Duration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.acz.mailsplat.MessageEvent.personAdded;
import static org.acz.mailsplat.MessageEvent.personRemoved;
import static org.acz.mailsplat.MessageEvent.personUpdated;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class TestPersonStore
{
    @Test
    public void testStartsEmpty()
    {
        MessageStore store = new MessageStore(new StoreConfig(), new InMemoryEventClient());
        assertTrue(store.getAll().isEmpty());
    }

    @Test
    public void testTtl()
            throws InterruptedException
    {
        StoreConfig config = new StoreConfig();
        config.setTtl(new Duration(1, TimeUnit.MILLISECONDS));

        MessageStore store = new MessageStore(config, new InMemoryEventClient());
        store.put("foo", new Message("foo@example.com", "Mr Foo"));
        Thread.sleep(2);
        Assert.assertNull(store.get("foo"));
    }

    @Test
    public void testPut()
    {
        InMemoryEventClient eventClient = new InMemoryEventClient();
        MessageStore store = new MessageStore(new StoreConfig(), eventClient);
        store.put("foo", new Message("foo@example.com", "Mr Foo"));

        assertEquals(new Message("foo@example.com", "Mr Foo"), store.get("foo"));
        assertEquals(store.getAll().size(), 1);

        assertEquals(eventClient.getEvents(), ImmutableList.of(personAdded("foo", new Message("foo@example.com", "Mr Foo"))));
    }

    @Test
    public void testIdempotentPut()
    {
        InMemoryEventClient eventClient = new InMemoryEventClient();
        MessageStore store = new MessageStore(new StoreConfig(), eventClient);
        store.put("foo", new Message("foo@example.com", "Mr Foo"));
        store.put("foo", new Message("foo@example.com", "Mr Bar"));

        assertEquals(new Message("foo@example.com", "Mr Bar"), store.get("foo"));
        assertEquals(store.getAll().size(), 1);

        assertEquals(eventClient.getEvents(), ImmutableList.of(
                personAdded("foo", new Message("foo@example.com", "Mr Foo")),
                personUpdated("foo", new Message("foo@example.com", "Mr Bar"))
        ));
    }

    @Test
    public void testDelete()
    {
        InMemoryEventClient eventClient = new InMemoryEventClient();
        MessageStore store = new MessageStore(new StoreConfig(), eventClient);
        store.put("foo", new Message("foo@example.com", "Mr Foo"));
        store.delete("foo");

        assertNull(store.get("foo"));
        assertTrue(store.getAll().isEmpty());

        assertEquals(eventClient.getEvents(), ImmutableList.of(
                personAdded("foo", new Message("foo@example.com", "Mr Foo")),
                personRemoved("foo", new Message("foo@example.com", "Mr Foo"))
        ));
    }

    @Test
    public void testIdempotentDelete()
    {
        InMemoryEventClient eventClient = new InMemoryEventClient();
        MessageStore store = new MessageStore(new StoreConfig(), eventClient);
        store.put("foo", new Message("foo@example.com", "Mr Foo"));

        store.delete("foo");
        assertTrue(store.getAll().isEmpty());
        assertNull(store.get("foo"));

        store.delete("foo");
        assertTrue(store.getAll().isEmpty());
        assertNull(store.get("foo"));

        assertEquals(eventClient.getEvents(), ImmutableList.of(
                personAdded("foo", new Message("foo@example.com", "Mr Foo")),
                personRemoved("foo", new Message("foo@example.com", "Mr Foo"))
        ));
    }

    @Test
    public void testGetAll()
    {
        MessageStore store = new MessageStore(new StoreConfig(), new InMemoryEventClient());

        store.put("foo", new Message("foo@example.com", "Mr Foo"));
        store.put("bar", new Message("bar@example.com", "Mr Bar"));

        assertEquals(store.getAll().size(), 2);
        assertEquals(store.getAll(), asList(new Message("foo@example.com", "Mr Foo"), new Message("bar@example.com", "Mr Bar")));
    }

}
