package ai.idealistic.spartan.compatibility.manual.building;

import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.compatibility.Compatibility;
import me.gwndaan.printer.PrinterModeAPI;

public class PrinterMode {

    public static boolean isUsing(PlayerProtocol p) {
        return Compatibility.CompatibilityType.PRINTER_MODE.isFunctional()
                && PrinterModeAPI.isInPrinterMode(p.bukkit());
    }
}
