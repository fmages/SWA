/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shop.positionsverwaltung.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.artikelverwaltung.domain.Artikel;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;

import static com.google.common.base.CharMatcher.is;
import static de.shop.util.Constants.HASH_PRIME;
/**
 *
 * @author Julian
 */
public class Position {
     private UUID id;
    
    @NotNull(message = "{position.bestellung.notNull")
    @JsonIgnore
    private Bestellung bestellung;
    
    private URI bestellungUri;
    
    @JsonIgnore
    private Artikel artikel;
    
    private URI artikelUri;
    
    private int anzahl;

    public UUID getId() {
        return id;
    }

    public Bestellung getBestellung() {
        return bestellung;
    }

    public URI getBestellungUri() {
        return bestellungUri;
    }

    public Artikel getArtikel() {
        return artikel;
    }

    public void setArtikelUri(URI artikelUri) {
        this.artikelUri = artikelUri;
    }

    public URI getArtikelUri() {
        return artikelUri;
    }

    public int getAnzahl() {
        return anzahl;
    }
    
    public double getGesamtpreis() {
        return anzahl * artikel.getPreis();
    }

    public void setBestellung(Bestellung bestellung) {
        this.bestellung = bestellung;
    }

    public void setBestellungUri(URI bestellungUri) {
        this.bestellungUri = bestellungUri;
    }

    public void setArtikel(Artikel artikel) {
        this.artikel = artikel;
    }

    public void setAnzahl(int anzahl) {
        this.anzahl = anzahl;
    }
    
    @Override
    public int hashCode() {
        final int prime = HASH_PRIME;
        return prime + Objects.hashCode(id);
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Position other = (Position) obj;
        return Objects.equals(id, other.id);
    }
    
    @Override
    public String toString() {
        return "Position {id=" + id + ", bestellungUri=" + bestellungUri + ", artikel= {" + artikel.ToString() + "}, anzahl=" + anzahl + ", gesamtpreis=" + getGesamtpreis();
    }
}
