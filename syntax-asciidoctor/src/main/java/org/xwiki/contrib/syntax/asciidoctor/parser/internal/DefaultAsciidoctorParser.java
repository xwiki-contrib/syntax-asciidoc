/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.xwiki.contrib.syntax.asciidoctor.parser.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.parser.XDOMGeneratorListener;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.emptyMap;

@Component
@Named("asciidoctor/1.0")
@Singleton
public class DefaultAsciidoctorParser implements Parser {
    public static final Syntax ASCIIDOCTOR = new Syntax(new SyntaxType("asciidoctor", "Asciidoctor"), "1.0");

    private final Asciidoctor asciidoctor = new JRubyAsciidoctor();

    @Override
    public Syntax getSyntax() {
        return ASCIIDOCTOR;
    }

    @Override
    public XDOM parse(Reader source) throws ParseException {
        XDOMGeneratorListener xdomGeneratorListener = new XDOMGeneratorListener();
        doParse(loadDocument(source), xdomGeneratorListener);
        return xdomGeneratorListener.getXDOM();
    }

    private Document loadDocument(Reader source) throws ParseException {
        return asciidoctor.load(asString(source), new HashMap<>());
    }

    private void doParse(Document doc, XDOMGeneratorListener xdomGeneratorListener) throws ParseException {
        MetaData metadata = new MetaData(Collections.singletonMap(MetaData.SYNTAX, ASCIIDOCTOR));
        xdomGeneratorListener.beginDocument(metadata);
        visitDocument(doc, xdomGeneratorListener);
        xdomGeneratorListener.endDocument(metadata);
    }

    private void visitDocument(Document doc, XDOMGeneratorListener listener) {
        List<StructuralNode> blocks = doc.getBlocks();
        visitBlockList(blocks, listener);
    }

    private void visitBlockList(List<StructuralNode> nodes, XDOMGeneratorListener listener) {
        for (StructuralNode node : nodes) {
            if (node instanceof Section) {
                visitSection((Section) node, listener);
            } else if (node instanceof Block){
                Block block = (Block) node;
                switch (node.getContext()) {
                    case "paragraph":
                        listener.beginParagraph(emptyMap());
                        listener.beginFormat(Format.NONE, emptyMap());
                        for (String line : block.getLines()) {
                            listener.onRawText(line, Syntax.PLAIN_1_0);
                        }
                        listener.endFormat(Format.NONE, emptyMap());
                        listener.endParagraph(emptyMap());
                        break;
                }
            }
            visitBlockList(node.getBlocks(), listener);
        }
    }

    private void visitSection(Section section, XDOMGeneratorListener listener) {
        listener.beginSection(emptyMap());
        listener.beginHeader(levelOf(section.getLevel()), section.getId(), emptyMap());
        listener.onRawText(section.getTitle(), Syntax.PLAIN_1_0);
        listener.endHeader(levelOf(section.getLevel()), section.getId(), emptyMap());
        listener.endSection(emptyMap());
    }

    private static HeaderLevel levelOf(int level) {
        return HeaderLevel.valueOf("LEVEL" + level);
    }

    private static String asString(Reader source) throws ParseException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(source)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(line);
            }
        } catch (IOException ex) {
            throw new ParseException("Unable to read source", ex);
        }
        return sb.toString();
    }
}
