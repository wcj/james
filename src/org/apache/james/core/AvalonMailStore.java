/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE file.
 */
package org.apache.james.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.avalon.AbstractLoggable;
import org.apache.avalon.Component;
import org.apache.avalon.ComponentNotAccessibleException;
import org.apache.avalon.ComponentNotFoundException;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.Composer;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.Initializable;
import org.apache.james.services.MailRepository;
import org.apache.james.services.MailStore;
import org.apache.log.LogKit;
import org.apache.log.Logger;
import org.apache.phoenix.Block;

/**
 *
 * @author <a href="mailto:fede@apache.org">Federico Barbieri</a>
 */
public class AvalonMailStore 
    extends AbstractLoggable 
    implements Block, Composer, Configurable, MailStore, Initializable {

    private static final String REPOSITORY_NAME = "Repository";
    private static long id;
    private HashMap repositories;
    private HashMap models;
    private HashMap classes;
    protected Configuration          configuration;
    protected ComponentManager       componentManager;

    public void compose( final ComponentManager componentManager )
        throws ComponentManagerException
    {
        this.componentManager = componentManager;
    }

    public void configure( final Configuration configuration )
        throws ConfigurationException
    {
        this.configuration = configuration;
    }
    
    public void init() 
        throws Exception {
    
        getLogger().info("JamesMailStore init...");
        repositories = new HashMap();
        models = new HashMap();
        classes = new HashMap();
        Iterator registeredClasses = configuration.getChild("repositories").getChildren("repository");
        while (registeredClasses.hasNext()) {
            registerRepository((Configuration) registeredClasses.next());
        }
        getLogger().info("James RepositoryManager ...init");
    }
    
    public void registerRepository(Configuration repConf) throws ConfigurationException {
        String className = repConf.getAttribute("class");
        getLogger().info("Registering Repository " + className);
        Iterator protocols = repConf.getChild("protocols").getChildren("protocol");
        Iterator types = repConf.getChild("types").getChildren("type");
        Iterator models = repConf.getChild("models").getChildren("model");
        while (protocols.hasNext()) {
            String protocol = ((Configuration) protocols.next()).getValue();
            while (types.hasNext()) {
                String type = ((Configuration) types.next()).getValue();
                while (models.hasNext()) {
                    String model = ((Configuration) models.next()).getValue();
                    classes.put(protocol + type + model, className);
                    getLogger().info("   for " + protocol + "," + type + "," + model);
                }
            }
        }
    }

    public Component select(Object hint) throws ComponentNotFoundException,
        ComponentNotAccessibleException {
        
        Configuration repConf = null;
        try {
            repConf = (Configuration) hint;
        } catch (ClassCastException cce) {
            throw new ComponentNotAccessibleException("hint is of the wrong type. Must be a Configuration", cce);
        }
        URL destination = null;
        try {
            destination = new URL(repConf.getAttribute("destinationURL"));
        } catch (ConfigurationException ce) {
            throw new ComponentNotAccessibleException("Malformed configuration has no destinationURL attribute", ce);
        } catch (MalformedURLException mue) {
            throw new ComponentNotAccessibleException("destination is malformed. Must be a valid URL", mue);
        }

        try
        {
            String type = repConf.getAttribute("type");
            String repID = destination + type;
            MailRepository reply = (MailRepository) repositories.get(repID);
            String model = (String) repConf.getAttribute("model");
            if (reply != null) {
                if (models.get(repID).equals(model)) {
                    return (Component)reply;
                } else {
                    throw new ComponentNotFoundException("There is already another repository with the same destination and type but with different model");
                }
            } else {
                String protocol = destination.getProtocol();
                String repClass = (String) classes.get( protocol + type + model );

                getLogger().debug( "Need instance of " + repClass + 
                                   " to handle: " + protocol + type + model );

                try {
                    reply = (MailRepository) Class.forName(repClass).newInstance();
                    if (reply instanceof Configurable) {
                        ((Configurable) reply).configure(repConf);
                    }
                    if (reply instanceof Composer) {
                        ((Composer) reply).compose( componentManager );
                    }
/*                if (reply instanceof Contextualizable) {
                  ((Contextualizable) reply).contextualize(context);
                  }*/
                    if (reply instanceof Initializable) {
                        ((Initializable) reply).init();
                    }
                    repositories.put(repID, reply);
                    models.put(repID, model);
                    getLogger().info( "New instance of " + repClass + 
                                      " created for " + destination );
                    return (Component)reply;
                } catch (Exception e) {
                    getLogger().warn( "Exception while creating repository:" +
                                      e.getMessage(), e );

                    throw new 
                        ComponentNotAccessibleException( "Cannot find or init repository", e );
                }
            }
        } catch( final ConfigurationException ce ) {
            throw new ComponentNotAccessibleException( "Malformed configuration", ce );
        }
    }
        
    public static final String getName() {
        return REPOSITORY_NAME + id++;
    }
}
