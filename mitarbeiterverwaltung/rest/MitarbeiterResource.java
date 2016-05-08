/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shop.mitarbeiterverwaltung.rest;
import de.shop.util.Mock;
import de.shop.mitarbeiterverwaltung.domain.Mitarbeiter;


import de.shop.util.ShopRuntimeException;
import de.shop.util.rest.UriHelper;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Objects;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import de.shop.util.Mock;
import java.net.URI;
import javax.ws.rs.core.Link;

import static de.shop.mitarbeiterverwaltung.domain.Mitarbeiter.NACHNAME_PATTERN;
import static de.shop.util.Constants.ADD_LINK;
import static de.shop.util.Constants.FIRST_LINK;
import static de.shop.util.Constants.LAST_LINK;
import static de.shop.util.Constants.REMOVE_LINK;
import static de.shop.util.Constants.SELF_LINK;
import static de.shop.util.Constants.UPDATE_LINK;
import static de.shop.util.Constants.UUID_PATTERN;
import static javafx.scene.input.KeyCode.M;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;


/**
 *
 * @author Julianderpro
 */
@Path("/Mitarbeiter")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
public class MitarbeiterResource {
    public static final String MITARBEITER_ID_PATH_PARAM = "MitarbeiterID";
    public static final String MITARBEITER_NACHNAME_QUERY_PARAM = "nachname";
    public static final String MITARBEITER_VORNAME_QUERY_PARAM = "vorname";
    
    
    //public static final String MITARBEITER_ID_PATH_PARAM = "mitarbeiterid";
    public static final Method FIND_BY_ID;
    public static final Method DELETE;
    
    @Inject
    private UriHelper uriHelper; // für Mitarbeiterliste zu bekommen
    
    private final Mock mock = new Mock();
    
    static {
        try {
            FIND_BY_ID = MitarbeiterResource.class.getMethod("findById", UUID.class, UriInfo.class);
            DELETE = MitarbeiterResource.class.getMethod("delete", UUID.class);
                    
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }
    }
    
    
    @GET
    @Path("{" + MITARBEITER_ID_PATH_PARAM + ":" + UUID_PATTERN +"}")
    public Response findByID(@PathParam(MITARBEITER_ID_PATH_PARAM)UUID id,@Context UriInfo uriinfo){
     
        //TODO Anwendungskern statt Mock
        final Optional <Mitarbeiter> mitarbeiteropt = mock.findMitarbeiterById(id);
        if(!mitarbeiteropt.isPresent()){
        return Response.status(NOT_FOUND).build();
    
    }
        
       final Mitarbeiter mitarbeiter = mitarbeiteropt.get();
        setStructuralLinks(mitarbeiter, uriinfo);
        
        return Response.ok(mitarbeiter).links(getTransitionalLinks(mitarbeiter, uriinfo)).build();
        
    }
    
    
    @GET
    public Response findByNachname(@Pattern
                                    (regexp = NACHNAME_PATTERN,message = "{Mitarbeiter.Nachname.pattern}")
                                    @QueryParam(MITARBEITER_ID_PATH_PARAM)
    String Nachname,
            @Context UriInfo uriInfo){
        final List<Mitarbeiter> mitarbeiter = Nachname !=null
                ? mock.findMitarbeiterByNachname(Nachname)
                : mock.findAllMitarbeiter();
        
        if (mitarbeiter.isEmpty()){
            return Response.status(NOT_FOUND).build();
        }
        
        mitarbeiter.forEach(m -> setStructuralLinks(m, uriInfo));
        
        return Response.ok(mitarbeiter)
                .links(getTransitionalLinksMitarbeiter(mitarbeiter, uriInfo)).build();
    }
                                       
    
    @POST
    public Response save(@Valid Mitarbeiter mitarbeiter, @Context UriInfo uriInfo){
        //TODO Anwendungskern statt Mock
        final Mitarbeiter result = mock.saveMitarbeiter(mitarbeiter);
        return Response.created(getUriMitarbeiter(result, uriInfo)).build();
    }
    
       
    @PUT
    public void update(@Valid Mitarbeiter mitarbeiter){
        //TODO Anwendungskern statt Mock
        
        mock.updateMitarbeiter(mitarbeiter);
    }
    
    @PUT
    @Path("{id:" + UUID_PATTERN +"}/mitarbeiter")
    public void update(@PathParam("id") UUID mitarbeiterId, @Valid Mitarbeiter mitarbeiter){
        if(Objects.equals(mitarbeiterId,mitarbeiter.getId())){
            update(mitarbeiter);
        }
    }
    
    
    @DELETE
    @Produces
    @Consumes
    @Path("{id:" + UUID_PATTERN + "}")
    public void delete(@PathParam("id") UUID mitarbeiterId){
        mock.deleteMitarbeiter(mitarbeiterId);
    }
    
 
    //--------------------------------------------------------------------------
    // Methoden fuer URIs und Links
    //--------------------------------------------------------------------------
    public URI getUriMitarbeiter(Mitarbeiter mitarbeiter, UriInfo uriInfo) {
        return uriHelper.getUri(MitarbeiterResource.class, FIND_BY_ID, mitarbeiter.getId(), uriInfo);
    }

        
    public void setStructuralLinks(Mitarbeiter mitarbeiter, UriInfo uriInfo) {
        //URI fuer Mitarbeiter setzen
        final URI uri = getUriMitarbeiter(mitarbeiter, uriInfo);
        mitarbeiter.setMitarbeiterUri(uri);
    }
    
    public Link[] getTransitionalLinks(Mitarbeiter mitarbeiter, UriInfo uriInfo) {
        final Link self = Link.fromUri(getUriMitarbeiter(mitarbeiter, uriInfo))
                              .rel(SELF_LINK)
                              .build();
        
        final Link add = Link.fromUri(uriHelper.getUri(MitarbeiterResource.class, uriInfo))
                             .rel(ADD_LINK)
                             .build();

        final Link update = Link.fromUri(uriHelper.getUri(MitarbeiterResource.class, uriInfo))
                                .rel(UPDATE_LINK)
                                .build();

        final Link remove = Link.fromUri(uriHelper.getUri(MitarbeiterResource.class, DELETE, mitarbeiter.getId(), uriInfo))
                                .rel(REMOVE_LINK)
                                .build();
        
        return new Link[] { self, add, update, remove };
    }
    
    private Link[] getTransitionalLinksMitarbeiter(List<Mitarbeiter> mitarbeiter, UriInfo uriInfo) {
        if (mitarbeiter == null || mitarbeiter.isEmpty()) {
            return new Link[0];
        }
        
        final Link first = Link.fromUri(getUriMitarbeiter(mitarbeiter.get(0), uriInfo))
                               .rel(FIRST_LINK)
                               .build();
        final int lastPos = mitarbeiter.size() - 1;
        final Link last = Link.fromUri(getUriMitarbeiter(mitarbeiter.get(lastPos), uriInfo))
                              .rel(LAST_LINK)
                              .build();
        
        return new Link[] { first, last };
    }
}
