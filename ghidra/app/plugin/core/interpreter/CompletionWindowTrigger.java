/* ###
 * IP: GHIDRA
 * REVIEWED: YES
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.app.plugin.core.interpreter;

import java.awt.AWTKeyStroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

public enum CompletionWindowTrigger {

	//@formatter:off
	TAB(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0, false)), 
	CONTROL_SPACE(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK, false));
	//@formatter:on

	private KeyStroke keyStroke;

	CompletionWindowTrigger(KeyStroke ks) {
		this.keyStroke = ks;
	}

	public KeyStroke getKeyStroke() {
		return keyStroke;
	}

	public boolean isTrigger(KeyEvent e) {
		AWTKeyStroke other = AWTKeyStroke.getAWTKeyStrokeForEvent(e);
		return other.equals(keyStroke);
	}
}
