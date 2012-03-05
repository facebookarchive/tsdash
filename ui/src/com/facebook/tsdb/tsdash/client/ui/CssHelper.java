/*
 * Copyright 2011 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.tsdb.tsdash.client.ui;

import com.google.gwt.user.client.ui.UIObject;

public class CssHelper {

    public static void toggleClass(UIObject el, String cssClass) {
        if (el == null) {
            return;
        }
        if (el.getStyleName().contains(cssClass)) {
            el.removeStyleName(cssClass);
        } else {
            el.addStyleName(cssClass);
        }
    }

    public static void replaceClass(UIObject el, String cssClass,
            String replacement) {
        if (el == null) {
            return;
        }
        if (el.getStyleName().contains(cssClass)) {
            el.removeStyleName(cssClass);
        }
        // add the replacement anyway
        el.addStyleName(replacement);
    }
}
