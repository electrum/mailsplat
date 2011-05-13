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

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.proofpoint.discovery.client.DiscoveryBinder;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.weakref.jmx.guice.MBeanModule;

import static com.proofpoint.configuration.ConfigurationModule.bindConfig;
import static com.proofpoint.event.client.EventBinder.eventBinder;

public class MainModule
        implements Module
{
    public void configure(Binder binder)
    {
        binder.requireExplicitBindings();
        binder.disableCircularProxies();

        binder.bind(MessageStore.class).in(Scopes.SINGLETON);
        MBeanModule.newExporter(binder).export(MessageStore.class).withGeneratedName();

        binder.bind(MessagesResource.class).in(Scopes.SINGLETON);
        binder.bind(MessageResource.class).in(Scopes.SINGLETON);

        bindConfig(binder).to(StoreConfig.class);
        eventBinder(binder).bindEventClient(MessageEvent.class);

        binder.bind(MessageListener.class).in(Scopes.SINGLETON);

        DiscoveryBinder.discoveryBinder(binder).bindHttpAnnouncement("mailsplat");
    }

    @Provides
    @Inject
    public MessageHandlerFactory provideMessageHandlerFactory(MessageListener listener)
    {
        return new SimpleMessageListenerAdapter(listener);
    }
}
