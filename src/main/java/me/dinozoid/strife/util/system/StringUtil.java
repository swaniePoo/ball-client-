package me.dinozoid.strife.util.system;

import net.minecraft.client.gui.GuiScreen;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.regex.Pattern;

public final class StringUtil {

    private static final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isNumeric(String strNum) {
        if (strNum == null) return false;
        return pattern.matcher(strNum).matches();
    }

    public static String upperSnakeCaseToPascal(String s) {
        if (s == null) return null;
        if (s.length() == 1) return Character.toString(s.charAt(0));
        return s.charAt(0) + s.substring(1).toLowerCase();
    }

    public static String trimmedClipboardContents() {
        String data = GuiScreen.getClipboardString();
        data = data.trim();
        if (data.indexOf('\n') != -1)
            data = data.replace("\n", "");
        return data;
    }

    public static void clipboardContents(String contents) {
        StringSelection selection = new StringSelection(contents);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    public static String replaceUnderscore(String input) {
        String[] split = input.split("_");
        String newString;
        if (split.length > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            int index = 0;
            for (String text : split) {
                if (index > 0) stringBuilder.append(" ");
                stringBuilder.append(StringUtils.capitalize(text));
                index++;
            }
            newString = stringBuilder.toString();
        } else newString = input.replaceAll("_", " ");
        return newString;
    }
}
