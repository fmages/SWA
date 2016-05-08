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

package de.shop.kundenverwaltung.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.rechnnungsverwaltung.domain.Rechnung;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;
import static de.shop.util.Constants.EMAIL_PATTERN;
import static de.shop.util.Constants.HASH_PRIME;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@JsonTypeInfo(use = NAME, property = AbstractKunde.DISKRIMINATOR)
@JsonSubTypes({
    @Type(value = Privatkunde.class, name = Privatkunde.DISKRIMINATOR_VALUE),
    @Type(value = Firmenkunde.class, name = Firmenkunde.DISKRIMINATOR_VALUE)
})
public abstract class AbstractKunde {
    public static final String DISKRIMINATOR = "typ";
    
    //Pattern mit UTF-8 (statt Latin-1 bzw. ISO-8859-1) Schreibweise fuer Umlaute:
    private static final String NAME_PATTERN = "[A-Z\u00C4\u00D6\u00DC][a-z\u00E4\u00F6\u00FC\u00DF]+";
    private static final String NACHNAME_PREFIX = "(o'|von|von der|von und zu|van)?";
    
    public static final String NACHNAME_PATTERN = NACHNAME_PREFIX + NAME_PATTERN + "(-" + NAME_PATTERN + ")?";
    private static final int NACHNAME_LENGTH_MIN = 2;
    private static final int NACHNAME_LENGTH_MAX = 32;
    private static final int EMAIL_LENGTH_MAX = 128;
    
    private UUID id;
    
    @NotNull(message = "{kunde.nachname.notNull}")
    @Size(min = NACHNAME_LENGTH_MIN, max = NACHNAME_LENGTH_MAX,
          message = "{kunde.nachname.length}")
    @Pattern(regexp = NACHNAME_PATTERN, message = "{kunde.nachname.pattern}")
    private String nachname;
    
    @Pattern(regexp = EMAIL_PATTERN, message = "{kunde.email.pattern}")
    @NotNull(message = "{kunde.email.notNull}")
    @Size(max = EMAIL_LENGTH_MAX, message = "{kunde.email.length}")
    private String email;
    
    @Valid
    @NotNull(message = "{kunde.adresse.notNull}")
    private Adresse adresse;
    
    private List<Bestellung> bestellungen;
    
    private List<Rechnung> rechnungen;
    
    private URI bestellungenUri;

    public UUID getId() {
        return id;
    }

    public String getNachname() {
        return nachname;
    }
    
    public void setNachname(String nachname) {
        this.nachname = nachname;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Adresse getAdresse() {
        return adresse;
    }
    
    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }
    
    public List<Bestellung> getBestellungen() {
        return bestellungen;
    }
    
    public void setBestellungen(List<Bestellung> bestellungen) {
        this.bestellungen = bestellungen;
    }

    public URI getBestellungenUri() {
        return bestellungenUri;
    }
    
    public void setBestellungenUri(URI bestellungenUri) {
        this.bestellungenUri = bestellungenUri;
    }

    public List<Rechnung> getRechnungen() {
        return rechnungen;
    }

    public void setRechnungen(List<Rechnung> rechnungen) {
        this.rechnungen = rechnungen;
    }
     
    @Override
    public int hashCode() {
        return HASH_PRIME + Objects.hashCode(email);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AbstractKunde other = (AbstractKunde) obj;
        return Objects.equals(email, other.email);
    }
    
    @Override
    public String toString() {
        return "AbstractKunde {id=" + id + ", nachname=" + nachname + ", email=" + email
               + ", bestellungenUri=" + bestellungenUri + '}';
    }
}
