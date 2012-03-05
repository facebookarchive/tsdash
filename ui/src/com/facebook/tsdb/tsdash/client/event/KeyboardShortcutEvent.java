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
package com.facebook.tsdb.tsdash.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class KeyboardShortcutEvent extends GwtEvent<KeyboardShortcutHandler> {

    public static final GwtEvent.Type<KeyboardShortcutHandler> TYPE =
        new GwtEvent.Type<KeyboardShortcutHandler>();

    public enum Shortcut {
        CTRL_SPACE, CTRL_F;
    }

    private final Shortcut shortcut;

    public KeyboardShortcutEvent(Shortcut shortcut) {
        this.shortcut = shortcut;
    }

    @Override
    public GwtEvent.Type<KeyboardShortcutHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(KeyboardShortcutHandler handler) {
        if (shortcut.equals(Shortcut.CTRL_SPACE)) {
            handler.onCtrlSpace(this);
        } else if (shortcut.equals(Shortcut.CTRL_F)) {
            handler.onCtrlF(this);
        }
    }

}
