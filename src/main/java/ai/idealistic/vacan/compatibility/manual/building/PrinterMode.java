package ai.idealistic.vacan.compatibility.manual.building;

import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.compatibility.Compatibility;
import me.gwndaan.printer.PrinterModeAPI;

public class PrinterMode {

    public static boolean isUsing(PlayerProtocol p) {
        return Compatibility.CompatibilityType.PRINTER_MODE.isFunctional()
                && PrinterModeAPI.isInPrinterMode(p.bukkit());
    }
}
