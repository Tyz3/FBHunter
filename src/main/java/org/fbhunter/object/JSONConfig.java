package org.fbhunter.object;

import com.google.gson.GsonBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
public class JSONConfig {
	
	protected Map<String, Object> object;
	
	private final File jsonFile;
	private final boolean compressJson;
	private final int capacityListElements;
	
	public String getName() {
		return jsonFile.getName();
	}

	/**
	 * @param filePath путь к JSON файлу.
	 * @param compressJson сжатие json-а: удаление отступов и переносов строк (по умолчанию false).
	 * @param capacityListElements максимальный размер списка (по умолчанию 50).
	 */
	public JSONConfig(Path filePath, boolean compressJson, int capacityListElements) {
		jsonFile = filePath.toFile();
		loadJsonFile();

		initializeMap();
		loadDataToRAM();
		this.compressJson = compressJson;
		this.capacityListElements = capacityListElements;
	}

	/**
	 * @param fileName название файла (будет создан, если не существует).
	 * @param path рабочая директория.
	 * @param compressJson сжатие json-а: удаление отступов и переносов строк (по умолчанию false).
	 * @param capacityListElements максимальный размер списка (по умолчанию 50).
	 */
	public JSONConfig(String fileName, String path, boolean compressJson, int capacityListElements) {
		this(Path.of(path.concat(File.separator).concat(fileName)), compressJson, capacityListElements);
	}

	/**
	 * @param fileName название файла (будет создан, если не существует).
	 * @param path рабочая директория.
	 * @param compressJson сжатие json-а: удаление отступов и переносов строк (по умолчанию false).
	 */
	public JSONConfig(String fileName, String path, boolean compressJson) {
		this(fileName, path, compressJson, 50);
	}

	/**
	 * @param fileName название файла (будет создан, если не существует).
	 * @param path рабочая директория.
	 */
	public JSONConfig(String fileName, String path) {
		this(fileName, path, false, 50);
	}

