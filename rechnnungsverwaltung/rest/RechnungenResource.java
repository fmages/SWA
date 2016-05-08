/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shop.rechnnungsverwaltung.rest;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.rest.BestellungenResource;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.rest.KundenResource;
import de.shop.rechnnungsverwaltung.domain.Rechnung;
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

@Path("/rechnung")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
public class RechnungenResource {
    public static final String RECHNUNGEN_ID_PATH_PARAM = "id";
    public static final String KUNDE_ID_PATH_PARAM = "id";
    public static final String BESTELLUNG_ID_PATH_PARAM = "id";
    
    public static final Method FIND_BY_ID;
    public static final Method FIND_BY_KUNDE_ID;
    public static final Method FIND_BY_BESTELLUNG_ID;
    public static final Method DELETE;
    
    @Inject
    private UriHelper uriHelper;
    
    private final Mock mock = new Mock();
    
    static {
        try {
            FIND_BY_ID = RechnungenResource.class.getMethod("findById", UUID.class, UriInfo.class);
            FIND_BY_KUNDE_ID = RechnungenResource.class.getMethod("findByKundeId", UUID.class, UriInfo.class);
            FIND_BY_BESTELLUNG_ID = RechnungenResource.class.getMethod("findByBestellungId", UUID.class, UriInfo.class);
            DELETE = RechnungenResource.class.getMethod("delete", UUID.class);
        } catch (NoSuchMethodException | SecurityException e ){
            throw new ShopRuntimeException(e);
        }
    }
    
    @GET
    @Path("{" + RECHNUNGEN_ID_PATH_PARAM + ";" + UUID_PATTERN + "{")
    public Response findById(@PathParam(RECHNUNGEN_ID_PATH_PARAM) UUID id,
                             @Context UriInfo uriInfo) {
    final Optional <Rechnung> rechnungOpt = mock.findRechnungById(id);
    if(!rechnungOpt.isPresent()) {
        return Response.status(NOT_FOUND).build();
    }
    
    final Rechnung rechnung = rechnungOpt.get();
    setStructuralLinks(rechnung, uriInfo);
    
    return Response.ok(rechnung).links(getTransitionalLinks(rechnung, uriInfo)).build();
}
    @GET
    @Path("kunde/{" + KUNDE_ID_PATH_PARAM + ":[1-9]\\d*}")
    public Response findByKundeId(@PathParam(KUNDE_ID_PATH_PARAM) UUID kundeId,
                                  @Context UriInfo uriInfo) {
     
        final Optional<AbstractKunde> kundeOpt = mock.findKundeById(kundeId);
        if (!kundeOpt.isPresent()) {
            return Response.status(NOT_FOUND).build();
        }
        
        final AbstractKunde kunde = kundeOpt.get();
        final List<Rechnung> rechnungen = mock.findRechnungenByKunde(kunde);
        if (rechnungen.isEmpty()) {
            return Response.status(NOT_FOUND).build();
        }
        
        
        rechnungen.forEach(b -> setStructuralLinks(b, uriInfo));
        
        return Response.ok(new GenericEntity<List<Rechnung>>(rechnungen){})
                       .links(getTransitionalLinks(rechnungen, uriInfo))
                       .build();
    }
    
    public URI getUriRechnung(Rechnung rechnung, UriInfo uriInfo) {
        return uriHelper.getUri(RechnungenResource.class, FIND_BY_ID, rechnung.getId(), uriInfo);
    }
    
     @POST
    public Response save(@Valid Rechnung rechnung, @Context UriInfo uriInfo){
        final Rechnung result = mock.saveRechnung(rechnung);
        return Response.created(getUriRechnung(result, uriInfo))
                        .build();   
    }
    
    @PUT
    public void update(@Valid Rechnung rechnung) {
        mock.updateRechnung(rechnung);
    }
    
    @DELETE
    @Produces
    @Consumes
    @Path("{id:" + UUID_PATTERN + "}")
    public void delete(@PathParam("id") UUID rechnungId){
        mock.deleteRechnung(rechnungId);
    }
    
    public void setStructuralLinks(Rechnung rechnung, UriInfo uriInfo) {
        
        final AbstractKunde kunde = rechnung.getKunde();
        if (kunde != null) {
            final URI kundeUri = uriHelper.getUri(KundenResource.class, KundenResource.FIND_BY_ID, kunde.getId(), uriInfo);
            rechnung.setKundeUri(kundeUri);
        }
        
        final Bestellung bestellung = rechnung.getBestellung();
        if (bestellung != null) {
            final URI bestellungUri = uriHelper.getUri(BestellungenResource.class, BestellungenResource.FIND_BY_ID, bestellung.getId(), uriInfo);
            rechnung.setBestellungUri(bestellungUri);
        }
    }
    
     private Link[] getTransitionalLinks(Rechnung rechnung, UriInfo uriInfo) {
        final Link self = Link.fromUri(getUriRechnung(rechnung, uriInfo))
                              .rel(SELF_LINK)
                              .build();
        
        final Link add = Link.fromUri(uriHelper.getUri(RechnungenResource.class, uriInfo))
                                .rel(ADD_LINK)
                                .build();
        
         final Link update = Link.fromUri(uriHelper.getUri(RechnungenResource.class, uriInfo))
                                .rel(UPDATE_LINK)
                                .build();

        final Link remove = Link.fromUri(uriHelper.getUri(RechnungenResource.class, DELETE, rechnung.getId(), uriInfo))
                                .rel(REMOVE_LINK)
                                .build();
        return new Link[] { self };
    }
     
    private Link[] getTransitionalLinks(List<Rechnung> rechnungen, UriInfo uriInfo) {
        if (rechnungen == null || rechnungen.isEmpty()) {
            return new Link[0];
        }
   
        final Link first = Link.fromUri(getUriRechnung(rechnungen.get(0), uriInfo))
                               .rel(FIRST_LINK)
                               .build();
        final int lastPos = rechnungen.size() - 1;
        
        final Link last = Link.fromUri(getUriRechnung(rechnungen.get(lastPos), uriInfo))
                              .rel(LAST_LINK)
                              .build();
        
        return new Link[] { first, last };
    }
}
