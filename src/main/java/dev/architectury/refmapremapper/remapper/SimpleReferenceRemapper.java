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

package dev.architectury.refmapremapper.remapper;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.architectury.refmapremapper.utils.DescriptorRemapper.remapDescriptor;

public class SimpleReferenceRemapper implements DirectReferenceRemapper {
    private static final Pattern METHOD_PATTERN = Pattern.compile("L(.*);(.*)(\\(.*)");
    private static final Pattern METHOD_PATTERN_WITHOUT_CLASS = Pattern.compile("(.*)(\\(.*)");
    private static final Pattern FIELD_PATTERN = Pattern.compile("L(.*);(.*):(.*)");
    private static final Pattern FIELD_PATTERN_WITHOUT_CLASS = Pattern.compile("(.*):(.*)");
    private final Remapper remapper;
    
    public SimpleReferenceRemapper(Remapper remapper) {
        this.remapper = remapper;
    }
    
    private String replaceFirst(String full, String toReplace, String replacement) {
        int index = full.indexOf(toReplace);
        if (index == -1) return full;
        return full.substring(0, index) + replacement + full.substring(index + toReplace.length());
    }
    
    @Override
    public String remapSimple(String key, String value) {
        String remappedRef = null;
        
        Matcher methodMatch = METHOD_PATTERN.matcher(value);
        Matcher fieldMatch = FIELD_PATTERN.matcher(value);
        Matcher fieldMatchWithoutClass = FIELD_PATTERN_WITHOUT_CLASS.matcher(value);
        Matcher methodMatchWithoutClass = METHOD_PATTERN_WITHOUT_CLASS.matcher(value);
        @Nullable String classMatch = remapper.mapClass(value);
        
        if (methodMatch.matches()) {
            String className = methodMatch.group(1);
            String methodName = methodMatch.group(2);
            String methodDescriptor = methodMatch.group(3);
            String replacementName = remapper.mapMethod(className, methodName, methodDescriptor);
            
            remappedRef = replaceFirst(value, className, either(remapper.mapClass(className), className));
            remappedRef = replaceFirst(remappedRef, methodName, either(replacementName, methodName));
            remappedRef = replaceFirst(remappedRef, methodDescriptor,
                    remapDescriptor(methodDescriptor, it -> either(remapper.mapClass(it), it)));
        } else if (fieldMatch.matches()) {
            String className = fieldMatch.group(1);
            String fieldName = fieldMatch.group(2);
            String fieldDescriptor = fieldMatch.group(3);
            String replacementName = remapper.mapField(className, fieldName, fieldDescriptor);
            
            remappedRef = replaceFirst(value, className, either(remapper.mapClass(className), className));
            remappedRef = replaceFirst(remappedRef, fieldName, replacementName);
            remappedRef = replaceFirst(remappedRef, fieldDescriptor,
                    remapDescriptor(fieldDescriptor, it -> either(remapper.mapClass(it), it)));
        } else if (fieldMatchWithoutClass.matches()) {
            String fieldName = fieldMatchWithoutClass.group(1);
            String fieldDescriptor = fieldMatchWithoutClass.group(2);
            String replacementName = remapper.mapField(null, fieldName, fieldDescriptor);
            remappedRef = replaceFirst(value, fieldName, either(replacementName, fieldName));
            remappedRef = replaceFirst(remappedRef, fieldDescriptor,
                    remapDescriptor(fieldDescriptor, it -> either(remapper.mapClass(it), it)));
        } else if (methodMatchWithoutClass.matches()) {
            String methodName = methodMatchWithoutClass.group(1);
            String methodDescriptor = methodMatchWithoutClass.group(2);
            String replacementName = remapper.mapMethod(null, methodName, methodDescriptor);
            
            remappedRef = replaceFirst(value, methodName, either(replacementName, methodName));
            remappedRef = replaceFirst(remappedRef, methodDescriptor,
                    remapDescriptor(methodDescriptor, it -> either(remapper.mapClass(it), it)));
        } else if (classMatch != null) {
            remappedRef = classMatch;
        }
        
        if (remappedRef == null) {
            return value;
        } else {
            return remappedRef;
        }
    }
    
    private static <T> T either(@Nullable T first, @Nullable T second) {
        return first == null ? second : first;
    }
    
    public interface Remapper {
        @Nullable
        String mapClass(String value);
        
        @Nullable
        String mapMethod(@Nullable String className, String methodName, String methodDescriptor);
        
        @Nullable
        String mapField(@Nullable String className, String fieldName, String fieldDescriptor);
    }
}
