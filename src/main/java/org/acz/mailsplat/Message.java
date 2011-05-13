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

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

@Immutable
public class Message
{
    private final String sender;
    private final String recipient;
    private final String data;

    public Message(String sender, String recipient, String data)
    {
        Preconditions.checkNotNull(sender, "sender is null");
        Preconditions.checkNotNull(recipient, "recipient is null");
        Preconditions.checkNotNull(data, "data is null");

        this.sender = sender;
        this.recipient = recipient;
        this.data = data;
    }

    public String getSender()
    {
        return sender;
    }

    public String getRecipient()
    {
        return recipient;
    }

    public String getData()
    {
        return data;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (!data.equals(message.data)) return false;
        if (!recipient.equals(message.recipient)) return false;
        if (!sender.equals(message.sender)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = sender.hashCode();
        result = 31 * result + recipient.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }
}
