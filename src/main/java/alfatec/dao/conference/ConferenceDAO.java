package alfatec.dao.conference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;

import alfatec.dao.utils.TableUtility;
import alfatec.model.conference.Conference;
import database.DatabaseTable;
import database.Getter;
import javafx.collections.ObservableList;
import util.BooleanUtil;

/**
 * DAO for table "conference".
 * 
 * Double-checked locking in singleton.
 * 
 * @author jelena
 *
 */
public class ConferenceDAO {

	private static ConferenceDAO instance;

	public static ConferenceDAO getInstance() {
		if (instance == null)
			synchronized (ConferenceDAO.class) {
				if (instance == null)
					instance = new ConferenceDAO();
			}
		return instance;
	}

	private final TableUtility table;

	private Getter<Conference> getConference;

	private ConferenceDAO() {
		table = new TableUtility(new DatabaseTable("conference", "conference_id", new String[] { "conference_title",
				"conference_email", "email_password", "conference_bcc", "note", "report", "field_id", "realized" }));
		getConference = new Getter<Conference>() {
			@Override
			public Conference get(ResultSet rs) {
				Conference conference = new Conference();
				try {
					conference.setConferenceID(rs.getInt(table.getTable().getPrimaryKey()));
					conference.setConferenceTitle(rs.getString(table.getTable().getColumnName(1)));
					conference.setConferenceEmail(rs.getString(table.getTable().getColumnName(2)));
					conference.readPasswordFromDB(rs.getString(table.getTable().getColumnName(3)));
					conference.setConferenceBcc(rs.getString(table.getTable().getColumnName(4)));
					conference.setNote(rs.getString(table.getTable().getColumnName(5)));
					Path path = Paths.get("src/resources/file/" + conference.getConferenceTitle());
					if (Files.notExists(path)) {
						File file = new File("src/resources/file/" + conference.getConferenceTitle());
						InputStream blob = rs.getBinaryStream(table.getTable().getColumnName(6));
						if (blob != null)
							FileUtils.copyInputStreamToFile(blob, file);
						conference.setReport(file);
						file.deleteOnExit();
					}
					conference.setFieldID(rs.getInt(table.getTable().getColumnName(7)));
					conference.setRealized(BooleanUtil.parse(rs.getBoolean(table.getTable().getColumnName(8))));
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
				return conference;
			}
		};
	}

	public void deleteConference(Conference conference) {
		table.delete(conference.getConferenceID());
	}

	public void endConference() {
		table.update(getCurrentConference().getConferenceID(), 8, 1);
		getCurrentConference().setRealized(1);
	}

	/**
	 * Search table by primary key - id
	 */
	public Conference findBy(int id) {
		return table.findBy(id, getConference);
	}

	/**
	 * Full text search in natural language by title
	 */
	public Conference findBy(String title) {
		return table.findByFulltext(title, getConference, 1);
	}

	/**
	 * @return all conferences
	 */
	public ObservableList<Conference> getAll() {
		return table.getAll(getConference);
	}

	/**
	 * @param fieldID
	 * @return all conferences tied to specified field
	 */
	public ObservableList<Conference> getAllForField(int fieldID) {
		return table.findBy(fieldID, 7, getConference);
	}

	/**
	 * @param field name of the field
	 * @return all conferences tied to specified field
	 */
	public ObservableList<Conference> getAllForField(String field) {
		return getAllForField(FieldDAO.getInstance().getField(field).getFieldID());
	}

	/**
	 * @return all realized - finished conferences
	 */
	public ObservableList<Conference> getAllRealized() {
		return table.findBy(1, 8, getConference);
	}

	/**
	 * Search table by primary key - id
	 */
	public Conference getConference(int id) {
		return table.findBy(id, getConference);
	}

	/**
	 * @return current conference
	 */
	public Conference getCurrentConference() {
		try {
			return table.findBy(0, 8, getConference).get(0);
		} catch (Exception e) {
			return null;
		}
	}

	public Conference startConference(String title, String field, String email, String password, String bcc,
			String note, String reportPath) {
		return table.create(reportPath, 7, new String[] { title, email, password, bcc, note },
				new int[] { FieldDAO.getInstance().getField(field).getFieldID(), 0 }, new long[] {}, getConference);
	}

	public void updateConferenceEmail(String email) {
		table.update(getCurrentConference().getConferenceID(), 2, email);
		getCurrentConference().setConferenceEmail(email);
	}

	public void updateConferenceField(int fieldID) {
		table.update(getCurrentConference().getConferenceID(), 7, fieldID);
		getCurrentConference().setFieldID(fieldID);
	}

	public void updateConferenceField(String field) {
		updateConferenceField(FieldDAO.getInstance().getField(field).getFieldID());
	}

	public void updateConferenceNote(String note) {
		table.update(getCurrentConference().getConferenceID(), 5, note);
		getCurrentConference().setNote(note);
	}

	public void updateConferencePassword(String password) {
		table.update(getCurrentConference().getConferenceID(), 3, password);
		getCurrentConference().setConferenceEmailPassword(password);
	}

	public void updateConferenceReport(String filePath) {
		table.updateBlob(getCurrentConference().getConferenceID(), 6, filePath);
		getCurrentConference().setReportFile(filePath);
	}

	public void updateConferenceTitle(String title) {
		table.update(getCurrentConference().getConferenceID(), 1, title);
		getCurrentConference().setConferenceTitle(title);
	}

	public void updateConferenceBcc(String conferenceBcc) {
		table.update(getCurrentConference().getConferenceID(), 4, conferenceBcc);
		getCurrentConference().setConferenceBcc(conferenceBcc);
	}

}
