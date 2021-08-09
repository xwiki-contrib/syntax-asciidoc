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

import java.io.StringReader;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.wiki.WikiModel;

import static org.xwiki.rendering.block.Block.Axes.DESCENDANT;

/**
 * Parse HTML content produced by AsciiDoctorJ for its inline block content (since AsciiDoctorJ currently
 * <a href="https://github.com/asciidoctor/asciidoctor/issues/61">doesn't implement any inline parser</a>.
 *
 * @version $Id$
 */
@Component
@Named("html")
@Singleton
public class HTMLParser extends AbstractParser implements org.xwiki.contrib.asciidoc.asciidoc10.internal.parser.Parser
{
    private static final String MAILTO_SCHEME_PREFIX = "mailto:";

    @Inject
    @Named("html/4.01")
    private Parser parser;

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Override
    public void parse(String text, Listener listener, boolean removeTopLevelBock) throws ParseException
    {
        XDOM xdom = this.parser.parse(new StringReader(text));
        removeTopLevelBlock(xdom, removeTopLevelBock);

        // Step 1: Fix links
        fixLinks(xdom);

        // Step 2: Generate the events from the XDOM blocks so that they are sent to the listener
        for (org.xwiki.rendering.block.Block block : xdom.getChildren()) {
            block.traverse(listener);
        }
    }

    private void fixLinks(XDOM xdom)
    {
        // Modify the links so that they are links to documents instead of PATH typed links, since
        // "<a href="reference">label</a>" will be parsed by the HTML parser as a PATH link.
        for (LinkBlock linkBlock : xdom.<LinkBlock>getBlocks(new ClassBlockMatcher(LinkBlock.class), DESCENDANT)) {
            // - Only change links that are not URL and not mailto links (these one are already properly handled).
            // - However the XHTML parser has a bug to parse mailto links (when there's no link comments), see
            // https://jira.xwiki.org/browse/XRENDERING-612. Thus ATM we work around it by recognizing PATH links having
            // a reference that starts with "mailto"
            // - For some reason AsciiDoc adds a class=bare parameter for URL links. We don't want that.
            ResourceReference rf = linkBlock.getReference();
            if (rf.getType().equals(ResourceType.URL)) {
                // Remove the extra parameter! We consider that it's not possible to specify parameters in AsciiDoc and
                // thus we remove them all.
                LinkBlock newLinkBlock = new LinkBlock(linkBlock.getChildren(), rf, linkBlock.isFreeStandingURI());
                linkBlock.getParent().replaceChild(newLinkBlock, linkBlock);
            } else if (!isMailToLink(rf)) {
                // There's no concept of typed or untyped links in asciidoc so all links are untyped
                rf.setTyped(false);
                // Consider that all non-URL and non-mailto links in asciidoc are links to documents
                // Note: in non-wiki mode, all links are URL links.
                if (isInWikiMode()) {
                    rf.setType(ResourceType.DOCUMENT);
                } else {
                    rf.setType(ResourceType.URL);
                }
                LinkBlock newLinkBlock = new LinkBlock(linkBlock.getChildren(), rf, false);
                linkBlock.getParent().replaceChild(newLinkBlock, linkBlock);
            } else {
                rf.setType(ResourceType.MAILTO);
                rf.setReference(StringUtils.removeStart(rf.getReference(), MAILTO_SCHEME_PREFIX));
            }
        }
    }

    private boolean isMailToLink(ResourceReference reference)
    {
        return reference.getType().equals(ResourceType.PATH)
            && reference.getReference().startsWith(MAILTO_SCHEME_PREFIX);
    }

    private boolean isInWikiMode()
    {
        return this.componentManagerProvider.get().hasComponent(WikiModel.class);
    }
}
