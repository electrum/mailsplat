package org.acz.mailsplat.smtp;

import com.proofpoint.configuration.Config;

public class SmtpServerConfig
{
    private int smtpPort = 2525;

    public int getSmtpPort()
    {
        return smtpPort;
    }

    @Config("smtp-server.smtp.port")
    public SmtpServerConfig setSmtpPort(int smtpPort)
    {
        this.smtpPort = smtpPort;
        return this;
    }
}
