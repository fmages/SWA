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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static de.shop.util.Constants.HASH_PRIME;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class Adresse {
    private static final int ORT_LENGTH_MIN = 2;
    private static final int ORT_LENGTH_MAX = 32;
    
    private static final int STRASSE_LENGTH_MIN = 2;
    private static final int STRASSE_LENGTH_MAX = 40;

    private UUID id;
    
    @NotNull(message = "{adresse.plz.notNull}")
    @Pattern(regexp = "\\d{5}", message = "{adresse.plz}")
    private String plz;
    
    @NotNull(message = "{adresse.ort.notNull}")
    @Size(min = ORT_LENGTH_MIN, max = ORT_LENGTH_MAX, message = "{adresse.ort.length}")
    private String ort;
    
    @NotNull(message = "{adresse.strasse.notNull}")
    @Size(min = STRASSE_LENGTH_MIN, max = STRASSE_LENGTH_MAX, message = "{adresse.ort.length}")
    private String strasse;
    
    @NotNull(message = "{adresse.hausnr.notNull}")
    @Pattern(regexp = "/d{1,4}+[a-z]?")
    private String hausnr;
    
    //NICHT @NotNull, weil beim Anlegen ueber REST der Rueckwaertsverweis noch nicht existiert
    @JsonIgnore
    private AbstractKunde kunde;
    
    public UUID getId() {
        return id;
    }

    public String getPlz() {
        return plz;
    }
    
    public void setPlz(String plz) {
        this.plz = plz;
    }
    
    public String getOrt() {
        return ort;
    }
    
    public void setOrt(String ort) {
        this.ort = ort;
    }
    
    public AbstractKunde getKunde() {
        return kunde;
    }
    
    public void setKunde(AbstractKunde kunde) {
        this.kunde = kunde;
    }
    
     public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }

    public String getHausnr() {
        return hausnr;
    }

    public void setHausnr(String hausnr) {
        this.hausnr = hausnr;
    }
     
    
    @Override
    public int hashCode() {
        final int prime = HASH_PRIME;
        int result = prime + Objects.hashCode(id);
        result = prime * result + Objects.hashCode(ort);
        return prime * result + Objects.hashCode(plz);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Adresse other = (Adresse) obj;
        if (!Objects.equals(id, other.id)) {
            return false;
        }
        if (!Objects.equals(ort, other.ort)) {
            return false;
        }
        if (!Objects.equals(strasse, other.strasse)) {
            return false;
        }
        if (!Objects.equals(hausnr, other.hausnr)) {
            return false;
        }
        return Objects.equals(plz, other.plz);
    }
    
    @Override
    public String toString() {
        return "Adresse {id=" + id + ", strasse=" + strasse +", hausnr=" + hausnr +", plz=" + plz + ", ort=" + ort + '}';
    }
}
