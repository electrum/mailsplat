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

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.proofpoint.json.JsonCodec;
import org.testng.annotations.Test;

import static com.proofpoint.json.JsonCodec.jsonCodec;
import static org.testng.Assert.assertEquals;

public class TestPersonRepresentation
{
    private final JsonCodec<MessageRepresentation> codec = jsonCodec(MessageRepresentation.class);

    // TODO: add equivalence test

    @Test
    public void testJsonRoundTrip()
    {
        MessageRepresentation expected = new MessageRepresentation("alice@example.com", "Alice", null);
        String json = codec.toJson(expected);
        MessageRepresentation actual = codec.fromJson(json);
        assertEquals(actual, expected);
    }

    @Test
    public void testJsonDecode()
            throws Exception
    {
        MessageRepresentation expected = new MessageRepresentation("foo@example.com", "Mr Foo", null);

        String json = Resources.toString(Resources.getResource("single.json"), Charsets.UTF_8);
        MessageRepresentation actual = codec.fromJson(json);

        assertEquals(actual, expected);
    }
}
