package com.prodject.gcontrol;

import android.content.*;

final class CarCommandBus {
    static String supportedCommands() {
        return "Климат, кондиционер, A/C max, авто-режим, eco climate, вентилятор, обдув, температура, рециркуляция, стекла, окна, люк, шторка, багажник, зеркала, подсветка, парктроник, проектор, свет, руль, сиденья, вентиляция, массаж, профили, режимы движения, топливо, температуры, запас хода, скорость.";
    }

    static void send(Context c, String command, String value) {
        Intent i = new Intent("com.prodject.gcontrol.CAR_COMMAND");
        i.setPackage(c.getPackageName());
        i.putExtra("command", command);
        i.putExtra("value", value);
        c.sendBroadcast(i);
        broadcast(c, "geely.oneos.intent.action.CAR_COMMAND", command, value);
        broadcast(c, "ecarx.intent.action.CAR_COMMAND", command, value);
        broadcast(c, "app.monji.CAR_COMMAND", command, value);
    }

    private static void broadcast(Context c, String action, String command, String value) {
        Intent i = new Intent(action);
        i.putExtra("command", command);
        i.putExtra("value", value);
        i.putExtra("source", c.getPackageName());
        c.sendBroadcast(i);
    }
}
