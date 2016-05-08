/*
 * Copyright (C) 2013 Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.shop.util.rest;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.Principal;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Provider
@ApplicationScoped
@Priority(APPLICATION)
public class LogFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
    
    @Override
    public void filter(ContainerRequestContext requestCtx) throws IOException {
        LOGGER.debugf("URI: %s", requestCtx.getUriInfo().getAbsolutePath());
        LOGGER.debugf("Method: %s", requestCtx.getMethod());
        LOGGER.debugf("Acceptable Media Types: %s", requestCtx.getAcceptableMediaTypes());
        LOGGER.debugf("Content Type: %s", requestCtx.getHeaderString("content-type"));
        final SecurityContext securityCtx = requestCtx.getSecurityContext();
        if (securityCtx == null) {
            LOGGER.debug("Security Context: null");
        } else {
            LOGGER.debugf("Authentication Scheme: %s", securityCtx.getAuthenticationScheme());
            final Principal principal = securityCtx.getUserPrincipal();
            final String principalName = principal == null ? null : principal.getName();
            LOGGER.debugf("Principal: %s", principalName);
        }
        LOGGER.debugf("Authorization: %s", requestCtx.getHeaderString("authorization"));
        LOGGER.debugf("Acceptable Languages: %s", requestCtx.getAcceptableLanguages());
    }

    @Override
    public void filter(ContainerRequestContext requestCtx, ContainerResponseContext responseCtx) throws IOException {
        LOGGER.debugf("Status Info: %d %s", responseCtx.getStatus(), responseCtx.getStatusInfo());
        LOGGER.debugf("Location: %s", responseCtx.getLocation());        
    }
}
