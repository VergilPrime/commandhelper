package com.laytonsmith.tools.docgen.localization;

import com.laytonsmith.PureUtilities.Common.UIUtils;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 *
 */
public class FindDialog extends javax.swing.JDialog {

	public static interface SearchModel {
		/**
		 * When a given entry is selected, the selected entry is provided to this method. This will
		 * also close the dialog and return control to the caller.
		 * @param index The index of the selected entry. This will be the index of the originally supplied
		 * entry set.
		 */
		void selectedEntry(int index);

		/**
		 * Returns the list of values that should be searched through.
		 * @return
		 */
		List<String> getEntrySet();

		/**
		 * Returns the dialog title
		 * @return
		 */
		String getDialogTitle();
	}

	private SearchModel callback;

	/**
	 * Creates new form FindSegmentDialog
	 */
	public FindDialog(java.awt.Frame parent, SearchModel callback) {
		super(parent, true);
		initComponents();
		setTitle(callback.getDialogTitle());
		UIUtils.centerWindowOnWindow(this, parent);
		searchButton.setEnabled(false);
		errorLabel.setVisible(false);
		searchField.requestFocus();
		this.callback = callback;
	}

	public void doSearch() {
		errorLabel.setVisible(false);
		boolean matchCase = matchCaseCheckbox.isSelected();
		boolean literalSearch = (searchTypeButtonGroup.getSelection() == literalSearchRadioButton.getModel());
		String prefix = (matchCase ? "" : "(?s)");
		String regex;
		if(literalSearch) {
			regex = Pattern.quote(searchField.getText());
		} else {
			regex = searchField.getText();
			try {
				Pattern.compile(regex);
			} catch (PatternSyntaxException ex) {
				errorLabel.setText("Invalid Regex: " + ex.getMessage());
				errorLabel.setVisible(true);
				return;
			}
		}
		regex = prefix + ".*" + regex + ".*";
		List<String> entrySet = callback.getEntrySet();
		Map<Integer, String> results = new LinkedHashMap<>();
		for(int i = 0; i < entrySet.size(); i++) {
			String s = entrySet.get(i);
			if(s.matches(regex)) {
				results.put(i, s);
			}
		}
		if(results.isEmpty()) {
			errorLabel.setText("No results found");
			errorLabel.setVisible(true);
		} else if(results.size() == 1) {
			// Go ahead and select that item
			this.setVisible(false);
			callback.selectedEntry(new ArrayList<>(results.keySet()).get(0));
			this.dispose();
		} else {
			resultsList.setModel(new ListModel<String>() {
				@Override
				public int getSize() {
					return results.size();
				}

				@Override
				public String getElementAt(int index) {
					return results.get(new ArrayList<>(results.keySet()).get(index));
				}

				@Override
				public void addListDataListener(ListDataListener l) {}

				@Override
				public void removeListDataListener(ListDataListener l) {}
			});
			resultsList.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					JList list = (JList) e.getSource();
					if(e.getClickCount() == 2) {
						int index = list.locationToIndex(e.getPoint());
						FindDialog.this.setVisible(false);
						callback.selectedEntry(new ArrayList<>(results.keySet()).get(index));
						FindDialog.this.dispose();
					}
				}
			});
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
	 * content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchTypeButtonGroup = new javax.swing.ButtonGroup();
        literalSearchRadioButton = new javax.swing.JRadioButton();
        regexSearchRadioButton = new javax.swing.JRadioButton();
        matchCaseCheckbox = new javax.swing.JCheckBox();
        searchField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultsList = new javax.swing.JList<>();
        searchButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        errorLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        searchTypeButtonGroup.add(literalSearchRadioButton);
        literalSearchRadioButton.setSelected(true);
        literalSearchRadioButton.setText("Literal Search");
        literalSearchRadioButton.setToolTipText("");
        literalSearchRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                literalSearchRadioButtonActionPerformed(evt);
            }
        });

        searchTypeButtonGroup.add(regexSearchRadioButton);
        regexSearchRadioButton.setText("Regex Search");
        regexSearchRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regexSearchRadioButtonActionPerformed(evt);
            }
        });

        matchCaseCheckbox.setText("Match Case");
        matchCaseCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchCaseCheckboxActionPerformed(evt);
            }
        });

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchFieldKeyReleased(evt);
            }
        });

        jLabel1.setText("Results");

        resultsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        resultsList.setToolTipText("Double click to open in main window");
        jScrollPane1.setViewportView(resultsList);

        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        errorLabel.setForeground(java.awt.Color.red);
        errorLabel.setText("Error Label");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
                    .addComponent(searchField)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(literalSearchRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(regexSearchRadioButton))
                            .addComponent(matchCaseCheckbox)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(searchButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(errorLabel)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(literalSearchRadioButton)
                    .addComponent(regexSearchRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(matchCaseCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchButton)
                    .addComponent(cancelButton)
                    .addComponent(errorLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.setVisible(false);
		this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        doSearch();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void searchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchFieldKeyReleased
        searchButton.setEnabled(!searchField.getText().trim().isEmpty());
		if(evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.setVisible(false);
			this.dispose();
		} else if(evt.getKeyCode() == KeyEvent.VK_ENTER && searchButton.isEnabled()) {
			doSearch();
		}
    }//GEN-LAST:event_searchFieldKeyReleased

    private void literalSearchRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_literalSearchRadioButtonActionPerformed
        searchField.requestFocus();
    }//GEN-LAST:event_literalSearchRadioButtonActionPerformed

    private void regexSearchRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regexSearchRadioButtonActionPerformed
        searchField.requestFocus();
    }//GEN-LAST:event_regexSearchRadioButtonActionPerformed

    private void matchCaseCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matchCaseCheckboxActionPerformed
        searchField.requestFocus();
    }//GEN-LAST:event_matchCaseCheckboxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton literalSearchRadioButton;
    private javax.swing.JCheckBox matchCaseCheckbox;
    private javax.swing.JRadioButton regexSearchRadioButton;
    private javax.swing.JList<String> resultsList;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.ButtonGroup searchTypeButtonGroup;
    // End of variables declaration//GEN-END:variables
}