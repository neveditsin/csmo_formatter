import java.util.Arrays;

public class CSMOEntry {
	@Override
	public String toString() {
		return "CSMOEntry [title=" + title + ", startHour=" + startHour + ", startMinute=" + startMinute
				+ ", startIsAM=" + startIsAM + ", endHour=" + endHour + ", endMinute=" + endMinute + ", endIsAM="
				+ endIsAM + ", meetdays=" + Arrays.toString(meetdays) + "]";
	}
	public CSMOEntry(String title) {
		super();
		this.title = title;
	}
	private String title;
	private int startHour;
	private int startMinute;
	private boolean startIsAM = false;
	private int endHour;
	private int endMinute;
	private boolean endIsAM = false;
	private boolean[] meetdays = new boolean[] {false, false, false, false, false, false, false};
	
	public boolean[] getMeetdays() {
		return meetdays;
	}
	public void setMeetdays(boolean[] meetdays) {
		this.meetdays = meetdays;
	}
	public int getStartHour() {
		return startHour;
	}
	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}
	public int getStartMinute() {
		return startMinute;
	}
	public void setStartMinute(int startMinute) {
		this.startMinute = startMinute;
	}
	public boolean isStartIsAM() {
		return startIsAM;
	}
	public void setStartIsAM(boolean startIsAM) {
		this.startIsAM = startIsAM;
	}
	public int getEndHour() {
		return endHour;
	}
	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}
	public int getEndMinute() {
		return endMinute;
	}
	public void setEndMinute(int endMinute) {
		this.endMinute = endMinute;
	}
	public boolean isEndIsAM() {
		return endIsAM;
	}
	public void setEndIsAM(boolean endIsAM) {
		this.endIsAM = endIsAM;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	

}
