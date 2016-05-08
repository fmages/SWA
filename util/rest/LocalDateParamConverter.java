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

package de.shop.util.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import static java.time.LocalDate.parse;

/**
 * Bei @QueryParam oder @PathParam z.B. "2001-10-31"
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Provider
@ApplicationScoped
public class LocalDateParamConverter implements ParamConverter<LocalDate>, ParamConverterProvider {
    private static final String ERROR_KEY = "invalidDate";
    
    @Inject
    private Messages messages;
    
    @Override
    public LocalDate fromString(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        
        try {
            return parse(dateStr);
        } catch (DateTimeParseException e) {                           //NOSONAR
            final String msg = messages.getMessage(null, ERROR_KEY, dateStr);
            // die Original-Exception wird bewusst nicht uebernommen, weil die neue
            // Exception zum HTTP-Response fuer den aufrufenden Client wird
            throw new BadRequestException(msg);
        }
    }

    @Override
    public String toString(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType == LocalDate.class) {
            return (ParamConverter<T>) this;
        }
        return null;
    }
}
