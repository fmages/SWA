/*
 * Copyright (C) 2013 - 2016 Juergen Zimmermann, Hochschule Karlsruhe
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

package de.shop.kundenverwaltung.rest;

import de.shop.bestellverwaltung.rest.BestellungenResource;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.Mock;
import de.shop.util.ShopRuntimeException;
import de.shop.util.rest.UriHelper;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static de.shop.kundenverwaltung.domain.AbstractKunde.NACHNAME_PATTERN;
import static de.shop.util.Constants.ADD_LINK;
import static de.shop.util.Constants.FIRST_LINK;
import static de.shop.util.Constants.LAST_LINK;
import static de.shop.util.Constants.REMOVE_LINK;
import static de.shop.util.Constants.SELF_LINK;
import static de.shop.util.Constants.UPDATE_LINK;
import static de.shop.util.Constants.UUID_PATTERN;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Path("/kunden")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
public class KundenResource {
    public static final String KUNDEN_ID_PATH_PARAM = "kundeId";
    public static final String KUNDEN_NACHNAME_QUERY_PARAM = "nachname";
    
    public static final Method FIND_BY_ID;
    public static final Method DELETE;
    
    @Inject
    private UriHelper uriHelper;
    
    private final Mock mock = new Mock();
    
    static {
        try {
            FIND_BY_ID = KundenResource.class.getMethod("findById", UUID.class, UriInfo.class);
            DELETE = KundenResource.class.getMethod("delete", UUID.class);
                    
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Path("version")
    public String getVersion() {
        return "{version: \"1.0\"}";
    }
    
    @GET
    @Path("{" + KUNDEN_ID_PATH_PARAM + ":" + UUID_PATTERN + "}")
    public Response findById(@PathParam(KUNDEN_ID_PATH_PARAM) UUID id,
                             @Context UriInfo uriInfo) {
        // TODO Anwendungskern statt Mock
        final Optional<AbstractKunde> kundeOpt = mock.findKundeById(id);
        if (!kundeOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        
        final AbstractKunde kunde = kundeOpt.get();
        setStructuralLinks(kunde, uriInfo);
        
        return Response.ok(kunde)
                       .links(getTransitionalLinks(kunde, uriInfo))
                       .build();
    }
    
    @GET
    public Response findByNachname(@Pattern(regexp = NACHNAME_PATTERN, message = "{kunde.nachname.pattern}")
                                   @QueryParam(KUNDEN_NACHNAME_QUERY_PARAM)
                                   String nachname,
                                   @Context UriInfo uriInfo) {
        final List<AbstractKunde> kunden = nachname != null
                                           ? mock.findKundenByNachname(nachname)
                                           : mock.findAllKunden();
        if (kunden.isEmpty()) {
            return Response.status(NOT_FOUND).build();
        }
        
        kunden.forEach(k -> setStructuralLinks(k, uriInfo));
        
        return Response.ok(new GenericEntity<List<? extends AbstractKunde>>(kunden){})   //NOSONAR
                       .links(getTransitionalLinksKunden(kunden, uriInfo))
                       .build();
    }

    
    @POST
    public Response save(@Valid AbstractKunde kunde, @Context UriInfo uriInfo) {
        // TODO Anwendungskern statt Mock
        final AbstractKunde result = mock.saveKunde(kunde);
        return Response.created(getUriKunde(result, uriInfo))
                       .build();
    }
    
    @PUT
    public void update(@Valid AbstractKunde kunde) {
        // TODO Anwendungskern statt Mock
        mock.updateKunde(kunde);
    }

    @PUT
    @Path("{id:" + UUID_PATTERN + "}/bestellungen")
    public void update(@PathParam("id") UUID kundeId, @Valid AbstractKunde kunde) {
        if (Objects.equals(kundeId, kunde.getId())) {
            update(kunde);
        }
    }
    
    @DELETE
    @Produces
    @Consumes
    @Path("{id:" + UUID_PATTERN + "}")
    public void delete(@PathParam("id") UUID kundeId) {               //NOSONAR
        // TODO Anwendungskern statt Mock
        mock.deleteKunde(kundeId);
    }
    
    //--------------------------------------------------------------------------
    // Methoden fuer URIs und Links
    //--------------------------------------------------------------------------
    public URI getUriKunde(AbstractKunde kunde, UriInfo uriInfo) {
        return uriHelper.getUri(KundenResource.class, FIND_BY_ID, kunde.getId(), uriInfo);
    }

    private URI getUriBestellungen(AbstractKunde kunde, UriInfo uriInfo) {
        return uriHelper.getUri(BestellungenResource.class, BestellungenResource.FIND_BY_KUNDE_ID, kunde.getId(), uriInfo);
    }        
    
    public void setStructuralLinks(AbstractKunde kunde, UriInfo uriInfo) {
        // URI fuer Bestellungen setzen
        final URI uri = getUriBestellungen(kunde, uriInfo);
        kunde.setBestellungenUri(uri);
    }
    
    public Link[] getTransitionalLinks(AbstractKunde kunde, UriInfo uriInfo) {
        final Link self = Link.fromUri(getUriKunde(kunde, uriInfo))
                              .rel(SELF_LINK)
                              .build();
        
        final Link add = Link.fromUri(uriHelper.getUri(KundenResource.class, uriInfo))
                             .rel(ADD_LINK)
                             .build();

        final Link update = Link.fromUri(uriHelper.getUri(KundenResource.class, uriInfo))
                                .rel(UPDATE_LINK)
                                .build();

        final Link remove = Link.fromUri(uriHelper.getUri(KundenResource.class, DELETE, kunde.getId(), uriInfo))
                                .rel(REMOVE_LINK)
                                .build();
        
        return new Link[] { self, add, update, remove };
    }
    
    private Link[] getTransitionalLinksKunden(List<? extends AbstractKunde> kunden, UriInfo uriInfo) {
        if (kunden == null || kunden.isEmpty()) {
            return new Link[0];
        }
        
        final Link first = Link.fromUri(getUriKunde(kunden.get(0), uriInfo))
                               .rel(FIRST_LINK)
                               .build();
        final int lastPos = kunden.size() - 1;
        final Link last = Link.fromUri(getUriKunde(kunden.get(lastPos), uriInfo))
                              .rel(LAST_LINK)
                              .build();
        
        return new Link[] { first, last };
    }
}
