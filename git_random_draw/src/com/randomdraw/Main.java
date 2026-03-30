package com.randomdraw;

import com.randomdraw.ui.MainFrame;
import java.nio.file.Path;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        UIManager.put("OptionPane.okButtonText", "确定");
        UIManager.put("OptionPane.cancelButtonText", "取消");
        UIManager.put("OptionPane.yesButtonText", "是");
        UIManager.put("OptionPane.noButtonText", "否");

        SwingUtilities.invokeLater(() -> {
            Path root = Path.of(System.getProperty("user.dir"));
            MainFrame frame = new MainFrame(root);
            frame.setVisible(true);
        });
    }
}
