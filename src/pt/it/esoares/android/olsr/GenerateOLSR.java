package pt.it.esoares.android.olsr;

import pt.it.esoares.android.devices.Device;

public class GenerateOLSR {

	public static String getOLSRConfig(Device device, OLSRSetting setting) {

		StringBuilder result = new StringBuilder();
		result.append("FIBMetric \"flat\"");
		result.append("ClearScreen yes");
		result.append("AllowNoInt yes");
		result.append("IpcConnect\n{");
		result.append("	MaxConnections 0\n");
		result.append("	Host 127.0.0.1\n}");
		result.append("UseHysteresis no\n" + "NicChgsPollInt 3.0\n" + "TcRedundancy 2\n" + "MprCoverage 3\n");
		
		// plugin
		if (setting.useTXTInfo()) {
			result.append("LoadPlugin \"" + setting.pluginTXTInfoLocation() + "\"\n");
			result.append("{\n");
			result.append("	PlParam \"Accept\" \"127.0.0.1\"");
			result.append("}\n");
		}

		// Interface
		result.append("Interface " + device.interfaceName() + "\n");
		result.append("{\n		 Ip4Broadcast 255.255.255.255\n");
		result.append("		 Mode \"mesh\"\n		}");

		return result.toString();
	}
}
