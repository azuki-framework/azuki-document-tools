/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.azkfw.document.tools.parser;

import java.util.ArrayList;
import java.util.List;

import org.azkfw.lang.LoggingObject;

/**
 * @since 1.0.0
 * @version 1.0.0 2015/01/30
 * @author kawakicchi
 */
public abstract class AbstractDocumentParser<DOCUMENT, EVENT, LISTENER extends DocumentParserListener<EVENT>> extends LoggingObject implements
		DocumentParser<DOCUMENT, EVENT, LISTENER> {

	private List<LISTENER> listeners;

	public AbstractDocumentParser() {
		listeners = new ArrayList<LISTENER>();
	}

	@Override
	public final void addListener(LISTENER listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public final void removeListener(LISTENER listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	protected final void callListener(EVENT event) {
		synchronized (listeners) {
			for (LISTENER listener : listeners) {
				listener.documentParserCallback(event);
			}
		}
	}

	@Override
	public final void parse(DOCUMENT document) {
		doParse(document);
	}

	protected abstract void doInitialize();

	protected abstract void doParse(final DOCUMENT document);

	protected abstract void doRelease();
}
