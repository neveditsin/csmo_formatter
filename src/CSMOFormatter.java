import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;

import org.apache.commons.csv.CSVRecord;


public class CSMOFormatter {

//	private final static String OUTPUT_FILEPATH = "C:\\vs\\src\\CSMOFormatter\\output.csmo";
//	private final static String DATA_FILE = "C:\\vs\\src\\CSMOFormatter\\csci1.csv";
	
	private final static int NUMBER_OF_CSV_FIELDS = 9;

	private final static CSVFormat CSV_FORMAT = CSVFormat.EXCEL;

	
	private final static String REGEX_DT = "^([MTWRF]{1,2}) (\\d{2}\\d{2}-\\d{2}\\d{2})$";
	private final static String REGEX_DT_DBL = "^([MTWRF]{1})([MTWRF]{1}) (\\d{2}\\d{2}-\\d{2}\\d{2})$";
	private final static String REGEX_CN = "^(.+) \\(\\d+\\)$";
	private final static String REGEX_DT_SINGLE = "^([MTWRF]{1}) (\\d{2})(\\d{2})-(\\d{2})(\\d{2})$";
	
	private final static Map<String, Integer> DAYS_MAP = new HashMap<>();
	static {
		DAYS_MAP.put("M", 0);
		DAYS_MAP.put("T", 1);
		DAYS_MAP.put("W", 2);
		DAYS_MAP.put("R", 3);
		DAYS_MAP.put("F", 4);
		DAYS_MAP.put("S", 5);
	}
	
	
	
	public static void main(String[] args) throws IOException {
		List<String[]> validRecords = new ArrayList<String[]>();
		String OUTPUT_FILEPATH = "";
				//"C:\\vs\\src\\CSMOFormatter\\output.csmo";
		final String dir = System.getProperty("user.dir");
		
		
		String DATA_FILE = "C:\\vs\\src\\CSMOFormatter\\csci.csv";
				//"C:\\vs\\src\\CSMOFormatter\\csci1.csv";
		
		if (args.length != 1) {
			System.out.println("USAGE: java -jar csformat.jar PATH_TO_CSV");
			System.exit(-1);
		}
		DATA_FILE = args[0];
		
		OUTPUT_FILEPATH = dir + File.separator + DATA_FILE.substring(DATA_FILE.lastIndexOf(File.separator), DATA_FILE.length()) + ".csmo";
		//"timetable.csmo";
		
		readCSV(DATA_FILE, validRecords);
		
		if(validRecords.size() < 1) {
			System.out.println("No valid records found. Exiting..");
			System.exit(-1);
		}
	
		//validRecords.forEach(r -> System.out.println(Arrays.toString(r)));
		
		Map<String, Set<String>> dtm = makeDtMap(validRecords);
		// System.out.println(dtm);
		Map<Set<String>, Set<String>> dt = mapSToDt(dtm);
		List<CSMOEntry> entries = buildEntries(dt);
		Collections.sort(entries, new Comparator<CSMOEntry>(){
			@Override
			public int compare(CSMOEntry o1, CSMOEntry o2) {
				return new Integer((o2.getEndHour() - o2.getStartHour())).compareTo(o1.getEndHour() - o1.getStartHour());
			}			
		});
		//System.out.println(entries);
		String csmo = buildCSMO(entries);
		//System.out.println(csmo);
	    FileWriter fileWriter = new FileWriter(OUTPUT_FILEPATH);
	    PrintWriter printWriter = new PrintWriter(fileWriter);
	    printWriter.print(csmo);
	    printWriter.close();
	    System.out.println("The output file is written to " + OUTPUT_FILEPATH);
	}
	
	public static String buildCSMO(List<CSMOEntry> entries) {
		String head = "{\"scheduleTitle\":\"Math & CS dept\",\"courses\":[";
		String tail = "]}";
		String comma = ",";
		StringBuilder courses = new StringBuilder();
		for(CSMOEntry e: entries) {
			String course = "{\"title\":\""
					+ e.getTitle()
					+ "\",\"meetingTimes\":["
					+ "{\"startHour\":"
					+ e.getStartHour()
					+ ",\"startMinute\":"
					+ e.getStartMinute()
					+ ",\"startIsAM\":"
					+ e.isStartIsAM()
					+ ",\"endHour\":"
					+ e.getEndHour()
					+ ",\"endMinute\":"
					+ e.getEndMinute()
					+ ",\"endIsAM\":"
					+ e.isEndIsAM()
					+ ",\"meetsOnMonday\":"
					+ e.getMeetdays()[0]
					+ ",\"meetsOnTuesday\":"
					+ e.getMeetdays()[1]
					+ ",\"meetsOnWednesday\":"
					+ e.getMeetdays()[2]
					+ ",\"meetsOnThursday\":"
					+ e.getMeetdays()[3]
					+ ",\"meetsOnFriday\":"
					+ e.getMeetdays()[4]
					+ ",\"meetsOnSaturday\":"
					+ e.getMeetdays()[5]
					+ ",\"meetsOnSunday\":"
					+ e.getMeetdays()[6]
					+ ",\"classType\":\"\",\"location\":\"\",\"instructor\":\"\"}],"
					+ "\"color\":\""
					+ "#F4D03F"
					+ "\",\"SAVE_VERSION\":3,\"DATA_CHECK\":\"69761aa6-de4c-4013-b455-eb2a91fb2b76\"}";
			courses.append(course);
			courses.append(comma);
		}
		return head + courses.deleteCharAt(courses.length()-1).toString() + tail;
				
	}
	
