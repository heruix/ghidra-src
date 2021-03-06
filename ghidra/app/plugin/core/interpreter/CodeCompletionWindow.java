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

import generic.util.WindowUtilities;
import ghidra.app.plugin.core.console.CodeCompletion;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataListener;

/**
 * This class encapsulates a code completion popup Window for the ConsolePlugin.
 * 
 * 
 *
 */
public class CodeCompletionWindow extends JDialog {
	private static final long serialVersionUID = 1L;
	/* from ReferenceHoverPlugin */
	private static final Color BACKGROUND_COLOR = new Color(255, 255, 230);

	protected final InterpreterPanel console;
	protected final JTextPane outputTextField;
	/* List of CodeCompletions.
	 * If the substitution value is null, then that attribute will not be
	 * selectable for substitution.
	 */
	protected List<CodeCompletion> completion_list;
	/* current list of completions */
	protected JList jlist;

	/**
	 * Constructs a new CodeCompletionWindow.
	 * 
	 * We pass in the PluginTool so we can get a reference to the Frame we are
	 * popping up over, as well as the text field we are coming from (so we
	 * can fine-tune where we pop up).
	 * 
	 * @param tool the PluginTool the has the Frame we are popping up over
	 * @param console the ConsolePlugin we are providing services for
	 * @param outputTextField the JTextField from whence we came
	 */
	public CodeCompletionWindow(Window parent, InterpreterPanel cp, JTextPane textField) {
		super(parent);

		this.console = cp;
		outputTextField = textField;
		jlist = new JList();
		completion_list = null;

		setUndecorated(true);
		/* don't steal focus from text input! */
		setFocusableWindowState(false);

		jlist.setBackground(BACKGROUND_COLOR);
		jlist.setCellRenderer(new CodeCompletionListCellRenderer());
		/* add the ability to double-click a code completion */
		MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				/* when the user clicks the popup window, make sure
				 * that the outputTextField gets the focus (so that the escape
				 * key and other hotkeys to manage the popup work correctly
				 */
				outputTextField.requestFocusInWindow();
				/* double-click inserts a completion */
				if (e.getClickCount() == 2) {
					console.insertCompletion(getCompletion());
				}
			}
		};
		jlist.addMouseListener(mouseListener);
		/* actually put the components together */
		getContentPane().add(new JScrollPane(jlist));
		updateCompletionList(completion_list);

		jlist.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				processKeyEvent(e);
			}
		});
	}

	/**
	 * Process a KeyEvent for this Window.
	 * 
	 * This method is located here so that others (e.g. ConsolePlugin) can
	 * forward KeyEvents to us, or we can process KeyEvents that were directed
	 * to us (because we had focus instead).
	 * 
	 * @param e KeyEvent
	 */
	@Override
	public void processKeyEvent(KeyEvent e) {
		// not sure what this is overridden--probably an unused concept
	}

	/**
	 * Updates the completion list with the given completion mapping.
	 * 
	 * The format, as mentioned above, is:
	 * "attribute" -> "substitution value"
	 * If the substitution value is null, then that attribute will not be
	 * selectable for substitution.
	 * 
	 * After updating the mapping, this Window then updates its size as
	 * appropriate.
	 * 
	 * The Window also will attempt to move out of the way of the cursor/caret
	 * in the textField.  However, if the caret's position had recently been
	 * changed and the caret had not been repainted yet, then the caret's
	 * location can be null.  In this case, the Window will not move.
	 * You can avoid this condition by calling this method in a
	 * SwingUtilities.invokeLater(Runnable).
	 * 
	 * @param list List of code completions
	 */
	public void updateCompletionList(List<CodeCompletion> list) {
		completion_list = list;
		jlist.setModel(new CodeCompletionListModel(list));
		jlist.setSelectionModel(new CodeCompletionListSelectionModel(list));
		jlist.clearSelection();
		/* size the window */
		pack();
		/* move the window close to the cursor, if possible */
		try {
			Point caretLocation = outputTextField.getCaret().getMagicCaretPosition();
			if (caretLocation != null) {
				setLocation(offsetLocation(caretLocation));

			}
			else {
				/* caretLocation can be null when the caret has been moved, but
				 * not repainted yet
				 */
			}
		}
		catch (IllegalComponentStateException icse) {
			/* okay, never mind then -- the Component is not on-screen yet
			 * (initialization), so we can't know where it is */
		}
		invalidate();
	}

	public Point offsetLocation(Point location) {
		Rectangle screenBounds = WindowUtilities.getScreenBounds();
		int screenWidth = screenBounds.width;
		int screenHeight = screenBounds.height;
		int textHeight = outputTextField.getHeight();

		/* double-check the width of the completion */
		if (getWidth() > screenWidth) {
			setSize(screenWidth, getHeight());
		}

		/* Out of the four positioning cases, this is the normal case:
		 * position the window lower and slightly to the right of the
		 * caret
		 */
		Point textFieldLocation = outputTextField.getLocationOnScreen();
		Point newLocation =
			new Point(textFieldLocation.x + location.x + textHeight, textFieldLocation.y +
				location.y + textHeight);
		/* does the right side of the window go off the right edge of
		 * the screen?
		 */
		if ((newLocation.x + getWidth()) > screenWidth) {
			/* yes it does... now what do we do about it? */
			if ((newLocation.y + getHeight()) > screenHeight) {
				/* and the bottom goes off the bottom of the screen --
				 * relocate to above the prompt
				 * This is the least desirable case...
				 */
				newLocation =
					new Point(screenWidth - getWidth(), textFieldLocation.y - getHeight() -
						textHeight);
			}
			else {
				/* the bottom is okay, however -- just move the 
				 * window left to get it back on the screen
				 */
				newLocation = new Point(screenWidth - getWidth(), newLocation.y);
			}
		}
		/* does the bottom of the window go off the bottom of the
		 * screen?
		 */
		else if ((newLocation.y + getHeight()) > screenHeight) {
			/* yes -- bump it up to get it back on the screen */
			newLocation = new Point(newLocation.x, screenHeight - getHeight());
		}

		return newLocation;
	}

	/**
	 * Sets the Font on this CodeCompletionWindow.
	 * 
	 * Basically sets the Font in the completion list.
	 * 
	 * @param font the new Font
	 */
	@Override
	public void setFont(Font font) {
		jlist.setFont(font);
	}

	/**
	 * Selects the previous item in the list with a usable completion.
	 *
	 */
	public void selectPrevious() {
		for (int i = jlist.getSelectedIndex() - 1; i >= 0; i--) {
			CodeCompletion completion = completion_list.get(i);
			if (CodeCompletion.isValid(completion)) {
				jlist.setSelectedIndex(i);
				jlist.ensureIndexIsVisible(i);
				break;
			}
		}
	}

	/**
	 * Selects the next item in the list with a usable completion.
	 *
	 */
	public void selectNext() {

		if (null == completion_list) {
			return;
		}
		for (int i = jlist.getSelectedIndex() + 1; i < completion_list.size(); i++) {
			CodeCompletion completion = completion_list.get(i);
			if (CodeCompletion.isValid(completion)) {
				jlist.setSelectedIndex(i);
				jlist.ensureIndexIsVisible(i);
				break;
			}
		}
	}

	/**
	 * Returns the currently selected code completion.
	 * 
	 * Returns "" if there is none.
	 * 
	 * @return the currently selected code completion, or null if none selected
	 */
	public CodeCompletion getCompletion() {
		int i = jlist.getSelectedIndex();
		if (-1 == i) {
			return null;
		}
		return completion_list.get(i);
	}
}

