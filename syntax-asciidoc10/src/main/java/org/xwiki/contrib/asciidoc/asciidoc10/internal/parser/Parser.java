/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.asciidoc.asciidoc10.internal.parser;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;

/**
 * Parse text content produced by AsciiDoctorJ for its inline block content (since AsciiDoctorJ currently
 * <a href="https://github.com/asciidoctor/asciidoctor/issues/61">doesn't implement any inline parser</a>.
 *
 * @version $Id$
 */
@Role
public interface Parser
{
    /**
     * @param text the text to parse
     * @param listener the listener to which to send the events for the parsed blocks to
     * @param removeTopLevelBock if true then remove the top level block (e.g. paragraph block)
     * @throws ParseException in case of parsing error
     */
    void parse(String text, Listener listener, boolean removeTopLevelBock) throws ParseException;
}
