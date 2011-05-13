package org.acz.mailsplat.smtp;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.proofpoint.configuration.ConfigurationModule;
import org.subethamail.smtp.server.SMTPServer;

public class SmtpServerModule implements Module
{
    @Override
    public void configure(Binder binder)
    {
        binder.requireExplicitBindings();
        binder.disableCircularProxies();

        binder.bind(SMTPServer.class).toProvider(SmtpServerProvider.class).in(Scopes.SINGLETON);

        ConfigurationModule.bindConfig(binder).to(SmtpServerConfig.class);
    }
}
