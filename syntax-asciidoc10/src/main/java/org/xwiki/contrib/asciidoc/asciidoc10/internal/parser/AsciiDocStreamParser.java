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

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.syntax.Syntax;

import static java.util.Collections.emptyMap;

/**
 * Stream Parser for AsciiDoc syntax.
 *
 * @version $Id$
 */
@Component
@Named("asciidoc/1.0")
@Singleton
public class AsciiDocStreamParser implements StreamParser, Initializable
{
    @Inject
    @Named("html")
    private Parser htmlParser;

    @Inject
    @Named("plain")
    private Parser plainParser;

    private Asciidoctor asciidoctor;

    @Override
    public Syntax getSyntax()
    {
        return AsciiDocSyntaxProvider.ASCIIDOC_10;
    }

    @Override
    public void initialize()
    {
        this.asciidoctor = JRubyAsciidoctor.create();
    }

    @Override
    public void parse(Reader source, Listener listener) throws ParseException
    {
        MetaData metadata = new MetaData(Collections.singletonMap(MetaData.SYNTAX, getSyntax()));
        listener.beginDocument(metadata);
        try {
            visitDocument(this.asciidoctor.load(IOUtils.toString(source), Options.builder().build()), listener);
        } catch (IOException e) {
            throw new ParseException("Failed to parse AsciiDoc content", e);
        }
        listener.endDocument(metadata);
    }

    private void visitDocument(Document doc, Listener listener) throws ParseException
    {
        List<StructuralNode> blocks = doc.getBlocks();
        visitBlockList(blocks, listener);
    }

    private void visitBlockList(List<StructuralNode> nodes, Listener listener) throws ParseException
    {
        for (StructuralNode node : nodes) {
            if (node instanceof Section) {
                visitSection((Section) node, listener);
            } else if (node instanceof org.asciidoctor.ast.List) {
                visitList((org.asciidoctor.ast.List) node, listener);
            } else if (node instanceof Block && node.getContext().equals("paragraph")) {
                visitParagraph(node, listener);
            }
        }
    }

    private void visitParagraph(StructuralNode node, Listener listener) throws ParseException
    {
        // Since AsciiDoc doesn't currently have an inline parser, we ask it to generate HTML and
        // parse it ourselves, see https://github.com/asciidoctor/asciidoctor/issues/61
        this.htmlParser.parse((String) node.getContent(), listener, false);
    }

    private void visitSection(Section section, Listener listener) throws ParseException
    {
        for (int i = 0; i < section.getLevel(); i++) {
            listener.beginSection(emptyMap());
        }
        listener.beginHeader(convertHeadingLevel(section.getLevel()), section.getId(), emptyMap());
        this.plainParser.parse(section.getTitle(), listener, true);
        listener.endHeader(convertHeadingLevel(section.getLevel()), section.getId(), emptyMap());
        for (int i = 0; i < section.getLevel(); i++) {
            listener.endSection(emptyMap());
        }
        visitBlockList(section.getBlocks(), listener);
    }

    private void visitList(org.asciidoctor.ast.List list, Listener listener) throws ParseException
    {
        ListType type = convertListType(list.getContext());
        listener.beginList(type, emptyMap());
        for (StructuralNode nodeItem : list.getItems()) {
            if (nodeItem instanceof ListItem) {
                ListItem listItem = (ListItem) nodeItem;
                listener.beginListItem();
                this.htmlParser.parse(listItem.getText(), listener, true);
                visitBlockList(nodeItem.getBlocks(), listener);
                listener.endListItem();
            }
        }
        listener.endList(type, emptyMap());
    }

    private ListType convertListType(String context)
    {
        ListType result;
        switch (context) {
            case "olist":
                result = ListType.NUMBERED;
                break;
            case "ulist":
            default:
                result = ListType.BULLETED;
        }
        return result;
    }

    private HeaderLevel convertHeadingLevel(int level)
    {
        return HeaderLevel.valueOf("LEVEL" + level);
    }
}
