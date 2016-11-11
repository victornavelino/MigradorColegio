/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migradorcolegiomedico;

import java.awt.Component;
import java.awt.HeadlessException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

/**
 *
 * @author franco
 */
public class FileChooserTop extends JFileChooser {

    @Override
    protected JDialog createDialog(Component parent)
            throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        // config here as needed - just to see a difference
        dialog.setLocationByPlatform(true);
        // might help - can't know because I can't reproduce the problem
        dialog.setAlwaysOnTop(true);
        return dialog;
    }

}
