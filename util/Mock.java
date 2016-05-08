/*
 * Copyright (C) 2013 - 2015 Juergen Zimmermann, Hochschule Karlsruhe
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

package de.shop.util;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.Firmenkunde;
import de.shop.kundenverwaltung.domain.HobbyType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.mitarbeiterverwaltung.domain.Mitarbeiter;
import de.shop.positionsverwaltung.domain.Position;
import de.shop.rechnnungsverwaltung.domain.Rechnung;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.lang.System.out;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;

/**
 * Emulation des Anwendungskerns
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class Mock {
    private static final long MAX_ID = 0xFFF_000_000_000L;
    private static final int MAX_KUNDEN = 8;
    private static final int MAX_BESTELLUNGEN = 4;
    private static final int MAX_POSITIONEN = 20;
    private static final int MAX_RECHNUNGEN = 4;
    private static final int MAX_MITARBEITER = 10;
    
    private static final List<String> NACHNAMEN = asList("Alpha", "Beta", "Gamma", "Delta", "Epsilon");
    private static final Random RANDOM = new Random();

    public Optional<AbstractKunde> findKundeById(UUID id) {
        return findKundeById(id, true);
    }
    
    private Optional<AbstractKunde> findKundeById(UUID id, boolean checkId) {
        final String idStr = id.toString();
        final long tmp = Long.decode("0x" + idStr.substring(idStr.length() - 12));
        if (checkId && tmp > MAX_ID) {
            return empty();
        }
        
        final AbstractKunde kunde = tmp % 2 == 1 ? new Privatkunde() : new Firmenkunde();   //NOSONAR
        
        // Das private Attribut "id" setzen, ohne dass es eine set-Methode gibt
        try {
            final Field idField = AbstractKunde.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(kunde, id);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }
        
        int idx = RANDOM.nextInt() % NACHNAMEN.size();
        if (idx < 0) {
            idx += NACHNAMEN.size();
        }
        final String nachname = NACHNAMEN.get(idx);
        kunde.setNachname(nachname);
        kunde.setEmail(nachname + "@hska.de");
        
        saveAdresse(randomUUID(), kunde);
        
        if (kunde.getClass().equals(Privatkunde.class)) {
            final Privatkunde privatkunde = (Privatkunde) kunde;
            final Set<HobbyType> hobbys = new HashSet<>();
            hobbys.add(HobbyType.LESEN);
            hobbys.add(HobbyType.REISEN);
            privatkunde.setHobbys(hobbys);
        }
        
        return of(kunde);
    }

    private static void saveAdresse(UUID id, AbstractKunde kunde) {
        final Adresse adresse = new Adresse();
        // Das private Attribut "id" setzen, ohne dass es eine set-Methode gibt
        try {
            final Field idField = Adresse.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(adresse, id);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }
        
        adresse.setPlz("12345");
        adresse.setOrt("Testort");

        adresse.setKunde(kunde);
        kunde.setAdresse(adresse);
    }

    public List<AbstractKunde> findAllKunden() {
        final int anzahl = MAX_KUNDEN;
        final List<AbstractKunde> kunden = new ArrayList<>(anzahl);
        IntStream.rangeClosed(1, anzahl)
                 .forEach(unused -> {
            final AbstractKunde kunde = findKundeById(randomUUID(), false).get();
            kunden.add(kunde);            
        });
        return kunden;
    }

    public List<AbstractKunde> findKundenByNachname(String nachname) {
        final int anzahl = nachname.length();
        final List<AbstractKunde> kunden = new ArrayList<>(anzahl);
        IntStream.rangeClosed(1, anzahl)
                 .forEach(unused -> {
            final AbstractKunde kunde = findKundeById(randomUUID(), false).get();
            kunde.setNachname(nachname);
            kunden.add(kunde);            
        });
        return kunden;
    }
    

    public List<Bestellung> findBestellungenByKunde(AbstractKunde kunde) {
        // Beziehungsgeflecht zwischen Kunde und Bestellungen aufbauen:
        // 1, 2, 3 oder 4 Bestellungen
        final int anzahl = (int) (kunde.getId().getLeastSignificantBits() % MAX_BESTELLUNGEN) + 1;
        final List<Bestellung> bestellungen = new ArrayList<>(anzahl);
        IntStream.rangeClosed(1, anzahl)
                 .forEach(unused -> {
            final Bestellung bestellung = findBestellungById(randomUUID()).get();
            bestellung.setKunde(kunde);
            bestellungen.add(bestellung);            
        });
        kunde.setBestellungen(bestellungen);
        
        return bestellungen;
    }

    public Optional<Bestellung> findBestellungById(UUID id) {
        // andere ID fuer den Kunden
        final AbstractKunde kunde = findKundeById(randomUUID(), false).get();

        final Bestellung bestellung = new Bestellung();

        // Das private Attribut "id" setzen, ohne dass es eine set-Methode gibt
        try {
            final Field idField = Bestellung.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(bestellung, id);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }

        bestellung.setAusgeliefert(false);
        bestellung.setKunde(kunde);
        
        return of(bestellung);
    }
      
    
    public AbstractKunde saveKunde(AbstractKunde kunde) {
        // Neue IDs fuer Kunde und zugehoerige Adresse
        // Ein neuer Kunde hat auch keine Bestellungen
        // SecureRandom ist eigentlich sicherer, aber auch l0x langsamer (hier: nur Mock-Klasse)
        final UUID id = randomUUID();

        // Das private Attribut "id" setzen, ohne dass es eine set-Methode gibt
        try {
            final Field idField = AbstractKunde.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(kunde, id);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }

        final Adresse adresse = kunde.getAdresse();
        // Das private Attribut "id" setzen, ohne dass es eine set-Methode gibt
        try {
            final Field idField = Adresse.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(adresse, randomUUID());
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }
        adresse.setKunde(kunde);

        kunde.setBestellungen(null);
        
        out.println("Neuer Kunde: " + kunde);                  //NOSONAR
        out.println("Neue Adresse: " + adresse);               //NOSONAR
        return kunde;
    }

    public void updateKunde(AbstractKunde kunde) {
        out.println("Aktualisierter Kunde: " + kunde);         //NOSONAR
    }

    public void deleteKunde(UUID kundeId) {
        out.println("Kunde mit ID=" + kundeId + " geloescht");   //NOSONAR
    }
    
    public Optional<Mitarbeiter> findMitarbeiterById(UUID id) {
        return findMitarbeiterById(id, true);
    }
   
    private Optional<Mitarbeiter> findMitarbeiterById(UUID id, boolean checkId) {
        final String idStr = id.toString();
        final long tmp = Long.decode("0x" + idStr.substring(idStr.length() - 12));
        if (checkId && tmp > MAX_ID) {
            return empty();
        }
        
        final Mitarbeiter mitarbeiter =  new Mitarbeiter(); //NOSONAR
        
        
        // Das private Attribut "id" setzen, ohne dass es eine set-Methode gibt
        try {
            final Field idField = Mitarbeiter.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mitarbeiter, id);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }
        
        int idx = RANDOM.nextInt() % NACHNAMEN.size();
        if (idx < 0) {
            idx += NACHNAMEN.size();
        }
        final String nachname = NACHNAMEN.get(idx);
        mitarbeiter.setNachname(nachname);
        
        return of (mitarbeiter);
        
    }
    
    public void updateMitarbeiter(Mitarbeiter mitarbeiter) {
        out.println("Aktualisierter Mitarbeiter: " + mitarbeiter);     //NOSONAR
    }

    public void deleteMitarbeiter(UUID mitarbeiterId) {
        out.println("Mitarbeiter mit ID=" + mitarbeiterId + " geloescht");   //NOSONAR
    }
    
    
    public Mitarbeiter saveMitarbeiter(Mitarbeiter mitarbeiter) {
        final UUID id  = randomUUID();
        // Das private Attribut "id" setzen, ohne dass es eine set-Methode gibt
        try {
            final Field idField = Adresse.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mitarbeiter, id);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }
        
        out.println("Neuer Mitarbeiter = " + mitarbeiter);
        return mitarbeiter;
    }
    
    
    public List<Mitarbeiter> findAllMitarbeiter() {
        final int anzahl = MAX_MITARBEITER;
        final List<Mitarbeiter> mitarbeiters = new ArrayList<>(anzahl);
        IntStream.rangeClosed(1, anzahl)
                 .forEach(unused -> {
            final Mitarbeiter mitarbeiter = findMitarbeiterById(randomUUID(), false).get();
            mitarbeiters.add(mitarbeiter);            
        });
        return mitarbeiters;
    }

    public List<Mitarbeiter> findMitarbeiterByNachname(String nachname) {
        final int anzahl = nachname.length();
        final List<Mitarbeiter> mitarbeiters = new ArrayList<>(anzahl);
        IntStream.rangeClosed(1, anzahl)
                 .forEach(unused -> {
            final Mitarbeiter mitarbeiter = findMitarbeiterById(randomUUID(), false).get();
            mitarbeiter.setNachname(nachname);
            mitarbeiters.add(mitarbeiter);            
        });
        return mitarbeiters;
    }
    
    public Optional<Position> findPositionById(UUID id) {
        final Bestellung bestellung = findBestellungById(randomUUID()).get();
        
        final Position position = new Position();
        
        try{
            final Field idField = Position.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(position, id);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e){
            throw new ShopRuntimeException(e);
        }
        
        position.setBestellung(bestellung);
        
        return of(position);
    }
    
    public List<Position> findPositionByBestellung(Bestellung bestellung) {
        final int anzahl = (int) (bestellung.getId().getLeastSignificantBits() % MAX_POSITIONEN) + 1;
 
        final List<Position> positionen = new ArrayList<>(anzahl);
        IntStream.rangeClosed(1, anzahl)
                    .forEach(unused -> {
                final Position position = findPositionById(randomUUID()).get();
                position.setBestellung(bestellung);
                positionen.add(position);
                        });
        bestellung.setPositionen(positionen);
        
        return positionen;
    }
    
    public void updatePosition(Position position) {
        out.println("Aktualisierte Position: " + position);     //NOSONAR
    }

    public void deletePosition(UUID positionId) {
        out.println("Position mit ID=" + positionId + " geloescht");   //NOSONAR
    }
    
    
    public Position savePosition(Position position) {
        final UUID id  = randomUUID();
        // Das private Attribut "id" setzen, ohne dass es eine set-Methode gibt
        try {
            final Field idField = Position.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(position, id);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }
        
        //Bestellung und Artikel noch hinzufügen
       
        out.println("Neue Position = " + position);
        return position;
    }
    
    public Optional <Rechnung> findRechnungById(UUID id) {
        final Bestellung bestellung = findBestellungById(randomUUID()).get();
         
        final Rechnung rechnung = new Rechnung();
         
        try{
            final Field idField = Rechnung.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(rechnung, id);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e){
            throw new ShopRuntimeException(e);
        }
         
        rechnung.setBestellung(bestellung);
         
        return of(rechnung);
    }
    
   
    public Rechnung findRechnungByBestellung(Bestellung bestellung) {
       Rechnung rechnung = new Rechnung();
        
       rechnung = bestellung.getRechnung();
         
        return rechnung;
    } 

    public List<Rechnung> findRechnungenByKunde(AbstractKunde kunde) {
        final int anzahl = (int) (kunde.getId().getLeastSignificantBits() % MAX_RECHNUNGEN) + 1;
  
        final List<Rechnung> rechnungen = new ArrayList<>(anzahl);
        IntStream.rangeClosed(1, anzahl)
                    .forEach(unused -> {
                final Rechnung rechnung = findRechnungById(randomUUID()).get();
                rechnung.setKunde(kunde);
                rechnungen.add(rechnung);
                        });
        kunde.setRechnungen(rechnungen);
         
        return rechnungen;
    }
   
    public void updateRechnung(Rechnung rechnung) {
        out.println("Aktualisierte Rechnung: " + rechnung);     //NOSONAR
    }

    public void deleteRechnung(UUID rechnungId) {
        out.println("Rechnung mit ID=" + rechnungId + " geloescht");   //NOSONAR
    }
    
    
    public Rechnung saveRechnung(Rechnung rechnung) {
        final UUID id  = randomUUID();
        // Das private Attribut "id" setzen, ohne dass es eine set-Methode gibt
        try {
            final Field idField = Rechnung.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(rechnung, id);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new ShopRuntimeException(e);
        }
        
        //Bestellung und Artikel noch hinzufügen
       
        out.println("Neue Rechnung = " + rechnung);
        return rechnung;
    }
}
