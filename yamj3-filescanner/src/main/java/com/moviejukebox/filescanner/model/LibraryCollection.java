package com.moviejukebox.filescanner.model;

import com.moviejukebox.common.dto.ImportDTO;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Service;

@Service("libraryCollection")
public class LibraryCollection {

    private static final Logger LOG = LoggerFactory.getLogger(LibraryCollection.class);
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;
    private List<Library> libraries;
    private String defaultPlayerPath = "";
    private String defaultClient = "";

    public LibraryCollection() {
        libraries = new ArrayList<Library>();
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    /**
     * Add a library to the collection
     *
     * @param library
     */
    public void add(Library library) {
        libraries.add(library);
    }

    /**
     * Remove a library entry from the collection
     *
     * @param library
     */
    public void remove(Library library) {
        libraries.remove(library);
    }

    /**
     * Remove libraries from the collection by path
     *
     * @param path
     */
    public void remove(String path) {
        List<Library> toRemove = new ArrayList<Library>();
        for (Library library : libraries) {
            if (library.getImportDTO().getBaseDirectory().equalsIgnoreCase(path)) {
                toRemove.add(library);
            }
        }
        libraries.removeAll(toRemove);
    }

    /**
     * Get the list of libraries
     *
     * @return
     */
    public List<Library> getLibraries() {
        return libraries;
    }

    /**
     * Clear the list of libraries
     */
    public void clear() {
        libraries.clear();
    }

    /**
     * Get the number of libraries in the collection
     *
     * @return
     */
    public int size() {
        return libraries.size();
    }

    /**
     * Read the library files (multiple) / command line properties and set up the library
     *
     * @param libraryFilenames The name and location of the library files to process
     * @param directoryProperty A single directory from the command line
     * @param defaultWatchState The default watch status
     */
    public void processLibraryList(List<String> libraryFilenames, boolean defaultWatchState) {
        LOG.info("Library files: {}", libraryFilenames);
        LOG.info("Default watch: {}", defaultWatchState);

        // Process the library files
        for (String singleLibrary : libraryFilenames) {
            processLibraryFile(singleLibrary, defaultWatchState);
        }
    }

    /**
     * Read the library file from disk and return the library object
     *
     * @param libraryFilename the file to read
     * @param defaultWatchState the default watched state if not provided in the file
     * @return
     */
    public void processLibraryFile(String libraryFilename, boolean defaultWatchState) {
        LOG.warn("processLibraryFile - Not supported yet.");
        LOG.warn("Library Filename    : {}", libraryFilename);
        LOG.warn("Default watch state : {}", defaultWatchState);

        // http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/oxm.html
        FileInputStream is = null;
        try {
            is = new FileInputStream(libraryFilename);
            Library library = (Library) this.unmarshaller.unmarshal(new StreamSource(is));
            add(library);
        } catch (FileNotFoundException ex) {
            LOG.warn("File not found: {}", libraryFilename);
        } catch (IOException ex) {
            LOG.warn("IO exception for: {}, Error: {}", libraryFilename, ex.getMessage());
        } catch (XmlMappingException ex) {
            LOG.warn("XML Mapping error for: {}, Error: {}", libraryFilename, ex.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    LOG.warn("Failed to close library file: {}, Error: {}", libraryFilename, ex.getMessage());
                }
            }
        }
    }

    public void saveLibraryToFile(String libraryFilename, Library library) {
        LOG.info("Attempting to save library to {}", libraryFilename);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(libraryFilename);
            this.marshaller.marshal(library, new StreamResult(os));
        } catch (FileNotFoundException ex) {
            LOG.warn("File not found: {}", libraryFilename);
        } catch (IOException ex) {
            LOG.warn("IO exception for: {}, Error: {}", libraryFilename, ex.getMessage());
        } catch (XmlMappingException ex) {
            LOG.warn("XML Mapping error for: {}, Error: {}", libraryFilename, ex.getMessage());
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                    LOG.warn("Failed to close library file: {}, Error: {}", libraryFilename, ex.getMessage());
                }
            }
        }
        LOG.info("Saving completed");
    }

    public void addLibraryDirectory(String baseDirectory, boolean defaultWatchState) {
        addLibraryDirectory(baseDirectory, defaultWatchState, defaultPlayerPath, defaultClient);
    }

    public void addLibraryDirectory(String baseDirectory, boolean defaultWatchState, String playerPath, String client) {
        Library library = new Library();

        // Set up the ImportDTO
        ImportDTO importDto = new ImportDTO();
        importDto.setBaseDirectory(baseDirectory);
        importDto.setClient(client);
        importDto.setPlayerPath(playerPath);

        library.setImportDTO(importDto);
        library.setWatch(defaultWatchState);
        add(library);
    }

    public String getDefaultPlayerPath() {
        return defaultPlayerPath;
    }

    public void setDefaultPlayerPath(String defaultPlayerPath) {
        this.defaultPlayerPath = defaultPlayerPath;
    }

    public String getDefaultClient() {
        return defaultClient;
    }

    public void setDefaultClient(String defaultClient) {
        this.defaultClient = defaultClient;
    }
}
