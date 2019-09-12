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

package io.arlas.subscriptions.logger;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ArlasLogger {
    private final Logger logger;
    private final String module;
    private final static String logFormat = "{} \"{}\"";
    private static final Marker fatal = MarkerFactory.getMarker("FATAL");

    public ArlasLogger(Logger logger, String module) {
        this.logger = logger;
        this.module = module;
    }

    public void trace(String message) {
        logger.trace(logFormat, module, message);
    }

    public void trace(String message, Throwable e) {
        logger.trace(logFormat, module, message, e);
    }

    public void debug(String message) {
        logger.debug(logFormat, module, message);
    }

    public void debug(String message, Throwable e) {
        logger.debug(logFormat, module, message, e);
    }

    public void info(String message) {
        logger.info(logFormat, module, message);
    }

    public void info(String message, Throwable e) {
        logger.info(logFormat, module, message, e);
    }

    public void warn(String message) {
        logger.warn(logFormat, module, message);
    }

    public void warn(String message, Throwable e) {
        logger.warn(logFormat, module, message, e);
    }

    public void error(String message) {
        logger.error(logFormat, module, message);
    }

    public void error(String message, Throwable e) {
        logger.error(logFormat, module, message, e);
    }

    public void fatal(String message) {
        logger.error(fatal, logFormat, module, message);
    }

    public void fatal(String message, Throwable e) {
        logger.error(fatal, logFormat, module, message, e);
    }
}
