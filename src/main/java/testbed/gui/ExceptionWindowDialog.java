/*
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Robert Eckstein - initial API and implementation
 *        http://www.oracle.com/technetwork/articles/javase/wizard-136789.html
 *     David Jelenc - adaptation and modification for ATB
 */
package testbed.gui;

import javax.swing.*;
import java.awt.*;

public class ExceptionWindowDialog extends JDialog {

    private static final long serialVersionUID = -1019952182258899050L;

    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;

    private int code;
    private String title, details;

    public ExceptionWindowDialog(String title, String details) {
        this.title = title;
        this.details = details;
        setModal(true);
        initComponents();
    }

    public static void main(String args[]) {
        ExceptionWindowDialog dialog = new ExceptionWindowDialog("Title",
                "Kind of long text.");
        System.out.println(dialog.showNotification());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 500);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea(details);

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(
                jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(jDialog1Layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 400, Short.MAX_VALUE));
        jDialog1Layout.setVerticalGroup(jDialog1Layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 300, Short.MAX_VALUE));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jButton1.setText("Continue");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                code = 0;
                close();
            }
        });

        jPanel1.add(jButton1);

        jButton2.setText("Terminate");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                code = 1;
                close();
            }
        });
        jPanel1.add(jButton2);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        jLabel1.setText("<html>Error: <b><font color='red'>" + title);
        getContentPane().add(jLabel1, java.awt.BorderLayout.PAGE_START);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }

    private void close() {
        dispose();
    }

    public int showNotification() {
        setLocationRelativeTo(getParent());
        setVisible(true);

        return code;
    }

}
