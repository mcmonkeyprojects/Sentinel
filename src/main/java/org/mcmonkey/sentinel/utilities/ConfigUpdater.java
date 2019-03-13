package org.mcmonkey.sentinel.utilities;

import com.freneticllc.freneticutilities.freneticdatasyntax.FDSData;
import com.freneticllc.freneticutilities.freneticdatasyntax.FDSSection;

import java.util.ArrayList;
import java.util.Arrays;

public class ConfigUpdater {
    /**
     * Adds missing entries to a config file, based on a modern config file.
     * Returns the patched config if patches were made, otherwise returns null.
     */
    public static String updateConfig(String configFile, String correctConfig) {
        FDSSection fileSection = new FDSSection(configFile);
        FDSSection correctSection = new FDSSection(correctConfig);
        if (transferKeys(fileSection, correctSection)) {
            int changes = fileSection.getInt("times_changed", 0);
            changes++;
            FDSData data = new FDSData(changes, new ArrayList<>(Arrays.asList(
                    "----------- Important! -----------",
                    " Your configuration file was outdated.",
                    " It has been automatically updated.",
                    " You may notice some duplicated comments or other minor inconsistencies,",
                    " if these annoy you, you may remove the duplicate comments OR",
                    " delete the config file to allow it to regenerate.",
                    "",
                    " The below value is how many times automatic updates have been applied",
                    " since you last cleaned this file.",
                    " ----------- End Important! -----------"
            )));
            fileSection.setRootData("times_changed", data);
            return fileSection.savetoString();
        }
        return null;
    }

    public static boolean fixComments(FDSData user, FDSData correct) {
        boolean changed = false;
        for (String comment : correct.precedingComments) {
            if (!user.precedingComments.contains(comment)) {
                user.precedingComments.add(comment);
                changed = true;
            }
        }
        return changed;
    }

    public static boolean transferKeys(FDSSection fileSection, FDSSection correctSection) {
        boolean changed = false;
        for (String rootKey : correctSection.getRootKeys()) {
            FDSData data = correctSection.getRootData(rootKey);
            if (!fileSection.hasKey(rootKey)) {
                fileSection.setRootData(rootKey, data);
                changed = true;
            }
            else if (data.internal instanceof FDSSection) {
                FDSData dataTwo = fileSection.getRootData(rootKey);
                if (dataTwo.internal instanceof FDSSection) {
                    if (transferKeys((FDSSection) dataTwo.internal, (FDSSection) data.internal)) {
                        changed = true;
                    }
                    if (fixComments(dataTwo, data)) {
                        changed = true;
                    }
                }
                else {
                    fileSection.setRootData(rootKey, data);
                    changed = true;
                }
            }
            else {
                if (fixComments(fileSection.getRootData(rootKey), data)) {
                    changed = true;
                }
            }
        }
        return changed;
    }
}
