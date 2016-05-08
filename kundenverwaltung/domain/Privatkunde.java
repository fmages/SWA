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

import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static de.shop.kundenverwaltung.domain.AbstractKunde.NACHNAME_PATTERN;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class Privatkunde extends AbstractKunde {
    private static final long serialVersionUID = -3177911520687689458L;

    public static final String DISKRIMINATOR_VALUE = "P";
    
     private static final String NAME_PATTERN = "[A-Z\u00C4\u00D6\u00DC][a-z\u00E4\u00F6\u00FC\u00DF]+";
    private static final String VORNAME_PREFIX = "(o'|von|von der|von und zu|van)?";
    
    public static final String VORNAME_PATTERN = VORNAME_PREFIX + NAME_PATTERN + "(-" + NAME_PATTERN + ")?";
    private static final int VORNAME_LENGTH_MIN = 2;
    private static final int VORNAME_LENGTH_MAX = 32;
    
    @NotNull(message = "{kunde.vorname.notNull}")
    @Size(min = VORNAME_LENGTH_MIN, max = VORNAME_LENGTH_MAX,
          message = "{kunde.vorname.length}")
    @Pattern(regexp = VORNAME_PATTERN, message = "{kunde.vorname.pattern}")
    private String vorname;

    public String getVorname() {
        return vorname;
    }
    
    public void setVorname(String vorname) {
        this.vorname = vorname;
    }
    
    private Set<HobbyType> hobbys;

    public Set<HobbyType> getHobbys() {
        return hobbys;
    }
    public void setHobbys(Set<HobbyType> hobbys) {
        this.hobbys = hobbys;
    }
    @Override
    public String toString() {
        return "Privatkunde {" + super.toString() + ", vorname=" + vorname + ", hobbys=" + hobbys + '}';
    }
}
