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

package de.shop.bestellverwaltung.rest;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.rest.KundenResource;
import de.shop.util.Mock;
import de.shop.util.ShopRuntimeException;
import de.shop.util.rest.UriHelper;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static de.shop.util.Constants.FIRST_LINK;
import static de.shop.util.Constants.LAST_LINK;
import static de.shop.util.Constants.SELF_LINK;
import static de.shop.util.Constants.UUID_PATTERN;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Path("/bestellungen")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
public class BestellungenResource {
    public static final String BESTELLUNGEN_ID_PATH_PARAM = "id";
    public static final String KUNDE_ID_PATH_PARAM = "id";
    
    public static final Method FIND_BY_ID;
    public static final Method FIND_BY_KUNDE_ID;
    
    @Inject
    private UriHelper uriHelper;
    
    private final Mock mock = new Mock();

    static {
        try {
            FIND_BY_ID = BestellungenResource.class.getMethod("findById", UUID.class, UriInfo.class);
            FIND_BY_KUNDE_ID = BestellungenResource.class.getMethod("findByKundeId", UUID.class, UriInfo.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }
    }

    @GET
    @Path("{" + BESTELLUNGEN_ID_PATH_PARAM + ":" + UUID_PATTERN + "}")
    public Response findById(@PathParam(BESTELLUNGEN_ID_PATH_PARAM) UUID id,
                             @Context UriInfo uriInfo) {
        // TODO Anwendungskern statt Mock
        final Optional<Bestellung> bestellungOpt = mock.findBestellungById(id);
        if (!bestellungOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        
        final Bestellung bestellung = bestellungOpt.get();
        setStructuralLinks(bestellung, uriInfo);
        
        // Link-Header setzen
        return Response.ok(bestellung)
                       .links(getTransitionalLinks(bestellung, uriInfo))
                       .build();
    }
    
        
    @GET
    @Path("kunde/{" + KUNDE_ID_PATH_PARAM + ":[1-9]\\d*}")
    public Response findByKundeId(@PathParam(KUNDE_ID_PATH_PARAM) UUID kundeId,
                                  @Context UriInfo uriInfo) {
        // TODO Anwendungskern statt Mock
        final Optional<AbstractKunde> kundeOpt = mock.findKundeById(kundeId);
        if (!kundeOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        
        final AbstractKunde kunde = kundeOpt.get();
        final List<Bestellung> bestellungen = mock.findBestellungenByKunde(kunde);
        if (bestellungen.isEmpty()) {
            return Response.status(NOT_FOUND).build();
        }
        
        // URIs innerhalb der gefundenen Bestellungen anpassen
        bestellungen.forEach(b -> setStructuralLinks(b, uriInfo));
        
        return Response.ok(new GenericEntity<List<Bestellung>>(bestellungen){})     //NOSONAR
                       .links(getTransitionalLinks(bestellungen, uriInfo))
                       .build();
    }

    
    //--------------------------------------------------------------------------
    // Methoden fuer URIs und Links
    //--------------------------------------------------------------------------
    public URI getUriBestellung(Bestellung bestellung, UriInfo uriInfo) {
        return uriHelper.getUri(BestellungenResource.class, FIND_BY_ID, bestellung.getId(), uriInfo);
    }
    
    public void setStructuralLinks(Bestellung bestellung, UriInfo uriInfo) {
        // URI fuer Kunde setzen
        final AbstractKunde kunde = bestellung.getKunde();
        if (kunde != null) {
            final URI kundeUri = uriHelper.getUri(KundenResource.class, KundenResource.FIND_BY_ID, kunde.getId(), uriInfo);
            bestellung.setKundeUri(kundeUri);
        }
    }
    
    private Link[] getTransitionalLinks(Bestellung bestellung, UriInfo uriInfo) {
        final Link self = Link.fromUri(getUriBestellung(bestellung, uriInfo))
                              .rel(SELF_LINK)
                              .build();
        return new Link[] { self };
    }
    
        
    private Link[] getTransitionalLinks(List<Bestellung> bestellungen, UriInfo uriInfo) {
        if (bestellungen == null || bestellungen.isEmpty()) {
            return new Link[0];
        }
   
        final Link first = Link.fromUri(getUriBestellung(bestellungen.get(0), uriInfo))
                               .rel(FIRST_LINK)
                               .build();
        final int lastPos = bestellungen.size() - 1;
        
        final Link last = Link.fromUri(getUriBestellung(bestellungen.get(lastPos), uriInfo))
                              .rel(LAST_LINK)
                              .build();
        
        return new Link[] { first, last };
    }
}
