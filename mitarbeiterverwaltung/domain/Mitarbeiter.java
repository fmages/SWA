/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shop.mitarbeiterverwaltung.domain;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
/**
 *
 * @author Julian
 */
public class Mitarbeiter {
    private static final String VORNAME_PATTERN = "[A-Z\u00C4\u00D6\u00DC][a-z\u00E4\u00F6\u00FC\u00DF]+";
    private static final String NACHNAME_PREFIX = "(o'|von|von der|von und zu|van)?";
    
    public static final String NACHNAME_PATTERN = NACHNAME_PREFIX + VORNAME_PATTERN + "(-" + VORNAME_PATTERN + ")?";
    private static final int VORNAME_LENGTH_MIN = 2;
    private static final int VORNAME_LENGTH_MAX = 32;
    private static final int NACHNAME_LENGTH_MIN = 2;
    private static final int NACHNAME_LENGTH_MAX = 32;
    
    
    @NotNull(message = "{mitarbeiter.Vorname.notNull}")
    @Size(min = VORNAME_LENGTH_MIN, max = VORNAME_LENGTH_MAX, message = "{Vorname.mitarbeiter.length}")
    @Pattern(regexp = "\\d{20}", message = "{mitarbeiter.Vorname.pattern}")
    private String Vorname;
    
    @NotNull(message = "{mitarbeiter.Nachname.notNull}")
    @Size(min = NACHNAME_LENGTH_MIN, max = NACHNAME_LENGTH_MAX, message = "{Nachmane.mitarbeiter.length}")
    @Pattern(regexp = "\\d{20}", message = "{mitarbeiter.Nachname}")
    private String Nachname;
    
    @NotNull(message = "{mitarbeiter.ID.notNull}")
    private UUID ID;
    private URI mitarbeiterUri;
    
    @NotNull(message = "{mitarbeiter.EmailAdresse.notNull}")
    @Pattern(regexp = "\\d{20}", message = "{mitarbeiter.EmailAdresse}")
    private String emailAdresse;

   
    public void setMitarbeiterUri(URI mitarbeiterUri) {
        this.mitarbeiterUri = mitarbeiterUri;
    }
    
     public String getVorname() {
        return Vorname;
    }
    public void setVorname(String vorname) {
        this.Vorname = vorname;
    }
    
    
    public String getNachname() {
        return Nachname;
    }
    public void setNachname(String Nachname) {
        this.Nachname = Nachname;
    }
    
    
    public UUID getId() {
        return ID;
    }

    public void setID(UUID ID) {
        this.ID = ID;
    }

    
    public String getEmailAdresse() {
        return emailAdresse;
    }

    public void setEmailAdresse(String emailAdresse) {
        this.emailAdresse = emailAdresse;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.Vorname);
        hash = 83 * hash + Objects.hashCode(this.Nachname);
        hash = 83 * hash + Objects.hashCode(this.ID);
        hash = 83 * hash + Objects.hashCode(this.mitarbeiterUri);
        hash = 83 * hash + Objects.hashCode(this.emailAdresse);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Mitarbeiter other = (Mitarbeiter) obj;
        if (!Objects.equals(this.Vorname, other.Vorname)) {
            return false;
        }
        if (!Objects.equals(this.Nachname, other.Nachname)) {
            return false;
        }
        if (!Objects.equals(this.emailAdresse, other.emailAdresse)) {
            return false;
        }
        if (!Objects.equals(this.ID, other.ID)) {
            return false;
        }
        if (!Objects.equals(this.mitarbeiterUri, other.mitarbeiterUri)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Mitarbeiter{" + "Vorname=" + Vorname + ", Nachname=" + Nachname + ", ID=" + ID + ", mitarbeiterUri=" + mitarbeiterUri + ", emailAdresse=" + emailAdresse + '}';
    }
    
}
