/*
 * This file is licensed under the MIT License, part of refmap-remapper.
 * Copyright (c) 2021 shedaniel
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

package me.shedaniel.architectury.refmapremapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.shedaniel.architectury.refmapremapper.remapper.MappingsRemapper;
import me.shedaniel.architectury.refmapremapper.remapper.ReferenceRemapper;
import me.shedaniel.architectury.refmapremapper.remapper.Remapper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RefmapRemapper {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    
    public static String remap(Remapper remapper, String content) {
        JsonObject object = GSON.fromJson(content, JsonObject.class);
        object = remap(remapper, object);
        return GSON.toJson(object);
    }
    
    public static JsonObject remap(Remapper remapper, JsonObject content) {
        if (content.has("mappings")) {
            MappingsRemapper mappingsRemapper = remapper.remapMappings();
            
            if (mappingsRemapper != null) {
                content.add("mappings", remapMappings(content.getAsJsonObject("mappings"), mappingsRemapper));
            }
        }
        if (content.has("data")) {
            JsonObject dataOut = new JsonObject();
            
            for (Map.Entry<String, JsonElement> entry : content.getAsJsonObject("data").entrySet()) {
                @Nullable
                Map.Entry<String, @Nullable MappingsRemapper> mappingsData = remapper.remapMappingsData(entry.getKey());
                
                if (mappingsData == null) {
                    dataOut.add(entry.getKey(), entry.getValue());
                } else if (mappingsData.getValue() != null) {
                    dataOut.add(mappingsData.getKey(), remapMappings(entry.getValue().getAsJsonObject(), mappingsData.getValue()));
                } else {
                    dataOut.add(mappingsData.getKey(), entry.getValue());
                }
            }
            
            content.add("data", dataOut);
        }
        
        return content;
    }
    
    private static JsonObject remapMappings(JsonObject object, MappingsRemapper remapper) {
        JsonObject out = new JsonObject();
        
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            @Nullable
            ReferenceRemapper referenceRemapper = remapper.remap(entry.getKey());
            
            if (referenceRemapper != null) {
                for (Map.Entry<String, JsonElement> elementEntry : entry.getValue().getAsJsonObject().entrySet()) {
                    Map.Entry<String, String> newEntry = referenceRemapper.remap(elementEntry.getKey(), elementEntry.getValue().getAsString());
                    out.addProperty(newEntry.getKey(), newEntry.getValue());
                }
            } else {
                out.add(entry.getKey(), entry.getValue());
            }
        }
        
        return out;
    }
}
