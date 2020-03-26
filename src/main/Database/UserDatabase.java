package main.Database;

import org.jetbrains.annotations.NotNull;
import java.sql.*;

public class UserDatabase {
    private static int userId = 0;

    public void createRegistrationTable() throws SQLException {
        createStatement().executeUpdate(createRegistrationTableSQL());
    }

    public void addUserToRegistrationTable(String username, String password) throws SQLException {
        ResultSet resultSet = ProjectConnectionPool.getInstance().createResultSet(selectDataFromRegistrationTable());

        while(resultSet.next()) {
            if(resultSet.getString(2).equals(username))
                throw new RuntimeException();
        }

        createStatement().executeUpdate(addUserToRegistrationTableSQL(username, password));
        createUserPlaylist(username);
    }

    public boolean checkUser(String username, String password) throws SQLException {
        createRegistrationTable();
        ResultSet resultSet = ProjectConnectionPool.getInstance().createResultSet(checkUserSQL(username, password));
        resultSet.next();
        return resultSet.getBoolean(1);
    }

    public ResultSet restoreUserDataResultSet(String username) throws SQLException {
        setUser(username);
        return ProjectConnectionPool.getInstance().createResultSet(selectDataFromUserPlaylistSQL());
    }

    public void createUserPlaylist(String username) throws SQLException {
        setUser(username);
        createStatement().executeUpdate(createUserPlaylistSQL());
    }

    public ResultSet setListResultSet(int userChoice) throws SQLException{
        createStatement().executeUpdate(addSongToUserPlaylistSQL(userChoice));
        return ProjectConnectionPool.getInstance().createResultSet(selectDataFromUserPlaylistSQL());
    }

    private void setUser(String username) throws SQLException {
        ResultSet resultSet = ProjectConnectionPool.getInstance().createResultSet(selectDataFromRegistrationTable());

        while(resultSet.next()) {
            if(resultSet.getString(2).equals(username))
                userId = resultSet.getInt(1);
        }
    }

    private Statement createStatement() throws SQLException {
        return ProjectConnectionPool.getInstance().getConnection().createStatement();
    }

    @NotNull
    private String createRegistrationTableSQL() {
        return "CREATE TABLE IF NOT EXISTS `awesomePlaylist`.`registrationTable` (`usernameID` INT NOT NULL AUTO_INCREMENT, `username` VARCHAR(50) NOT NULL, `password` VARCHAR(50) NOT NULL, PRIMARY KEY (`usernameID`), UNIQUE INDEX `userID_UNIQUE` (`usernameID` ASC) VISIBLE)";
    }

    @NotNull
    private String addUserToRegistrationTableSQL(String username, String password) {
        return "INSERT INTO awesomePlaylist.registrationTable (usernameID, username, password) SELECT NULL, '"  + username + "', '" + password + "' FROM DUAL WHERE NOT EXISTS (SELECT * FROM awesomePlaylist.registrationTable WHERE usernameID = NULL AND username = '" + username + "' AND password = '" + password + "')";
    }

    @NotNull
    private String checkUserSQL(String username, String password) {
        return "SELECT EXISTS (SELECT * FROM awesomePlaylist.registrationTable WHERE username = '" + username + "' AND password = '" + password + "') LIMIT 1";
    }

    @NotNull
    private String createUserPlaylistSQL() {
        return "CREATE TABLE IF NOT EXISTS `awesomePlaylist`.`" + userId + "` (`ID` INT NOT NULL AUTO_INCREMENT, `" + userId + "SongID` INT NOT NULL, INDEX `songID_idx` (`" + userId + "SongID` ASC) VISIBLE, INDEX `ID` (`ID` ASC) VISIBLE, CONSTRAINT `" + userId + "SongID` FOREIGN KEY (`" + userId + "SongID`) REFERENCES `awesomePlaylist`.`songs` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE)";
    }

    @NotNull
    private String addSongToUserPlaylistSQL(Integer userChoice) {
        return "INSERT INTO awesomePlaylist." + userId + " VALUES (NULL, " + userChoice + ")";
    }

    @NotNull
    private String selectDataFromUserPlaylistSQL() {
        return "SELECT awesomePlaylist." + userId + ".ID, title, artistName, albumName, year FROM awesomePlaylist." + userId + " LEFT JOIN awesomePlaylist.songs ON (awesomePlaylist." + userId + "." + userId + "songID = awesomePlaylist.songs.ID) LEFT JOIN awesomePlaylist.artists ON (awesomePlaylist.songs.artistID = awesomePlaylist.artists.ID)";
    }

    @NotNull
    private String selectDataFromRegistrationTable() {
        return "SELECT * FROM awesomePlaylist.registrationTable";
    }
}