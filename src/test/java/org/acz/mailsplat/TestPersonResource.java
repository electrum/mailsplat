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
import com.proofpoint.jaxrs.testing.MockUriInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.net.URI;

import static org.acz.mailsplat.MessageEvent.personAdded;
import static org.acz.mailsplat.MessageEvent.personRemoved;
import static org.acz.mailsplat.MessageEvent.personUpdated;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class TestPersonResource
{
    private MessageResource resource;
    private MessageStore store;
    private InMemoryEventClient eventClient;

    @BeforeMethod
    public void setup()
    {
        eventClient = new InMemoryEventClient();
        store = new MessageStore(new StoreConfig(), eventClient);
        resource = new MessageResource(store);
    }

    @Test
    public void testNotFound()
    {
        Response response = resource.get("foo", MockUriInfo.from(URI.create("http://localhost/v1/person/1")));
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGet()
    {
        store.put("foo", new Message("foo@example.com", "Mr Foo"));

        Response response = resource.get("foo", MockUriInfo.from(URI.create("http://localhost/v1/person/1")));
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(response.getEntity(), new MessageRepresentation("foo@example.com", "Mr Foo", URI.create("http://localhost/v1/person/1")));
        assertNull(response.getMetadata().get("Content-Type")); // content type is set by jersey based on @Produces
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetNull()
    {
        resource.get(null, MockUriInfo.from(URI.create("http://localhost/v1/person/1")));
    }

    @Test
    public void testAdd()
    {
        Response response = resource.put("foo", new MessageRepresentation("foo@example.com", "Mr Foo", null));

        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        assertNull(response.getEntity());
        assertNull(response.getMetadata().get("Content-Type")); // content type is set by jersey based on @Produces

        assertEquals(store.get("foo"), new Message("foo@example.com", "Mr Foo"));


        assertEquals(eventClient.getEvents(), ImmutableList.of(
                personAdded("foo", new Message("foo@example.com", "Mr Foo"))
        ));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testPutNullId()
    {
        resource.put(null, new MessageRepresentation("foo@example.com", "Mr Foo", null));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testPutNullValue()
    {
        resource.put("foo", null);
    }

    @Test
    public void testReplace()
    {
        store.put("foo", new Message("foo@example.com", "Mr Foo"));

        Response response = resource.put("foo", new MessageRepresentation("bar@example.com", "Mr Bar", null));

        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
        assertNull(response.getEntity());
        assertNull(response.getMetadata().get("Content-Type")); // content type is set by jersey based on @Produces

        assertEquals(store.get("foo"), new Message("bar@example.com", "Mr Bar"));

        assertEquals(eventClient.getEvents(), ImmutableList.of(
                personAdded("foo", new Message("foo@example.com", "Mr Foo")),
                personUpdated("foo", new Message("bar@example.com", "Mr Bar"))
        ));
    }

    @Test
    public void testDelete()
    {
        store.put("foo", new Message("foo@example.com", "Mr Foo"));

        Response response = resource.delete("foo");
        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
        assertNull(response.getEntity());

        assertNull(store.get("foo"));

        assertEquals(eventClient.getEvents(), ImmutableList.of(
                personAdded("foo", new Message("foo@example.com", "Mr Foo")),
                personRemoved("foo", new Message("foo@example.com", "Mr Foo"))
        ));
    }

    @Test
    public void testDeleteMissing()
    {
        Response response = resource.delete("foo");
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
        assertNull(response.getEntity());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testDeleteNullId()
    {
        resource.delete(null);
    }
}
