package ourneighborschild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import au.com.bytecode.opencsv.CSVWriter;

public class NoteDB extends ONCSearchableDatabase
{
	private static final EntityType DB_TYPE = EntityType.NOTE;
	private static NoteDB instance = null;
	private List<ONCNote> noteList;
	private List<ONCNote> filteredNoteList;
	private int filterID;
	
	private NoteDB()
	{
		super(DB_TYPE);
		this.noteList = new ArrayList<ONCNote>();
		this.filteredNoteList = new ArrayList<ONCNote>();
		this.filterID = -1;
	}
	
	public static NoteDB getInstance()
	{
		if(instance == null)
			instance = new NoteDB();
		
		return instance;
	}
	
	@Override
	int size() { return filteredNoteList.size(); }
	
	@Override
	ONCEntity getObjectAtIndex(int index)
	{
		return index < filteredNoteList.size() ? filteredNoteList.get(index) : null; 
	}
	
	@Override
	List<? extends ONCEntity> getList() { return filteredNoteList; }
	
	/***
	 * Method sets the filter. The filter is an integer used to filter objects in the
	 * database to a filtered list. The filter operates on one field only. In the case of
	 * this Note DB the filter operates on the note owner field. 
	 * 
	 * @param filterID
	 */
	void setFilter(int filterID)
	{
		this.filterID = filterID;
		updateFilteredNoteList();
	}
	
	void updateFilteredNoteList()
	{
		filteredNoteList.clear();
		
		if(filterID > -1)
		{
			for(ONCNote n : noteList)
				if(n.getOwnerID() == filterID)
					filteredNoteList.add(n);
		}
		
		Collections.sort(filteredNoteList, new ONCNoteTimeCreatedComparator());
	}
	
	ONCNote getNote(int id)
	{
		int index = 0;
		while(index < noteList.size() && noteList.get(index).getID() != id)
			index++;
		
		return index < noteList.size() ? noteList.get(index) : null;
	}
	
	//Used to create a note internal to the application via the ONC Server
	ONCObject add(Object source, ONCObject entity)
	{	
		ONCObject addedNote = null;
					
		//send add request to server
		String response = "";
					
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			response = serverIF.sendRequest("POST<add_note>" + gson.toJson(entity, ONCNote.class));
					
			//if the server added the note,  add the new note to the data base and notify ui's
			if(response.startsWith("ADDED_NOTE"))		
				addedNote =  processAddedNote(source, response.substring(10));
		}
					
