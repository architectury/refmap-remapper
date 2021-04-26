/*
 * This file is licensed under the MIT License, part of refmap-remapper.
 * Copyright (c) 2021 architectury
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.architectury.refmapremapper.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.function.UnaryOperator;

public final class DescriptorRemapper {
    private DescriptorRemapper() {}
    
    public static String remapDescriptor(String self, UnaryOperator<String> classMappings) {
        try {
            StringReader reader = new StringReader(self);
            StringBuilder result = new StringBuilder();
            boolean insideClassName = false;
            StringBuilder className = new StringBuilder();
            while (true) {
                int c = reader.read();
                if (c == -1) {
                    break;
                }
                if (c == (int) ';') {
                    insideClassName = false;
                    result.append(classMappings.apply(className.toString()));
                }
                if (insideClassName) {
                    className.append((char) c);
                } else {
                    result.append((char) c);
                }
                if (!insideClassName && c == (int) 'L') {
                    insideClassName = true;
                    className.setLength(0);
                }
            }
            return result.toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
