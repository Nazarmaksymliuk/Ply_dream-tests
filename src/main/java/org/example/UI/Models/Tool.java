package org.example.UI.Models;

import java.util.List;

public class Tool {

    public String name;            // Tool Name *
    public String mfgNumber;       // MFG #
    public String description;     // Tool Description
    public String tags;      // Tags (multi)
    public List<ToolUnit> toolsUnits;

    public Tool() {}

    public Tool(String name, String mfgNumber, String description, String tags) {
        this.name = name;
        this.mfgNumber = mfgNumber;
        this.description = description;
        this.tags = tags;
    }
}
