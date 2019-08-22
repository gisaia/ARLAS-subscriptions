/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.subscriptions.utils;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

public class JsonSchemaValidator {

    private Schema schema;

    public JsonSchemaValidator(String pathToSchema) throws FileNotFoundException {
        File jsonSchemaFile = new File(pathToSchema);
        this.schema = SchemaLoader.load( new JSONObject(new JSONTokener(new FileInputStream(jsonSchemaFile))));
    }

    public  void validJsonObjet(Map<String,Object> map) throws ValidationException {
            JSONObject jsonObject = new JSONObject(map);
            schema.validate(jsonObject); // throws a ValidationException if this object is invalid
    }

}
