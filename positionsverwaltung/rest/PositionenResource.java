/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shop.positionsverwaltung.rest;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.rest.BestellungenResource;
import de.shop.positionsverwaltung.domain.Position;
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
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static de.shop.kundenverwaltung.rest.KundenResource.DELETE;
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
 *
 * @author Julian
 */
@Path("/position")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
public class PositionenResource {
    public static final String POSITIONEN_ID_PATH_PARAM = "id";
    public static final String BESTELLUNG_ID_PATH_PARAM = "id";
    
    public static final Method FIND_BY_ID;
    public static final Method FIND_BY_BESTELLUNG_ID;
    public static final Method DELETE;
    
    @Inject
    private UriHelper uriHelper;
    
    private final Mock mock = new Mock();
    
    static {
        try {
            FIND_BY_ID = PositionenResource.class.getMethod("findById", UUID.class, UriInfo.class);
            FIND_BY_BESTELLUNG_ID = PositionenResource.class.getMethod("findByBestellungId", UUID.class, UriInfo.class);
            DELETE = PositionenResource.class.getMethod("delete", UUID.class);
        } catch (NoSuchMethodException | SecurityException e){
            throw new ShopRuntimeException(e);
        }
    }
    
    @GET
    @Path("{" + POSITIONEN_ID_PATH_PARAM + ":" + UUID_PATTERN + "}")
    public Response findById(@PathParam(POSITIONEN_ID_PATH_PARAM) UUID id, @Context UriInfo uriInfo) {
        final Optional<Position> positionOpt = mock.findPositionById(id);
        if(!positionOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        
        final Position position = positionOpt.get();
        setStructuralLinks(position, uriInfo);
        
        return Response.ok(position)
                        .links(getTransitionalLinks(position, uriInfo))
                        .build();
    }
    
    @GET
    @Path("bestellung/{" + BESTELLUNG_ID_PATH_PARAM + ":[1-9]\\d*}")
    public Response findByBesttellungId(@PathParam(BESTELLUNG_ID_PATH_PARAM) UUID bestellungId, @Context UriInfo uriInfo) {
        final Optional<Bestellung> bestellungOpt = mock.findBestellungById(bestellungId);
        if(!bestellungOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        
        final Bestellung bestellung = bestellungOpt.get();
        final List<Position> positionen = mock.findPositionByBestellung(bestellung);
        if(positionen.isEmpty()) {
            return Response.status(NOT_FOUND).build();
        }
        
        positionen.forEach(p -> setStructuralLinks(p, uriInfo));
        
        return Response.ok(new GenericEntity<List<Position>>(positionen){})
                        .links(getTransitionalLinks(positionen, uriInfo))
                        .build();
    }
    
    @POST
    public Response save(@Valid Position position, @Context UriInfo uriInfo){
        final Position result = mock.savePosition(position);
        return Response.created(getUriPosition(result, uriInfo))
                        .build();   
    }
    
    @PUT
    public void update(@Valid Position position) {
        mock.updatePosition(position);
    }
    
    @DELETE
    @Produces
    @Consumes
    @Path("{id:" + UUID_PATTERN + "}")
    public void delete(@PathParam("id") UUID positionId){
        mock.deletePosition(positionId);
    }
    
    public URI getUriPosition(Position position, UriInfo uriInfo) {
        return uriHelper.getUri(PositionenResource.class, FIND_BY_ID, position.getId(), uriInfo);
    }
    
    public void setStructuralLinks(Position position, UriInfo uriInfo) {
        final Bestellung bestellung = position.getBestellung();
        if(bestellung != null) {
            final URI bestellungUri = uriHelper.getUri(BestellungenResource.class, BestellungenResource.FIND_BY_ID, bestellung.getId(), uriInfo);
            position.setBestellungUri(bestellungUri);
        }
        
        final Artikel artikel = position.getArtikel();
        if(artikel != null) {
            final URI artikelUri = uriHelper.getUri(ArtikelResource.class, ArtikelResource.FIND_BY_ID, artikel.getId(), uriInfo);
            position.setArtikelUri(artikelUri);
        }
    }
    
    private Link[] getTransitionalLinks(Position position, UriInfo uriInfo) {
        final Link self = Link.fromUri(getUriPosition(position, uriInfo))
                                .rel(SELF_LINK)
                                .build();
        
        final Link add = Link.fromUri(uriHelper.getUri(PositionenResource.class, uriInfo))
                                .rel(ADD_LINK)
                                .build();
        
         final Link update = Link.fromUri(uriHelper.getUri(PositionenResource.class, uriInfo))
                                .rel(UPDATE_LINK)
                                .build();

        final Link remove = Link.fromUri(uriHelper.getUri(PositionenResource.class, DELETE, position.getId(), uriInfo))
                                .rel(REMOVE_LINK)
                                .build();
        return new Link[] {self};
    }
    
    private Link[] getTransitionalLinks(List<Position> positionen, UriInfo uriInfo) {
        if(positionen == null || positionen.isEmpty()) {
            return new Link[0];
        }
        
        final Link first = Link.fromUri(getUriPosition(positionen.get(0), uriInfo))
                                .rel(FIRST_LINK)
                                .build();
        final int lastPos = positionen.size() -1;
        
        final Link last = Link.fromUri(getUriPosition(positionen.get(lastPos), uriInfo))
                                .rel(LAST_LINK)
                                .build();
        
        return new Link[] {first, last};
    }
    
}
