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

package org.xwiki.contrib.syntax.asciidoctor.parser.internal

import org.xwiki.rendering.block.XDOM
import spock.lang.Specification
import spock.lang.Subject

class DefaultAsciidoctorParserTest extends Specification {
    @Subject
    private final DefaultAsciidoctorParser parser = new DefaultAsciidoctorParser()

    private XDOM xdom

    def "parses asciidoctor document"() {
        when:
        parse 'testdoc1'

        then:
        noExceptionThrown()
    }

    private void parse(String testResource) {
        DefaultAsciidoctorParser.class.getResourceAsStream("/${testResource}.adoc").withReader("utf-8") {
            xdom = parser.parse(it)
        }
    }
}
