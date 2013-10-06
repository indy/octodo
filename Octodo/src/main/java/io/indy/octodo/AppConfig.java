/*
 * Copyright 2013 Inderjit Gill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.indy.octodo;

// a frontend for the auto-generated BuildConfig
// useful in the rare occasions that values need to be overridden (e.g. debug info in release)
public class AppConfig {

    public static final boolean DEBUG = BuildConfig.DEBUG;

    public static final String VERSION = BuildConfig.VERSION;
    public static final String BUILD_CODE = BuildConfig.BUILD_TIME + "-" + BuildConfig.GIT_SHA;

}
