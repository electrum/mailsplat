package org.acz.mailsplat;

import com.google.common.base.Preconditions;
import com.proofpoint.event.client.EventField;
import com.proofpoint.event.client.EventType;

@EventType("test:type=message")
public class MessageEvent
{
    public static MessageEvent messageAdded(String messageId, Message message)
    {
        return new MessageEvent(Operation.ADDED, messageId, message);
    }

    public static MessageEvent messageRemoved(String messageId, Message message)
    {
        return new MessageEvent(Operation.REMOVED, messageId, message);
    }

    public enum Operation{ ADDED, REMOVED }

    private final Operation operation;
    private final String messageId;
    private final Message message;

    private MessageEvent(Operation operation, String messageId, Message message)
    {
        Preconditions.checkNotNull(operation, "operation is null");
        Preconditions.checkNotNull(messageId, "id is null");
        Preconditions.checkNotNull(message, "message is null");

        this.operation = operation;
        this.messageId = messageId;
        this.message = message;
    }

    @EventField
    public String getOperation()
    {
        return operation.toString();
    }

    @EventField
    public String getmessageId()
    {
        return messageId;
    }

    @EventField
    public String getSender()
    {
        return message.getSender();
    }

    @EventField
    public String getRecipient()
    {
        return message.getRecipient();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MessageEvent that = (MessageEvent) o;

        if (operation != that.operation) {
            return false;
        }
        if (!message.equals(that.message)) {
            return false;
        }
        if (!messageId.equals(that.messageId)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = operation.hashCode();
        result = 31 * result + messageId.hashCode();
        result = 31 * result + message.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("MessageEvent");
        sb.append("{id='").append(messageId).append('\'');
        sb.append(", message=").append(message);
        sb.append('}');
        return sb.toString();
    }
}