		return addedNote;
	}
				
	ONCObject processAddedNote(Object source, String json)
	{
		ONCNote addedNote = null;
		Gson gson = new Gson();
		addedNote = gson.fromJson(json, ONCNote.class);
				
		if(addedNote != null)
		{	
			noteList.add(addedNote);
			updateFilteredNoteList();
			fireDataChanged(source, "ADDED_NOTE", addedNote);
		}
				
			return addedNote;
	}
	/***************************************************************
	 * This method is called when a note has been updated by 
	 * the user. The request updated note object is passed. 
	 *************************************************************/
	@Override
	String update(Object source, ONCObject updatedNote)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		response = serverIF.sendRequest("POST<update_note>" + gson.toJson(updatedNote, ONCNote.class));
		
		//check response. If response from server indicates a successful update,
		//create and store the updated note in the local data base and notify local
		//ui listeners of a change.
		if(response.startsWith("UPDATED_NOTE"))
			processUpdatedNote(source, response.substring(12));
		
		return response;
	}
	
	void processUpdatedNote(Object source, String json)
	{
		//Create a ONCNote object for the updated note
		Gson gson = new Gson();
		ONCNote updatedNote = gson.fromJson(json, ONCNote.class);
		
		//Find the position for the current note being updated
		int index = 0;
		while(index < noteList.size() &&  noteList.get(index).getID() != updatedNote.getID())
			index++;
		
		//Replace the current ONCNote object with the update
		if(index <  noteList.size())
		{
			noteList.set(index, updatedNote);
			updateFilteredNoteList();
			fireDataChanged(source, "UPDATED_NOTE", updatedNote);
		}
		else
			System.out.println(String.format("Note DB processUpdatedNOte - note id %d not found",
					updatedNote.getID()));
	}
	
	/*******************************************************************************************
	 * This method is called when the user requests to delete a note.
	 **********************************************************************************************************/
	ONCNote delete(Object source, ONCNote reqDelNote)
	{
		String response = "";
		ONCNote deletedNote = null;
		
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			
			response = serverIF.sendRequest("POST<delete_note>" + gson.toJson(reqDelNote, ONCNote.class));		
			
			//if the server deleted the note, notify ui's
			if(response.startsWith("DELETED_NOTE"))		
				deletedNote = processDeletedNote(source, response.substring(12));
		}
		
		return deletedNote;
	}
	
	ONCNote processDeletedNote(Object source, String json)
	{
		//remove the note from this local data base
		Gson gson = new Gson();
		ONCNote deletedNote = removeNote(source, gson.fromJson(json, ONCNote.class).getID());
		
		if(deletedNote != null)
			fireDataChanged(source, "DELETED_NOTE", deletedNote);
		
		return deletedNote;
	}
	
	ONCNote removeNote(Object source, int noteID)
	{
		//remove the note from this data base
		ONCNote deletedNote = null;
		
		int index = 0;
		while(index < noteList.size() && noteList.get(index).getID() != noteID)
				index++;
				
		if(index < noteList.size())
		{
			deletedNote = noteList.get(index);
			noteList.remove(index);
			updateFilteredNoteList();
		}
		
		return deletedNote;
	}
	String exportDBToCSV(JFrame pf, String filename)
    {
		File oncwritefile = null;
		
		if(filename == null)
    		{
    			ONCFileChooser fc = new ONCFileChooser(pf);
    			oncwritefile= fc.getFile("Select .csv file to save Notet DB to",
								new FileNameExtensionFilter("CSV Files", "csv"), ONCFileChooser.SAVE_FILE);
    		}
    		else
    			oncwritefile = new File(filename);
    	
		if(oncwritefile!= null)
    		{
    			//If user types a new filename and doesn't include the .csv, add it
	    		String filePath = oncwritefile.getPath();		
	    		if(!filePath.toLowerCase().endsWith(".csv")) 
	    			oncwritefile = new File(filePath + ".csv");
	    	
	    		try 
	    		{
	    			String[] header = {"Note ID", "Owner ID", "Status", "Title", "Note", "Changed By",
	    					"Response", "Response By", "Time Created", "Time Viewed",
	    					"Time Responded", "Stoplight Pos", "Stoplight Mssg", "Stoplight C/B"};
	    			
	    		
	    			CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    			writer.writeNext(header);
	    	    
	    			for(ONCNote n : noteList)
	    				writer.writeNext(n.getExportRow());	//Get family data
	    	 
	    			writer.close();
	    			filename = oncwritefile.getName();
	    	       	    
	    		} 
	    		catch (IOException x)
	    		{
	    			System.err.format("IO Exception: %s%n", x);
	    			JOptionPane.showMessageDialog(pf, oncwritefile.getName() + " could not be saved", 
						"ONC File Save Error", JOptionPane.ERROR_MESSAGE);
	    		}
	    }
    	
	    return filename;
    }
	
	List<ONCNote> getNotesForFamily(int ownerID)
	{
		if(filterID > -1 && ownerID == filterID)
			return filteredNoteList;
		else
		{
			List<ONCNote> noteList = new ArrayList<ONCNote>();
			for(ONCNote n : noteList)
				if(n.getOwnerID() == ownerID)
					noteList.add(n);
					
			Collections.sort(noteList, new ONCNoteTimeCreatedComparator());
			
			return noteList;
		}		 
	}
	
	//take advantage of the fact the list of notes if saved in time order. Search 
	//from the bottom for the first note for the family. If no note, return null
	ONCNote getLastNoteForFamily(int ownerID)
	{
		int index = noteList.size()-1;
		while(index >= 0 && noteList.get(index).getOwnerID() != ownerID)
			index--;
			
		return index >= 0 ? noteList.get(index) : null;
	}
	
	String importDB()
	{
		String response = "NO_NOTES";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCNote>>(){}.getType();
			
			response = serverIF.sendRequest("GET<notes>");
			noteList = gson.fromJson(response, listtype);
			
			if(!response.startsWith("NO_NOTES"))
			{
				response =  "NOTES_LOADED";
			}	fireDataChanged(this, "LOADED_NOTES", null);
		}
		
		return response;
	}
	
	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("UPDATED_NOTE"))
		{
			processUpdatedNote(this, ue.getJson());
		}
		else if(ue.getType().equals("ADDED_NOTE"))
		{
			processAddedNote(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_NOTE"))
		{
			processDeletedNote(this, ue.getJson());
		}			
	}

	@Override
	String searchForListItem(ArrayList<Integer> searchAL, String data)
	{
		searchAL.clear();
		
		//Determine the type of search based on characteristics of search string
		for(ONCNote note : filteredNoteList)
			if(note.getTitle().toLowerCase().contains(data.toLowerCase()) ||
			    note.getNote().toLowerCase().contains(data.toLowerCase()) ||
			     note.getResponse().toLowerCase().contains(data.toLowerCase()))
		{
				searchAL.add(note.getID());
		}
		
		return "Text";
	}
	
	private class ONCNoteTimeCreatedComparator implements Comparator<ONCNote>
	{
		@Override
		public int compare(ONCNote o1, ONCNote o2)
		{
			if(o1.getDateChanged().compareTo(o2.getDateChanged()) == 0) 
	            return 0;
	        else if(o1.getDateChanged().after(o2.getDateChanged()))
	        		return -1;
	        else
	        		return 1;
		}
	}
}
