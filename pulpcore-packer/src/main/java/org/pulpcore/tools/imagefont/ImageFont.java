/*
    Copyright (c) 2007-2011, Interactive Pulp, LLC
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of Interactive Pulp, LLC nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package org.pulpcore.tools.imagefont;

import java.util.List;

public class ImageFont {

    private final String name;
    private final int ascent;
    private final int descent;
    private final boolean hasLowercase;
    private final List<Glyph> glyphs;
    private final List<Kerning> kerningPairs;

    public ImageFont(String name, int ascent, int descent, boolean hasLowercase, 
            List<Glyph> glyphs, List<Kerning> kerningPairs) {
        this.name = name;
        this.ascent = ascent;
        this.descent = descent;
        this.hasLowercase = hasLowercase;
        this.glyphs = glyphs;
        this.kerningPairs = kerningPairs;
        for (Glyph glyph : glyphs) {
            glyph.setFont(this);
        }
    }

    public List<Glyph> getGlyphs() {
        return glyphs;
    }

    public boolean hasLowercase() {
        return hasLowercase;
    }

    public int getAscent() {
        return ascent;
    }

    public int getDescent() {
        return descent;
    }

    public String getName() {
        return name;
    }

    public List<Kerning> getKerningPairs() {
        return kerningPairs;
    }
}