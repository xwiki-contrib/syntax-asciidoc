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

import java.io.Reader;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.parser.XDOMGeneratorListener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.syntax.Syntax;

/**
 * XDOM parser for AsciiDoc, delegating the work to the stream parser implementation.
 *
 * @version $Id$
 */
@Component
@Named("asciidoc/1.0")
@Singleton
public class AsciiDocParser implements Parser
{
    @Inject
    @Named("asciidoc/1.0")
    private StreamParser asciidocStreamParser;

    @Override
    public Syntax getSyntax()
    {
        return AsciiDocSyntaxProvider.ASCIIDOC_10;
    }

    @Override
    public XDOM parse(Reader source) throws ParseException
    {
        XDOMGeneratorListener xdomGeneratorListener = new XDOMGeneratorListener();
        this.asciidocStreamParser.parse(source, xdomGeneratorListener);
        return xdomGeneratorListener.getXDOM();
    }
}