	public static List<CSMOEntry> buildEntries(Map<Set<String>, Set<String>> dt) {
		List<CSMOEntry> l = new ArrayList<>();
		for(Entry<Set<String>, Set<String>> e : dt.entrySet()) {
			StringBuilder title = new StringBuilder(e.getKey().toString());
			title.deleteCharAt(title.length()-1).deleteCharAt(0);			
			CSMOEntry c = new CSMOEntry(title.toString());
			boolean[] meetdays = new boolean[] {false, false, false, false, false, false, false};
        	int startHour = 0;
        	int startMin = 0;
        	int endHour = 0;
        	int endMin = 0;
			for (String dti : e.getValue()) {
				Pattern pattern = Pattern.compile(REGEX_DT_SINGLE);
				Matcher matcher = pattern.matcher(dti);
				while (matcher.find()) {
					meetdays[DAYS_MAP.get(matcher.group(1))] = true;
					startHour = Integer.parseInt(matcher.group(2));
					startMin = Integer.parseInt(matcher.group(3));
					endHour = Integer.parseInt(matcher.group(4));
					endMin = Integer.parseInt(matcher.group(5));
					// System.out.println("DAY: " + matcher.group(1));
					// System.out.println("START HOUR: " + matcher.group(2));
					// System.out.println("START MIN: " + matcher.group(3));
					// System.out.println("END HOUR:" + matcher.group(4));
					// System.out.println("END MIN: " + matcher.group(5));
				}
			}
		
			c.setMeetdays(meetdays);
			c.setStartMinute(startMin);
			c.setEndMinute(endMin);
			if(startHour < 12) {
				c.setStartIsAM(true);
				c.setStartHour(startHour);				
			} else {
				c.setStartIsAM(false);
				c.setStartHour(startHour == 12? startHour : startHour - 12);
			}
			
			if(endHour < 12) {
				c.setEndIsAM(true);
				c.setEndHour(endHour);				
			} else {
				c.setEndIsAM(false);
				c.setEndHour(endHour == 12? endHour : endHour - 12);
			}
			
			l.add(c);			
		}
		
		return l;
	}
	
	public static Map<String, Set<String>> makeDtMap(List<String[]> validRecords) {
		Map<String, Set<String>> dtm = new HashMap<>();
		for(String[] r : validRecords) {
			if(!dtm.containsKey(r[1])) {
				dtm.put(r[1], new HashSet<String>());
			}			
			dtm.get(r[1]).add(r[0]);
		}
		return dtm;		
	}
	
	public static Map<Set<String>, Set<String>> mapSToDt(Map<String, Set<String>> dtm) {
		Map<Set<String>, Set<String>> sm = new HashMap<>();
		for(Set<String> subj: dtm.values()) {
			if(!sm.containsKey(subj)) {
				sm.put(subj, new HashSet<String>());
			}
			for(String time: dtm.keySet()) {
				if(dtm.get(time).equals(subj)) {
					sm.get(subj).add(time);
				}
			}
		}
		//System.out.println(sm);
		return sm;

	}
    
	public static void readCSV(String path, List<String[]> validRecords) {
		try (Reader in = new FileReader(path)) {
			final Iterable<CSVRecord> records = CSV_FORMAT.parse(in);

			for (CSVRecord record : records) {
				
				if (NUMBER_OF_CSV_FIELDS != record.size()) {
					// invalid record found					
					final StringBuilder recordValue = new StringBuilder();
					record.forEach(r -> recordValue.append(r).append(","));					
					System.out.print("Invalid record found and skipped: " );					
					record.forEach(r -> System.out.print(r + ","));
					System.out.println();
				} else {

					String cn = record.get(0).trim();
					String ct = record.get(7).trim();
					
					if(cn == null || cn.isEmpty() || ct == null || ct.isEmpty()) {
						System.out.print("Invalid record found and skipped: " );
						record.forEach(r -> System.out.print(r + ","));
						System.out.println();
					} else {
						
						
					
						if(Pattern.matches(REGEX_DT, ct)) {
							if(Pattern.matches(REGEX_DT_DBL, ct)) {
								String r1 = ct.replaceAll(REGEX_DT_DBL, "$1 $3");
								String r2 = ct.replaceAll(REGEX_DT_DBL, "$2 $3");
								//System.out.println(cn.replaceAll(REGEX_CN, "$1") + " " + r1);
								//System.out.println(cn.replaceAll(REGEX_CN, "$1") + " " + r2);
								validRecords.add(new String[] {cn.replaceAll(REGEX_CN, "$1"), r1});
								validRecords.add(new String[] {cn.replaceAll(REGEX_CN, "$1"), r2});
							}
							else {
								validRecords.add(new String[] {cn.replaceAll(REGEX_CN, "$1"), ct});
							}
						}
					}
					
				}
			}
		} catch (IOException e) {
		}
	}
	

    
}
