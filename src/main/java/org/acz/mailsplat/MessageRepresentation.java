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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Header;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class MessageRepresentation
{
    private final Message message;
    private final URI self;
    private final boolean full;

    public static MessageRepresentation from(Message message, URI self, boolean full)
    {
        return new MessageRepresentation(message, self, full);
    }

    @JsonCreator
    public MessageRepresentation(
        @JsonProperty("id") String id,
        @JsonProperty("sender") String sender,
        @JsonProperty("recipient") String recipient,
        @JsonProperty("data") String data,
        @JsonProperty("self") URI self)
    {
        this(new Message(id, sender, recipient, data), self, true);
    }

    private MessageRepresentation(Message message, URI self, boolean full)
    {
        this.message = message;
        this.self = self;
        this.full = full;
    }

    @JsonProperty
    @NotNull(message = "is missing")
    @Pattern(regexp = "\\p{XDigit}{8}(-\\p{XDigit}{4}){3}\\p{XDigit}{12}")
    public String getId()
    {
        return message.getId();
    }

    @JsonProperty
    @NotNull(message = "is missing")
    @Pattern(regexp = "|[^@]+@[^@]+", message = "is malformed")
    public String getSender()
    {
        return message.getSender();
    }

    @JsonProperty
    @NotNull(message = "is missing")
    @Pattern(regexp = "[^@]+@[^@]+", message = "is malformed")
    public String getRecipient()
    {
        return message.getRecipient();
    }

    @JsonProperty
    public String getData()
    {
        return full ? message.getData() : null;
    }

    @JsonProperty
    public Map<String, List<String>> getHeaders()
    {
        if (!full) return null;
        try {
            Session session = Session.getDefaultInstance(new Properties());
            ByteArrayInputStream stream = new ByteArrayInputStream(message.getData().getBytes(Charsets.ISO_8859_1));
            MimeMessage message = new MimeMessage(session, stream);

            Map<String, List<String>> headers = Maps.newTreeMap();
            for (Enumeration e = message.getAllHeaders(); e.hasMoreElements(); ) {
                Header header = (Header) e.nextElement();
                if (!headers.containsKey(header.getName())) {
                    headers.put(header.getName(), new ArrayList<String>());
                }
                headers.get(header.getName()).add(header.getValue());
            }
            return headers;
        }
        catch (Exception e) {
            return null;
        }
    }

    @JsonProperty
    public URI getSelf()
    {
        return self;
    }

    public Message toMessage()
    {
        return message;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageRepresentation that = (MessageRepresentation) o;

        if (!message.equals(that.message)) return false;
        if (!self.equals(that.self)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = message.hashCode();
        result = 31 * result + self.hashCode();
        return result;
    }
}
