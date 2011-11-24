/*
    Copyright (c) 2011, Interactive Pulp, LLC
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
package org.pulpcore.tools.png.heuristic;

public class RunCountHeuristic extends AdaptiveFilterHeuristic {

    public double getCompressability(byte filter, byte[] scanline) {
        int minWordSize = 1;
        int maxWordSize = 4;
        if (scanline.length < maxWordSize) {
            // Don't bother
            return 0;
        }
        int bestRunCount = Integer.MAX_VALUE;
        for (int wordSize = minWordSize; wordSize <= maxWordSize; wordSize++) {
            int numRuns = 1;
            long lastValue = filter & 255;
            int len = scanline.length / wordSize * wordSize;
            for (int i = 0; i < len; i += wordSize) {
                long v = 0;
                for (int j = 0; j < wordSize; j++) {
                    v = (v << 8) | (scanline[i + j] & 255);
                }
                if (lastValue != v) {
                    numRuns++;
                    lastValue = v;
                }
            }
            if (numRuns < bestRunCount) {
                bestRunCount = numRuns;
            }
        }
        return bestRunCount;
    }

    @Override
    protected boolean shouldUsePreviousFilter(double bestCompressability, double prevCompressability) {
        return prevCompressability - bestCompressability < 2;
    }
}
