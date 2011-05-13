package org.acz.mailsplat.smtp;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;

public class SmtpServerProvider implements Provider<SMTPServer>
{
    private final SmtpServerConfig config;
    private final MessageHandlerFactory messageHandlerFactory;

    @Inject
    public SmtpServerProvider(SmtpServerConfig config, MessageHandlerFactory messageHandlerFactory)
    {
        Preconditions.checkNotNull(config, "config is null");
        Preconditions.checkNotNull(messageHandlerFactory, "messageHandlerFactory is null");

        this.config = config;
        this.messageHandlerFactory = messageHandlerFactory;
    }

    @Override
    public SMTPServer get()
    {
        try {
            SMTPServer smtpServer = new SMTPServer(messageHandlerFactory);
            smtpServer.setPort(config.getSmtpPort());
            smtpServer.start();
            return smtpServer;
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
