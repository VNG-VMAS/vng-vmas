package com.vng;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;

public class CheckLog {

	public static void main(String[] args) {
		// checklog TMN 20140726 20140727
		// Variable Declaration
		String gameCode = null;
		Properties vngProperty = new Properties();
		String groups = null;
		SimpleDateFormat dateFormat = null;
		SimpleDateFormat dateTimeFormat = null;
		String dateTimeToday = null;
		String serverListFile = null;
		String serverMapConnection = null;
		String serverMapTable = null;
		String serverMapQuerry = null;
		String serverName = null;
		int ccuFilesize = 0;
		Calendar calendar = Calendar.getInstance();
		Connection connection = null;
		PreparedStatement pst;

		// Check args
		try {
			vngProperty.load(new FileInputStream("VNG.properties"));
		} catch (IOException e) {
			System.out.println("Khong tim thay file VNG.properties");
			return;
		}
		if (args.length == 0) {
			System.out.println("Thieu gamecode");
			return;
		} else {
			gameCode = args[0];
		}
		dateFormat = new SimpleDateFormat((String) vngProperty.get(gameCode + ".date_format"));
		String endDate = dateFormat.format(calendar.getTime()); // today
		dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		dateTimeToday = dateTimeFormat.format(calendar.getTime());
		calendar.add(Calendar.DATE, -1);
		String startDate = dateFormat.format(calendar.getTime()); // yesterday
		if (args.length == 1) {

		} else if (args.length == 2) {
			startDate = args[1];
		} else if (args.length == 3) {
			startDate = args[1];
			endDate = args[2];
		} else {
			System.out.println("Args khong chinh xac");
			return;
		}

		// Variables Initialization
		groups = vngProperty.getProperty(gameCode + ".groups");
		serverListFile = vngProperty.getProperty(gameCode + ".server_list.file");
		serverMapConnection = vngProperty.getProperty(gameCode + ".server_map.connection");
		serverMapTable = vngProperty.getProperty(gameCode + ".server_map.table");
		serverMapQuerry = vngProperty.getProperty(gameCode + ".server_map.query");
		String ccuFilesizeString = (String) vngProperty.get(gameCode + ".ccu.filesize");
		if (ccuFilesizeString.length() < 1) {
			ccuFilesize = 0;
		} else {
			ccuFilesize = Integer.parseInt(ccuFilesizeString);
		}
		serverName = vngProperty.getProperty(gameCode + ".name");
		if (groups == null || serverListFile == null || serverMapConnection == null || serverMapTable == null | serverMapQuerry == null) {
			System.out
					.println("File VNG.properties phai co dang\nTMN.groups=TMN_g1#TMN_g2\nTMN.date_format=yyyymmdd\nTMN.server_list.file=server.txt\nTMN.server_map.connection=jdbc:mysql://host:port/database#user#password\nTMN.server_map.table=serverList\nTMN.server_map.query=insert into");
			return;
		}
		Properties gameProperty = new Properties();
		List<String> server = new LinkedList<String>();
		try {
			BufferedReader serverReader = new BufferedReader(new FileReader(serverListFile));
			String line = null;
			while ((line = serverReader.readLine()) != null) {
				server.add(line);
			}
			serverReader.close();
		} catch (Exception e) {
			System.out.println("Khong tim thay server list file");
			return;
		}
		try {
			connection = DriverManager.getConnection(serverMapConnection);
			pst = connection.prepareStatement(serverMapQuerry);
		} catch (SQLException e1) {
			System.out.println("Khong the ket noi den mysql");
			return;
		}

		// Create & Initialization gameProperties
		String[] vngGroups = ((String) vngProperty.getProperty(gameCode + ".groups")).split("#");
		String[][] gameProperties = new String[vngGroups.length][];
		for (int i = 0; i < vngGroups.length; i++) {
			try {
				gameProperty.load(new FileInputStream(vngGroups[i] + ".properties"));
				String[] logType = ((String) gameProperty.get("log_types")).split("#");
				gameProperties[i] = new String[logType.length + 1];
				gameProperties[i][0] = (String) gameProperty.getProperty("base_dir");
				for (int j = 0; j < logType.length; j++) {
					gameProperties[i][j + 1] = ((String) gameProperty.get(logType[j] + ".pattern")).replace("?", ".").replace("*", ".*");
				}
			} catch (IOException e) {
				System.out.println("Khong tim thay file " + vngGroups[i] + ".properties");
				return;
			}
		}
		for (int i = 0; i < gameProperties.length; i++) {
			if (gameProperties[i].length == 2)
				continue;
			try {
				calendar.setTime(dateFormat.parse(startDate));
			} catch (Exception e) {
				System.out.println("Dinh dang ngay thang sai");
			}
			calendar.add(Calendar.DATE, -1);
			while (!dateFormat.format(calendar.getTime()).equals(endDate)) {
				calendar.add(Calendar.DATE, 1);
				File folder = new File((gameProperties[i][0] + "/" + dateFormat.format(calendar.getTime()) + "/").toString());
				if (folder.exists()) {
					File[] listOfServer = folder.listFiles();
					for (int j = 0; j < listOfServer.length; j++) {
						if (!server.contains(listOfServer[j].getName())) {
							byte count = 0;
							File listOfLogs[] = new File(gameProperties[i][0] + "/" + dateFormat.format(calendar.getTime()) + "/" + listOfServer[j].getName()).listFiles();
							for (int l = 1; l < gameProperties[i].length; l++) {
								for (int k = 0; k < listOfLogs.length; k++) {
									if (listOfLogs[k].getName().matches(gameProperties[i][l]) && listOfLogs[k].length() / 1024 >= ccuFilesize) {
										count++;
										break;
									}
								}
								if ((count == 2) && !server.contains(listOfServer[j].getName())) {
									server.add(listOfServer[j].getName());
									try {
										pst.setInt(1, Integer.parseInt(listOfServer[j].getName()));
										pst.setString(2, serverName + listOfServer[j].getName());
										pst.setInt(3, 1);
										pst.setString(4, dateTimeToday);
										pst.setString(5, null);
										pst.setString(6, null);
										pst.executeUpdate();
									} catch (SQLException e) {
										System.out.println("Loi insert SQL");
									}
								}
							}
						}
					}
				}
			}
		}

		// Write log
		try {
			FileWriter serverWriter = new FileWriter(serverListFile);
			BufferedWriter wr = new BufferedWriter(serverWriter);
			for (int i = 0; i < server.size(); i++) {
				wr.write(server.get(i) + "\n");
			}
			wr.close();
		} catch (Exception e) {
			System.out.println("Khong the ghi ra file");
		}

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
