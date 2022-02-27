package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

public class ApplicationMain {

    private static final String MAC_ADDRESS = "ether";
    private static final String IF_CONFIG = "ifconfig";


    public static void main(String[] args) {
        System.out.println("Выбери в каком интерфейсе хочешь изменить mac address");
        var paramIfConfigValueMap = getParcIfConfig();
        if (paramIfConfigValueMap != null) {
            paramIfConfigValueMap.keySet().forEach(System.out::println);
        }
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Введите название интерфейса: ");
            String inputInterface = bufferedReader.readLine();
            if (paramIfConfigValueMap != null && paramIfConfigValueMap.get(inputInterface) == null) {
                System.out.println("Ошибка, не найден интерфейс: " + inputInterface);
            }
            var list = paramIfConfigValueMap
                    .get(inputInterface).stream()
                    .filter(search -> search.contains(MAC_ADDRESS))
                    .findFirst()
                    .toString();
            Pattern pattern = Pattern.compile("\\w\\w:\\w\\w:\\w\\w:\\w\\w:\\w\\w:\\w\\w");
            var matcher = pattern.matcher(list).group(1);
            System.out.println("Ваш mac address до изменения: " + matcher);

            try {
                var arrayString = new String[]{IF_CONFIG, inputInterface, "hw" , MAC_ADDRESS, generateRandomMacAddress()};
                var down = new String[]{IF_CONFIG, inputInterface, "down" };
                var up = new String[]{IF_CONFIG, inputInterface, "up" };
                Runtime.getRuntime().exec(down);
                Runtime.getRuntime().exec(arrayString);
                Runtime.getRuntime().exec(up);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void changeMacAddress(String[] args) {
        var paramIfConfigValue = getParcIfConfig();
    }

    public static String generateRandomMacAddress() {
        Random rand = new Random();
        byte[] macAddr = new byte[6];
        rand.nextBytes(macAddr);
        macAddr[0] = (byte) (macAddr[0] & (byte) 254);
        StringBuilder sb = new StringBuilder(18);
        for (byte b : macAddr) {
            if (sb.length() > 0)
                sb.append(":");
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    private static Map<String, List<String>> getParcIfConfig() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(IF_CONFIG);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            var list = reader.lines().toList();
            Map<String, List<String>> map = new LinkedHashMap<>();
            var listNew = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                if (!list.get(i).contains("\t") || !list.get(i).contains("\t\t")) {
                    for (int o = i + 1; o < list.size() + 1; o++) {
                        if (o == list.size()) {
                            map.put(list.get(i).substring(0, list.get(i).indexOf(": ")), new ArrayList(listNew));
                            listNew.clear();
                            i = o - 1;
                            break;
                        }
                        if (list.get(o).contains("\t") || list.get(o).contains("\t\t")) {
                            listNew.add(list.get(o));
                        } else {
                            map.put(list.get(i).substring(0, list.get(i).indexOf(": ")), new ArrayList(listNew));
                            listNew.clear();
                            i = o - 1;
                            break;
                        }
                    }
                }
            }
            return map;
        } catch (IOException e) {
            e.printStackTrace();
            return Map.of();
        }
    }
}

