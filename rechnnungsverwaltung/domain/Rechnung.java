/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shop.rechnnungsverwaltung.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.positionsverwaltung.domain.Position;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static de.shop.util.Constants.HASH_PRIME;

/**
 *
 * @author Julian
 */
public class Rechnung {
    
    private UUID id;
    
    private URI bestellungUri;
    
    @JsonIgnore
    private Bestellung bestellung;
    
    @JsonIgnore
    private AbstractKunde kunde;
    
    private URI kundeUri;
    
    private String status;

    public Rechnung() {
    }

    public double getGesamtpreis(){
        List<Position> positionen = bestellung.getPositionen();
        double sum = 0;
        for(Position p: positionen) {
            sum += p.getArtikel().getPreis();
        }
        return sum;
    }
    
    public UUID getId() {
        return id;
    }

    public URI getBestellungUri() {
        return bestellungUri;
    }

    public Bestellung getBestellung() {
        return bestellung;
    }

    public AbstractKunde getKunde() {
        return kunde;
    }

    public URI getKundeUri() {
        return kundeUri;
    }

    public String getStatus() {
        return status;
    }
    
    public void setBestellungUri(URI bestellungUri) {
        this.bestellungUri = bestellungUri;
    }

    public void setBestellung(Bestellung bestellung) {
        this.bestellung = bestellung;
    }

    public void setKunde(AbstractKunde kunde) {
        this.kunde = kunde;
    }

    public void setKundeUri(URI kundeUri) {
        this.kundeUri = kundeUri;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public int hashCode() {
        final int prime = HASH_PRIME;
        return prime + Objects.hashCode(id);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Rechnung other = (Rechnung) obj;
        return Objects.equals(id, other.id);
    }
    
    @Override
    public String toString() {
        return "Rechnung {id=" + id + ", status=" + status + ", kundeUri=" + kundeUri + ", bestellungUri" + bestellungUri +'}';
    }
    
}
