package org.acz.mailsplat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.proofpoint.log.Logger;
import org.subethamail.smtp.helper.SimpleMessageListener;

public class MessageListener implements SimpleMessageListener
{
    private final Logger log = Logger.get(getClass());
    private final MessageStore store;

    @Inject
    public MessageListener(MessageStore store)
    {
        this.store = store;
    }

    @Override
    public boolean accept(String from, String recipient)
    {
        return true;
    }

    @Override
    public void deliver(String from, String recipient, InputStream data) throws IOException
    {
        String id = UUID.randomUUID().toString();
        log.info("deliver: from=%s, recipient=%s, id=%s", from, recipient, id);

        String messageData = CharStreams.toString(new InputStreamReader(data, Charsets.ISO_8859_1));
        Message message = new Message(from, recipient, messageData);
        store.put(id, message);
    }
}