	/**
	 * @param filePath путь к JSON файлу.
	 */
	public JSONConfig(Path filePath) {
		this(filePath, false, 50);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		JSONConfig that = (JSONConfig) o;
		return jsonFile.equals(that.jsonFile);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jsonFile);
	}

	private void loadJsonFile() {
		if (!jsonFile.getParentFile().exists()) {
			if (!jsonFile.getParentFile().mkdirs()) {
				System.err.println("Не удалось создать директорию " + jsonFile.getPath() + " для JSON-файла " + jsonFile.getName() + ".");
			}
        }

		if (!jsonFile.exists() || jsonFile.length() < 2) {
			createDefaultFile();
		}
	}
	
	protected void initializeMap() {
		object = new HashMap<>();
	}
	
	private void loadDataToRAM() {
		FileReader fr;

		try {
			fr = new FileReader(jsonFile);

			JSONObject object = (JSONObject) new JSONParser().parse(fr);
			Set<Entry<String, Object>> o = object.entrySet();

			for (Entry<String, Object> e : o) {
				this.object.put(e.getKey(), e.getValue());
			}

	    	fr.close();
		} catch (IOException | ParseException e) {
			System.err.println("Произошла ошибка парсинга JSON-файла " + jsonFile.getName() + ".");
			e.printStackTrace();
		}
	}

	public void createDefaultFile() {
		clear();
	}

	/**
	 * Отчистка всего содержимого
	 */
	public void clear() {
		PrintWriter pw;

		try {
			pw = new PrintWriter(jsonFile, StandardCharsets.UTF_8);

			pw.print("{}");
			pw.flush();
			pw.close();

		} catch (FileNotFoundException e) {
			System.err.println("Ошибка: JSON-файл " + jsonFile.getName() + "не найден.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Ошибка ввода/вывода JSON-файла " + jsonFile.getName() + ".");
			e.printStackTrace();
		}

		loadDataToRAM();
	}

	/**
	 * Сохранение JSON-файла на диск.
	 */
	public void save() {
		JSONObject obj = new JSONObject();
		obj.putAll(this.object);

		try {
			String jsonForWrite = (compressJson) ? obj.toJSONString() : new GsonBuilder().setPrettyPrinting()
					.create().toJson(obj);

			FileWriter outfile = new FileWriter(jsonFile, false);
			outfile.write(jsonForWrite);
			outfile.flush();
			outfile.close();
		} catch (IOException e) {
			System.err.println("Ошибка при сохранении JSON-файла " + jsonFile.getName() + ".");
			e.printStackTrace();
		}
	}

	/**
	 * @param path путь в JSON структуре, имеющий вид "object1.object2. и так далее".
	 * @param object значение, которое будет вставлено по указанному пути.
	 */
	public void set(String path, Object object) {
		String[] positions = splitPath(path);

		Map<String, Object> map = this.object;

		for (int i = 0; i < positions.length - 1; i++) {
			Object obj = map.get(positions[i]);

			if (obj instanceof HashMap<?, ?>) {
				map = (HashMap<String, Object>) obj;

			} else {
				Map<String, Object> hm = new HashMap<>();
				map.put(positions[i], hm);
				map = hm;
			}
		}

		if (object == null)
			map.remove(positions[positions.length - 1]);
		else
			map.put(positions[positions.length - 1], object);
	}

	public Object get(String path, Object defValue) {
		String[] positions = splitPath(path);

		Map<String, Object> map = this.object;

		for (int i = 0; i < positions.length - 1; i++) {
			Object obj = map.get(positions[i]);

			if (obj instanceof HashMap<?, ?>) {
				map = (HashMap<String, Object>) obj;

			} else if (obj == null || i < positions.length - 2) {
				return defValue;

			} else if (i == positions.length - 2) {
				Object obj2 = map.get(positions[i + 1]);

				return obj2 != null ? obj2 : defValue;
			}
		}

		return map.getOrDefault(positions[positions.length - 1], defValue);
	}

	public Set<String> keySet() {
		return object.keySet();
	}

	public Set<String> keySet(String path) {
		return getMap(path).keySet();
	}
	
	public String getString(String path, String defValue) {

		Object res = get(path, defValue);

		return res instanceof String ? res.toString() : defValue;
	}
	
	public int getInt(String path, int defValue) {
		return (int) getLong(path, defValue);
	}
	
	public long getLong(String path, long defValue) {

		Object res = get(path, defValue);

		return res instanceof Long ? (Long) res : defValue;
	}
	
	public double getDouble(String path, double defValue) {

		Object res = get(path, defValue);

		return res instanceof Double ? (Double) res : defValue;
	}
	
	public boolean getBoolean(String path, boolean defValue) {

		Object res = get(path, defValue);

		return res instanceof Boolean ? (Boolean) res : defValue;
	}

	public List<String> getStringList(String path) {

		Object res = get(path, new ArrayList<String>());

		return res instanceof Collection<?> ? (ArrayList<String>) res : new ArrayList<>();
	}

	public List<Long> getLongList(String path) {

		Object res = get(path, new ArrayList<Long>());

		return res instanceof Collection<?> ? (ArrayList<Long>) res : new ArrayList<>();
	}

	public List<Object> getObjectList(String path) {

		Object res = get(path, new ArrayList<>());

		return res instanceof Collection<?> ? (ArrayList<Object>) res : new ArrayList<>();

	}
	
	/**
	 * @param path путь в JSON структуре, имеющий вид "object1.object2. и так далее".
	 */
	public void addKeyValueToArray(String path, String key, Object value) {
		List<Object> list = getObjectList(path);

		HashMap<String, Object> hm = new HashMap<>();
		hm.put(key, value);

		list.add(hm);

		// Удаление лишних записей
		trimList(list, true, capacityListElements);

	}
	
	/**
	 * @param path путь в JSON структуре, имеющий вид "object1.object2. и так далее".
	 * @param value строка для конвертации в JSONObject в виде "key=value".
	 * @param position позиция добавляемого объекта.
	 */
	public void addKeyValueToArray(String path, String key, Object value, int position) {
		List<Object> list = getObjectList(path);

		HashMap<String, Object> hm = new HashMap<>();
		hm.put(key, value);

		list.add(position, hm);

		// Удаление лишних записей
		trimList(list, true, capacityListElements);
	}
	
	/**
	 * @param path путь в JSON структуре, имеющий вид "object1.object2. и так далее".
	 * @param value добавляемый объект тип String.
	 */
	public void addToList(String path, Object value) {
		List<Object> list = getObjectList(path);

		list.add(value);
		
		// Удаление лишних записей
		trimList(list, false, capacityListElements);
	}
	
	/**
	 * @param path путь в JSON структуре, имеющий вид "object1.object2. и так далее".
	 * @param value добавляемый объект типа Object.
	 * @param position позиция в списке добавляемого элемента.
	 */
	public void addToList(String path, Object value, int position) {
		List<Object> list = getObjectList(path);

		list.add(position, value);

		// Удаление лишних записей
		trimList(list, true, capacityListElements);
	}
	
	/**
	 * @param path путь в JSON структуре, имеющий вид "object1.object2. и так далее".
	 * @param position позиция в списке удаляемого элемента.
	 */
	public void removeFromList(String path, int position) {
		List<Object> list = getObjectList(path);

		if (list.size() > position)
			list.remove(position);
	}
	
	public Map<String, Object> getMap(String path) {

		Object res = get(path, new HashMap<String, Object>());

		return res instanceof Map<?, ?> ? (HashMap<String, Object>) res : new HashMap<>();
	}

	public Map<String, Integer> getIntMap(String path) {
		Map<String, Object> hm = getMap(path);

		Map<String, Integer> ihm = new HashMap<>();

		if (!hm.isEmpty()) {
			for (Entry<String, Object> entry : hm.entrySet()) {
				assert entry.getValue() instanceof Integer;
				ihm.put(entry.getKey(), (Integer) entry.getValue());
			}
		}

		return ihm;
	}

	public Map<String, Long> getLongMap(String path) {
		Map<String, Object> hm = getMap(path);

		Map<String, Long> ihm = new HashMap<>();

		if (!hm.isEmpty()) {
			for (Entry<String, Object> entry : hm.entrySet()) {
				assert entry.getValue() instanceof Long;
				ihm.put(entry.getKey(), (Long) entry.getValue());
			}
		}

		return ihm;
	}
	
	/**
	 * @param path путь в JSON структуре, имеющий вид "object1.object2. и так далее".
	 * @param value слагаемое, которое будет сложено с полученным числом,
	 * если такового нет, то берётся значение по умолчанию.
	 * @param defValue первое слагаемое значение по умолчанию.
	 */
	public void inc(String path, long value, long defValue) {
		long l = getLong(path, defValue) + value;
		set(path, l);
	}

	/**
	 * @param path путь в JSON структуре, имеющий вид "object1.object2. и так далее".
	 * @param value вычитаемое, которое будет сложено с полученным числом,
	 * если такового нет, то берётся значение по умолчанию.
	 * @param defValue уменьшаемое значение по умолчанию.
	 */
	public void dec(String path, long value, long defValue) {
		long l = getLong(path, defValue) - value;
		set(path, l);
	}
	
	public static boolean fileExists(String folder, String fileName) {
		File file = new File(".".concat(File.separator).concat(folder).concat(File.separator).concat(fileName));
		
		return file.exists() && file.isFile() && file.length() > 2;
	}

	private static <E> void trimList(List<E> list, boolean atTheEnd, int maxElements) {
		if (list.size() > maxElements) {
			if (atTheEnd) {
				list.subList(maxElements, list.size()).clear();
			} else {
				list.subList(0, list.size() - maxElements).clear();
			}
		}
	}

	private static String[] splitPath(String path) {
		return path.split("\\.");
	}
}
