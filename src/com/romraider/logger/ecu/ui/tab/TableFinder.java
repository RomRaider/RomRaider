package com.romraider.logger.ecu.ui.tab;

import com.romraider.maps.Rom;
import com.romraider.maps.Table;
import java.util.List;

public final class TableFinder {
    public static Table findTableStartsWith(Rom rom, String name) {
        List<Table> tables = rom.findTables("^" + name + ".*$");
        if (tables.isEmpty()) throw new IllegalStateException("No table found for name: \"" + name + "\"");
        if (tables.size() > 1) throw new IllegalStateException("Multiple tables found for name: \"" + name + "\"");
        return tables.get(0);
    }
}