/**
 * Code completion ListModel.
 */
class CodeCompletionListModel implements ListModel {
	List<CodeCompletion> completion_list;

	public CodeCompletionListModel(List<CodeCompletion> completion_list) {
		this.completion_list = completion_list;
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		// don't support listeners
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		// don't support listeners
	}

	@Override
	public Object getElementAt(int index) {
		if ((null == completion_list) || completion_list.isEmpty()) {
			if (0 == index) {
				return new CodeCompletion("(no completions available)", null, null);
			}
			return null;
		}
		return completion_list.get(index);
	}

	@Override
	public int getSize() {
		if ((null == completion_list) || completion_list.isEmpty()) {
			return 1;
		}
		return completion_list.size();
	}
}

/**
 * This data type handles selection changes in the CodeCompletionWindow.
 * 
 * This contains all the "smarts" to determine whether or not indices can be
 * selected.  So when the user clicks on an entry with the mouse, we choose
 * whether or not that index can actually be highlighted/selected.
 * 
 * 
 *
 */
class CodeCompletionListSelectionModel extends DefaultListSelectionModel {
	List<CodeCompletion> list;

	/**
	 * Constructs a new CodeCompletionListSelectionModel using the given List.
	 * 
	 * @param l the List to use
	 */
	public CodeCompletionListSelectionModel(List<CodeCompletion> l) {
		list = l;
		setSelectionMode(SINGLE_SELECTION);
	}

	/**
	 * Called when the selection needs updating.
	 * 
	 * Here we will check the value of the new index and determine whether or
	 * not we actually want to select it.
	 * 
	 * @param index0 old index
	 * @param index1 new index
	 */
	@Override
	public void setSelectionInterval(int index0, int index1) {
		try {
			CodeCompletion completion = list.get(index1);
			if (CodeCompletion.isValid(completion)) {
				super.setSelectionInterval(index0, index1);
			}
		}
		catch (IndexOutOfBoundsException ioobe) {
			/* okay, then we won't change the selection */
		}
	}
}

/**
 * Renders CodeCompletions for the CodeCompletionWindow.
 * 
 * 
 *
 */
class CodeCompletionListCellRenderer extends DefaultListCellRenderer {

	/**
	 * Render either a default list cell, or use the one provided.
	 * 
	 * If the CodeCompletion we got has a Component to be used, then use that.
	 * Otherwise, we use the DefaultListCellRenderer routine.
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object completion, int index,
			boolean isSelected, boolean cellHasFocus) {
		CodeCompletion codeCompletion = (CodeCompletion) completion;
		if (null == codeCompletion.getComponent()) {
			return super.getListCellRendererComponent(list, codeCompletion.getDescription(), index,
				isSelected, cellHasFocus);
		}

		/* ooh, we have a fancy component! */
		JComponent component = codeCompletion.getComponent();
		/* if it's selected, make sure it shows up that way */
		component.setOpaque(true);
		if (isSelected) {
			component.setBackground(list.getSelectionBackground());
		}
		else {
			component.setBackground(list.getBackground());
		}
		/* other nice formatting stuff */
		component.setEnabled(list.isEnabled());
		component.setFont(list.getFont());
		component.setComponentOrientation(list.getComponentOrientation());
		Border border = null;
		if (cellHasFocus) {
			if (isSelected) {
				border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
			}
			if (border == null) {
				border = UIManager.getBorder("List.focusCellHighlightBorder");
			}
		}
		else {
			border = new EmptyBorder(1, 1, 1, 1);
		}
		component.setBorder(border);

		return component;
	}
}
